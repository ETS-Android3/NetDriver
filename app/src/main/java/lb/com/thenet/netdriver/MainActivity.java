package lb.com.thenet.netdriver;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.nfc.NfcAdapter;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.os.Build;
import android.os.Bundle;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

import java.util.LinkedList;

import lb.com.thenet.netdriver.dataholders.DriverLocation;
import lb.com.thenet.netdriver.onlineservices.DriverServices;
import lb.com.thenet.netdriver.onlineservices.DriverServicesError;
import lb.com.thenet.netdriver.onlineservices.json.ActionRoute;
import lb.com.thenet.netdriver.onlineservices.json.JsonStop;
import lb.com.thenet.netdriver.onlineservices.json.LoggedInUserData;
import lb.com.thenet.netdriver.onlineservices.json.OrderType;
import lb.com.thenet.netdriver.onlineservices.json.ResponseMessage;
import lb.com.thenet.netdriver.onlineservices.json.SettingItem;
import lb.com.thenet.netdriver.rooms.entities.LOVRepository;
import lb.com.thenet.netdriver.rooms.entities.OrderRepository;
import lb.com.thenet.netdriver.ui.ConfirmDialog;
import lb.com.thenet.netdriver.ui.home.HomeFragment;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;


public class MainActivity extends AppCompatActivity {

    public static final int BARCODE_ACTIVITY_REQUEST_CODE = 2;
    private AppBarConfiguration mAppBarConfiguration;
    private MainActivityViewModel mainActivityViewModel;
    private TextView navDisplayName;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest mLocationRequest;

    private long UPDATE_INTERVAL = 10 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2 * 1000;  /* 2 sec */


    // list of NFC technologies detected:
    private final String[][] techList = new String[][]{
            new String[]{
                    NfcA.class.getName(),
                    NfcB.class.getName(),
                    NfcF.class.getName(),
                    NfcV.class.getName(),
                    IsoDep.class.getName(),
                    MifareClassic.class.getName(),
                    MifareUltralight.class.getName(), Ndef.class.getName()
            }
    };

    private LinkedList<String> testBarcodes = new LinkedList<>();

    FloatingActionButton cameraScanFAB;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //setup toolbar for common title app bar menu
        Toolbar toolbar = findViewById(R.id.toolbar);

        cameraScanFAB = findViewById(R.id.cameraScanFAB);

        //Make sure to observe the lock status in order to lock the application if there were pending jobs
        mainActivityViewModel = ViewModelProviders.of(this).get(MainActivityViewModel.class);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        navDisplayName = headerView.findViewById(R.id.navDisplayName);

        UPDATE_INTERVAL = getResources().getInteger(R.integer.locationUpdateIntervalSeconds) * 1000;  /* 10 secs */
        FASTEST_INTERVAL = getResources().getInteger(R.integer.locationFastestIntervalSeconds) * 1000;// 2000; /* 2 sec */

        setSupportActionBar(toolbar);

        setupTestFloatingButton();
        setupCameraScanFAB();

        setupNavigationDrawer();


        GlobalCoordinator.getInstance().checkFirebaseToken(this);
        GlobalCoordinator.getInstance().checkIntentForJobs(getIntent(), this);

        observeGlobalBooleans();

        DriverServices.mToken.observe(this, new Observer<LoggedInUserData>() {
            @Override
            public void onChanged(LoggedInUserData loggedInUserData) {
                if (loggedInUserData == null) return;
                navDisplayName.setText(loggedInUserData.DisplayName);
                mainActivityViewModel.getSettings();
                //Get forced pickups from server and save in DB
                if (mainActivityViewModel.getmDriverRepository().mForceAssignedCount != null)
                    mainActivityViewModel.getmDriverRepository().mForceAssignedCount.removeObservers(MainActivity.this);
                getForcePickupsFromServer();

                //observe any forced pickups in DB
                mainActivityViewModel.getmDriverRepository().initializeForcedAssignedStopsFromDB();//.initializeForceAssignedStopsFromDB();
                mainActivityViewModel.getmDriverRepository().mForceAssignedCount.observe(MainActivity.this, new Observer<Integer>() {
                    @Override
                    public void onChanged(Integer integer) {
                        //if there were force assigned stops in the DB, and the screen is not locked, the lock it
                        if (integer > 0 && !GlobalCoordinator.getInstance().isLocked()) {
                            GlobalCoordinator.getInstance().InvokeLockScreen(MainActivity.this, false);
                        }
                    }
                });

            }
        });


