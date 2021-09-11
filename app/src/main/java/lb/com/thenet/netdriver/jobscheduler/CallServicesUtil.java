package lb.com.thenet.netdriver.jobscheduler;

import android.content.Context;
import android.util.Base64;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import lb.com.thenet.netdriver.GlobalCoordinator;
import lb.com.thenet.netdriver.onlineservices.DriverServices;
import lb.com.thenet.netdriver.onlineservices.json.ResponseMessage;

public class CallServicesUtil {

    public boolean postToServer(final Context mContext, String url, String key, String jsonToPost){

        //String url = "https://netdrivermobileapp.azurewebsites.net/api/v1/" + "stops/received" + "/";//"https://netdrivermobileapp.azurewebsites.net/api/v1/authentication/login";


        URL obj = null;
        HttpURLConnection con = null;
        String errorMessage = "";
        try {
                /*
                jsonObjSend = new JSONObject();
                jsonObjSend.put("username",mUserName);
                jsonObjSend.put("passwor",mPassword);



                 */
            obj = new URL(url);
            con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("POST");


            con.setRequestProperty("X-User-Token", DriverServices.mToken.getValue().Token);
            con.setRequestProperty("Content-Type", GlobalCoordinator.SERVICE_APPLICATION_JSON);
            con.setRequestProperty("x-functions-key", key);

            con.setDoOutput(true);

            String str =  jsonToPost;


            byte[] outputInBytes = str.getBytes("UTF-8");
            OutputStream os = con.getOutputStream();
            os.write( outputInBytes );
            os.close();


            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.flush();
            wr.close();



            int status = con.getResponseCode();
            if(status != HttpURLConnection.HTTP_OK){
                BufferedReader err = new BufferedReader(new InputStreamReader(con.getErrorStream()));

                String errorLine;
                StringBuffer responseError = new StringBuffer();
                while ((errorLine = err.readLine()) != null) {
                    responseError.append(errorLine);
                }
                err.close();
            }
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            Gson gson = new Gson();
            Type responseType = new TypeToken<ResponseMessage<Boolean>>(){}.getType();

            ResponseMessage<Boolean> retVal = gson.fromJson(response.toString(), responseType);

            // print result
            return retVal.success;

        } catch (MalformedURLException e) {
            errorMessage = e.getMessage();
            e.printStackTrace();
        } catch (ProtocolException e) {
            errorMessage = e.getMessage();
            e.printStackTrace();
        } catch (IOException e) {
            errorMessage = e.getMessage();
            e.printStackTrace();
        }


        return false;


    }



    public boolean sendSignature(final Context mContext, String imageName, String imageString) {

        String url = "https://netdrivermobileapp.azurewebsites.net/api/v1/" + "signaturebinary" + "/" + imageName + ".PNG";
        URL obj = null;
        HttpURLConnection con = null;
        String errorMessage = "";

        try{
            /*
            GZIPInputStream gzip2 = new GZIPInputStream(new ByteArrayInputStream(Base64.decode(imageString, Base64.DEFAULT)));
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[256];
            int n = gzip2.read(buffer);
            while(n >= 0){
                outputStream.write(buffer,0,n);
                n = gzip2.read(buffer);
            }
            byte[] signatureUnCompressed = outputStream.toByteArray();

             */

            obj = new URL(url);
            con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("POST");

            con.setRequestProperty("X-User-Token", "37DF02E8-1097-4C7C-BD9B-61DFC2BD4B05");
            con.setRequestProperty("Content-Type","application/json");
            con.setRequestProperty("x-functions-key", "8J5oemgClfo1DPB/j6paDvpAqvpUUsumQfxnkU0bcZYfk6mpGZ9X4Q==");

            con.setDoOutput(true);

            //String str = Base64.encodeToString(signatureUnCompressed,Base64.DEFAULT);
            //byte[] outputInBytes = str.getBytes("UTF-8");
            byte[] imageBytes = Base64.decode(imageString, Base64.DEFAULT);
            String str = Base64.encodeToString(imageBytes,Base64.DEFAULT);
            byte[] outputInBytes = str.getBytes("UTF-8");

            OutputStream os = con.getOutputStream();
            os.write( outputInBytes );
            os.close();

            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.flush();
            wr.close();


            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            Gson gson = new Gson();
            Type responseType = new TypeToken<ResponseMessage<Boolean>>(){}.getType();

            ResponseMessage<Boolean> retVal = gson.fromJson(response.toString(), responseType);

            // print result
            return retVal.success;



        } catch (MalformedURLException e) {
            errorMessage = e.getMessage();
            e.printStackTrace();
        } catch (ProtocolException e) {
            errorMessage = e.getMessage();
            e.printStackTrace();
        } catch (IOException e) {
            errorMessage = e.getMessage();
            e.printStackTrace();
        }
        return false;

    }

}
