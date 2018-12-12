package com.extcord.jg3215.mailbot.delivery_mode;

import android.arch.persistence.room.Room;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.extcord.jg3215.mailbot.BluetoothConnectionService;
import com.extcord.jg3215.mailbot.LockerManager;
import com.extcord.jg3215.mailbot.PackageData;
import com.extcord.jg3215.mailbot.R;
import com.extcord.jg3215.mailbot.collection_mode.MainActivity_Collection;
import com.extcord.jg3215.mailbot.database.LockerItem;
import com.extcord.jg3215.mailbot.database.LockerItemDatabase;
import com.extcord.jg3215.mailbot.email.eMailService;

import java.nio.charset.Charset;

/**
 * Created by javigeis on 12/11/2018.
 */

public class OpenLockerActivity_Delivery extends AppCompatActivity {

    private final static String TAG = "OpenLockerActivity";

    private LockerManager mLockerManager;

    private PackageData SenderDetails;
    private PackageData RecipientDetails;

    private int lockerID;

    Button doneButton;
    Button retryButton;

    private BluetoothConnectionService mBluetoothConnection;

    private BroadcastReceiver mBroadcastReceiverDeliverySuccess = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String text = intent.getStringExtra("theMessage");
            Log.i(TAG, "Received: " + text);

            switch (text) {
                // The first two cases executed if the lockers are being opened again
                // Locker Re-Open
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
                    break;

                // The last two cases are executed upon delivery success
                // Delivery Complete
                case "5060":
                    String goToNext = "GTN";
                    byte[] gtnBytes = goToNext.getBytes(Charset.defaultCharset());
                    mBluetoothConnection.write(gtnBytes);
                    Log.i(TAG, "Written: " + goToNext + " to Output Stream");
                    break;
                case "done":
                    String response = "yes";
                    byte[] resBytes = response.getBytes(Charset.defaultCharset());
                    mBluetoothConnection.write(resBytes);
                    Log.i(TAG, "Written: " + response + " to Output Stream");

                    int aLockers = mLockerManager.getAvailability(LockerManager.LETTER_STANDARD) + mLockerManager.getAvailability(LockerManager.LETTER_LARGE) + mLockerManager.getAvailability(LockerManager.PARCEL);
                    Log.i(TAG, "Number of full lockers: " + String.valueOf(7 - aLockers));

                    // You are routed back to MainActivity from EnRouteActivity
                    toEnRouteActivity();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.delivery_activity_openlocker);
        Log.i(TAG, "onCreate() method called");

        // Bundle boi

        Bundle OpenLockerActivityData = this.getIntent().getExtras();
        if (OpenLockerActivityData != null) {

            try {
                // Gets Sender details
                SenderDetails = OpenLockerActivityData.getParcelable("senderData");
                Log.i(TAG, "Sender Data: Name: " + SenderDetails.getName() + ", E-mail: " + SenderDetails.getEmailAddress());
            } catch (NullPointerException e) {
                Log.i(TAG, "No Sender Data: " + e.getMessage());
            }

            try {
                // Gets Sender details
                RecipientDetails = OpenLockerActivityData.getParcelable("recipientData");
                Log.i(TAG, "Recipient Data: Name: " + RecipientDetails.getName() + ", E-mail: " + RecipientDetails.getEmailAddress());
            } catch (NullPointerException e) {
                Log.i(TAG, "No Recipient Data: " + e.getMessage());
            }

            try {
                lockerID = OpenLockerActivityData.getInt("lockerID");
                Log.i(TAG, "Locker ID: " + String.valueOf(lockerID));
            } catch (NullPointerException e) {
                Log.i(TAG, "No lockerID Data: " + e.getMessage());
            }
        } else {
            Log.i(TAG, "Bundle contains no data, RIP");
            // throw an exception mebbe
        }

        mBluetoothConnection = BluetoothConnectionService.getBcsInstance();

        mLockerManager = new LockerManager(this);

