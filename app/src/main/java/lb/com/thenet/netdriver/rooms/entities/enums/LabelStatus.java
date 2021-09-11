package lb.com.thenet.netdriver.rooms.entities.enums;

import java.util.HashMap;
import java.util.Map;

public enum LabelStatus {
    //EMPTY(""),
    Scanned("S"),
    NotScanned("NS"),
    Delivered("D"),
    NotDelivered("ND")
    ;

    private String labelStatusCode;

    LabelStatus(String labelStatusCode){
        this.labelStatusCode = labelStatusCode;
    }

    public String getLabelStatusCode() {
        return labelStatusCode;
    }

    //****** Reverse Lookup Implementation************//

    //Lookup table
    private static final Map<String, LabelStatus> lookup = new HashMap<>();

    //Populate the lookup table on loading time
    static
    {
        for(LabelStatus ls : LabelStatus.values())
        {
            lookup.put(ls.getLabelStatusCode(), ls);
        }
    }

    //This method can be used for reverse lookup purpose
    public static LabelStatus get(String status)
    {
        return lookup.get(status);
    }
}