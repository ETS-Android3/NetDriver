package lb.com.thenet.netdriver.ui.delivery;


import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import lb.com.thenet.netdriver.R;
import lb.com.thenet.netdriver.adapters.OrderContactAdapter;
import lb.com.thenet.netdriver.rooms.entities.stops.Stop;
import lb.com.thenet.netdriver.rooms.entities.stops.StopContact;

/**
 * A simple {@link Fragment} subclass.
 */
public class StopContactsFragment extends Fragment implements OrderContactAdapter.OnStopClickedListener {

    private StopsViewModel stopsViewModel;
    private Fragment hostingFragment;
    private RecyclerView contactsRecycler;
    private OrderContactAdapter adapter;
    private TextView contactsCountText;


    public StopContactsFragment() {
        // Required empty public constructor
    }

    public StopContactsFragment(Fragment hostingFragment){
        this.hostingFragment = hostingFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        stopsViewModel = ViewModelProviders.of(hostingFragment).get(StopsViewModel.class);

        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_stop_contacts, container, false);

        contactsRecycler = root.findViewById(R.id.contactsRecycler);
        contactsCountText = root.findViewById(R.id.contactsCountText);


        //contactsCountText.setText(String.valueOf(stopsViewModel.getSelectedStop().getValue().numberOfContacts));
        //stopsViewModel.DeepLoadStop(stopsViewModel.getSelectedStop().getValue());
        stopsViewModel.DeepLoadStop(stopsViewModel.getSelectedStop().getValue());
        stopsViewModel.getSelectedStop().getValue().stopContactLiveData.observe(this, new Observer<List<StopContact>>() {
            @Override
            public void onChanged(List<StopContact> stopContacts) {

                contactsCountText.setText(String.valueOf(stopContacts.size()));

                adapter.setStops(stopContacts);
            }
        });

        Button contactsBackButton = root.findViewById(R.id.contactsBackButton);
        contactsBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((OnStopsFragmentsInteraction)hostingFragment).backToPreviousFragment(false);
                //stopsViewModel.getFragmentToLoad().setValue(StopsViewModel.StopFragments.StopDetailsFragment);
            }
        });

        setupRecyclerView();
        return root;
    }

    private void setupRecyclerView() {
        adapter = new OrderContactAdapter(this.getActivity(), this);
        contactsRecycler.setAdapter(adapter);
        contactsRecycler.setLayoutManager(new LinearLayoutManager(this.getContext()));

    }

    @Override
    public void onStopClick(int position, StopContact stopContact) {
        //stopsViewModel.DeepLoadStop(stopContact);
        stopsViewModel.getSelectedStopContact().setValue(stopContact);
        stopsViewModel.getFragmentToLoad().setValue(StopsViewModel.StopFragments.StopLabelsFragment);
    }
}
