package lb.com.thenet.netdriver.rooms.entities;


import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.Date;
import java.util.List;

import lb.com.thenet.netdriver.GlobalCoordinator;
import lb.com.thenet.netdriver.onlineservices.DriverServices;
import lb.com.thenet.netdriver.onlineservices.json.LoggedInUserData;
import lb.com.thenet.netdriver.onlineservices.json.OrderType;
import lb.com.thenet.netdriver.rooms.entities.info.Info;
import lb.com.thenet.netdriver.rooms.entities.info.InfoDao;
import lb.com.thenet.netdriver.rooms.entities.stops.Stop;
import lb.com.thenet.netdriver.rooms.entities.stops.StopDao;
import lb.com.thenet.netdriver.rooms.entities.user.User;
import lb.com.thenet.netdriver.rooms.entities.user.UserDao;

/**
 * This class holds the implementation code for the methods that interact with the database.
 * Using a repository allows us to group the implementation methods together,
 * and allows the WordViewModel to be a clean interface between the rest of the app
 * and the database.
 *
 * For insert, update and delete, and longer-running queries,
 * you must run the database interaction methods in the background.
 *
 * Typically, all you need to do to implement a database method
 * is to call it on the data access object (DAO), in the background if applicable.
 */
public class DriverRepository implements asyncTaskListener{

    private InfoDao mInfoDao;
    private UserDao mUserDao;
    private StopDao mStopDao;
    //private StopContactDao mStopContactDao;
    //private StopLabelDao mStopLabelDao;


//    private LiveData<List<Info>> mAllInfo;
//    public String mLatestToken;
    public MutableLiveData<String> mLatestToken;

    public MutableLiveData<User> mLoggedInUser;

    //make this static to have one instance regardless of the instances of theDriverRepository
    public static MutableLiveData<Boolean> mLockStatus = new MutableLiveData<>();
    public static MutableLiveData<Boolean> mShouldLogin = new MutableLiveData<>();
    //public static MutableLiveData<Boolean> mDeliveryOrderStatus;// = new MutableLiveData<>();
    public static MutableLiveData<ScannedLocation> scannedLocation = new MutableLiveData<>();


    private static boolean lockStatusInitialized = false;
    private static boolean loginStatusInitialized = false;

    private final Context mContent;

    public LiveData<List<Stop>> mForceAssignedStops;
    public LiveData<Integer> mForceAssignedCount;

    public DriverRepository(Context context) {
        mContent = context;
        DriverRoomDatabase db = DriverRoomDatabase.getDatabase(context);
        mInfoDao = db.infoDao();
        mUserDao = db.userDao();
        mStopDao = db.stopDao();
        //mStopContactDao = db.stopContactDao();
        //mStopLabelDao = db.stopLabelDao();

//        mAllInfo = mInfoDao.getAllInfo();
        mLatestToken = new MutableLiveData<>();
        mLoggedInUser = new MutableLiveData<>();

        getLatestToken();
        initializeLockStatus();
        initializeLoginStatus();
        initializeOrdersStatus();



    }

    /*
    public LiveData<List<Info>> getAllInfo() {
        return mAllInfo;
    }


     */

    boolean forceAssignedStopsInitialized = false;
    public void initializeForcedAssignedStopsFromDB(){
        if(!forceAssignedStopsInitialized) {
            mForceAssignedStops = mStopDao.getForcedAssignedStops(DriverServices.mToken.getValue().UserName, GlobalCoordinator.getInstance().getLongToday());
            mForceAssignedCount = mStopDao.countForcedAssignedStops(DriverServices.mToken.getValue().UserName, GlobalCoordinator.getInstance().getLongToday());
            forceAssignedStopsInitialized = true;
        }
    }

    public void UpdateStop(Stop stop){
        new updateStopAsync(mStopDao).execute(stop);
    }

    //Token Handling
    private void getLatestToken() {
        new getLatestTokenAsyncTask(this).execute();
    }

    public void insertNewToken(String token) {
        Info info = new Info(GlobalCoordinator.TOKEN_KEY,token);
        new deleteAndInsertNewToken(mInfoDao).execute(info);
        //mLatestToken.setValue(token);
    }

