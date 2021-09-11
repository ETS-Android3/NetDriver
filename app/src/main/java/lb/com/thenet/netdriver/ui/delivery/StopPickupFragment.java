package lb.com.thenet.netdriver.ui.delivery;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.github.gcacace.signaturepad.views.SignaturePad;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.ByteArrayOutputStream;
import java.util.LinkedList;

import lb.com.thenet.netdriver.GlobalCoordinator;
import lb.com.thenet.netdriver.MainActivity;
import lb.com.thenet.netdriver.R;
import lb.com.thenet.netdriver.adapters.DeliveryLabelAdapter;
import lb.com.thenet.netdriver.dataholders.DriverLocation;
import lb.com.thenet.netdriver.onlineservices.DriverServicesError;
import lb.com.thenet.netdriver.onlineservices.ServicesErrorType;
import lb.com.thenet.netdriver.onlineservices.json.OrderType;
import lb.com.thenet.netdriver.onlineservices.json.ResponseMessage;
import lb.com.thenet.netdriver.onlineservices.json.SendLabelItem;
import lb.com.thenet.netdriver.rooms.entities.DriverRepository;
import lb.com.thenet.netdriver.rooms.entities.enums.LabelStatus;
import lb.com.thenet.netdriver.rooms.entities.stops.StopLabel;
import lb.com.thenet.netdriver.ui.ManualEntryDialog;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link StopPickupFragment#} factory method to
 * create an instance of this fragment.
 */
public class StopPickupFragment extends Fragment implements MainActivity.OnScanListener {

    private StopsViewModel stopsViewModel;
    private Fragment hostingFragment;

    private EditText nameCollectedText;

    SignaturePad mSignaturePad;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private Button mClearButton;
    private Button mSaveButton;
    private Button mCancelButton;

    private ProgressBar sendPickupProgress;

    private boolean signed = false;
    private ConstraintLayout stopRecipientLayout;

    private DeliveryLabelAdapter adapter;
    private final LinkedList<SendLabelItem> sendLabelItems = new LinkedList<>();
    private RecyclerView labelsRecycler;
    FloatingActionButton fabAddManual;


    public StopPickupFragment() {
        // Required empty public constructor
    }
    public StopPickupFragment(Fragment hostingFragment){
        this.hostingFragment = hostingFragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //stopsViewModel = ViewModelProviders.of(hostingFragment).get(StopsViewModel.class);
        //As of 2.2.0. (2020) the lifecycle-extensions has been deprecated. Refer to Google Documentation.
        //The APIs in lifecycle-extensions have been deprecated. Instead, add dependencies for the specific Lifecycle artifacts you need.

        stopsViewModel = new ViewModelProvider(hostingFragment).get(StopsViewModel.class);

        View root = inflater.inflate(R.layout.fragment_stop_pickup, container, false);

        findAndAssignTextAndActions(root);
        observeServerResponse();

        verifyStoragePermissions(this.getActivity());


        mSignaturePad = root.findViewById(R.id.signature_pad);
        setupSignaturePad();


        //setAmountText();

        ((MainActivity)getActivity()).updateDriverLocation();

        //Fill in the shipment labels from previous screen to add to the adapter and display
        for (StopLabel sl :
                stopsViewModel.getSelectedStopContact().getValue().stopLabelLiveData.getValue()) {
            if (sl.status.equals(LabelStatus.Scanned.getLabelStatusCode())) {
                SendLabelItem newItem = new SendLabelItem();
                newItem.type = SendLabelItem.CODType.CASH.getCodType();
                newItem.label = sl.hawb;
                newItem.cod = 0;
                newItem.currency = GlobalCoordinator.getInstance().settingsCountryCurrency.toUpperCase();
                newItem.orderId = sl.orderId;
                sendLabelItems.add(newItem);


                //since this is pickup, if it has refund, then add the erefund label
                // and get the cod info from the new fields (refund fields)
                if(sl.hasRefund) {
                    SendLabelItem refundItem = new SendLabelItem();
                    refundItem.type = SendLabelItem.CODType.CASH.getCodType();
                    refundItem.label = "";
                    refundItem.cod = sl.refundAmountLBP;
                    refundItem.currency = GlobalCoordinator.getInstance().settingsCountryCurrency.toUpperCase();
                    refundItem.orderId = sl.refundOrderId;
                    sendLabelItems.add(refundItem);
                }
                /*
                if(sl.codUSD > 0){
                    newItem.cod = sl.codUSD;
                    newItem.currency = "USD";
                }
                if(sl.codLBP > 0){
                    newItem.cod = sl.codLBP;
                    newItem.currency = "LBP";
                }

                 */


            }
        }

        adapter = new DeliveryLabelAdapter(this.getContext(), sendLabelItems, OrderType.PICKUP);

        // Connect the adapter with the recycler view.
        labelsRecycler.setAdapter(adapter);
        // Give the recycler view a default layout manager.
        labelsRecycler.setLayoutManager(new LinearLayoutManager(this.getContext()));

        setViewsVisibility();
        ((MainActivity)getActivity()).showCameraScanFAB();

        return root;
    }




