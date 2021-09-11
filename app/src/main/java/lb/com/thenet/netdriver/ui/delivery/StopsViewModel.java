package lb.com.thenet.netdriver.ui.delivery;

import android.app.Application;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.annotation.StringRes;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import lb.com.thenet.netdriver.BaseViewModel;
import lb.com.thenet.netdriver.GlobalCoordinator;
import lb.com.thenet.netdriver.R;
import lb.com.thenet.netdriver.onlineservices.DriverServices;
import lb.com.thenet.netdriver.onlineservices.DriverServicesError;
import lb.com.thenet.netdriver.onlineservices.json.JsonStop;
import lb.com.thenet.netdriver.onlineservices.json.NetLockerData;
import lb.com.thenet.netdriver.onlineservices.json.OrderType;
import lb.com.thenet.netdriver.onlineservices.json.ResponseMessage;
import lb.com.thenet.netdriver.onlineservices.json.ScanBuilding;
import lb.com.thenet.netdriver.onlineservices.json.SendLabelItem;
import lb.com.thenet.netdriver.onlineservices.json.SendDeliveryRequest;
import lb.com.thenet.netdriver.onlineservices.json.SendPickupRequest;
import lb.com.thenet.netdriver.rooms.entities.LOVRepository;
import lb.com.thenet.netdriver.rooms.entities.OrderRepository;
import lb.com.thenet.netdriver.rooms.entities.lov.ActionRoute;
import lb.com.thenet.netdriver.rooms.entities.stops.Stop;
import lb.com.thenet.netdriver.rooms.entities.stops.StopContact;
import lb.com.thenet.netdriver.rooms.entities.stops.StopLabel;

public class StopsViewModel extends BaseViewModel {

    private DriverServices mDriverServices;
    private MutableLiveData<DriverServicesError> mError;
    private MutableLiveData<ResponseMessage<JsonStop[]>> mServerStops;

    //private MutableLiveData<Integer> mTotalStops = new MutableLiveData<>(0);
    //private MutableLiveData<Integer> mDoneStops = new MutableLiveData<>(0);
    //private MutableLiveData<Integer> mPendingStops = new MutableLiveData<>(0);

    //TODO: Delete these two test data: mTest and setTF
    private MutableLiveData<String> mTest;
    private MutableLiveData<Boolean> setTF = new MutableLiveData<>(false);


    private OrderRepository mOrderRepository;
    private LiveData<List<Stop>> mLocalStops;
    private LiveData<Integer> mAllStopCount;
    private LiveData<Integer> mCompletedStopCount;
    private LiveData<Integer> mInCompletedStopCount;
    private LiveData<Integer> mStartedStopsCount;
    private LiveData<Integer> mNotStartedStopsCount;

    private LiveData<Integer> mInCompleteLabelCount;
    private LiveData<Integer> mCompleteLabelCount;


    //This live data is to load the specific fragment. It is being watched by the navigator fragment and loads the needed fragment accordingly
    private MutableLiveData<StopFragments> fragmentToLoad = new MutableLiveData<>(StopFragments.StopsFragment);
    private MutableLiveData<Stop> selectedStop = new MutableLiveData<>();
    //private MutableLiveData<List<StopContact>> selectedStopContacts = new MutableLiveData<>();
    private MutableLiveData<StopContact> selectedStopContact = new MutableLiveData<>();
    //private MutableLiveData<List<StopLabel>> selectedStopLabels = new MutableLiveData<>();
    private MutableLiveData<StopLabel> selectedStopLabel = new MutableLiveData<>();

