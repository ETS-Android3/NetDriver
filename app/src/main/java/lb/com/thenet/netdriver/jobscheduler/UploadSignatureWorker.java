package lb.com.thenet.netdriver.jobscheduler;

import android.app.NotificationManager;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.gson.Gson;

import lb.com.thenet.netdriver.GlobalCoordinator;
import lb.com.thenet.netdriver.rooms.entities.ProcessQueueRepository;

public class UploadSignatureWorker extends Worker {
    public UploadSignatureWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    private NotificationManager notificationManager =
            (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);


    @NonNull
    @Override
    public Result doWork() {

        //Log.d("ayusch", Thread.currentThread().toString());
        //displayNotification(ProgressUpdateEvent("Please wait...", 3, 0))
        String itemName = getInputData().getString("itemName");
        ProcessQueueRepository processQueueRepository = new ProcessQueueRepository(getApplicationContext());
        String imageString = processQueueRepository.getProcessQueueString(itemName);
        String imageName = getInputData().getString("imageName");


        Gson gson = new Gson();
        CallServicesUtil util = new CallServicesUtil();
        //Type responseType = new TypeToken<LinkedList<ReceivedStop>>(){}.getType();
        //LinkedList<ReceivedStop> stopList =gson.fromJson(imagesJson,responseType);
        boolean success = util.sendSignature(getApplicationContext(),imageName,imageString);
        //notificationManager.cancel(notificationId)

        if(success){
            //Clean the DB
            processQueueRepository.deleteProcessQueueWithName(itemName);
        }else {
            long today = GlobalCoordinator.getInstance().getLongToday();
            processQueueRepository.updateProcessQueueWithFailure(itemName,today);
        }
        return success?Result.success():Result.retry();
    }

}
