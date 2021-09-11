package lb.com.thenet.netdriver.rooms.entities.stops;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import lb.com.thenet.netdriver.GlobalCoordinator;

@Entity(tableName = GlobalCoordinator.STOP_LABEL_TABLE)
public class StopLabel {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @NonNull
    @ColumnInfo(name = "stopId")
    public String stopId;
    @NonNull
    @ColumnInfo(name = "contactId")
    public long contactId;

    @NonNull
    @ColumnInfo(name = "orderTypeId")
    public String orderTypeId;

    @ColumnInfo(name = "orderId")
    public String orderId;

    @ColumnInfo(name = "hawb")
    public String hawb;

    @ColumnInfo(name = "numberOfPieces")
    public int numberOfPieces;

    @ColumnInfo(name = "codLBP")
    public double codLBP;

    @ColumnInfo(name = "codUSD")
    public double codUSD;

    @ColumnInfo(name = "shipmentType")
    public String shipmentType;

    @ColumnInfo(name = "status")
    public String status;

    @ColumnInfo(name = "deliveryReason")
    public String deliveryReason;

    @ColumnInfo(name = "shipperReference")
    public String shipperReference;

    @ColumnInfo(name = "specialInstructions")
    public String specialInstructions;

    @ColumnInfo(name = "hasRetour")
    public boolean hasRetour;

    @ColumnInfo(name = "hasRefund")
    public boolean hasRefund;

    @ColumnInfo(name = "refundAmountLBP")
    public double refundAmountLBP;

    @ColumnInfo(name = "refundAmount")
    public double refundAmount;

    @ColumnInfo(name = "retourInstructions")
    public String retourInstructions;

    @ColumnInfo(name = "fromTime")
    public String fromTime;

    @ColumnInfo(name = "toTime")
    public String toTime;

    @ColumnInfo(name = "consigneeName")
    public String consigneeName;

    @ColumnInfo(name = "consigneeCity")
    public String consigneeCity;

    @ColumnInfo(name = "ClientConnectionInfo")
    public String refundOrderId;

    @ColumnInfo(name = "retourDone")
    public boolean retourDone;


}
