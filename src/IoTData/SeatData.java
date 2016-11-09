package IoTData;

/**
 * Created by Ilya on 8/11/16.
 */
public class SeatData {
    private int id, person_id;
    private boolean is_sitting;
    private long timestamp;

    public SeatData(int id, int person_id, boolean is_sitting, long timestamp) {
        this.id = id;
        this.person_id = person_id;
        this.is_sitting = is_sitting;
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public int getPerson_id() {
        return person_id;
    }

    public boolean is_sitting() {
        return is_sitting;
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

    public void setIs_sitting(boolean is_sitting) {
        this.is_sitting = is_sitting;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