    //Lock Status Handling
    private void initializeLockStatus(){
        if(!lockStatusInitialized) {
            new getLockStatusAsyncTask(this).execute();
            lockStatusInitialized = true;
        }
    }

    public void updateLockStatus(Boolean lockStatus){
        if(mLockStatus.getValue() == lockStatus) return;
        String lockValue = lockStatus ? "1" : "0";
        Info info = new Info(GlobalCoordinator.LOCK_KEY,lockValue);
        new getAndUpdateLockStatus(mInfoDao).execute(info);
        mLockStatus.setValue(lockStatus);
    }

    //login status handling
    private void initializeLoginStatus(){
        if(!loginStatusInitialized){
            new getLoginStatusAsyncTask(this).execute();
            loginStatusInitialized = true;
        }
    }

    private void initializeOrdersStatus(){

        new getOrdersStatusAsyncTask(this, OrderType.DELIVERY).execute();
        new getOrdersStatusAsyncTask(this, OrderType.PICKUP).execute();

    }


    public void addLoggedInUser(User user){

        new loginNewUserAsyncTask(this).execute(user);

        //mShouldLogin.setValue(!user.getIsLoggedIn());
    }

    public void removeLoggedInUser(){

        forceAssignedStopsInitialized = false;
        DriverServices.mToken.setValue(null);
        new logoutUserAsyncTask(this).execute();
    }

    public void updateInfo(Info info)  {
        new updateInfoAsyncTask(mInfoDao).execute(info);
    }


    // Must run off main thread
    public void deleteInfo(Info info) {
        new deleteInfoAsyncTask(mInfoDao).execute(info);
    }

    //call back when the query for the latest token returns
    @Override
    public void processLatestToken(String token) {
        mLatestToken.setValue(token);
    }

    @Override
    public void processLockStatus(Boolean lockStatus) {
        mLockStatus.setValue(lockStatus);
    }

    //necessary to pass it to the async task in order to do the query
    @Override
    public InfoDao getInfoDao(){
        return mInfoDao;
    }

    @Override
    public UserDao getUserDao() {
        return mUserDao;
    }

    @Override
    public void processLoggedInUser(User user) {
        if(user != null) {
            mLoggedInUser.setValue(user);
            mShouldLogin.setValue(!user.getIsLoggedIn());
            //DriverServices.mToken.setValue(GlobalCoordinator.getInstance().getUserToken());
        }else   {
            mShouldLogin.setValue(true);
        }
    }

    /*
    @Override
    public StopDao getStopDao() {
        return mStopDao;
    }

    @Override
    public StopContactDao getStopContactDao() {
        return mStopContactDao;
    }

    @Override
    public StopLabelDao getStopLabelDao() {
        return mStopLabelDao;
    }


     */

    // Static inner classes below here to run database interactions in the background.

    private static class deleteAndInsertNewToken extends AsyncTask<Info, Void, Void> {
        private InfoDao mAsyncTaskDao;
        private Info newInfo;

        deleteAndInsertNewToken(InfoDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Info... params) {
            newInfo = params[0];
            mAsyncTaskDao.deleteAllTokens();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            new insertInfoAsyncTask(mAsyncTaskDao).execute(newInfo);
        }
    }

    private static class getAndUpdateLockStatus extends AsyncTask<Info, Void, Info>{
        private InfoDao mAsyncTaskDao;
        private Info newInfo;

        getAndUpdateLockStatus(InfoDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Info doInBackground(final Info... params) {
            newInfo = params[0];
            //Log.d(this.getClass().getName(), "New Lock Status to be set: " + newInfo.getInfoValue());
            Info[] lockInfo = mAsyncTaskDao.getLockStatus();
            if (lockInfo != null && lockInfo.length > 0) {
                //Log.d(this.getClass().getName(), "Retrieved Lock Status: " + lockInfo[0].getInfoValue());
                return lockInfo[0];
            } else {

                //Log.d(this.getClass().getName(),"No Lock Status Found" );
                return new Info(GlobalCoordinator.LOCK_KEY, "0");
            }
        }

        @Override
        protected void onPostExecute(Info oldInfo) {
            newInfo.setId(oldInfo.getId());
            //Log.d(this.getClass().getName(),"calling to update the lock status to: " + newInfo.getInfoValue());
            new updateInfoAsyncTask(mAsyncTaskDao).execute(newInfo);
        }
    }

