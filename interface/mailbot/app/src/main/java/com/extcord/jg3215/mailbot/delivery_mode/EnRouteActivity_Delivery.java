package com.extcord.jg3215.mailbot.delivery_mode;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.extcord.jg3215.mailbot.BluetoothConnectionService;
import com.extcord.jg3215.mailbot.R;
import com.extcord.jg3215.mailbot.collection_mode.MainActivity_Collection;
import com.extcord.jg3215.mailbot.database.LockerItem;
import com.extcord.jg3215.mailbot.database.LockerItemDatabase;
import com.extcord.jg3215.mailbot.email.eMailService;

import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.locks.Lock;

// import static com.extcord.jg3215.mailbot.collection_mode.MainActivity_Collection.mBluetoothConnection;

/**
 * Created by javigeis on 12/11/2018.
 */

public class EnRouteActivity_Delivery extends AppCompatActivity{

    private final static String TAG = "EnRouteActivity";

    // Indicates whether a person has been detected to delivery to
    // private boolean personDetected = false;

    private Context mContext;

    private String pinCode;

    // For identification purposes of the locker
    private int lockerID;

    private BluetoothConnectionService mBluetoothConnection;

    // Listen for message (Serial communication) that MailBot is at destination
    // TODO: Register and unregister Broadcast Receivers in onCreate() and when no longer needed (respectively)
    BroadcastReceiver mBroadcastReceiverArrival = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String text = intent.getStringExtra("theMessage");
            Log.i(TAG, "Received: " + text);

            if (text.contains(",")) {
                String[] separated = text.split(",");
                Log.i(TAG, "Data length: " + separated.length);

                if (separated.length == 2) {
                    // Data should be good

                    // The location the MailBot is when it is ready to deliver item
                    String destinationPos = separated[1];
                    Log.i(TAG, "Destination Position: " + destinationPos);

                    // Get the lockers associated with this delivery address
                    List<LockerItem> currentLockers = LockerItemDatabase.getInstance(mContext.getApplicationContext()).lockerDataAccessObject().findLockerByLocation(destinationPos);
                    Log.i(TAG, "At the location: " + destinationPos + " there are: " + String.valueOf(currentLockers.size()) + " items to be delivered.");
                    Log.i(TAG, "Recipient ID: " + currentLockers.get(0).getLockerNo());

                    // Get the pin code for this mail item
                    pinCode = currentLockers.get(0).getPINcode();
                    Log.i(TAG, "Pin Code: " + pinCode);

                    lockerID = currentLockers.get(0).getLockerNo();

                    // Play knock sound 4 times over the space of 2 minutes
                    // Begin a timer at the first knock

                    // Assume all delivery attempts are successful for now
                    toPasswordActivity();

                    // Might not need this here (below)
                    // LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mBroadcastReceiverArrival);
                } else {
                    Log.i(TAG, "Data length: " + separated.length);
                    // Il y a un probleme le lmao
                }
            } else {
                switch (text) {
                    case "0507":
                        String responseCode = "1409";
                        byte[] rcBytes = responseCode.getBytes(Charset.defaultCharset());
                        mBluetoothConnection.write(rcBytes);
                        Log.i(TAG, "Written: " + responseCode + " to Output Stream");
                        break;
                    case "adm":
                        String refRequest = "ref";
                        byte[] rrBytes = refRequest.getBytes(Charset.defaultCharset());
                        mBluetoothConnection.write(rrBytes);
                        Log.i(TAG, "Written: " + refRequest + " to Output Stream");
                        break;
                }
            }

            // if personDetected at any point within the 2 minutes
                // Log.i(TAG, "MailBot has detected the presence of a recipient: personDetected = " + String.valueOf(personDetected));
                // toPasswordActivity();
            // if timer >= 2 minutes
                // toUnsuccessfulActivity();
        }
    };

    // Listen for message (Serial communication) that MailBot has detected a person
    // TODO: Register and unregister Broadcast Receivers in onCreate() or when no longer needed
    // TODO: To use later skater -> stretch goals
    /* BroadcastReceiver mBroadcastReceiverPersonDetected = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String text = intent.getStringExtra("detectPerson");
            Log.i(TAG, "Received: " + text);

            toPasswordActivity();
            personDetected = true;
        }
    }; */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.delivery_activity_enroute);
        Log.i(TAG, "onCreate() method called");

        mContext = this;
        // mBluetoothConnection.setmContext(mContext);
        mBluetoothConnection = BluetoothConnectionService.getBcsInstance();

        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiverArrival, new IntentFilter("incomingMessage"));
        //sendEmails();
    }

    // Set the BR to this instance of the class again
    protected void onDestroy() {
        Log.i(TAG, "onDestroy() method called");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiverArrival);
        super.onDestroy();
    }

    // Currently useless
    private void toUnsuccessfulActivity() {
        Log.i(TAG, "toUnsuccessfulActivity() method called");

        Intent toUnsuccessfulIntent = new Intent(this, UnsuccessfulActivity_Delivery.class);
        startActivity(toUnsuccessfulIntent);
        finish();
    }

    private void toPasswordActivity() {
        Log.i(TAG, "toPasswordActivity() method called");
        String pinCodeTag = "lockerPINCode";
        String lockerTag = "lockerID";

        Intent toPasswordActivityIntent = new Intent(this, PasswordActivity_Delivery.class);

        // Sending extras to the next activity
        Bundle extras = new Bundle();
        extras.putString(pinCodeTag, pinCode);
        extras.putInt(lockerTag, lockerID);
        Log.i(TAG, "extras added: " + pinCodeTag + ": " + pinCode + ", " + lockerTag + ": " + String.valueOf(lockerID));

        toPasswordActivityIntent.putExtras(extras);
        startActivity(toPasswordActivityIntent);
        finish();
    }

        /* for (String address : senderEmailAddresses) {
            try {
                eMailService mailService = new eMailService("", "");
                mailService.sendMail("Delivery of your mail item", "Your mail item is being delivered to", "", "");
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        } */

}
