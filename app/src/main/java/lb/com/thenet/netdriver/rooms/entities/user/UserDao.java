package lb.com.thenet.netdriver.rooms.entities.user;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;


import lb.com.thenet.netdriver.GlobalCoordinator;

@Dao
public interface UserDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(User user);


    @Query("DELETE FROM "+GlobalCoordinator.USER_TABLE)
    void deleteUser();


    @Query("SELECT * from "+GlobalCoordinator.USER_TABLE+" order by id desc LIMIT 1")
    User[] getUser();

    @Query("Update " + GlobalCoordinator.USER_TABLE + " set isLoggedIn = 0")
    void setUserLogout();
}
