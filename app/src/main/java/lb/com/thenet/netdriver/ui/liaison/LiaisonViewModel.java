package lb.com.thenet.netdriver.ui.liaison;

import android.app.Application;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.lifecycle.MutableLiveData;

import java.util.LinkedList;

import lb.com.thenet.netdriver.BaseViewModel;
import lb.com.thenet.netdriver.GlobalCoordinator;
import lb.com.thenet.netdriver.onlineservices.DriverServices;
import lb.com.thenet.netdriver.onlineservices.DriverServicesError;
import lb.com.thenet.netdriver.onlineservices.json.AdjustedShipment;
import lb.com.thenet.netdriver.onlineservices.json.ResponseMessage;
import lb.com.thenet.netdriver.onlineservices.json.ScanBuilding;

public class LiaisonViewModel extends BaseViewModel {
    private DriverServices mDriverServices;
    private MutableLiveData<ResponseMessage<Boolean>> mLiaisonResponse;
    private MutableLiveData<ResponseMessage<Boolean>> mCheckLabelResponse;
    private MutableLiveData<DriverServicesError> mError;
    private MutableLiveData<ResponseMessage<ScanBuilding>> mBuildingNFC;




    public LiaisonViewModel(Application application) {

        super(application);
        mDriverServices = new DriverServices(application.getApplicationContext());
        mLiaisonResponse = mDriverServices.mLiaisonResponse;
        mCheckLabelResponse = mDriverServices.mCheckLabelResponse;
        mError = mDriverServices.mError;
        mBuildingNFC = mDriverServices.mBuildingNFC;

    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void liaison(String buildingCode, double longitude, double latitude, LinkedList<String> barcodes){
        String dateTime = GlobalCoordinator.getNowFormatted();

        mDriverServices.liaisonBarcodes(buildingCode, longitude, latitude, dateTime, barcodes);
        //mDriverServices.actionStop(DriverServices.StopActionType.Stop, selectedStop.getValue().stopId, longitude, latitude, dateTime, reason);

    }


    public MutableLiveData<ResponseMessage<Boolean>> getmLiaisonResponse() {return mLiaisonResponse;}

    public MutableLiveData<ResponseMessage<Boolean>> getmCheckLabelResponse() {
        return mCheckLabelResponse;
    }

    public MutableLiveData<DriverServicesError> getmError(){return mError;}
    public MutableLiveData<ResponseMessage<ScanBuilding>> getmBuildingNFC(){return mBuildingNFC;}

    public DriverServices getmDriverServices() {
        return mDriverServices;
    }
}
