package Main;

import HttpRequests.HttpRequester;
import IoTData.EmotionData;
import IoTData.Person;
import Utilities.MSBlobUploader;
import Utilities.UrlList;
import com.google.gson.Gson;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static HttpRequests.HttpRequester.parameterfier;
import static org.bytedeco.javacpp.opencv_core.cvFlip;
import static org.bytedeco.javacpp.opencv_imgcodecs.cvSaveImage;

/**
 * Created by Ilya on 6/12/16.
 */
public class BackgroundRunner {
    public static void main(String[] args) {
        startServer();
    }

    public static void startServer(){
        getEmotionData(UrlList.testUrl);
        getTotemData();
        getSeatData();

    }

    private static SerialPort serialPort0;
    private static SerialPort serialPort1;

    public static void getEmotionData(String apiUrl){
        HttpRequester req = new HttpRequester();

        opencv_core.IplImage image;
        FrameGrabber grab = new OpenCVFrameGrabber(0);

        Gson gson = new Gson();
        int i = 0;
        String id = System.getProperty("user.name");
        HashMap<String,String> personParams = new HashMap<String, String>();
        personParams.put("name",id);
        try {

            // Find if the person exists in the database from the username of the person
            String personResponse = HttpRequester.generalRequester(apiUrl, "/person/by_name", personParams, "", "GET");
            Person pers = null;
            if (!personResponse.matches("null")) {
                pers = new Person(personResponse);
            }
            // If we cannot find a person we will create a person
            else {
                // Create a new person.
                Person p = new Person(0, id, 'U', "192.127.0.1");
                String pjson = gson.toJson(p);
                String request = HttpRequester.generalRequester(apiUrl, "/person", p.toHashMap(), pjson, "POST");
                System.out.println(request);
                pers = p;
            }
            try {
                grab.start();
                opencv_core.IplImage img;
                OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();
                OpenCVFrameConverter.ToMat converter2 = new OpenCVFrameConverter.ToMat();
                Pattern p = Pattern.compile("url:");
                while (true) {
                    img = converter.convert(grab.grab());
                    opencv_core.Mat mat = new opencv_core.Mat();
                    String current = new java.io.File(".").getCanonicalPath();
                    if (img != null) {
                        cvFlip(img, img, 1);
                        cvSaveImage(current + "/img/capture" + id + ".jpg", img);
                        MSBlobUploader.initializeContainer();
                        String URL = MSBlobUploader.createBlob("testBlob", id);
                        Matcher m = p.matcher("URL");
                        if (m.matches()) {
                            URL = URL.substring(m.end(), URL.length() - 1);
                        }
                        if (pers != null) {
                            EmotionData ed = HttpRequester.emotionRequester("{\"url\":\"" + URL + "\"}", "POST", pers.getId());
                            if (ed != null) {
                                ed.setFeeling(ed.getHighestEmotion());
                                String result = HttpRequester.generalRequester(apiUrl, "/emotiondatum", ed.toHashMap(), "", "POST");
                                System.out.println(result);
                            }
                        }
                    }
                    Thread.sleep(5000);
                }
            } catch (Exception e) {
                System.out.println(e);
            }
        }catch(Exception e){
            System.out.println("Failure to connect");
        }

    }

    public static void getTotemData(){
        String pomodoroPort = "COM9";

        serialPort0 = new SerialPort(pomodoroPort);

        try {
            serialPort0.openPort();
            System.out.println("Totem opened");

            serialPort0.setParams(SerialPort.BAUDRATE_9600,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);

            serialPort0.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN |
                    SerialPort.FLOWCONTROL_RTSCTS_OUT);

            serialPort0.addEventListener(new PortReader0(), SerialPort.MASK_RXCHAR);

        }
        catch (SerialPortException ex) {
            System.out.println("Error writing data to totem port: " + ex);
        }

    }

    public static void getSeatData(){
        String seatsensorPort = "COM11";

        serialPort1 = new SerialPort(seatsensorPort);

        try {
            serialPort1.openPort();
            System.out.println("Seat sensor opened");

            serialPort1.setParams(SerialPort.BAUDRATE_9600,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);

            serialPort1.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN |
                    SerialPort.FLOWCONTROL_RTSCTS_OUT);

            serialPort1.addEventListener(new PortReader1(), SerialPort.MASK_RXCHAR);
        }
        catch (SerialPortException ex) {
            System.out.println("Error writing data to seat sensor port: " + ex);
        }

    }

    public void sendEmotionData(double feeling){

    }

    private static class PortReader0 implements SerialPortEventListener {

        @Override
        public void serialEvent(SerialPortEvent event) {
            if(event.isRXCHAR() && event.getEventValue() > 0) {
                try {
                    String receivedDataPomodoro = serialPort0.readString(event.getEventValue());
                    System.out.println("POMODORO " + receivedDataPomodoro);

                    HashMap totemParams = new HashMap();
                    Integer i = Integer.parseInt(receivedDataPomodoro);
                    String state = "Standby";

                    switch(i){
                        case 0:
                            state = "Email";
                        case 1:
                            state = "Meeting";
                        case 2:
                            state = "Coding";
                        case 3:
                            state = "Break";
                        case 4:
                            state = "Research";
                    }

                    totemParams.put("state",state);
                    totemParams.put("id",001);
                    totemParams.put("person_id",001);

                    String params = parameterfier(totemParams);
                    HttpRequester totemReq = new HttpRequester();
//                    totemReq.generalRequester("iotfocus.herokuapp.com/person/",params,"data","PUT");

                    System.out.println(receivedDataPomodoro+" sent to server");

                }
                catch (SerialPortException ex) {
                    System.out.println("Error in receiving response from totem port: " + ex);
                }
            }
        }
    }

    private static class PortReader1 implements SerialPortEventListener {

        @Override
        public void serialEvent(SerialPortEvent event) {
            if(event.isRXCHAR() && event.getEventValue() > 0) {
                try {
                    String receivedDataSeat = serialPort1.readString(event.getEventValue());
                    System.out.println("SEAT " + receivedDataSeat);

                    HashMap seatParams = new HashMap();
                    Integer i = Integer.parseInt(receivedDataSeat);
                    boolean is_sitting;

                    if (i == 1){
                        is_sitting = true;
                    } else{
                        is_sitting = false;
                    }

                    seatParams.put("is_sitting",is_sitting);
                    seatParams.put("id",001);
                    seatParams.put("person_id",001);

                    String params = parameterfier(seatParams);
                    HttpRequester totemReq = new HttpRequester();
//                    totemReq.generalRequester("iotfocus.herokuapp.com/person/",params,"data","PUT");

                    System.out.println(receivedDataSeat+" sent to server");

                }
                catch (SerialPortException ex) {
                    System.out.println("Error in receiving response from pomodoro port: " + ex);
                }
            }
        }
    }

}
