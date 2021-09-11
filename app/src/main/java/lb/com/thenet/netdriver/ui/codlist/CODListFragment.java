package lb.com.thenet.netdriver.ui.codlist;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import lb.com.thenet.netdriver.GlobalCoordinator;
import lb.com.thenet.netdriver.MainActivity;
import lb.com.thenet.netdriver.R;
import lb.com.thenet.netdriver.adapters.CODItemAdapter;
import lb.com.thenet.netdriver.dataholders.DriverLocation;
import lb.com.thenet.netdriver.onlineservices.json.CODItem;
import lb.com.thenet.netdriver.onlineservices.json.ResponseMessage;
import lb.com.thenet.netdriver.ui.liaison.LiaisonFragment;
import lb.com.thenet.netdriver.ui.liaison.LiaisonViewModel;

public class CODListFragment extends Fragment implements MainActivity.OnScanListener {

    private CODListViewModel codListViewModel;

    RecyclerView codListRecycler;

    Context context;

    CODItemAdapter codItemAdapter;
    TextView codListTotalLBP;
    TextView codListTotalLBPDetails;
    TextView codListTotalUSD;
    TextView codListTotalUSDDetails;


    public CODListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        codListViewModel = ViewModelProviders.of(this).get(CODListViewModel.class);

        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_cod_list, container, false);

        codListRecycler = root.findViewById(R.id.codListRecycler);

        codListTotalLBP = root.findViewById(R.id.codListTotalLBP);
        codListTotalLBPDetails = root.findViewById(R.id.codListTotalLBPDetails);
        codListTotalUSD = root.findViewById(R.id.codListTotalUSD);
        codListTotalUSDDetails = root.findViewById(R.id.codListTotalUSDDetails);

        context = this.getActivity();

        codListViewModel.getmCodItems().observe(getViewLifecycleOwner(), new Observer<ResponseMessage<CODItem[]>>() {
            @Override
            public void onChanged(ResponseMessage<CODItem[]> responseMessage) {
                if(responseMessage != null){

                    if(responseMessage.success) {
                        codItemAdapter = new CODItemAdapter(CODListFragment.this.getContext(), responseMessage.data);
                        codListRecycler.setAdapter(codItemAdapter);
                        codListRecycler.setLayoutManager(new LinearLayoutManager(CODListFragment.this.getContext()));

                        String totalLPB = context.getString(R.string.codTotalLBPFormat, codItemAdapter.getTotalCollectedLBP(""),GlobalCoordinator.getInstance().settingsCountryCurrency.toUpperCase());
                        codListTotalLBP.setText(totalLPB);
                        String totalUSD = context.getString(R.string.codTotalUSDFormat, codItemAdapter.getTotalCollectedUSD(""));
                        codListTotalUSD.setText(totalUSD);

                        String totalLPBDetails = context.getString(R.string.codTotalDetailsFormat, codItemAdapter.getTotalCollectedLBP("cash"), codItemAdapter.getTotalCollectedLBP("check"), codItemAdapter.getTotalCollectedLBP("cc"));
                        codListTotalLBPDetails.setText(totalLPBDetails);
                        String totalUSDDetails = context.getString(R.string.codTotalDetailsFormat, codItemAdapter.getTotalCollectedUSD("cash"), codItemAdapter.getTotalCollectedUSD("check"), codItemAdapter.getTotalCollectedUSD("cc"));
                        codListTotalUSDDetails.setText(totalUSDDetails);


                    }else {
                        Toast.makeText(CODListFragment.this.getContext(), "Failure: " + responseMessage.errorMessage + responseMessage.message, Toast.LENGTH_LONG).show();
                    }

                }
            }
        });

        codListViewModel.getCodList();


        return root;
    }

    @Override
    public void onNewScan(String characters) {

    }

    @Override
    public void onNewIntent(Intent intent) {

    }

    @Override
    public void onNewLocation(DriverLocation driverLocation) {

    }
}
