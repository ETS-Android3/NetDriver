package lb.com.thenet.netdriver.onlineservices;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Base64;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.lifecycle.MutableLiveData;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.sql.Driver;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import lb.com.thenet.netdriver.GlobalCoordinator;
import lb.com.thenet.netdriver.R;
import lb.com.thenet.netdriver.dataholders.DriverLocation;
import lb.com.thenet.netdriver.dataholders.ReceivedStop;
import lb.com.thenet.netdriver.jobscheduler.PostToServerWorker;
import lb.com.thenet.netdriver.jobscheduler.UploadSignatureWorker;
import lb.com.thenet.netdriver.onlineservices.json.ActionRoute;
import lb.com.thenet.netdriver.onlineservices.json.ActivityType;
import lb.com.thenet.netdriver.onlineservices.json.AdjustedShipment;
import lb.com.thenet.netdriver.onlineservices.json.CODItem;
import lb.com.thenet.netdriver.onlineservices.json.CheckStatus;
import lb.com.thenet.netdriver.onlineservices.json.DelegateInfo;
import lb.com.thenet.netdriver.onlineservices.json.JsonStop;
import lb.com.thenet.netdriver.onlineservices.json.LoggedInUserData;
import lb.com.thenet.netdriver.onlineservices.json.NetLockerData;
import lb.com.thenet.netdriver.onlineservices.json.OrderType;
import lb.com.thenet.netdriver.onlineservices.json.ResponseMessage;
import lb.com.thenet.netdriver.onlineservices.json.ScanBuilding;
import lb.com.thenet.netdriver.onlineservices.json.SendDeliveryRequest;
import lb.com.thenet.netdriver.onlineservices.json.SendLabelsLabelItem;
import lb.com.thenet.netdriver.onlineservices.json.SendLabelsRequest;
import lb.com.thenet.netdriver.onlineservices.json.SendPickupRequest;
import lb.com.thenet.netdriver.onlineservices.json.SpecialDelivery;
import lb.com.thenet.netdriver.onlineservices.json.StartStopRequest;
import lb.com.thenet.netdriver.rooms.entities.DriverRepository;
import lb.com.thenet.netdriver.rooms.entities.ProcessQueueRepository;
import lb.com.thenet.netdriver.rooms.entities.ScannedLocation;
import lb.com.thenet.netdriver.rooms.entities.asyncTaskListener;
import lb.com.thenet.netdriver.rooms.entities.enums.ActionRouteType;
import lb.com.thenet.netdriver.rooms.entities.enums.QueueItemType;
import lb.com.thenet.netdriver.rooms.entities.info.Info;
import lb.com.thenet.netdriver.rooms.entities.stops.Stop;
import lb.com.thenet.netdriver.rooms.entities.stops.StopLabel;

public class DriverServices {

    final String servicesURL;
    //public String userToken = "6a5aad71-2a02-4299-bf91-2ced3ddaea1f";
    private Context mContext;

    public static MutableLiveData<LoggedInUserData> mToken = new MutableLiveData<>();

    public static MutableLiveData<Date> deliveryOrdersTimeStamp = new MutableLiveData<>();
    public static MutableLiveData<Boolean> deliveryOrdersLoaded = new MutableLiveData<>(false);
    public static MutableLiveData<Date> pickupOrdersTimeStamp = new MutableLiveData<>();
    public static MutableLiveData<Boolean> pickupOrdersLoaded = new MutableLiveData<>(false);

    public MutableLiveData<ResponseMessage<ScanBuilding>> mBuildingNFC;
    public MutableLiveData<ResponseMessage<Boolean>> mCheckinResponse;
    public MutableLiveData<ResponseMessage<Boolean>> mCheckOutResponse;
    public MutableLiveData<ResponseMessage<Boolean>> mBulkPickupResponse;
    public MutableLiveData<ResponseMessage<SpecialDelivery[]>> mSpecialDeliveriesResponse;
    public MutableLiveData<ResponseMessage<JsonStop[]>> mStopsResponse;
    public MutableLiveData<ResponseMessage<JsonStop[]>> mForceAssignedStopsResponse;
    public MutableLiveData<ResponseMessage<Boolean>> mStartStopResponse;
    public MutableLiveData<ResponseMessage<Boolean>> mStopStopResponse;
    public MutableLiveData<ResponseMessage<Boolean>> mAcceptStopResponse;
    public MutableLiveData<ResponseMessage<Boolean>> mRejectStopResponse;
    public MutableLiveData<ResponseMessage<Boolean>> mSendDeliveryResponse;
    public MutableLiveData<ResponseMessage<Boolean>> mSendNonDeliveryResponse;
    public MutableLiveData<ResponseMessage<Boolean>> mSendPickupResponse;
    public MutableLiveData<ResponseMessage<Boolean>> mSendNonPickupResponse;
    public MutableLiveData<ResponseMessage<Boolean>> mSendSignatureResponse;
    public MutableLiveData<ResponseMessage<NetLockerData>> mNetLockerResponse;
    public MutableLiveData<ResponseMessage<Boolean>> mRetourResponse;
    public MutableLiveData<ResponseMessage<Boolean>> mLiaisonResponse;
    public MutableLiveData<ResponseMessage<Boolean>> mWeightAdjustResponse;
    public MutableLiveData<ResponseMessage<Boolean>> mReceivedStopsResponse;
    public MutableLiveData<ResponseMessage<Boolean>> mCheckLabelResponse;
    public MutableLiveData<ResponseMessage<CODItem[]>> mCODListResponse;
    public MutableLiveData<ResponseMessage<CheckStatus>> mLabelStatus;
    public MutableLiveData<ResponseMessage<DelegateInfo[]>> mDelegates;



    public MutableLiveData<DriverServicesError> mError;