    private MutableLiveData<ResponseMessage<Boolean>> mStartStopResponse;
    private MutableLiveData<ResponseMessage<Boolean>> mStopStopResponse;
    private MutableLiveData<ResponseMessage<Boolean>> mAcceptStopResponse;
    private MutableLiveData<ResponseMessage<Boolean>> mRejectStopResponse;
    private MutableLiveData<ResponseMessage<Boolean>> mSendDeliveryResponse;
    private MutableLiveData<ResponseMessage<Boolean>> mSendNonDeliveryResponse;
    private MutableLiveData<ResponseMessage<Boolean>> mSendPickupResponse;
    private MutableLiveData<ResponseMessage<Boolean>> mSendNonPickupResponse;
    private MutableLiveData<ResponseMessage<Boolean>> mSendSignatureResponse;
    private MutableLiveData<ResponseMessage<NetLockerData>> mNetLockerResponse;
    private MutableLiveData<ResponseMessage<Boolean>> mRetourResponse;

    private LOVRepository mLOVRepository;
    private LiveData<List<lb.com.thenet.netdriver.rooms.entities.lov.ActionRoute>> mNonDeliveryActionRoutes;
    private LiveData<List<lb.com.thenet.netdriver.rooms.entities.lov.ActionRoute>> mNonPickupActionRoutes;
    private LiveData<List<lb.com.thenet.netdriver.rooms.entities.lov.ActionRoute>> mRejectActionRoutes;


    private MutableLiveData<ResponseMessage<ScanBuilding>> mBuildingNFC;
    private OrderType orderType;


    public StopsViewModel(Application application) {
        super(application);


        mDriverServices = new DriverServices(application.getApplicationContext());
        mError = mDriverServices.mError;
        mTest = new MutableLiveData<>();
        mTest.setValue("Initial Value");

        mServerStops = mDriverServices.mStopsResponse;

        //Check if the stops are already loaded from server
        //and check if they are loaded within the past 10 minutes in order not to load them again



        mStartStopResponse = mDriverServices.mStartStopResponse;
        mStopStopResponse = mDriverServices.mStopStopResponse;
        mAcceptStopResponse = mDriverServices.mAcceptStopResponse;
        mRejectStopResponse = mDriverServices.mRejectStopResponse;

        mSendDeliveryResponse = mDriverServices.mSendDeliveryResponse;
        mSendNonDeliveryResponse = mDriverServices.mSendNonDeliveryResponse;
        mSendPickupResponse = mDriverServices.mSendPickupResponse;
        mSendNonPickupResponse = mDriverServices.mSendNonPickupResponse;
        mSendSignatureResponse = mDriverServices.mSendSignatureResponse;
        mNetLockerResponse = mDriverServices.mNetLockerResponse;

        mBuildingNFC = mDriverServices.mBuildingNFC;
        mRetourResponse = mDriverServices.mRetourResponse;


    }

    public OrderType getOrderType() {
        return orderType;
    }

    public void setOrderType(OrderType orderType) {
        this.orderType = orderType;

        mOrderRepository = new OrderRepository(getApplication(), orderType);
        mLocalStops = mOrderRepository.getmAllStops();
        mAllStopCount = mOrderRepository.getmAllStopCount();
        mCompletedStopCount = mOrderRepository.getmCompletedStopCount();
        mInCompletedStopCount = mOrderRepository.getmInCompletedStopCount();
        mStartedStopsCount = mOrderRepository.getmStartedStopsCount();
        mNotStartedStopsCount = mOrderRepository.getmNotStartedStopsCount();
        mInCompleteLabelCount = mOrderRepository.getmInCompleteLabelCount();
        mCompleteLabelCount = mOrderRepository.getmCompleteLabelCount();

        mLOVRepository = new LOVRepository(getApplication());
        mNonDeliveryActionRoutes = mLOVRepository.getmNonDeliveryActionRoutes();
        mNonPickupActionRoutes = mLOVRepository.getmNonPickupActionRoutes();
        mRejectActionRoutes = mLOVRepository.getmRejectActionRoutes();
    }


    public void checkLoadStopsFromServer(){
        if(shouldLoadStopsFromServer())
            GetStopsFromServer();
    }


