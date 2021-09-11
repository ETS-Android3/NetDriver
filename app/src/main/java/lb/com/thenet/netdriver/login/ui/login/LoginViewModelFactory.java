package lb.com.thenet.netdriver.login.ui.login;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.annotation.NonNull;

import lb.com.thenet.netdriver.login.data.LoginDataSource;
import lb.com.thenet.netdriver.login.data.LoginRepository;

/**
 * ViewModel provider factory to instantiate LoginViewModel.
 * Required given LoginViewModel has a non-empty constructor
 */
public class LoginViewModelFactory implements ViewModelProvider.Factory {

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {

        if (modelClass.isAssignableFrom(LoginViewModel.class)) {

            return null;//(T) new LoginViewModel(LoginRepository.getInstance(new LoginDataSource(), null));
        } else {
            throw new IllegalArgumentException("Unknown ViewModel class");
        }


    }
}
