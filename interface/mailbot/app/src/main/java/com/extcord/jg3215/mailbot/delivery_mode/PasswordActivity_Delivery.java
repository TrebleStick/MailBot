package com.extcord.jg3215.mailbot.delivery_mode;

import android.arch.persistence.room.Room;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.extcord.jg3215.mailbot.BluetoothConnectionService;
import com.extcord.jg3215.mailbot.LockerManager;
import com.extcord.jg3215.mailbot.PackageData;
import com.extcord.jg3215.mailbot.R;
import com.extcord.jg3215.mailbot.database.LockerItem;
import com.extcord.jg3215.mailbot.database.LockerItemDatabase;

import java.nio.charset.Charset;
import java.util.List;

/**
 * NAME:        PasswordActivity_Delivery.java
 * PURPOSE:     This activity is where the mail item recipient can input the PIN code they received
 *              via email. The tablet sends a request to the PC to open the relevant locker. If the
 *              locker is opened successfully (ie correct PIN has been entered), the application goes
 *              to OpenLockerActivity. If the incorrect PIN is given consistently, the delivery attempt
 *              is marked as unsuccessful and the application goes to UnsuccessfulActivity.
 *
 * AUTHORS:     Ifeanyi Chinweze, Javi Geis
 * NOTES:
 * REVISION:    13/12/2018
 */

public class PasswordActivity_Delivery extends AppCompatActivity {

    private final static String TAG = "PasswordActivity";

    private final static String DATABASE_NAME = "lockerDB";

    // User use this button to submit the pin code they have typed in
    Button submitButton;

    // Collects the pin submitted by the user
    EditText pinEditText;

    private int pinAttempt = 0;

    private String pinCode;

    private int lockerID;

    private PackageData SenderDetails;

    private PackageData RecipientDetails;

    private LockerManager mLockerManager;

    private BluetoothConnectionService mBluetoothConnection;

    // Broadcast Receiver flags when a message is received on the Bluetooth input stream
    private BroadcastReceiver mBroadcastReceiverLockerComm = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String text = intent.getStringExtra("theMessage");
            Log.i(TAG, "Received: " + text);

            switch (text) {
                case "1409":
                    String requestLO = "RLO";
                    byte[] rloBytes = requestLO.getBytes(Charset.defaultCharset());
                    mBluetoothConnection.write(rloBytes);
                    Log.i(TAG, "Written: " + requestLO + " to output stream");
                    break;
                case "num":
                    String lockToOpen = String.valueOf(lockerID);
                    byte[] lockNumBytes = lockToOpen.getBytes(Charset.defaultCharset());
                    mBluetoothConnection.write(lockNumBytes);
                    Log.i(TAG, "Written: " + lockToOpen + " to output stream");

                    // Assume locker opens once requested
                    mLockerManager.updateAvailability((lockerID - 1), false);

                    // Update database
                    LockerItemDatabase lockerItemDatabase;
                    lockerItemDatabase = Room.databaseBuilder(getApplicationContext(), LockerItemDatabase.class, DATABASE_NAME).allowMainThreadQueries().build();
                    LockerItem currentLocker = lockerItemDatabase.lockerDataAccessObject().findLockerByID(lockerID);
                    lockerItemDatabase.lockerDataAccessObject().deleteLockerItem(currentLocker);
                    toOpenLockerActivity();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.delivery_activity_password);
        hideNavigationBar();
        Log.i(TAG, "onCreate() method called");

