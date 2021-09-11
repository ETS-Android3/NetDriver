package lb.com.thenet.netdriver.onlineservices.json;


//this class is used to generate the json string for one labeel item to be sent (as pickup or delivery) within the send labels request that will be sent using a scheduler with network connectivity constraints

public class SendLabelsLabelItem {
    public String[] label;
    public String recipient;
    public String signature;
    public double cod;
    public String currency;
    public String orderId;
}
