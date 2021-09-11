package lb.com.thenet.netdriver.ui.delivery;


import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import lb.com.thenet.netdriver.GlobalCoordinator;
import lb.com.thenet.netdriver.MainActivity;
import lb.com.thenet.netdriver.R;
import lb.com.thenet.netdriver.dataholders.DriverLocation;
import lb.com.thenet.netdriver.onlineservices.DriverServicesError;
import lb.com.thenet.netdriver.onlineservices.ServicesErrorType;
import lb.com.thenet.netdriver.onlineservices.json.OrderType;
import lb.com.thenet.netdriver.onlineservices.json.ResponseMessage;
import lb.com.thenet.netdriver.rooms.entities.enums.StopStatus;
import lb.com.thenet.netdriver.rooms.entities.stops.Stop;
import lb.com.thenet.netdriver.ui.ConfirmDialog;

/**
 * A simple {@link Fragment} subclass.
 */
public class StopDetailsFragment extends Fragment implements MainActivity.OnScanListener, OnFragmentUnhide {


    private StopsViewModel stopsViewModel;
    private Fragment hostingFragment;

    private TextView tvClientName;
    private TextView tvAddressCity;
    private TextView tvAddressStreet;
    private TextView tvNumberOfLabels;
    private TextView tvNumberOfContacts;
    private Button btnBack;
    private Button btnStart;
    private Button btnStop;
    private TextView btnDetails;
    private ProgressBar startStopProgress;


    public StopDetailsFragment() {
        // Required empty public constructor
    }

    public StopDetailsFragment(Fragment hostingFragment){
        this.hostingFragment = hostingFragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        stopsViewModel = ViewModelProviders.of(hostingFragment).get(StopsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_stop_details, container, false);

        findAndAssignTextAndActions(root);

        observeServerResponses();
        setViewVisibility();

        ((MainActivity)getActivity()).updateDriverLocation();
        return root;

    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        //this doesnt geet called :( //setViewVisibility();
    }

