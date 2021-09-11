package lb.com.thenet.netdriver.rooms.entities.enums;

import java.util.HashMap;
import java.util.Map;

public enum StopStatus {
    NotStarted("NS"),
    Started("S"),
    Completed("C"),
    Accepted("A"),
    Rejected("R"),
    EMPTY("");


    private String stopStatusCode;

    StopStatus(String stopStatusCode){
        this.stopStatusCode = stopStatusCode;
    }

    public String getStopStatusCode() {
        return stopStatusCode;
    }

    //****** Reverse Lookup Implementation************//

    //Lookup table
    private static final Map<String, StopStatus> lookup = new HashMap<>();

    //Populate the lookup table on loading time
    static
    {
        for(StopStatus ot : StopStatus.values())
        {
            lookup.put(ot.getStopStatusCode(), ot);
        }
    }

    //This method can be used for reverse lookup purpose
    public static StopStatus get(String status)
    {
        return lookup.get(status);
    }
}
