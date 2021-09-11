package lb.com.thenet.netdriver.login.ui.login;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import android.app.Application;
import android.util.Patterns;

import java.sql.Driver;

import lb.com.thenet.netdriver.GlobalCoordinator;
import lb.com.thenet.netdriver.R;
import lb.com.thenet.netdriver.login.data.LoginDataSource;
import lb.com.thenet.netdriver.login.data.LoginRepository;
import lb.com.thenet.netdriver.login.data.Result;
import lb.com.thenet.netdriver.login.data.model.LoggedInUser;
import lb.com.thenet.netdriver.onlineservices.DriverServices;
import lb.com.thenet.netdriver.onlineservices.json.LoggedInUserData;
import lb.com.thenet.netdriver.rooms.entities.DriverRepository;


public class LoginViewModel extends AndroidViewModel {

    private MutableLiveData<LoginFormState> loginFormState = new MutableLiveData<>();
    private MutableLiveData<LoginResult> loginResult = new MutableLiveData<>();
    private LoginRepository loginRepository;

    //private MutableLiveData<Boolean> mShouldLogin;

    //private DriverRepository mDriverRepository;


/*
    LoginViewModel(LoginRepository loginRepository) {
        //this.loginRepository = loginRepository;
        //this.loginRepository = LoginRepository.getInstance(new LoginDataSource());

    }

 */

    public LoginViewModel(Application application){

        super(application);
        this.loginRepository = LoginRepository.getInstance(new LoginDataSource(), application.getApplicationContext());
    }

    LiveData<LoginFormState> getLoginFormState() {
        return loginFormState;
    }

    LiveData<LoginResult> getLoginResult() {
        return loginResult;
    }

    public void login(String username, String password) {
        // can be launched in a separate asynchronous job
        Result<LoggedInUser> result =
        loginRepository.login(username, password);


        if (result instanceof Result.Success) {
            LoggedInUser data = ((Result.Success<LoggedInUser>) result).getData();
            loginResult.setValue(new LoginResult(new LoggedInUserView(data.getDisplayName())));
            //GlobalCoordinator.getInstance().setUserToken(data.token);
            LoggedInUserData tempData = DriverServices.mToken.getValue();
            if(tempData == null) tempData = new LoggedInUserData();

            tempData.Token = data.token;
            tempData.DisplayName = data.getDisplayName();
            tempData.UserName = data.username;
            DriverServices.mToken.setValue(tempData);
            //DriverServices.mToken.setValue(data.token);

        } else {
            loginResult.setValue(new LoginResult(R.string.login_failed));

        }
    }

    public void loginDataChanged(String username, String password) {
        if (!isUserNameValid(username)) {
            loginFormState.setValue(new LoginFormState(R.string.invalid_username, null));
        } else if (!isPasswordValid(password)) {
            loginFormState.setValue(new LoginFormState(null, R.string.invalid_password));
        } else {
            loginFormState.setValue(new LoginFormState(true));
        }
    }

    // A placeholder username validation check
    private boolean isUserNameValid(String username) {
        if (username == null) {
            return false;
        }
        if (username.contains("@")) {
            return Patterns.EMAIL_ADDRESS.matcher(username).matches();
        } else {
            return !username.trim().isEmpty();
        }
    }

    // A placeholder password validation check
    private boolean isPasswordValid(String password) {
        return password != null && password.trim().length() > 5;
    }


}
