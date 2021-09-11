package lb.com.thenet.netdriver.ui.delivery;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import lb.com.thenet.netdriver.GlobalCoordinator;
import lb.com.thenet.netdriver.MainActivity;
import lb.com.thenet.netdriver.R;
import lb.com.thenet.netdriver.dataholders.DriverLocation;
import lb.com.thenet.netdriver.onlineservices.DriverServicesError;
import lb.com.thenet.netdriver.onlineservices.ServicesErrorType;
import lb.com.thenet.netdriver.onlineservices.json.ResponseMessage;
import lb.com.thenet.netdriver.ui.ManualEntryDialog;

import static android.view.View.GONE;

/**
 * A simple {@link Fragment} subclass.
 */
public class StopLabelRetourFragment extends Fragment implements MainActivity.OnScanListener {


    private StopsViewModel stopsViewModel;
    private Fragment hostingFragment;
    ProgressBar retourProgress;
    Button retourBackButton;
    Button retourSendButton;
    FloatingActionButton fabAddManual;
    TextView retourLabelText;

    RadioButton retourTypeCashRadio;
    RadioButton retourTypeDomesticRadio;
    EditText retourAmountText;
    RadioButton retourUSDRadio;
    RadioButton retourLBPRadio;
    EditText retourSpecialInstructionsText;
    int serviceType = 4;
    String currency = GlobalCoordinator.getInstance().settingsCountryCurrency.toUpperCase();
    String retourLabel = "";

    public StopLabelRetourFragment() {
        // Required empty public constructor
    }

    public StopLabelRetourFragment(Fragment hostingFragment) {
        this.hostingFragment = hostingFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        stopsViewModel = ViewModelProviders.of(hostingFragment).get(StopsViewModel.class);
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_stop_label_retour, container, false);
        findAndAssignTextAndActions(root);
        observeServerResponses();


