package lb.com.thenet.netdriver.ui.delivery;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import java.util.ArrayList;
import java.util.List;

import lb.com.thenet.netdriver.GlobalCoordinator;
import lb.com.thenet.netdriver.MainActivity;
import lb.com.thenet.netdriver.R;
import lb.com.thenet.netdriver.dataholders.DriverLocation;
import lb.com.thenet.netdriver.onlineservices.DriverServicesError;
import lb.com.thenet.netdriver.onlineservices.ServicesErrorType;
import lb.com.thenet.netdriver.onlineservices.json.ResponseMessage;
import lb.com.thenet.netdriver.rooms.entities.DriverRepository;
import lb.com.thenet.netdriver.rooms.entities.enums.StopStatus;
import lb.com.thenet.netdriver.rooms.entities.lov.ActionRoute;

public class StopRejectFragment extends Fragment implements MainActivity.OnScanListener {

    private StopsViewModel stopsViewModel;
    private Fragment hostingFragment;
    private Spinner rejectStopActionsSpinner;
    private TextView rejectStopLabel;
    private ProgressBar rejectStopProgress;
    private Button rejectStopBackButton;
    private Button rejectStopSendButton;

    ArrayList<String> routesList;
    ArrayList<String> routesIdList;


    public StopRejectFragment(){

    }

    public StopRejectFragment(Fragment hostingFragment){
        this.hostingFragment = hostingFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_stop_delivery_failure, container, false);

        // Inflate the layout for this fragment
        stopsViewModel = ViewModelProviders.of(hostingFragment).get(StopsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_stop_reject, container, false);

        findAndAssignTextAndActions(root);

        observeServerResponses();

        ((MainActivity)this.getActivity()).updateDriverLocation();
        return root;
    }

    private void showError(String errorMessage){
        Toast.makeText(StopRejectFragment.this.getContext(), errorMessage, Toast.LENGTH_LONG).show();
    }

    private void observeServerResponses() {

        stopsViewModel.getmRejectStopResponse().observe(this, new Observer<ResponseMessage<Boolean>>() {
            @Override
            public void onChanged(ResponseMessage<Boolean> booleanResponseMessage) {
                if(booleanResponseMessage == null) return;
                rejectStopProgress.setVisibility(View.GONE);
                if(booleanResponseMessage.success){
                    stopsViewModel.getSelectedStop().getValue().status = StopStatus.Rejected.getStopStatusCode();
                }else {
                    showError(booleanResponseMessage.errorMessage);
                }
                stopsViewModel.UpdateStop(stopsViewModel.getSelectedStop().getValue());
                stopsViewModel.getmRejectStopResponse().setValue(null);
                if(booleanResponseMessage.success)
                    ((OnStopsFragmentsInteraction) hostingFragment).backToPreviousFragment(false);
            }
        });

        this.stopsViewModel.getmError().observe(this, new Observer<DriverServicesError>() {
            @Override
            public void onChanged(DriverServicesError driverServicesError) {
                if(driverServicesError == null) return;;
                rejectStopProgress.setVisibility(View.GONE);
                if(driverServicesError.errorType.equals(ServicesErrorType.rejectStopError))
                    showError(driverServicesError.volleyError.toString());
                stopsViewModel.getmError().setValue(null);

            }
        });
    }

    private void findAndAssignTextAndActions(View root) {

        routesList = new ArrayList<>();
        routesIdList = new ArrayList<>();
        rejectStopActionsSpinner = root.findViewById(R.id.rejectStopActionsSpinner);
        rejectStopProgress = root.findViewById(R.id.rejectStopProgress);
        rejectStopLabel = root.findViewById(R.id.rejectStopLabel);
        rejectStopBackButton = root.findViewById(R.id.rejectStopBackButton);
        rejectStopSendButton = root.findViewById(R.id.rejectStopSendButton);

        rejectStopLabel.setText(getContext().getString(R.string.rejectStopReasonFormat, stopsViewModel.getSelectedStop().getValue().clientName));

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.action_route_spinner_item, routesList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        rejectStopActionsSpinner.setAdapter(adapter);

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


        rejectStopBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((OnStopsFragmentsInteraction) hostingFragment).backToPreviousFragment(false);
                //stopsViewModel.getFragmentToLoad().setValue(StopsViewModel.StopFragments.StopLabelsFragment);
            }
        });

        rejectStopSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rejectStopProgress.setVisibility(View.VISIBLE);
                String nfcBuilding = DriverRepository.scannedLocation.getValue() != null ? DriverRepository.scannedLocation.getValue().getLocationCode() : "";
                double longitude = stopsViewModel.mDriverLocation.getValue().longitude;
                double latitude = stopsViewModel.mDriverLocation.getValue().latitude;
                String activityDate = GlobalCoordinator.getNowFormatted();
                String stopId = stopsViewModel.getSelectedStop().getValue().stopId;
                String actionRouteId = routesIdList.get(rejectStopActionsSpinner.getSelectedItemPosition());
                stopsViewModel.rejectStop(longitude,latitude, actionRouteId);
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