        /**
     * Inserts info into the database.
     */
    private static class insertInfoAsyncTask extends AsyncTask<Info, Void, Void> {

        private InfoDao mAsyncTaskDao;

        insertInfoAsyncTask(InfoDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Info... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }

    }


    /**
     *  Deletes a single word from the database.
     */
    private static class deleteInfoAsyncTask extends AsyncTask<Info, Void, Void> {
        private InfoDao mAsyncTaskDao;

        deleteInfoAsyncTask(InfoDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Info... params) {
            mAsyncTaskDao.deleteInfo(params[0]);
            return null;
        }
    }

    /**
     *  Updates a word in the database.
     */
    private static class updateInfoAsyncTask extends AsyncTask<Info, Void, Void> {
        private InfoDao mAsyncTaskDao;

        updateInfoAsyncTask(InfoDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Info... params) {
            mAsyncTaskDao.update(params[0]);
            return null;
        }
    }

    private static class getLockStatusAsyncTask extends AsyncTask<Void, Void, Info> {
        private asyncTaskListener mlistener;
        private Info mLockStatus;
        getLockStatusAsyncTask(asyncTaskListener listener) {
            mlistener = listener;
        }

        @Override
        protected Info doInBackground( Void... voids) {
            //TODO: if no user is logged in, set lock to false

            //for first time open, the tokens table might be empty
            Info[] lockStatusInfo = mlistener.getInfoDao().getLockStatus();
            if(lockStatusInfo != null && lockStatusInfo.length != 0) {
                //Log.d(getClass().getName(),"Getting Lock Status: " + lockStatusInfo[0].getInfoValue());
                mLockStatus = lockStatusInfo[0];
            }
            else {
                mLockStatus = new Info(GlobalCoordinator.LOCK_KEY, "0");
                //Log.d(getClass().getName(),"Getting Lock Status: Not found!!!");

            }
            return mLockStatus;
        }

        @Override
        protected void onPostExecute(Info result) {

            String strLockStatus = result.getInfoValue();
            if (strLockStatus != null && !strLockStatus.isEmpty() && strLockStatus.equals("1")) {
                //Log.d(getClass().getName(), "Converting to true");
                mlistener.processLockStatus(true);
            } else {
                mlistener.processLockStatus(false);
                //Log.d(getClass().getName(), "Converting to false");
            }
        }
    }


    private static class getLatestTokenAsyncTask extends AsyncTask<Void, Void, Info> {
        private asyncTaskListener mlistener;
        private Info mLatestToken;
        getLatestTokenAsyncTask(asyncTaskListener listener) {
            mlistener = listener;
        }

        @Override
        protected Info doInBackground( Void... voids) {
            //for first time open, the tokens table might be empty
            Info[] latestTokenInfo = mlistener.getInfoDao().getLatestToken();
            if(latestTokenInfo != null && latestTokenInfo.length != 0)
                mLatestToken = latestTokenInfo[0];
            else
                mLatestToken = new Info(GlobalCoordinator.TOKEN_KEY,"NA");
            return mLatestToken;
        }

        @Override
        protected void onPostExecute(Info result){

            mlistener.processLatestToken(result.getInfoValue());
        }
    }

    private static class loginNewUserAsyncTask extends AsyncTask<User, Void, Void>{
        private asyncTaskListener mlistener;
        private User mUser;
        loginNewUserAsyncTask(asyncTaskListener listener) {
            mlistener = listener;
        }
        @Override
        protected Void doInBackground( User... params) {
            mUser = params[0];

            mlistener.getUserDao().setUserLogout();
            mlistener.getUserDao().insert(mUser);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mlistener.processLoggedInUser(mUser);
        }

    }

    private static class getOrdersStatusAsyncTask extends AsyncTask<Void, Void, Boolean>{

