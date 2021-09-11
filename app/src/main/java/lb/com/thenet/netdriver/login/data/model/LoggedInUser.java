package lb.com.thenet.netdriver.login.data.model;

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
public class LoggedInUser {

    private String userId;
    private String displayName;

    public LoggedInUser(String firstName, String lastName, String username, String token, String image){
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.token = token;
        this.image = image;


    }
    public LoggedInUser(String userId, String displayName) {
        this.userId = userId;
        this.displayName = displayName;

    }

    public String getUserId() {
        return userId;
    }

    public String getDisplayName() {
        return firstName + " " + lastName;
    }

    public String token;
    public String firstName;
    public String lastName;
    public String username;
    public String image;
}
