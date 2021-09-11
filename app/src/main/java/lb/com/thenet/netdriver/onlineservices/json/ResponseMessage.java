package lb.com.thenet.netdriver.onlineservices.json;

public class ResponseMessage<T> {
    public ResponseMessage(){

    }

    public boolean success;
    public String message;
    public String errorMessage;
    public String errorCode;
    public Integer dataCount;
    public T data;
}

