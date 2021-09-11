package lb.com.thenet.netdriver.rooms.entities.lov;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import lb.com.thenet.netdriver.GlobalCoordinator;

@Entity(tableName = GlobalCoordinator.LOV_ACTION_ROUTE_TABLE)

public class ActionRoute {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @NonNull
    @ColumnInfo(name = "parentActionRouteId")
    public String parentActionRouteId;

    @NonNull
    @ColumnInfo(name = "parentActionRouteName")
    public String parentActionRouteName;

    @NonNull
    @ColumnInfo(name = "parentActionRouteDesc")
    public String parentActionRouteDesc;

    @NonNull
    @ColumnInfo(name = "actionRouteId")
    public String actionRouteId;

    @NonNull
    @ColumnInfo(name = "actionRouteName")
    public String actionRouteName;

    @NonNull
    @ColumnInfo(name = "actionRouteDesc")
    public String actionRouteDesc;

}
