package spotify;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.OutputStream;
import java.nio.Buffer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.Proxy;
import java.net.InetSocketAddress;
import java.util.Base64;
import java.util.regex.*;
/**
 * GetAccessToken
 */
public class GetAccessToken {

    public String getAccessToken(String targetURL, String ClientID, String ClientSecretID){
        try {
     
            String url = targetURL;
         
            URL obj = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
         
            //conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
         
            conn.setRequestMethod("POST");
         
            String userpass = ClientSecretID + ":" + ClientSecretID;
            //System.out.println(Base64.getEncoder().encodeToString(userpass.getBytes("UTF-8")));
            String basicAuth = "Basic " + Base64.getEncoder().encodeToString(userpass.getBytes("UTF-8")); 
            
            
            conn.setRequestProperty ("Authorization", basicAuth);
            //conn.setRequestProperty("grant_type", "client_credentials");
            
            byte[] curl_data = "grant_type=client_credentials".getBytes();
            
            String data =  "{\"format\":\"json\",\"pattern\":\"#\"}";
            OutputStream outStream = conn.getOutputStream();
            
            outStream.write(curl_data);
            outStream.close();
         
            BufferedReader finalRes = new BufferedReader ( new InputStreamReader(conn.getInputStream()));   
            System.out.println(finalRes.readLine());
        
            return parse(finalRes.readLine());

        } catch (Exception e) {
            e.printStackTrace();
        }
         
    }

    private String parse(String in){  //to do: parse the json to the format we want
        // Pattern p = Pattern.compile("\".?\":\".?\"");
        // Matcher m = p.matcher(in);


    }
}
    
