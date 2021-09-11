package lb.com.thenet.netdriver.rooms.entities.info;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import lb.com.thenet.netdriver.GlobalCoordinator;
import lb.com.thenet.netdriver.rooms.entities.info.Info;

@Dao
public interface InfoDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Info info);



    @Query("DELETE FROM info_table where infoName = '"+GlobalCoordinator.TOKEN_KEY+"'")
    void deleteAllTokens();



    @Delete
    void deleteInfo(Info info);


    @Query("SELECT * from info_table where infoName = '"+ GlobalCoordinator.TOKEN_KEY+"' order by id desc LIMIT 1")
    Info[] getLatestToken();

    @Query("SELECT * from info_table where infoName = '"+GlobalCoordinator.LOCK_KEY+"' order by id desc LIMIT 1")
    Info[] getLockStatus();

    @Query("SELECT * from info_table ORDER BY infoName ASC")
    LiveData<List<Info>> getAllInfo();

    @Update
    void update(Info... infos);

    @Query("Update info_table set infoValue = '0' where infoName = '"+GlobalCoordinator.LOCK_KEY+"'")
    void resetLock();

    @Query("SELECT * FROM info_table where infoName = '" + GlobalCoordinator.DELIVERY_ORDER_STATUS_KEY + "' order by id desc LIMIT 1")
    Info[] getDeliveryOrderStatus();

    @Query("Update info_table set infoValue=:status  where infoName = '" + GlobalCoordinator.DELIVERY_ORDER_STATUS_KEY + "'")
    void updateDeliveryOrderStatus(String status);

    @Query("SELECT * FROM info_table where infoName = '" + GlobalCoordinator.PICKUP_ORDER_STATUS_KEY + "' order by id desc LIMIT 1")
    Info[] getPickupOrderStatus();

    @Query("Update info_table set infoValue=:status  where infoName = '" + GlobalCoordinator.PICKUP_ORDER_STATUS_KEY + "'")
    void updatePickupOrderStatus(String status);



}
