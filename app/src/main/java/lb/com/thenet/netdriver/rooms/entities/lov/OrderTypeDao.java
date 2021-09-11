package lb.com.thenet.netdriver.rooms.entities.lov;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import lb.com.thenet.netdriver.GlobalCoordinator;

@Dao
public interface OrderTypeDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(OrderType info);

    @Query("DELETE FROM "+ GlobalCoordinator.ORDER_TYPE_TABLE)
    void deleteAllOrderTypes();

    @Query("SELECT * FROM " + GlobalCoordinator.ORDER_TYPE_TABLE + " where orderTypeId LIKE :orderTypeId")
    OrderType[] getOrderType(String orderTypeId);

    @Query("SELECT * from " + GlobalCoordinator.ORDER_TYPE_TABLE)
    LiveData<List<OrderType>> getAllOrderTypes();
}
