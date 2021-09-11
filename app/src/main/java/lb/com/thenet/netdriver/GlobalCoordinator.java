package lb.com.thenet.netdriver;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.net.PortUnreachableException;
import java.security.PublicKey;
import java.sql.Driver;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import lb.com.thenet.netdriver.login.ui.login.LoginActivity;
import lb.com.thenet.netdriver.rooms.entities.DriverRepository;
import lb.com.thenet.netdriver.ui.ConfirmDialog;

import static android.Manifest.permission.CALL_PHONE;

public class GlobalCoordinator {
    //Singleton implementation
    private static final GlobalCoordinator ourInstance = new GlobalCoordinator();

    public static GlobalCoordinator getInstance() {
        return ourInstance;
    }

    public static boolean testMode = false;

    private GlobalCoordinator() {
        mLock = false;
        mLoginDisplayed = false;
        mShouldInitializeLOVs = false;
    }

    //final strings to be used for fixed strings and notification tags
    public static final String TOKEN_KEY = "token";
    public static final String LOG_NOTIFICATION = "Notification::";
    public static final String LOG_FIREBASE = "Firebase::";
    public static final String LOCK_KEY = "lock";
    public static final String DELIVERY_ORDER_STATUS_KEY = "delivery_order_status";
    public static final String PICKUP_ORDER_STATUS_KEY = "pickup_order_status";

    public static final String INFO_TABLE = "info_table";
    public static final String USER_TABLE = "user_table";
    public static final String ORDER_TYPE_TABLE = "ordertype_table";
    public static final String STOP_TABLE = "stop_table";
    public static final String STOP_CONTACT_TABLE = "stop_contact_table";
    public static final String STOP_LABEL_TABLE = "stop_label_table";
    public static final String LOV_ACTION_ROUTE_TABLE = "action_route_lov_table";
    public static final String PROCESS_QUEUE_TABLE = "process_queue_table";

    public static final String DISPLAY_NAME_KEY = "display_name";


    public static final String SERVICE_HEADER_TOKEN = "X-User-Token";
    public static final String SERVICE_HEADER_CONTENT = "Content-Type";
    public static final String SERVICE_HEADER_FUNCTION = "x-functions-key";
    public static final String SERVICE_APPLICATION_JSON = "application/json";

    //this boolean states and tracks if the screen is already locked or not
    private boolean mLock;
    //this boolean states and tracks whether the user needs to login
    private boolean mLoginDisplayed;
    private boolean mShouldInitializeLOVs;
    public int settingsNumberOfStops = 4;
    public String settingsCountryCurrency = "Local";
    public boolean settingsDisableCheckInNFC = false;
    public boolean settingsDisableCheckOutNFC = false;
    public boolean settingsDisableDeliveryNFC = false;
    public boolean settingsDisableStartStop = false;

    public int configNumberOfRetries = 60;

