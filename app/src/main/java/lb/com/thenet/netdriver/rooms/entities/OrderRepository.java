package lb.com.thenet.netdriver.rooms.entities;


import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.Date;
import java.util.List;

//import lb.com.thenet.netdriver.onlineservices.json.ActionRoute;
import lb.com.thenet.netdriver.GlobalCoordinator;
import lb.com.thenet.netdriver.TypeConverters;
import lb.com.thenet.netdriver.notificationtypes.updateCODType;
import lb.com.thenet.netdriver.onlineservices.DriverServices;
import lb.com.thenet.netdriver.onlineservices.json.JsonStop;
import lb.com.thenet.netdriver.onlineservices.json.OrderType;
import lb.com.thenet.netdriver.rooms.entities.enums.LabelStatus;
import lb.com.thenet.netdriver.rooms.entities.enums.ShipmentType;
import lb.com.thenet.netdriver.rooms.entities.enums.StopStatus;
import lb.com.thenet.netdriver.rooms.entities.info.InfoDao;
import lb.com.thenet.netdriver.rooms.entities.lov.ActionRouteDao;
import lb.com.thenet.netdriver.rooms.entities.stops.Stop;
import lb.com.thenet.netdriver.rooms.entities.stops.StopContact;
import lb.com.thenet.netdriver.rooms.entities.stops.StopContactDao;
import lb.com.thenet.netdriver.rooms.entities.stops.StopDao;
import lb.com.thenet.netdriver.rooms.entities.stops.StopLabel;
import lb.com.thenet.netdriver.rooms.entities.stops.StopLabelDao;

/**
 * This class holds the implementation code for the methods that interact with the database.
 * Using a repository allows us to group the implementation methods together,
 * and allows the Order Delivery or Pickup View Models to be a clean interface between the rest of the app
 * and the database.
 *
 * For insert, update and delete, and longer-running queries,
 * we must run the database interaction methods in the background.
 *
 * Typically, all you need to do to implement a database method
 * is to call it on the data access object (DAO), in the background if applicable.
 */

public class OrderRepository {
    private StopDao stopDao;
    private StopContactDao stopContactDao;
    private StopLabelDao stopLabelDao;
    private InfoDao infoDao;
    private ActionRouteDao actionRouteDao;

    private LiveData<List<Stop>> mAllStops;
    private LiveData<Integer> mAllStopCount;
    private LiveData<Integer> mStartedStopsCount;
    private LiveData<Integer> mNotStartedStopsCount;
    private LiveData<Integer> mCompletedStopCount;
    private LiveData<Integer> mInCompletedStopCount;
    private LiveData<Integer> mCompleteLabelCount;
    private LiveData<Integer> mInCompleteLabelCount;



    private OrderType orderType;

    //private LiveData<List<ActionRoute>> mAllActionRoutes;


    public OrderRepository(Application application, OrderType orderType){
        this.orderType = orderType;
        DriverRoomDatabase db = DriverRoomDatabase.getDatabase(application);
        stopDao = db.stopDao();
        stopContactDao = db.stopContactDao();
        stopLabelDao = db.stopLabelDao();
        infoDao = db.infoDao();
        actionRouteDao = db.actionRouteDao();

        loadAllStopsFromLocal(orderType);
        countStopsPerType();

        //loadAllActionRoutesFromLocal();
    }