        // mBluetoothConnection.setmContext(this);
        mBluetoothConnection = BluetoothConnectionService.getBcsInstance();

        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiverLockerComm, new IntentFilter("incomingMessage"));

        mLockerManager = new LockerManager(this);

        Bundle enRouteActivityData = this.getIntent().getExtras();
        if (enRouteActivityData != null) {
            try {
                // Gets pin code
                pinCode = enRouteActivityData.getString("lockerPINCode");
                Log.i(TAG, "Pin code successfully retrieved = " + pinCode);
            } catch (NullPointerException e){
                Log.i(TAG, "No pin code data: " + e.getMessage());
            }

            try {
                // NOTE: LockerID should be the same as the locker number to be opened
                lockerID = enRouteActivityData.getInt("lockerID");
                Log.i(TAG, "Locker ID = " + String.valueOf(lockerID));
            } catch (NullPointerException e) {
                Log.i(TAG, "No ID data: " + e.getMessage());
            }

            try {
                // Gets Sender details
                SenderDetails = enRouteActivityData.getParcelable("senderData");
                Log.i(TAG, "Sender Data: Name: " + SenderDetails.getName() + ", E-mail: " + SenderDetails.getEmailAddress());
            } catch (NullPointerException e) {
                Log.i(TAG, "No Sender Data: " + e.getMessage());
            }

            try {
                // Gets Sender details
                RecipientDetails = enRouteActivityData.getParcelable("recipientData");
                Log.i(TAG, "Recipient Data: Name: " + RecipientDetails.getName() + ", E-mail: " + RecipientDetails.getEmailAddress());
            } catch (NullPointerException e) {
                Log.i(TAG, "No Recipient Data: " + e.getMessage());
            }

        } else {
            Log.i(TAG, "Bundle contains no data, RIP");
            // throw an exception mebbe
        }

        pinEditText = (EditText) findViewById(R.id.pinInput);

        submitButton = (Button) findViewById(R.id.buttonSubmit);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pinEditText.getText().toString().equals(pinCode)) {

                    // Sends a serial message to ROS to request locker opening
                    String startCommCode = "0507";
                    byte[] startBytes = startCommCode.getBytes(Charset.defaultCharset());
                    mBluetoothConnection.write(startBytes);
                    Log.i(TAG, "Written: " + startCommCode + " to Output Stream");
                } else {
                    pinAttempt++;

                    // Tells user that pin was incorrect and how many attempts they have left
                    Toast.makeText(PasswordActivity_Delivery.this, (getResources().getString(R.string.incorrectPin)) + " " + String.valueOf(3 - pinAttempt), Toast.LENGTH_SHORT).show();

                    if (pinAttempt == 3) {
                        toUnsuccessfulActivity();
                    }
                }
            }
        });
    }

    private void toOpenLockerActivity() {
        Log.i(TAG, "toOpenLockerActivity() called");
        Intent toOpenLockerActivityIntent = new Intent(this, OpenLockerActivity_Delivery.class);

        // TODO: Add senderDetails as an extra (if you wish to send the email in OpenLockerActivity
        String senderDataTag = "senderData";
        String recipientDataTag = "recipientData";
        String lockerTag = "lockerID";

        // Create a bundle for holding the extras
        Bundle extras = new Bundle();

        // Adds this extra detail to the intent which indicates:
            // The data given to MailBot about the sender
            // The data given to MailBot about the recipient

        extras.putParcelable(senderDataTag, SenderDetails);
        extras.putParcelable(recipientDataTag, RecipientDetails);
        extras.putInt(lockerTag, lockerID);

        // Add all the extras content to the intent
        toOpenLockerActivityIntent.putExtras(extras);

        startActivity(toOpenLockerActivityIntent);
        finish();
    }

    private void toUnsuccessfulActivity() {
        Log.i(TAG, "toUnsuccessfulActivity() called");
        Intent toUnsuccessfulIntent = new Intent(this, UnsuccessfulActivity_Delivery.class);

        String senderDataTag = "senderData";
        String recipientDataTag = "recipientData";

        // Create a bundle for holding the extras
        Bundle extras = new Bundle();

        // Adds this extra detail to the intent which indicates:
            // The data given to MailBot about the sender
            // The data given to MailBot about the recipient
        extras.putParcelable(senderDataTag, SenderDetails);
        extras.putParcelable(recipientDataTag, RecipientDetails);

        // Add all the extras content to the intent
        toUnsuccessfulIntent.putExtras(extras);

        startActivity(toUnsuccessfulIntent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideNavigationBar();
    }

    private void hideNavigationBar() {
        Log.i(TAG, "hideNavigationBar() method called");
        this.getWindow().getDecorView()
                .setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_FULLSCREEN |
                                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                );
    }

    protected void onDestroy() {
        Log.i(TAG, "onDestroy() called");
        if (mLockerManager != null) {
            mLockerManager.unregisterListener();
        }
        mLockerManager = null;
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiverLockerComm);
        super.onDestroy();
    }
}
