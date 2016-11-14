import HttpRequests.HttpRequester;
import javax.swing.*;

import org.bytedeco.javacv.*;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.indexer.*;
import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import static org.bytedeco.javacpp.opencv_imgcodecs.*;
/**
 * Created by Ilya on 7/11/16.
 */
public class tester implements Runnable{
    static CanvasFrame canvas = new CanvasFrame("Web Cam");
    public static void main(String[] args) {
//        HttpRequester.requester("http://localhost:3000/posts","  data: {\n" +
//                "    title: 'foo',\n" +
//                "    body: 'bar',\n" +
//                "    userId: 1\n" +
//                "  }","POST");
        IplImage image;
//        System.out.println(HttpRequester.requester("http://jsonplaceholder.typicode.com/posts/","","GET").get("id"));
        canvas.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
        FrameGrabber grab = new OpenCVFrameGrabber(0);
        int i = 0;
        try {
            grab.start();
            IplImage img;
            OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();

            while (true) {

                img = converter.convert(grab.grab());
                if (img != null) {
                    cvFlip(img, img, 1);// l-r = 90_degrees_steps_anti_clockwise
                    cvSaveImage((i++) + "-capture.jpg", img);
                    // show image on window
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