    private void countStopsPerType() {
        //stopDao.deleteEmptyStops();
        mNotStartedStopsCount = stopDao.countStopsOfStatus(orderType.getOrderTypeId(), StopStatus.NotStarted.getStopStatusCode(), DriverServices.mToken.getValue().UserName, GlobalCoordinator.getInstance().getLongToday());
        mStartedStopsCount = stopDao.countStopsOfStatus(orderType.getOrderTypeId(),StopStatus.Started.getStopStatusCode(), DriverServices.mToken.getValue().UserName, GlobalCoordinator.getInstance().getLongToday());
        mAllStopCount = stopDao.countStops(orderType.getOrderTypeId(), DriverServices.mToken.getValue().UserName, GlobalCoordinator.getInstance().getLongToday());
        mCompletedStopCount = stopDao.countCompleteStops(orderType.getOrderTypeId(), DriverServices.mToken.getValue().UserName, GlobalCoordinator.getInstance().getLongToday());
        mInCompletedStopCount = stopDao.countInCompleteStops(orderType.getOrderTypeId(), DriverServices.mToken.getValue().UserName, GlobalCoordinator.getInstance().getLongToday());
        mCompleteLabelCount = stopDao.countCompleteLabels(orderType.getOrderTypeId(), DriverServices.mToken.getValue().UserName, GlobalCoordinator.getInstance().getLongToday());
        mInCompleteLabelCount = stopDao.countInCompleteLabels(orderType.getOrderTypeId(), DriverServices.mToken.getValue().UserName, GlobalCoordinator.getInstance().getLongToday());
    }

    /*
    private void loadAllActionRoutesFromLocal() {
        mAllActionRoutes = actionRouteDao.getActionRoutesOfType("");
    }

     */

    private void loadAllStopsFromLocal(OrderType orderType) {


        //Get Today 12:00 a.m. timestamp as long to return all assigned stops after that
        long today = GlobalCoordinator.getInstance().getLongToday();

        mAllStops = stopDao.getStopsofType(orderType.getOrderTypeId(), DriverServices.mToken.getValue().UserName, today);

        }


    public void deepLoadStop(Stop stop) {
        stop.stopContactLiveData = stopContactDao.getStopContactsFor(stop.stopId);

    }

    public void deepLoadStopContact(StopContact stopContact) {

        stopContact.stopLabelLiveData = stopLabelDao.getStopLabelFor(stopContact.contactId, stopContact.stopId);

    }



    public LiveData<List<Stop>> getmAllStops(){return mAllStops;}

    public LiveData<Integer> getmAllStopCount() {
        return mAllStopCount;
    }

    public LiveData<Integer> getmStartedStopsCount() {
        return mStartedStopsCount;
    }

    public LiveData<Integer> getmNotStartedStopsCount() {
        return mNotStartedStopsCount;
    }

    public LiveData<Integer> getmCompletedStopCount() {
        return mCompletedStopCount;
    }

    public LiveData<Integer> getmInCompletedStopCount() {
        return mInCompletedStopCount;
    }

    public LiveData<Integer> getmCompleteLabelCount() {
        return mCompleteLabelCount;
    }

    public LiveData<Integer> getmInCompleteLabelCount() {
        return mInCompleteLabelCount;
    }

    public LiveData<Integer> countIncompleteLabelsForStop(String stopId){
        return stopDao.countIncompleteLabelsForStop(stopId);
    }

    public void refreshStops(JsonStop[] data, OrderType orderType) {

        new refreshStopsAsync(stopDao,stopContactDao,stopLabelDao, infoDao, orderType).execute(data);
    }

    public void updateStopLabel(StopLabel stopLabel){
        new updateStopLabelAsync(stopLabelDao, stopDao).execute(stopLabel);
    }

    public void insertStopLabel(StopLabel stopLabel){
        new insertStopLabelAsync(stopLabelDao, stopDao).execute(stopLabel);
    }

    public void updateStop(Stop stop) {
        new updateStopAsync(stopDao).execute(stop);
    }

    public void deleteAllOrdersOfType(OrderType orderType){
        new deleteOrdersAsync(stopDao,stopContactDao,stopLabelDao).execute(orderType.getOrderTypeId());
    }

    public Stop getStopOfLabel(String hawb, String orderTypeId, String userName, long dateInserted){

        getStopOfLabelAsync task = new getStopOfLabelAsync(stopLabelDao, orderTypeId,userName,dateInserted);
        try {
            task.execute(hawb).get();
            if(task.foundStop) return task.stop;
            else return null;
        }catch (Exception ex){
            return null;
        }
    }

    public Stop getStopById(String stopId){

        getStopByIdAsync task = new getStopByIdAsync(stopDao);
        try {
            return task.execute(stopId).get();
        }catch (Exception ex){
            return  null;
        }

    }

