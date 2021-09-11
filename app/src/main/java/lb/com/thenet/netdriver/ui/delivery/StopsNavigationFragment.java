package lb.com.thenet.netdriver.ui.delivery;


import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import lb.com.thenet.netdriver.MainActivity;
import lb.com.thenet.netdriver.R;
import lb.com.thenet.netdriver.dataholders.DriverLocation;
import lb.com.thenet.netdriver.onlineservices.json.JsonStop;
import lb.com.thenet.netdriver.onlineservices.json.OrderType;
import lb.com.thenet.netdriver.onlineservices.json.ResponseMessage;

/**
 * A simple {@link Fragment} subclass.
 */
public class StopsNavigationFragment extends Fragment implements MainActivity.OnScanListener, OnStopsFragmentsInteraction {


    StopsViewModel stopsViewModel;

    OrderType orderType;
    public StopsNavigationFragment(OrderType orderType) {
        // Required empty public constructor
        this.orderType = orderType;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        stopsViewModel = ViewModelProviders.of(this).get(StopsViewModel.class);
        stopsViewModel.setOrderType(orderType);
        stopsViewModel.checkLoadStopsFromServer();

        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_stops_navigation, container, false);

        observeServerOrdersCount();

        loadInitialFragment();

        return root;
    }

    private void observeServerOrdersCount() {


        stopsViewModel.getmServerStops().observe(getViewLifecycleOwner(), new Observer<ResponseMessage<JsonStop[]>>() {
            @Override
            public void onChanged(ResponseMessage<JsonStop[]> responseMessage) {
                if(responseMessage.success) {
                    if(responseMessage.data != null) {
                        //stopsViewModel.getmTotalDeliveryStops().setValue(responseMessage.data.length);
                        //stopsViewModel.getmDoneDeliveryStops().setValue(0);
                        //stopsViewModel.getmPendingDeliveryStops().setValue(responseMessage.data.length);
                        stopsViewModel.RefreshStops();
                    }else{
                        //stopsViewModel.getmTotalDeliveryStops().setValue(0);
                        //stopsViewModel.getmDoneDeliveryStops().setValue(0);
                        //stopsViewModel.getmPendingDeliveryStops().setValue(0);
                        //stopsViewModel.RefreshStops();
                    }
                }else{
                    //TODO: Handle get delivery stops error

                }
            }
        });

        /*
        stopsViewModel.getmCompletedStopCount().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                stopsViewModel.getmDoneStops().setValue(integer);
            }
        });

        stopsViewModel.getmAllStopCount().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                stopsViewModel.getmTotalStops().setValue(integer);
            }
        });

        stopsViewModel.getmStartedStopsCount().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                //stopsViewModel.getmDoneDeliveryStops().setValue(integer);
            }
        });

        stopsViewModel.getmNotStartedStopsCount().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                stopsViewModel.getmPendingStops().setValue(integer);
            }
        });

         */

    }

    private void loadInitialFragment() {
        StopsFragment stopsFragment = new StopsFragment(this, orderType);
        getFragmentManager().beginTransaction().add(R.id.fragment_container, stopsFragment).commit();
        ((MainActivity)getActivity()).showCameraScanFAB();


        stopsViewModel.getFragmentToLoad().observe(getViewLifecycleOwner(), new Observer<StopsViewModel.StopFragments>() {
            @Override
            public void onChanged(StopsViewModel.StopFragments stopFragments) {
                switch (stopFragments){
                    case StopDetailsFragment: {

                        ((MainActivity)getActivity()).hideCameraScanFAB();


                        FragmentTransaction transaction = getFragmentManager().beginTransaction();

                        /*
                        Fragment loadedStopsFragment = getFragmentManager().findFragmentById(R.id.fragment_container);
                        if(loadedStopsFragment != null && loadedStopsFragment instanceof StopsFragment){
                            transaction.add(R.id.fragment_container, new StopDetailsFragment(StopsNavigationFragment.this));
                            transaction.commit();
                        }else {

                         */
                            //FragmentTransaction transaction = getFragmentManager().beginTransaction();
                            StopDetailsFragment fragment = new StopDetailsFragment(StopsNavigationFragment.this);
                            // Replace whatever is in the fragment_container view with this fragment,
                            // and add the transaction to the back stack so the user can navigate back
                            transaction.add(R.id.fragment_container, fragment);
                            transaction.addToBackStack(null);

                            // Commit the transaction
                            transaction.commit();



                        break;
                    }
                    case StopContactsFragment:{
                        ((MainActivity)getActivity()).hideCameraScanFAB();

                        FragmentTransaction transaction = getFragmentManager().beginTransaction();
                        StopContactsFragment fragment = new StopContactsFragment(StopsNavigationFragment.this);
                        // Replace whatever is in the fragment_container view with this fragment,
                        // and add the transaction to the back stack so the user can navigate back
                        transaction.add(R.id.fragment_container, fragment);
                        //transaction.addToBackStack(null);

                        // Commit the transaction
                        transaction.commit();
                        break;
                    }
                    case StopsFragment:{

                        ((MainActivity)getActivity()).showCameraScanFAB();


                        /*
                        Fragment loadedDetailsFragment = getFragmentManager().findFragmentById(R.id.fragment_container);
                        if(loadedDetailsFragment != null && loadedDetailsFragment instanceof StopDetailsFragment){
                            transaction.remove(loadedDetailsFragment);
                            transaction.commit();
                        }
                        else{

                         */
                        //FragmentTransaction transaction = getFragmentManager().beginTransaction();
                        // Replace whatever is in the fragment_container view with this fragment,
                        // and add the transaction to the back stack so the user can navigate back
                        //transaction.addToBackStack(null);

                        // Commit the transaction
                        /*
                        FragmentTransaction transaction = getFragmentManager().beginTransaction();
                        StopsFragment fragment = new StopsFragment(StopsNavigationFragment.this);
                        transaction.replace(R.id.fragment_container, fragment);
                        transaction.commit();
*

                         */


                        break;
                    }
                    case StopLabelsFragment:{
                        ((MainActivity)getActivity()).showCameraScanFAB();

                        FragmentTransaction transaction = getFragmentManager().beginTransaction();
                        StopLabelLabelsFragment fragment = new StopLabelLabelsFragment(StopsNavigationFragment.this);
                        // Replace whatever is in the fragment_container view with this fragment,
                        // and add the transaction to the back stack so the user can navigate back
                        transaction.add(R.id.fragment_container, fragment);
                        //transaction.addToBackStack(null);

                        // Commit the transaction
                        transaction.commit();
                        break;
                    }


                    case StopDeliveryFragment:{

                        ((MainActivity)getActivity()).hideCameraScanFAB();

                        FragmentTransaction transaction = getFragmentManager().beginTransaction();
                        StopDeliveryFragment fragment = new StopDeliveryFragment(StopsNavigationFragment.this);
                        // Replace whatever is in the fragment_container view with this fragment,
                        // and add the transaction to the back stack so the user can navigate back
                        transaction.add(R.id.fragment_container, fragment);
                        //transaction.addToBackStack(null);

                        // Commit the transaction
                        transaction.commit();
                        break;
                    }
                    case StopPickupFragment:{
                        ((MainActivity)getActivity()).showCameraScanFAB();

                        FragmentTransaction transaction = getFragmentManager().beginTransaction();
                        StopPickupFragment fragment = new StopPickupFragment(StopsNavigationFragment.this);
                        // Replace whatever is in the fragment_container view with this fragment,
                        // and add the transaction to the back stack so the user can navigate back
                        transaction.add(R.id.fragment_container, fragment);
                        //transaction.addToBackStack(null);

                        // Commit the transaction
                        transaction.commit();
                        break;
                    }
                    case StopDeliveryFailureFragment:{
                        ((MainActivity)getActivity()).hideCameraScanFAB();

                        FragmentTransaction transaction = getFragmentManager().beginTransaction();
                        StopDeliveryFailureFragment fragment = new StopDeliveryFailureFragment(StopsNavigationFragment.this);
                        // Replace whatever is in the fragment_container view with this fragment,
                        // and add the transaction to the back stack so the user can navigate back
                        transaction.add(R.id.fragment_container, fragment);
                        //transaction.addToBackStack(null);

                        // Commit the transaction
                        transaction.commit();
                        break;

                    }
                    case StopRejectFragment:{
                        ((MainActivity)getActivity()).hideCameraScanFAB();

                        FragmentTransaction transaction = getFragmentManager().beginTransaction();
                        StopRejectFragment fragment = new StopRejectFragment(StopsNavigationFragment.this);
                        // Replace whatever is in the fragment_container view with this fragment,
                        // and add the transaction to the back stack so the user can navigate back
                        transaction.add(R.id.fragment_container, fragment);
                        //transaction.addToBackStack(null);

                        // Commit the transaction
                        transaction.commit();
                        break;
                    }
                    case StopLabelRetourFragment:{
                        ((MainActivity)getActivity()).hideCameraScanFAB();

                        FragmentTransaction transaction = getFragmentManager().beginTransaction();
                        StopLabelRetourFragment fragment = new StopLabelRetourFragment(StopsNavigationFragment.this);
                        // Replace whatever is in the fragment_container view with this fragment,
                        // and add the transaction to the back stack so the user can navigate back
                        transaction.add(R.id.fragment_container, fragment);
                        //transaction.addToBackStack(null);

                        // Commit the transaction
                        transaction.commit();
                        break;
                    }
                    default:{

                    }

                }
            }
        });

    }

    @Override
    public void onNewScan(String characters) {

        Fragment loadedDetailsFragment = getFragmentManager().findFragmentById(R.id.fragment_container);
        if(loadedDetailsFragment != null && loadedDetailsFragment instanceof MainActivity.OnScanListener){
            ((MainActivity.OnScanListener) loadedDetailsFragment).onNewScan(characters);
        }

    }

    private void removeOneFragment(Fragment currentFragment){
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        //currentFragment = getFragmentManager().findFragmentById(R.id.fragment_container);
        transaction.remove(currentFragment);
        transaction.commit();
    }

    @Override
    public void onNewIntent(Intent intent) {

        Fragment loadedDetailsFragment = getFragmentManager().findFragmentById(R.id.fragment_container);
        if(loadedDetailsFragment != null && loadedDetailsFragment instanceof MainActivity.OnScanListener){
            ((MainActivity.OnScanListener) loadedDetailsFragment).onNewIntent(intent);
        }
    }

    @Override
    public void onNewLocation(DriverLocation driverLocation) {
        Fragment loadedDetailsFragment = getFragmentManager().findFragmentById(R.id.fragment_container);
        if(loadedDetailsFragment != null && loadedDetailsFragment instanceof MainActivity.OnScanListener){
            ((MainActivity.OnScanListener) loadedDetailsFragment).onNewLocation(driverLocation);
        }

    }

    @Override
    public void backToPreviousFragment(boolean initialFragment) {
        try {
            if (!initialFragment) {
                //currentFragment = getFragmentManager().findFragmentById(R.id.fragment_container);

                for (Fragment fragment : getFragmentManager().getFragments()) {
                    if(fragment instanceof OnFragmentUnhide )
                        ((OnFragmentUnhide) fragment).fragmentUnHidden();
                }

                Fragment currentFragment = getFragmentManager().findFragmentById(R.id.fragment_container);

                removeOneFragment(currentFragment);



                return;
            }

            for (Fragment fragment : getFragmentManager().getFragments()) {
                if (fragment instanceof StopsFragment || fragment instanceof StopsNavigationFragment) {
                    continue;
                } else if (fragment != null) {
                    removeOneFragment(fragment);
                }
            }
        }catch (Exception ex){}


/*
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        Fragment currentFragment = getFragmentManager().findFragmentById(R.id.fragment_container);
        transaction.remove(currentFragment);
        if(initialFragment){
            currentFragment = getFragmentManager().findFragmentById(R.id.fragment_container);
            while(!(currentFragment instanceof StopsFragment))
            {
                transaction.remove(currentFragment);
            }
        }
        transaction.commit();

 */
    }
}
