package Main;

import HttpRequests.HttpRequester;
import IoTData.EmotionData;
import IoTData.Person;
import IoTData.TotemData;
import Multithreader.Multithreader;
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
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import static HttpRequests.HttpRequester.parameterfier;
import static org.bytedeco.javacpp.opencv_core.cvFlip;
import static org.bytedeco.javacpp.opencv_imgcodecs.cvSaveImage;

/**
 * Created by Ilya on 6/12/16.
 */
public class BackgroundRunner {
    public static void main(String[] args) {

//        Scanner reader = new Scanner(System.in);  // Reading from System.in
//        System.out.println("Please enter the COM port of your seat sensor:");
//        seatCOM = reader.nextLine();

        if(args.length != 0){
            seatCOM = args[0];
            totemCOM = args[1];
        }
        else{
            seatCOM = "COM11";
            totemCOM = "COM8";
        }

//        System.out.println("Please enter the COM port of your totem:");
//        totemCOM = reader.nextLine(); // Scans the next token of the input as an int.

        startServer();
    }

    public static String seatCOM;
    public static String totemCOM;

    public static void startServer(){

        perss = initPerson();
        Multithreader emotion = new Multithreader("Emotion Sensor");
        Multithreader seat = new Multithreader("Seat Sensor");
        Multithreader totem = new Multithreader("Totem");
        emotion.start();
        seat.start();
        totem.start();
        //getEmotionData(UrlList.testUrl);


    }

    private static SerialPort serialPort0;
    private static SerialPort serialPort1;
    public static Person perss;

    public static Person initPerson(){

        Gson gson = new Gson();
        String name = System.getProperty("user.name");
        HashMap<String,String> personParams = new HashMap<String, String>();
        personParams.put("name",name);
        try {

            // Find if the person exists in the database from the username of the person
            String personResponse = HttpRequester.generalRequester(UrlList.APIUrl, "/person/by_name", personParams, "", "GET");
            Person pers;
            if (!personResponse.matches("null")) {
                pers = new Person(personResponse);
                return pers;
            }
            // If we cannot find a person we will create a person
            else {
                // Create a new person.
                Person p = new Person(0, name, 'U');
                String pjson = gson.toJson(p);
                String request = HttpRequester.generalRequester(UrlList.APIUrl, "/person", p.toHashMap(), pjson, "POST");
                System.out.println(request);
                pers = p;
                return pers;
            }
            }catch(Exception e) {

            System.out.println(e);
            System.exit(0);
        }

        return null;

    }

    public static void getEmotionData(String apiUrl){
        HttpRequester req = new HttpRequester();
        String id = System.getProperty("user.name");


        opencv_core.IplImage image;
        FrameGrabber grab = new OpenCVFrameGrabber(0);

        try{

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
                        if (perss != null) {
                            EmotionData ed = HttpRequester.emotionRequester("{\"url\":\"" + URL + "\"}", "POST", perss.getId());
                            if (ed != null) {
                                System.out.println("here"+ed.getPerson_id());

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
        String pomodoroPort = totemCOM;

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

            while(true){

            }

        }
        catch (SerialPortException ex) {
            System.out.println("Error writing data to totem port: " + ex);

        }

    }

    public static void getSeatData(){
        String seatsensorPort = seatCOM;

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


            while(true){

            }

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

                if (event.isRXCHAR() && event.getEventValue() > 0) {
                    try {
                        String receivedDataPomodoro = serialPort0.readString(event.getEventValue());
                        System.out.println("POMODORO " + receivedDataPomodoro);

                        HashMap totemParams = new HashMap();
                        Integer i = Integer.parseInt(receivedDataPomodoro);
                        String state = "Standby";

                        switch (i) {
                            case 0:
                                state = "Email";
                                break;
                            case 1:
                                state = "Meeting";
                                break;
                            case 2:
                                state = "Working";
                                break;
                            case 3:
                                state = "Break";
                                break;
                            case 4:
                                state = "Research";
                        }

                        totemParams.put("state", state);
                        totemParams.put("person_id", Integer.toString(perss.getId()));

                        String result = HttpRequester.generalRequester(UrlList.APIUrl, "/totemdatum", totemParams, "", "POST");
                        System.out.println(totemParams);
                        System.out.println(result);
//                    totemParams.put("id",001);
//                    totemParams.put("person_id",001);

//                    String params = parameterfier(totemParams);
//                    HttpRequester totemReq = new HttpRequester();
//                    totemReq.generalRequester("iotfocus.herokuapp.com/person/",params,"data","PUT");







                       // String result = HttpRequester.generalRequester(UrlList.APIUrl, "/totemdatum", totemParams, "", "POST");

                        //System.out.println(result);

                    } catch (SerialPortException ex) {
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
                    String is_sitting;

                    if (i == 1){
                        is_sitting = "true";
                    } else{
                        is_sitting = "false";
                    }

                    seatParams.put("is_sitting",is_sitting);
                    seatParams.put("person_id", Integer.toString(perss.getId()));

//                    seatParams.put("id",001);
//                    seatParams.put("person_id",001);

//                    HttpRequester totemReq = new HttpRequester();

                    String result = HttpRequester.generalRequester(UrlList.APIUrl, "/seatdatum", seatParams, "", "POST");

                    System.out.println(result);

                }
                catch (SerialPortException ex) {
                    System.out.println("Error in receiving response from seat port: " + ex);
                }
            }
        }
    }

}
