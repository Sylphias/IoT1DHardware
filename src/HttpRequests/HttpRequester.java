package HttpRequests;

import IoTData.EmotionData;
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

//    public static String generalRequester(String targetURL,String urlParameters,String data, String requestType){
//        HttpURLConnection connections = null;
//        try{
//            String fullURL = targetURL;
//            if(urlParameters != null){
//                fullURL += urlParameters;
//            }
//            URL url = new URL(fullURL);
//            connections = (HttpURLConnection) url.openConnection();
//            connections.setRequestProperty("Connection","keep-alive");
//            connections.setRequestProperty("Content-Type", "application/json");
////            connections.setRequestProperty("Method", requestType);
//            connections.setRequestProperty("Host", targetURL);
//            connections.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.0; en-US) AppleWebKit/525.28 (KHTML, like Gecko) Version/3.2.2 Safari/525.28.1");
//            connections.setUseCaches(false);
//            connections.setDoOutput(true);
//            connections.setRequestMethod(requestType);
//            //The Following Sends the data stream as a request
//            if((requestType == "POST" || requestType =="PUT") && data != "") {
//                DataOutputStream wr = new DataOutputStream(connections.getOutputStream());
//                wr.writeBytes(data);
//                wr.close();
//            }
//            InputStream is = connections.getInputStream();
//            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
//            StringBuilder response = new StringBuilder();
//            String line;
//            while((line = rd.readLine()) != null ){
//                response.append(line);
//            }
//            rd.close();
//            return response.toString();
//        }catch(Exception e){
//            System.out.println(e);
//        }
//        return null;
//    }

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
            sb.append(entry.getKey()+"="+entry.getValue());
        }
        return sb.toString();
    }

    public static HashMap<String,String> objectifier(String json){
        Gson gson = new Gson();
        Type type = new TypeToken<HashMap<String, String>>() {
        }.getType();
        HashMap<String, String> data = gson.fromJson(json, type);
        return data;
    }


//    public static String responseHandler(){
//        CloseableHttpClient httpclient = HttpClients.createDefault();
//        HttpGet httpget = new HttpGet("http://localhost/json");
//
//        ResponseHandler<MyJsonObject> rh = new ResponseHandler<MyJsonObject>() {
//
//            @Override
//            public JsonObject handleResponse(
//                    final HttpResponse response) throws IOException {
//                StatusLine statusLine = response.getStatusLine();
//                HttpEntity entity = response.getEntity();
//                if (statusLine.getStatusCode() >= 300) {
//                    throw new HttpResponseException(
//                            statusLine.getStatusCode(),
//                            statusLine.getReasonPhrase());
//                }
//                if (entity == null) {
//                    throw new ClientProtocolException("Response contains no content");
//                }
//                Gson gson = new GsonBuilder().create();
//                ContentType contentType = ContentType.getOrDefault(entity);
//                Charset charset = contentType.getCharset();
//                Reader reader = new InputStreamReader(entity.getContent(), charset);
//                return gson.fromJson(reader, MyJsonObject.class);
//            }
//        };
//        MyJsonObject myjson = client.execute(httpget, rh);
//
//        return "";
//    }


}
