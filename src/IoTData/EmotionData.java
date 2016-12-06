package IoTData;

import java.util.HashMap;

/**
 * Created by Ilya on 8/11/16.
 */
public class EmotionData {
    private long timestamp;
    private String feeling;
    private double anger, happiness , sadness;
    private int id, person_id;

    public EmotionData(long timestamp, String feeling, double anger, double happiness, double sadness, int id, int person_id) {
        this.timestamp = timestamp;
        this.feeling = feeling;
        this.anger = anger;
        this.happiness = happiness;
        this.sadness = sadness;
        this.id = id;
        this.person_id = person_id;
    }

    public EmotionData(double anger, double happiness, double sadness, int person_id) {
        this.anger = anger;
        this.happiness = happiness;
        this.sadness = sadness;
        this.person_id = person_id;
        this.feeling = this.getHighestEmotion();
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getFeeling() {
        return feeling;
    }

    public double getAnger() {
        return anger;
    }

    public double getHappiness() {
        return happiness;
    }

    public double getSadness() {
        return sadness;
    }

    public int getId() {
        return id;
    }

    public int getPerson_id() {
        return person_id;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setFeeling(String feeling) {
        this.feeling = feeling;
    }

    public void setAnger(double anger) {
        this.anger = anger;
    }

    public void setHappiness(double happiness) {
        this.happiness = happiness;
    }

    public void setSadness(double sadness) {
        this.sadness = sadness;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setPerson_id(int person_id) {
        this.person_id = person_id;
    }

    public String getHighestEmotion(){
        HashMap<String,Double> emotionMap = new HashMap<String,Double>();
        emotionMap.put("Happiness",this.getHappiness());
        emotionMap.put("Sadness",this.getSadness());
        emotionMap.put("Anger",this.getAnger());
        HashMap.Entry<String,Double> maxEntry = null;
        for (HashMap.Entry<String, Double> entry : emotionMap.entrySet())
        {
            if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0)
            {
                maxEntry = entry;
            }
        }
        return maxEntry.getKey();
    }
}

