package lb.com.thenet.netdriver.rooms.entities.lov;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import lb.com.thenet.netdriver.GlobalCoordinator;

@Dao
public interface ActionRouteDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(ActionRoute actionRoute);

    @Query("DELETE FROM "+ GlobalCoordinator.LOV_ACTION_ROUTE_TABLE)
    void deleteAllActionRoutes();



    @Query("SELECT * FROM " + GlobalCoordinator.LOV_ACTION_ROUTE_TABLE + " where parentActionRouteId LIKE :parentActionRouteId")
    LiveData<List<ActionRoute>> getActionRoutesOfType(String parentActionRouteId);

}