    //TODO: HERE set the should load delivery stops
    private boolean shouldLoadStopsFromServer() {
        if(GlobalCoordinator.testMode) return false;
        switch (orderType){
            case DELIVERY:{
                boolean ordersLoaded = DriverServices.deliveryOrdersLoaded.getValue();
                if(!ordersLoaded) return true;
                Date dateLoaded = DriverServices.deliveryOrdersTimeStamp.getValue();
                if(dateLoaded == null) return true;
                Date currentTime = new Date();
                long millisecs = currentTime.getTime() - dateLoaded.getTime();
                Integer refreshMinutes = getApplication().getResources().getInteger(R.integer.ordersLoadValidityMinutes);
                if (millisecs > refreshMinutes * 60 * 1000) return true;
                break;
            }
            case PICKUP:{
                return true;
                /*
                boolean ordersLoaded = DriverServices.pickupOrdersLoaded.getValue();
                if(!ordersLoaded) return true;
                Date dateLoaded = DriverServices.pickupOrdersTimeStamp.getValue();
                if(dateLoaded == null) return true;
                Date currentTime = new Date();
                long millisecs = currentTime.getTime() - dateLoaded.getTime();
                Integer refreshMinutes = getApplication().getResources().getInteger(R.integer.ordersLoadValidityMinutes);
                if (millisecs > refreshMinutes * 60 * 1000) return true;
                break;

                 */
            }
        }

        return false;


    }

    public void DeepLoadStop(Stop stop){ mOrderRepository.deepLoadStop(stop);}
    public void DeepLoadContact(StopContact stopContact){
        mOrderRepository.deepLoadStopContact(stopContact);
    }

    Date lastRefreshed;

    public boolean GetStopsFromServer(){

        if(lastRefreshed != null) {
            Date currentTime = new Date();
            long millisecs = currentTime.getTime() - lastRefreshed.getTime();
            Integer refreshSeconds = 25;
            if (millisecs <= refreshSeconds * 1000) return false;
        }

        switch (orderType){
            case DELIVERY:{
                DriverServices.deliveryOrdersLoaded.setValue(false);
                mDriverServices.getOrderStops(OrderType.DELIVERY.getOrderTypeId());
                break;
            }
            case PICKUP:{
                DriverServices.pickupOrdersLoaded.setValue(false);
                mDriverServices.getOrderStops(OrderType.PICKUP.getOrderTypeId());
            }
        }
        lastRefreshed = new Date();
        return true;

    }

    public void RefreshStops(){

            setStopsReceived(mServerStops.getValue().data, orderType);
            mOrderRepository.refreshStops(mServerStops.getValue().data, orderType);
        }


    public void UpdateStopLabel(StopLabel stopLabel){
        mOrderRepository.updateStopLabel(stopLabel);

    }

    public void DeleteStopLabel(StopLabel stopLabel){
        mOrderRepository.deleteStopLabel(stopLabel);
    }


    public LiveData<Integer> countIncompleteLabels(String stopId){
        return mOrderRepository.countIncompleteLabelsForStop(stopId);
    }

    public MutableLiveData<DriverServicesError> getmError(){return mError;}
    public DriverServices getmDriverServices(){return mDriverServices;}
    public MutableLiveData<String> getmTest(){return mTest;}
    public MutableLiveData<Boolean> getSetTF(){return setTF;}

    public MutableLiveData<ResponseMessage<JsonStop[]>> getmServerStops() {return mServerStops;}

    public LiveData<List<Stop>> getmLocalStops() {
        return mLocalStops;
    }

    public LiveData<List<ActionRoute>> getmNonDeliveryActionRoutes() {
        return mNonDeliveryActionRoutes;
    }

    public LiveData<List<ActionRoute>> getmNonPickupActionRoutes() {
        return mNonPickupActionRoutes;
    }

    public LiveData<List<ActionRoute>> getmRejectActionRoutes() {
        return mRejectActionRoutes;
    }

    /*
    public MutableLiveData<Integer> getmTotalStops(){return mTotalStops;}
    public MutableLiveData<Integer> getmDoneStops() {
        return mDoneStops;
    }

    public MutableLiveData<Integer> getmPendingStops() {
        return mPendingStops;
    }

     */