    public static void CallOnClick(View phoneView,final Activity activity) {
        phoneView.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                String phone_no = ((TextView) v).getText().toString().replaceAll("-", "");
                phone_no = phone_no.replace("+", "00");
                phone_no = phone_no.replace(".", "");
                //if(phone_no.length() <= 8) phone_no = "00961"+phone_no;

                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + phone_no));
                //callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                if (activity.checkSelfPermission(CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    Activity#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for Activity#requestPermissions for more details.
                    String[] permissionsCall = {CALL_PHONE};
                    ActivityCompat.requestPermissions(activity, permissionsCall, 0);
                    return;
                }


                activity.startActivity(callIntent);
            }
        });

    }

    public static String getNowFormatted() {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
        String dateTime = sdf.format(new Date());
        return dateTime;
    }
    /*
    private MutableLiveData<Boolean> lockStatus = new MutableLiveData<>(false);
    public MutableLiveData<Boolean>

     */
    public MutableLiveData<Boolean> shouldLoadHomeScreen = new MutableLiveData<>(false);

    public void setUnlocked(){
        mLock = false;

    }

    public boolean isLocked(){
        return mLock;
    }


    //getFromServer is passed when the lock is invoked but the data is not surely saved (e.g. from a push notification)
    public void InvokeLockScreen(Context context, boolean getFromServer){
        //check the mLock status to make sure not to invote the lock screen twice
        if(!mLock){
            mLock = true;
            Intent blockIntent = new Intent(context, BlockUIActivity.class);
            blockIntent.putExtra("getFromServer", getFromServer);
            blockIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(blockIntent);
        }
    }

    public void invokeLoginScreen(Context context, String reason){
        //check the shouldlogin status to make sure not to invoke the login screen twice
        if(!mLoginDisplayed){
            mLoginDisplayed = true;
            Intent loginIntent = new Intent(context, LoginActivity.class);
            //TODO:add extras to display to the user why s/he is asked to login

            //Set the FLAG_ACTIVITY_NEW_TASK in order to allow the login screen to be displayed from a background thread
            //the reason for that is to support a feature of logging out the user from a notification
            loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(loginIntent);
        }
    }

    public void setmLoginDisplayed(boolean loginDisplayed){
        mLoginDisplayed = loginDisplayed;
    }

    public void setmShouldInitializeLOVs(boolean mShouldInitializeLOVs) {
        this.mShouldInitializeLOVs = mShouldInitializeLOVs;
    }

    public boolean shouldInitializeLOVs(){return mShouldInitializeLOVs;}

    //TODO: Modify this to take specifically to the job in the intent data
    public void checkIntentForJobs(Intent intent, Context context){
        if(intent.getExtras() != null){
            for (String key : intent.getExtras().keySet()) {
                Object value = intent.getExtras().get(key);
                //Log.d(LOG_NOTIFICATION+this.getClass().getName(),"Key: " + key + " Value: " + value);

            }
        }
    }

    public void checkFirebaseToken(final Context context){
        //get the FireBase notification token
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            //Log.w(GlobalCoordinator.LOG_FIREBASE + this.getClass().getName(), "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        String token = task.getResult().getToken();

                        // Log and toast
                        //String msg = getString(R.string.msg_token_fmt, token);
                        //Log the notification token generated
                        //Log.d(GlobalCoordinator.LOG_NOTIFICATION + this.getClass().getName(), "Token Generated: " + token);
                        //Toast.makeText(MainActivity.this, token, Toast.LENGTH_SHORT).show();
                        DriverRepository driverRepository = new DriverRepository(context);
                        driverRepository.insertNewToken(token);
                    }
                });
    }

    public String getServiceUrl(String functionName){
        return "https://netdrivermobileapp.azurewebsites.net/api/v1/" + functionName + "/";
    }



    public String getBuildingCode(String nfcCode){
        if(nfcCode.isEmpty()) return "80 5c 03 c2 6b 30 04";
        else
            return nfcCode;
        /*
        if(testMode) return "X78Nu";
        else return nfcCode;

         */
    }

    //Helper Methods
    public void beepScanned(Context c) {
        try {
            final ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 150);
            toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 150);

            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (toneGen1 != null) {
                        //Log.d(this.getClass().getName(), "ToneGenerator released");
                        toneGen1.release();
                    }
                }

            }, 200);
        }catch (Exception e) {
            //Log.d(this.getClass().getName(), "Exception while playing sound:" + e);
        }

    }

    public void notifyMessage(Context c, String message, int dismissAfter){
        notifyMessage(c, message,dismissAfter,true);
    }
    public void notifyMessage(Context c, String message, int dismissAfter, boolean withImage) {
        final ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 250);

        try{
        toneGen1.startTone(ToneGenerator.TONE_SUP_ERROR,150);
        Vibrator vibe = (Vibrator) c.getSystemService(Context.VIBRATOR_SERVICE);
        vibe.vibrate(150);

            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (toneGen1 != null) {
                        //Log.d(this.getClass().getName(), "ToneGenerator released");
                        toneGen1.release();
                    }
                }

            }, 200);
        }catch (Exception e) {
            //Log.d(this.getClass().getName(), "Exception while playing sound:" + e);
        }


        final ConfirmDialog confirmDialog = new ConfirmDialog(c, message);

        confirmDialog.setConfirmDialogListener(new ConfirmDialog.ConfirmDialogListener() {
            @Override
            public void onConfirmClick(View v) {

                confirmDialog.dismiss();
            }

            @Override
            public void onCancelClick(View v) {
                confirmDialog.dismiss();

            }
        });
        confirmDialog.show();
        confirmDialog.setImageAction(R.mipmap.handstop);
        if(!withImage) confirmDialog.hideImageAction();


        if(dismissAfter > 0) {
            // Hide after some seconds
            final Handler handler = new Handler();
            final Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    if (confirmDialog.isShowing()) {
                        confirmDialog.dismiss();
                    }
                }
            };

            confirmDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    handler.removeCallbacks(runnable);
                }
            });

            handler.postDelayed(runnable, dismissAfter * 1000);
        }
    }

    public void notifySpecialDelivery(Context c){
        try {
            final ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 250);

        toneGen1.startTone(ToneGenerator.TONE_SUP_ERROR,150);
        Vibrator vibe = (Vibrator) c.getSystemService(Context.VIBRATOR_SERVICE);
        vibe.vibrate(300);
            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (toneGen1 != null) {
                        //Log.d(this.getClass().getName(), "ToneGenerator released");
                        toneGen1.release();
                    }
                }

            }, 200);
        }catch (Exception e) {
            //Log.d(this.getClass().getName(), "Exception while playing sound:" + e);
        }

    }


    public long getLongToday() {
        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

        Date todayWithZeroTime = new Date();

        try{
            todayWithZeroTime = formatter.parse(formatter.format(new Date()));}
        catch (Exception ex){}
        //long now = TypeConverters.dateToTimestamp(new Date());
        long today = TypeConverters.dateToTimestamp(todayWithZeroTime);
        //long diff = now - today;
        if(testMode)
            today = today - 23 * 24 * 60 * 60 * 1000;
        return today;
    }
}
