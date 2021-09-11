package lb.com.thenet.netdriver.ui.home;

import android.app.Application;

import androidx.lifecycle.MutableLiveData;

import lb.com.thenet.netdriver.BaseViewModel;
import lb.com.thenet.netdriver.onlineservices.DriverServices;
import lb.com.thenet.netdriver.onlineservices.DriverServicesError;
import lb.com.thenet.netdriver.onlineservices.json.ResponseMessage;
import lb.com.thenet.netdriver.onlineservices.json.ScanBuilding;

public class HomeViewModel extends BaseViewModel {
    private MutableLiveData<ResponseMessage<ScanBuilding>> mBuildingNFC;
    private DriverServices mDriverServices;



    public HomeViewModel(Application application) {
        super(application);
        mDriverServices = new DriverServices(application.getApplicationContext());

        mBuildingNFC = mDriverServices.mBuildingNFC;

    }

    public MutableLiveData<ResponseMessage<ScanBuilding>> getmBuildingNFC(){return mBuildingNFC;}

    public DriverServices getmDriverServices() {
        return mDriverServices;
    }
}