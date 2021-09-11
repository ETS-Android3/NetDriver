package lb.com.thenet.netdriver;

import android.app.Application;
import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.sql.Date;
import java.sql.Driver;

import lb.com.thenet.netdriver.onlineservices.DriverServices;
import lb.com.thenet.netdriver.onlineservices.json.JsonStop;
import lb.com.thenet.netdriver.onlineservices.json.OrderType;
import lb.com.thenet.netdriver.onlineservices.json.ResponseMessage;

public class MainActivityViewModel extends BaseViewModel {


    public MainActivityViewModel(Application application){
        super(application);

    }



}
