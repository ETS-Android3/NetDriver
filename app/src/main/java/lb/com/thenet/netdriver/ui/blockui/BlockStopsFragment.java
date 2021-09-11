package lb.com.thenet.netdriver.ui.blockui;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;

import lb.com.thenet.netdriver.BlockUIActivityViewModel;
import lb.com.thenet.netdriver.GlobalCoordinator;
import lb.com.thenet.netdriver.R;
import lb.com.thenet.netdriver.SwipeToAcceptRejectCallback;
import lb.com.thenet.netdriver.adapters.OrderAdapter;
import lb.com.thenet.netdriver.onlineservices.json.OrderType;
import lb.com.thenet.netdriver.rooms.entities.DriverRepository;
import lb.com.thenet.netdriver.rooms.entities.enums.StopStatus;
import lb.com.thenet.netdriver.rooms.entities.stops.Stop;

/**
 * A simple {@link Fragment} subclass.
 */
public class BlockStopsFragment extends Fragment implements OrderAdapter.OnStopClickedListener {


    FragmentActivity hostingActivity;
    BlockUIActivityViewModel blockUIActivityViewModel;
    TextView blockUIText;
    RecyclerView blockStops;
    private OrderAdapter adapter;

    public BlockStopsFragment(FragmentActivity activity) {
        // Required empty public constructor
        hostingActivity = activity;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        blockUIActivityViewModel = ViewModelProviders.of(hostingActivity).get(BlockUIActivityViewModel.class);

        View root = inflater.inflate(R.layout.fragment_block_stops, container, false);

        blockUIText = root.findViewById(R.id.blockUIText);
        blockStops = root.findViewById(R.id.blockStops);

        //final DriverRepository repository = new DriverRepository(BlockStopsFragment.this.getContext());


        final Button dismissButton = root.findViewById(R.id.dismissButton);
        dismissButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                blockUIActivityViewModel.getmDriverRepository().updateLockStatus(false);
                GlobalCoordinator.getInstance().setUnlocked();
                hostingActivity.finish();
            }
        });


        blockUIActivityViewModel.testString.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                blockUIText.setText(s);
            }
        });

        blockUIActivityViewModel.getmDriverRepository().initializeForcedAssignedStopsFromDB();
        blockUIActivityViewModel.getmDriverRepository().mForceAssignedCount.observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                if(integer == 0)
                    dismissButton.setVisibility(View.VISIBLE);
                else
                    dismissButton.setVisibility(View.GONE);
            }
        });
        blockUIActivityViewModel.getmDriverRepository().mForceAssignedStops.observe(this, new Observer<List<Stop>>() {
            @Override
            public void onChanged(List<Stop> stops) {
                adapter.setStops(stops);
            }
        });

        setupRecyclerView();

        return root;

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(!(hostingActivity instanceof OnBlockFragmentInteraction)){
            throw new RuntimeException("Invalid Hosting Activity", new Throwable("Should Implement OnBlockFragmentInteraction"));
        }
    }

    private void setupRecyclerView() {
        // Set up the RecyclerView.
        adapter = new OrderAdapter(this.getActivity(), this);
        blockStops.setAdapter(adapter);
        blockStops.setLayoutManager(new LinearLayoutManager(this.getContext()));

        ItemTouchHelper acceptRejectTouchHelper = new ItemTouchHelper(new SwipeToAcceptRejectCallback(adapter));
        acceptRejectTouchHelper.attachToRecyclerView(blockStops);

    }

    @Override
    public void onStopClick(int position, Stop stop) {

    }

    @Override
    public void onStopSwipeLeft(int position, Stop stop) {



        blockUIActivityViewModel.getmDriverRepository().UpdateStop(stop);

    }

    @Override
    public void onStopSwipeRight(int position, Stop stop) {
        stop.status = StopStatus.Accepted.getStopStatusCode();
        stop.orderTypeId = OrderType.PICKUP.getOrderTypeId();
        blockUIActivityViewModel.getmDriverRepository().UpdateStop(stop);

    }
}
