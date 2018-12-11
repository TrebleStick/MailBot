package com.extcord.jg3215.mailbot.delivery_mode;

import android.arch.persistence.room.Room;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.extcord.jg3215.mailbot.BluetoothConnectionService;
import com.extcord.jg3215.mailbot.LockerManager;
import com.extcord.jg3215.mailbot.R;
import com.extcord.jg3215.mailbot.StopWatch;
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
    private final static String DATABASE_NAME = "lockerDB";

    // Indicates whether a person has been detected to delivery to
    // private boolean personDetected = false;

    private String pinCode;

    // For identification purposes of the locker
    private int lockerID;

    private BluetoothConnectionService mBluetoothConnection;

    private Context mContext;

    private boolean finished;

    private LockerManager mLockerManager;

    private TextView recipientTv;

    private StopWatch knockTimer;

    private Button enRouteTest;

    // Listen for message (Serial communication) that MailBot is at destination
    private BroadcastReceiver mBroadcastReceiverArrival = new BroadcastReceiver() {
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
                    LockerItemDatabase lockerItemDatabase;
                    lockerItemDatabase = Room.databaseBuilder(getApplicationContext(), LockerItemDatabase.class, DATABASE_NAME).allowMainThreadQueries().build();
                    List<LockerItem> currentLockers = lockerItemDatabase.lockerDataAccessObject().findLockerByLocation(destinationPos);
                    Log.i(TAG, "At the location: " + destinationPos + " there are: " + String.valueOf(currentLockers.size()) + " items to be delivered.");
                    Log.i(TAG, "Recipient ID: " + currentLockers.get(0).getLockerNo());

                    // Get the pin code for this mail item
                    pinCode = currentLockers.get(0).getPINcode();
                    Log.i(TAG, "Pin Code: " + pinCode);

                    // TODO: Is it possible to open multiple lockers at once? In case a recipient has multiple items to collect
                        // No - each item will have a different PIN Code
                    lockerID = currentLockers.get(0).getLockerNo();

                    // Make the TextView visible and play the knock-knock sound until it is pressed
                    // Essentially a replacement for the person detected thing
                    recipientTv.setVisibility(View.VISIBLE);

                    // Play knock sound 4 times over the space of 2 minutes
                        // First knock: 3s after you arrive at destination
                        // Second knock: 20s after you arrive at destination
                        // Third knock: 60s after you arrive at destination
                        // Fourth knock: 120s after you arrive at destination
                    knockTimer = new StopWatch(mContext, StopWatch.KNOCK_TIMER, 0);
                    // Set timerDuration to 0 as it is preset in StopWatch class

                    // Might not need this here (below)
                    // LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mBroadcastReceiverArrival);
                } else {
                    Log.i(TAG, "Data length: " + separated.length + ", Text: " + text);
                    // Il y a un probleme le lmao
                }
            } else {
                switch (text) {
                    case "8062":
                        String responseCode = "6208";
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
    /* private BroadcastReceiver mBroadcastReceiverPersonDetected = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String text = intent.getStringExtra("detectPerson");
            Log.i(TAG, "Received: " + text);

            toPasswordActivity();
            personDetected = true;
        }
    }; */

    private BroadcastReceiver mBroadcastReceiverKnock = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Intent to play knocking sound received");

            // Is the knock sound long enough?
            final MediaPlayer mp = MediaPlayer.create(mContext, R.raw.knock_sound);
            mp.start();
        }
    };

    private BroadcastReceiver mBroadcastReceiverFail = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Timer has timed out. Delivery considered unsuccessful.");
            toUnsuccessfulActivity();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.delivery_activity_enroute);
        Log.i(TAG, "onCreate() method called");

        mLockerManager = new LockerManager(this);
        if (!mLockerManager.getLockerState().equals("0000000")) {
            mContext = this;
            // mBluetoothConnection.setmContext(mContext);
            mBluetoothConnection = BluetoothConnectionService.getBcsInstance();

            // Register receivers
            LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiverArrival, new IntentFilter("incomingMessage"));
            LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiverKnock, new IntentFilter("playKnock"));
            LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiverFail, new IntentFilter("failNotification"));

            recipientTv = (TextView) findViewById(R.id.textRecipient);
            recipientTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // The person who the mail item is intended for has been detected
                    Log.i(TAG, "TextView onClickListener method has been called");

                    // Stop the timer
                    knockTimer.stopExecuting();
                    toPasswordActivity();
                }
            });

            enRouteTest = (Button) findViewById(R.id.btnDemo);
            enRouteTest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String destinationPos = "0";

                    // Get the lockers associated with this delivery address
                    LockerItemDatabase lockerItemDatabase;
                    lockerItemDatabase = Room.databaseBuilder(getApplicationContext(), LockerItemDatabase.class, DATABASE_NAME).allowMainThreadQueries().build();
                    List<LockerItem> currentLockers = lockerItemDatabase.lockerDataAccessObject().findLockerByLocation(destinationPos);
                    Log.i(TAG, "At the location: " + destinationPos + " there are: " + String.valueOf(currentLockers.size()) + " items to be delivered.");
                    Log.i(TAG, "Recipient ID: " + currentLockers.get(0).getLockerNo());

                    // Get the pin code for this mail item
                    pinCode = currentLockers.get(0).getPINcode();
                    Log.i(TAG, "Pin Code: " + pinCode);

                    // TODO: Is it possible to open multiple lockers at once? In case a recipient has multiple items to collect
                        // No - each item will have a different PIN Code
                    lockerID = currentLockers.get(0).getLockerNo();

                    // Make the TextView visible and play the knock-knock sound until it is pressed
                    // Essentially a replacement for the person detected thing
                    recipientTv.setVisibility(View.VISIBLE);

                    // Play knock sound 4 times over the space of 2 minutes
                        // First knock: 3s after you arrive at destination
                        // Second knock: 20s after you arrive at destination
                        // Third knock: 60s after you arrive at destination
                        // Fourth knock: 120s after you arrive at destination
                    knockTimer = new StopWatch(mContext, StopWatch.KNOCK_TIMER, 0);
                    // Set timerDuration to 0 as it is preset in StopWatch class
                }
            });
        }
    }

    protected void onResume() {
        super.onResume();

        if (mLockerManager.getLockerState().equals(LockerManager.EMPTY_LOCKER)) {
            Log.i(TAG, "Finishing EnRouteActivity");
            finished = true;
            // TODO: Check that calling finish() here does not cause any problems
            finish();
        }
    }

    // Set the BR to this instance of the class again
    protected void onDestroy() {
        Log.i(TAG, "onDestroy() method called");

        if (!finished) {
            Log.i(TAG, "Broadcast Receivers unregistered");
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiverArrival);
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiverFail);
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiverKnock);
        }
        super.onDestroy();
    }

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
}
