package lb.com.thenet.netdriver.rooms.entities.stops;

import android.provider.Settings;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import lb.com.thenet.netdriver.GlobalCoordinator;

@Dao
public interface StopLabelDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(StopLabel stopLabel);

    @Query("DELETE FROM "+ GlobalCoordinator.STOP_LABEL_TABLE + " WHERE orderTypeId like :orderTypeId")
    void deleteAllStopLabelsofType(String orderTypeId);

    @Update
    void update(StopLabel... stopLabels);

    @Delete
    void delete(StopLabel stopLabel);

    @Query("SELECT * FROM " + GlobalCoordinator.STOP_LABEL_TABLE + " where contactId = :contactId and stopId = :stopId")
    LiveData<List<StopLabel>> getStopLabelFor(long contactId, String stopId);

    @Query("SELECT * FROM " + GlobalCoordinator.STOP_TABLE + " where stopId in (SELECT StopId FROM " + GlobalCoordinator.STOP_LABEL_TABLE + " WHERE HAWB LIKE :label OR shipperReference LIKE :label) AND orderTypeId LIKE :orderTypeId AND userName LIKE :userName AND dateInserted > :dateInserted")
    Stop[] getStopOfLabel(String orderTypeId, String label, String userName, long dateInserted);
    /*
    @Query("SELECT * FROM " + GlobalCoordinator.STOP_LABEL_TABLE)
    LiveData<List<StopLabel>> getAllStopLabels();

     */

    @Query("SELECT * FROM " + GlobalCoordinator.STOP_LABEL_TABLE + " where stopId like :stopId AND orderId like :orderId AND hawb like :label")
    StopLabel[] getStopLabelOfOrderId(String stopId, String orderId, String label);

    @Query("Update " + GlobalCoordinator.STOP_LABEL_TABLE + " SET codLBP = :codLBP, codUSD = :codUSD WHERE orderId like :orderId AND hawb like :hawb")
    void updateCOD(String orderId, String hawb, double codLBP, double codUSD);
}