    public DriverServices(Context context) {
        servicesURL = "";

        mContext = context;
        mBuildingNFC = new MutableLiveData<>();
        mCheckinResponse = new MutableLiveData<>();
        mBulkPickupResponse = new MutableLiveData<>();
        mCheckOutResponse = new MutableLiveData<>();
        mSpecialDeliveriesResponse = new MutableLiveData<>();
        mStopsResponse = new MutableLiveData<>();
        mForceAssignedStopsResponse = new MutableLiveData<>();
        mAcceptStopResponse =  new MutableLiveData<>();
        mRejectStopResponse =  new MutableLiveData<>();
        mStartStopResponse = new MutableLiveData<>();
        mStopStopResponse = new MutableLiveData<>();
        mSendDeliveryResponse = new MutableLiveData<>();
        mSendNonDeliveryResponse = new MutableLiveData<>();
        mSendPickupResponse = new MutableLiveData<>();
        mSendNonPickupResponse = new MutableLiveData<>();
        mSendSignatureResponse = new MutableLiveData<>();
        mNetLockerResponse = new MutableLiveData<>();
        mRetourResponse = new MutableLiveData<>();
        mLiaisonResponse = new MutableLiveData<>();
        mWeightAdjustResponse = new MutableLiveData<>();
        mReceivedStopsResponse = new MutableLiveData<>();
        mCheckLabelResponse = new MutableLiveData<>();
        mCODListResponse = new MutableLiveData<>();
        mLabelStatus = new MutableLiveData<>();
        mDelegates = new MutableLiveData<>();

        mError= new MutableLiveData<>();

    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void checkinBarcodes(String buildingCode, LinkedList<String> barcodes, DriverLocation driverLocation, String nowFormatted){
        RequestQueue queue = Volley.newRequestQueue(mContext);

        JSONObject postparams = new JSONObject();
        try {
            postparams.put("buildingCode", buildingCode);
            postparams.put("longitude", driverLocation.longitude);
            postparams.put("latitude", driverLocation.latitude);
            postparams.put("activityDate", nowFormatted);

            JSONArray labels = new JSONArray();
            for (String label:barcodes
            ) {
                labels.put(label);
            }
            postparams.put("labels", labels);
        }catch (JSONException e){

        }
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                GlobalCoordinator.getInstance().getServiceUrl(mContext.getString(R.string.func_checkin_url)), postparams,
                new Response.Listener() {
                    @Override
                    public void onResponse(Object response) {
                        //Success Callback
                        String obj = response.toString();
                        Gson gson = new Gson();
                        Type responseType = new TypeToken<ResponseMessage<Boolean>>(){}.getType();
                        ResponseMessage<Boolean> retVal = gson.fromJson(obj,responseType);
                        if(retVal.success){
                            mCheckinResponse.setValue(retVal);
                        }
                        else {
                            DriverServicesError error = new DriverServicesError();
                            error.errorType = ServicesErrorType.checkInError;
                            error.errorCode = retVal.errorCode;
                            error.errorMessage = retVal.errorMessage;
                            mError.setValue(error);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Failure Callback
                        DriverServicesError error = new DriverServicesError();
                        error.errorType = ServicesErrorType.generalError;
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
                headers.put(GlobalCoordinator.SERVICE_HEADER_TOKEN, mToken.getValue().Token);
                headers.put(GlobalCoordinator.SERVICE_HEADER_CONTENT,GlobalCoordinator.SERVICE_APPLICATION_JSON);
                headers.put(GlobalCoordinator.SERVICE_HEADER_FUNCTION, mContext.getString(R.string.func_checkin_key));
                return headers;
            }
        };


// Add the request to the RequestQueue.
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(jsonObjReq);

    }



    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void bulkPickupBarcodes(String buildingCode, LinkedList<String> barcodes, DriverLocation driverLocation, String nowFormatted){
        RequestQueue queue = Volley.newRequestQueue(mContext);

        JSONObject postparams = new JSONObject();
        try {
            postparams.put("buildingCode", buildingCode);
            postparams.put("longitude", driverLocation.longitude);
            postparams.put("latitude", driverLocation.latitude);
            postparams.put("activityDate", nowFormatted);

            JSONArray labels = new JSONArray();
            for (String label:barcodes
            ) {
                labels.put(label);
            }
            postparams.put("labels", labels);
        }catch (JSONException e){

        }
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                GlobalCoordinator.getInstance().getServiceUrl(mContext.getString(R.string.func_bulkpickup_url)), postparams,
                new Response.Listener() {
                    @Override
                    public void onResponse(Object response) {
                        //Success Callback
                        String obj = response.toString();
                        Gson gson = new Gson();
                        Type responseType = new TypeToken<ResponseMessage<Boolean>>(){}.getType();
                        ResponseMessage<Boolean> retVal = gson.fromJson(obj,responseType);
                        if(retVal.success){
                            mBulkPickupResponse.setValue(retVal);
                        }
                        else {
                            DriverServicesError error = new DriverServicesError();
                            error.errorType = ServicesErrorType.bulkPickupError;
                            error.errorCode = retVal.errorCode;
                            error.errorMessage = retVal.errorMessage;
                            mError.setValue(error);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Failure Callback
                        DriverServicesError error = new DriverServicesError();
                        error.errorType = ServicesErrorType.generalError;
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
                headers.put(GlobalCoordinator.SERVICE_HEADER_TOKEN, mToken.getValue().Token);
                headers.put(GlobalCoordinator.SERVICE_HEADER_CONTENT,GlobalCoordinator.SERVICE_APPLICATION_JSON);
                headers.put(GlobalCoordinator.SERVICE_HEADER_FUNCTION, mContext.getString(R.string.func_bulkpickup_key));
                return headers;
            }
        };


// Add the request to the RequestQueue.
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(jsonObjReq);

    }



    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void checkOutBarcodes(String buildingCode, LinkedList<String> barcodes, DriverLocation driverLocation, String nowFormatted){
        RequestQueue queue = Volley.newRequestQueue(mContext);

        JSONObject postparams = new JSONObject();
        try {
            postparams.put("buildingCode", buildingCode);
            postparams.put("longitude", driverLocation.longitude);
            postparams.put("latitude", driverLocation.latitude);
            postparams.put("activityDate", nowFormatted);

            JSONArray labels = new JSONArray();
            for (String label:barcodes
            ) {
                labels.put(label);
            }
            postparams.put("labels", labels);
        }catch (JSONException e){

        }
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                GlobalCoordinator.getInstance().getServiceUrl(mContext.getString(R.string.func_checkout_url)), postparams,
                new Response.Listener() {
                    @Override
                    public void onResponse(Object response) {
                        //Success Callback
                        String obj = response.toString();
                        Gson gson = new Gson();
                        Type responseType = new TypeToken<ResponseMessage<Boolean>>(){}.getType();
                        ResponseMessage<Boolean> retVal = gson.fromJson(obj,responseType);
                        if(retVal.success){
                            mCheckOutResponse.setValue(retVal);
                        }
                        else {
                            DriverServicesError error = new DriverServicesError();
                            error.errorType = ServicesErrorType.checkOutError;
                            error.errorCode = retVal.errorCode;
                            error.errorMessage = retVal.errorMessage;
                            mError.setValue(error);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Failure Callback
                        DriverServicesError error = new DriverServicesError();
                        error.errorType = ServicesErrorType.generalError;
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
                headers.put(GlobalCoordinator.SERVICE_HEADER_TOKEN, mToken.getValue().Token);
                headers.put(GlobalCoordinator.SERVICE_HEADER_CONTENT,GlobalCoordinator.SERVICE_APPLICATION_JSON);
                headers.put(GlobalCoordinator.SERVICE_HEADER_FUNCTION, mContext.getString(R.string.func_checkout_key));
                return headers;
            }
        };


// Add the request to the RequestQueue.
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(jsonObjReq);




    }

    public void scanBuildingNFC(final String buildingCode){
        RequestQueue queue = Volley.newRequestQueue(mContext);


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                GlobalCoordinator.getInstance().getServiceUrl(mContext.getString(R.string.func_scanbuilding_url))+buildingCode, null,
                new Response.Listener() {
                    @Override
                    public void onResponse(Object response) {
                        String obj = response.toString();
                        Gson gson = new Gson();
                        Type responseType = new TypeToken<ResponseMessage<ScanBuilding>>(){}.getType();
                        ResponseMessage<ScanBuilding> retVal = gson.fromJson(obj,responseType);
                        if(retVal.success){
                            mBuildingNFC.setValue(retVal);
                            DriverRepository.scannedLocation.setValue(new ScannedLocation(buildingCode, retVal.data.description));
                        }
                        else {
                            DriverServicesError error = new DriverServicesError();
                            error.errorType = ServicesErrorType.scanBuildingError;
                            error.errorCode = retVal.errorCode;
                            error.errorMessage = retVal.errorMessage;
                            mError.setValue(error);
                        }
                    }


                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Failure Callback
                        DriverServicesError error = new DriverServicesError();
                        error.errorType = ServicesErrorType.generalError;
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
                headers.put(GlobalCoordinator.SERVICE_HEADER_TOKEN, mToken.getValue().Token);
                headers.put(GlobalCoordinator.SERVICE_HEADER_CONTENT,GlobalCoordinator.SERVICE_APPLICATION_JSON);
                headers.put(GlobalCoordinator.SERVICE_HEADER_FUNCTION, mContext.getString(R.string.func_scanbuilding_key));
                return headers;
            }
        };


// Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);


    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void liaisonBarcodes(String buildingCode, double longitude, double latitude, String dateTime, LinkedList<String> barcodes){
        RequestQueue queue = Volley.newRequestQueue(mContext);

        JSONObject postparams = new JSONObject();
        try {
            postparams.put("buildingCode", buildingCode);
            postparams.put("longitude", longitude);
            postparams.put("latitude", latitude);
            postparams.put("activityDate", dateTime);

            JSONArray labels = new JSONArray();
            for (String label:barcodes
            ) {
                labels.put(label);
            }
            postparams.put("labels", labels);
        }catch (JSONException e){

        }
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                GlobalCoordinator.getInstance().getServiceUrl(mContext.getString(R.string.func_liaison_url)), postparams,
                new Response.Listener() {
                    @Override
                    public void onResponse(Object response) {
                        //Success Callback
                        String obj = response.toString();
                        Gson gson = new Gson();
                        Type responseType = new TypeToken<ResponseMessage<Boolean>>(){}.getType();
                        ResponseMessage<Boolean> retVal = gson.fromJson(obj,responseType);
                        mLiaisonResponse.setValue(retVal);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Failure Callback
                        DriverServicesError error = new DriverServicesError();
                        error.errorType = ServicesErrorType.liaisonError;
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
                headers.put(GlobalCoordinator.SERVICE_HEADER_TOKEN, mToken.getValue().Token);
                headers.put(GlobalCoordinator.SERVICE_HEADER_CONTENT,GlobalCoordinator.SERVICE_APPLICATION_JSON);
                headers.put(GlobalCoordinator.SERVICE_HEADER_FUNCTION, mContext.getString(R.string.func_liaison_key));
                return headers;
            }
        };


// Add the request to the RequestQueue.
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(jsonObjReq);

    }


    public void getSpecialDeliveries(){
        RequestQueue queue = Volley.newRequestQueue(mContext);


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                GlobalCoordinator.getInstance().getServiceUrl(mContext.getString(R.string.func_specialdeliveries_url)), null,
                new Response.Listener() {
                    @Override
                    public void onResponse(Object response) {
                        String obj = response.toString();
                        Gson gson = new Gson();
                        Type responseType = new TypeToken<ResponseMessage<SpecialDelivery[]>>(){}.getType();
                        ResponseMessage<SpecialDelivery[]> retVal = gson.fromJson(obj,responseType);
                        if(retVal.success){
                            mSpecialDeliveriesResponse.setValue(retVal);
                        }
                        else {
                            DriverServicesError error = new DriverServicesError();
                            error.errorType = ServicesErrorType.specialDeliveriesError;
                            error.errorCode = retVal.errorCode;
                            error.errorMessage = retVal.errorMessage;
                            mError.setValue(error);
                        }
                    }


                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Failure Callback
                        DriverServicesError error = new DriverServicesError();
                        error.errorType = ServicesErrorType.generalError;
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
                headers.put(GlobalCoordinator.SERVICE_HEADER_TOKEN, mToken.getValue().Token);
                headers.put(GlobalCoordinator.SERVICE_HEADER_CONTENT,GlobalCoordinator.SERVICE_APPLICATION_JSON);
                headers.put(GlobalCoordinator.SERVICE_HEADER_FUNCTION, mContext.getString(R.string.func_specialdeliveries_key));
                return headers;
            }
        };


// Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);


    }

    public void getOrderStops(final String orderType){
        //if(orderType != null && orderType.isEmpty())
        //    orderType = "50490C2C-5DF6-4433-9B46-6BB671799773";


        RequestQueue queue = Volley.newRequestQueue(mContext);


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                GlobalCoordinator.getInstance().getServiceUrl(mContext.getString(R.string.func_deliverystops_url)) + orderType, null,
                new Response.Listener() {
                    @Override
                    public void onResponse(Object response) {
                        String obj = response.toString();
                        Gson gson = new Gson();
                        Type responseType = new TypeToken<ResponseMessage<JsonStop[]>>(){}.getType();
                        ResponseMessage<JsonStop[]> retVal = gson.fromJson(obj,responseType);
                        if(retVal.success){
                            switch (OrderType.get(orderType)){
                                case DELIVERY:{
                                    DriverServices.deliveryOrdersLoaded.setValue(true);
                                    DriverServices.deliveryOrdersTimeStamp.setValue(new Date());
                                    break;
                                }
                                case PICKUP:{
                                    DriverServices.pickupOrdersLoaded.setValue(true);
                                    DriverServices.pickupOrdersTimeStamp.setValue(new Date());
                                    break;
                                }
                                case FORCEPICKUP:{
                                    break;
                                }
                            }
                            if(GlobalCoordinator.testMode && retVal.dataCount == 0){

                            }
                            if(!OrderType.FORCEPICKUP.equals(OrderType.get(orderType)))
                                mStopsResponse.setValue(retVal);
                            else
                                mForceAssignedStopsResponse.setValue(retVal);
                        }
                        else {
                            DriverServicesError error = new DriverServicesError();
                            error.errorType = ServicesErrorType.stopsError;
                            error.errorCode = retVal.errorCode;
                            error.errorMessage = retVal.errorMessage;
                            mError.setValue(error);
                        }
                    }


                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Failure Callback
                        DriverServicesError error = new DriverServicesError();
                        error.errorType = ServicesErrorType.generalError;
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

                if(GlobalCoordinator.testMode)
                    //headers.put(GlobalCoordinator.SERVICE_HEADER_TOKEN, "01542edd-cdd9-4389-a574-9ed0041d4c92");
                    headers.put(GlobalCoordinator.SERVICE_HEADER_TOKEN, "1cb73983-0290-4d64-9ff8-d3463fd20752");



                else
                    headers.put(GlobalCoordinator.SERVICE_HEADER_TOKEN, mToken.getValue().Token);


                //headers.put(GlobalCoordinator.SERVICE_HEADER_TOKEN, mToken.getValue());
                headers.put(GlobalCoordinator.SERVICE_HEADER_CONTENT,GlobalCoordinator.SERVICE_APPLICATION_JSON);
                headers.put(GlobalCoordinator.SERVICE_HEADER_FUNCTION, mContext.getString(R.string.func_deliverystops_key));
                return headers;
            }
        };


        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
// Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);


    }


    public void receivedStops(LinkedList<ReceivedStop> receivedStops){
        RequestQueue queue = Volley.newRequestQueue(mContext);
        JSONObject postParams = new JSONObject();

        try {

            JSONArray stops = new JSONArray();
            for (ReceivedStop receivedStop:receivedStops
            ) {
                JSONObject jsonStop = new JSONObject();
                jsonStop.put("stopId",receivedStop.stopId);
                jsonStop.put("activityDate",receivedStop.activityDate);
                jsonStop.put("orderTypeId",receivedStop.orderTypeId);
                JSONArray jsonOrderIds = new JSONArray();
                for (String orderId :
                        receivedStop.orderIds) {
                    jsonOrderIds.put(orderId);
                }
                jsonStop.put("orderIds",jsonOrderIds);

                stops.put(jsonStop);
            }
            postParams.put("stops",stops);

        }catch (JSONException e){
            String s = e.getMessage();
        }


        JSONObject r = new JSONObject();


        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                GlobalCoordinator.getInstance().getServiceUrl(mContext.getString(R.string.func_receivedStops_url)), postParams,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        //Success Callback
                        String obj = response.toString();
                        Gson gson = new Gson();
                        Type responseType = new TypeToken<ResponseMessage<Boolean>>(){}.getType();
                        ResponseMessage<Boolean> retVal = gson.fromJson(obj,responseType);
                        mReceivedStopsResponse.setValue(retVal);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Failure Callback
                        DriverServicesError error = new DriverServicesError();
                        error.errorType = ServicesErrorType.receivedStopsError;
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
                headers.put(GlobalCoordinator.SERVICE_HEADER_TOKEN, mToken.getValue().Token);
                headers.put(GlobalCoordinator.SERVICE_HEADER_CONTENT,GlobalCoordinator.SERVICE_APPLICATION_JSON);
                headers.put(GlobalCoordinator.SERVICE_HEADER_FUNCTION, mContext.getString(R.string.func_receivedStops_key));
                return headers;
            }
        };


// Add the request to the RequestQueue.
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(jsonObjReq);


    }

