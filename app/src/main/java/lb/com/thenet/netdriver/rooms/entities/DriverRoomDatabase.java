package lb.com.thenet.netdriver.rooms.entities;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import lb.com.thenet.netdriver.GlobalCoordinator;
import lb.com.thenet.netdriver.rooms.entities.info.Info;
import lb.com.thenet.netdriver.rooms.entities.info.InfoDao;
import lb.com.thenet.netdriver.rooms.entities.lov.ActionRoute;
import lb.com.thenet.netdriver.rooms.entities.lov.ActionRouteDao;
import lb.com.thenet.netdriver.rooms.entities.lov.OrderType;
import lb.com.thenet.netdriver.rooms.entities.lov.OrderTypeDao;
import lb.com.thenet.netdriver.rooms.entities.offline.ProcessQueue;
import lb.com.thenet.netdriver.rooms.entities.offline.ProcessQueueDao;
import lb.com.thenet.netdriver.rooms.entities.stops.Stop;
import lb.com.thenet.netdriver.rooms.entities.stops.StopContact;
import lb.com.thenet.netdriver.rooms.entities.stops.StopContactDao;
import lb.com.thenet.netdriver.rooms.entities.stops.StopDao;
import lb.com.thenet.netdriver.rooms.entities.stops.StopLabel;
import lb.com.thenet.netdriver.rooms.entities.stops.StopLabelDao;
import lb.com.thenet.netdriver.rooms.entities.user.User;
import lb.com.thenet.netdriver.rooms.entities.user.UserDao;

@Database(entities = {Info.class, User.class, OrderType.class, Stop.class, StopContact.class, StopLabel.class, ActionRoute.class, ProcessQueue.class}, version = 8, exportSchema = false)
public abstract class DriverRoomDatabase extends RoomDatabase {

    public abstract InfoDao infoDao();
    public abstract UserDao userDao();

    public abstract StopDao stopDao();
    public abstract StopContactDao stopContactDao();
    public abstract StopLabelDao stopLabelDao();

    public abstract ActionRouteDao actionRouteDao();

    public abstract OrderTypeDao orderTypeDao();

    public abstract ProcessQueueDao processQueueDao();


    private static DriverRoomDatabase INSTANCE;

    public static DriverRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (DriverRoomDatabase.class) {
                if (INSTANCE == null) {
                    // Create database here.
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            DriverRoomDatabase.class, "drivers_database")
                            // Wipes and rebuilds instead of migrating if no Migration object.
                            .fallbackToDestructiveMigration()
                            .addCallback(sRoomDatabaseCallback)
                            .build();

                }
            }
        }

        return INSTANCE;
    }

    // This callback is called when the database has opened.
    // In this case, use PopulateDbAsync to populate the database
    // with the initial data set if the database has no entries.
    private static RoomDatabase.Callback sRoomDatabaseCallback =
            new RoomDatabase.Callback(){

                @Override
                public void onOpen (@NonNull SupportSQLiteDatabase db){
                    super.onOpen(db);
                    new PopulateDbAsync(INSTANCE).execute();
                }
            };

    // Populate the database with the initial data set
    // only if the database has no entries.
    private static class PopulateDbAsync extends AsyncTask<Void, Void, Void> {

        private final InfoDao mDao;

        // Initial data set
        //private static String [] words = {"dolphin", "crocodile", "cobra", "elephant", "goldfish", "tiger", "snake"};

        PopulateDbAsync(DriverRoomDatabase db) {
            mDao = db.infoDao();
        }

        @Override
        protected Void doInBackground(final Void... params) {
            // If we have no words, then create the initial list of words.
            if(mDao.getLatestToken().length < 1){
                Info noToken = new Info(GlobalCoordinator.TOKEN_KEY,"NA");
                mDao.insert(noToken);
                Info noLock = new Info(GlobalCoordinator.LOCK_KEY, "0");
                mDao.insert(noLock);
                Info deliveryOderStatusInfo = new Info(GlobalCoordinator.DELIVERY_ORDER_STATUS_KEY, "0");
                mDao.insert(deliveryOderStatusInfo);
                Info pickupOderStatusInfo = new Info(GlobalCoordinator.PICKUP_ORDER_STATUS_KEY, "0");
                mDao.insert(pickupOderStatusInfo);
            }
            return null;
        }
    }

}
