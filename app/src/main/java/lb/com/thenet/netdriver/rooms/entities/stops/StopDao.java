package lb.com.thenet.netdriver.rooms.entities.stops;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;


import java.util.List;

import lb.com.thenet.netdriver.GlobalCoordinator;
import lb.com.thenet.netdriver.rooms.entities.enums.StopStatus;

@Dao
public interface StopDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Stop stop);

    @Query("DELETE FROM "+ GlobalCoordinator.STOP_TABLE)
    void deleteAllStops();

    @Query("DELETE FROM " + GlobalCoordinator.STOP_TABLE + " WHERE orderTypeId LIKE :orderTypeId")
    void deleteAllStopsofType(String orderTypeId);

    @Query("DELETE FROM " + GlobalCoordinator.STOP_TABLE + " WHERE stopId LIKE :stopId")
    void deleteStop(String stopId);

    @Update
    void update(Stop... stops);

    @Query("SELECT * FROM " + GlobalCoordinator.STOP_TABLE + " where orderTypeId LIKE :orderTypeId AND userName LIKE :userName AND dateInserted > :dateInserted")
    LiveData<List<Stop>> getStopsofType(String orderTypeId, String userName, long dateInserted);

    @Query("SELECT * FROM " + GlobalCoordinator.STOP_TABLE + " where stopId LIKE :stopId")
    Stop[] getStopOfId(String stopId);

    @Query("SELECT COUNT(*) FROM " + GlobalCoordinator.STOP_TABLE + " where status LIKE :status AND orderTypeId LIKE :orderTypeId AND userName LIKE :userName AND dateInserted > :dateInserted")
    LiveData<Integer> countStopsOfStatus(String orderTypeId, String status, String userName, long dateInserted);

    @Query("SELECT COUNT(*) FROM " + GlobalCoordinator.STOP_TABLE + " where orderTypeId LIKE :orderTypeId AND userName LIKE :userName AND dateInserted > :dateInserted")
    LiveData<Integer> countStops(String orderTypeId, String userName, long dateInserted);

    @Query("SELECT COUNT(*) FROM " + GlobalCoordinator.STOP_TABLE + " where  orderTypeId LIKE :orderTypeId AND userName LIKE :userName AND dateInserted > :dateInserted AND numberOfInCompleteLabels = 0")
    LiveData<Integer> countCompleteStops(String orderTypeId, String userName, long dateInserted);

    @Query("SELECT COUNT(*) FROM " + GlobalCoordinator.STOP_TABLE + " where status LIKE 'S' AND orderTypeId LIKE :orderTypeId AND userName LIKE :userName AND dateInserted > :dateInserted AND numberOfInCompleteLabels > 0")
    LiveData<Integer> countInCompleteStops(String orderTypeId, String userName, long dateInserted);

    @Query("SELECT SUM(numberOfInCompleteLabels) FROM " + GlobalCoordinator.STOP_TABLE + " where orderTypeId LIKE :orderTypeId AND userName LIKE :userName AND dateInserted > :dateInserted")
    LiveData<Integer> countInCompleteLabels(String orderTypeId, String userName, long dateInserted);

    @Query("SELECT SUM(numberOfCompleteLabels) FROM " + GlobalCoordinator.STOP_TABLE + " where orderTypeId LIKE :orderTypeId AND userName LIKE :userName AND dateInserted > :dateInserted")
    LiveData<Integer> countCompleteLabels(String orderTypeId, String userName, long dateInserted);

    @Query("UPDATE " + GlobalCoordinator.STOP_TABLE + " SET numberOfInCompleteLabels = ( SELECT COUNT(*) FROM " + GlobalCoordinator.STOP_LABEL_TABLE + " where stopId LIKE :stopId AND status NOT LIKE 'D' AND status NOT LIKE 'ND') where stopId = :stopId")
    void updateInCompleteLabelsCountForStop(String stopId);

    @Query("UPDATE " + GlobalCoordinator.STOP_TABLE + " SET numberOfCompleteLabels = ( SELECT COUNT(*) FROM " + GlobalCoordinator.STOP_LABEL_TABLE + " where stopId LIKE :stopId AND (status  LIKE 'D' OR status LIKE 'ND')) where stopId = :stopId")
    void updateCompleteLabelsCountForStop(String stopId);

    @Query("SELECT numberOfInCompleteLabels FROM " + GlobalCoordinator.STOP_TABLE + " WHERE stopId LIKE :stopId")
    LiveData<Integer> countIncompleteLabelsForStop(String stopId);


    @Query("SELECT * FROM " + GlobalCoordinator.STOP_TABLE + " where userName LIKE :userName AND dateInserted > :dateInserted AND status like 'NS' AND orderTypeId LIKE '8d2e0633-d7bc-4f58-a121-74320ca9e0c2'")//AND stopId in (SELECT stopId FROM " + GlobalCoordinator.STOP_LABEL_TABLE + " WHERE shipmentType like 'f82a7dac-0631-4609-bec7-67597703318f' AND status  NOT LIKE 'D' AND status NOT LIKE 'ND')")
    LiveData<List<Stop>> getForcedAssignedStops(String userName, long dateInserted);

    @Query("SELECT Count(*) FROM " + GlobalCoordinator.STOP_TABLE + " where userName LIKE :userName AND dateInserted > :dateInserted AND status like 'NS' AND orderTypeId LIKE '8d2e0633-d7bc-4f58-a121-74320ca9e0c2'")// AND stopId in (SELECT stopId FROM " + GlobalCoordinator.STOP_LABEL_TABLE + " WHERE shipmentType like 'f82a7dac-0631-4609-bec7-67597703318f' AND status  NOT LIKE 'D' AND status NOT LIKE 'ND')")
    LiveData<Integer> countForcedAssignedStops(String userName, long dateInserted);



}
