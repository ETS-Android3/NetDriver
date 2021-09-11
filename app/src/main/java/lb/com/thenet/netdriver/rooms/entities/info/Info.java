package lb.com.thenet.netdriver.rooms.entities.info;


import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import lb.com.thenet.netdriver.GlobalCoordinator;

@Entity(tableName = GlobalCoordinator.INFO_TABLE)
public class Info {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    @ColumnInfo(name = "infoName")
    private String infoName;

    @NonNull
    @ColumnInfo(name = "infoValue")
    private String infoValue;

    public Info(@NonNull String infoName, @NonNull String infoValue) {
        this.infoName = infoName;
        this.infoValue = infoValue;
    }

    /*
     * This constructor is annotated using @Ignore, because Room expects only
     * one constructor by default in an entity class.
     */

    @Ignore
    public Info(int id, @NonNull String name, @NonNull String val) {
        this.id = id;
        this.infoName = name;
        this.infoValue = val;
    }

    public String getInfoName() {
        return this.infoName;
    }
    public String getInfoValue() { return this.infoValue; }

    public int getId() {return id;}

    public void setId(int id) {
        this.id = id;
    }
}