        // mBluetoothConnection.setmContext(this);
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiverDeliverySuccess, new IntentFilter("incomingMessage"));

        doneButton = (Button) findViewById(R.id.btnStartAgain);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick() button listener method called");

                // Tell computer that delivery was successful and take MailBot to next location
                String startCommCode = "0605";
                byte[] startBytes = startCommCode.getBytes(Charset.defaultCharset());
                mBluetoothConnection.write(startBytes);
                Log.i(TAG, "Written: " + startCommCode + " to Output Stream");

                new sendDeliveryCompleteEmails(SenderDetails, RecipientDetails).execute();
            }
        });

        retryButton = (Button) findViewById(R.id.btnReopen);
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick() button listener method called. Retry button clicked");

                // Sends a serial message to ROS to request locker opening
                String startCommCode = "0507";
                byte[] startBytes = startCommCode.getBytes(Charset.defaultCharset());
                mBluetoothConnection.write(startBytes);
                Log.i(TAG, "Written: " + startCommCode + " to Output Stream");
            }
        });
    }

    private void toEnRouteActivity() {
        Log.i(TAG, "toEnRouteActivity() method called");

        Intent toEnRouteIntent = new Intent(this, EnRouteActivity_Delivery.class);

        // TODO: Briefly change speech bubble to Thank you?
        startActivity(toEnRouteIntent);
        finish();
    }

    /* private void returnToMainActivity() {
        Log.i(TAG, "returnToMainActivity() method called");

        Intent returnToMainIntent = new Intent(this, MainActivity_Collection.class);
        startActivity(returnToMainIntent);
        finish();
    } */

    private static class sendDeliveryCompleteEmails extends AsyncTask<Void, Void, Void> {

        String senderName;
        String senderEmail;

        String recipientName;
        String recipientEmail;

        public sendDeliveryCompleteEmails(PackageData senderData, PackageData recipientData) {
            senderName = senderData.getName();
            senderEmail = senderData.getEmailAddress();

            recipientName = recipientData.getName();
            recipientEmail = recipientData.getEmailAddress();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            recipientEmail(senderName, senderEmail, recipientName, recipientEmail);
            senderEmail(senderName, senderEmail, recipientName, recipientEmail);
            return null;
        }

        private void recipientEmail(final String senderName, final String senderEmail, final String recipientName, final String recipientEmail) {
            Log.i(TAG, "AsyncTask: recipientEmail() method called");
            // send email to recipient
            /* final ProgressDialog dialog = new ProgressDialog(OpenLockerActivity_Delivery.this);
            dialog.setTitle("Sending Delivery Update to Recipient");
            dialog.setMessage("Please wait");
            dialog.show(); */

            final Thread recipient = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        eMailService sender = new eMailService("mailbot.noreply18@gmail.com", "mailbotHCR");
                        sender.sendMail("We successfully delivered you a mail item! :)",
                                "Dear " + recipientName + "\n\nI am MailBot and I messaged you to notify you I delivered a mail item for you"
                                        + " from " + senderName + " just now. Hope your experience with me as your mail delivery method was great! " +
                                        "You can meet me at the collection point to send a parcel or a letter in the future." + "\n\nSee you soon!\n\nMailBot",
                                senderEmail,
                                recipientEmail);
                        // dialog.dismiss();
                    } catch (Exception e) {
                        Log.e("recipient error: ", "Error: " + e.getMessage());
                    }
                }
            });
            recipient.start();
            Log.i(TAG, "AsyncTask: Email sent to: " + recipientEmail);
        }

        private void senderEmail(final String senderName, final String senderEmail, final String recipientName, final String recipientEmail) {
            Log.i(TAG, "AsyncTask: senderEmail() method called");

            // send email to sender
            /* final ProgressDialog dialog2 = new ProgressDialog(OpenLockerActivity_Delivery.this);
            dialog2.setTitle("Sending Delivery Update to Sender");
            dialog2.setMessage("Please wait");
            dialog2.show(); */

            Thread sender = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        eMailService sender = new eMailService("mailbot.noreply18@gmail.com", "mailbotHCR");
                        sender.sendMail("Delivery of your mail item to " + recipientName + " was successful! :)",
                                "Hey " + senderName + ",\n\nThis is Mailbot! I'm just messaging you to inform"
                                        + " you that you gave me a mail item that you wanted me to deliver to " +
                                        recipientName + ". This delivery was carried out successfully, and I'm sure " + recipientName +
                                        " loved it. \n\nThanks for trusting me with this delivery, and hope to see you again!" + "\n\nMailBot",
                                recipientEmail,
                                senderEmail);
                        // dialog2.dismiss();
                    } catch (Exception e) {
                        Log.e("E-mail failed: ", "Error: " + e.getMessage());
                    }
                }
            });
            sender.start();
            Log.i(TAG, "AsyncTask: Email sent to: " + senderEmail);
        }
    }

    protected void onDestroy() {
        if (mLockerManager != null) {
            mLockerManager.unregisterListener();
        }
        mLockerManager = null;
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiverDeliverySuccess);
        super.onDestroy();
    }
}
