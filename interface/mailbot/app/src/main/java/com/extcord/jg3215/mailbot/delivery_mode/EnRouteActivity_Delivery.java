package com.extcord.jg3215.mailbot.delivery_mode;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.extcord.jg3215.mailbot.R;
import com.extcord.jg3215.mailbot.collection_mode.MainActivity_Collection;
import com.extcord.jg3215.mailbot.database.LockerItem;
import com.extcord.jg3215.mailbot.email.eMailService;

import java.util.List;
import java.util.concurrent.locks.Lock;

/**
 * Created by javigeis on 12/11/2018.
 */

public class EnRouteActivity_Delivery extends AppCompatActivity{

    private final static String TAG = "EnRouteActivity";

    private boolean personDetected = false;

    private String[] deliveryLocations;

    private List<LockerItem> lockerItemList;

    // TODO: Figure out which mail item (in the 7 digit string) is being delivered

    // Listen for message (Serial communication) that MailBot is at destination
    // TODO: Register and unregister Broadcast Receivers in onCreate() and when no longer needed (respectively)
    BroadcastReceiver mBroadcastReceiverArrival = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String text = intent.getStringExtra("arrival");
            Log.i(TAG, "Received: " + text);

            // Get the pin code for this mail item

            // Play knock sound 4 times over the space of 2 minutes
            // Begin a timer at the first knock

            // if personDetected at any point within the 2 minutes
                Log.i(TAG, "MailBot has detected the presence of a recipient: personDetected = " + String.valueOf(personDetected));
                toPasswordActivity();
            // if timer >= 2 minutes
                toUnsuccessfulActivity();
        }
    };

    // Listen for message (Serial communication) that MailBot has detected a person
    // TODO: Register and unregister Broadcast Receivers in onCreate() and when no longer needed (respectively)
    BroadcastReceiver mBroadcastReceiverPersonDetected = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String text = intent.getStringExtra("detectPerson");
            Log.i(TAG, "Received: " + text);

            personDetected = true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.delivery_activity_enroute);
        Log.i(TAG, "onCreate() method called");

        lockerItemList = MainActivity_Collection.lockerItemDatabase.lockerDataAccessObject().readLockerItem();
        deliveryLocations = getDeliveryLocations(lockerItemList);
        sendEmails();
    }

    // Provides locations but also checks that database do what is supposed to do
    private String[] getDeliveryLocations(List<LockerItem> itemList) {
        Log.i(TAG, "getDeliveryLocations() method called");

        String[] locationList = new String[7];
        int index = 0;

        for (LockerItem lockerItems : itemList) {
            int nLocker = lockerItems.getLockerNo();
            String sName = lockerItems.getSenderName();
            String rName = lockerItems.getRecipientName();

            String dLocation = lockerItems.getDeliveryLocation();

            locationList[index] = dLocation;

            Log.i(TAG, "Index = " + String.valueOf(index));
            Log.i(TAG, "Locker Number: " + String.valueOf(nLocker) + ", Sender Name: " + sName + ", Recipient Name: " + rName + ", Delivery Location: " + dLocation);

            index++;
        }

        // Extract the delivery locations from the database of lockerItem information
        Log.i(TAG, "Delivery Location list successfully created");
        return locationList;
    }

    private void toUnsuccessfulActivity() {
        Log.i(TAG, "toUnsuccessfulActivity() method called");

        Intent toUnsuccessfulIntent = new Intent(this, UnsuccessfulActivity_Delivery.class);
        startActivity(toUnsuccessfulIntent);
        finish();
    }

    private void toPasswordActivity() {
        Log.i(TAG, "toPasswordActivity() method called");

        Intent toPasswordActivityIntent = new Intent(this, PasswordActivity_Delivery.class);

        // TODO: Figure out if there are extras to send

        startActivity(toPasswordActivityIntent);
        finish();
    }

    private void sendEmails() {
        Log.i(TAG, "sendEmails() method called");

        for (String address : senderEmailAddresses) {
            try {
                eMailService mailService = new eMailService("", "");
                mailService.sendMail("Delivery of your mail item", "Your mail item is being delivered to", "", "");
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }

    private int[] getDistinctProfiles;
}
