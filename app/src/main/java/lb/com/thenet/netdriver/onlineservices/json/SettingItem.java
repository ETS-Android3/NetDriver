package lb.com.thenet.netdriver.onlineservices.json;

public class SettingItem {
    public String name;
    public String value;
    public Integer getIntValue(){
        return Integer.parseInt(value);
    }
}
