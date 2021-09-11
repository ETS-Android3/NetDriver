package lb.com.thenet.netdriver.ui.tools;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import lb.com.thenet.netdriver.BaseViewModel;
import lb.com.thenet.netdriver.onlineservices.json.OrderType;
import lb.com.thenet.netdriver.rooms.entities.OrderRepository;

public class ToolsViewModel extends BaseViewModel {

    private MutableLiveData<String> mText;
    private OrderRepository orderRepository;

    public ToolsViewModel(Application application) {
        super(application);
        mText = new MutableLiveData<>();
        mText.setValue("This is tools fragment");
    }

    public void deleteAllOrdersOfType(OrderType orderType){
        orderRepository = new OrderRepository(getApplication(), orderType);

        orderRepository.deleteAllOrdersOfType(orderType);
    }
    public LiveData<String> getText() {
        return mText;
    }
}