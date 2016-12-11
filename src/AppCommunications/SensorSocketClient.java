package AppCommunications;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by Charles on 12/12/2016.
 */
public class SensorSocketClient {

    public void SensorSocketClient(String ServerName, Integer PortNumber) {

        Socket client = null;
        DataOutputStream sensorData = null;

        try {
            client = new Socket(ServerName,PortNumber);
            sensorData = new DataOutputStream(client.getOutputStream());

        }
            catch(IOException e){
            System.out.println(e);
        }
        try {
//            sensorData.writeChars(seatParams);
//            sensorData.writeChars(totemParams);

            sensorData.close();
            client.close();

        } catch(Exception e){
            System.out.println(e);
        }

    }
}
