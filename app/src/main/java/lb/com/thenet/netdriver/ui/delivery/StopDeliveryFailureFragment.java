package lb.com.thenet.netdriver.ui.delivery;


import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import lb.com.thenet.netdriver.GlobalCoordinator;
import lb.com.thenet.netdriver.MainActivity;
import lb.com.thenet.netdriver.R;
import lb.com.thenet.netdriver.dataholders.DriverLocation;
import lb.com.thenet.netdriver.onlineservices.DriverServicesError;
import lb.com.thenet.netdriver.onlineservices.ServicesErrorType;
import lb.com.thenet.netdriver.onlineservices.json.OrderType;
import lb.com.thenet.netdriver.onlineservices.json.ResponseMessage;
import lb.com.thenet.netdriver.rooms.entities.DriverRepository;
import lb.com.thenet.netdriver.rooms.entities.enums.LabelStatus;
import lb.com.thenet.netdriver.rooms.entities.lov.ActionRoute;
import lb.com.thenet.netdriver.rooms.entities.stops.StopLabel;

/**
 * A simple {@link Fragment} subclass.
 */
public class StopDeliveryFailureFragment extends Fragment implements MainActivity.OnScanListener {

    private StopsViewModel stopsViewModel;
    private Fragment hostingFragment;
    private Spinner nonDeliveryActionsSpinner;
    private TextView sendNonDeliveryLabel;
    private ProgressBar sendNonDeliveryProgress;
    private Button nonDeliveryBackButton;
    private Button nonDeliverySendButton;


    public StopDeliveryFailureFragment() {
        // Required empty public constructor
    }

    public StopDeliveryFailureFragment(Fragment hostingFragment){
        this.hostingFragment = hostingFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_stop_delivery_failure, container, false);

        // Inflate the layout for this fragment
        stopsViewModel = ViewModelProviders.of(hostingFragment).get(StopsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_stop_delivery_failure, container, false);

        findAndAssignTextAndActions(root);