        observeServerResponse();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermission();
        }


        startLocationUpdates();


    }

    private void startLocationUpdates() {
        // Create the location request to start receiving updates
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        // Check whether location settings are satisfied
        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        // do work here
                        onLocationChanged(locationResult.getLastLocation());
                    }
                },
                Looper.myLooper());
    }

    public void onLocationChanged(Location location) {
        // New location has now been determined
        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
        //Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        // You can now create a LatLng Object for use with maps
        mainActivityViewModel.getmDriverLocation().setValue(new DriverLocation(location.getLongitude(), location.getLatitude()));
        //LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
    }


    private void observeServerResponse() {

        mainActivityViewModel.getmRunSheetResponse().observe(this, new Observer<ResponseMessage<Boolean>>() {
            @Override
            public void onChanged(ResponseMessage<Boolean> booleanResponseMessage) {
                if (booleanResponseMessage == null) return;
                if (booleanResponseMessage.success) {
                    Toast.makeText(MainActivity.this, "Print Request Success: " + booleanResponseMessage.message, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this, "Print Request Failure: " + booleanResponseMessage.errorMessage, Toast.LENGTH_LONG).show();
                }
                mainActivityViewModel.getmRunSheetResponse().setValue(null);
            }
        });

        mainActivityViewModel.getmActionRoutesResponse().observe(this, new Observer<ResponseMessage<ActionRoute[]>>() {
            @Override
            public void onChanged(ResponseMessage<ActionRoute[]> responseMessage) {
                if (responseMessage == null) return;
                if (responseMessage.success) {
                    LOVRepository repository = new LOVRepository(getApplication());
                    repository.refreshActionRoutes(responseMessage.data);
                    GlobalCoordinator.getInstance().setmShouldInitializeLOVs(false);
                }
                mainActivityViewModel.getmActionRoutesResponse().setValue(null);
            }
        });

        mainActivityViewModel.getmSettingsResponse().observe(this, new Observer<ResponseMessage<SettingItem[]>>() {
            @Override
            public void onChanged(ResponseMessage<SettingItem[]> responseMessage) {
                if (responseMessage == null) return;
                if (responseMessage.success) {

                    //set settings token in order not to load the settings more than once for the same user
                    mainActivityViewModel.currentSettingsToken.setValue(DriverServices.mToken.getValue().Token);

                    for (SettingItem setting :
                            responseMessage.data) {
                        switch (setting.name.toLowerCase()) {
                            case "numberofstops": {
                                GlobalCoordinator.getInstance().settingsNumberOfStops = Integer.parseInt(setting.value);
                                break;
                            }
                            case "disablecheckoutnfc": {
                                if (setting.value.equals("1"))
                                    GlobalCoordinator.getInstance().settingsDisableCheckOutNFC = true;
                                else
                                    GlobalCoordinator.getInstance().settingsDisableCheckOutNFC = false;
                                break;
                            }
                            case "disablecheckinnfc": {
                                if (setting.value.equals("1"))
                                    GlobalCoordinator.getInstance().settingsDisableCheckInNFC = true;
                                else
                                    GlobalCoordinator.getInstance().settingsDisableCheckInNFC = false;
                                break;
                            }
                            case "disabledeliverynfc": {
                                if (setting.value.equals("1"))
                                    GlobalCoordinator.getInstance().settingsDisableDeliveryNFC = true;
                                else
                                    GlobalCoordinator.getInstance().settingsDisableDeliveryNFC = false;
                                break;
                            }
                            case "disablestartstop":{
                                if(setting.value.equals("1"))
                                    GlobalCoordinator.getInstance().settingsDisableStartStop = true;
                                else
                                    GlobalCoordinator.getInstance().settingsDisableStartStop = false;
                            }
                            case "countrycurrency":{
                                GlobalCoordinator.getInstance().settingsCountryCurrency = setting.value;
                            }
                        }
                    }
                    Toast.makeText(MainActivity.this, "Loaaded Settings...", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this, "Couldn't get settings: " + responseMessage.errorMessage, Toast.LENGTH_LONG).show();
                }
                mainActivityViewModel.getmSettingsResponse().setValue(null);
            }
        });


        mainActivityViewModel.getmForceAssignedStopsResponse().observe(MainActivity.this, new Observer<ResponseMessage<JsonStop[]>>() {
            @Override
            public void onChanged(ResponseMessage<JsonStop[]> responseMessage) {
                if (responseMessage.success) {
                    if (responseMessage.dataCount == 0 || responseMessage.data == null || responseMessage.data.length == 0)
                        return;
                    OrderRepository repository = new OrderRepository(MainActivity.this.getApplication(), OrderType.FORCEPICKUP);

                    mainActivityViewModel.setStopsReceived(responseMessage.data, OrderType.FORCEPICKUP);
                    repository.refreshStops(responseMessage.data, OrderType.FORCEPICKUP);

                    //TODO: Block Screen here
                    GlobalCoordinator.getInstance().InvokeLockScreen(MainActivity.this, true);

                }
            }
        });

        mainActivityViewModel.getmError().observe(this, new Observer<DriverServicesError>() {
            @Override
            public void onChanged(DriverServicesError driverServicesError) {
                if (driverServicesError == null) return;
                switch (driverServicesError.errorType) {
                    case runSheetError: {
                        Toast.makeText(MainActivity.this, "Runsheet Print Error: " + driverServicesError.errorMessage, Toast.LENGTH_LONG).show();
                        break;
                    }
                    case settingsError: {
                        Toast.makeText(MainActivity.this, "Settings Error: " + driverServicesError.errorMessage, Toast.LENGTH_LONG).show();

                        break;
                    }
                    case lovActionRoutesError: {
                        Toast.makeText(MainActivity.this, "Action Routes Error: " + driverServicesError.errorMessage, Toast.LENGTH_LONG).show();
                        break;
                    }
                }
                mainActivityViewModel.getmError().setValue(null);
            }
        });
        //GlobalCoordinator.getInstance().InvokeLockScreen(MainActivity.this, true);

    }



    private void observeGlobalBooleans() {
        //In case the screen was locked, and the driver killed the application and started it again, get the lock status from the DB and Invoke screen if locked
        mainActivityViewModel.getLockStatus().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean)
                    GlobalCoordinator.getInstance().InvokeLockScreen(MainActivity.this, true);
            }
        });

        //Make sure to observe the shouldlogin in order to display the login screen if needed
        mainActivityViewModel.getShouldLogin().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) {
                    GlobalCoordinator.getInstance().invokeLoginScreen(MainActivity.this, "Not Logged In");

                }
            }
        });

        GlobalCoordinator.getInstance().shouldLoadHomeScreen.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean shouldLoadHome) {
                if (shouldLoadHome) {

                    NavHostFragment hostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);

                    FragmentTransaction myFragmentTransaction = hostFragment.getChildFragmentManager().beginTransaction();
                    //myFragmentTransaction.remove(loadedFragment);
                    myFragmentTransaction.replace(R.id.nav_host_fragment, new HomeFragment());
                    myFragmentTransaction.addToBackStack(null);
                    myFragmentTransaction.commit();

                    Toolbar toolbar = findViewById(R.id.toolbar);
                    toolbar.setTitle(getString(R.string.menu_home));
                    GlobalCoordinator.getInstance().shouldLoadHomeScreen.setValue(false);
                }
            }
        });
    }

    private void checkInitializeLOVs() {
        if (GlobalCoordinator.getInstance().shouldInitializeLOVs()) {
            //final DriverServices driverServices = new DriverServices(this);
            //driverServices.getLOVActionRoutes();
            mainActivityViewModel.getLOVActionRoutes();


        }


    }

    private void setupTestFloatingButton() {

        if (GlobalCoordinator.testMode) {
            fillTestBarcodes();
        }


        //setup floating action buttons
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();


                 */
                if (GlobalCoordinator.testMode) {
                    if (testBarcodes.size() == 0) {
                        fillTestBarcodes();
                    }
                    sendScannedKeyToFragment(testBarcodes.get(0));
                    testBarcodes.remove(0);

                }
            }
        });

        //if(!GlobalCoordinator.testMode) fab.hide();
        fab.hide();

    }


    private void setupNavigationDrawer() {

        //setup drawer navigation
        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        final NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_checkout, R.id.nav_checkin, R.id.nav_bulkPickup, R.id.nav_liaison, R.id.nav_delivery, R.id.nav_pickup, R.id.nav_adjust, R.id.nav_codlist, R.id.nav_checkstatus, R.id.nav_settings)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

    }

    private void fillTestBarcodes() {
        testBarcodes.add("012782");
        testBarcodes.add("100119912000100000");
        testBarcodes.add("017829");
        testBarcodes.add("9393887737122asdfd");
        testBarcodes.add("012782");
        testBarcodes.add("2882773774884");
        testBarcodes.add("9991127346");
    }



    @Override
    protected void onResume() {
        super.onResume();

        //Log.d("onResume", "1");

        //mTextView.setText("onResume:");
        // creating pending intent:
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        // creating intent receiver for NFC events:
        IntentFilter filter = new IntentFilter();
        filter.addAction(NfcAdapter.ACTION_TAG_DISCOVERED);
        filter.addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
        filter.addAction(NfcAdapter.ACTION_TECH_DISCOVERED);
        // enabling foreground dispatch for getting intent from NFC event:
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter != null)
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, new IntentFilter[]{filter}, this.techList);

        refreshListOfValues();

        getForcePickupsFromServer();

        getSettingsFromServer();

    }

    private void getSettingsFromServer() {
        if (DriverServices.mToken != null && DriverServices.mToken.getValue() != null && DriverServices.mToken.getValue().Token != null && !DriverServices.mToken.getValue().Token.equals(""))
            if (mainActivityViewModel.getShouldLogin() != null && !mainActivityViewModel.getShouldLogin().getValue()) {
                //call web service toget settings\
                //if the settings token is the same as the curreent token, this means that the settings havee already beeen loaded
                if (mainActivityViewModel.currentSettingsToken != null && mainActivityViewModel.currentSettingsToken.getValue() != null
                        && !DriverServices.mToken.getValue().Token.equals(mainActivityViewModel.currentSettingsToken.getValue()))
                    mainActivityViewModel.getSettings();


            }
    }

    //Gets forced pickups from server and saves in DB
    private void getForcePickupsFromServer() {
        if (DriverServices.mToken != null && DriverServices.mToken.getValue() != null && DriverServices.mToken.getValue().Token != null && !DriverServices.mToken.getValue().Token.equals(""))
            if (mainActivityViewModel.getShouldLogin() != null && !mainActivityViewModel.getShouldLogin().getValue()) {
                //call web service to store the pickups?\
                mainActivityViewModel.getForcePickupsFromServer();

            }
    }

    private void refreshListOfValues() {
        checkInitializeLOVs();

        /*
        //if the user is logged in
        if(DriverServices.mToken != null && DriverServices.mToken.getValue() != null && DriverServices.mToken.getValue().Token != null && !DriverServices.mToken.getValue().Token.equals(""))
            if(mainActivityViewModel.getShouldLogin() != null && !mainActivityViewModel.getShouldLogin().getValue())
                return;

         */
    }


    @Override
    protected void onPause() {
        super.onPause();

        // disabling foreground dispatch:
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter != null)
            nfcAdapter.disableForegroundDispatch(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout: {
                mainActivityViewModel.logoutUser();
                break;
            }
            case R.id.action_runsheet: {
                final ConfirmDialog confirmDialog = new ConfirmDialog(this, "Are you sure you want to print?");
                confirmDialog.setConfirmDialogListener(new ConfirmDialog.ConfirmDialogListener() {
                    @Override
                    public void onConfirmClick(View v) {
                        mainActivityViewModel.callRunSheet();
                        confirmDialog.dismiss();
                    }

                    @Override
                    public void onCancelClick(View v) {
                        confirmDialog.dismiss();

                    }
                });
                confirmDialog.show();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }


    //when the user clicks back, if the drawer is open, close it
    //otherwise, call the super.onBackPressed
    @Override
    public void onBackPressed() {

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer != null) {
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                if (GlobalCoordinator.testMode) {
                    if (!sendIntentToFragment(getIntent())) {
                        //super.onBackPressed();
                    }
                } else {
                    //super.onBackPressed();
                }


            }
        }


    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        sendIntentToFragment(intent);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getScanCode() == 0 && event.getCharacters() != null) {
            sendScannedKeyToFragment(event.getCharacters());
        }
        return super.dispatchKeyEvent(event);
    }

    private boolean sendIntentToFragment(Intent intent) {
        NavHostFragment hostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        Fragment loadedFragment = hostFragment.getChildFragmentManager().getPrimaryNavigationFragment();

        if (loadedFragment instanceof OnScanListener) {
            ((OnScanListener) loadedFragment).onNewIntent(intent);
            return true;
        } else
            return false;

    }

    private void sendScannedKeyToFragment(String characters) {
        NavHostFragment hostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        Fragment loadedFragment = hostFragment.getChildFragmentManager().getPrimaryNavigationFragment();
        if (loadedFragment instanceof OnScanListener) {
            ((OnScanListener) loadedFragment).onNewScan(characters);
        }


    }

    public interface OnScanListener {
        void onNewScan(String characters);

        void onNewIntent(Intent intent);

        void onNewLocation(DriverLocation driverLocation);


    }


    public void updateDriverLocation() {
        fusedLocationClient = getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            DriverLocation driverLocation = new DriverLocation(location.getLongitude(), location.getLatitude());
                            mainActivityViewModel.getmDriverLocation().setValue(driverLocation);
                            NavHostFragment hostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
                            Fragment loadedFragment = hostFragment.getChildFragmentManager().getPrimaryNavigationFragment();
                            if (loadedFragment instanceof OnScanListener) {
                                ((OnScanListener) loadedFragment).onNewLocation(driverLocation);
                            }
                        }
                    }
                }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                GlobalCoordinator.getInstance().notifyMessage(MainActivity.this, e.getMessage(), 0);
            }
        });
    }

    public void checkPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ){//Can add more as per requirement

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},
                    123);
        }
    }

    private void setupCameraScanFAB(){

        cameraScanFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCameraScan();
            }
        });

        cameraScanFAB.hide();
    }

    public void showCameraScanFAB(){
        cameraScanFAB.show();
    }

    public void hideCameraScanFAB(){
        cameraScanFAB.hide();
    }

    public void startCameraScan(){
        Intent intent=new Intent(MainActivity.this,LiveBarcodeScanningActivity.class);
        startActivityForResult(intent, BARCODE_ACTIVITY_REQUEST_CODE);// Activity is started with requestCode 2
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        // check if the request code is same as what is passed  here it is 2
        if(requestCode==BARCODE_ACTIVITY_REQUEST_CODE)
        {
            if(data == null) return;
            String message=data.getStringExtra("BARCODE");
            if(message != null && !message.equals("")){
                sendScannedKeyToFragment(message);
            }
        }
    }

}
