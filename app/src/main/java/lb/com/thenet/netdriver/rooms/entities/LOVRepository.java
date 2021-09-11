package lb.com.thenet.netdriver.rooms.entities;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;

import lb.com.thenet.netdriver.rooms.entities.enums.ActionRouteType;
import lb.com.thenet.netdriver.rooms.entities.lov.ActionRoute;
import lb.com.thenet.netdriver.rooms.entities.lov.ActionRouteDao;

public class LOVRepository {

    private ActionRouteDao actionRouteDao;
    private LiveData<List<ActionRoute>> mNonPickupActionRoutes;
    private LiveData<List<ActionRoute>> mNonDeliveryActionRoutes;
    private LiveData<List<ActionRoute>> mRejectActionRoutes;

    public LOVRepository(Application application) {
        DriverRoomDatabase db = DriverRoomDatabase.getDatabase(application);
        actionRouteDao = db.actionRouteDao();
        loadNonPickupActionRoutesFromLocal();
        loadNonDeliveryActionRoutesFromLocal();
        loadRejectActionRoutesFromLocal();
    }

    private void loadNonPickupActionRoutesFromLocal() {
        mNonPickupActionRoutes = actionRouteDao.getActionRoutesOfType(ActionRouteType.NonPickup.getActionRouteId());
    }
    private void loadNonDeliveryActionRoutesFromLocal(){
        mNonDeliveryActionRoutes = actionRouteDao.getActionRoutesOfType(ActionRouteType.NonDelivery.getActionRouteId());
    }
    private void loadRejectActionRoutesFromLocal(){
        mRejectActionRoutes = actionRouteDao.getActionRoutesOfType(ActionRouteType.Reject.getActionRouteId());
    }

    public LiveData<List<ActionRoute>> getmNonPickupActionRoutes() {
        return mNonPickupActionRoutes;
    }

    public LiveData<List<ActionRoute>> getmRejectActionRoutes() {
        return mRejectActionRoutes;
    }

    public LiveData<List<ActionRoute>> getmNonDeliveryActionRoutes() {
        return mNonDeliveryActionRoutes;
    }

    public void refreshActionRoutes(lb.com.thenet.netdriver.onlineservices.json.ActionRoute[] data) {
        new refreshActionRoutesAsync(actionRouteDao).execute(data);
    }

    private static class refreshActionRoutesAsync extends AsyncTask<lb.com.thenet.netdriver.onlineservices.json.ActionRoute[], Void, Void> {
        private ActionRouteDao actionRouteDao;
        refreshActionRoutesAsync(ActionRouteDao actionRouteDao){
            this.actionRouteDao = actionRouteDao;
        }
        @Override
        protected Void doInBackground(lb.com.thenet.netdriver.onlineservices.json.ActionRoute[]... actionRoutes) {
            refreshActionRoutes(actionRoutes[0]);
            return null;
        }

        private void refreshActionRoutes(lb.com.thenet.netdriver.onlineservices.json.ActionRoute[] actionRoutes) {
            actionRouteDao.deleteAllActionRoutes();
            if(actionRoutes != null)
                for (lb.com.thenet.netdriver.onlineservices.json.ActionRoute parent :
                        actionRoutes) {
                    //Parent Action Routes
                    ActionRoute parentActionRoute = new ActionRoute();
                    parentActionRoute.parentActionRouteDesc = "";
                    parentActionRoute.parentActionRouteId = "";
                    parentActionRoute.parentActionRouteName = "";
                    parentActionRoute.actionRouteDesc = parent.description;
                    parentActionRoute.actionRouteId = parent.actionRouteId;
                    parentActionRoute.actionRouteName = parent.name;
                    actionRouteDao.insert(parentActionRoute);
                    if(parent.actionRoutes != null)
                        for (lb.com.thenet.netdriver.onlineservices.json.ActionRoute child :
                                parent.actionRoutes) {
                            //Child Action Routes
                            ActionRoute childActionRoute = new ActionRoute();
                            childActionRoute.parentActionRouteDesc = parent.description;
                            childActionRoute.parentActionRouteId = parent.actionRouteId;
                            childActionRoute.parentActionRouteName = parent.name;
                            childActionRoute.actionRouteDesc = child.description;
                            childActionRoute.actionRouteId = child.actionRouteId;
                            childActionRoute.actionRouteName = child.name;
                            actionRouteDao.insert(childActionRoute);
                        }
                }
        }
    }

}