    private String signatureImageName = "";
    private String getSignatureImageName(){
        if(signatureImageName.equals("")){
            String contactId = String.valueOf(stopsViewModel.getSelectedStopContact().getValue().contactId);
            String stopId = stopsViewModel.getSelectedStop().getValue().stopId;
            signatureImageName = stopId + "-" + contactId;
        }
        return signatureImageName;
    }
    private void observeServerResponse() {

        this.stopsViewModel.getmSendSignatureResponse().observe(getViewLifecycleOwner(), new Observer<ResponseMessage<Boolean>>() {

            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onChanged(ResponseMessage<Boolean> booleanResponseMessage) {


                if(booleanResponseMessage == null) {
                    return;
                }

                sendPickupProgress.setVisibility(View.GONE);

                if(booleanResponseMessage.success) {

                    stopsViewModel.getmSendSignatureResponse().setValue(null);
                    Toast.makeText(StopPickupFragment.this.getContext(), "Uploaded the Signature... Sending the Pickup Confirmation. Please wait", Toast.LENGTH_LONG).show();


                    String nfcBuilding = DriverRepository.scannedLocation.getValue() != null ? DriverRepository.scannedLocation.getValue().getLocationCode() : "";
                    double longitude = stopsViewModel.mDriverLocation.getValue().longitude;
                    double latitude = stopsViewModel.mDriverLocation.getValue().latitude;
                    String activityDate = GlobalCoordinator.getNowFormatted();
                    /*
                    LinkedList<StopLabel> labels = new LinkedList<>();
                    for (StopLabel sl :
                            stopsViewModel.getSelectedStopContact().getValue().stopLabelLiveData.getValue()) {
                        if (sl.status.equals(LabelStatus.Scanned.getLabelStatusCode()))
                            labels.add(sl);
                    }

                     */
                    String recipient = nameCollectedText.getText().toString();

                    sendPickupProgress.setVisibility(View.VISIBLE);
                    //stopsViewModel.sendDelivery(nfcBuilding, longitude, latitude, activityDate, labels, recipient, getSignatureImageName() + ".png", isLBP, amountCollected);


                    stopsViewModel.sendPickup(nfcBuilding, longitude, latitude, activityDate, adapter.getSendLabelItems(), recipient, getSignatureImageName() + ".png");

                    sendPickupProgress.setVisibility(View.VISIBLE);

                }else {
                    showError(booleanResponseMessage.errorMessage + " " + booleanResponseMessage.message);
                    mCancelButton.setVisibility(View.VISIBLE);
                    mClearButton.setVisibility(View.VISIBLE);
                    mSaveButton.setVisibility(View.VISIBLE);

                }

            }
        });


        this.stopsViewModel.getmSendPickupResponse().observe(getViewLifecycleOwner(), new Observer<ResponseMessage<Boolean>>() {
            @Override
            public void onChanged(ResponseMessage<Boolean> booleanResponseMessage) {
                if(booleanResponseMessage == null) return;
                handleServerSendResponse(booleanResponseMessage);
                stopsViewModel.getmSendPickupResponse().setValue(null);

            }
        });



        this.stopsViewModel.getmError().observe(getViewLifecycleOwner(), new Observer<DriverServicesError>() {
            @Override
            public void onChanged(DriverServicesError driverServicesError) {
                if(driverServicesError == null) return;;
                sendPickupProgress.setVisibility(View.GONE);

                mCancelButton.setVisibility(View.VISIBLE);
                mClearButton.setVisibility(View.VISIBLE);
                mSaveButton.setVisibility(View.VISIBLE);

                if(driverServicesError.errorType.equals(ServicesErrorType.generalError))
                    showError(driverServicesError.volleyError.toString());
                else
                    showError(driverServicesError.errorMessage);
                stopsViewModel.getmError().setValue(null);

            }
        });
    }

    private void handleServerSendResponse(ResponseMessage<Boolean> booleanResponseMessage) {
        sendPickupProgress.setVisibility(View.GONE);

        mCancelButton.setVisibility(View.VISIBLE);
        mClearButton.setVisibility(View.VISIBLE);
        mSaveButton.setVisibility(View.VISIBLE);


        if(booleanResponseMessage.success){
            for (StopLabel sl :
                    stopsViewModel.getSelectedStopContact().getValue().stopLabelLiveData.getValue()) {
                if (sl.status.equals(LabelStatus.Scanned.getLabelStatusCode())){
                    sl.status = LabelStatus.Delivered.getLabelStatusCode();
                    stopsViewModel.UpdateStopLabel(sl);
                }

                //stopsViewModel.getFragmentToLoad().setValue(StopsViewModel.StopFragments.StopLabelsFragment);
            }


            //((OnStopsFragmentsInteraction) hostingFragment).backToPreviousFragment(stopsViewModel.getSelectedStop().getValue().numberOfInCompleteLabels == 0);
            ((OnStopsFragmentsInteraction)hostingFragment).backToPreviousFragment(false);


        }else {
            showError(booleanResponseMessage.errorMessage + " " + booleanResponseMessage.message);
        }

    }