    public void acceptStop(String stopId, double longitude, double latitude, String dateTime){

        RequestQueue queue = Volley.newRequestQueue(mContext);


        JSONObject postparams = new JSONObject();

        try {
            postparams.put("stopId", stopId);
            postparams.put("longitude", longitude);
            postparams.put("latitude", latitude);
            postparams.put("activityDate", dateTime);
            //if(actionType.equals(StopActionType.Reject))
            //    postparams.put("actionRouteId", actionRouteId);

        }catch (JSONException e){

        }

        String functionUrl = "";
        String functionKey = "";
        functionUrl = mContext.getString(R.string.func_acceptstop_url);
        functionKey = mContext.getString(R.string.func_acceptstop_key);

        /*
        switch (actionType){
            case Start:{
                functionUrl = mContext.getString(R.string.func_startstop_url);
                functionKey = mContext.getString(R.string.func_startstop_key);
                break;
            }
            case Stop:{
                functionUrl = mContext.getString(R.string.func_stopstop_url);
                functionKey = mContext.getString(R.string.func_stopstop_key);
                break;
            }
            case Accept:{
                functionUrl = mContext.getString(R.string.func_acceptstop_url);
                functionKey = mContext.getString(R.string.func_acceptstop_key);
                break;
            }
            case Reject:{
                functionUrl = mContext.getString(R.string.func_rejectstop_url);
                functionKey = mContext.getString(R.string.func_rejectstop_key);
                break;
            }
        }

         */


        final String finalFunctionKey = functionKey;

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                GlobalCoordinator.getInstance().getServiceUrl(functionUrl), postparams,
                new Response.Listener() {
                    @Override
                    public void onResponse(Object response) {
                        //Success Callback
                        String obj = response.toString();
                        Gson gson = new Gson();
                        Type responseType = new TypeToken<ResponseMessage<Boolean>>(){}.getType();
                        ResponseMessage<Boolean> retVal = gson.fromJson(obj,responseType);
                        mAcceptStopResponse.setValue(retVal);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Failure Callback
                        DriverServicesError error = new DriverServicesError();
                        error.errorType = ServicesErrorType.acceptStopError;
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
                headers.put(GlobalCoordinator.SERVICE_HEADER_TOKEN, mToken.getValue().Token);
                headers.put(GlobalCoordinator.SERVICE_HEADER_CONTENT,GlobalCoordinator.SERVICE_APPLICATION_JSON);
                headers.put(GlobalCoordinator.SERVICE_HEADER_FUNCTION, finalFunctionKey);
                return headers;
            }
        };


// Add the request to the RequestQueue.
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(jsonObjReq);


    }