    private void observeServerResponses() {
        stopsViewModel.getmStartStopResponse().observe(getViewLifecycleOwner(), new Observer<ResponseMessage<Boolean>>() {
            @Override
            public void onChanged(ResponseMessage<Boolean> booleanResponseMessage) {
                if(booleanResponseMessage == null) return;
                startStopProgress.setVisibility(View.GONE);
                if(booleanResponseMessage.success){
                    stopsViewModel.getmStartStopResponse().setValue(null);
                    stopsViewModel.getSelectedStop().getValue().status = StopStatus.Started.getStopStatusCode();
                    stopsViewModel.UpdateStop(stopsViewModel.getSelectedStop().getValue());
                    setViewVisibility();
                    loadStopContacts();
                }else{
                    Toast.makeText(StopDetailsFragment.this.getContext(), booleanResponseMessage.errorMessage, Toast.LENGTH_LONG).show();
                }
            }
        });
        stopsViewModel.getmStopStopResponse().observe(getViewLifecycleOwner(), new Observer<ResponseMessage<Boolean>>() {
            @Override
            public void onChanged(ResponseMessage<Boolean> booleanResponseMessage) {
                if(booleanResponseMessage == null) return;
                startStopProgress.setVisibility(View.GONE);
                if(booleanResponseMessage.success){
                    stopsViewModel.getmStopStopResponse().setValue(null);
                    if(stopsViewModel.getOrderType().equals(OrderType.DELIVERY))
                        stopsViewModel.getSelectedStop().getValue().status = StopStatus.NotStarted.getStopStatusCode();
                    if(stopsViewModel.getOrderType().equals(OrderType.PICKUP))
                        stopsViewModel.getSelectedStop().getValue().status = StopStatus.Accepted.getStopStatusCode();
                    stopsViewModel.UpdateStop(stopsViewModel.getSelectedStop().getValue());
                    setViewVisibility();
                    backToPrevious();
                }else{
                    Toast.makeText(StopDetailsFragment.this.getContext(), booleanResponseMessage.errorMessage, Toast.LENGTH_LONG).show();
                }
            }
        });
        stopsViewModel.getmError().observe(getViewLifecycleOwner(), new Observer<DriverServicesError>() {
            @Override
            public void onChanged(DriverServicesError driverServicesError) {
                if(driverServicesError == null) return;
                startStopProgress.setVisibility(View.GONE);
                if(driverServicesError.errorType.equals(ServicesErrorType.generalError))
                    Toast.makeText(StopDetailsFragment.this.getContext(), driverServicesError.volleyError.toString(), Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(StopDetailsFragment.this.getContext(), driverServicesError.errorMessage, Toast.LENGTH_LONG).show();

                stopsViewModel.getmError().setValue(null);
            }
        });
    }

    private void loadStopContacts() {
        stopsViewModel.getFragmentToLoad().setValue(StopsViewModel.StopFragments.StopContactsFragment);

    }

    private void setViewVisibility(){
        switch (StopStatus.get(stopsViewModel.getSelectedStop().getValue().status)){
            case EMPTY: case NotStarted: {

                btnStart.setVisibility(View.VISIBLE);

                if(stopsViewModel.getOrderType().equals(OrderType.PICKUP))
                    btnStart.setVisibility(View.GONE);
                btnStop.setVisibility(View.GONE);
                btnDetails.setVisibility(View.GONE);
                break;
            }
            case Started: {
                btnStart.setVisibility(View.GONE);
                btnStop.setVisibility(View.VISIBLE);
                btnDetails.setVisibility(View.VISIBLE);
                break;
            }
            case Completed: {
                btnStart.setVisibility(View.GONE);
                btnStop.setVisibility(View.GONE);
                btnDetails.setVisibility(View.VISIBLE);
                break;
            }
            case Accepted:{
                btnStart.setVisibility(View.VISIBLE);
                btnStart.setText(getContext().getString(R.string.pick_up));
                btnStop.setVisibility(View.GONE);
                btnDetails.setVisibility(View.GONE);
                break;
            }
            case Rejected: {
                btnStart.setVisibility(View.GONE);
                btnStop.setVisibility(View.GONE);
                btnDetails.setVisibility(View.GONE);
                break;
            }

        }

        Stop refreshedStop = stopsViewModel.getStopById(stopsViewModel.getSelectedStop().getValue().stopId);
        if(refreshedStop == null) return;

        if(refreshedStop.numberOfInCompleteLabels == 0 || refreshedStop.numberOfCompleteLabels > 0){
            btnStart.setVisibility(View.GONE);
            btnStop.setVisibility(View.GONE);
        }

    }

    private void findAndAssignTextAndActions(View root) {
        Stop currentStop = stopsViewModel.getSelectedStop().getValue();

        tvAddressCity = root.findViewById(R.id.detAddressCity);
        tvAddressStreet = root.findViewById(R.id.detAddressStreet);
        tvClientName = root.findViewById(R.id.detClientName);
        tvNumberOfContacts = root.findViewById(R.id.detNumberOfContacts);
        tvNumberOfLabels = root.findViewById(R.id.detNumberOfLabels);
        btnBack = root.findViewById(R.id.detBackButton);
        btnStart = root.findViewById(R.id.detStartButton);
        btnStop = root.findViewById(R.id.detStopButton);
        btnDetails = root.findViewById(R.id.detDetailsButton);
        startStopProgress = root.findViewById(R.id.startStopProgress);

        tvAddressCity.setText(currentStop.addressCity);


        tvAddressStreet.setText(getContext().getString(R.string.fullAddressFormat, currentStop.addressStreet, currentStop.addressBuilding, currentStop.addressFloor, currentStop.addressLandmark));
        tvClientName.setText(getResources().getString(R.string.clientNameFormat,currentStop.clientName,currentStop.numberOfLabels));

        tvNumberOfContacts.setText(getResources().getQuantityString(R.plurals.numberOfContactsFormat, currentStop.numberOfContacts, currentStop.numberOfContacts));
        tvNumberOfLabels.setText(getResources().getQuantityString(R.plurals.numberOfLabelsFormat, currentStop.numberOfLabels, currentStop.numberOfLabels));

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(stopsViewModel.getSelectedStop().getValue().status.equals(StopStatus.Completed) ||
                        stopsViewModel.getSelectedStop().getValue().status.equals(StopStatus.Started)) {
                    String msg = "This has already started!";
                    final ConfirmDialog stopDialog = new ConfirmDialog(StopDetailsFragment.this.getContext(), msg);
                    stopDialog.setConfirmDialogListener(new ConfirmDialog.ConfirmDialogListener() {
                        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                        @Override
                        public void onConfirmClick(View v) {
                            stopDialog.dismiss();
                            backToPrevious();
                        }

                        @Override
                        public void onCancelClick(View v) {
                            stopDialog.dismiss();
                            backToPrevious();
                        }
                    });
                    stopDialog.show();
                    return;
                }

                String s = "Are you sure you want to start this Delivery?";
                if(stopsViewModel.getOrderType().equals(OrderType.PICKUP))
                    s = "Are you sure you want to start this Pickup?";
                final ConfirmDialog confirmDialog = new ConfirmDialog(StopDetailsFragment.this.getContext(), s);
                confirmDialog.setConfirmDialogListener(new ConfirmDialog.ConfirmDialogListener() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onConfirmClick(View v) {
                        confirmDialog.dismiss();
                        startStop();
                    }

                    @Override
                    public void onCancelClick(View v) {
                        confirmDialog.dismiss();

                    }
                });
                confirmDialog.show();

            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s = "Are you sure you want to STOP this Delivery?";
                if(stopsViewModel.getOrderType().equals(OrderType.PICKUP))
                    s = "Are you sure you want to STOP this Pickup?";
                final ConfirmDialog confirmDialog = new ConfirmDialog(StopDetailsFragment.this.getContext(), s);
                confirmDialog.setConfirmDialogListener(new ConfirmDialog.ConfirmDialogListener() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onConfirmClick(View v) {
                        confirmDialog.dismiss();
                        stopStop("");
                    }

                    @Override
                    public void onCancelClick(View v) {
                        confirmDialog.dismiss();

                    }
                });
                confirmDialog.show();
            }
        });


        btnDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadStopContacts();
            }
        });
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backToPrevious();
            }

        });

    }

    private void startStop() {
        switch (StopStatus.get(stopsViewModel.getSelectedStop().getValue().status)){
            case Started:
            case Completed: {
                loadStopContacts();
                break;
            }
            case Rejected:{
                Toast.makeText(StopDetailsFragment.this.getContext(),"You already Rejected this Order!", Toast.LENGTH_LONG);
                break;
            }
            case NotStarted:
                case EMPTY:
            default:{
                if(stopsViewModel.canStartStop()) {
                    startStopProgress.setVisibility(View.VISIBLE);
                    stopsViewModel.startStop(stopsViewModel.getmDriverLocation().getValue().longitude, stopsViewModel.getmDriverLocation().getValue().latitude);
                }else{
                    GlobalCoordinator.getInstance().notifyMessage(StopDetailsFragment.this.getContext(),"Cannot Start! You have Exceeded the Number of Started Orders", 0);
                }
            }
        }
    }

    private void stopStop(String reason){
        switch (StopStatus.get(stopsViewModel.getSelectedStop().getValue().status)){
            case Started:
            case Completed: {
                startStopProgress.setVisibility(View.VISIBLE);
                stopsViewModel.stopStop(stopsViewModel.getmDriverLocation().getValue().longitude, stopsViewModel.getmDriverLocation().getValue().latitude, reason);
                break;
            }
            case Rejected:{
                Toast.makeText(StopDetailsFragment.this.getContext(),"You already Rejected this Order!", Toast.LENGTH_LONG);
                break;
            }
            case NotStarted:
            case EMPTY:
            default:{
                Toast.makeText(StopDetailsFragment.this.getContext(),"This is not a started Order!", Toast.LENGTH_LONG);
            }
        }
    }

    private void backToPrevious() {
        startStopProgress.setVisibility(View.GONE);
        ((OnStopsFragmentsInteraction) hostingFragment).backToPreviousFragment(false);
        //stopsViewModel.getFragmentToLoad().setValue(StopsViewModel.StopFragments.StopsFragment);
    }

    @Override
    public void onNewScan(String characters) {

    }

    @Override
    public void onNewIntent(Intent intent) {

    }

    @Override
    public void onNewLocation(DriverLocation driverLocation) {
        stopsViewModel.getmDriverLocation().setValue(driverLocation);
    }

    @Override
    public void fragmentUnHidden() {
        setViewVisibility();
    }
}
