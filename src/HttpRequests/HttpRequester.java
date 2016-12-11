package HttpRequests;

import IoTData.EmotionData;
import Utilities.UrlList;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.*;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Utilities.APIKeys;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

/**
 * Created by Ilya on 7/11/16.
 *
 * This class handles Http requests for services consumed by the pi machine
 */
public class HttpRequester {
    public static EmotionData emotionRequester(String urlParameters, String requestType,int person_id){
        HttpURLConnection connection = null;
//        String targetURL = UrlList.MSEmotionAPIUrl;

        try{

            URL url = new URL(UrlList.MSEmotionAPIUrl);
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
                EmotionData ed = new EmotionData(Double.parseDouble(data.get("scores").get("anger")),Double.parseDouble(data.get("scores").get("happiness")),Double.parseDouble(data.get("scores").get("sadness")),Double.parseDouble(data.get("scores").get("neutral")),person_id);
                generalRequester(UrlList.APIUrl,"/emotiondatum",ed.toHashMap(),null,"POST");
            }
            else{
                return null;
            }
        }catch(Exception e){
            System.out.println(e);
        }
        return null;
    }


    public static String generalRequester(String targetURL, String path, HashMap<String,String> urlParameters, String data, String requestType){
        HttpURLConnection connections = null;
        String responseEntity =null;
        try{
            URIBuilder uri = new URIBuilder()
                    .setScheme("http")
                    .setHost(targetURL)
                    .setPath(path);
            for (HashMap.Entry<String,String> params: urlParameters.entrySet()){
                uri.addParameter(params.getKey(),params.getValue());
            }
            uri.build();

            HttpClient client = HttpClientBuilder.create().build();
            //The Following Sends the data stream as a request
            if((requestType == "POST" || requestType =="PUT") && data != "") {
                HttpPost hp = new HttpPost(String.valueOf(uri));
                List<NameValuePair> urlParams = new ArrayList<NameValuePair>();
                for (HashMap.Entry<String,String> params : urlParameters.entrySet()){
                    urlParams.add(new BasicNameValuePair(params.getKey(),params.getValue()));
                }
                hp.setEntity(new UrlEncodedFormEntity(urlParams));
                HttpResponse response = client.execute(hp);
                System.out.println("Response Code : "
                        + response.getStatusLine().getStatusCode());

                BufferedReader rd = new BufferedReader(
                        new InputStreamReader(response.getEntity().getContent()));

                StringBuffer result = new StringBuffer();
                String line = "";
                while ((line = rd.readLine()) != null) {
                    result.append(line);
                }
                return result.toString();
            }
            else if (requestType == "GET"){
                HttpGet hg = new HttpGet(String.valueOf(uri));
                CloseableHttpClient httpclient = HttpClients.createDefault();
                CloseableHttpResponse response = httpclient.execute(hg);
                try {
                    HttpEntity entity = response.getEntity();
                    if (entity != null) {
                        long len = entity.getContentLength();
                        if (len != -1 && len < 2048) {
                            //Entities can only be read once
                            responseEntity = EntityUtils.toString(entity);
                        }else {
                            InputStream instream = entity.getContent();
                        }
                    }
                } finally {
                    response.close();
                }
                return responseEntity;
            }
        }catch(Exception e){
            System.out.println(e);
        }
        return responseEntity;
    }


// Started GET "/person/by_name?name=Ilya" for 202.94.70.25 at 2016-12-07 18:54:08 +0000
// Started GET "/person/by_name?name=Ilya" for 202.94.70.51 at 2016-12-07 19:10:18 +0000

    public static String parameterfier(HashMap<String,String> params){
        StringBuilder sb = new StringBuilder();
        sb.append("?");
        for(HashMap.Entry<String,String> entry: params.entrySet()){
            String key = String.valueOf(entry.getKey());
            String value = String.valueOf(entry.getValue());

            sb.append(key+"="+value);
        }
        return sb.toString();

    public static HashMap<String,String> objectifier(String json){
        Gson gson = new Gson();
        Type type = new TypeToken<HashMap<String, String>>() {
        }.getType();
        HashMap<String, String> data = gson.fromJson(json, type);
        return data;
    }



}