    public void rejectStop(String stopId, double longitude, double latitude, String dateTime, String actionRouteId){

        RequestQueue queue = Volley.newRequestQueue(mContext);


        JSONObject postparams = new JSONObject();

        try {
            postparams.put("stopId", stopId);
            postparams.put("longitude", longitude);
            postparams.put("latitude", latitude);
            postparams.put("activityDate", dateTime);
            postparams.put("actionRouteId", actionRouteId);

        }catch (JSONException e){

        }

        String functionUrl = "";
        String functionKey = "";
        functionUrl = mContext.getString(R.string.func_rejectstop_url);
        functionKey = mContext.getString(R.string.func_rejectstop_key);

        /*
        switch (actionType){
            case Start:{
                functionUrl = mContext.getString(R.string.func_startstop_url);
                functionKey = mContext.getString(R.string.func_startstop_key);
                break;
            }
            case Stop:{
                functionUrl = mContext.getString(R.string.func_stopstop_url);
                functionKey = mContext.getString(R.string.func_stopstop_key);
                break;
            }
            case Accept:{
                functionUrl = mContext.getString(R.string.func_acceptstop_url);
                functionKey = mContext.getString(R.string.func_acceptstop_key);
                break;
            }
            case Reject:{
                functionUrl = mContext.getString(R.string.func_rejectstop_url);
                functionKey = mContext.getString(R.string.func_rejectstop_key);
                break;
            }
        }

         */


        final String finalFunctionKey = functionKey;

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                GlobalCoordinator.getInstance().getServiceUrl(functionUrl), postparams,
                new Response.Listener() {
                    @Override
                    public void onResponse(Object response) {
                        //Success Callback
                        String obj = response.toString();
                        Gson gson = new Gson();
                        Type responseType = new TypeToken<ResponseMessage<Boolean>>(){}.getType();
                        ResponseMessage<Boolean> retVal = gson.fromJson(obj,responseType);
                        mRejectStopResponse.setValue(retVal);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Failure Callback
                        DriverServicesError error = new DriverServicesError();
                        error.errorType = ServicesErrorType.rejectStopError;
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
                headers.put(GlobalCoordinator.SERVICE_HEADER_TOKEN, mToken.getValue().Token);
                headers.put(GlobalCoordinator.SERVICE_HEADER_CONTENT,GlobalCoordinator.SERVICE_APPLICATION_JSON);
                headers.put(GlobalCoordinator.SERVICE_HEADER_FUNCTION, finalFunctionKey);
                return headers;
            }
        };


// Add the request to the RequestQueue.
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(jsonObjReq);


    }

    public void startStop(String stopId, double longitude, double latitude, String dateTime) {

        //actionStop(StopActionType.Start, stopId,longitude,latitude,dateTime,"");
        //return;

        StartStopRequest fullJson = new StartStopRequest();
        fullJson.stopId = stopId;
        fullJson.longitude = longitude;
        fullJson.latitude = latitude;
        fullJson.activityDate = dateTime;
        fullJson.reason = "";

        mStartStopResponse.setValue(
            DriverServices.<StartStopRequest>enqueOneTimeRequest(fullJson, GlobalCoordinator.getInstance().getServiceUrl(mContext.getString(R.string.func_startstop_url)),mContext.getString(R.string.func_startstop_key), "startStop")
        );
        return;

        /*
        Gson g = new Gson();
        String jsonString = g.toJson(fullJson);


        Data receivedStopsData = new Data.Builder()
                .putString("jsonToSend", jsonString)
                .putString("url",GlobalCoordinator.getInstance().getServiceUrl(mContext.getString(R.string.func_startstop_url)))
                .putString("key",mContext.getString(R.string.func_startstop_key))
                .build();

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED).build();

        OneTimeWorkRequest oneTimeRequest = new OneTimeWorkRequest.Builder(PostToServerWorker.class)
                .setInputData(receivedStopsData)
                .setConstraints(constraints)
                .addTag("demo")
                .build();


        WorkManager.getInstance().enqueueUniqueWork("RS"+GlobalCoordinator.getNowFormatted(), ExistingWorkPolicy.KEEP,oneTimeRequest);

        ResponseMessage<Boolean> enqued = new ResponseMessage<>();
        enqued.success = true;
        enqued.data = true;
        enqued.errorMessage = "";
        enqued.message = "Enqueued";

        mStartStopResponse.setValue(enqued);


        return;


         */

        /*


        RequestQueue queue = Volley.newRequestQueue(mContext);


        JSONObject postparams = new JSONObject();

        try {
            postparams.put("stopId", stopId);
            postparams.put("longitude", longitude);
            postparams.put("latitude", latitude);
            postparams.put("activityDate", dateTime);

        }catch (JSONException e){

        }
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                GlobalCoordinator.getInstance().getServiceUrl(mContext.getString(R.string.func_startstop_url)), postparams,
                new Response.Listener() {
                    @Override
                    public void onResponse(Object response) {
                        //Success Callback
                        String obj = response.toString();
                        Gson gson = new Gson();
                        Type responseType = new TypeToken<ResponseMessage<Boolean>>(){}.getType();
                        ResponseMessage<Boolean> retVal = gson.fromJson(obj,responseType);
                        if(retVal.success){
                            mStartStopResponse.setValue(retVal);
                        }
                        else {
                            //TODO: remove the line below
                            if(GlobalCoordinator.testMode){
                            retVal.success = true;
                            mStartStopResponse.setValue(retVal);
                            return;}else {

                            DriverServicesError error = new DriverServicesError();
                            error.errorType = ServicesErrorType.startStopError;
                            error.errorCode = retVal.errorCode;
                            error.errorMessage = retVal.errorMessage;
                            mError.setValue(error);



                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Failure Callback
                        DriverServicesError error = new DriverServicesError();
                        error.errorType = ServicesErrorType.generalError;
                        error.errorMessage = volleyError.getMessage();
                        error.volleyError = volleyError;
                        mError.setValue(error);
                    }
                })


        {
            @Override
            public Map getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put(GlobalCoordinator.SERVICE_HEADER_TOKEN, mToken.getValue().Token);
                headers.put(GlobalCoordinator.SERVICE_HEADER_CONTENT,GlobalCoordinator.SERVICE_APPLICATION_JSON);
                headers.put(GlobalCoordinator.SERVICE_HEADER_FUNCTION, mContext.getString(R.string.func_startstop_key));
                return headers;
            }
        };


// Add the request to the RequestQueue.
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(jsonObjReq);

*/


    }

    private static <T> ResponseMessage<Boolean> enqueOneTimeRequest(T toSend, String url, String key, String tag){

        ResponseMessage<Boolean> enqued = new ResponseMessage<>();
        enqued.success = true;
        enqued.data = true;
        enqued.errorMessage = "";
        enqued.message = "Enqueued";
        Gson g = new Gson();
        String jsonString = g.toJson(toSend);


        Data toSendData = new Data.Builder()
                .putString("jsonToSend", jsonString)
                .putString("url",url)
                .putString("key",key)
                .build();

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED).build();

        OneTimeWorkRequest oneTimeRequest = new OneTimeWorkRequest.Builder(PostToServerWorker.class)
                .setInputData(toSendData)
                .setConstraints(constraints)
                .addTag(tag)
                //Todo: cheeck the below method to set the backoff criteeria properly
                .setBackoffCriteria(BackoffPolicy.LINEAR,60 * 1000, TimeUnit.MILLISECONDS)
                .build();


        WorkManager.getInstance().enqueueUniqueWork("RS"+GlobalCoordinator.getNowFormatted(), ExistingWorkPolicy.KEEP,oneTimeRequest);


