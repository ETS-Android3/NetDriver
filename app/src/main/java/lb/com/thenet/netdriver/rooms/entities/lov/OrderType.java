package lb.com.thenet.netdriver.rooms.entities.lov;


import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import lb.com.thenet.netdriver.GlobalCoordinator;

@Entity(tableName = GlobalCoordinator.ORDER_TYPE_TABLE)
public class OrderType {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    @ColumnInfo(name = "orderTypeId")
    private String orderTypeId;

    @NonNull
    @ColumnInfo(name = "title")
    private String title;

    public OrderType(@NonNull String orderTypeId, @NonNull String title) {
        this.orderTypeId = orderTypeId;
        this.title = title;
    }

    public String getOrderTypeId(){return orderTypeId;}
    public String getTitle() {return title; }
    public int getId() {return id;}

    public void setId(int id) {
        this.id = id;
    }
}
