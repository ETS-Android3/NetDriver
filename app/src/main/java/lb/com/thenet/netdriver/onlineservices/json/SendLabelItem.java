package lb.com.thenet.netdriver.onlineservices.json;

import java.util.HashMap;
import java.util.Map;

public class SendLabelItem {
    public String label;
    public double cod;
    public String currency;
    public String type;

    //Used only for pickup
    public String orderId;

    public String orderTypeId;

    public enum CODType {

        CASH("cash"),
        CHECK("check"),
        CREDITCARD("cc");

        private String codType;

        CODType(String codType){
            this.codType = codType;
        }

        public String getCodType() {
            return codType;
        }

        //****** Reverse Lookup Implementation************//

        //Lookup table
        private static final Map<String, CODType> lookup = new HashMap<>();

        //Populate the lookup table on loading time
        static
        {
            for(CODType ot : CODType.values())
            {
                lookup.put(ot.getCodType(), ot);
            }
        }

        //This method can be used for reverse lookup purpose
        public static CODType get(String codType)
        {
            return lookup.get(codType);
        }
    }
}

