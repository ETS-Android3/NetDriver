package lb.com.thenet.netdriver.rooms.entities.stops;

import android.provider.Settings;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import lb.com.thenet.netdriver.GlobalCoordinator;

@Dao
public interface StopContactDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(StopContact stopContact);

    @Query("DELETE FROM "+ GlobalCoordinator.STOP_CONTACT_TABLE + " WHERE orderTypeId like :orderTypeId")
    void deleteAllStopContactsofType(String orderTypeId);

    @Update
    void update(StopContact... stopContacts);

    @Query("SELECT * FROM " + GlobalCoordinator.STOP_CONTACT_TABLE + " where stopId LIKE :stopId")
    LiveData<List<StopContact>> getStopContactsFor(String stopId);

    @Query("SELECT * FROM " + GlobalCoordinator.STOP_CONTACT_TABLE)
    LiveData<List<StopContact>> getAllStopContacts();

    @Query("DELETE FROM " + GlobalCoordinator.STOP_CONTACT_TABLE + " where contactId = :contactId")
    void deleteStopContact(long contactId);

    @Query("SELECT * FROM " + GlobalCoordinator.STOP_CONTACT_TABLE + " where stopId like :stopId AND contactId = :stopContactId")
    StopContact[] getStopContactOfId(String stopId, long stopContactId);
}