        return enqued;
    }

    public void stopStop(String stopId, double longitude, double latitude, String dateTime, String reason) {
        //actionStop(StopActionType.Stop, stopId, longitude, latitude, dateTime, reason);
        //return;


        StartStopRequest fullJson = new StartStopRequest();
        fullJson.stopId = stopId;
        fullJson.longitude = longitude;
        fullJson.latitude = latitude;
        fullJson.activityDate = dateTime;
        fullJson.reason = reason;

        mStopStopResponse.setValue(
            DriverServices.<StartStopRequest>enqueOneTimeRequest(fullJson, GlobalCoordinator.getInstance().getServiceUrl(mContext.getString(R.string.func_stopstop_url)),mContext.getString(R.string.func_stopstop_key), "stopStop")
        );
        return;

        //enqueOneTimeRequest(fullJson, GlobalCoordinator.getInstance().getServiceUrl(mContext.getString(R.string.func_stopstop_url)),mContext.getString(R.string.func_stopstop_key), "stopStop" );

        /*
        Gson g = new Gson();
        String jsonString = g.toJson(fullJson);


        Data receivedStopsData = new Data.Builder()
                .putString("jsonToSend", jsonString)
                .putString("url",GlobalCoordinator.getInstance().getServiceUrl(mContext.getString(R.string.func_stopstop_url)))
                .putString("key",mContext.getString(R.string.func_stopstop_key))
                .build();

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED).build();

        OneTimeWorkRequest oneTimeRequest = new OneTimeWorkRequest.Builder(PostToServerWorker.class)
                .setInputData(receivedStopsData)
                .setConstraints(constraints)
                .addTag("demo")
                .build();


        WorkManager.getInstance().enqueueUniqueWork("RS"+GlobalCoordinator.getNowFormatted(), ExistingWorkPolicy.KEEP,oneTimeRequest);

        ResponseMessage<Boolean> enqued = new ResponseMessage<>();
        enqued.success = true;
        enqued.data = true;
        enqued.errorMessage = "";
        enqued.message = "Enqueued";

        mStopStopResponse.setValue(enqued);


        return;

         */

        /*
        RequestQueue queue = Volley.newRequestQueue(mContext);


        JSONObject postparams = new JSONObject();

        try {
            postparams.put("stopId", stopId);
            postparams.put("longitude", longitude);
            postparams.put("latitude", latitude);
            postparams.put("activityDate", dateTime);
            postparams.put("reason", reason);

        }catch (JSONException e){

        }
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                GlobalCoordinator.getInstance().getServiceUrl(mContext.getString(R.string.func_stopstop_url)), postparams,
                new Response.Listener() {
                    @Override
                    public void onResponse(Object response) {
                        //Success Callback
                        String obj = response.toString();
                        Gson gson = new Gson();
                        Type responseType = new TypeToken<ResponseMessage<Boolean>>(){}.getType();
                        ResponseMessage<Boolean> retVal = gson.fromJson(obj,responseType);
                        if(retVal.success){
                            mStopStopResponse.setValue(retVal);
                        }
                        else {

                            DriverServicesError error = new DriverServicesError();
                            error.errorType = ServicesErrorType.startStopError;
                            error.errorCode = retVal.errorCode;
                            error.errorMessage = retVal.errorMessage;
                            mError.setValue(error);


                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Failure Callback
                        DriverServicesError error = new DriverServicesError();
                        error.errorType = ServicesErrorType.generalError;
                        error.errorMessage = volleyError.getMessage();
                        error.volleyError = volleyError;
                        mError.setValue(error);
                    }
                })



        {
            @Override
            public Map getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put(GlobalCoordinator.SERVICE_HEADER_TOKEN, mToken.getValue().Token);
                headers.put(GlobalCoordinator.SERVICE_HEADER_CONTENT,GlobalCoordinator.SERVICE_APPLICATION_JSON);
                headers.put(GlobalCoordinator.SERVICE_HEADER_FUNCTION, mContext.getString(R.string.func_stopstop_key));
                return headers;
            }
        };


// Add the request to the RequestQueue.
        queue.add(jsonObjReq);



         */


    }

    //Used for delivery
    public void sendDelivery(SendDeliveryRequest sendDeliveryRequest){

        String orderURL = mContext.getString(R.string.func_senddeliverycod_url);
        String orderKEY = mContext.getString(R.string.func_senddeliverycod_key);

        //Place the full json string into the processQueue table in the Database with a unique ID key
        String uniqueID = UUID.randomUUID().toString();

        Gson g = new Gson();
        String jsonString = g.toJson(sendDeliveryRequest);

        ProcessQueueRepository processQueueRepository = new ProcessQueueRepository(mContext);

        QueueItemType queueItemType = QueueItemType.Delivery;
        switch (sendDeliveryRequest.orderType){
            case PICKUP: queueItemType = QueueItemType.Pickup;
            break;
            case DELIVERY: queueItemType = QueueItemType.Delivery;
            break;
            default: queueItemType = QueueItemType.Delivery;
            break;
        }
        processQueueRepository.putProcessQueue(uniqueID, jsonString, queueItemType);


        //in the queue, put "DB" for jsonToSend in order to pick it up from the database
        //and also put "uniqueID" to identify which record to get
        Data receivedStopsData = new Data.Builder()
                .putString("jsonToSend", "DB")
                .putString("uniqueID", uniqueID)
                .putString("url",GlobalCoordinator.getInstance().getServiceUrl(orderURL))
                .putString("key",orderKEY)
                .build();

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED).build();

        OneTimeWorkRequest oneTimeRequest = new OneTimeWorkRequest.Builder(PostToServerWorker.class)
                .setInputData(receivedStopsData)
                .setConstraints(constraints)
                .addTag("demo")
                .setBackoffCriteria(BackoffPolicy.LINEAR, 60 * 1000, TimeUnit.MILLISECONDS)

                .build();


        WorkManager.getInstance().enqueueUniqueWork("RS"+GlobalCoordinator.getNowFormatted(), ExistingWorkPolicy.KEEP,oneTimeRequest);

        ResponseMessage<Boolean> enqued = new ResponseMessage<>();
        enqued.success = true;
        enqued.data = true;
        enqued.errorMessage = "";
        enqued.message = "Enqueued";

        mSendDeliveryResponse.setValue(enqued);

    }

    //Used for pickup
    public void sendPickup(SendPickupRequest sendPickupRequest){

        String orderURL = mContext.getString(R.string.func_sendpickupcod_url);
        String orderKEY = mContext.getString(R.string.func_sendpickupcod_key);


        //Place the full json string into the processQueue table in the Database with a unique ID key
        String uniqueID = UUID.randomUUID().toString();

        Gson g = new Gson();
        String jsonString = g.toJson(sendPickupRequest);

        ProcessQueueRepository processQueueRepository = new ProcessQueueRepository(mContext);

        QueueItemType queueItemType = QueueItemType.Pickup;
        switch (sendPickupRequest.orderType){
            case PICKUP: queueItemType = QueueItemType.Pickup;
                break;
            case DELIVERY: queueItemType = QueueItemType.Delivery;
                break;
            default: queueItemType = QueueItemType.Pickup;
                break;
        }
        processQueueRepository.putProcessQueue(uniqueID, jsonString, queueItemType);


        //in the queue, put "DB" for jsonToSend in order to pick it up from the database
        //and also put "uniqueID" to identify which record to get
        Data receivedStopsData = new Data.Builder()
                .putString("jsonToSend", "DB")
                .putString("uniqueID", uniqueID)
                .putString("url",GlobalCoordinator.getInstance().getServiceUrl(orderURL))
                .putString("key",orderKEY)
                .build();

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED).build();

        OneTimeWorkRequest oneTimeRequest = new OneTimeWorkRequest.Builder(PostToServerWorker.class)
                .setInputData(receivedStopsData)
                .setConstraints(constraints)
                .addTag("demo")
                .setBackoffCriteria(BackoffPolicy.LINEAR,60 * 1000, TimeUnit.MILLISECONDS)

                .build();


        WorkManager.getInstance().enqueueUniqueWork("RS"+GlobalCoordinator.getNowFormatted(), ExistingWorkPolicy.KEEP,oneTimeRequest);

        ResponseMessage<Boolean> enqued = new ResponseMessage<>();
        enqued.success = true;
        enqued.data = true;
        enqued.errorMessage = "";
        enqued.message = "Enqueued";


        mSendPickupResponse.setValue(enqued);

    }


    public void sendNonDeliveryOffline(final OrderType orderType, double longitude, double latitude, String activityDate, String label, String actionRouteId, String orderId){

        String nonDeliveryURL = "";
        String nonDeliveryKey = "";
        switch (orderType){
            case DELIVERY:{
                nonDeliveryURL = mContext.getString(R.string.func_sendnondelivery_url);
                nonDeliveryKey = mContext.getString(R.string.func_sendnondelivery_key);
                break;
            }
            case PICKUP:{
                nonDeliveryURL = mContext.getString(R.string.func_sendnonpickup_url);
                nonDeliveryKey = mContext.getString(R.string.func_sendnonpickup_key);
                break;
            }
        }
        //Place the full json string into the processQueue table in the Database with a unique ID key
        String uniqueID = UUID.randomUUID().toString();


        JSONObject postparams = new JSONObject();
        try {
            //postparams.put("nfcBuilding", nfcBuilding);
            postparams.put("label", label);
            postparams.put("actionRouteId", actionRouteId);
            postparams.put("longitude", longitude);
            postparams.put("latitude", latitude);
            postparams.put("activityDate", activityDate);
            postparams.put("orderid", orderId);
        }catch (JSONException e){
            String ex = e.toString();
            DriverServicesError error = new DriverServicesError();
            error.errorType = ServicesErrorType.generalError;
            error.errorMessage = ex;
            error.volleyError = null;
            mError.setValue(error);
            return;
        }
        String jsonString = postparams.toString();

        ProcessQueueRepository processQueueRepository = new ProcessQueueRepository(mContext);

        QueueItemType queueItemType = QueueItemType.NonDelivery;

        processQueueRepository.putProcessQueue(uniqueID, jsonString, queueItemType);


        //in the queue, put "DB" for jsonToSend in order to pick it up from the database
        //and also put "uniqueID" to identify which record to get
        Data nonDeliveryData = new Data.Builder()
                .putString("jsonToSend", "DB")
                .putString("uniqueID", uniqueID)
                .putString("url",GlobalCoordinator.getInstance().getServiceUrl(nonDeliveryURL))
                .putString("key",nonDeliveryKey)
                .build();

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED).build();

        OneTimeWorkRequest oneTimeRequest = new OneTimeWorkRequest.Builder(PostToServerWorker.class)
                .setInputData(nonDeliveryData)
                .setConstraints(constraints)
                .addTag("demo")
                .setBackoffCriteria(BackoffPolicy.LINEAR, 60 * 1000, TimeUnit.MILLISECONDS)
                .build();


        WorkManager.getInstance().enqueueUniqueWork("RS"+GlobalCoordinator.getNowFormatted(), ExistingWorkPolicy.KEEP,oneTimeRequest);

        ResponseMessage<Boolean> enqued = new ResponseMessage<>();
        enqued.success = true;
        enqued.data = true;
        enqued.errorMessage = "";
        enqued.message = "Enqueued";

        mSendNonDeliveryResponse.setValue(enqued);
    }
    public void sendNonDelivery(final OrderType orderType, String nfcBuilding, double longitude, double latitude, String activityDate, String label, String actionRouteId, String orderId) {
        RequestQueue queue = Volley.newRequestQueue(mContext);

        String orderURL = "";
        String orderKEY = "";
        switch (orderType){
            case DELIVERY:{
                orderURL = mContext.getString(R.string.func_sendnondelivery_url);
                orderKEY = mContext.getString(R.string.func_sendnondelivery_key);
                break;
            }
            case PICKUP:{
                orderURL = mContext.getString(R.string.func_sendnonpickup_url);
                orderKEY = mContext.getString(R.string.func_sendnonpickup_key);
                break;
            }
        }

        JSONObject postparams = new JSONObject();
        try {
            //postparams.put("nfcBuilding", nfcBuilding);
            postparams.put("label", label);
            postparams.put("actionRouteId", actionRouteId);
            postparams.put("longitude", longitude);
            postparams.put("latitude", latitude);
            postparams.put("activityDate", activityDate);
            postparams.put("orderid", orderId);
        }catch (JSONException e){
            String ex = e.toString();
            DriverServicesError error = new DriverServicesError();
            error.errorType = ServicesErrorType.generalError;
            error.errorMessage = ex;
            error.volleyError = null;
            mError.setValue(error);
            return;
        }

        final String finalOrderKEY = orderKEY;
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                GlobalCoordinator.getInstance().getServiceUrl(orderURL), postparams,
                new Response.Listener() {
                    @Override
                    public void onResponse(Object response) {
                        //Success Callback
                        String obj = response.toString();
                        Gson gson = new Gson();
                        Type responseType = new TypeToken<ResponseMessage<Boolean>>(){}.getType();
                        ResponseMessage<Boolean> retVal = gson.fromJson(obj,responseType);

                        if(orderType.equals(OrderType.DELIVERY))
                            mSendNonDeliveryResponse.setValue(retVal);
                        if(orderType.equals(OrderType.PICKUP))
                            mSendNonPickupResponse.setValue(retVal);

                        /*
                        if(retVal.success){
                            if(orderType.equals(OrderType.DELIVERY))
                                mSendNonDeliveryResponse.setValue(retVal);
                            if(orderType.equals(OrderType.PICKUP))
                                mSendNonPickupResponse.setValue(retVal);
                        }
                        else {
                            DriverServicesError error = new DriverServicesError();
                            error.errorType = ServicesErrorType.sendNonDeliveryError;
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
                        error.errorType = ServicesErrorType.generalError;
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
                headers.put(GlobalCoordinator.SERVICE_HEADER_TOKEN, mToken.getValue().Token);
                headers.put(GlobalCoordinator.SERVICE_HEADER_CONTENT,GlobalCoordinator.SERVICE_APPLICATION_JSON);
                headers.put(GlobalCoordinator.SERVICE_HEADER_FUNCTION, finalOrderKEY);
                return headers;
            }
        };


        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
// Add the request to the RequestQueue.
        queue.add(jsonObjReq);

    }

    public void sendSignature(String imageName, final byte[] signature) {



        String imageString = Base64.encodeToString(signature,Base64.DEFAULT);


        ProcessQueueRepository processQueueRepository = new ProcessQueueRepository(mContext);
        processQueueRepository.putProcessQueue(imageName, imageString, QueueItemType.Signature);

        Data uploadImageData = new Data.Builder()
                .putString("itemName",imageName)
                .putString("imageName",imageName)
                .build();
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED).build();

        OneTimeWorkRequest oneTimeRequest = new OneTimeWorkRequest.Builder(UploadSignatureWorker.class)
                .setInputData(uploadImageData)
                .setConstraints(constraints)
                .addTag("demo")
                .setBackoffCriteria(BackoffPolicy.LINEAR,60 * 1000, TimeUnit.MILLISECONDS)

                .build();

        WorkManager.getInstance().enqueueUniqueWork("UI" + GlobalCoordinator.getNowFormatted(), ExistingWorkPolicy.KEEP,oneTimeRequest);

        ResponseMessage<Boolean> retVal = new ResponseMessage<>();
        retVal.success = true;
        retVal.message = "Image Queued";
        mSendSignatureResponse.setValue(retVal);

        return;

        /*
        try {

            //String uncompressedString = Base64.encodeToString(signature, Base64.DEFAULT);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            GZIPOutputStream gzip1 = new GZIPOutputStream(baos);
            gzip1.write(signature);
            gzip1.close();
            byte[] signatureCompressed = baos.toByteArray();
            String zippedString = Base64.encodeToString(signatureCompressed, Base64.DEFAULT);

            GZIPInputStream gzip2 = new GZIPInputStream(new ByteArrayInputStream(Base64.decode(zippedString, Base64.DEFAULT)));
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[256];
            int n = gzip2.read(buffer);
            while(n >= 0){
                outputStream.write(buffer,0,n);
                n = gzip2.read(buffer);
            }
            byte[] signatureUnCompressed = outputStream.toByteArray();
            signature.equals(signatureUnCompressed);

        } catch (IOException e) {
            e.printStackTrace();
        }




         */


        /*
        RequestQueue queue = Volley.newRequestQueue(mContext);

        final JSONObject bodyParams = new JSONObject();


        try {
            bodyParams.put("", signature);
        }catch (Exception ex){
            //TODO: Handle Exception
        }



        StringRequest jsonObjReq = new StringRequest(Request.Method.POST,
                GlobalCoordinator.getInstance().getServiceUrl(mContext.getString(R.string.func_sendSignaturebinary_url)) + imageName + ".png",
                new Response.Listener() {
                    @Override
                    public void onResponse(Object response) {
                        //Success Callback
                        String obj = response.toString();
                        Gson gson = new Gson();
                        Type responseType = new TypeToken<ResponseMessage<Boolean>>(){}.getType();
                        ResponseMessage<Boolean> retVal = gson.fromJson(obj,responseType);
                        if(retVal.success){
                            mSendSignatureResponse.setValue(retVal);
                        }
                        else {
                            DriverServicesError error = new DriverServicesError();
                            error.errorType = ServicesErrorType.sendDeliveryError;
                            error.errorCode = retVal.errorCode;
                            error.errorMessage = retVal.errorMessage;
                            mError.setValue(error);

                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Failure Callback
                        DriverServicesError error = new DriverServicesError();
                        error.errorType = ServicesErrorType.generalError;
                        error.errorMessage = volleyError.getMessage();
                        error.volleyError = volleyError;
                        mError.setValue(error);
                    }
                })



        {
            @Override
            public Map getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put(GlobalCoordinator.SERVICE_HEADER_TOKEN, mToken.getValue().Token);
                headers.put(GlobalCoordinator.SERVICE_HEADER_CONTENT,GlobalCoordinator.SERVICE_APPLICATION_JSON);
                //headers.put(GlobalCoordinator.SERVICE_HEADER_CONTENT,"application/x-www-form-urlencoded");
                headers.put(GlobalCoordinator.SERVICE_HEADER_FUNCTION, mContext.getString(R.string.func_sendSignaturebinary_key));
                return headers;
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                String body = Base64.encodeToString(signature, Base64.DEFAULT);

                try{
                    return body.getBytes("UTF-8");
                }catch (Exception e){
                    return  signature;
                }
            }
        };


// Add the request to the RequestQueue.
            jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(
                    10000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(jsonObjReq);
        */

    }



    public void getNetLocker(final String orderId){
        RequestQueue queue = Volley.newRequestQueue(mContext);


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                GlobalCoordinator.getInstance().getServiceUrl(mContext.getString(R.string.func_getnetlocker_url))+orderId, null,
                new Response.Listener() {
                    @Override
                    public void onResponse(Object response) {
                        String obj = response.toString();
                        Gson gson = new Gson();
                        Type responseType = new TypeToken<ResponseMessage<NetLockerData>>(){}.getType();
                        ResponseMessage<NetLockerData> retVal = gson.fromJson(obj,responseType);
                        mNetLockerResponse.setValue(retVal);
                        if(retVal.success){

                        }
                        else {
                            DriverServicesError error = new DriverServicesError();
                            error.errorType = ServicesErrorType.netLockerError;
                            error.errorCode = retVal.errorCode;
                            error.errorMessage = retVal.errorMessage;
                            mError.setValue(error);
                        }
                    }


                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Failure Callback
                        DriverServicesError error = new DriverServicesError();
                        error.errorType = ServicesErrorType.generalError;
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
                headers.put(GlobalCoordinator.SERVICE_HEADER_TOKEN, mToken.getValue().Token);
                headers.put(GlobalCoordinator.SERVICE_HEADER_CONTENT,GlobalCoordinator.SERVICE_APPLICATION_JSON);
                headers.put(GlobalCoordinator.SERVICE_HEADER_FUNCTION, mContext.getString(R.string.func_getnetlocker_key));
                return headers;
            }
        };


// Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);


    }


    public void retourOffline(String orderId, String retourLabel, String specialInstruction, int amount, String currency, int serviceType, boolean isManual) {

        String retourURL = mContext.getString(R.string.func_retourorder_url);
        String retourKey = mContext.getString(R.string.func_retourorder_key);

        JSONObject postparams = new JSONObject();
        try {
            postparams.put("orderId", orderId);
            postparams.put("retourLabel", retourLabel);
            //if this is a manually added retour, thn add th deetails. otherwise, just send the orderid and label (if hasRetour = true)
            if(isManual) {
                postparams.put("specialInstruction", specialInstruction);
                postparams.put("totalAmount", amount);
                postparams.put("currency", currency);
                postparams.put("serviceType", serviceType);
            }

        }catch (JSONException e){
            DriverServicesError error = new DriverServicesError();
            error.errorType = ServicesErrorType.retourError;
            error.errorCode = "-100";
            error.errorMessage = e.getMessage();
            mError.setValue(error);
            return;
        }

        //Place the full json string into the processQueue table in the Database with a unique ID key
        String uniqueID = UUID.randomUUID().toString();

        String jsonString = postparams.toString();

        ProcessQueueRepository processQueueRepository = new ProcessQueueRepository(mContext);

        QueueItemType queueItemType = QueueItemType.Retour;

        processQueueRepository.putProcessQueue(uniqueID, jsonString, queueItemType);


        //in the queue, put "DB" for jsonToSend in order to pick it up from the database
        //and also put "uniqueID" to identify which record to get
        Data retourData = new Data.Builder()
                .putString("jsonToSend", "DB")
                .putString("uniqueID", uniqueID)
                .putString("url",GlobalCoordinator.getInstance().getServiceUrl(retourURL))
                .putString("key",retourKey)
                .build();

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED).build();

        OneTimeWorkRequest oneTimeRequest = new OneTimeWorkRequest.Builder(PostToServerWorker.class)
                .setInputData(retourData)
                .setConstraints(constraints)
                .addTag("demo")
                .setBackoffCriteria(BackoffPolicy.LINEAR, 60 * 1000, TimeUnit.MILLISECONDS)
                .build();


        WorkManager.getInstance().enqueueUniqueWork("RS"+GlobalCoordinator.getNowFormatted(), ExistingWorkPolicy.KEEP,oneTimeRequest);

        ResponseMessage<Boolean> enqued = new ResponseMessage<>();
        enqued.success = true;
        enqued.data = true;
        enqued.errorMessage = "";
        enqued.message = "Enqueued";

        mRetourResponse.setValue(enqued);

    }
    public void retour(String orderId, String retourLabel, String specialInstruction, int amount, String currency, int serviceType, boolean isManual) {
        RequestQueue queue = Volley.newRequestQueue(mContext);

        JSONObject postparams = new JSONObject();
        try {
            postparams.put("orderId", orderId);
            postparams.put("retourLabel", retourLabel);
            //if this is a manually added retour, thn add th deetails. otherwise, just send the orderid and label (if hasRetour = true)
            if(isManual) {
                postparams.put("specialInstruction", specialInstruction);
                postparams.put("totalAmount", amount);
                postparams.put("currency", currency);
                postparams.put("serviceType", serviceType);
            }

        }catch (JSONException e){
            DriverServicesError error = new DriverServicesError();
            error.errorType = ServicesErrorType.retourError;
            error.errorCode = "-100";
            error.errorMessage = e.getMessage();
            mError.setValue(error);
            return;
        }
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                GlobalCoordinator.getInstance().getServiceUrl(mContext.getString(R.string.func_retourorder_url)), postparams,
                new Response.Listener() {
                    @Override
                    public void onResponse(Object response) {
                        //Success Callback
                        String obj = response.toString();
                        Gson gson = new Gson();
                        Type responseType = new TypeToken<ResponseMessage<Boolean>>(){}.getType();
                        ResponseMessage<Boolean> retVal = gson.fromJson(obj,responseType);
                        mRetourResponse.setValue(retVal);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Failure Callback
                        DriverServicesError error = new DriverServicesError();
                        error.errorType = ServicesErrorType.retourError;
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
                headers.put(GlobalCoordinator.SERVICE_HEADER_TOKEN, mToken.getValue().Token);
                headers.put(GlobalCoordinator.SERVICE_HEADER_CONTENT,GlobalCoordinator.SERVICE_APPLICATION_JSON);
                headers.put(GlobalCoordinator.SERVICE_HEADER_FUNCTION, mContext.getString(R.string.func_retourorder_key));
                return headers;
            }
        };


// Add the request to the RequestQueue.
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(jsonObjReq);

    }

    public void weightAdjust(LinkedList<AdjustedShipment> adjustedShipments){
        RequestQueue queue = Volley.newRequestQueue(mContext);

        JSONObject postparams = new JSONObject();
        try {

            JSONArray shipments = new JSONArray();
            for (AdjustedShipment adjustedShipment:adjustedShipments
            ) {
                JSONObject shipment = new JSONObject();
                shipment.put("reference",adjustedShipment.reference);

                JSONObject dimension = new JSONObject();
                JSONObject weight = new JSONObject();

                dimension.put("length", adjustedShipment.dimension.length);
                dimension.put("width", adjustedShipment.dimension.width);
                dimension.put("height", adjustedShipment.dimension.height);

                weight.put("chargeable", adjustedShipment.weight.chargeable);
                weight.put("volumetric", adjustedShipment.weight.volumetric);
                weight.put("volume", adjustedShipment.weight.volume);
                weight.put("nop", adjustedShipment.weight.nop);

                shipment.put("dimension", dimension);
                shipment.put("weight", weight);

                shipments.put(shipment);
            }
            postparams.put("shipments", shipments);
        }catch (JSONException e){

        }
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                GlobalCoordinator.getInstance().getServiceUrl(mContext.getString(R.string.func_weightadjust_url)), postparams,
                new Response.Listener() {
                    @Override
                    public void onResponse(Object response) {
                        //Success Callback
                        String obj = response.toString();
                        Gson gson = new Gson();
                        Type responseType = new TypeToken<ResponseMessage<Boolean>>(){}.getType();
                        ResponseMessage<Boolean> retVal = gson.fromJson(obj,responseType);
                        mWeightAdjustResponse.setValue(retVal);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Failure Callback
                        DriverServicesError error = new DriverServicesError();
                        error.errorType = ServicesErrorType.weightAdjustError;
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
                headers.put(GlobalCoordinator.SERVICE_HEADER_TOKEN, mToken.getValue().Token);
                headers.put(GlobalCoordinator.SERVICE_HEADER_CONTENT,GlobalCoordinator.SERVICE_APPLICATION_JSON);
                headers.put(GlobalCoordinator.SERVICE_HEADER_FUNCTION, mContext.getString(R.string.func_weightadjust_key));
                return headers;
            }
        };


// Add the request to the RequestQueue.
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(jsonObjReq);

    }

    public void checkLabel(ActivityType activity, String label){




        RequestQueue queue = Volley.newRequestQueue(mContext);


        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                GlobalCoordinator.getInstance().getServiceUrl(mContext.getString(R.string.func_checklabel_url)) + label + "/" + activity.getActivityTypeId(), null,
                new Response.Listener() {
                    @Override
                    public void onResponse(Object response) {
                        //Success Callback
                        String obj = response.toString();
                        Gson gson = new Gson();
                        Type responseType = new TypeToken<ResponseMessage<Boolean>>(){}.getType();
                        ResponseMessage<Boolean> retVal = gson.fromJson(obj,responseType);
                        mCheckLabelResponse.setValue(retVal);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Failure Callback
                        DriverServicesError error = new DriverServicesError();
                        error.errorType = ServicesErrorType.checkLabelError;
                        error.errorMessage = volleyError.getMessage();
                        error.volleyError = volleyError;
                        mError.setValue(error);
                    }
                })



        {
            @Override
            public Map getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put(GlobalCoordinator.SERVICE_HEADER_TOKEN, mToken.getValue().Token);
                headers.put(GlobalCoordinator.SERVICE_HEADER_CONTENT,GlobalCoordinator.SERVICE_APPLICATION_JSON);
                headers.put(GlobalCoordinator.SERVICE_HEADER_FUNCTION, mContext.getString(R.string.func_checklabel_key));
                return headers;
            }
        };


