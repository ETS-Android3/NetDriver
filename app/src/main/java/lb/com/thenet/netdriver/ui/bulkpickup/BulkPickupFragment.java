package lb.com.thenet.netdriver.ui.bulkpickup;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.LinkedList;

import lb.com.thenet.netdriver.GlobalCoordinator;
import lb.com.thenet.netdriver.MainActivity;
import lb.com.thenet.netdriver.R;
import lb.com.thenet.netdriver.SwipeToDeleteCallback;
import lb.com.thenet.netdriver.adapters.BarcodeListAdapter;
import lb.com.thenet.netdriver.dataholders.DriverLocation;
import lb.com.thenet.netdriver.onlineservices.DriverServicesError;
import lb.com.thenet.netdriver.onlineservices.ServicesErrorType;
import lb.com.thenet.netdriver.onlineservices.json.ActivityType;
import lb.com.thenet.netdriver.onlineservices.json.ResponseMessage;
import lb.com.thenet.netdriver.onlineservices.json.ScanBuilding;
import lb.com.thenet.netdriver.rooms.entities.DriverRepository;
import lb.com.thenet.netdriver.ui.ConfirmDialog;
import lb.com.thenet.netdriver.ui.ManualEntryDialog;
import lb.com.thenet.netdriver.ui.checkout.CheckoutFragment;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BulkPickupFragment#} factory method to
 * create an instance of this fragment.
 */
public class BulkPickupFragment extends Fragment implements MainActivity.OnScanListener {


    private BulkPickupViewModel bulkPickupViewModel;
    public static final String ERROR_DETECTED = "No NFC tag detected!";

    NfcAdapter nfcAdapter;
    Tag myTag;
    Context context;

    TextView tvNFCContent;
    TextView testView;
    String mBuildingCode = "";
    RecyclerView recyclerView;
    Button okButton;
    Button cancelButton;
    ImageView scanLocationImage;
    ImageView scanLabelsImage;
    TextView counterLabel;
    ProgressBar progressBar;

    BarcodeListAdapter barcodeListAdapter;

    private final LinkedList<String> mBarcodeList = new LinkedList<>();


    LinearLayout linearLayout;
    FloatingActionButton fabAddManual;




