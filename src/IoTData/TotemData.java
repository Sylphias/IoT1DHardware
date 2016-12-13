package IoTData;

import java.util.HashMap;

/**
 * Created by Ilya on 8/11/16.
 */
public class TotemData {
    private int id, person_id;
    private String state;
    private long timestamp;

    public TotemData(int id, int person_id, String state, long timestamp) {
        this.id = id;
        this.person_id = person_id;
        this.state = state;
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public int getPerson_id() {
        return person_id;
    }

    public String getState() {
        return state;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setPerson_id(int person_id) {
        this.person_id = person_id;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public HashMap<String,String> toHashMap() {
        HashMap<String, String> totemHash = new HashMap<String, String>();
        totemHash.put("id", Integer.toString(this.id));
        totemHash.put("person_id", Integer.toString(this.person_id));
        totemHash.put("state", this.state);
        totemHash.put("id", Long.toString(this.timestamp));

        return totemHash;
    }
}
