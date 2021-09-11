package lb.com.thenet.netdriver.rooms.entities.stops;


import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.List;

import lb.com.thenet.netdriver.GlobalCoordinator;

@Entity(tableName = GlobalCoordinator.STOP_CONTACT_TABLE)
public class StopContact {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @NonNull
    @ColumnInfo(name = "contactId")
    public long contactId;

    @NonNull
    @ColumnInfo(name = "stopId")
    public String stopId;

    @NonNull
    @ColumnInfo(name = "orderTypeId")
    public String orderTypeId;

    @ColumnInfo(name = "companyName")
    public String companyName;

    @ColumnInfo(name = "salutation")
    public String salutation;

    @ColumnInfo(name = "firstName")
    public String firstName;

    @ColumnInfo(name = "lastName")
    public String lastName;

    @ColumnInfo(name = "mobile")
    public String mobile;

    @ColumnInfo(name = "phone")
    public String phone;

    @ColumnInfo(name = "numberOfLabels")
    public int numberOfLabels;

    @ColumnInfo(name = "numberOfCODs")
    public int numberOfCODs;

    @ColumnInfo(name = "totalCODLBP")
    public double totalCODLBP;

    @ColumnInfo(name = "totalCODUSD")
    public double totalCODUSD;

    @Ignore
    public LiveData<List<StopLabel>> stopLabelLiveData;

    @ColumnInfo(name = "status")
    public String status;

}
