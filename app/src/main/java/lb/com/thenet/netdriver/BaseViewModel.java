package lb.com.thenet.netdriver;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.LinkedList;

import lb.com.thenet.netdriver.dataholders.DriverLocation;
import lb.com.thenet.netdriver.dataholders.ReceivedStop;
import lb.com.thenet.netdriver.onlineservices.DriverServices;
import lb.com.thenet.netdriver.onlineservices.DriverServicesError;
import lb.com.thenet.netdriver.onlineservices.GeneralServices;
import lb.com.thenet.netdriver.onlineservices.json.ActionRoute;
import lb.com.thenet.netdriver.onlineservices.json.DelegateInfo;
import lb.com.thenet.netdriver.onlineservices.json.JsonStop;
import lb.com.thenet.netdriver.onlineservices.json.OrderType;
import lb.com.thenet.netdriver.onlineservices.json.ResponseMessage;
import lb.com.thenet.netdriver.onlineservices.json.SettingItem;
import lb.com.thenet.netdriver.rooms.entities.DriverRepository;


//This View Model should contain all the data needed to be common for more than one fragment
public class BaseViewModel extends AndroidViewModel {
    private MutableLiveData<String> mText;
    private MutableLiveData<Boolean> mLock;
    private MutableLiveData<Boolean> mShouldLogin;

    private static DriverRepository mDriverRepository;
    private GeneralServices mGeneralServices;
    private MutableLiveData<ResponseMessage<Boolean>> mRunSheetResponse;
    private MutableLiveData<ResponseMessage<SettingItem[]>> mSettingsResponse;
    private MutableLiveData<ResponseMessage<ActionRoute[]>> mActionRoutesResponse;

    private MutableLiveData<DriverServicesError> mError;
    DriverServices driverServices;
    public MutableLiveData<ResponseMessage<JsonStop[]>> mForceAssignedStopsResponse;
    public MutableLiveData<DriverLocation> mDriverLocation;
    public MutableLiveData<Boolean> disableNFC;
    public MutableLiveData<String> currentSettingsToken;
    public MutableLiveData<ResponseMessage<DelegateInfo[]>> mDelegates;




    public BaseViewModel(Application application) {
        super(application);
        mText = new MutableLiveData<>();
        mLock = new MutableLiveData<>();
        mShouldLogin = new MutableLiveData<>();
        currentSettingsToken = new MutableLiveData<>("");
        if(mDriverRepository == null)
            mDriverRepository = new DriverRepository(application);
        mGeneralServices = new GeneralServices(application.getApplicationContext());

        mText = mDriverRepository.mLatestToken;
        mLock = DriverRepository.mLockStatus;
        mShouldLogin = DriverRepository.mShouldLogin;
        mRunSheetResponse = mGeneralServices.mRunSheetResponse;
        mSettingsResponse = mGeneralServices.mSettingsResponse;
        mActionRoutesResponse = mGeneralServices.mActionRoutesResponse;


        mError = mGeneralServices.mError;

        driverServices = new DriverServices(application.getApplicationContext());
        mForceAssignedStopsResponse = driverServices.mForceAssignedStopsResponse;

        mDriverLocation = new MutableLiveData<>(new DriverLocation(33.3,33.3));
        disableNFC = new MutableLiveData<>(false);
    }

    public LiveData<String> getText() {
        return mText;
    }
    public LiveData<Boolean> getLockStatus() {return mLock;}
    public void setLockStatus(Boolean aBool){mDriverRepository.updateLockStatus(aBool);}
    public LiveData<Boolean> getShouldLogin() {return mShouldLogin;}
    public void logoutUser(){
        mDriverRepository.removeLoggedInUser();
        mShouldLogin.setValue(true);
    }
    public void callRunSheet(){
        mGeneralServices.callRunSheet();
    }
    public MutableLiveData<ResponseMessage<Boolean>> getmRunSheetResponse() {
        return mRunSheetResponse;
    }
    public void getSettings() { mGeneralServices.getSettings();}

    public MutableLiveData<ResponseMessage<SettingItem[]>> getmSettingsResponse() {
        return mSettingsResponse;
    }

    public void getLOVActionRoutes(){
        mGeneralServices.getLOVActionRoutes();
    }

    public MutableLiveData<ResponseMessage<ActionRoute[]>> getmActionRoutesResponse() {
        return mActionRoutesResponse;
    }


    public MutableLiveData<DriverServicesError> getmError() {
        return mError;
    }


    public DriverRepository getmDriverRepository() {
        return mDriverRepository;
    }

    public void getForcePickupsFromServer() {
        driverServices.getOrderStops(OrderType.FORCEPICKUP.getOrderTypeId());
    }

    public void getDelegates(String userName){
        driverServices.getDelegates(userName);
    }

    public MutableLiveData<ResponseMessage<JsonStop[]>> getmForceAssignedStopsResponse() {
        return mForceAssignedStopsResponse;
    }

    public MutableLiveData<DriverLocation> getmDriverLocation() {
        return mDriverLocation;
    }

    public MutableLiveData<ResponseMessage<DelegateInfo[]>> getmDelegates() {
        return mDelegates;
    }

    public void setStopsReceived(JsonStop[] data, OrderType orderType) {
        if (data != null) {

            LinkedList<ReceivedStop> receivedStops = new LinkedList<>();
            for (JsonStop jsonStop :
                    data) {
                ReceivedStop receivedStop = new ReceivedStop();
                receivedStop.stopId = jsonStop.stopId;
                receivedStop.orderTypeId = orderType.getOrderTypeId();
                receivedStop.activityDate = GlobalCoordinator.getNowFormatted();
                receivedStop.orderIds = new LinkedList<>();
                if (jsonStop.contact != null)
                    for (JsonStop.Contact jsonContact :
                            jsonStop.contact) {
                        if (jsonContact.stoplabel != null)
                            for (JsonStop.Contact.StopLabel label :
                                    jsonContact.stoplabel) {
                                receivedStop.orderIds.add(label.orderId);
                            }
                    }
                receivedStops.add(receivedStop);
            }
            driverServices.receivedStops(receivedStops);

        }
    }

}