    public BulkPickupFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            //mParam1 = getArguments().getString(ARG_PARAM1);
            //mParam2 = getArguments().getString(ARG_PARAM2);
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_bulk_pickup, container, false);
        bulkPickupViewModel = (new ViewModelProvider(this)).get(BulkPickupViewModel.class);
        //bulkPickupViewModel = ViewModelProviders.of(this).get(BulkPickupViewModel.class);

        View root = inflater.inflate(R.layout.fragment_bulk_pickup, container, false);


        context = this.getActivity();
        tvNFCContent = root.findViewById(R.id.nfc_contents);
        testView = root.findViewById(R.id.test_bulkPickup);
        recyclerView = root.findViewById(R.id.bulkPickupScannedItems);
        okButton = root.findViewById(R.id.bulkPickupDoBulkPickup);
        cancelButton = root.findViewById(R.id.bulkPickupCancelBulkPickup);
        scanLocationImage = root.findViewById(R.id.scanBuildingImage);
        scanLabelsImage = root.findViewById(R.id.scanLabelsImage);
        counterLabel = root.findViewById(R.id.labelCounter);
        linearLayout = root.findViewById(R.id.bulkPickupLayout);
        progressBar = root.findViewById(R.id.bulkPickupProgress);
        fabAddManual = root.findViewById(R.id.fabAddManual);

        //button click handlers
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkbarcode();

                cancelBulkPickup();
            }
        });

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bulkPickup();
            }
        });

        fabAddManual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final ManualEntryDialog manualEntryDialog = new ManualEntryDialog(BulkPickupFragment.this.getContext());
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


        //Make sure this device supports NFC
        nfcAdapter = NfcAdapter.getDefaultAdapter(context);
        if (nfcAdapter == null) {
            // Stop here, we definitely need NFC
            Toast.makeText(context, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
            bulkPickupViewModel.disableNFC.setValue(true);
        }


        if(scannedBuildingActive()){

            if(DriverRepository.scannedLocation != null && DriverRepository.scannedLocation.getValue() != null) {
                tvNFCContent.setText(DriverRepository.scannedLocation.getValue().getLocationName());
                tvNFCContent.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                mBuildingCode = DriverRepository.scannedLocation.getValue().getLocationCode();

                enableActions();
                showHideViews();
            }
        }

        //Set Observer for Scan Building Service Result
        bulkPickupViewModel.getmBuildingNFC().observe(getViewLifecycleOwner(), new Observer<ResponseMessage<ScanBuilding>>() {
            @Override
            public void onChanged(ResponseMessage<ScanBuilding> scanBuildingResponseMessage) {

                if(scanBuildingResponseMessage.success){
                    tvNFCContent.setText(scanBuildingResponseMessage.data.description);
                    tvNFCContent.setTextColor(getResources().getColor(android.R.color.holo_green_dark));

                }else {
                    tvNFCContent.setText("Failure: " + scanBuildingResponseMessage.errorMessage);

                    Toast.makeText(BulkPickupFragment.this.getContext(), "Failure: " + scanBuildingResponseMessage.errorMessage + scanBuildingResponseMessage.message, Toast.LENGTH_LONG).show();

                }
                enableActions();
                showHideViews();

            }
        });

        bulkPickupViewModel.getmCheckLabelResponse().observe(getViewLifecycleOwner(), new Observer<ResponseMessage<Boolean>>() {
            @Override
            public void onChanged(ResponseMessage<Boolean> booleanResponseMessage) {
                checkingLabel = false;
                fabAddManual.setVisibility(View.VISIBLE);
                okButton.setVisibility(View.VISIBLE);
                if(booleanResponseMessage.success && booleanResponseMessage.data){
                    addCurrentLabelToAdapterAndBeep();
                }else {
                    GlobalCoordinator.getInstance().notifyMessage(BulkPickupFragment.this.getContext(),
                            booleanResponseMessage.message + ": " + booleanResponseMessage.errorMessage + " (" + currentLabel +")",
                            0, false
                    );
                }
            }
        });

        //Set Observer for BulkPiickup Result
        bulkPickupViewModel.getmBulkPickupResponse().observe(getViewLifecycleOwner(), new Observer<ResponseMessage<Boolean>>() {
            @Override
            public void onChanged(ResponseMessage<Boolean> bulkPickupResponse) {

                testView.setText(bulkPickupResponse.message);
                if(bulkPickupResponse.success){
                    while(mBarcodeList.size()>0) {
                        barcodeListAdapter.removeItem(0);
                        /*
                        mBarcodeList.remove(0);
                        recyclerView.getAdapter().notifyItemRemoved(0);

                         */

                    }
                    Toast.makeText(BulkPickupFragment.this.getContext(), "Success - " + bulkPickupResponse.message, Toast.LENGTH_LONG).show();

                }
                else {
                    Toast.makeText(BulkPickupFragment.this.getContext(), "Failure: " + bulkPickupResponse.errorMessage + bulkPickupResponse.message, Toast.LENGTH_LONG).show();

                }
                enableActions();

            }
        });

        bulkPickupViewModel.getmError().observe(getViewLifecycleOwner(), new Observer<DriverServicesError>() {
            @Override
            public void onChanged(DriverServicesError driverServicesError) {

                if(driverServicesError == null) return;
                enableActions();

                if(driverServicesError.errorType.equals(ServicesErrorType.checkLabelError)){
                    checkingLabel = false;
                    fabAddManual.setVisibility(View.VISIBLE);
                    okButton.setVisibility(View.VISIBLE);

                }

                if(driverServicesError.volleyError != null){
                    Toast.makeText(BulkPickupFragment.this.getContext(), driverServicesError.volleyError.toString(), Toast.LENGTH_LONG).show();

                }
                else {
                    Toast.makeText(BulkPickupFragment.this.getContext(), driverServicesError.errorMessage, Toast.LENGTH_LONG).show();
                }
                bulkPickupViewModel.getmError().setValue(null);
            }
        });

        barcodeListAdapter = new BarcodeListAdapter(this.getContext(), mBarcodeList);

        // Connect the adapter with the recycler view.
        recyclerView.setAdapter(barcodeListAdapter);
        // Give the recycler view a default layout manager.
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));

        enableSwipeToDeleteAndUndo();


        barcodeListAdapter.observeListCountChange(new BarcodeListAdapter.ListCountChangeObserver() {
            @Override
            public void listCountChanged(int size) {
                if(size ==0){
                    scanLabelsImage.setVisibility(View.VISIBLE);
                    counterLabel.setVisibility(View.GONE);
                    okButton.setVisibility(View.GONE);
                    cancelButton.setVisibility(View.GONE);
                }else {
                    scanLabelsImage.setVisibility(View.GONE);
                    counterLabel.setVisibility(View.VISIBLE);
                    okButton.setVisibility(View.VISIBLE);
                    cancelButton.setVisibility(View.VISIBLE);
                }
                counterLabel.setText(String.valueOf(size));
            }
        });

        ((MainActivity)getActivity()).updateDriverLocation();

        ((MainActivity)getActivity()).showCameraScanFAB();

        return root;

    }


    private void checkbarcode() {

    }

    private void dismissKeyboard() {
        View viewWithFocus = BulkPickupFragment.this.getActivity().getCurrentFocus();
        if(viewWithFocus!=null){
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(viewWithFocus.getWindowToken(),0);

        }
    }
    private boolean scannedBuildingActive(){

        return !mBuildingCode.equals("") || (DriverRepository.scannedLocation.getValue() != null && DriverRepository.scannedLocation.getValue().isActive(getContext()));
    }

    private void disableActions(){
        progressBar.setVisibility(View.VISIBLE);
        okButton.setEnabled(false);
        cancelButton.setEnabled(false);

    }

    private void enableActions(){
        progressBar.setVisibility(View.GONE);
        okButton.setEnabled(true);
        cancelButton.setEnabled(true);
    }
    private void enableSwipeToDeleteAndUndo() {
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(this.getContext()) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {


                final int position = viewHolder.getAdapterPosition();
                final String item = barcodeListAdapter.getData().get(position);

                barcodeListAdapter.removeItem(position);


                Snackbar snackbar = Snackbar
                        .make(linearLayout, "Item was removed from the list.", Snackbar.LENGTH_LONG);
                snackbar.setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        barcodeListAdapter.restoreItem(item, position);
                        recyclerView.scrollToPosition(position);
                    }
                });

                snackbar.setActionTextColor(Color.YELLOW);
                snackbar.show();

            }
        };

        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchhelper.attachToRecyclerView(recyclerView);
    }



    @Override
    public void onResume() {
        super.onResume();
        showHideViews();

    }

    private void showHideViews(){

        //as NFC is snot needed here, the first option in thee if statemnet is truee
        if (true || bulkPickupViewModel.disableNFC.getValue() || GlobalCoordinator.getInstance().settingsDisableCheckInNFC || scannedBuildingActive() || (bulkPickupViewModel.getmDriverServices().mBuildingNFC != null && bulkPickupViewModel.getmDriverServices().mBuildingNFC.getValue() != null && bulkPickupViewModel.getmDriverServices().mBuildingNFC.getValue().success)){
            recyclerView.setVisibility(View.VISIBLE);
            okButton.setVisibility(View.VISIBLE);
            cancelButton.setVisibility(View.VISIBLE);
            scanLocationImage.setVisibility(View.GONE);
            fabAddManual.show();

            if(mBarcodeList.size()==0){
                scanLabelsImage.setVisibility(View.VISIBLE);
                counterLabel.setVisibility(View.GONE);
                okButton.setVisibility(View.GONE);
                cancelButton.setVisibility(View.GONE);
            }else {
                scanLabelsImage.setVisibility(View.GONE);
                counterLabel.setVisibility(View.VISIBLE);
                okButton.setVisibility(View.VISIBLE);
                cancelButton.setVisibility(View.VISIBLE);
            }

        }else{
            recyclerView.setVisibility(View.GONE);
            okButton.setVisibility(View.GONE);
            cancelButton.setVisibility(View.GONE);
            scanLocationImage.setVisibility(View.VISIBLE);
            scanLabelsImage.setVisibility(View.GONE);
            counterLabel.setVisibility(View.GONE);
            fabAddManual.hide();
        }

        if(true || GlobalCoordinator.getInstance().settingsDisableCheckInNFC || bulkPickupViewModel.disableNFC.getValue()){
            tvNFCContent.setVisibility(View.GONE);
        }
    }

    private void cancelBulkPickup(){


        final ConfirmDialog confirmDialog = new ConfirmDialog(this.getContext(), "Please Confirm Canceling All Scanned Labels");
        confirmDialog.setConfirmDialogListener(new ConfirmDialog.ConfirmDialogListener() {
            @Override
            public void onConfirmClick(View v) {
                while(mBarcodeList.size()>0) {
                    barcodeListAdapter.removeItem(0);
                }
                confirmDialog.dismiss();
            }

            @Override
            public void onCancelClick(View v) {
                confirmDialog.dismiss();

            }
        });
        confirmDialog.show();

    }
    private void bulkPickup() {
        if(mBarcodeList.size() == 0) return;

        if(true || GlobalCoordinator.getInstance().settingsDisableCheckInNFC){
            mBuildingCode = getString(R.string.out_of_office);
            tvNFCContent.setText(getString(R.string.out_of_office));
        }

        final ConfirmDialog confirmDialog = new ConfirmDialog(this.getContext(), "Are you sure you want to bulk pickup " + mBarcodeList.size() + " items to " + tvNFCContent.getText() +"?");
        confirmDialog.setConfirmDialogListener(new ConfirmDialog.ConfirmDialogListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onConfirmClick(View v) {

                bulkPickupViewModel.getmDriverServices().bulkPickupBarcodes(mBuildingCode,mBarcodeList, bulkPickupViewModel.getmDriverLocation().getValue(), GlobalCoordinator.getNowFormatted());


                confirmDialog.dismiss();
                disableActions();

            }

            @Override
            public void onCancelClick(View v) {
                confirmDialog.dismiss();

            }
        });
        confirmDialog.show();


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

    private void readFromIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {


            Parcelable pe = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            Tag tagg = (Tag) pe;
            byte[] idd = tagg.getId();
            String rfid = getHex(idd);

            mBuildingCode = rfid;
            tvNFCContent.setText("Scanned Code: " + mBuildingCode);
            tvNFCContent.setTextColor(getResources().getColor(R.color.colorPrimary));
        }
        mBuildingCode = GlobalCoordinator.getInstance().getBuildingCode(mBuildingCode);

    }


    @Override
    public void onNewIntent(Intent intent) {
        if(NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())){
            myTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            //TODO: Get the buildingCode
        }

        readFromIntent(intent);
        disableActions();

        bulkPickupViewModel.getmDriverServices().scanBuildingNFC(mBuildingCode);
    }


    @Override
    public void onNewLocation(DriverLocation driverLocation) {
        bulkPickupViewModel.getmDriverLocation().setValue(driverLocation);
    }

    @Override
    public void onNewScan(String  keyCharacterMap) {


        if (true || bulkPickupViewModel.disableNFC.getValue() || GlobalCoordinator.getInstance().settingsDisableCheckInNFC || scannedBuildingActive() || (bulkPickupViewModel.getmDriverServices().mBuildingNFC != null && bulkPickupViewModel.getmDriverServices().mBuildingNFC.getValue() != null && bulkPickupViewModel.getmDriverServices().mBuildingNFC.getValue().success)) {
            testView.setText(keyCharacterMap);
            if(mBarcodeList.contains(keyCharacterMap)){
                GlobalCoordinator.getInstance().notifyMessage(context, "Item Already Scanned!",2);
                return;
            }

            int barcodeListSize = mBarcodeList.size();
            if(barcodeListSize >= getResources().getInteger(R.integer.maximumScannedBarcodes)){
                GlobalCoordinator.getInstance().notifyMessage(context, "Too Much Items - Please Save and scan again!", 0);
                return;
            }

            //Check the label using the limitations service
            currentLabel = keyCharacterMap;

            checkCurrentLabel();
            //addCurrentLabelToAdapterAndBeep();

        }

    }



    String currentLabel;
    boolean checkingLabel = false;

    private void checkCurrentLabel(){
        checkingLabel = true;
        fabAddManual.setVisibility(View.GONE);
        okButton.setVisibility(View.GONE);

        bulkPickupViewModel.getmDriverServices().checkLabel(ActivityType.BULKPICKUP, currentLabel);

    }

    private void addCurrentLabelToAdapterAndBeep() {

        if(mBarcodeList.contains(currentLabel)){
            GlobalCoordinator.getInstance().notifyMessage(context, "Item Already Scanned!",2);
            return;
        }

        int barcodeListSize = mBarcodeList.size();
        barcodeListAdapter.insertItem(currentLabel,barcodeListSize);
        recyclerView.smoothScrollToPosition(barcodeListSize);

        //beep success
        GlobalCoordinator.getInstance().beepScanned(context);
    }




}