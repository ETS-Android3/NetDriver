package lb.com.thenet.netdriver.rooms.entities.user;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Date;

import lb.com.thenet.netdriver.GlobalCoordinator;

@Entity(tableName = GlobalCoordinator.USER_TABLE)
public class User {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    @ColumnInfo(name = "displayName")
    private String mDisplayName;

    @NonNull
    @ColumnInfo(name = "loginTime")
    private String mLoginTime;

    @NonNull
    @ColumnInfo(name="isLoggedIn")
    private boolean mIsLoggedIn;

    @NonNull
    @ColumnInfo(name="userName")
    private String mUserName;

    @ColumnInfo(name="userToken")
    private String mUserToken;

    public User(@NonNull String displayName, @NonNull String userName, boolean isLoggedIn, String loginTime, String userToken) {
        this.mDisplayName = displayName;
        this.mUserName = userName;
        this.mIsLoggedIn = isLoggedIn;
        this.mLoginTime = loginTime;
        this.mUserToken = userToken;
    }

    @Ignore
    public User(@NonNull String displayName, @NonNull String userName) {
        this.mDisplayName = displayName;
        this.mUserName = userName;
        this.mIsLoggedIn = true;
        this.mLoginTime = (new Date()).toString();
    }


    public String getDisplayName(){return mDisplayName;}
    public String getUserName() { return mUserName;}
    public boolean getIsLoggedIn() { return mIsLoggedIn;}
    public String getLoginTime() { return mLoginTime;}

    public String getUserToken() {
        return mUserToken;
    }

    public int getId() {return id;}

    public void setId(int id) {
        this.id = id;
    }


}
