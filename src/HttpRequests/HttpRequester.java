package HttpRequests;

import IoTData.EmotionData;
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

import Utilities.APIKeys;
/**
 * Created by Ilya on 7/11/16.
 *
 * This class handles Http requests for services consumed by the pi machine
 */
public class HttpRequester {
    public static EmotionData emotionRequester(String urlParameters, String requestType,int person_id){
        HttpURLConnection connection = null;
        String targetURL = "https://api.projectoxford.ai/emotion/v1.0/recognize";

        try{

            URL url = new URL(targetURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Method", requestType);
            connection.setRequestProperty("User-Agent", "Mozilla/4.76");
//            connection.setRequestProperty("Content-Length", Integer.toString(urlParameters.getBytes().length));
//            connection.setRequestProperty("Content-Language", "en-US");
            connection.setUseCaches(false);
            connection.setDoOutput(true);
            connection.setRequestProperty("Ocp-Apim-Subscription-Key", APIKeys.MSAPIKey);
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
            if(response.toString().length() > 2) {
                Gson gson = new Gson();
                Type type = new TypeToken<Map<String, Map<String, String>>>() {
                }.getType();
                Map<String, Map<String, String>> data = gson.fromJson(response.toString().substring(1, response.length() - 1), type);
                EmotionData ed = new EmotionData(Double.parseDouble(data.get("scores").get("anger")),Double.parseDouble(data.get("scores").get("happiness")),Double.parseDouble(data.get("scores").get("sadness")),person_id);

            }
            else{
                return null;
            }
        }catch(Exception e){
            System.out.println(e);
        }
        return null;
    }

    public static String generalRequester(String targetURL,String urlParameters,String data, String requestType){
        HttpURLConnection connection = null;
        try{
            if(urlParameters != null){
                targetURL += urlParameters;
            }
            URL url = new URL(targetURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Method", requestType);
            connection.setRequestProperty("User-Agent", "Mozilla/4.76");
            connection.setUseCaches(false);
            connection.setDoOutput(true);

            //The Following Sends the data stream as a request

            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            if(requestType == "POST" || requestType =="PUT") {
                wr.writeBytes(data);
            }
            wr.close();
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            StringBuilder response = new StringBuilder();
            String line;
            while((line = rd.readLine()) != null ){
                response.append(line);
            }
            rd.close();
            return response.toString();
        }catch(Exception e){
            System.out.println(e);
        }
        return null;
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
