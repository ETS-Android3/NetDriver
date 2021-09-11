package lb.com.thenet.netdriver.onlineservices;

import android.content.Context;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.MutableLiveData;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import lb.com.thenet.netdriver.GlobalCoordinator;
import lb.com.thenet.netdriver.R;
import lb.com.thenet.netdriver.onlineservices.json.ActionRoute;
import lb.com.thenet.netdriver.onlineservices.json.ResponseMessage;
import lb.com.thenet.netdriver.onlineservices.json.SettingItem;

public class GeneralServices {

    private Context mContext;

    public MutableLiveData<ResponseMessage<Boolean>> mRunSheetResponse;
    public MutableLiveData<ResponseMessage<SettingItem[]>> mSettingsResponse;
    public MutableLiveData<ResponseMessage<ActionRoute[]>> mActionRoutesResponse;



    public MutableLiveData<DriverServicesError> mError;

    public GeneralServices(Context context){
        mContext = context;
        mRunSheetResponse = new MutableLiveData<>();
        mSettingsResponse = new MutableLiveData<>();
        mActionRoutesResponse = new MutableLiveData<>();


        mError= new MutableLiveData<>();

    }


    public void callRunSheet(){
        RequestQueue queue = Volley.newRequestQueue(mContext);


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                GlobalCoordinator.getInstance().getServiceUrl(mContext.getString(R.string.func_runsheet_url)), null,
                new Response.Listener() {
                    @Override
                    public void onResponse(Object response) {
                        String obj = response.toString();
                        Gson gson = new Gson();
                        Type responseType = new TypeToken<ResponseMessage<Boolean>>(){}.getType();
                        ResponseMessage<Boolean> retVal = gson.fromJson(obj,responseType);
                        mRunSheetResponse.setValue(retVal);
                        if(retVal.success){

                        }
                        else {

                        }
                    }


                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Failure Callback
                        DriverServicesError error = new DriverServicesError();
                        error.errorType = ServicesErrorType.runSheetError;
                        error.errorMessage = volleyError.getMessage();
                        error.volleyError = volleyError;
                        mError.setValue(error);
                    }
                })



        {
            /** Passing some request headers* */
            @Override
            public Map getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put(GlobalCoordinator.SERVICE_HEADER_TOKEN, DriverServices.mToken.getValue().Token);
                headers.put(GlobalCoordinator.SERVICE_HEADER_CONTENT,GlobalCoordinator.SERVICE_APPLICATION_JSON);
                headers.put(GlobalCoordinator.SERVICE_HEADER_FUNCTION, mContext.getString(R.string.func_runsheet_key));
                return headers;
            }
        };


// Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);


    }

    public void getSettings(){
        RequestQueue queue = Volley.newRequestQueue(mContext);


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                GlobalCoordinator.getInstance().getServiceUrl(mContext.getString(R.string.func_settings_url)), null,
                new Response.Listener() {
                    @Override
                    public void onResponse(Object response) {
                        String obj = response.toString();
                        Gson gson = new Gson();
                        Type responseType = new TypeToken<ResponseMessage<SettingItem[]>>(){}.getType();
                        ResponseMessage<SettingItem[]> retVal = gson.fromJson(obj,responseType);
                        mSettingsResponse.setValue(retVal);
                        if(retVal.success){

                        }
                        else {

                        }
                    }


                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Failure Callback
                        DriverServicesError error = new DriverServicesError();
                        error.errorType = ServicesErrorType.settingsError;
                        error.errorMessage = volleyError.getMessage();
                        error.volleyError = volleyError;
                        mError.setValue(error);
                    }
                })



        {
            /** Passing some request headers* */
            @Override
            public Map getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put(GlobalCoordinator.SERVICE_HEADER_TOKEN, DriverServices.mToken.getValue().Token);
                headers.put(GlobalCoordinator.SERVICE_HEADER_CONTENT,GlobalCoordinator.SERVICE_APPLICATION_JSON);
                headers.put(GlobalCoordinator.SERVICE_HEADER_FUNCTION, mContext.getString(R.string.func_settings_key));
                return headers;
            }
        };


// Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);


    }

    public void getLOVActionRoutes(){

        RequestQueue queue = Volley.newRequestQueue(mContext);


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                GlobalCoordinator.getInstance().getServiceUrl(mContext.getString(R.string.func_getactionroutes_url)), null,
                new Response.Listener() {
                    @Override
                    public void onResponse(Object response) {
                        String obj = response.toString();
                        Gson gson = new Gson();
                        Type responseType = new TypeToken<ResponseMessage<ActionRoute[]>>(){}.getType();
                        ResponseMessage<ActionRoute[]> retVal = gson.fromJson(obj,responseType);
                        mActionRoutesResponse.setValue(retVal);

                        /*
                        if(retVal.success){
                            String nm = retVal.data[2].name;
                            Integer len = retVal.data[2].actionRoutes.length;
                            String id = retVal.data[2].actionRouteId;
                        }
                        else {
                            DriverServicesError error = new DriverServicesError();
                            error.errorType = ServicesErrorType.lovActionRoutesError;
                            error.errorCode = retVal.errorCode;
                            error.errorMessage = retVal.errorMessage;
                            mError.setValue(error);
                        }

                         */
                    }


                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Failure Callback
                        DriverServicesError error = new DriverServicesError();
                        error.errorType = ServicesErrorType.lovActionRoutesError;
                        error.errorMessage = volleyError.getMessage();
                        error.volleyError = volleyError;
                        mError.setValue(error);
                    }
                })

        {
            /** Passing some request headers* */
            @Override
            public Map getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put(GlobalCoordinator.SERVICE_HEADER_TOKEN, DriverServices.mToken.getValue().Token);
                headers.put(GlobalCoordinator.SERVICE_HEADER_CONTENT,GlobalCoordinator.SERVICE_APPLICATION_JSON);
                headers.put(GlobalCoordinator.SERVICE_HEADER_FUNCTION, mContext.getString(R.string.func_getactionroutes_key));
                return headers;
            }
        };

        queue.add(jsonObjectRequest);

    }
}
