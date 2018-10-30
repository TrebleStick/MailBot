package com.extcord.jg3215.mailbot;

/**
 * Created by IChinweze on 30/10/2018.
 */

public class PackageData {
    // TODO: Check that is/is not the best way to format data to be sent along
    private String Name;
    private String emailAddress;

    // TODO: Check what the format of the delivery location is going to be
    private String deliveryLocation;

    private boolean photoOptionState;

    // Constructor
    public PackageData(String userName, String userEmail, boolean optionState) {
        Name = userName;
        emailAddress = userEmail;
        photoOptionState = optionState;
    }

    // TODO: Check if the set methods are unnecessary
    // Might only be necessary for delivery location
    public void setName(String nameToSet) {
        nameToSet = this.Name;
    }

    public String getName() {
        return this.Name;
    }

    public void setEmailAddress(String addressToSet) {
        addressToSet = this.emailAddress;
    }

    public String getEmailAddress() {
        return this.emailAddress;
    }

    public void setDeliveryLocation(String deliveryLocationToSet) {
        deliveryLocationToSet = this.deliveryLocation;
    }

    public String getDeliveryLocation() {
        return this.deliveryLocation;
    }

    public boolean getPhotoOptionState() {  return photoOptionState;}
}
