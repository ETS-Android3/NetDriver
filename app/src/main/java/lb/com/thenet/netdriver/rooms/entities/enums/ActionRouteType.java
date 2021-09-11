package lb.com.thenet.netdriver.rooms.entities.enums;

import java.util.HashMap;
import java.util.Map;

public enum ActionRouteType {
    NonPickup("860089d4-d965-416b-9086-b6d1bd57a98e"),
    Reject("face7fdb-1683-4f40-89b9-1ac020e2ea47"),
    NonDelivery("b9b4d433-712e-474e-b013-309430b786a9"),
    UNKNOWN("")
    ;

    private String actionRouteId;

    ActionRouteType(String actionRouteId){
        this.actionRouteId = actionRouteId;
    }

    public String getActionRouteId() {
        return actionRouteId;
    }

    //****** Reverse Lookup Implementation************//

    //Lookup table
    private static final Map<String, ActionRouteType> lookup = new HashMap<>();

    //Populate the lookup table on loading time
    static
    {
        for(ActionRouteType art : ActionRouteType.values())
        {
            lookup.put(art.getActionRouteId(), art);
        }
    }

    //This method can be used for reverse lookup purpose
    public static ActionRouteType get(String actionRouteId)
    {
        return lookup.get(actionRouteId.toLowerCase());
    }

}
