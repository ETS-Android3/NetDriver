package lb.com.thenet.netdriver.ui.checkout;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import lb.com.thenet.netdriver.BaseViewModel;
import lb.com.thenet.netdriver.onlineservices.DriverServices;
import lb.com.thenet.netdriver.onlineservices.DriverServicesError;
import lb.com.thenet.netdriver.onlineservices.json.ResponseMessage;
import lb.com.thenet.netdriver.onlineservices.json.ScanBuilding;
import lb.com.thenet.netdriver.onlineservices.json.SpecialDelivery;

public class CheckoutViewModel extends BaseViewModel {

    private DriverServices mDriverServices;
    private MutableLiveData<ResponseMessage<ScanBuilding>> mBuildingNFC;
    private MutableLiveData<ResponseMessage<Boolean>> mCheckOutResponse;
    private MutableLiveData<ResponseMessage<Boolean>> mCheckLabelResponse;

    private MutableLiveData<ResponseMessage<SpecialDelivery[]>> mSpecialDeliveries;
    private MutableLiveData<DriverServicesError> mError;


    public CheckoutViewModel(Application application) {

        super(application);
        mDriverServices = new DriverServices(application.getApplicationContext());
        mBuildingNFC = mDriverServices.mBuildingNFC;
        mCheckOutResponse = mDriverServices.mCheckOutResponse;
        mCheckLabelResponse = mDriverServices.mCheckLabelResponse;
        mSpecialDeliveries = mDriverServices.mSpecialDeliveriesResponse;
        mError = mDriverServices.mError;
    }

    public MutableLiveData<ResponseMessage<ScanBuilding>> getmBuildingNFC(){return mBuildingNFC;}
    public MutableLiveData<ResponseMessage<Boolean>> getmCheckOutResponse() {return mCheckOutResponse;}
    public MutableLiveData<DriverServicesError> getmError(){return mError;}
    public MutableLiveData<ResponseMessage<SpecialDelivery[]>> getmSpecialDeliveries() {return mSpecialDeliveries;}

    public MutableLiveData<ResponseMessage<Boolean>> getmCheckLabelResponse() { return mCheckLabelResponse; }

    public DriverServices getmDriverServices(){return mDriverServices;}




}
