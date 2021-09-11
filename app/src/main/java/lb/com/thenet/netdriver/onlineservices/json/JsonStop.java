package lb.com.thenet.netdriver.onlineservices.json;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import lb.com.thenet.netdriver.TypeConverters;

public class JsonStop {



    public String stopId;
    public String clientName;
    public String shipperName;
    public String service;
    public int numberOfPieces;

    public Address address;

    public Contact[] contact;

    public String status;

    public int getNumberOfLabels() {
        int retVal = 0;
        if(this.contact!= null)
        {
            for (Contact c :
                    contact) {
                if (c.stoplabel != null){
                    for (Contact.StopLabel l :
                            c.stoplabel) {
                        retVal++;
                    }
                }
            }
        }
        return retVal;
    }

    public int getNumberOfContacts(){
        int retVal = 0;
        if(this.contact!= null) {
            retVal = this.contact.length;
        }
        return retVal;
    }

    public double getCODLBP() {
        double retVal = 0.0;
        if(this.contact != null){
            for (Contact c :
                    contact) {
                if (c.stoplabel != null)
                    for (Contact.StopLabel s :
                            c.stoplabel) {
                        retVal += s.codLBP;
                    }
            }
        }
        return retVal;
    }
    public double getCODUSD() {
        double retVal = 0.0;
        if(this.contact != null){
            for (Contact c :
                    contact) {
                if (c.stoplabel != null)
                    for (Contact.StopLabel s :
                            c.stoplabel) {
                        retVal += s.codUSD;
                    }
            }
        }
        return retVal;
    }

    public String getContactPhone() {
        String retVal = "";
        if(this.contact != null && this.contact.length > 0){
            retVal = this.contact[0].phone;
        }
        return retVal;
    }

    public String getOneLabel() {
        if(this.contact != null && this.contact.length == 1 && this.contact[0].stoplabel != null && this.contact[0].stoplabel.length == 1)
            return this.contact[0].stoplabel[0].hawb;
        return "";
    }


    public class Address{
        public String addressId;
        public String city;
        public String street;
        public String building;
        public String floor;
        public String landmark;
        public double longitude;
        public double latitude;
    }

    public class Contact{
        public long contactId;
        public String companyName;
        public String salutation;
        public String firstName;
        public String lastName;
        public String mobile;
        public String phone;
        public StopLabel[] stoplabel;

        public int getNumberOfLabels() {
            int retVal = 0;
            if(this.stoplabel != null){
                retVal = stoplabel.length;
            }
            return retVal;
        }

        public int getNumberOfCODs() {
            int retVal = 0;
            if(this.stoplabel != null){
                for (StopLabel l :
                        this.stoplabel) {
                    if (l.codLBP + l.codUSD > 1.0) retVal++;
                }
            }
            return retVal;
        }

        public double getTotalCODLBP() {

            double retVal = 0.0;
            if(this.stoplabel != null){
                for (StopLabel l :
                        this.stoplabel) {
                    retVal += l.codLBP;
                }
            }
            return retVal;
        }

        public double getTotalCODUSD() {

            double retVal = 0.0;
            if(this.stoplabel != null){
                for (StopLabel l :
                        this.stoplabel) {
                    retVal += l.codUSD;
                }
            }
            return retVal;
        }

        public class StopLabel{
            public String orderId;
            public String hawb;
            public int numberOfPieces;
            public double codLBP;
            public double codUSD;
            public String shipmentType;
            public String shipperReference;
            public String specialInstructions;
            public boolean hasRetour;
            public boolean hasRefund;
            public double refundAmountLBP;
            public double refundAmount;
            public String retourInstructions;
            public String fromTime;
            public String toTime;
            public String consigneeName;
            public String consigneeCity;
            public String refundOrderId;

        }

    }


}
