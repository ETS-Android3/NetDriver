package lb.com.thenet.netdriver.rooms.entities;

import lb.com.thenet.netdriver.rooms.entities.info.InfoDao;
import lb.com.thenet.netdriver.rooms.entities.stops.StopContactDao;
import lb.com.thenet.netdriver.rooms.entities.stops.StopDao;
import lb.com.thenet.netdriver.rooms.entities.stops.StopLabelDao;
import lb.com.thenet.netdriver.rooms.entities.user.User;
import lb.com.thenet.netdriver.rooms.entities.user.UserDao;

public interface asyncTaskListener {
    void processLatestToken(String token);
    void processLockStatus(Boolean lockStatus);
    InfoDao getInfoDao();
    UserDao getUserDao();
    void processLoggedInUser(User user);

    /*
    StopDao getStopDao();
    StopContactDao getStopContactDao();
    StopLabelDao getStopLabelDao();

     */
}