    public void deleteStopLabel(StopLabel stopLabel) {
        new deleteStopLabelAsync(stopLabelDao, stopDao).execute(stopLabel);
    }

    public void updateOrderCOD(updateCODType updateData){
        new updateCODAsync(stopLabelDao).execute(updateData);
    }

    /**
     * Deletes all words from the database (does not delete the table).
     */
    private static class refreshStopsAsync extends AsyncTask<JsonStop[], Void, Void> {

        private StopDao stopDao;
        private StopContactDao stopContactDao;
        private StopLabelDao stopLabelDao;
        private InfoDao infoDao;
        private OrderType orderType;

        refreshStopsAsync(StopDao stopDao, StopContactDao stopContactDao, StopLabelDao stopLabelDao, InfoDao infoDao, OrderType orderType) {
            this.stopDao = stopDao;
            this.stopContactDao = stopContactDao;
            this.stopLabelDao = stopLabelDao;
            this.infoDao = infoDao;
            this.orderType = orderType;
        }

        @Override
        protected Void doInBackground(JsonStop[]... jsonStops) {
            refreshStops(jsonStops[0], orderType);
            setOrdersLoadedStatus(orderType);
            return null;
        }


        private void setOrdersLoadedStatus(OrderType orderType) {
            //get info of order status
            //update it to be true "1"
            switch (orderType){
                case DELIVERY:{
                    infoDao.updateDeliveryOrderStatus("1");
                    break;
                }
                case PICKUP:{
                    infoDao.updatePickupOrderStatus("1");
                    break;
                }
            }
        }

