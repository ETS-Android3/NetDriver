package lb.com.thenet.netdriver.rooms.entities.offline;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import lb.com.thenet.netdriver.GlobalCoordinator;

@Entity(tableName = GlobalCoordinator.PROCESS_QUEUE_TABLE)
public class ProcessQueue {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @NonNull
    @ColumnInfo(name = "itemName")
    public String itemName;

    @NonNull
    @ColumnInfo(name = "itemValue")
    public String itemValue;


    public ProcessQueue(@NonNull String itemName, @NonNull String itemValue, @NonNull String itemType) {
        this.itemName = itemName;
        this.itemValue = itemValue;
        this.itemType = itemType;
        this.dateInsereted =  GlobalCoordinator.getInstance().getLongToday();
        this.numberOfRetries = 0;
    }

    @NonNull
    @ColumnInfo(name="itemType")
    public String itemType;


    @NonNull
    @ColumnInfo(name = "dateInserted")
    public long dateInsereted;

    @NonNull
    @ColumnInfo(name="numberOfRetries")
    public int numberOfRetries;

    @ColumnInfo(name="dateLastRetry")
    public long dateLastRetry;


}
