package lb.com.thenet.netdriver;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

import lb.com.thenet.netdriver.notificationtypes.updateCODType;
import lb.com.thenet.netdriver.onlineservices.json.OrderType;
import lb.com.thenet.netdriver.onlineservices.json.ResponseMessage;
import lb.com.thenet.netdriver.rooms.entities.DriverRepository;
import lb.com.thenet.netdriver.rooms.entities.OrderRepository;

public class MyFirebaseMessagingService extends FirebaseMessagingService {




    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(String token) {
        //Log.d(GlobalCoordinator.LOG_NOTIFICATION + this.getClass().getName(), "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        //sendRegistrationToServer(token);


        //TODO: make sure the insert new token doesn't happen on background thread
        DriverRepository driverRepository = new DriverRepository(this.getApplicationContext());
        driverRepository.insertNewToken(token);

    }


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // ...

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        //Log.d(GlobalCoordinator.LOG_NOTIFICATION + this.getClass().getName(), "Message Received From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.

        if (remoteMessage.getData().size() > 0) {
            //Log.d(GlobalCoordinator.LOG_NOTIFICATION + this.getClass().getName(), "Message data payload: " + remoteMessage.getData());

            if (/* Check if data needs to be processed by long running job */ true) {
                // For long-running tasks (10 seconds or more) use WorkManager.

            } else {
                // Handle message within 10 seconds


            }
        }

        switch (remoteMessage.getData().get("type")){
            case "updateCOD":
                Type notificationMessage = new TypeToken<updateCODType>(){}.getType();
                Gson converter = new Gson();
                updateCODType message = converter.fromJson(remoteMessage.getData().get("data"),notificationMessage);
                OrderRepository repository = new OrderRepository(this.getApplication(), OrderType.ALL);
                repository.updateOrderCOD(message);
                break;
        }




        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            //Log.d(GlobalCoordinator.LOG_NOTIFICATION + this.getClass().getName(), "Message Notification Body: " + remoteMessage.getNotification().getBody());

        }




        /*

         */



        GlobalCoordinator.getInstance().InvokeLockScreen(this.getApplicationContext(), true);



    }


}
