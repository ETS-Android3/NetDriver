package lb.com.thenet.netdriver.rooms.entities;

import android.content.Context;

import java.util.Date;

import lb.com.thenet.netdriver.R;

public class ScannedLocation {
    private Date scannedDate;
    private String locationName;
    private String locationCode;
    private boolean isActive;
    public ScannedLocation(String locationCode, String locationName){
        if(locationName != null && !locationName.isEmpty()) {
            this.isActive = true;
            this.scannedDate = new Date();
            this.locationName = locationName;
            this.locationCode = locationCode;
        }else {
            this.isActive = false;
        }
    }

    public ScannedLocation(){
        this.isActive = false;
    }

    public boolean isActive(Context context) {
        if(isActive) {
            Date currentTime = new Date();
            long millisecs = currentTime.getTime() - scannedDate.getTime();
            Integer refreshMinutes = context.getResources().getInteger(R.integer.placeScanValidityMinutes);

            if (millisecs > refreshMinutes * 60 * 1000) isActive = false;
        }
        return isActive;
    }
    public String getLocationName(){ return locationName;}

    public String getLocationCode() {
        return locationCode;
    }

    public Date getScannedDate() {return scannedDate;}

    public void setInActive(){this.isActive = false;}
}