    private void showError(String errorMessage){
        Toast.makeText(StopPickupFragment.this.getContext(), errorMessage, Toast.LENGTH_LONG).show();
    }

    boolean isFirstSignClick = true;
    private void setupSignaturePad() {
        mSignaturePad.setOnSignedListener(new SignaturePad.OnSignedListener() {
            @Override
            public void onStartSigning() {
                //Toast.makeText(StopDeliveryFragment.this.getActivity(), "OnStartSigning", Toast.LENGTH_SHORT).show();
                //amountCollectedText.clearFocus();
                dismissKeyboard();
                setViewsVisibility();
                if(isFirstSignClick)
                {
                    mSignaturePad.clear();
                    isFirstSignClick = false;
                    //setViewsVisibility();
                }
            }

            @Override
            public void onSigned() {
                dismissKeyboard();
                //setViewsVisibility();

                if(!signed) {
                    signed = true;
                    setViewsVisibility();
                }


            }

            @Override
            public void onClear() {
                signed = false;
                setViewsVisibility();


            }


        });


    }

    private void dismissKeyboard() {
        View viewWithFocus = StopPickupFragment.this.getActivity().getCurrentFocus();
        if(viewWithFocus!=null){
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(viewWithFocus.getWindowToken(),0);

        }
        stopRecipientLayout.requestFocus();

    }

    private void setViewsVisibility() {




        if(signed && !nameCollectedText.getText().toString().equals("")){

            mSaveButton.setVisibility(View.VISIBLE);
            mClearButton.setVisibility(View.VISIBLE);
        }
        else {

            mSaveButton.setVisibility(View.GONE);
            mClearButton.setVisibility(View.GONE);
        }

    }

    private void findAndAssignTextAndActions(View root) {

        nameCollectedText = root.findViewById(R.id.nameCollectedText);
        mClearButton = root.findViewById(R.id.clear_button);
        mSaveButton = root.findViewById(R.id.save_button);
        mCancelButton = root.findViewById(R.id.cancel_button);


        sendPickupProgress = root.findViewById(R.id.sendPickupProgress);

        stopRecipientLayout = root.findViewById(R.id.stopPickupLayout);
        labelsRecycler = root.findViewById(R.id.labelsRecycler);




        nameCollectedText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                setViewsVisibility();
            }
        });

        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissKeyboard();
                ((OnStopsFragmentsInteraction) hostingFragment).backToPreviousFragment(false);
                //stopsViewModel.getFragmentToLoad().setValue(StopsViewModel.StopFragments.StopLabelsFragment);
            }
        });

        mClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSignaturePad.clear();
            }
        });

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View view) {

                //Make sure if there was a refund that the label is scanned

                for (SendLabelItem label :
                        adapter.getSendLabelItems()) {
                    if (label.cod > 0 && label.label.equals("")){
                        GlobalCoordinator.getInstance().notifyMessage(StopPickupFragment.this.getContext(),"Please Scan the refund label",0);
                        return;
                    }
                }
                sendPickupProgress.setVisibility(View.VISIBLE);
                mCancelButton.setVisibility(View.GONE);
                mClearButton.setVisibility(View.GONE);
                mSaveButton.setVisibility(View.GONE);

                stopsViewModel.sendSignature(getSignatureImageName(),getSignatureBytes());

            }
        });


        fabAddManual = root.findViewById(R.id.fabAddManual);
        fabAddManual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final ManualEntryDialog manualEntryDialog = new ManualEntryDialog(StopPickupFragment.this.getContext());
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


    }


    private byte[] getSignatureBytes() {
        Bitmap signatureBitmap = mSignaturePad.getSignatureBitmap();



        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        signatureBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        //signatureBitmap.compress(Bitmap.CompressFormat.JPEG,0,baos);
        byte[] imageBytes = baos.toByteArray();

        return imageBytes;
    }




    /* Mark Storage of Signature Photos */



    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length <= 0
                        || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(StopPickupFragment.this.getActivity(), "Cannot write images to external storage", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }




    /**
     * Checks if the app has permission to write to device storage
     * <p/>
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity the activity from which permissions are checked
     */

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }


    @Override
    public void onNewScan(String characters) {
        adapter.setScannedLabel(characters);
    }

    @Override
    public void onNewIntent(Intent intent) {

    }

    @Override
    public void onNewLocation(DriverLocation driverLocation) {
        stopsViewModel.getmDriverLocation().setValue(driverLocation);
    }
}