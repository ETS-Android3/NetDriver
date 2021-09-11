package lb.com.thenet.netdriver.jobscheduler;

import android.app.NotificationManager;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.gson.Gson;

import lb.com.thenet.netdriver.GlobalCoordinator;
import lb.com.thenet.netdriver.rooms.entities.ProcessQueueRepository;
import lb.com.thenet.netdriver.rooms.entities.offline.ProcessQueue;

public class PostToServerWorker extends Worker {



    private NotificationManager notificationManager =
            (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);


    public PostToServerWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);

    }

    @NonNull
    @Override
    public Result doWork() {
        //Log.d("ayusch", Thread.currentThread().toString());
        //displayNotification(ProgressUpdateEvent("Please wait...", 3, 0))
        String jsonToSend = getInputData().getString("jsonToSend");
        String url = getInputData().getString("url");
        String key = getInputData().getString("key");

        //In case the json to send was "DB", this means that we need to get the full string from the Database
        //This means that a "uniqueID" is enqueued for this json
        if(jsonToSend != null && jsonToSend.equals("DB")){
            String uniqueID = getInputData().getString("uniqueID");
            ProcessQueueRepository processQueueRepository = new ProcessQueueRepository(getApplicationContext());
            ProcessQueue queueItem = processQueueRepository.getProcessQueue(uniqueID);
            if(queueItem != null && queueItem.numberOfRetries > GlobalCoordinator.getInstance().configNumberOfRetries)
            {
                return Result.failure();
            }
            jsonToSend = processQueueRepository.getProcessQueueString(uniqueID);


        }
        Gson gson = new Gson();
        CallServicesUtil util = new CallServicesUtil();
        //Type responseType = new TypeToken<LinkedList<ReceivedStop>>(){}.getType();
        //LinkedList<ReceivedStop> stopList =gson.fromJson(imagesJson,responseType);
        boolean success = util.postToServer(getApplicationContext(),url, key, jsonToSend);
        //notificationManager.cancel(notificationId)

        ProcessQueueRepository processQueueRepository = new ProcessQueueRepository(getApplicationContext());

        jsonToSend = getInputData().getString("jsonToSend");
        //Update or Clean the DB
        if(jsonToSend != null && jsonToSend.equals("DB")) {
            String uniqueID = getInputData().getString("uniqueID");
            if (success) {

                //Todo: updatee the sstop status?

                processQueueRepository.deleteProcessQueueWithName(uniqueID);

            } else {
                long today = GlobalCoordinator.getInstance().getLongToday();
                processQueueRepository.updateProcessQueueWithFailure(uniqueID,today);
            }
        }

        return success?Result.success():Result.retry();
    }




}
