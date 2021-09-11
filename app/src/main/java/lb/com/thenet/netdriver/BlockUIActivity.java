package lb.com.thenet.netdriver;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import lb.com.thenet.netdriver.onlineservices.DriverServices;
import lb.com.thenet.netdriver.onlineservices.json.JsonStop;
import lb.com.thenet.netdriver.onlineservices.json.OrderType;
import lb.com.thenet.netdriver.onlineservices.json.ResponseMessage;
import lb.com.thenet.netdriver.rooms.entities.DriverRepository;
import lb.com.thenet.netdriver.rooms.entities.OrderRepository;
import lb.com.thenet.netdriver.rooms.entities.stops.Stop;
import lb.com.thenet.netdriver.ui.blockui.BlockStopsFragment;
import lb.com.thenet.netdriver.ui.blockui.OnBlockFragmentInteraction;

import static lb.com.thenet.netdriver.BlockUIActivity.*;

public class BlockUIActivity extends FragmentActivity implements OnBlockFragmentInteraction {

    private BlockUIActivityViewModel blockUIActivityViewModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        blockUIActivityViewModel = ViewModelProviders.of(this).get(BlockUIActivityViewModel.class);

        setContentView(R.layout.activity_block_ui);

        DriverRepository repository = new DriverRepository(this);

        repository.updateLockStatus(true);

        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if(findViewById(R.id.blockFragmentContainer) != null){
            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }
            // Create a new Fragment to be placed in the activity layout
            BlockStopsFragment blockStopsFragment = new BlockStopsFragment(this);

            // In case this activity was started with special instructions from an
            // Intent, pass the Intent's extras to the fragment as arguments
            blockStopsFragment.setArguments(getIntent().getExtras());

            // Add the fragment to the 'fragment_container' FrameLayout
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.blockFragmentContainer, blockStopsFragment).commit();

            blockUIActivityViewModel.testString.setValue("Accept Pickups");

        }

        Intent intent = getIntent();
        boolean getForceAssignedOrdersFromServer = intent.getBooleanExtra("getFromServer", false);
        if(getForceAssignedOrdersFromServer){
            getForcePickupsFromServer();
            blockUIActivityViewModel.getmForceAssignedStopsResponse().observe(BlockUIActivity.this, new Observer<ResponseMessage<JsonStop[]>>() {
                @Override
                public void onChanged(ResponseMessage<JsonStop[]> responseMessage) {
                    if(responseMessage.success) {
                        if(responseMessage.dataCount != 0 && responseMessage.data != null && responseMessage.data.length != 0) {
                            OrderRepository repository = new OrderRepository(BlockUIActivity.this.getApplication(), OrderType.FORCEPICKUP);
                            blockUIActivityViewModel.setStopsReceived(responseMessage.data, OrderType.FORCEPICKUP);
                            repository.refreshStops(responseMessage.data, OrderType.FORCEPICKUP);
                        }
                    }
                }
            });
        }

        this.setTitle("Blocked - Force Assigned");


    }

    private void getForcePickupsFromServer() {
        if(DriverServices.mToken != null && DriverServices.mToken.getValue() != null && DriverServices.mToken.getValue().Token != null && !DriverServices.mToken.getValue().Token.equals(""))
            if(blockUIActivityViewModel.getShouldLogin() != null && !blockUIActivityViewModel.getShouldLogin().getValue()) {
                //call web service to store the pickups?\
                blockUIActivityViewModel.getForcePickupsFromServer();

            }
    }

    @Override
    public void onBackPressed() {

        return;
    }

    @Override
    public void viewStop(Stop stop) {

    }

    @Override
    public void rejectStop(Stop stop) {

    }



}
