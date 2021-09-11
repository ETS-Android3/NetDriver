package lb.com.thenet.netdriver.rooms.entities.stops;


import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Date;
import java.util.List;

import lb.com.thenet.netdriver.GlobalCoordinator;


@Entity(tableName = GlobalCoordinator.STOP_TABLE)
public class Stop {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @NonNull
    @ColumnInfo(name = "stopId")
    public String stopId;

    @NonNull
    @ColumnInfo(name = "userName")
    public String userName;

    @NonNull
    @ColumnInfo(name = "orderTypeId")
    public String orderTypeId;

    @ColumnInfo(name = "clientName")
    public String clientName;

    @ColumnInfo(name = "shipperName")
    public String shipperName;

    @ColumnInfo(name = "service")
    public String service;

    @ColumnInfo(name = "numberOfPieces")
    public int numberOfPieces;

    @ColumnInfo(name = "status")
    public String status;

    @ColumnInfo(name = "addressId")
    public String addressId;

    @ColumnInfo(name = "addressCity")
    public String addressCity;

    @ColumnInfo(name = "addressStreet")
    public String addressStreet;

    @ColumnInfo(name = "addressBuilding")
    public String addressBuilding;

    @ColumnInfo(name = "addressFloor")
    public String addressFloor;

    @ColumnInfo(name = "addressLandmark")
    public String addressLandmark;

    @ColumnInfo(name = "addressLongitude")
    public double addressLongitude;

    @ColumnInfo(name = "addressLatitude")
    public double addressLatitude;

    @ColumnInfo(name = "numberOfLabels")
    public int numberOfLabels;

    @ColumnInfo(name = "codLBP")
    public double codLPB;

    @ColumnInfo(name = "codUSD")
    public double codUSD;

    @ColumnInfo(name = "contactPhone")
    public String contactPhone;

    @ColumnInfo(name = "oneLabel")
    public String oneLabel;

    @ColumnInfo(name = "numberOfContacts")
    public int numberOfContacts;

    @Ignore
    public LiveData<List<StopContact>> stopContactLiveData;

    @ColumnInfo(name = "dateInserted")
    public long dateInserted;

    @NonNull
    @ColumnInfo(name = "numberOfInCompleteLabels")
    public int numberOfInCompleteLabels;

    @NonNull
    @ColumnInfo(name = "numberOfCompleteLabels")
    public int numberOfCompleteLabels;

}
