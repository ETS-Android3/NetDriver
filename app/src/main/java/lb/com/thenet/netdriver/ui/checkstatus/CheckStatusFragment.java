package lb.com.thenet.netdriver.ui.checkstatus;


import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import lb.com.thenet.netdriver.GlobalCoordinator;
import lb.com.thenet.netdriver.MainActivity;
import lb.com.thenet.netdriver.R;
import lb.com.thenet.netdriver.dataholders.DriverLocation;
import lb.com.thenet.netdriver.onlineservices.DriverServicesError;
import lb.com.thenet.netdriver.onlineservices.json.CheckStatus;
import lb.com.thenet.netdriver.onlineservices.json.ResponseMessage;
import lb.com.thenet.netdriver.ui.ManualEntryDialog;



public class CheckStatusFragment extends Fragment  implements MainActivity.OnScanListener {


    private CheckStatusViewModel checkStatusViewModel;
    FloatingActionButton fabAddManual;
    TextView statusPicked;
    TextView statusCheckIn;
    TextView statusCheckOut;
    TextView statusReason;
    TextView statusLabel;


    public CheckStatusFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        checkStatusViewModel = ViewModelProviders.of(this).get(CheckStatusViewModel.class);
        View root = inflater.inflate(R.layout.fragment_check_status, container, false);

        fabAddManual = root.findViewById(R.id.fabAddManual);
        statusPicked = root.findViewById(R.id.statusPicked);
        statusCheckIn = root.findViewById(R.id.statusCheckIn);
        statusCheckOut = root.findViewById(R.id.statusCheckOut);
        statusReason = root.findViewById(R.id.statusReason);
        statusLabel = root.findViewById(R.id.statusLabel);

        fabAddManual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final ManualEntryDialog manualEntryDialog = new ManualEntryDialog(CheckStatusFragment.this.getContext());
                manualEntryDialog.setmListener(new ManualEntryDialog.OnManualEntryDialogListener() {
                    @Override
                    public void doneEditing(String s) {
                        dismissKeyboard();
                        onNewScan(s);
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


        checkStatusViewModel.getmLabelStatus().observe(getViewLifecycleOwner(), new Observer<ResponseMessage<CheckStatus>>() {
            @Override
            public void onChanged(ResponseMessage<CheckStatus> checkStatusResponseMessage) {
                if(checkStatusResponseMessage.success){

                    statusPicked.setText(checkStatusResponseMessage.data.pickedDate);
                    statusCheckIn.setText(checkStatusResponseMessage.data.checkInDate);
                    statusCheckOut.setText(checkStatusResponseMessage.data.checkOutDate);
                    statusReason.setText(checkStatusResponseMessage.data.nonDeliveryReason);

                }else {
                    Toast.makeText(CheckStatusFragment.this.getContext(), "Failure: " + checkStatusResponseMessage.errorMessage + checkStatusResponseMessage.message, Toast.LENGTH_LONG).show();

                }
            }
        });

        checkStatusViewModel.getmError().observe(getViewLifecycleOwner(), new Observer<DriverServicesError>() {
            @Override
            public void onChanged(DriverServicesError driverServicesError) {
                if(driverServicesError == null) return;


                if(driverServicesError.volleyError != null){
                    Toast.makeText(CheckStatusFragment.this.getContext(), driverServicesError.volleyError.toString(), Toast.LENGTH_LONG).show();

                }
                else {
                    Toast.makeText(CheckStatusFragment.this.getContext(), driverServicesError.errorMessage, Toast.LENGTH_LONG).show();
                }
                checkStatusViewModel.getmError().setValue(null);
            }
        });

        ((MainActivity)getActivity()).showCameraScanFAB();

        return root;

    }
    private String getStyledDate(String dt){
        String retVal = "";
        if(dt != null && dt != ""){
            if(!dt.contains("1900"))
                retVal = dt;
        }
        return retVal;
    }
    private void dismissKeyboard() {
        View viewWithFocus = CheckStatusFragment.this.getActivity().getCurrentFocus();
        if(viewWithFocus!=null){
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(viewWithFocus.getWindowToken(),0);

        }
    }

    private String getHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = bytes.length - 1; i >= 0; --i) {
            int b = bytes[i] & 0xff;
            if (b < 0x10)
                sb.append('0');
            sb.append(Integer.toHexString(b));
            if (i > 0) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }




    @Override
    public void onNewScan(String characters) {
        statusPicked.setText("");
        statusCheckIn.setText("");
        statusCheckOut.setText("");
        statusReason.setText("");
        statusLabel.setText(characters);
        checkStatusViewModel.getmDriverServices().checkLabelStatus(characters);
    }

    @Override
    public void onNewIntent(Intent intent) {

    }

    @Override
    public void onNewLocation(DriverLocation driverLocation) {
    }
}