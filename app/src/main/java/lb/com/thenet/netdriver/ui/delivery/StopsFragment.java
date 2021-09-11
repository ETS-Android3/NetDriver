package lb.com.thenet.netdriver.ui.delivery;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import lb.com.thenet.netdriver.GlobalCoordinator;
import lb.com.thenet.netdriver.MainActivity;
import lb.com.thenet.netdriver.R;
import lb.com.thenet.netdriver.SwipeToAcceptRejectCallback;
import lb.com.thenet.netdriver.adapters.OrderAdapter;
import lb.com.thenet.netdriver.dataholders.DriverLocation;
import lb.com.thenet.netdriver.onlineservices.DriverServicesError;
import lb.com.thenet.netdriver.onlineservices.ServicesErrorType;
import lb.com.thenet.netdriver.onlineservices.json.JsonStop;
import lb.com.thenet.netdriver.onlineservices.json.OrderType;
import lb.com.thenet.netdriver.onlineservices.json.ResponseMessage;
import lb.com.thenet.netdriver.rooms.entities.enums.StopStatus;
import lb.com.thenet.netdriver.rooms.entities.stops.Stop;
import lb.com.thenet.netdriver.ui.ManualEntryDialog;

/**
 * A simple {@link Fragment} subclass.
 */
public class StopsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, OrderAdapter.OnStopClickedListener, MainActivity.OnScanListener {
    private StopsViewModel stopsViewModel;
    private Fragment hostingFragment;

    private TextView textCountAll;
    private TextView textCountDone;
    private TextView textCountPending;
    private TextView textCountInCompleteLabels;
    private TextView textInCompleteLabels;
    private OrderAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private FloatingActionButton fabSearchLabel;

    private boolean skipstartstop = GlobalCoordinator.getInstance().settingsDisableStartStop;

    OrderType orderType;
    public StopsFragment() {
        // Required empty public constructor
    }
    public StopsFragment(Fragment hostingFragment, OrderType orderType){
        this.hostingFragment = hostingFragment;
        this.orderType = orderType;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        stopsViewModel = ViewModelProviders.of(hostingFragment).get(StopsViewModel.class);

        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_stops, container, false);

        //stopsViewModel = ViewModelProviders.of(getParentFragment()).get(StopsViewModel.class);
        //stopsViewModel.getSetTF().setValue(true);

        textCountAll = root.findViewById(R.id.stopCountAll);
        textCountDone = root.findViewById(R.id.stopCountDone);
        textCountPending = root.findViewById(R.id.stopCountPending);
        textCountInCompleteLabels = root.findViewById(R.id.textCountInCompleteLabels);
        textInCompleteLabels = root.findViewById(R.id.textInCompleteLabels);
        swipeRefreshLayout = root.findViewById(R.id.refreshStops);
        recyclerView = root.findViewById(R.id.stopsRecycler);
        fabSearchLabel = root.findViewById(R.id.fabSearchLabel);

        if(orderType.equals(OrderType.PICKUP)){
            textCountPending.setVisibility(View.GONE);
            textCountDone.setVisibility(View.GONE);
            textCountAll.setVisibility(View.GONE);
            textCountInCompleteLabels.setVisibility(View.GONE);
            textInCompleteLabels.setVisibility(View.GONE);
        }

        swipeRefreshLayout.setOnRefreshListener(this);
        //stopsViewModel = ViewModelProviders.of(hostingFragment).get(StopsViewModel.class);
        /*
        stopsViewModel.getmTest().setValue("Stops Fragment Loaded");
        ((Button)root.findViewById(R.id.testButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopsViewModel.getSetTF().setValue(true);

            }
        });

         */


        setupActions();

        observeServerResponses();

        observeCounters();

        setupRecyclerView();