        private void refreshStops(JsonStop[] jsonStops, OrderType orderType) {

            //stopDao.deleteAllStopsofType(OrderType.DELIVERY.getOrderTypeId());
            //stopContactDao.deleteAllStopContactsofType(OrderType.DELIVERY.getOrderTypeId());
            //stopLabelDao.deleteAllStopLabelsofType(OrderType.DELIVERY.getOrderTypeId());


            boolean alternatingTestShipmentTypes = true;
            if (jsonStops != null)
                for (JsonStop jsonStop :
                        jsonStops) {
                    //get the status of the existing stop, if any
                    Stop[] stops = stopDao.getStopOfId(jsonStop.stopId);
                    if (stops != null && stops.length > 0) {
                        //jsonStop.status = stops[0].status;
                        //stopDao.deleteStop(stops[0].stopId);
                        Stop stopToUpdate = stops[0];
                        //first, update the status of rejected and completed stops
                        if (stopToUpdate.status.equals(StopStatus.Rejected))
                            stopToUpdate.status = StopStatus.NotStarted.getStopStatusCode();
                        if (stopToUpdate.status.equals(StopStatus.Completed))
                            stopToUpdate.status = StopStatus.Started.getStopStatusCode();

                        stopToUpdate.numberOfPieces += jsonStop.numberOfPieces;
                        stopToUpdate.numberOfLabels += jsonStop.getNumberOfLabels();
                        stopToUpdate.codLPB += jsonStop.getCODLBP();
                        stopToUpdate.codUSD += jsonStop.getCODUSD();
                        //stopToUpdate.numberOfContacts = jsonStop.getNumberOfContacts();
                        stopDao.update(stopToUpdate);

                        if (jsonStop.contact != null)
                            for (JsonStop.Contact stopContact :
                                    jsonStop.contact) {
                                StopContact[] stopContacts = stopContactDao.getStopContactOfId(stopToUpdate.stopId, stopContact.contactId);
                                long contactId = stopContact.contactId;

                                if (stopContacts != null && stopContacts.length > 0) {
                                    //udpate the existing contact number of labels and COD's
                                    StopContact stopContactToUpdate = stopContacts[0];
                                    stopContactToUpdate.numberOfLabels += stopContact.getNumberOfLabels();
                                    stopContactToUpdate.numberOfCODs += stopContact.getNumberOfCODs();
                                    stopContactToUpdate.totalCODLBP += stopContact.getTotalCODLBP();
                                    stopContactToUpdate.totalCODUSD += stopContact.getTotalCODUSD();
                                    stopContactDao.update(stopContactToUpdate);
                                } else {
                                    //New Contact - Insert
                                    StopContact newContact = new StopContact();

                                    newContact.companyName = stopContact.companyName;
                                    newContact.contactId = stopContact.contactId;
                                    contactId = newContact.contactId;

                                    newContact.firstName = stopContact.firstName;

                                    if (stopContact.lastName == null) stopContact.lastName = "";
                                    newContact.lastName = stopContact.lastName;

                                    newContact.mobile = stopContact.mobile;
                                    newContact.orderTypeId = orderType.getOrderTypeId();
                                    OrderType.DELIVERY.getOrderTypeId();
                                    newContact.phone = stopContact.phone;
                                    if (stopContact.salutation == null) stopContact.salutation = "";
                                    newContact.salutation = stopContact.salutation;
                                    newContact.stopId = stopToUpdate.stopId;
                                    newContact.numberOfLabels = stopContact.getNumberOfLabels();
                                    newContact.numberOfCODs = stopContact.getNumberOfCODs();
                                    newContact.totalCODLBP = stopContact.getTotalCODLBP();
                                    newContact.totalCODUSD = stopContact.getTotalCODUSD();


                                    stopContactDao.insert(newContact);

                                    stopToUpdate.numberOfContacts += 1;//jsonStop.getNumberOfContacts();
                                    stopDao.update(stopToUpdate);

                                }

                                //loop to insert new labels
                                if (stopContact.stoplabel != null)
                                    for (JsonStop.Contact.StopLabel stopLabel :
                                            stopContact.stoplabel) {
                                        StopLabel[] stopLabels = stopLabelDao.getStopLabelOfOrderId(stopToUpdate.stopId, stopLabel.orderId, stopLabel.hawb);
                                        if (stopLabels != null && stopLabels.length > 0) {
                                            //Label already exists; continue
                                            continue;
                                        } else {
                                            //Insert a new label
                                            StopLabel newLabel = new StopLabel();
                                            newLabel.codLBP = stopLabel.codLBP;
                                            newLabel.codUSD = stopLabel.codUSD;
                                            newLabel.contactId = contactId;
                                            newLabel.hawb = stopLabel.hawb;
                                            newLabel.numberOfPieces = stopLabel.numberOfPieces;
                                            newLabel.orderId = stopLabel.orderId;
                                            newLabel.orderTypeId = orderType.getOrderTypeId();// OrderType.DELIVERY.getOrderTypeId();
                                            newLabel.shipmentType = stopLabel.shipmentType;
                                            newLabel.shipperReference = stopLabel.shipperReference;
                                            newLabel.status = LabelStatus.NotScanned.getLabelStatusCode();
                                            newLabel.stopId = stopToUpdate.stopId;
                                            newLabel.specialInstructions = stopLabel.specialInstructions;
                                            newLabel.hasRetour = stopLabel.hasRetour;
                                            newLabel.hasRefund = stopLabel.hasRefund;
                                            newLabel.refundAmountLBP = stopLabel.refundAmountLBP;
                                            newLabel.refundAmount = stopLabel.refundAmount;
                                            newLabel.retourInstructions = stopLabel.retourInstructions;
                                            newLabel.consigneeName = stopLabel.consigneeName;
                                            newLabel.consigneeCity = stopLabel.consigneeCity;
                                            newLabel.fromTime = stopLabel.fromTime;
                                            newLabel.toTime = stopLabel.toTime;
                                            newLabel.refundOrderId = stopLabel.refundOrderId;
                                            newLabel.retourDone = false;
                                            //label.isScanned = false;
                                            stopLabelDao.insert(newLabel);
                                        }
                                    }
                            }

                        //update the number of complete and incomplete labels to reflect the newly inserted labels
                        stopDao.updateCompleteLabelsCountForStop(stopToUpdate.stopId);
                        stopDao.updateInCompleteLabelsCountForStop(stopToUpdate.stopId);
                        continue;
                    } else {
                        //Stop did not exist before; thus, set its status to either the server status, or the NotStarted
                        if (jsonStop.status == null || jsonStop.status.equals(""))
                            jsonStop.status = StopStatus.NotStarted.getStopStatusCode();


                        Stop stop = new Stop();
                        if (jsonStop.address != null) {
                            stop.addressId = jsonStop.address.addressId;
                            stop.addressBuilding = jsonStop.address.building;
                            stop.addressCity = jsonStop.address.city;
                            stop.addressFloor = jsonStop.address.floor;
                            stop.addressLandmark = jsonStop.address.landmark;
                            stop.addressStreet = jsonStop.address.street;
                            stop.addressLongitude = jsonStop.address.longitude;
                            stop.addressLatitude = jsonStop.address.latitude;
                        }
                        stop.userName = DriverServices.mToken.getValue().UserName;
                        stop.clientName = jsonStop.clientName;
                        stop.numberOfPieces = jsonStop.numberOfPieces;
                        stop.orderTypeId = orderType.getOrderTypeId();// OrderType.DELIVERY.getOrderTypeId();
                        stop.service = jsonStop.service;
                        stop.shipperName = jsonStop.shipperName;
                        stop.status = jsonStop.status;
                        stop.stopId = jsonStop.stopId;
                        stop.numberOfLabels = jsonStop.getNumberOfLabels();
                        stop.codLPB = jsonStop.getCODLBP();
                        stop.codUSD = jsonStop.getCODUSD();
                        stop.contactPhone = jsonStop.getContactPhone();
                        stop.oneLabel = jsonStop.getOneLabel();
                        stop.numberOfContacts = jsonStop.getNumberOfContacts();
                        stop.dateInserted = TypeConverters.dateToTimestamp(new Date());
                        stop.numberOfInCompleteLabels = 100;
                        stop.numberOfCompleteLabels = 0;
                        stopDao.insert(stop);

                        if (jsonStop.contact != null)
                            for (JsonStop.Contact stopContact :
                                    jsonStop.contact) {


                                StopContact contact = new StopContact();
                                contact.companyName = stopContact.companyName;
                                contact.contactId = stopContact.contactId;

                                if (stopContact.firstName == null) stopContact.firstName = "";
                                contact.firstName = stopContact.firstName;

                                if (stopContact.lastName == null) stopContact.lastName = "";
                                contact.lastName = stopContact.lastName;

                                contact.mobile = stopContact.mobile;
                                contact.orderTypeId = orderType.getOrderTypeId();
                                OrderType.DELIVERY.getOrderTypeId();
                                contact.phone = stopContact.phone;
                                if (stopContact.salutation == null) stopContact.salutation = "";
                                contact.salutation = stopContact.salutation;
                                contact.stopId = stop.stopId;
                                contact.numberOfLabels = stopContact.getNumberOfLabels();
                                contact.numberOfCODs = stopContact.getNumberOfCODs();
                                contact.totalCODLBP = stopContact.getTotalCODLBP();
                                contact.totalCODUSD = stopContact.getTotalCODUSD();

                                //stopContactDao.deleteStopContact(stopContact.contactId);

                                stopContactDao.insert(contact);

                                if (stopContact.stoplabel != null)
                                    for (JsonStop.Contact.StopLabel stopLabel :
                                            stopContact.stoplabel) {

                                        StopLabel label = new StopLabel();
                                        label.codLBP = stopLabel.codLBP;
                                        label.codUSD = stopLabel.codUSD;
                                        label.contactId = stopContact.contactId;
                                        label.hawb = stopLabel.hawb;
                                        label.numberOfPieces = stopLabel.numberOfPieces;
                                        label.orderId = stopLabel.orderId;
                                        label.orderTypeId = orderType.getOrderTypeId();// OrderType.DELIVERY.getOrderTypeId();
                                        label.shipmentType = stopLabel.shipmentType;
                                        label.shipperReference = stopLabel.shipperReference;
                                        label.status = LabelStatus.NotScanned.getLabelStatusCode();
                                        label.stopId = contact.stopId;
                                        label.specialInstructions = stopLabel.specialInstructions;
                                        label.hasRetour = stopLabel.hasRetour;
                                        label.hasRefund = stopLabel.hasRefund;
                                        label.refundAmount = stopLabel.refundAmount;
                                        label.refundAmountLBP = stopLabel.refundAmountLBP;
                                        label.retourInstructions = stopLabel.retourInstructions;
                                        label.fromTime = stopLabel.fromTime;
                                        label.toTime = stopLabel.toTime;
                                        label.consigneeCity = stopLabel.consigneeCity;
                                        label.consigneeName = stopLabel.consigneeName;
                                        label.refundOrderId = stopLabel.refundOrderId;
                                        label.retourDone = false;
                                        //label.isScanned = false;
                                        stopLabelDao.insert(label);
                                    }
                                if (GlobalCoordinator.testMode) {
                                    StopLabel label = new StopLabel();
                                    label.codLBP = 0.0;
                                    label.codUSD = 0.0;
                                    label.contactId = stopContact.contactId;
                                    label.hawb = "100119912000100000";
                                    label.numberOfPieces = 2;
                                    label.orderId = "A5F72A92-FE56-EA11-81EC-000C29EA376A";
                                    label.orderTypeId = orderType.getOrderTypeId();// OrderType.DELIVERY.getOrderTypeId();
                                    if (alternatingTestShipmentTypes) {
                                        label.shipmentType = ShipmentType.NetLocker.getShipmentTypeId();
                                        alternatingTestShipmentTypes = false;
                                    } else {
                                        label.shipmentType = ShipmentType.NetPoint.getShipmentTypeId();
                                        alternatingTestShipmentTypes = true;
                                    }
                                    label.status = LabelStatus.NotScanned.getLabelStatusCode();
                                    label.stopId = contact.stopId;
                                    //label.isScanned = false;
                                    label.specialInstructions = "";
                                    label.hasRetour = true;
                                    label.hasRefund = true;
                                    label.refundAmount = 10.0;
                                    label.refundAmountLBP = 75000.0;
                                    label.retourInstructions = "some instructions";
                                    label.fromTime = "";
                                    label.toTime = "";
                                    label.consigneeCity = "Beirut";
                                    label.consigneeName = "Rami Farran";
                                    label.refundOrderId = "111122223333";
                                    label.retourDone = false;
                                    stopLabelDao.insert(label);
                                }
                            }


                        stopDao.updateCompleteLabelsCountForStop(stop.stopId);
                        stopDao.updateInCompleteLabelsCountForStop(stop.stopId);
                    }
                }
        }
    }

