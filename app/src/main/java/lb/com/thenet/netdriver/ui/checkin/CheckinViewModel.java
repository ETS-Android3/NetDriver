package lb.com.thenet.netdriver.ui.checkin;

import android.app.Application;

import androidx.lifecycle.MutableLiveData;

import lb.com.thenet.netdriver.BaseViewModel;
import lb.com.thenet.netdriver.onlineservices.DriverServices;
import lb.com.thenet.netdriver.onlineservices.DriverServicesError;
import lb.com.thenet.netdriver.onlineservices.json.ResponseMessage;
import lb.com.thenet.netdriver.onlineservices.json.ScanBuilding;
import lb.com.thenet.netdriver.rooms.entities.DriverRepository;

public class CheckinViewModel extends BaseViewModel {

    private DriverServices mDriverServices;
    private MutableLiveData<ResponseMessage<ScanBuilding>> mBuildingNFC;
    private MutableLiveData<ResponseMessage<Boolean>> mCheckinResponse;
    private MutableLiveData<ResponseMessage<Boolean>> mCheckLabelResponse;

    private MutableLiveData<DriverServicesError> mError;


    public CheckinViewModel(Application application) {

        super(application);
        mDriverServices = new DriverServices(application.getApplicationContext());
        mBuildingNFC = mDriverServices.mBuildingNFC;
        mCheckinResponse = mDriverServices.mCheckinResponse;
        mCheckLabelResponse = mDriverServices.mCheckLabelResponse;
        mError = mDriverServices.mError;
    }



    public MutableLiveData<ResponseMessage<ScanBuilding>> getmBuildingNFC(){return mBuildingNFC;}
    public MutableLiveData<ResponseMessage<Boolean>> getmCheckinResponse() {return mCheckinResponse;}

    public MutableLiveData<ResponseMessage<Boolean>> getmCheckLabelResponse() {
        return mCheckLabelResponse;
    }

    public MutableLiveData<DriverServicesError> getmError(){return mError;}
    public DriverServices getmDriverServices(){return mDriverServices;}



}
