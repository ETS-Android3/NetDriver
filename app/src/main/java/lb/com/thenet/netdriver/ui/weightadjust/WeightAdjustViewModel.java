package lb.com.thenet.netdriver.ui.weightadjust;

import android.app.Application;

import androidx.lifecycle.MutableLiveData;

import java.util.LinkedList;

import lb.com.thenet.netdriver.BaseViewModel;
import lb.com.thenet.netdriver.onlineservices.DriverServices;
import lb.com.thenet.netdriver.onlineservices.DriverServicesError;
import lb.com.thenet.netdriver.onlineservices.json.AdjustedShipment;
import lb.com.thenet.netdriver.onlineservices.json.ResponseMessage;

public class WeightAdjustViewModel extends BaseViewModel {
    private DriverServices mDriverServices;
    private MutableLiveData<ResponseMessage<Boolean>> mWeightAdjustResponse;
    private MutableLiveData<DriverServicesError> mError;

    private AdjustedShipment sampleShipment;
    private boolean isCopied = false;

    public WeightAdjustViewModel(Application application) {

        super(application);
        mDriverServices = new DriverServices(application.getApplicationContext());
        mWeightAdjustResponse = mDriverServices.mWeightAdjustResponse;
        mError = mDriverServices.mError;
    }

    public void weightAdjust(LinkedList<AdjustedShipment> adjustedShipments){
        mDriverServices.weightAdjust(adjustedShipments);
    }

    public MutableLiveData<ResponseMessage<Boolean>> getmWeightAdjustResponse() {
        return mWeightAdjustResponse;
    }

    public void setSampleShipment(AdjustedShipment sampleShipment) {
        this.isCopied = true;
        this.sampleShipment = sampleShipment;
    }
    public void clearSampleShipment(){
        this.isCopied = false;
        this.sampleShipment = null;
    }
    public boolean isCopied() { return isCopied; }

    public AdjustedShipment getSampleShipment() {
        return sampleShipment;
    }

    @Override
    public MutableLiveData<DriverServicesError> getmError() {
        return mError;
    }
}