    private static class getStopByIdAsync extends AsyncTask<String, Void, Stop> {

        private StopDao stopDao;

        public getStopByIdAsync(StopDao stopDao){
            this.stopDao = stopDao;
        }
        @Override
        protected Stop doInBackground(String... stopIds) {
            stopDao.updateInCompleteLabelsCountForStop(stopIds[0]);
            stopDao.updateCompleteLabelsCountForStop(stopIds[0]);
            Stop[] stops = stopDao.getStopOfId(stopIds[0]);
            if(stops!=null && stops.length >0)
               return stops[0];
            return null;
        }
        @Override
        protected void onPostExecute(Stop stop) {
            super.onPostExecute(stop);

        }

    }
    private static class updateStopLabelAsync extends AsyncTask<StopLabel, Void, Void> {

        private StopLabelDao stopLabelDao;
        private StopDao stopDao;

        public updateStopLabelAsync(StopLabelDao stopLabelDao, StopDao stopDao){
            this.stopLabelDao = stopLabelDao;
            this.stopDao = stopDao;
        }
        @Override
        protected Void doInBackground(StopLabel... stopLabels) {
            stopLabelDao.update(stopLabels[0]);
            //int i = stopDao.returnInCompleteLabelsForStop(stopLabels[0].stopId);
            stopDao.updateInCompleteLabelsCountForStop(stopLabels[0].stopId);
            stopDao.updateCompleteLabelsCountForStop(stopLabels[0].stopId);
            return null;
        }
    }

