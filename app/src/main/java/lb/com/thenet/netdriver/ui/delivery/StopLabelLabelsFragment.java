package lb.com.thenet.netdriver.ui.delivery;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import lb.com.thenet.netdriver.GlobalCoordinator;
import lb.com.thenet.netdriver.MainActivity;
import lb.com.thenet.netdriver.R;
import lb.com.thenet.netdriver.SwipeToUnScanCallback;
import lb.com.thenet.netdriver.adapters.OrderLabelAdapter;
import lb.com.thenet.netdriver.dataholders.DriverLocation;
import lb.com.thenet.netdriver.onlineservices.DriverServicesError;
import lb.com.thenet.netdriver.onlineservices.json.NetLockerData;
import lb.com.thenet.netdriver.onlineservices.json.OrderType;
import lb.com.thenet.netdriver.onlineservices.json.ResponseMessage;
import lb.com.thenet.netdriver.onlineservices.json.ScanBuilding;
import lb.com.thenet.netdriver.rooms.entities.enums.LabelStatus;
import lb.com.thenet.netdriver.rooms.entities.enums.ShipmentType;
import lb.com.thenet.netdriver.rooms.entities.stops.StopLabel;
import lb.com.thenet.netdriver.ui.ConfirmDialog;
import lb.com.thenet.netdriver.ui.ManualEntryDialog;

/**
 * A simple {@link Fragment} subclass.
 */
public class StopLabelLabelsFragment extends Fragment implements OrderLabelAdapter.OnStopLabelClickedListener, MainActivity.OnScanListener {

    private StopsViewModel stopsViewModel;
    private Fragment hostingFragment;
    private RecyclerView labelsRecycler;
    private OrderLabelAdapter adapter;

    ImageView labelsSSendNonDeliveryImage;
    ImageView labelsSSendDeliveryImage;
    TextView labelScanEmptyLabel;
    TextView tvNFCContent;
    ProgressBar stopLabelsProgress;
    FloatingActionButton fabAddManual;

    ConstraintLayout layout;
    String mBuildingCode = "";
    StopLabel clickedEmptyStopLabel = null;
    boolean listening_to_empty_label = false;
    boolean firstCount = true;

    private MutableLiveData<Boolean> buildingScanned;


    public StopLabelLabelsFragment() {
        // Required empty public constructor
    }

    public StopLabelLabelsFragment(Fragment hostingFragment){
        this.hostingFragment = hostingFragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        stopsViewModel = ViewModelProviders.of(hostingFragment).get(StopsViewModel.class);

        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_stop_labels, container, false);

        labelsRecycler = root.findViewById(R.id.labelsRecycler);
        TextView contactFullNameText = root.findViewById(R.id.labelContactFullNameText);
        TextView contactMobileText = root.findViewById(R.id.labelContactMobileText);
        TextView contactPhoneText = root.findViewById(R.id.labelContactPhoneText);
        labelsSSendNonDeliveryImage = root.findViewById(R.id.labelsSSendNonDeliveryImage);
        labelsSSendDeliveryImage = root.findViewById(R.id.labelsSSendDeliveryImage);
        layout = root.findViewById(R.id.stopLabelsLayout);
        stopLabelsProgress = root.findViewById(R.id.stopLabelsProgress);
        fabAddManual = root.findViewById(R.id.fabAddManual);
        tvNFCContent = root.findViewById(R.id.nfc_contents);
        labelScanEmptyLabel = root.findViewById(R.id.labelScanEmptyLabel);


        contactFullNameText.setText(getContext().getString(R.string.firstNamelastNameFormat, stopsViewModel.getSelectedStopContact().getValue().firstName, stopsViewModel.getSelectedStopContact().getValue().lastName));
        contactMobileText.setText(stopsViewModel.getSelectedStopContact().getValue().mobile);
        contactPhoneText.setText(stopsViewModel.getSelectedStopContact().getValue().phone);
        GlobalCoordinator.CallOnClick(contactPhoneText,this.getActivity());
        GlobalCoordinator.CallOnClick(contactPhoneText,this.getActivity());

