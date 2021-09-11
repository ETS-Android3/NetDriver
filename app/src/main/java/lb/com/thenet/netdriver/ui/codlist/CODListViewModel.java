package lb.com.thenet.netdriver.ui.codlist;

import android.app.Application;

import androidx.lifecycle.MutableLiveData;

import lb.com.thenet.netdriver.BaseViewModel;
import lb.com.thenet.netdriver.onlineservices.DriverServices;
import lb.com.thenet.netdriver.onlineservices.DriverServicesError;
import lb.com.thenet.netdriver.onlineservices.json.CODItem;
import lb.com.thenet.netdriver.onlineservices.json.JsonStop;
import lb.com.thenet.netdriver.onlineservices.json.ResponseMessage;

public class CODListViewModel extends BaseViewModel {
    private DriverServices mDriverServices;
    private MutableLiveData<DriverServicesError> mError;
    private MutableLiveData<ResponseMessage<CODItem[]>> mCodItems;

    public CODListViewModel(Application application){
        super(application);
        mDriverServices = new DriverServices(application.getApplicationContext());
        mError = mDriverServices.mError;
        mCodItems = mDriverServices.mCODListResponse;
    }

    public boolean getCodList(){
        mDriverServices.getCodList();
        return true;
    }

    public MutableLiveData<ResponseMessage<CODItem[]>> getmCodItems() {
        return mCodItems;
    }
}
