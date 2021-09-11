package lb.com.thenet.netdriver.ui.delivery;

import lb.com.thenet.netdriver.onlineservices.json.OrderType;

public class PickupNavigationFragment extends StopsNavigationFragment  {
    public PickupNavigationFragment(){
        super(OrderType.PICKUP);
    }
}
