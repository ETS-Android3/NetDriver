package lb.com.thenet.netdriver.rooms.entities.offline;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import lb.com.thenet.netdriver.GlobalCoordinator;

@Dao
public interface ProcessQueueDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(ProcessQueue processQueue);

    @Query("SELECT * from "+ GlobalCoordinator.PROCESS_QUEUE_TABLE + " where itemName = :itemName  order by id desc LIMIT 1")
    ProcessQueue[] getProcessQueue(String itemName);

    @Query("DELETE from "+ GlobalCoordinator.PROCESS_QUEUE_TABLE + " where itemName = :itemName")
    void deleteItemWithName(String itemName);

    @Query("UPDATE "+ GlobalCoordinator.PROCESS_QUEUE_TABLE + " set numberOfRetries = numberOfRetries + 1, dateLastRetry = :retryDate where itemName = :itemName")
    void updateItemRetries(String itemName, long retryDate);

    @Query("Select * from "+ GlobalCoordinator.PROCESS_QUEUE_TABLE + " where numberOfRetries >= :numberOfRetries order by dateInserted desc")
    LiveData<List<ProcessQueue>> getPendingQueue(int numberOfRetries);


}
