package lb.com.thenet.netdriver.rooms.entities.enums;

import java.util.HashMap;
import java.util.Map;

public enum QueueItemType {
    Pickup("Pickup"),
    Delivery("Delivery"),
    Signature("Signature"),
    Checkin("Checkin"),
    Checkout("Checkout"),
    NonDelivery("NonDelivery"),
    Retour("Retour"),
    EMPTY("");


    private String queueItemTypeCode;

    QueueItemType(String queueItemTypeCode){
        this.queueItemTypeCode = queueItemTypeCode;
    }

    public String getQueueItemTypeCode() {
        return queueItemTypeCode;
    }

    //****** Reverse Lookup Implementation************//

    //Lookup table
    private static final Map<String, QueueItemType> lookup = new HashMap<>();

    //Populate the lookup table on loading time
    static
    {
        for(QueueItemType ot : QueueItemType.values())
        {
            lookup.put(ot.getQueueItemTypeCode(), ot);
        }
    }

    //This method can be used for reverse lookup purpose
    public static QueueItemType get(String type)
    {
        return lookup.get(type);
    }
}
