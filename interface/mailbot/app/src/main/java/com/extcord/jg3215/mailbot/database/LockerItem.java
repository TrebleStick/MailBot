package com.extcord.jg3215.mailbot.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import com.extcord.jg3215.mailbot.PackageData;

/**
 * Created by IChinweze on 27/11/2018.
 */

@Entity (tableName = "lockers")
public class LockerItem {
    @PrimaryKey
    @ColumnInfo
    private int lockerNo;

    @ColumnInfo
    private String senderName;

    @ColumnInfo
    private String senderEmail;

    @ColumnInfo
    private String recipientName;

    @ColumnInfo
    private String recipientEmail;

    @ColumnInfo (name = "delivery_location")
    private String deliveryLocation;

    @ColumnInfo
    private String PINcode;

    public int getLockerNo() {
        return lockerNo;
    }

    public void setLockerNo(int lockerNo) {
        this.lockerNo = lockerNo;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    public String getDeliveryLocation() {
        return deliveryLocation;
    }

    public void setDeliveryLocation(String deliveryLocation) { this.deliveryLocation = deliveryLocation; }

    public String getPINcode() { return PINcode; }

    public void setPINcode(String PINcode){ this.PINcode = PINcode; }
}