    private static class deleteStopLabelAsync extends AsyncTask<StopLabel, Void, Void> {

        private StopLabelDao stopLabelDao;
        private StopDao stopDao;
        public deleteStopLabelAsync(StopLabelDao stopLabelDao, StopDao stopDao){
            this.stopLabelDao = stopLabelDao;
            this.stopDao = stopDao;
        }
        @Override
        protected Void doInBackground(StopLabel... stopLabels) {
            stopLabelDao.delete(stopLabels[0]);
            //int i = stopDao.returnInCompleteLabelsForStop(stopLabels[0].stopId);
            stopDao.updateInCompleteLabelsCountForStop(stopLabels[0].stopId);
            stopDao.updateCompleteLabelsCountForStop(stopLabels[0].stopId);
            return null;
        }
    }


    private static class insertStopLabelAsync extends AsyncTask<StopLabel, Void, Void> {

        private StopLabelDao stopLabelDao;
        private StopDao stopDao;
        public insertStopLabelAsync(StopLabelDao stopLabelDao, StopDao stopDao){
            this.stopLabelDao = stopLabelDao;
            this.stopDao = stopDao;
        }
        @Override
        protected Void doInBackground(StopLabel... stopLabels) {
            stopLabelDao.insert(stopLabels[0]);
            //int i = stopDao.returnInCompleteLabelsForStop(stopLabels[0].stopId);
            stopDao.updateInCompleteLabelsCountForStop(stopLabels[0].stopId);
            stopDao.updateCompleteLabelsCountForStop(stopLabels[0].stopId);
            return null;
        }
    }