    public MutableLiveData<StopFragments> getFragmentToLoad() {
        return fragmentToLoad;
    }

    public MutableLiveData<Stop> getSelectedStop() {
        return selectedStop;
    }

    public MutableLiveData<StopContact> getSelectedStopContact() {
        return selectedStopContact;
    }

    public MutableLiveData<StopLabel> getSelectedStopLabel() {
        return selectedStopLabel;
    }

    public boolean canStartStop(){
        return (getmInCompletedStopCount().getValue() < GlobalCoordinator.getInstance().settingsNumberOfStops);

    }

    public void startStop(double longitude, double latitude) {
        String dateTime = GlobalCoordinator.getNowFormatted();

        mDriverServices.startStop(selectedStop.getValue().stopId, longitude, latitude, dateTime);

        //mDriverServices.actionStop(DriverServices.StopActionType.Start, selectedStop.getValue().stopId, longitude, latitude, dateTime, "");


    }

    public void stopStop(double longitude, double latitude, String reason){
        String dateTime = GlobalCoordinator.getNowFormatted();

        mDriverServices.stopStop(selectedStop.getValue().stopId, longitude, latitude, dateTime, reason);
        //mDriverServices.actionStop(DriverServices.StopActionType.Stop, selectedStop.getValue().stopId, longitude, latitude, dateTime, reason);

    }

    public void acceptStop(double longitude, double latitude){
        String dateTime = GlobalCoordinator.getNowFormatted();

        mDriverServices.acceptStop(selectedStop.getValue().stopId, longitude, latitude, dateTime);
        //mDriverServices.actionStop(DriverServices.StopActionType.Stop, selectedStop.getValue().stopId, longitude, latitude, dateTime, reason);

    }

    public void rejectStop(double longitude, double latitude, String reason){
        String dateTime = GlobalCoordinator.getNowFormatted();

        mDriverServices.rejectStop(selectedStop.getValue().stopId, longitude, latitude, dateTime, reason);
        //mDriverServices.actionStop(DriverServices.StopActionType.Stop, selectedStop.getValue().stopId, longitude, latitude, dateTime, reason);

    }


    public MutableLiveData<ResponseMessage<Boolean>> getmStartStopResponse() {
        return mStartStopResponse;
    }

    public MutableLiveData<ResponseMessage<Boolean>> getmStopStopResponse() {
        return mStopStopResponse;
    }

    public MutableLiveData<ResponseMessage<Boolean>> getmAcceptStopResponse() {
        return mAcceptStopResponse;
    }

    public MutableLiveData<ResponseMessage<Boolean>> getmRejectStopResponse() {
        return mRejectStopResponse;
    }

    public MutableLiveData<ResponseMessage<Boolean>> getmSendDeliveryResponse() {
        return mSendDeliveryResponse;
    }

    public MutableLiveData<ResponseMessage<Boolean>> getmSendNonDeliveryResponse() {
        return mSendNonDeliveryResponse;
    }

    public MutableLiveData<ResponseMessage<Boolean>> getmSendPickupResponse() {
        return mSendPickupResponse;
    }

    public MutableLiveData<ResponseMessage<Boolean>> getmSendNonPickupResponse() {
        return mSendNonPickupResponse;
    }

    public MutableLiveData<ResponseMessage<Boolean>> getmSendSignatureResponse() {
        return mSendSignatureResponse;
    }

    public MutableLiveData<ResponseMessage<NetLockerData>> getmNetLockerResponse() {
        return mNetLockerResponse;
    }



    public MutableLiveData<ResponseMessage<ScanBuilding>> getmBuildingNFC(){return mBuildingNFC;}


    public LiveData<Integer> getmStartedStopsCount() {
        return mStartedStopsCount;
    }

    public LiveData<Integer> getmAllStopCount() {
        return mAllStopCount;
    }