// Add the request to the RequestQueue.
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(jsonObjReq);


    }


    public void getCodList(){
        RequestQueue queue = Volley.newRequestQueue(mContext);


        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                GlobalCoordinator.getInstance().getServiceUrl(mContext.getString(R.string.func_codlist_url)), null,
                new Response.Listener() {
                    @Override
                    public void onResponse(Object response) {
                        //Success Callback
                        String obj = response.toString();
                        Gson gson = new Gson();
                        Type responseType = new TypeToken<ResponseMessage<CODItem[]>>(){}.getType();
                        ResponseMessage<CODItem[]> retVal = gson.fromJson(obj,responseType);
                        mCODListResponse.setValue(retVal);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Failure Callback
                        DriverServicesError error = new DriverServicesError();
                        error.errorType = ServicesErrorType.codListError;
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
                headers.put(GlobalCoordinator.SERVICE_HEADER_TOKEN, mToken.getValue().Token);
                headers.put(GlobalCoordinator.SERVICE_HEADER_CONTENT,GlobalCoordinator.SERVICE_APPLICATION_JSON);
                headers.put(GlobalCoordinator.SERVICE_HEADER_FUNCTION, mContext.getString(R.string.func_codlist_key));
                return headers;
            }
        };


// Add the request to the RequestQueue.
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(jsonObjReq);

    }


