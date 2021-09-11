package lb.com.thenet.netdriver.onlineservices.json;

import java.util.HashMap;
import java.util.Map;

public enum ActivityType {


    CHECKIN("F25BF12D-F408-42FF-9C4E-8A25DA6D5FE5"),
    CHECKOUT("001A6330-95D3-4EC7-A410-58151A02C701"),
    LIAISON("493FD6D6-4BAD-4624-95C9-2CD0F35F37E7"),
    BULKPICKUP("BC2E5067-B64A-437B-B3A6-FD641EF87F1A"),
    PICKUP("C6E3FC06-A51C-442F-A2A7-0BB2F86D426A"),
    ADJUST("C9C1CB43-8F14-417E-98FD-D21E72290372")
    ;

    private String activityTypeId;

    ActivityType(String typeId){
        this.activityTypeId = typeId;
    }

    public String getActivityTypeId() {
        return activityTypeId;
    }

    //****** Reverse Lookup Implementation************//

    //Lookup table
    private static final Map<String, ActivityType> lookup = new HashMap<>();

    //Populate the lookup table on loading time
    static
    {
        for(ActivityType ot : ActivityType.values())
        {
            lookup.put(ot.getActivityTypeId(), ot);
        }
    }

    //This method can be used for reverse lookup purpose
    public static ActivityType get(String url)
    {
        return lookup.get(url);
    }
}
