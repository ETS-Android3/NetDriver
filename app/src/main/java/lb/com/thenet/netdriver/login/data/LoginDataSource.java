package lb.com.thenet.netdriver.login.data;

import android.os.AsyncTask;

import com.android.volley.Response;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import lb.com.thenet.netdriver.login.data.model.LoggedInUser;
import lb.com.thenet.netdriver.onlineservices.json.ResponseMessage;
import lb.com.thenet.netdriver.onlineservices.json.ScanBuilding;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class LoginDataSource {


    public Result<LoggedInUser> login(String username, String password) {

        try {
            // TODO: handle loggedInUser authentication

            LoginUserAsync task = new LoginUserAsync(username,password);
            task.execute().get();
            if(task.mLoggedInUserResult.success){
                LoggedInUser loggedInUser = new LoggedInUser(
                        task.mLoggedInUserResult.data.firstName,
                        task.mLoggedInUserResult.data.lastName,
                        task.mLoggedInUserResult.data.username,
                        task.mLoggedInUserResult.data.token,
                        task.mLoggedInUserResult.data.image
                );
                return new Result.Success<>(loggedInUser);

            } else {
                return new Result.Error(new IOException("User Not Authenticated"));

            }
            /*
            LoggedInUser fakeUser =
                    new LoggedInUser(
                            java.util.UUID.randomUUID().toString(),
                            "Jane Doe");

             */
            //mListener.notifyLoginResult(new Result.Success<>(fakeUser));
        } catch (Exception e) {
            //mListener.notifyLoginResult(new Result.Error(new IOException("Error logging in", e)));
            return new Result.Error(new IOException("Error logging in", e));

        }
    }

    public void logout() {
        // TODO: revoke authentication
    }

    /*
    private class Credentials{
        String userName;
        String password;

        public String getPassword() {
            return password;
        }

        public String getUserName() {
            return userName;
        }
        public Credentials(String u, String p){userName = u; password = p;}
    }

     */
    private class LoginUserAsync extends AsyncTask<Void,Void, ResponseMessage<LoggedInUser>>{

        private String mUserName;
        private String mPassword;
        public ResponseMessage<LoggedInUser> mLoggedInUserResult;

        public LoginUserAsync(String userName, String password){
            mUserName = userName;
            mPassword = password;
        }

        @Override
        protected ResponseMessage<LoggedInUser> doInBackground(Void... voids) {

            String url = "https://netdrivermobileapp.azurewebsites.net/api/v1/authentication/login";
            JSONObject jsonObjSend;

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

                con.setRequestProperty("Content-Type", "application/json");
                con.setRequestProperty("X-Functions-key","8J5oemgClfo1DPB/j6paDvpAqvpUUsumQfxnkU0bcZYfk6mpGZ9X4Q==");

                con.setDoOutput(true);

                String str =  "{\n" +
                        "    \"username\": \""+mUserName+"\",\n" +
                        "    \"password\": \""+mPassword+"\"\n" +
                        "}";


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
                Type responseType = new TypeToken<ResponseMessage<LoggedInUser>>(){}.getType();

                ResponseMessage<LoggedInUser> retVal = gson.fromJson(response.toString(), responseType);
                mLoggedInUserResult = retVal;
                // print result
                return retVal;

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


            ResponseMessage<LoggedInUser> errorResult = new ResponseMessage<>();
            errorResult.success = false;
            errorResult.errorMessage = errorMessage;
            errorResult.message = errorMessage;
            mLoggedInUserResult = errorResult;
            return errorResult;

        }

        @Override
        protected void onPostExecute(ResponseMessage<LoggedInUser> loggedInUserResponseMessage) {
            super.onPostExecute(loggedInUserResponseMessage);
        }
    }


}
