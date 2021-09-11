package lb.com.thenet.netdriver.onlineservices.json;

import java.util.HashMap;
import java.util.Map;

public enum OrderType
{
    DELIVERY("709b22f2-61de-4981-8771-f066002a7553"),
    PICKUP("c6e3fc06-a51c-442f-a2a7-0bb2f86d426a"),
    FORCEPICKUP("8d2e0633-d7bc-4f58-a121-74320ca9e0c2"),
    CANCELPICKUP("8463CA35-6C48-497D-B947-C05AD5707E47"),
    MESSAGEE("3D0A517B-B232-4B4D-9A96-1470759BD8D7"),
    ALL("50490c2c-5df6-4433-9b46-6bb671799773");

    private String orderTypeId;

    OrderType(String orderTypeId){
        this.orderTypeId = orderTypeId;
    }

    public String getOrderTypeId() {
        return orderTypeId;
    }

    //****** Reverse Lookup Implementation************//

    //Lookup table
    private static final Map<String, OrderType> lookup = new HashMap<>();

    //Populate the lookup table on loading time
    static
    {
        for(OrderType ot : OrderType.values())
        {
            lookup.put(ot.getOrderTypeId(), ot);
        }
    }

    //This method can be used for reverse lookup purpose
    public static OrderType get(String url)
    {
        return lookup.get(url);
    }
}