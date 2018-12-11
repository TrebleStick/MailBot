package com.extcord.jg3215.mailbot;

import android.os.Parcel;
import android.os.Parcelable;

// Make it parcelable so that its instances can be passed as extras between activities
public class PackageData implements Parcelable{
    private String name;
    private String emailAddress;
    private String deliveryLocation;

    // Constructor
    public PackageData(String userName, String userEmail) {
        name = userName;
        emailAddress = userEmail;
    }

    // Set methods might only be necessary for delivery location
    public void setName(String nameToSet) {
        this.name = nameToSet;
    }

    public String getName() {
        return this.name;
    }

    public void setEmailAddress(String addressToSet) {
        this.emailAddress = addressToSet;
    }

    public String getEmailAddress() {
        return this.emailAddress;
    }

    public void setDeliveryLocation(String deliveryLocationToSet) {
        this.deliveryLocation = deliveryLocationToSet;
    }

    public String getDeliveryLocation() {
        return this.deliveryLocation;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.name);
        out.writeString(this.emailAddress);

        // leaving deliveryLocation as null -> crashing
        if (deliveryLocation != null) {
            // should only take delivery location for recipient data object
            out.writeString(this.deliveryLocation);
        } else {
            out.writeString("");
        }
    }

    public static final Parcelable.Creator<PackageData> CREATOR = new Parcelable.Creator<PackageData>() {
        @Override
        public PackageData createFromParcel(Parcel parcel) {
            return new PackageData(parcel);
        }

        @Override
        public PackageData[] newArray(int size) {
            return new PackageData[size];
        }
    };

    // It needs to read stuff in the order that it was written in
    public PackageData(Parcel parcel) {
        this.name = parcel.readString();
        this.emailAddress = parcel.readString();
        this.deliveryLocation = parcel.readString();
    }
}
