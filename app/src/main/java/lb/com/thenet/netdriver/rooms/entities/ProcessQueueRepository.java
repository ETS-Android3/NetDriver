package lb.com.thenet.netdriver.rooms.entities;

import android.content.Context;
import android.os.AsyncTask;

import lb.com.thenet.netdriver.rooms.entities.enums.QueueItemType;
import lb.com.thenet.netdriver.rooms.entities.offline.ProcessQueue;
import lb.com.thenet.netdriver.rooms.entities.offline.ProcessQueueDao;

public class ProcessQueueRepository {
    private ProcessQueueDao processQueueDao;
    public ProcessQueueRepository(Context context){
        DriverRoomDatabase db = DriverRoomDatabase.getDatabase(context);
        processQueueDao = db.processQueueDao();
    }

    public void putProcessQueue(String itemName, String itemValue, QueueItemType type){
        new insertProcessQueueAsync(processQueueDao).execute(new ProcessQueue(itemName, itemValue, type.getQueueItemTypeCode()));
    }
    public String getProcessQueueString(String itemName){
        ProcessQueue[] processQueue = processQueueDao.getProcessQueue(itemName);
        if(processQueue == null || processQueue.length == 0) return "";
        return processQueue[0].itemValue;
    }
    public ProcessQueue getProcessQueue(String itemName){
        ProcessQueue[] processQueue = processQueueDao.getProcessQueue(itemName);
        if(processQueue == null || processQueue.length == 0) return null;
        return processQueue[0];
    }
    public void deleteProcessQueueWithName(String itmeName){
        processQueueDao.deleteItemWithName(itmeName);
    }

    public void updateProcessQueueWithFailure(String itemName, long dateOfFailure){
        processQueueDao.updateItemRetries(itemName, dateOfFailure);
    }

    private static class insertProcessQueueAsync extends AsyncTask<ProcessQueue, Void, Void> {

        private ProcessQueueDao processQueueDao;
        public insertProcessQueueAsync(ProcessQueueDao processQueueDao){
            this.processQueueDao = processQueueDao;
        }
        @Override
        protected Void doInBackground(ProcessQueue... processQueues) {
            processQueueDao.insert(processQueues[0]);
            return null;
        }
    }


}