        stopsViewModel.DeepLoadContact(stopsViewModel.getSelectedStopContact().getValue());
        stopsViewModel.getSelectedStopContact().getValue().stopLabelLiveData.observe(getViewLifecycleOwner(), new Observer<List<StopLabel>>() {
            @Override
            public void onChanged(List<StopLabel> stopLabels) {
                adapter.setStops(stopLabels);

                enableDisableButtons();
            }
        });

        Button contactsBackButton = root.findViewById(R.id.contactsBackButton);
        contactsBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //make sure the retour is done
                if(checkIfAssignedRetourDone())
                    ((OnStopsFragmentsInteraction) hostingFragment).backToPreviousFragment(false);
                else {
                    GlobalCoordinator.getInstance().notifyMessage(StopLabelLabelsFragment.this.getContext(), "Please make sure to do the retour on assigned label(s)", 0);
                }
                //stopsViewModel.getFragmentToLoad().setValue(StopsViewModel.StopFragments.StopContactsFragment);
            }
        });

        labelsSSendDeliveryImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //labelsSSendDeliveryImage.setVisibility(View.INVISIBLE);
                //labelsSSendNonDeliveryImage.setVisibility(View.INVISIBLE);

                //If Delivery, Invoke the new Delivery Screen with flexible COD
                if(stopsViewModel.getOrderType().equals(OrderType.DELIVERY)){
                    stopsViewModel.getFragmentToLoad().setValue(StopsViewModel.StopFragments.StopDeliveryFragment);

                }
                else
                    //for all pickups, invoke the new pickup screen
                    stopsViewModel.getFragmentToLoad().setValue(StopsViewModel.StopFragments.StopPickupFragment);
            }
        });
        labelsSSendNonDeliveryImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopsViewModel.getFragmentToLoad().setValue(StopsViewModel.StopFragments.StopDeliveryFailureFragment);
            }
        });
        fabAddManual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final ManualEntryDialog manualEntryDialog = new ManualEntryDialog(StopLabelLabelsFragment.this.getContext());
                manualEntryDialog.setmListener(new ManualEntryDialog.OnManualEntryDialogListener() {
                    @Override
                    public void doneEditing(String s) {
                        dismissKeyboard();
                        setLabelAsScanned(s);
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

        stopsViewModel.getmBuildingNFC().observe(getViewLifecycleOwner(), new Observer<ResponseMessage<ScanBuilding>>() {
            @Override
            public void onChanged(ResponseMessage<ScanBuilding> scanBuildingResponseMessage) {

                if(scanBuildingResponseMessage == null) return;
                if(scanBuildingResponseMessage.success){
                    tvNFCContent.setText(scanBuildingResponseMessage.data.description);
                    tvNFCContent.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    buildingScanned.setValue(true);

                }else {
                    tvNFCContent.setText("Failure: " + scanBuildingResponseMessage.errorMessage);

                    Toast.makeText(StopLabelLabelsFragment.this.getContext(), "Failure: " + scanBuildingResponseMessage.errorMessage + scanBuildingResponseMessage.message, Toast.LENGTH_LONG).show();

                }
                stopsViewModel.getmBuildingNFC().setValue(null);

            }
        });
        setupRecyclerView();
        enableSwipeToUndoScan();
        labelsSSendNonDeliveryImage.setVisibility(View.INVISIBLE);
        labelsSSendDeliveryImage.setVisibility(View.INVISIBLE);

        buildingScanned = new MutableLiveData<>(false);
        buildingScanned.observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean)
                    GlobalCoordinator.getInstance().notifyMessage(StopLabelLabelsFragment.this.getContext(), "You Can Proceed with Scanning Net Point Shipments", 0, false
                    );
            }
        });

        stopsViewModel.getmNetLockerResponse().observe(getViewLifecycleOwner(), new Observer<ResponseMessage<NetLockerData>>() {
            @Override
            public void onChanged(ResponseMessage<NetLockerData> stringResponseMessage) {
                if(stringResponseMessage == null) return;
                stopLabelsProgress.setVisibility(View.GONE);
                if(stringResponseMessage.success) {
                    //if(stringResponseMessage.data == null) stringResponseMessage.data = "No Data Found for Net Locker";
                    GlobalCoordinator.getInstance().notifyMessage(StopLabelLabelsFragment.this.getContext(), StopLabelLabelsFragment.this.getString(R.string.netLockerDisplayMessageFormat, stringResponseMessage.data.lockerNumber, stringResponseMessage.data.pinNumber), 0, false);
                    if(netLockerStopLabel != null) {
                        netLockerStopLabel.status = LabelStatus.Scanned.getLabelStatusCode();
                        stopsViewModel.UpdateStopLabel(netLockerStopLabel);
                    }
                }else{
                    GlobalCoordinator.getInstance().notifyMessage(StopLabelLabelsFragment.this.getContext(), "Couldn't Get the Net Locker and PIN. UNDO and Scan Again. " + stringResponseMessage.errorMessage, 0);
                    if(netLockerStopLabel != null) {
                        netLockerStopLabel.status = LabelStatus.NotScanned.getLabelStatusCode();
                        stopsViewModel.UpdateStopLabel(netLockerStopLabel);
                    }
                }
                stopsViewModel.getmNetLockerResponse().setValue(null);
            }
        });
        stopsViewModel.getmError().observe(getViewLifecycleOwner(), new Observer<DriverServicesError>() {
            @Override
            public void onChanged(DriverServicesError driverServicesError) {
                if(driverServicesError == null) return;
                stopLabelsProgress.setVisibility(View.GONE);
                GlobalCoordinator.getInstance().notifyMessage(StopLabelLabelsFragment.this.getContext(), "Couldn't Get the Net Locker and PIN. UNDO and Scan Again. " + driverServicesError.errorMessage, 0);
                if(netLockerStopLabel != null) {
                    netLockerStopLabel.status = LabelStatus.NotScanned.getLabelStatusCode();
                    stopsViewModel.UpdateStopLabel(netLockerStopLabel);
                }
                stopsViewModel.getmError().setValue(null);
            }
        });

        stopsViewModel.countIncompleteLabels(stopsViewModel.getSelectedStop().getValue().stopId).observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                if (firstCount) {
                    firstCount = false;
                    return;
                }
                if (integer == 0)
                    if(checkIfAssignedRetourDone())
                        ((OnStopsFragmentsInteraction) hostingFragment).backToPreviousFragment(true);
                    else {
                        GlobalCoordinator.getInstance().notifyMessage(StopLabelLabelsFragment.this.getContext(), "Please make sure to do the retour on assigned label(s)", 0);
                        return;
                    }
                    //((OnStopsFragmentsInteraction) hostingFragment).backToPreviousbackToPreviousFragment(true);
            }
        });

        return root;
    }

    //Checks if thee assigned retour(s) arre done after delivering the labels
    private boolean checkIfAssignedRetourDone(){
        boolean done = true;
        //this only applies in delivery
        if(stopsViewModel.getOrderType().equals(OrderType.PICKUP))
            return done;

        for (StopLabel label :
                adapter.getData()) {
            if(label.status.equals(LabelStatus.Delivered.getLabelStatusCode()) && label.hasRetour && !label.retourDone){
                done = false;
                break;
            }
        }
        return done;
    }

    private void dismissKeyboard() {
        View viewWithFocus = StopLabelLabelsFragment.this.getActivity().getCurrentFocus();
        if(viewWithFocus!=null){
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(viewWithFocus.getWindowToken(),0);

        }
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    private void enableSwipeToUndoScan() {
        SwipeToUnScanCallback swipeToDeleteCallback = new SwipeToUnScanCallback(this.getContext()) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {


                final int position = viewHolder.getAdapterPosition();
                final StopLabel item = adapter.getData().get(position);

                //adapter.removeItem(position);
                final String oldStatus = item.status;
                switch (LabelStatus.get(oldStatus)){
                    case Scanned:{
                        item.status = LabelStatus.NotScanned.getLabelStatusCode();
                        stopsViewModel.UpdateStopLabel(item);


                        Snackbar snackbar = Snackbar
                                .make(layout, "Item was updated as not scanned.", Snackbar.LENGTH_LONG);
                        snackbar.setAction("UNDO", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                //adapter.restoreItem(item, position);
                                item.status = oldStatus;
                                stopsViewModel.UpdateStopLabel(item);
                                labelsRecycler.scrollToPosition(position);
                            }
                        });

                        snackbar.setActionTextColor(Color.YELLOW);
                        snackbar.show();
                        break;
                    }
                    case Delivered:  {
                        stopsViewModel.UpdateStopLabel(item);
                        Toast.makeText(StopLabelLabelsFragment.this.getContext(),"Nothing to do", Toast.LENGTH_LONG).show();
                        break;
                    }
                    case NotScanned:{
                        //if this was added manually to a fully new label, and is not scanned, allow to delete
                        if(item.deliveryReason != null && item.deliveryReason.equals("Added")) {


                            String s = "Do you want to DELETE the label?";
                            final ConfirmDialog confirmDialog = new ConfirmDialog(StopLabelLabelsFragment.this.getContext(), s);
                            confirmDialog.setConfirmDialogListener(new ConfirmDialog.ConfirmDialogListener() {
                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                @Override
                                public void onConfirmClick(View v) {

                                    stopsViewModel.DeleteStopLabel(item);
                                    confirmDialog.dismiss();

                                }

                                @Override
                                public void onCancelClick(View v) {
                                    confirmDialog.dismiss();

                                }
                            });
                            confirmDialog.show();
                            confirmDialog.setImageAction(R.mipmap.ic_delete);


                        }else {

                            //item.hawb = "";
                            //stopsViewModel.UpdateStopLabel(item);

                            stopsViewModel.UpdateStopLabel(item);
                            stopsViewModel.getSelectedStopLabel().setValue(item);
                            stopsViewModel.getFragmentToLoad().setValue(StopsViewModel.StopFragments.StopDeliveryFailureFragment);
                        }
                        break;
                    }
                    case NotDelivered:{
                        String s = "Do you want to reset this label?";
                        final ConfirmDialog confirmDialog = new ConfirmDialog(StopLabelLabelsFragment.this.getContext(), s);
                        confirmDialog.setConfirmDialogListener(new ConfirmDialog.ConfirmDialogListener() {
                            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                            @Override
                            public void onConfirmClick(View v) {
                                item.status = LabelStatus.NotScanned.getLabelStatusCode();
                                stopsViewModel.UpdateStopLabel(item);
                                confirmDialog.dismiss();
                            }

                            @Override
                            public void onCancelClick(View v) {
                                stopsViewModel.UpdateStopLabel(item);
                                confirmDialog.dismiss();
                            }
                        });
                        confirmDialog.show();
                        confirmDialog.setImageAction(R.mipmap.ic_undo);
                        break;
                    }
                }
                /*
                if(oldStatus.equals(LabelStatus.Scanned.getLabelStatusCode())) {
                    item.status = LabelStatus.NotScanned.getLabelStatusCode();
                    stopsViewModel.UpdateStopLabel(item);


                    Snackbar snackbar = Snackbar
                            .make(layout, "Item was updated as not scanned.", Snackbar.LENGTH_LONG);
                    snackbar.setAction("UNDO", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            //adapter.restoreItem(item, position);
                            item.status = oldStatus;
                            stopsViewModel.UpdateStopLabel(item);
                            labelsRecycler.scrollToPosition(position);
                        }
                    });

                    snackbar.setActionTextColor(Color.YELLOW);
                    snackbar.show();
                }
                else{
                    stopsViewModel.UpdateStopLabel(item);
                    Toast.makeText(StopLabelsFragment.this.getContext(),"Nothing to do", Toast.LENGTH_LONG).show();
                }

                 */
                //adapter.notifyItemChanged(position);
                enableDisableButtons();

            }
        };

        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchhelper.attachToRecyclerView(labelsRecycler);
    }

    private void setupRecyclerView() {
        adapter = new OrderLabelAdapter(this.getContext(), this);
        labelsRecycler.setAdapter(adapter);
        labelsRecycler.setLayoutManager(new LinearLayoutManager(this.getContext()));

    }

    @Override
    public void onStopLabelClicked(int position, final StopLabel stopLabel) {

        //If this was an empty label, ask the user to scan a label to fill in the empty label
        if(stopsViewModel.getOrderType().equals(OrderType.PICKUP) && (stopLabel.hawb == null || stopLabel.hawb.trim().equals(""))){
            //This is an empty label - need to allow the driver to scan in order to fill in the hawb
            clickedEmptyStopLabel = stopLabel;
            listening_to_empty_label = true;
            labelScanEmptyLabel.setVisibility(View.VISIBLE);
        }
        //if this was a delivery order, and the label was delivered, and thiss label does not have an assigned retour, allow manual retour
        else if(stopsViewModel.getOrderType().equals(OrderType.DELIVERY) && stopLabel.status.equals(LabelStatus.Delivered.getLabelStatusCode()) && !stopLabel.hasRetour){
            //This is an already scanned label - need to allow the driver to retour




            stopsViewModel.getSelectedStopLabel().setValue(stopLabel);

            //let's check first if this has been retoured yet
            if(stopLabel.deliveryReason != null && stopLabel.deliveryReason.equals("Retour"))
            {
                GlobalCoordinator.getInstance().notifyMessage(this.getContext(), "Cannot Retour Twice!",0);
                return;
            }

            String s = "Do you want to RETOUR the item?";
            final ConfirmDialog confirmDialog = new ConfirmDialog(StopLabelLabelsFragment.this.getContext(), s);
            confirmDialog.setConfirmDialogListener(new ConfirmDialog.ConfirmDialogListener() {
                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                @Override
                public void onConfirmClick(View v) {
                    confirmDialog.dismiss();
                    stopsViewModel.getFragmentToLoad().setValue(StopsViewModel.StopFragments.StopLabelRetourFragment);
                }

                @Override
                public void onCancelClick(View v) {
                    confirmDialog.dismiss();

                }
            });
            confirmDialog.show();
            confirmDialog.setImageAction(R.mipmap.ic_return);


        } //if this was manually filled to an empty label, and the status is still not scanned, allow to remove the label code
        else if(stopLabel.deliveryReason != null && stopLabel.deliveryReason.equals("Manual")){
            if(stopLabel.status.equals(LabelStatus.NotScanned.getLabelStatusCode()))// || stopLabel.status.equals(LabelStatus.NotScanned.getLabelStatusCode())){
            {

                String s = "Do you want to CLEAR the label?";
                final ConfirmDialog confirmDialog = new ConfirmDialog(StopLabelLabelsFragment.this.getContext(), s);
                confirmDialog.setConfirmDialogListener(new ConfirmDialog.ConfirmDialogListener() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onConfirmClick(View v) {
                        stopLabel.deliveryReason = "";
                        stopLabel.status = LabelStatus.NotScanned.getLabelStatusCode();
                        stopLabel.hawb = "";
                        stopsViewModel.UpdateStopLabel(stopLabel);

                        confirmDialog.dismiss();

                    }

                    @Override
                    public void onCancelClick(View v) {
                        confirmDialog.dismiss();

                    }
                });
                confirmDialog.show();
                confirmDialog.setImageAction(R.mipmap.ic_delete);


            }
        }

        else if(GlobalCoordinator.testMode) {
            //In Test Mode, simulate scanning the label aas there is no scanner in the emulator
            setLabelAsScanned(stopLabel.hawb);
        }
    }

    @Override
    public void onStopLabelRetour(int position, final StopLabel stopLabel) {

        //set this to be the selecteed label in order to send its ordr ID in th eretour fragment
        stopsViewModel.getSelectedStopLabel().setValue(stopLabel);

        if(!stopLabel.status.equals(LabelStatus.Delivered.getLabelStatusCode()))
        {
            //make sure the label is delivered before retour
            if(stopLabel.orderTypeId.equals(OrderType.DELIVERY.getOrderTypeId())) {
                GlobalCoordinator.getInstance().notifyMessage(this.getContext(), "Please Deliver the label before performing retou", 0);
            }else {
                GlobalCoordinator.getInstance().notifyMessage(this.getContext(), stopLabel.retourInstructions, 0);
            }
            return;
        }

        //let's check first if this has been retoured yet
        if(stopLabel.deliveryReason != null && stopLabel.retourDone)
        {
            GlobalCoordinator.getInstance().notifyMessage(this.getContext(), "Cannot Retour Twice!",0);
            return;
        }
        if(stopLabel.orderTypeId.equals(OrderType.DELIVERY.getOrderTypeId())) {
            stopsViewModel.getFragmentToLoad().setValue(StopsViewModel.StopFragments.StopLabelRetourFragment);
            return;
        }
        GlobalCoordinator.getInstance().notifyMessage(this.getContext(), stopLabel.retourInstructions,0, false);


    }

    @Override
    public void onStopLabelRefund(int position, final StopLabel stopLabel) {
        showRefundDetails(stopLabel);
    }

    @Override
    public void onSpecialInstructionsFound(int position, StopLabel stop){

        if(stop.status.equals(LabelStatus.NotScanned.getLabelStatusCode()))
            GlobalCoordinator.getInstance().notifyMessage(this.getContext(), stop.specialInstructions,0,false);

    }

    @Override
    public void onRetourInstructions(int position, StopLabel stop){

        if(stop.hasRetour && stop.retourInstructions != null && !stop.retourInstructions.equals(""))
            if(stop.status.equals(LabelStatus.Delivered.getLabelStatusCode()))
                GlobalCoordinator.getInstance().notifyMessage(this.getContext(), stop.specialInstructions,0,false);

    }

    private void showRefundDetails(StopLabel stopLabel){


        String text = getContext().getString(R.string.refundTextFormat, stopLabel.refundAmount, stopLabel.refundAmountLBP, GlobalCoordinator.getInstance().settingsCountryCurrency.toUpperCase());

        GlobalCoordinator.getInstance().notifyMessage(this.getContext(), text,0, false);

    }


        StopLabel templateStopLabel = null;
    public void setLabelAsScanned( String hawb) {

        if(hawb == null || hawb.equals("")) return;
        hawb = hawb.trim();
        boolean found = false;
        StopLabel currentEmptyStopLabel = null;
        for (StopLabel sl :
                stopsViewModel.getSelectedStopContact().getValue().stopLabelLiveData.getValue()) {

            if((sl.hawb != null && sl.hawb.toLowerCase().equals(hawb.toLowerCase()))
                    || (sl.shipperReference != null && sl.shipperReference.toLowerCase().equals(hawb.toLowerCase()))) {
                /*
                if(sl.status.equals(LabelStatus.NotScanned.getLabelStatusCode()) || sl.status.equals(LabelStatus.NotDelivered.getLabelStatusCode())) {
                    //Toast.makeText(StopLabelsFragment.this.getContext(), hawb, Toast.LENGTH_LONG).show();
                    sl.status = LabelStatus.Scanned.getLabelStatusCode();
                    stopsViewModel.UpdateStopLabel(sl);
                    handleStopLabelScanned(sl);
                }

                 */
                found = true;

                handleStopLabelScanned(sl);

            }
            if((sl.hawb == null || sl.hawb.equals("")) && sl.status.equals(LabelStatus.NotScanned.getLabelStatusCode()))
                currentEmptyStopLabel = sl;
            else if(templateStopLabel == null)
                templateStopLabel = sl;
        }
        if(!found){


            if(stopsViewModel.getOrderType().equals(OrderType.DELIVERY))
                GlobalCoordinator.getInstance().notifyMessage(StopLabelLabelsFragment.this.getContext(), "ERROR: ITEM NOT IN CURRENT ORDER", 0);
            if(stopsViewModel.getOrderType().equals(OrderType.PICKUP))
            {



                if(listening_to_empty_label){
                    clickedEmptyStopLabel.hawb = hawb;
                    clickedEmptyStopLabel.deliveryReason = "Manual";
                    clickedEmptyStopLabel.status = LabelStatus.Scanned.getLabelStatusCode();
                    stopsViewModel.UpdateStopLabel(clickedEmptyStopLabel);
                    labelScanEmptyLabel.setVisibility(View.GONE);
                    listening_to_empty_label = false;
                    return;
                }


                if(currentEmptyStopLabel != null){
                    currentEmptyStopLabel.hawb = hawb;
                    currentEmptyStopLabel.deliveryReason = "Manual";
                    currentEmptyStopLabel.status = LabelStatus.Scanned.getLabelStatusCode();
                    stopsViewModel.UpdateStopLabel(currentEmptyStopLabel);
                    labelScanEmptyLabel.setVisibility(View.GONE);
                    listening_to_empty_label = false;
                    //Added: when the label is updated with the enew hawb, set it back to null in order not to be updated/overwritten
                    currentEmptyStopLabel = null;
                    return;
                }

                if(templateStopLabel != null){
                    confirmAddNewLabel(hawb);
                        /*
                        currentFirstStopLabel.hawb = hawb;
                        currentFirstStopLabel.deliveryReason = "Added";
                        currentEmptyStopLabel.status = LabelStatus.Scanned.getLabelStatusCode();
                        stopsViewModel.UpdateStopLabel(currentEmptyStopLabel);
                        labelScanEmptyLabel.setVisibility(View.GONE);
                        listening_to_empty_label = false;

                         */
                    return;
                }

                if(templateStopLabel == null && clickedEmptyStopLabel == null) {
                    //look if there were an empty label first




                    //if there was no empty label, and no previously scanned, select any label


                    GlobalCoordinator.getInstance().notifyMessage(StopLabelLabelsFragment.this.getContext(), "Please scan at least one label before adding a new one!", 0);
                    return;
                }


                confirmAddNewLabel(hawb);
            }
        }
        enableDisableButtons();

    }

    private void confirmAddNewLabel(final String hawb) {
        String s = "This item does not exist in the order. Are you sure you want to add it?";
        final ConfirmDialog confirmDialog = new ConfirmDialog(StopLabelLabelsFragment.this.getContext(), s);
        confirmDialog.setConfirmDialogListener(new ConfirmDialog.ConfirmDialogListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onConfirmClick(View v) {
                confirmDialog.dismiss();
                addNewLabel(hawb);
            }

            @Override
            public void onCancelClick(View v) {
                confirmDialog.dismiss();

            }
        });
        confirmDialog.show();
    }


    private void addNewLabel(String hawb) {
        //Add the label to the order
        StopLabel label = new StopLabel();
        label.codLBP = 0;
        label.codUSD = 0;


        label.hawb = hawb;
        label.numberOfPieces = 1;
        label.orderId = "";
        label.deliveryReason = "Added";
        label.specialInstructions = "";
        label.hasRetour = false;
        label.hasRefund = false;
        label.refundAmountLBP = 0.0;
        label.refundAmount = 0.0;
        label.retourInstructions = "";
        label.fromTime = "";
        label.toTime = "";
        label.consigneeName = "";
        label.consigneeCity = "";
        label.refundOrderId = "";
        label.retourDone = false;

        if(templateStopLabel != null)
        {
            label.contactId = templateStopLabel.contactId;
            label.orderTypeId = templateStopLabel.orderTypeId;// OrderType.DELIVERY.getOrderTypeId();
            label.orderId = templateStopLabel.orderId;
            label.shipmentType = templateStopLabel.shipmentType;
            label.stopId = templateStopLabel.stopId;
        }
        else if(clickedEmptyStopLabel != null){
            label.contactId = clickedEmptyStopLabel.contactId;
            label.orderTypeId = clickedEmptyStopLabel.orderTypeId;// OrderType.DELIVERY.getOrderTypeId();
            label.orderId = clickedEmptyStopLabel.orderId;
            label.shipmentType = clickedEmptyStopLabel.shipmentType;
            label.stopId = clickedEmptyStopLabel.stopId;
        }

        label.status = LabelStatus.Scanned.getLabelStatusCode();
        //label.isScanned = false;
        stopsViewModel.insertStopLabel(label);
    }

    private void handleStopLabelScanned(StopLabel sl) {
        templateStopLabel = sl;
        switch (LabelStatus.get(sl.status)){
            case Scanned:{
                GlobalCoordinator.getInstance().notifyMessage(StopLabelLabelsFragment.this.getContext(), "Item Already Scanned!!", 2);
                break;
            }
            case Delivered:{
                GlobalCoordinator.getInstance().notifyMessage(StopLabelLabelsFragment.this.getContext(), "Item Already Delivered!", 2);
                break;
            }
            case NotDelivered:{
                GlobalCoordinator.getInstance().notifyMessage(StopLabelLabelsFragment.this.getContext(), "ERROR: ITEM SET AS DELIVERY FAILURE!!!", 0);
                break;
            }
            case NotScanned:{
                GlobalCoordinator.getInstance().beepScanned(StopLabelLabelsFragment.this.getContext());
                checkShipmentType(sl);
                /*
                sl.status = LabelStatus.Scanned.getLabelStatusCode();
                stopsViewModel.UpdateStopLabel(sl);

                 */
                break;
            }
        }

    }
    private StopLabel netLockerStopLabel = null;

    private void checkShipmentType(final StopLabel sl) {
        switch (ShipmentType.get(sl.shipmentType)){
            case NetPoint:{

                if(GlobalCoordinator.getInstance().settingsDisableDeliveryNFC || buildingScanned.getValue()){
                    sl.status = LabelStatus.Scanned.getLabelStatusCode();
                    stopsViewModel.UpdateStopLabel(sl);
                }else {
                    GlobalCoordinator.getInstance().notifyMessage(StopLabelLabelsFragment.this.getContext(), "NetPoint Job - PLEASE SCAN BUILDING", 0);
                }


                break;
            }
            case NetLocker:{
                //Need to display the Net Locker
                //stopsViewModel.getNetLocker(sl.orderId);
                netLockerStopLabel = sl;
                stopsViewModel.getmDriverServices().getNetLocker(sl.orderId);
                stopLabelsProgress.setVisibility(View.VISIBLE);


            }
            default:{
                sl.status = LabelStatus.Scanned.getLabelStatusCode();
                stopsViewModel.UpdateStopLabel(sl);
            }
        }
    }

    /*
    private void alert(String message) {
        final ConfirmDialog confirmDialog = new ConfirmDialog(StopLabelsFragment.this.getContext(), message);
        confirmDialog.setConfirmDialogListener(new ConfirmDialog.ConfirmDialogListener() {
            @Override
            public void onConfirmClick(View v) {

                confirmDialog.dismiss();
            }

            @Override
            public void onCancelClick(View v) {
                confirmDialog.dismiss();

            }
        });
        confirmDialog.show();
        confirmDialog.setImageAction(R.mipmap.handstop);

        // Hide after some seconds
        final Handler handler  = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (confirmDialog.isShowing()) {
                    confirmDialog.dismiss();
                }
            }
        };

        confirmDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                handler.removeCallbacks(runnable);
            }
        });

        handler.postDelayed(runnable, 2000);
    }


     */
    private void enableDisableButtons() {
        boolean hasScannedLabels = false;
        boolean hasNotScannedLabels = false;
        if(stopsViewModel.getSelectedStopContact().getValue().stopLabelLiveData.getValue() != null)
        for (StopLabel sl :
                stopsViewModel.getSelectedStopContact().getValue().stopLabelLiveData.getValue()) {
            if(sl.status.equals(LabelStatus.NotScanned.getLabelStatusCode())) hasNotScannedLabels = true;
            if(sl.status.equals(LabelStatus.Scanned.getLabelStatusCode())) hasScannedLabels = true;
            /*
            if(sl.status != null && sl.status.equals(LabelStatus.Scanned.getLabelStatusCode()))
                labelsSSendDeliveryImage.setVisibility(View.VISIBLE);
            if(sl.status != null && sl.status.equals(LabelStatus.NotScanned.getLabelStatusCode()))
                labelsSSendNonDeliveryImage.setVisibility(View.VISIBLE);

             */
        }
        if(hasScannedLabels) labelsSSendDeliveryImage.setVisibility(View.VISIBLE); else labelsSSendDeliveryImage.setVisibility(View.INVISIBLE);
        //TODO: Remove the below when confirmed with client
        if(hasNotScannedLabels) labelsSSendNonDeliveryImage.setVisibility(View.INVISIBLE); else labelsSSendNonDeliveryImage.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onNewScan(String characters) {
        setLabelAsScanned(characters);
    }

    @Override
    public void onNewIntent(Intent intent) {

        readFromIntent(intent);

        stopsViewModel.getmDriverServices().scanBuildingNFC(mBuildingCode);


    }

    @Override
    public void onNewLocation(DriverLocation driverLocation) {

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

        }
        mBuildingCode = GlobalCoordinator.getInstance().getBuildingCode(mBuildingCode);

        tvNFCContent.setText("Scanned Code: " + mBuildingCode);
        tvNFCContent.setTextColor(getResources().getColor(R.color.colorPrimary));
        tvNFCContent.setVisibility(View.VISIBLE);
    }
}
