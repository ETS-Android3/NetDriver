package lb.com.thenet.netdriver.onlineservices.json;

public class SendPickupRequest {
    public double longitude;
    public double latitude;
    public String activityDate;
    public String recipient;
    public String signature;
    public String nfcBuilding;
    public SendLabelItem[] labels;
    public OrderType orderType;
}