        ((MainActivity)getActivity()).updateDriverLocation();
        return root;
    }

    private void setupActions() {
        fabSearchLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ManualEntryDialog manualEntryDialog = new ManualEntryDialog(StopsFragment.this.getContext());
                manualEntryDialog.setmListener(new ManualEntryDialog.OnManualEntryDialogListener() {
                    @Override
                    public void doneEditing(String s) {
                        dismissKeyboard();
                        searchForLabel(s);
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

    private void searchForLabel(String s) {
        Stop stop = stopsViewModel.getStopOfLabel(s);
        if(stop!=null){
            stopsViewModel.getSelectedStop().setValue(stop);
            if(skipstartstop){
                stopsViewModel.getSelectedStop().getValue().status = StopStatus.Started.getStopStatusCode();
                stopsViewModel.UpdateStop(stopsViewModel.getSelectedStop().getValue());
                stopsViewModel.getFragmentToLoad().setValue(StopsViewModel.StopFragments.StopContactsFragment);
            }else {
                stopsViewModel.getFragmentToLoad().setValue(StopsViewModel.StopFragments.StopDetailsFragment);
            }
            //stopsViewModel.getFragmentToLoad().setValue(StopsViewModel.StopFragments.StopDetailsFragment);
        }else{
            GlobalCoordinator.getInstance().notifyMessage(StopsFragment.this.getContext(), "Cannot find a stop with a label: " + s,0);
        }
    }

    private void dismissKeyboard() {
        View viewWithFocus = StopsFragment.this.getActivity().getCurrentFocus();
        if(viewWithFocus!=null){
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(viewWithFocus.getWindowToken(),0);

        }
    }

    private void setupRecyclerView() {
        // Set up the RecyclerView.
        adapter = new OrderAdapter(this.getActivity(), this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        if(orderType.equals(OrderType.PICKUP)) {
            ItemTouchHelper acceptRejectTouchHelper = new ItemTouchHelper(new SwipeToAcceptRejectCallback(adapter));
            acceptRejectTouchHelper.attachToRecyclerView(recyclerView);
        }
    }

    private void observeServerResponses() {

        stopsViewModel.getmError().observe(getViewLifecycleOwner(), new Observer<DriverServicesError>() {
            @Override
            public void onChanged(DriverServicesError driverServicesError) {
                if(driverServicesError == null) return;
                if(driverServicesError.errorType.equals(ServicesErrorType.generalError)){
                    //TODO: Handle General Error (eg disconnected or server error
                    Toast.makeText(StopsFragment.this.getContext(), driverServicesError.volleyError.toString(), Toast.LENGTH_LONG ).show();
                }else{
                    if(driverServicesError.errorType.equals(ServicesErrorType.stopsError)){
                        //TODO: void the current loaded list of orders as the server returned a handled exception
                        Toast.makeText(StopsFragment.this.getContext(), driverServicesError.errorMessage, Toast.LENGTH_LONG ).show();
                    }

                }

                swipeRefreshLayout.setRefreshing(false);
            }
        });


        stopsViewModel.getmServerStops().observe(getViewLifecycleOwner(), new Observer<ResponseMessage<JsonStop[]>>() {
            @Override
            public void onChanged(ResponseMessage<JsonStop[]> responseMessage) {
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        stopsViewModel.getmAcceptStopResponse().observe(getViewLifecycleOwner(), new Observer<ResponseMessage<Boolean>>() {
            @Override
            public void onChanged(ResponseMessage<Boolean> booleanResponseMessage) {
                if(booleanResponseMessage == null) return;
                if(booleanResponseMessage.success){
                    stopsViewModel.getSelectedStop().getValue().status = StopStatus.Accepted.getStopStatusCode();
                }else {
                    Toast.makeText(StopsFragment.this.getContext(), booleanResponseMessage.errorMessage, Toast.LENGTH_LONG).show();
                }
                stopsViewModel.UpdateStop(stopsViewModel.getSelectedStop().getValue());
                stopsViewModel.getmAcceptStopResponse().setValue(null);
            }
        });


    }



    private void observeCounters(){
        stopsViewModel.getmAllStopCount().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                textCountAll.setText(integer.toString());
            }
        });

        stopsViewModel.getmCompletedStopCount().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                textCountDone.setText(integer.toString());
            }
        });

        stopsViewModel.getmNotStartedStopsCount().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                textCountPending.setText(integer.toString());
            }
        });

        stopsViewModel.getmLocalStops().observe(getViewLifecycleOwner(), new Observer<List<Stop>>() {
            @Override
            public void onChanged(List<Stop> stops) {
                if(stops!=null){
                    adapter.setStops(stops);
                }
            }
        });

        stopsViewModel.getmInCompletedStopCount().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                //TODO: Check Settings
                /*
                if(integer == 2){
                    Toast.makeText(StopsFragment.this.getContext(), "Settings for Incomplete Stops Reached", Toast.LENGTH_LONG).show();
                }
                if(integer >= 2){
                    Toast.makeText(StopsFragment.this.getContext(), "Settings for Incomplete Stops Exceeded", Toast.LENGTH_LONG).show();
                }

                 */

            }
        });

        stopsViewModel.getmCompleteLabelCount().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {

            }
        });

        stopsViewModel.getmInCompleteLabelCount().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                if(integer == null) return;
                textCountInCompleteLabels.setText(integer.toString());
            }
        });
    }

    @Override
    public void onRefresh() {
        if(!stopsViewModel.GetStopsFromServer())
            swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onStopClick(int position, Stop stop) {
        stopsViewModel.getSelectedStop().setValue(stop);
        if(skipstartstop){
            stopsViewModel.getSelectedStop().getValue().status = StopStatus.Started.getStopStatusCode();
            stopsViewModel.UpdateStop(stopsViewModel.getSelectedStop().getValue());
            stopsViewModel.getFragmentToLoad().setValue(StopsViewModel.StopFragments.StopContactsFragment);
        }else {
            stopsViewModel.getFragmentToLoad().setValue(StopsViewModel.StopFragments.StopDetailsFragment);
        }
    }

    @Override
    public void onStopSwipeLeft(int position, Stop stop) {
        //Reject
        stopsViewModel.getSelectedStop().setValue(stop);
        stopsViewModel.UpdateStop(stop);
        stopsViewModel.getFragmentToLoad().setValue(StopsViewModel.StopFragments.StopRejectFragment);
    }

    @Override
    public void onStopSwipeRight(int position, Stop stop) {
        //Accept
        stopsViewModel.getSelectedStop().setValue(stop);
        stopsViewModel.acceptStop(stopsViewModel.getmDriverLocation().getValue().longitude,stopsViewModel.getmDriverLocation().getValue().latitude);

    }


    @Override
    public void onNewScan(String characters) {
        characters = characters.trim();
        searchForLabel(characters);
    }

    @Override
    public void onNewIntent(Intent intent) {

    }

    @Override
    public void onNewLocation(DriverLocation driverLocation) {
        stopsViewModel.getmDriverLocation().setValue(driverLocation);
    }
}
