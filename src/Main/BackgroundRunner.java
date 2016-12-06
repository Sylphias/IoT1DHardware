package Main;

import HttpRequests.HttpRequester;
import Utilities.MSBlobUploader;
import com.google.gson.Gson;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.bytedeco.javacpp.opencv_core.cvFlip;
import static org.bytedeco.javacpp.opencv_imgcodecs.cvSaveImage;

import java.io.*;
import jssc.*;

/**
 * Created by Ilya on 6/12/16.
 */
public class BackgroundRunner {
    public static void main(String[] args) {
        startServer();
    }

    public static void startServer(){
        Integer id = 001;
        Integer person_id = 001;
        getEmotionData();
        getTotemData();
        getSeatData();

    }

    public static void getEmotionData(){
        HttpRequester req = new HttpRequester();

        opencv_core.IplImage image;
//        System.out.println(HttpRequester.requester("http://jsonplaceholder.typicode.com/posts/","","GET").get("id"));
        FrameGrabber grab = new OpenCVFrameGrabber(0);

        Gson gson = new Gson();
        int i = 0;
        String id = System.getProperty("user.name");


//        HttpRequester.generalRequester("iotfocus.herokuapp.com/person/",)
//        HttpRequester.generalRequester("iotfocus.herokuapp.com/person/all_people")
        try {
            grab.start();
            opencv_core.IplImage img;
            OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();
            OpenCVFrameConverter.ToMat converter2 = new OpenCVFrameConverter.ToMat();
            Pattern p = Pattern.compile("url:");
            while (true) {
                img = converter.convert(grab.grab());
                opencv_core.Mat mat = new opencv_core.Mat();
                String current = new java.io.File( "." ).getCanonicalPath();
                if (img != null) {
                    cvFlip(img, img, 1);
                    cvSaveImage(current+"/img/capture"+id+".jpg", img);
//                  show image on window
                    MSBlobUploader.initializeContainer();
                    String URL = MSBlobUploader.createBlob("testBlob",id);
                    Matcher m = p.matcher("URL");
                    if(m.matches()){
                        URL = URL.substring(m.end(),URL.length()-1);
                    }
                    HttpRequester.emotionRequester("{\"url\":\""+URL+"\"}","POST",id);
                }
                Thread.sleep(5000);
            }

        } catch (Exception e) {
            System.out.println(e);
        }

    }

    private static SerialPort serialPort0;

    private static SerialPort serialPort1;

    public static void getTotemData(){

        HttpRequester totemReq = new HttpRequester();

        String[] portNames = SerialPortList.getPortNames();

        if (portNames.length == 0) {
            System.out.println("There are no serial ports.");
            System.out.println("Press Enter to exit...");
            try {
                System.in.read();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        System.out.println("Available COM ports:");
        for (int i = 0; i < portNames.length; i++){
            System.out.println(portNames[i]);
        }
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

        HttpRequester seatReq = new HttpRequester();

        String seatSensorPort = "COM11";

        serialPort1 = new SerialPort(seatSensorPort);

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
                    System.out.println("TOTEM " + receivedDataPomodoro);
                    int i  = Integer.parseInt(receivedDataPomodoro);
                    String state = "Standby";
                    switch(i){
                        case 0:
                            state = "Email";
                        case 1:
                            state = "Break";
                        case 2:
                            state = "Code";
                        case 3:
                            state = "Meeting";
                        case 4:
                            state = "Research";
                    }

                    HttpRequester totemReq = new HttpRequester();

                    HashMap totemParams = new HashMap();

                    totemParams.put("id",001);
                    totemParams.put("person_id",001);
                    totemParams.put("state",state);

                    String params = parameterfier(totemParams);



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
                }
                catch (SerialPortException ex) {
                    System.out.println("Error in receiving response from pomodoro port: " + ex);
                }
            }
        }
    }

    public static String parameterfier(HashMap<String,String> params){
        StringBuilder sb = new StringBuilder();
        sb.append("?");
        for(HashMap.Entry<String,String> entry: params.entrySet()){
            sb.append(entry.getKey()+"="+entry.getValue());
        }
        return sb.toString();
    }

}