        observeServerResponses();
        ((MainActivity)getActivity()).updateDriverLocation();
        return root;
    }

    private void observeServerResponses() {
        stopsViewModel.getmSendNonDeliveryResponse().observe(this, new Observer<ResponseMessage<Boolean>>() {
            @Override
            public void onChanged(ResponseMessage<Boolean> booleanResponseMessage) {
                if(booleanResponseMessage == null) return;
                handleSendResponse(booleanResponseMessage);
                stopsViewModel.getmSendNonDeliveryResponse().setValue(null);

            }
        });

        stopsViewModel.getmSendNonPickupResponse().observe(this, new Observer<ResponseMessage<Boolean>>() {
            @Override
            public void onChanged(ResponseMessage<Boolean> booleanResponseMessage) {
                if(booleanResponseMessage == null) return;
                handleSendResponse(booleanResponseMessage);
                stopsViewModel.getmSendNonPickupResponse().setValue(null);
            }
        });

        this.stopsViewModel.getmError().observe(this, new Observer<DriverServicesError>() {
            @Override
            public void onChanged(DriverServicesError driverServicesError) {
                if(driverServicesError == null) return;;
                sendNonDeliveryProgress.setVisibility(View.GONE);
                if(driverServicesError.errorType.equals(ServicesErrorType.generalError))
                    showError(driverServicesError.volleyError.toString());
                else
                    showError(driverServicesError.errorMessage);
                stopsViewModel.getmError().setValue(null);

            }
        });

    }

    private void handleSendResponse(ResponseMessage<Boolean> booleanResponseMessage) {
        sendNonDeliveryProgress.setVisibility(View.GONE);
        if(booleanResponseMessage.success){
            StopLabel stopLabel = stopsViewModel.getSelectedStopLabel().getValue();
            stopLabel.status = LabelStatus.NotDelivered.getLabelStatusCode();
            stopsViewModel.UpdateStopLabel(stopLabel);
            ((OnStopsFragmentsInteraction) hostingFragment).backToPreviousFragment(false);

            //stopsViewModel.getFragmentToLoad().setValue(StopsViewModel.StopFragments.StopLabelsFragment);
        }else {
            showError(booleanResponseMessage.errorMessage + " " + booleanResponseMessage.message);
        }
    }

    private void showError(String errorMessage){
        Toast.makeText(StopDeliveryFailureFragment.this.getContext(), errorMessage, Toast.LENGTH_LONG).show();
    }

    ArrayList<String> routesList;
    ArrayList<String> routesIdList;

    private void findAndAssignTextAndActions(View root) {

        routesList = new ArrayList<>();
        routesIdList = new ArrayList<>();
        nonDeliveryActionsSpinner = root.findViewById(R.id.nonDeliveryActionsSpinner);
        sendNonDeliveryProgress = root.findViewById(R.id.sendNonDeliveryProgress);
        sendNonDeliveryLabel = root.findViewById(R.id.sendNonDeliveryLabel);
        nonDeliveryBackButton = root.findViewById(R.id.nonDeliveryBackButton);
        nonDeliverySendButton = root.findViewById(R.id.nonDeliverySendButton);

        if(stopsViewModel.getOrderType().equals(OrderType.DELIVERY))
            sendNonDeliveryLabel.setText(getContext().getString(R.string.nonDeliveryReasonFormat, stopsViewModel.getSelectedStopLabel().getValue().hawb));
        if(stopsViewModel.getOrderType().equals(OrderType.PICKUP))
            sendNonDeliveryLabel.setText(getContext().getString(R.string.nonPickupReasonFormat, stopsViewModel.getSelectedStopLabel().getValue().hawb));

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.action_route_spinner_item, routesList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        nonDeliveryActionsSpinner.setAdapter(adapter);

        switch (stopsViewModel.getOrderType()){
            case DELIVERY:{
                stopsViewModel.getmNonDeliveryActionRoutes().observe(this, new Observer<List<ActionRoute>>() {
                    @Override
                    public void onChanged(List<ActionRoute> actionRoutes) {
                        for (ActionRoute route :
                                actionRoutes) {
                            routesList.add(route.actionRouteName);
                            routesIdList.add(route.actionRouteId);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
                break;
            }
            case PICKUP:{
                stopsViewModel.getmNonPickupActionRoutes().observe(this, new Observer<List<ActionRoute>>() {
                    @Override
                    public void onChanged(List<ActionRoute> actionRoutes) {
                        for (ActionRoute route :
                                actionRoutes) {
                            routesList.add(route.actionRouteName);
                            routesIdList.add(route.actionRouteId);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
                break;
            }
        }


        nonDeliveryBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((OnStopsFragmentsInteraction) hostingFragment).backToPreviousFragment(false);
                //stopsViewModel.getFragmentToLoad().setValue(StopsViewModel.StopFragments.StopLabelsFragment);
            }
        });

        nonDeliverySendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendNonDeliveryProgress.setVisibility(View.VISIBLE);
                String nfcBuilding = DriverRepository.scannedLocation.getValue() != null ? DriverRepository.scannedLocation.getValue().getLocationCode() : "";
                double longitude = stopsViewModel.mDriverLocation.getValue().longitude;
                double latitude = stopsViewModel.mDriverLocation.getValue().latitude;
                String activityDate = GlobalCoordinator.getNowFormatted();
                String hawb = stopsViewModel.getSelectedStopLabel().getValue().hawb;
                String actionRouteId = routesIdList.get(nonDeliveryActionsSpinner.getSelectedItemPosition());
                String orderId = stopsViewModel.getSelectedStopLabel().getValue().orderId;
                stopsViewModel.sendNonDelivery(nfcBuilding,longitude,latitude, activityDate, hawb, actionRouteId, orderId);
            }
        });
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
}
