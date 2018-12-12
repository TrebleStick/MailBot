package com.extcord.jg3215.mailbot.delivery_mode;

import android.app.ProgressDialog;
import android.arch.persistence.room.Room;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.extcord.jg3215.mailbot.BluetoothConnectionService;
import com.extcord.jg3215.mailbot.LockerManager;
import com.extcord.jg3215.mailbot.PackageData;
import com.extcord.jg3215.mailbot.R;
import com.extcord.jg3215.mailbot.StopWatch;
import com.extcord.jg3215.mailbot.collection_mode.MainActivity_Collection;
import com.extcord.jg3215.mailbot.database.LockerItem;
import com.extcord.jg3215.mailbot.database.LockerItemDatabase;
import com.extcord.jg3215.mailbot.email.eMailService;

import java.lang.ref.WeakReference;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;

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

    private LockerManager mLockerManager;

    private TextView recipientTv;

    private StopWatch knockTimer;

    private PackageData SenderDetails;
    private PackageData RecipientDetails;

    private AudioManager audioManager;

    private LockerItemDatabase lockerItemDatabase;

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

                    if (destinationPos.equals(LockerManager.HOME)) {
                        // If the tablet gets this location, it will interpret MailBot's state as completely empty
                        lockerItemDatabase.lockerDataAccessObject().clearDatabase();
                        mLockerManager.setLockerState(LockerManager.EMPTY_LOCKER);
                        returnToMainActivity();
                    } else {
                        // This is a try-catch in case there has been an error (ie no matches for destinationPos)
                        List<LockerItem> currentLockers;
                        try {
                            // Get the lockers associated with this delivery address
                            // This is what could bring up the exception
                            currentLockers = lockerItemDatabase.lockerDataAccessObject().findLockerByLocation(destinationPos);

                            if (currentLockers != null) {
                                Log.i(TAG, "At the location: " + destinationPos + " there are: " + String.valueOf(currentLockers.size()) + " items to be delivered.");
                                Log.i(TAG, "Recipient ID: " + currentLockers.get(0).getLockerNo());

                                // Get the pin code for this mail item
                                pinCode = currentLockers.get(0).getPINcode();
                                Log.i(TAG, "Pin Code: " + pinCode);

                                // Is it possible to open multiple lockers at once? In case a recipient has multiple items to collect
                                    // No - each item will have a different PIN Code
                                lockerID = currentLockers.get(0).getLockerNo();

                                // TODO: Tell the user the packageType that is being delivered (in case they are expecting multiple items

                                // Retrieve sender/recipient data
                                SenderDetails = new PackageData(currentLockers.get(0).getSenderName(), currentLockers.get(0).getSenderEmail());
                                RecipientDetails = new PackageData(currentLockers.get(0).getRecipientName(), currentLockers.get(0).getRecipientEmail());

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
                        } catch (Exception e) {
                            Log.i(TAG, "There are no locker items at: " + destinationPos + ": " + e.getMessage());
                            Toast.makeText(EnRouteActivity_Delivery.this, getResources().getString(R.string.locationMissing) + ": " + destinationPos, Toast.LENGTH_SHORT).show();
                        }
                    }
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

            // TODO: Check that this plays music out of the speakers!
            AudioManager audioManager = (AudioManager) (EnRouteActivity_Delivery.this).getSystemService(Context.AUDIO_SERVICE);
            if (audioManager != null) {
                // Turns tablet speakerphone on
                if (!audioManager.isSpeakerphoneOn()) {
                    audioManager.setSpeakerphoneOn(true);
                }
            } else {
                Log.i(TAG, "audioManager is null");
            }

            AudioAttributes attributes = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
                                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build();
            final MediaPlayer mp = MediaPlayer.create(mContext, R.raw.knock_sound, attributes, 0);
            mp.start();
        }
    };

    private BroadcastReceiver mBroadcastReceiverFail = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Timer has timed out. Delivery considered unsuccessful.");
            audioManager.setSpeakerphoneOn(false);
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
                    audioManager.setSpeakerphoneOn(false);
                    toPasswordActivity();
                }
            });

            // Only sends emails if all lockers are perceived as being full
            // This avoids sending the email each time EnRouteActivity is created

            lockerItemDatabase = Room.databaseBuilder(getApplicationContext(), LockerItemDatabase.class, DATABASE_NAME).allowMainThreadQueries().build();
            if (mLockerManager.getLockerState().equals(LockerManager.FULL_LOCKER)) {
                new sendUpdateEmails(EnRouteActivity_Delivery.this).execute();
            }
            
            audioManager = (AudioManager) (EnRouteActivity_Delivery.this).getSystemService(Context.AUDIO_SERVICE);
        }
    }

    private static class sendUpdateEmails extends AsyncTask<Void, Void, Void> {

        WeakReference<EnRouteActivity_Delivery> context;

        public sendUpdateEmails(Context context) {
            this.context = new WeakReference<>((EnRouteActivity_Delivery) context);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Log.i(TAG, "AsyncTask: Sending update emails to recipients in the background.");
            LockerItemDatabase lockerItemDatabase = LockerItemDatabase.getInstance(context.get());

            // Used to check if interface has already sent an email to this recipient from the specific sender
            List<String> srList = new ArrayList<>();

            for (int i = 1; i <= 7; i++) {
                LockerItem cLocker = lockerItemDatabase.lockerDataAccessObject().findLockerByID(i);
                StringBuilder srTag = new StringBuilder("");

                String senderName = cLocker.getSenderName();
                srTag.append(senderName);
                String senderEmail = cLocker.getSenderEmail();

                String recipientName = cLocker.getRecipientName();
                srTag.append(recipientName);
                String recipientEmail = cLocker.getRecipientEmail();

                String srTagFinal = srTag.toString();
                Log.i(TAG, "Async Task: srTag = " + srTagFinal);
                String pinCode = cLocker.getPINcode();

                // This should make it only send an email to repeat sender-recipient pair once
                if (!srList.contains(srTagFinal)) {
                    srList.add(srTagFinal);
                    sendEmails(senderName, senderEmail, recipientName, recipientEmail, pinCode);
                    Log.i(TAG, "AsyncTask: Emails sent");
                }
            }
            return null;
        }

        private void sendEmails(final String senderName, final String senderEmail, final String recipientName, final String recipientEmail, final String pinCode) {
            Log.i(TAG, "sendEmails() method called");

            // Task runs in the background so do not load up the progress dialog
            /* final ProgressDialog dialog = new ProgressDialog(context.get());
            dialog.setTitle("Sending Delivery Update to Recipient");
            dialog.setMessage("Please wait");
            dialog.show(); */

            final Thread recipient = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        // send email to recipient
                        eMailService sender = new eMailService("mailbot.noreply18@gmail.com", "mailbotHCR");
                        sender.sendMail("Your mail item is on its way!",
                                "Dear " + recipientName + "\n\nA mail item addressed to you"
                                        + " from " + senderName + " is on its way to you just now. " +
                                        "Make sure to be at the delivery location to receive it. Remember that the" +
                                        " pin code to open the locker will be " + pinCode + ".\n\nSee you soon!\n\nMailBot",
                                senderEmail,
                                recipientEmail);
                        // dialog.dismiss();
                    } catch (Exception e) {
                        Log.e("recipient error: ", "Error: " + e.getMessage());
                    }
                }
            });
            recipient.start();

            Log.i(TAG, "Email sent to: " + recipientEmail);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Log.i(TAG, "AsyncTask: Complete.");
            super.onPostExecute(aVoid);
        }
    }

    protected void onResume() {
        super.onResume();

        // TODO: Wait for computer to tell interface that it is at "LoadingBay" to cue finish();
            // Get rid of the finish() in this onResume method
            // Get rid of the if lockerState == EMPTY LOCKER in onCreate()
        if (mLockerManager.getLockerState().equals(LockerManager.EMPTY_LOCKER)) {
            Log.i(TAG, "Finishing EnRouteActivity");
            finish();
        }
    }

    private void toUnsuccessfulActivity() {
        Log.i(TAG, "toUnsuccessfulActivity() method called");

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

    private void toPasswordActivity() {
        Log.i(TAG, "toPasswordActivity() method called");

        String senderDataTag = "senderData";
        String recipientDataTag = "recipientData";
        String pinCodeTag = "lockerPINCode";
        String lockerTag = "lockerID";

        Intent toPasswordActivityIntent = new Intent(this, PasswordActivity_Delivery.class);

        // Sending extras to the next activity
        Bundle extras = new Bundle();
        extras.putString(pinCodeTag, pinCode);
        extras.putInt(lockerTag, lockerID);
        extras.putParcelable(senderDataTag, SenderDetails);
        extras.putParcelable(recipientDataTag, RecipientDetails);

        Log.i(TAG, "extras added: " + pinCodeTag + ": " + pinCode + ", " + lockerTag + ": " + String.valueOf(lockerID) + senderDataTag + ": " + SenderDetails.getName() + ", " + recipientDataTag + ": " + RecipientDetails.getName());

        toPasswordActivityIntent.putExtras(extras);
        startActivity(toPasswordActivityIntent);
        finish();
    }

    private void returnToMainActivity() {
        Log.i(TAG, "returnToMainActivity() method called");

        Intent returnToMainIntent = new Intent(this, MainActivity_Collection.class);
        startActivity(returnToMainIntent);
        finish();
    }

    // Set the BR to this instance of the class again
    protected void onDestroy() {
        Log.i(TAG, "onDestroy() method called");

        Log.i(TAG, "Broadcast Receivers unregistered");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiverArrival);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiverFail);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiverKnock);

        if (mLockerManager != null) {
            mLockerManager.unregisterListener();
        }
        mLockerManager = null;
        super.onDestroy();
    }
}
