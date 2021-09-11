package lb.com.thenet.netdriver.ui.checkstatus;


import android.app.Application;

import androidx.lifecycle.MutableLiveData;

import lb.com.thenet.netdriver.BaseViewModel;
import lb.com.thenet.netdriver.onlineservices.DriverServices;
import lb.com.thenet.netdriver.onlineservices.DriverServicesError;
import lb.com.thenet.netdriver.onlineservices.json.CheckStatus;
import lb.com.thenet.netdriver.onlineservices.json.ResponseMessage;
import lb.com.thenet.netdriver.onlineservices.json.ScanBuilding;


public class CheckStatusViewModel extends BaseViewModel {
    private DriverServices mDriverServices;
    private MutableLiveData<ResponseMessage<CheckStatus>> mLabelStatus;
    private MutableLiveData<DriverServicesError> mError;

    public CheckStatusViewModel(Application application) {

        super(application);
        mDriverServices = new DriverServices(application.getApplicationContext());
        mLabelStatus = mDriverServices.mLabelStatus;
        mError = mDriverServices.mError;
    }

    public MutableLiveData<ResponseMessage<CheckStatus>> getmLabelStatus(){return mLabelStatus;}
    public MutableLiveData<DriverServicesError> getmError(){return mError;}
    public DriverServices getmDriverServices(){return mDriverServices;}

}
