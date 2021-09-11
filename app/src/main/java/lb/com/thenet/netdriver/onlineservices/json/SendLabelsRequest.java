package lb.com.thenet.netdriver.onlineservices.json;

//this class is used to generate the full json string (as pickup or delivery)that will be used to send labels request that will be sent using a scheduler with network connectivity constraints

public class SendLabelsRequest {
    public double longitude;
    public double latitude;
    public String activityDate;
    public SendLabelsLabelItem[] labels;
}
