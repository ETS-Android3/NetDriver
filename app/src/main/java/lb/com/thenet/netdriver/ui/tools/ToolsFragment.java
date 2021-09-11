package lb.com.thenet.netdriver.ui.tools;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import lb.com.thenet.netdriver.GlobalCoordinator;
import lb.com.thenet.netdriver.MainActivity;
import lb.com.thenet.netdriver.R;
import lb.com.thenet.netdriver.onlineservices.json.OrderType;

public class ToolsFragment extends Fragment {

    private ToolsViewModel toolsViewModel;

    private CheckBox chkTestMode;
    private EditText settingsNumberOfStops;
    private CheckBox settingsDisableCheckInNFC;
    private CheckBox settingsDisableCheckOutNFC;
    private CheckBox settingsDisableDeliveryNFC;
    private CheckBox settingsDisableStartStop;


    EditText settingsPasswordText;
    Button settingsUnlockButton;
    TextView settingsVersionText;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        toolsViewModel =
                ViewModelProviders.of(this).get(ToolsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_tools, container, false);
        /*
        final TextView textView = root.findViewById(R.id.text_tools);
        toolsViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

         */

        final Button adminDeleteAllDeliveriesButton = root.findViewById(R.id.adminDeleteAllDeliveriesButton);
        final Button adminDeleteAllPickupsButton = root.findViewById(R.id.adminDeleteAllPickupsButton);
        chkTestMode = root.findViewById(R.id.chkTestMode);
        settingsNumberOfStops = root.findViewById(R.id.settingsNumberOfStops);
        settingsDisableCheckInNFC = root.findViewById(R.id.settingsDisableCheckInNFC);
        settingsDisableCheckOutNFC = root.findViewById(R.id.settingsDisableCheckOutNFC);
        settingsDisableDeliveryNFC = root.findViewById(R.id.settingsDisableDeliveryNFC);
        settingsDisableStartStop = root.findViewById(R.id.settingsDisableStartStop);

        settingsPasswordText = root.findViewById(R.id.settingsPasswordText);
        settingsUnlockButton = root.findViewById(R.id.settingsUnlockButton);
        settingsVersionText = root.findViewById(R.id.settingsVersionText);

        chkTestMode.setChecked(GlobalCoordinator.testMode);
        settingsNumberOfStops.setText(String.valueOf(GlobalCoordinator.getInstance().settingsNumberOfStops));
        settingsDisableCheckInNFC.setChecked(GlobalCoordinator.getInstance().settingsDisableCheckInNFC);
        settingsDisableCheckOutNFC.setChecked(GlobalCoordinator.getInstance().settingsDisableCheckOutNFC);
        settingsDisableDeliveryNFC.setChecked(GlobalCoordinator.getInstance().settingsDisableDeliveryNFC);
        settingsDisableStartStop.setChecked(GlobalCoordinator.getInstance().settingsDisableStartStop);


        adminDeleteAllDeliveriesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toolsViewModel.deleteAllOrdersOfType(OrderType.DELIVERY);
            }
        });

        adminDeleteAllPickupsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toolsViewModel.deleteAllOrdersOfType(OrderType.PICKUP);
            }
        });


        chkTestMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GlobalCoordinator.testMode = chkTestMode.isChecked();
            }
        });
        settingsDisableCheckInNFC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GlobalCoordinator.getInstance().settingsDisableCheckInNFC = settingsDisableCheckInNFC.isChecked();
            }
        });
        settingsDisableCheckOutNFC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GlobalCoordinator.getInstance().settingsDisableCheckOutNFC = settingsDisableCheckOutNFC.isChecked();
            }
        });
        settingsDisableDeliveryNFC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GlobalCoordinator.getInstance().settingsDisableDeliveryNFC = settingsDisableDeliveryNFC.isChecked();
            }
        });
        settingsDisableStartStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GlobalCoordinator.getInstance().settingsDisableStartStop = settingsDisableStartStop.isChecked();
            }
        });
        settingsNumberOfStops.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    GlobalCoordinator.getInstance().settingsNumberOfStops = Integer.valueOf(settingsNumberOfStops.getText().toString());
                }catch (Exception ex) {
                    //ignore
                }
            }
        });


        settingsVersionText.setText("Version: " + getResources().getString(R.string.netDriverVersion));
        settingsUnlockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pwd = settingsPasswordText.getText().toString();
                if(pwd != null && pwd.equals("N&tDr1v&r")){
                    chkTestMode.setVisibility(View.VISIBLE);
                    settingsNumberOfStops.setVisibility(View.VISIBLE);
                    settingsDisableCheckInNFC.setVisibility(View.VISIBLE);
                    settingsDisableCheckOutNFC.setVisibility(View.VISIBLE);
                    adminDeleteAllDeliveriesButton.setVisibility(View.VISIBLE);
                    adminDeleteAllPickupsButton.setVisibility(View.VISIBLE);
                    settingsDisableDeliveryNFC.setVisibility(View.VISIBLE);
                    settingsDisableStartStop.setVisibility(View.VISIBLE);

                }
            }
        });
        ((MainActivity)getActivity()).hideCameraScanFAB();

        return root;
    }
}