package lb.com.thenet.netdriver;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import lb.com.thenet.netdriver.rooms.entities.DriverRepository;

public class BlockUIActivityViewModel extends BaseViewModel {

    public MutableLiveData<String> testString = new MutableLiveData<>();

    public BlockUIActivityViewModel(Application application){
        super(application);
        testString.setValue("TEST");
    }
}
