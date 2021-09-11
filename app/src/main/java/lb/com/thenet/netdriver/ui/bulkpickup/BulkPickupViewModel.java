package lb.com.thenet.netdriver.ui.bulkpickup;

import android.app.Application;

import androidx.lifecycle.MutableLiveData;

import lb.com.thenet.netdriver.BaseViewModel;
import lb.com.thenet.netdriver.onlineservices.DriverServices;
import lb.com.thenet.netdriver.onlineservices.DriverServicesError;
import lb.com.thenet.netdriver.onlineservices.json.ResponseMessage;
import lb.com.thenet.netdriver.onlineservices.json.ScanBuilding;

public class BulkPickupViewModel  extends BaseViewModel {

    private DriverServices mDriverServices;
    private MutableLiveData<ResponseMessage<ScanBuilding>> mBuildingNFC;
    private MutableLiveData<ResponseMessage<Boolean>> mBulkPickupResponse;
    private MutableLiveData<ResponseMessage<Boolean>> mCheckLabelResponse;
    private MutableLiveData<DriverServicesError> mError;


    public BulkPickupViewModel(Application application) {

        super(application);
        mDriverServices = new DriverServices(application.getApplicationContext());
        mBuildingNFC = mDriverServices.mBuildingNFC;
        mBulkPickupResponse = mDriverServices.mBulkPickupResponse;
        mCheckLabelResponse = mDriverServices.mCheckLabelResponse;
        mError = mDriverServices.mError;
    }



    public MutableLiveData<ResponseMessage<ScanBuilding>> getmBuildingNFC(){return mBuildingNFC;}
    public MutableLiveData<ResponseMessage<Boolean>> getmBulkPickupResponse() {return mBulkPickupResponse;}

    public MutableLiveData<ResponseMessage<Boolean>> getmCheckLabelResponse() {
        return mCheckLabelResponse;
    }

    public MutableLiveData<DriverServicesError> getmError(){return mError;}
    public DriverServices getmDriverServices(){return mDriverServices;}



}