        return root;
    }

    private void observeServerResponses() {
        //stopsViewModel.getmRetourResponse().observe()
        stopsViewModel.getmRetourResponse().observe(getViewLifecycleOwner(), new Observer<ResponseMessage<Boolean>>() {
            @Override
            public void onChanged(ResponseMessage<Boolean> booleanResponseMessage) {
                if(booleanResponseMessage == null) return;
                retourProgress.setVisibility(GONE);
                if(booleanResponseMessage.success) {
                    Toast.makeText(StopLabelRetourFragment.this.getContext(), "Successfully Submitted Retour", Toast.LENGTH_LONG);

                    stopsViewModel.getSelectedStopLabel().getValue().deliveryReason = "Retour";
                    stopsViewModel.getSelectedStopLabel().getValue().retourDone = true;
                    stopsViewModel.UpdateStopLabel(stopsViewModel.getSelectedStopLabel().getValue());

                    ((OnStopsFragmentsInteraction) hostingFragment).backToPreviousFragment(false);
                }else {
                    Toast.makeText(StopLabelRetourFragment.this.getContext(), "Error: " + booleanResponseMessage.errorMessage, Toast.LENGTH_LONG);
                }
                stopsViewModel.getmRetourResponse().setValue(null);

            }
        });

        stopsViewModel.getmError().observe(getViewLifecycleOwner(), new Observer<DriverServicesError>() {
            @Override
            public void onChanged(DriverServicesError driverServicesError) {
                if(driverServicesError == null) return;
                if(driverServicesError.errorType.equals(ServicesErrorType.retourError)){
                    retourProgress.setVisibility(GONE);
                    Toast.makeText(StopLabelRetourFragment.this.getContext(),"Error: " + driverServicesError.errorMessage, Toast.LENGTH_LONG);
                }
                stopsViewModel.getmError().setValue(null);
            }
        });
    }

    private void findAndAssignTextAndActions(View root) {
        retourProgress = root.findViewById(R.id.retourProgress);
        retourBackButton = root.findViewById(R.id.retourBackButton);
        retourSendButton = root.findViewById(R.id.retourSendButton);
        fabAddManual = root.findViewById(R.id.fabAddManual);
        retourTypeCashRadio = root.findViewById(R.id.retourTypeCashRadio);
        retourTypeDomesticRadio = root.findViewById(R.id.retourTypeDomesticRadio);
        retourAmountText = root.findViewById(R.id.retourAmountText);
        retourUSDRadio = root.findViewById(R.id.retourUSDRadio);
        retourLBPRadio = root.findViewById(R.id.retourLBPRadio);
        retourSpecialInstructionsText = root.findViewById(R.id.retourSpecialInstructionsText);
        retourLabelText = root.findViewById(R.id.retourLabelText);


        retourLBPRadio.setText(GlobalCoordinator.getInstance().settingsCountryCurrency.toUpperCase());
        fabAddManual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final ManualEntryDialog manualEntryDialog = new ManualEntryDialog(StopLabelRetourFragment.this.getContext());
                manualEntryDialog.setmListener(new ManualEntryDialog.OnManualEntryDialogListener() {
                    @Override
                    public void doneEditing(String s) {
                        dismissKeyboard();
                        setRetourLabel(s);
                        manualEntryDialog.dismiss();
                    }

                    @Override
                    public void dismiss() {
                        manualEntryDialog.dismiss();
                    }
                });
                manualEntryDialog.show();
            }
        });


        //If this was an assigned retour (not manual), then no need for all the controls
        if(stopsViewModel.getSelectedStopLabel().getValue().hasRetour)
        {
            retourTypeCashRadio.setVisibility(GONE);
            retourTypeDomesticRadio.setVisibility(GONE);
            retourAmountText.setVisibility(GONE);
            retourUSDRadio.setVisibility(GONE);
            retourLBPRadio.setVisibility(GONE);
            //retourSpecialInstructionsText.setVisibility(GONE);
            retourSpecialInstructionsText.setText(stopsViewModel.getSelectedStopLabel().getValue().retourInstructions);

            retourSpecialInstructionsText.setEnabled(false);

        }else{
            retourTypeCashRadio.setOnClickListener(v -> applyRetourType());
            retourTypeDomesticRadio.setOnClickListener(v -> applyRetourType());
            retourUSDRadio.setOnClickListener(v -> currency = "USD");
            retourLBPRadio.setOnClickListener(v -> currency = GlobalCoordinator.getInstance().settingsCountryCurrency.toUpperCase());
        }

        retourBackButton.setOnClickListener(v -> ((OnStopsFragmentsInteraction) hostingFragment).backToPreviousFragment(false));

        retourSendButton.setOnClickListener(v -> {

            //first, check if the shipper reference is the same as the scanned label
            if(stopsViewModel.getSelectedStopLabel().getValue().shipperReference != null && stopsViewModel.getSelectedStopLabel().getValue().shipperReference.toLowerCase().trim() == retourLabel.toLowerCase().trim()){
                GlobalCoordinator.getInstance().notifyMessage(StopLabelRetourFragment.this.getContext(), "You cannot use the same label as shipper reference", 2);
                return;
            }
            //if this is an assigned rrtour, then just send the orderid and the label using same retour service
            if(stopsViewModel.getSelectedStopLabel().getValue().hasRetour){

                stopsViewModel.retour(stopsViewModel.getSelectedStopLabel().getValue().orderId, retourLabel, "", 0, "", 0, false);

            }
            else {
                //this is a manual retour, mak sure to do the validations
                if (retourLabel.equals("")) {
                    GlobalCoordinator.getInstance().notifyMessage(StopLabelRetourFragment.this.getContext(), "Please Scan A Return Label", 2);
                    return;
                }
                if (retourTypeCashRadio.isChecked()) {
                    if (retourAmountText.getText().toString().equals("")) {
                        GlobalCoordinator.getInstance().notifyMessage(StopLabelRetourFragment.this.getContext(), "Please Enter the Cash Amount", 2);
                        return;
                    }
                    if (!retourAmountText.getText().toString().matches("^\\d+(\\.\\d+)?")) {
                        GlobalCoordinator.getInstance().notifyMessage(StopLabelRetourFragment.this.getContext(), "Cash Amount is not in correct format", 2);
                        return;
                    }
                }


                retourProgress.setVisibility(View.VISIBLE);

                if (retourTypeCashRadio.isChecked())
                    stopsViewModel.retour(stopsViewModel.getSelectedStopLabel().getValue().orderId, retourLabel, retourSpecialInstructionsText.getText().toString(), Integer.valueOf(retourAmountText.getText().toString()), currency, serviceType, true);
                else
                    stopsViewModel.retour(stopsViewModel.getSelectedStopLabel().getValue().orderId, retourLabel, retourSpecialInstructionsText.getText().toString(), 0, currency, serviceType, true);
            }

    });
    }

    private void applyRetourType() {
        if(retourTypeCashRadio.isChecked()){
            retourUSDRadio.setVisibility(View.VISIBLE);
            retourLBPRadio.setVisibility(View.VISIBLE);
            retourAmountText.setVisibility(View.VISIBLE);
            serviceType = 4;
        }
        if(retourTypeDomesticRadio.isChecked()){
            retourUSDRadio.setVisibility(GONE);
            retourLBPRadio.setVisibility(GONE);
            retourAmountText.setVisibility(GONE);
            serviceType = 2;
        }
    }

    private void dismissKeyboard() {
        View viewWithFocus = StopLabelRetourFragment.this.getActivity().getCurrentFocus();
        if(viewWithFocus!=null){
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(viewWithFocus.getWindowToken(),0);

        }
    }

    private void setRetourLabel(String characters) {

        retourLabel = characters;
        retourLabelText.setText(characters);
        retourLabelText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        retourSendButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void onNewScan(String characters) {
        setRetourLabel(characters);
    }



    @Override
    public void onNewIntent(Intent intent) {

    }

    @Override
    public void onNewLocation(DriverLocation driverLocation) {

    }
}