    public LiveData<Integer> getmCompletedStopCount() {
        return mCompletedStopCount;
    }


    public LiveData<Integer> getmInCompletedStopCount() {
        return mInCompletedStopCount;
    }

    public LiveData<Integer> getmNotStartedStopsCount() {
        return mNotStartedStopsCount;
    }

    public LiveData<Integer> getmCompleteLabelCount() {
        return mCompleteLabelCount;
    }

    public LiveData<Integer> getmInCompleteLabelCount() {
        return mInCompleteLabelCount;
    }

    public MutableLiveData<ResponseMessage<Boolean>> getmRetourResponse() {
        return mRetourResponse;
    }

    public void UpdateStop(Stop value) {
        mOrderRepository.updateStop(value);
    }




    //Used for Deliveery
    public void sendDelivery(String nfcBuilding, double longitude, double latitude, String activityDate, LinkedList<SendLabelItem> labels, String recipient, String signature) {
        SendDeliveryRequest sendDeliveryRequest = new SendDeliveryRequest();
        sendDeliveryRequest.labels = new SendLabelItem[labels.size()];

        sendDeliveryRequest.activityDate = activityDate;
        sendDeliveryRequest.latitude = latitude;
        sendDeliveryRequest.longitude = longitude;
        sendDeliveryRequest.nfcBuilding = nfcBuilding;
        sendDeliveryRequest.recipient = recipient;
        sendDeliveryRequest.signature = signature;
        sendDeliveryRequest.orderType = getOrderType();

        for(int i=0; i < labels.size(); i++){
            sendDeliveryRequest.labels[i] = labels.get(i);
        }

        mDriverServices.sendDelivery(sendDeliveryRequest);
    }

    //Used for Pickup
    public void sendPickup(String nfcBuilding, double longitude, double latitude, String activityDate, LinkedList<SendLabelItem> labels, String recipient, String signature) {
        SendPickupRequest sendPickupRequest = new SendPickupRequest();
        sendPickupRequest.labels = new SendLabelItem[labels.size()];

        sendPickupRequest.activityDate = activityDate;
        sendPickupRequest.latitude = latitude;
        sendPickupRequest.longitude = longitude;
        sendPickupRequest.nfcBuilding = nfcBuilding;
        sendPickupRequest.recipient = recipient;
        sendPickupRequest.signature = signature;
        sendPickupRequest.orderType = getOrderType();

        for(int i=0; i < labels.size(); i++){
            sendPickupRequest.labels[i] = labels.get(i);
        }

        mDriverServices.sendPickup(sendPickupRequest);
    }



    public void sendNonDelivery(String nfcBuilding, double longitude, double latitude, String activityDate, String label, String actionRouteId, String orderId) {
        mDriverServices.sendNonDeliveryOffline(orderType, longitude, latitude, activityDate, label, actionRouteId, orderId);
    }

    public void sendSignature(String imageName, byte[] signature){
        mDriverServices.sendSignature(imageName,signature);
    }

    public void insertStopLabel(StopLabel stopLabel){
        mOrderRepository.insertStopLabel(stopLabel);
    }

    public Stop getStopOfLabel(String s) {

        return mOrderRepository.getStopOfLabel(s,getOrderType().getOrderTypeId(),DriverServices.mToken.getValue().UserName,GlobalCoordinator.getInstance().getLongToday());
    }

    public Stop getStopById(String Id){
        return mOrderRepository.getStopById(Id);
    }

    public void retour(String orderId, String retourLabel, String specialInstruction, int amount, String currency, int serviceType, boolean isManual) {
        mDriverServices.retourOffline(orderId, retourLabel, specialInstruction, amount, currency, serviceType, isManual);
    }


    public enum StopFragments{
        StopsFragment,
        StopDetailsFragment,
        StopContactsFragment,
        StopLabelsFragment,
        StopDeliveryFragment,
        StopPickupFragment,
        StopDeliveryFailureFragment,
        StopRejectFragment,
        StopLabelRetourFragment
    }
}
