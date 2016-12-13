package Multithreader;

import Main.BackgroundRunner;
import Utilities.UrlList;

/**
 * Created by Charles on 12/12/2016.
 */
public class Multithreader implements Runnable {

    private Thread thread;
    private String threadName;

    public Multithreader(String name){
        threadName = name;
        System.out.println("Creating "+threadName);
    }

    public void run(){
        System.out.println("Running "+threadName);
        try {
            if(threadName.contains("Emotion")){
                BackgroundRunner.getEmotionData(BackgroundRunner.pers);
            } else if(threadName.contains(("Seat"))){
                BackgroundRunner.getSeatData();
            } else if(threadName.contains("Totem")){
                BackgroundRunner.getTotemData();
            } else {
                System.out.println("Sausages");
            }
                Thread.sleep(50);

        }catch (InterruptedException e) {
            System.out.println("Thread " +  threadName + " interrupted.");
        }
        System.out.println("Thread " +  threadName + " exiting.");
    }

    public void start () {
        System.out.println("Starting " +  threadName );
        if (thread == null) {
            thread = new Thread (this, threadName);
            thread.start ();
        }
    }
}