/*
    private static class checkLabelAsyncTask extends AsyncTask<Void, Void, ResponseMessage<Boolean>> {

        private ActivityType activity;
        private String label;
        private Context context;

        checkLabelAsyncTask(ActivityType activityType, String lbl, Context ctx) {
            activity = activityType;
            this.label = lbl;
            context = ctx;
        }

        @Override
        protected ResponseMessage<Boolean> doInBackground(Void... voids) {
            URL obj = null;
            HttpURLConnection con = null;
            String errorMessage = "";
            try {

                obj = new URL(GlobalCoordinator.getInstance().getServiceUrl(context.getString(R.string.func_checklabel_url)));
                con = (HttpURLConnection) obj.openConnection();
                con.setRequestMethod("GET");


                con.setRequestProperty("X-User-Token", DriverServices.mToken.getValue().Token);
                con.setRequestProperty("Content-Type", GlobalCoordinator.SERVICE_APPLICATION_JSON);
                con.setRequestProperty("x-functions-key", context.getString(R.string.func_checklabel_key));

                con.setDoOutput(true);

                String str = "{\n" +
                        "    \"activity\": \"001A6330-95D3-4EC7-A410-58151A02C701\",\n" +
                        "    \"label\": \"3003190596\"\n" +
                        "}";


                byte[] outputInBytes = str.getBytes("UTF-8");
                OutputStream os = con.getOutputStream();
                os.write(outputInBytes);
                os.close();


                DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                wr.flush();
                wr.close();


                int status = con.getResponseCode();
                if (status != HttpURLConnection.HTTP_OK) {
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
                Type responseType = new TypeToken<ResponseMessage<Boolean>>() {
                }.getType();

                ResponseMessage<Boolean> retVal = gson.fromJson(response.toString(), responseType);

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
            } catch (Exception e) {
                errorMessage = e.getMessage();
                e.printStackTrace();
            }
            return null;

        }

        @Override
        protected void onPostExecute(ResponseMessage<Boolean> aBoolean) {

        }
    }
*/

    public void getDelegates(final String userName){


        RequestQueue queue = Volley.newRequestQueue(mContext);


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                GlobalCoordinator.getInstance().getServiceUrl(mContext.getString(R.string.func_delegates_url)) + userName, null,
                new Response.Listener() {
                    @Override
                    public void onResponse(Object response) {
                        String obj = response.toString();
                        Gson gson = new Gson();
                        Type responseType = new TypeToken<ResponseMessage<JsonStop[]>>(){}.getType();
                        ResponseMessage<DelegateInfo[]> retVal = gson.fromJson(obj,responseType);
                        if(retVal.success){

                            if(GlobalCoordinator.testMode && retVal.dataCount == 0){

                            }
                                mDelegates.setValue(retVal);

                        }
                        else {
                            DriverServicesError error = new DriverServicesError();
                            error.errorType = ServicesErrorType.stopsError;
                            error.errorCode = retVal.errorCode;
                            error.errorMessage = retVal.errorMessage;
                            mError.setValue(error);
                        }
                    }


                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Failure Callback
                        DriverServicesError error = new DriverServicesError();
                        error.errorType = ServicesErrorType.generalError;
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

                if(GlobalCoordinator.testMode)
                    //headers.put(GlobalCoordinator.SERVICE_HEADER_TOKEN, "01542edd-cdd9-4389-a574-9ed0041d4c92");
                    headers.put(GlobalCoordinator.SERVICE_HEADER_TOKEN, "1cb73983-0290-4d64-9ff8-d3463fd20752");



                else
                    headers.put(GlobalCoordinator.SERVICE_HEADER_TOKEN, mToken.getValue().Token);


                //headers.put(GlobalCoordinator.SERVICE_HEADER_TOKEN, mToken.getValue());
                headers.put(GlobalCoordinator.SERVICE_HEADER_CONTENT,GlobalCoordinator.SERVICE_APPLICATION_JSON);
                headers.put(GlobalCoordinator.SERVICE_HEADER_FUNCTION, mContext.getString(R.string.func_delegates_key));
                return headers;
            }
        };


        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
// Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);


    }


    public void checkLabelStatus(final String label){
        RequestQueue queue = Volley.newRequestQueue(mContext);


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                GlobalCoordinator.getInstance().getServiceUrl(mContext.getString(R.string.func_checkstatus_url))+label, null,
                new Response.Listener() {
                    @Override
                    public void onResponse(Object response) {
                        String obj = response.toString();
                        Gson gson = new Gson();
                        Type responseType = new TypeToken<ResponseMessage<CheckStatus>>(){}.getType();
                        ResponseMessage<CheckStatus> retVal = gson.fromJson(obj,responseType);
                        if(retVal.success){
                            mLabelStatus.setValue(retVal);
                        }
                        else {
                            DriverServicesError error = new DriverServicesError();
                            error.errorType = ServicesErrorType.checkStatusError;
                            error.errorCode = retVal.errorCode;
                            error.errorMessage = retVal.errorMessage;
                            mError.setValue(error);
                        }
                    }


                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Failure Callback
                        DriverServicesError error = new DriverServicesError();
                        error.errorType = ServicesErrorType.generalError;
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
                headers.put(GlobalCoordinator.SERVICE_HEADER_TOKEN, mToken.getValue().Token);
                headers.put(GlobalCoordinator.SERVICE_HEADER_CONTENT,GlobalCoordinator.SERVICE_APPLICATION_JSON);
                headers.put(GlobalCoordinator.SERVICE_HEADER_FUNCTION, mContext.getString(R.string.func_checkstatus_key));
                return headers;
            }
        };


// Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);


    }

    public enum StopActionType{
        Start, Stop, Accept, Reject
    }


}