        private asyncTaskListener mlistener;
        private OrderType orderType;

        getOrdersStatusAsyncTask(asyncTaskListener listener, OrderType orderType){
            mlistener = listener;
            this.orderType = orderType;
        }
        @Override
        protected Boolean doInBackground(Void... voids) {
            switch (orderType){
                case DELIVERY: {
                    Info[] status = mlistener.getInfoDao().getDeliveryOrderStatus();
                    if (status != null && status.length > 0) {
                        String s = status[0].getInfoValue();
                        if (!s.isEmpty() && s.equals("1"))
                            return true;
                    }
                    break;
                }
                case PICKUP:{

                    //Info pickupOderStatusInfo = new Info(GlobalCoordinator.PICKUP_ORDER_STATUS_KEY, "0");
                    //mlistener.getInfoDao().insert(pickupOderStatusInfo);

                    Info[] status = mlistener.getInfoDao().getPickupOrderStatus();
                    if (status != null && status.length > 0) {
                        String s = status[0].getInfoValue();
                        if (!s.isEmpty() && s.equals("1"))
                            return true;
                    }
                    break;
                }
            }


            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            switch (orderType){
                case DELIVERY:{
                    DriverServices.deliveryOrdersLoaded.setValue(aBoolean);
                    break;
                }
                case PICKUP:{
                    DriverServices.pickupOrdersLoaded.setValue(aBoolean);
                    break;
                }
            }
        }
    }
    private static class getLoginStatusAsyncTask extends AsyncTask<Void, Void, User>{
        private asyncTaskListener mlistener;
        private User mUser;
        getLoginStatusAsyncTask(asyncTaskListener listener) {
            mlistener = listener;
        }

        @Override
        protected User doInBackground( Void... voids) {
            //for first time open, the tokens table might be empty
            User[] users = mlistener.getUserDao().getUser();
            if(users != null && users.length != 0) {
                //Log.d(getClass().getName(),"Getting Logged User: " + users[0].getDisplayName() + " >> " + users[0].getIsLoggedIn());
                mUser = users[0];
                //here - set the user token in
            }
            else {
                mUser = new User("NA", "", false, new Date().toString(), "");
                //Log.d(getClass().getName(),"Getting Logged User: Not found!!!");

            }
            return mUser;
        }

        @Override
        protected void onPostExecute(User result) {
            mlistener.processLoggedInUser(mUser);
            LoggedInUserData tempData = DriverServices.mToken.getValue();
            if(tempData == null) tempData = new LoggedInUserData();
            tempData.Token = mUser.getUserToken();
            tempData.DisplayName = mUser.getDisplayName();
            tempData.UserName = mUser.getUserName();
            DriverServices.mToken.setValue(tempData);

            //DriverServices.mToken.setValue(mUser.getUserToken());

        }
    }

    private static class logoutUserAsyncTask extends AsyncTask<Void, Void, Void>{
        private asyncTaskListener mlistener;
        logoutUserAsyncTask(asyncTaskListener listener) {
            mlistener = listener;
        }
        @Override
        protected Void doInBackground( Void... voids) {

            //mlistener.getStopLabelDao().deleteAllStopLabelsofType(OrderType.DELIVERY.getOrderTypeId());
            //mlistener.getStopContactDao().deleteAllStopContactsofType(OrderType.DELIVERY.getOrderTypeId());
            //mlistener.getStopDao().deleteAllStopsofType(OrderType.DELIVERY.getOrderTypeId());
            mlistener.getUserDao().setUserLogout();
            mlistener.getInfoDao().resetLock();
            mlistener.getInfoDao().updateDeliveryOrderStatus("0");
            mlistener.getInfoDao().updatePickupOrderStatus("0");

            //TODO: Reset all other values

            return null;
        }


    }


    private static class updateStopAsync extends AsyncTask<Stop, Void, Void> {

        private StopDao stopDao;
        public updateStopAsync(StopDao stopDao){
            this.stopDao = stopDao;
        }
        @Override
        protected Void doInBackground(Stop... stops) {
            stopDao.update(stops[0]);
            return null;
        }
    }
}
