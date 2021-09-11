package lb.com.thenet.netdriver.ui.weightadjust;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.LinkedList;

import lb.com.thenet.netdriver.GlobalCoordinator;
import lb.com.thenet.netdriver.MainActivity;
import lb.com.thenet.netdriver.R;
import lb.com.thenet.netdriver.SwipeToDeleteCallback;
import lb.com.thenet.netdriver.adapters.AdjustedShipmentListAdapter;
import lb.com.thenet.netdriver.adapters.BarcodeListAdapter;
import lb.com.thenet.netdriver.dataholders.DriverLocation;
import lb.com.thenet.netdriver.onlineservices.DriverServicesError;
import lb.com.thenet.netdriver.onlineservices.json.AdjustedShipment;
import lb.com.thenet.netdriver.onlineservices.json.ResponseMessage;
import lb.com.thenet.netdriver.onlineservices.json.Weight;
import lb.com.thenet.netdriver.ui.ConfirmDialog;
import lb.com.thenet.netdriver.ui.ManualEntryDialog;
import lb.com.thenet.netdriver.ui.WeightAdjustDialog;
import lb.com.thenet.netdriver.ui.liaison.LiaisonFragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class WeightAdjustFragment extends Fragment implements MainActivity.OnScanListener {


    private WeightAdjustViewModel weightAdjustViewModel;

    RecyclerView recyclerView;
    Button okButton;
    Button cancelButton;
    ImageView scanLabelsImage;
    TextView counterLabel;
    ProgressBar progressBar;

    AdjustedShipmentListAdapter adjustedShipmentListAdapter;
    private final LinkedList<AdjustedShipment> adjustedShipments = new LinkedList<>();


    LinearLayout linearLayout;
    FloatingActionButton fabAddManual;
    Context context;

    //Controls for weight or dimension adjust
    RadioGroup weightAdjustType;
    RadioButton weightAdjustTypeWeight;
    RadioButton weightAdjustTypeDimension;
    EditText weightAdjustNOP;
    EditText weightAdjustWeight;
    EditText weightAdjustLength;
    EditText weightAdjustWidth;
    EditText weightAdjustHeight;
    TextView weightAdjustFillDetails;
    Button weightAdjustStartScanning;

    public WeightAdjustFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        weightAdjustViewModel = ViewModelProviders.of(this).get(WeightAdjustViewModel.class);

        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_weight_adjust, container, false);

        context = this.getActivity();
        recyclerView = root.findViewById(R.id.weightAdjustScannedItems);
        okButton = root.findViewById(R.id.weightAdjustDoWeightAdjust);
        cancelButton = root.findViewById(R.id.weightAdjustCancelWeightAdjust);
        scanLabelsImage = root.findViewById(R.id.scanLabelsImage);
        counterLabel = root.findViewById(R.id.labelCounter);
        linearLayout = root.findViewById(R.id.weightAdjustLayout);
        progressBar = root.findViewById(R.id.weightAdjustProgress);
        fabAddManual = root.findViewById(R.id.fabAddManual);

        weightAdjustType = root.findViewById(R.id.weightAdjustType);
        weightAdjustTypeWeight = root.findViewById(R.id.weightAdjustTypeWeight);
        weightAdjustTypeDimension = root.findViewById(R.id.weightAdjustTypeDimension);
        weightAdjustNOP = root.findViewById(R.id.weightAdjustNOP);
        weightAdjustWeight = root.findViewById(R.id.weightAdjustWeight);
        weightAdjustLength = root.findViewById(R.id.weightAdjustLength);
        weightAdjustWidth = root.findViewById(R.id.weightAdjustWidth);
        weightAdjustHeight = root.findViewById(R.id.weightAdjustHeight);
        weightAdjustFillDetails = root.findViewById(R.id.weightAdjustFillDetails);
        weightAdjustStartScanning = root.findViewById(R.id.weightAdjustStartScanning);

        //button click handlers
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelAdjustedShipments();
            }
        });

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adjustShipments();
            }
        });

        weightAdjustStartScanning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearFocuses();
            }
        });

        fabAddManual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final ManualEntryDialog manualEntryDialog = new ManualEntryDialog(WeightAdjustFragment.this.getContext());
                manualEntryDialog.setmListener(new ManualEntryDialog.OnManualEntryDialogListener() {
                    @Override
                    public void doneEditing(String s) {
                        dismissKeyboard();
                        onNewScan(s);
                        manualEntryDialog.dismiss();
                        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

                    }

                    @Override
                    public void dismiss() {
                        manualEntryDialog.dismiss();
                    }
                });
                manualEntryDialog.show();
            }
        });


        weightAdjustViewModel.getmWeightAdjustResponse().observe(getViewLifecycleOwner(), new Observer<ResponseMessage<Boolean>>() {
            @Override
            public void onChanged(ResponseMessage<Boolean> booleanResponseMessage) {
                if(booleanResponseMessage.success){
                    while(adjustedShipments.size()>0) {
                        adjustedShipmentListAdapter.removeItem(0);
                        /*
                        mBarcodeList.remove(0);
                        recyclerView.getAdapter().notifyItemRemoved(0);

                         */

                    }
                    Toast.makeText(WeightAdjustFragment.this.getContext(), "Success - " + booleanResponseMessage.message, Toast.LENGTH_LONG).show();

                }
                else {
                    Toast.makeText(WeightAdjustFragment.this.getContext(), "Failure: " + booleanResponseMessage.errorMessage + booleanResponseMessage.message, Toast.LENGTH_LONG).show();

                }
                enableActions();
            }
        });

        weightAdjustViewModel.getmError().observe(this, new Observer<DriverServicesError>() {
            @Override
            public void onChanged(DriverServicesError driverServicesError) {
                if(driverServicesError == null) return;
                enableActions();

                if(driverServicesError.volleyError != null){
                    Toast.makeText(WeightAdjustFragment.this.getContext(), driverServicesError.volleyError.toString(), Toast.LENGTH_LONG).show();

                }
                else {
                    Toast.makeText(WeightAdjustFragment.this.getContext(), driverServicesError.errorMessage, Toast.LENGTH_LONG).show();
                }
                weightAdjustViewModel.getmError().setValue(null);
            }
        });

        adjustedShipmentListAdapter = new AdjustedShipmentListAdapter(this.getContext(), adjustedShipments);

        // Connect the adapter with the recycler view.
        recyclerView.setAdapter(adjustedShipmentListAdapter);
        // Give the recycler view a default layout manager.
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));

        enableSwipeToDeleteAndUndo();

        adjustedShipmentListAdapter.observeListCountChange(new AdjustedShipmentListAdapter.ListCountChangeObserver() {
            @Override
            public void listCountChanged(int size) {
                if(size ==0){
                    //scanLabelsImage.setVisibility(View.VISIBLE);
                    counterLabel.setVisibility(View.GONE);

                }else {
                    //scanLabelsImage.setVisibility(View.GONE);
                    counterLabel.setVisibility(View.VISIBLE);
                }
                counterLabel.setText(String.valueOf(size));
                showHideControls();
            }
        });

        weightAdjustTypeDimension.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissKeyboard();
                showHideControls();
            }
        });

        weightAdjustTypeWeight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissKeyboard();
                showHideControls();
            }
        });

        watchAfterEdit(weightAdjustNOP, weightAdjustWeight, weightAdjustLength, weightAdjustHeight, weightAdjustWidth);

        ((MainActivity)getActivity()).showCameraScanFAB();
        return root;
    }

    private void clearFocuses() {
        dismissKeyboard();
        weightAdjustNOP.clearFocus();
        weightAdjustWeight.clearFocus();
        weightAdjustLength.clearFocus();
        weightAdjustWidth.clearFocus();
        weightAdjustHeight.clearFocus();

    }

    private void watchAfterEdit(EditText... editTexts) {
        for (EditText editText :
                editTexts) {
            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    showHideControls();
                    //dismissKeyboard();
                }
            });
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        showHideControls();
    }

    boolean canScan = false;

    private void showHideControls() {
        boolean oneScanned = false;
        canScan = false;
        //scanLabelsImage  adapter.countItems
        if(weightAdjustTypeWeight.isChecked()){
            //validate NOP and Weight
            weightAdjustLength.setVisibility(View.GONE);
            weightAdjustWidth.setVisibility(View.GONE);
            weightAdjustHeight.setVisibility(View.GONE);
            weightAdjustNOP.setVisibility(View.VISIBLE);
            weightAdjustWeight.setVisibility(View.VISIBLE);
            if(checkInteger(weightAdjustNOP) && checkDouble(weightAdjustWeight))
                canScan = true;

        }
        if(weightAdjustTypeDimension.isChecked()){
            weightAdjustLength.setVisibility(View.VISIBLE);
            weightAdjustWidth.setVisibility(View.VISIBLE);
            weightAdjustHeight.setVisibility(View.VISIBLE);
            weightAdjustNOP.setVisibility(View.GONE);
            weightAdjustWeight.setVisibility(View.GONE);
            if(checkDouble(weightAdjustLength) && checkDouble(weightAdjustWidth) && checkDouble(weightAdjustHeight))
                canScan = true;
        }
        if(adjustedShipmentListAdapter.countItems()>0) oneScanned = true;
        if(canScan){
            weightAdjustFillDetails.setVisibility(View.GONE);
            weightAdjustStartScanning.setVisibility(View.VISIBLE);

            if(oneScanned)
                scanLabelsImage.setVisibility(View.GONE);
            else
                scanLabelsImage.setVisibility(View.VISIBLE);

        }else {
            weightAdjustFillDetails.setVisibility(View.VISIBLE);
            weightAdjustStartScanning.setVisibility(View.GONE);
            scanLabelsImage.setVisibility(View.GONE);
        }

        //dismissKeyboard();
    }

    private boolean checkInteger(EditText editText){
        try {
            Integer dbl = Integer.parseInt(editText.getText().toString());
            if(dbl != null && dbl >=0) return true;
            else return false;
        }catch (Exception ex){return false;}
    }
    private boolean checkDouble(EditText editText){
        boolean retVal = false;
        try {
            Double dbl = Double.parseDouble(editText.getText().toString());
            if(dbl != null && dbl >=0) retVal = true;
            else retVal = false;
        }catch (Exception ex){retVal = false;}

        return retVal;
    }

    private void enableSwipeToDeleteAndUndo() {
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(this.getContext()) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {


                final int position = viewHolder.getAdapterPosition();
                final AdjustedShipment item = adjustedShipmentListAdapter.getData().get(position);

                adjustedShipmentListAdapter.removeItem(position);


                Snackbar snackbar = Snackbar
                        .make(linearLayout, "Item was removed from the list.", Snackbar.LENGTH_LONG);
                snackbar.setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        adjustedShipmentListAdapter.restoreItem(item, position);
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
    private void dismissKeyboard() {
        View viewWithFocus = WeightAdjustFragment.this.getActivity().getCurrentFocus();
        if(viewWithFocus!=null){
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(viewWithFocus.getWindowToken(),0);

        }
    }

    private void adjustShipments() {
        if(adjustedShipments.size() == 0) return;

        final ConfirmDialog confirmDialog = new ConfirmDialog(this.getContext(), "Are you sure you want to adjust " + adjustedShipments.size() + " shipments?");
        confirmDialog.setConfirmDialogListener(new ConfirmDialog.ConfirmDialogListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onConfirmClick(View v) {
                weightAdjustViewModel.weightAdjust(adjustedShipments);

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

    private void cancelAdjustedShipments() {
        final ConfirmDialog confirmDialog = new ConfirmDialog(this.getContext(), "Please Confirm Canceling All Adjusted Shipments");
        confirmDialog.setConfirmDialogListener(new ConfirmDialog.ConfirmDialogListener() {
            @Override
            public void onConfirmClick(View v) {
                while(adjustedShipments.size()>0) {
                    adjustedShipmentListAdapter.removeItem(0);
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

    @Override
    public void onNewScan(final String characters) {
        dismissKeyboard();
        showHideControls();
        if(!canScan){
            GlobalCoordinator.getInstance().notifyMessage(context, "Please Fill in adjustments first!",2);
            return;
        }
        boolean found = false;
        for (AdjustedShipment s :
                adjustedShipments) {
            if(s.reference.equals(characters)) found = true;
        }
        if(found){
            GlobalCoordinator.getInstance().notifyMessage(context, "Item Already Scanned!",2);
            return;
        }

        AdjustedShipment s = new AdjustedShipment(0,0,0,0,0,0,0);
        s.reference = characters;

        if(weightAdjustTypeWeight.isChecked()) {
            s.weight.chargeable = Double.parseDouble(weightAdjustWeight.getText().toString());
            s.weight.nop = Integer.parseInt(weightAdjustNOP.getText().toString());
        }
        if(weightAdjustTypeDimension.isChecked()){
            s.dimension.length = Double.parseDouble(weightAdjustLength.getText().toString());
            s.dimension.width = Double.parseDouble(weightAdjustWidth.getText().toString());
            s.dimension.height = Double.parseDouble(weightAdjustHeight.getText().toString());
        }
        int barcodeListSize = adjustedShipments.size();
        adjustedShipmentListAdapter.insertItem(s, barcodeListSize);
        // Scroll to the bottom.
        recyclerView.smoothScrollToPosition(barcodeListSize);
        //beep success
        GlobalCoordinator.getInstance().beepScanned(context);
        clearFocuses();
        /*

        if(weightAdjustViewModel.isCopied()){
            int barcodeListSize = adjustedShipments.size();
            AdjustedShipment s = new AdjustedShipment(weightAdjustViewModel.getSampleShipment().dimension.length,weightAdjustViewModel.getSampleShipment().dimension.width,weightAdjustViewModel.getSampleShipment().dimension.height,
                    weightAdjustViewModel.getSampleShipment().weight.chargeable,weightAdjustViewModel.getSampleShipment().weight.volumetric,weightAdjustViewModel.getSampleShipment().weight.volume,
                    weightAdjustViewModel.getSampleShipment().weight.nop);
            s.reference = characters;
            adjustedShipmentListAdapter.insertItem(s, barcodeListSize);

            // Scroll to the bottom.
            recyclerView.smoothScrollToPosition(barcodeListSize);
            //beep success
            GlobalCoordinator.getInstance().beepScanned(context);

        }else {
            final WeightAdjustDialog weightAdjustDialog = new WeightAdjustDialog(WeightAdjustFragment.this.getContext());
            weightAdjustDialog.setmListener(new WeightAdjustDialog.OnWeightAdjustDialogListener() {
                @Override
                public void doneEditing(AdjustedShipment s, boolean copy) {
                    dismissKeyboard();
                    int barcodeListSize = adjustedShipments.size();
                    if (copy)
                        weightAdjustViewModel.setSampleShipment(s);
                    s.reference = characters;
                    adjustedShipmentListAdapter.insertItem(s, barcodeListSize);

                    // Scroll to the bottom.
                    recyclerView.smoothScrollToPosition(barcodeListSize);
                    //beep success
                    GlobalCoordinator.getInstance().beepScanned(context);

                    weightAdjustDialog.dismiss();
                }

                @Override
                public void dismiss() {
                    weightAdjustDialog.dismiss();
                }
            });
            weightAdjustDialog.show();
        }

         */


    }

    @Override
    public void onNewIntent(Intent intent) {

    }

    @Override
    public void onNewLocation(DriverLocation driverLocation) {

    }
}
