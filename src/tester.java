import HttpRequests.HttpRequester;
import javax.swing.*;

import Utilities.APIKeys;
import Utilities.MSBlobUploader;
import com.google.gson.Gson;
import org.bytedeco.javacv.*;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.indexer.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import static org.bytedeco.javacpp.opencv_imgcodecs.*;
/**
 * Created by Ilya on 7/11/16.
 */
public class tester implements Runnable{
    static CanvasFrame canvas = new CanvasFrame("Web Cam");
    //        HttpRequester.requester("http://localhost:3000/posts","  data: {\n" +
//                "    title: 'foo',\n" +
//                "    body: 'bar',\n" +
//                "    userId: 1\n" +
//                "  }","POST");

    public static void main(String[] args) {
        String msURL = "https://api.projectoxford.ai/emotion/v1.0/recognize";
        HttpRequester req = new HttpRequester();

        IplImage image;
//        System.out.println(HttpRequester.requester("http://jsonplaceholder.typicode.com/posts/","","GET").get("id"));
        canvas.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
        FrameGrabber grab = new OpenCVFrameGrabber(0);

        Gson gson = new Gson();
        int i = 0;
        String id = "1234";
        try {
            grab.start();
            IplImage img;
            OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();
            OpenCVFrameConverter.ToMat converter2 = new OpenCVFrameConverter.ToMat();
            Pattern p = Pattern.compile("url:");
            while (true) {
                img = converter.convert(grab.grab());
                Mat mat = new Mat();
                mat = converter2.convert(grab.grab());
                String current = new java.io.File( "." ).getCanonicalPath();
                BufferedImage imger = new BufferedImage(mat.cols(),mat.rows(),BufferedImage.TYPE_3BYTE_BGR);
                byte[] imgData = ((DataBufferByte) imger.getRaster().getDataBuffer()).getData();
                if (img != null) {
//                    Map<String,String> rawr = HttpRequester.requester(msURL,"",imgData,"POST");
                    cvFlip(img, img, 1);// l-r = 90_degrees_steps_anti_clockwise
                    cvSaveImage(current+"/img/capture"+id+".jpg", img);
//                  show image on window
                    MSBlobUploader.initializeContainer();
                    String URL = MSBlobUploader.createBlob("testBlob",id);
                    Matcher m = p.matcher("URL");
                    if(m.matches()){
                        URL = URL.substring(m.end(),URL.length()-1);
                    }

                    HttpRequester.emotionRequester("{\"url\":\""+URL+"\"}","POST");
                    canvas.showImage(grab.grab());
                }
                Thread.sleep(5000);
            }

        } catch (Exception e) {
            System.out.println(e);
        }

    }



    private static void createAndShowGUI(){
        JFrame frame = new JFrame("HelloWorldSwing");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        //Add the ubiquitous "Hello World" label.
        JLabel label = new JLabel("Hello World");
        frame.getContentPane().add(label);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    @Override
        public void run() {

        }
    }
