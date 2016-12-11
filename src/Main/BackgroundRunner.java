package Main;

import HttpRequests.HttpRequester;
import IoTData.EmotionData;
import IoTData.Person;
import Utilities.MSBlobUploader;
import Utilities.UrlList;
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

/**
 * Created by Ilya on 6/12/16.
 */
public class BackgroundRunner {
    public static void main(String[] args) {
        startServer();
    }

    public static void startServer(){
        getEmotionData();

    }

    public static void getEmotionData(){
        HttpRequester req = new HttpRequester();

        opencv_core.IplImage image;
//        System.out.println(HttpRequester.requester("http://jsonplaceholder.typicode.com/posts/","","GET").get("id"));
        FrameGrabber grab = new OpenCVFrameGrabber(0);

        Gson gson = new Gson();
        int i = 0;
        String id = System.getProperty("user.name");
        HashMap<String,String> personParams = new HashMap<String, String>();
        personParams.put("name",id);
        String personResponse = HttpRequester.generalRequester(UrlList.APIUrl,"/person/by_name",personParams,"","GET");
        Person pers = null;
        if(!personResponse.matches("null")) {
            pers = new Person(personResponse);
        }
        // If we cannot find a person we will create a person
        else{
            // Create a new person.
            Person p = new Person(18,id,'U');
            String pjson = gson.toJson(p);
            String request = HttpRequester.generalRequester(UrlList.APIUrl,"/person",p.toHashMap(),pjson,"POST");
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
                String current = new java.io.File( "." ).getCanonicalPath();
                if (img != null) {
                    cvFlip(img, img, 1);
                    cvSaveImage(current+"/img/capture"+id+".jpg", img);
                    MSBlobUploader.initializeContainer();
                    String URL = MSBlobUploader.createBlob("testBlob",id);
                    Matcher m = p.matcher("URL");
                    if(m.matches()){
                        URL = URL.substring(m.end(),URL.length()-1);
                    }
                    if (pers != null) {
                       EmotionData ed = HttpRequester.emotionRequester("{\"url\":\"" + URL + "\"}", "POST", pers.getId());
                        if( ed != null) {
                            ed.setFeeling(ed.getHighestEmotion());
                            String result = HttpRequester.generalRequester(UrlList.APIUrl, "/emotiondatum", ed.toHashMap(), "", "POST");
                            System.out.println(result);
                        }
                    }
                }
                Thread.sleep(5000);
            }
        } catch (Exception e) {
            System.out.println(e);
        }

    }

    public void sendEmotionData(double feeling){

    }

}
