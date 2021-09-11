package lb.com.thenet.netdriver.rooms.entities.enums;

import java.util.HashMap;
import java.util.Map;

public enum ShipmentType {
    Regular("fad1ef31-acee-42ee-8169-1234ee18caef"),
    NormalPickup("0256e0dc-0aff-4c4c-82a2-5294863a9161"),
    CashCollection("f2607e47-5a6f-47ba-936d-551ce4e8bc9c"),
    NetPoint("d07ffc30-93a0-43aa-8ea5-578088cc1c69"),
    ForcedlyAssigned("f82a7dac-0631-4609-bec7-67597703318f"),
    NetLocker("4fce4ad0-14bc-4d1c-86d9-d8d9fd62e3ef"),
    COD("a65a6886-3f82-42ec-b224-f5a8af458a09"),
    UNKNOWN("");

    private String shipmentTypeId;

    ShipmentType(String shipmentTypeId){
        this.shipmentTypeId = shipmentTypeId;
    }

    public String getShipmentTypeId() {
        return shipmentTypeId;
    }

    //****** Reverse Lookup Implementation************//

    //Lookup table
    private static final Map<String, ShipmentType> lookup = new HashMap<>();

    //Populate the lookup table on loading time
    static
    {
        for(ShipmentType shipmentType : ShipmentType.values())
        {
            lookup.put(shipmentType.getShipmentTypeId(), shipmentType);
        }
    }

    //This method can be used for reverse lookup purpose
    public static ShipmentType get(String shipmentTypeId)
    {
        return lookup.get(shipmentTypeId.toLowerCase());
    }


}