    private static class updateStopAsync extends AsyncTask<Stop, Void, Void> {

        private StopDao stopDao;
        public updateStopAsync(StopDao stopDao){
            this.stopDao = stopDao;
        }
        @Override
        protected Void doInBackground(Stop... stops) {
            stopDao.update(stops[0]);
            return null;
        }
    }


    private static class deleteOrdersAsync extends AsyncTask<String, Void, Void>{
        private StopDao stopDao;
        private StopContactDao stopContactDao;
        private StopLabelDao stopLabelDao;
        public deleteOrdersAsync(StopDao stopDao, StopContactDao stopContactDao, StopLabelDao stopLabelDao){
            this.stopDao = stopDao;
            this.stopContactDao = stopContactDao;
            this.stopLabelDao = stopLabelDao;
        }

        @Override
        protected Void doInBackground(String... strings) {
            String orderTypeId = strings[0];
            stopDao.deleteAllStopsofType(orderTypeId);
            stopContactDao.deleteAllStopContactsofType(orderTypeId);
            stopLabelDao.deleteAllStopLabelsofType(orderTypeId);
            return null;
        }
    }

    private static class getStopOfLabelAsync extends AsyncTask<String,Void, Stop>{
        private StopLabelDao stopLabelDao;
        public boolean foundStop;
        public Stop stop;
        private String orderTypeId;
        private String userName;
        private long dateInserted;

        public getStopOfLabelAsync(StopLabelDao stopLabelDao, String orderTypeId, String userName, long dateInserted){
            this.stopLabelDao = stopLabelDao;
            this.orderTypeId = orderTypeId;
            this.userName = userName;
            this.dateInserted = dateInserted;
        }

        @Override
        protected Stop doInBackground(String... strings) {
            Stop[] stops = stopLabelDao.getStopOfLabel(orderTypeId,strings[0],userName,dateInserted);
            if(stops!=null && stops.length>0){
                foundStop = true;
                stop = stops[0];
                return stops[0];
            }
            return  null;
        }

        @Override
        protected void onPostExecute(Stop stop) {
            super.onPostExecute(stop);

        }
    }

    private static class updateCODAsync extends AsyncTask<updateCODType, Void, Void>{

        private StopLabelDao _stopLabelDao;
        public updateCODAsync(StopLabelDao stopLabelDao){
            this._stopLabelDao = stopLabelDao;
        }
        @Override
        protected Void doInBackground(updateCODType... updateCODTypes) {
            _stopLabelDao.updateCOD(updateCODTypes[0].orderId,updateCODTypes[0].hawb,updateCODTypes[0].codLBP,updateCODTypes[0].codUSD);
            return null;
        }
    }
}
