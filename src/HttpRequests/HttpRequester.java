package HttpRequests;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ilya on 7/11/16.
 *
 * This class handles Http requests for services consumed by the pi machine
 */
public class HttpRequester {
    public static Map<String, String> requester(String targetURL, String urlParameters,byte[] data, String requestType){
        HttpURLConnection connection = null;
        try{

            URL url = new URL(targetURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Content-Type", "application/x-ww-form-urlenconded");
            connection.setRequestProperty("method", requestType);
            connection.setRequestProperty("User-Agent", "Mozilla/4.76");
            connection.setRequestProperty("Content-Length", Integer.toString(urlParameters.getBytes().length));
            connection.setRequestProperty("Content-Language", "en-US");
            connection.setUseCaches(false);
            connection.setDoOutput(true);

            //The Following Sends the data stream as a request
            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.close();

            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            StringBuilder response = new StringBuilder();
            String line;
            while((line = rd.readLine()) != null ){
                response.append(line);
            }
            rd.close();
            Gson gson = new Gson();
            Type type = new TypeToken<Map<String, String>>(){}.getType();
            Map <String, String> dataz = gson.fromJson(response.toString(),type);
            return dataz;

        }catch(Exception e){
            System.out.println(e);
        }
        return null;
    }

}
