package com.extcord.jg3215.mailbot.delivery_mode;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.extcord.jg3215.mailbot.BluetoothConnectionService;
import com.extcord.jg3215.mailbot.LockerManager;
import com.extcord.jg3215.mailbot.PackageData;
import com.extcord.jg3215.mailbot.R;
import com.extcord.jg3215.mailbot.StopWatch;
import com.extcord.jg3215.mailbot.delivery_mode.UnsuccessfulActivity_Delivery;
import com.extcord.jg3215.mailbot.email.eMailService;

import java.nio.charset.Charset;

/**
 * Created by javigeis on 12/11/2018.
 */

public class UnsuccessfulActivity_Delivery extends AppCompatActivity {

    private final static String TAG = "UnsuccessfulActivity";

    private PackageData SenderDetails;
    private PackageData RecipientDetails;

    private BluetoothConnectionService mBluetoothConnection;

    private BroadcastReceiver mBroadcastReceiverFail = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Timer has timed out. Delivery considered unsuccessful.");

            // Tell computer that delivery attempt is complete
            String startCommCode = "0605";
            byte[] startBytes = startCommCode.getBytes(Charset.defaultCharset());
            mBluetoothConnection.write(startBytes);
            Log.i(TAG, "Written: " + startCommCode + " to Output Stream");
        }
    };

    private BroadcastReceiver mBroadcastReceiverAttemptComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String text = intent.getStringExtra("theMessage");
            Log.i(TAG, "Received: " + text);

            switch (text) {
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

                    // Return toEnRouteActivity
                    toEnRouteActivity();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.delivery_activity_unsuccessful);
        Log.i(TAG, "onCreate() method called");

        mBluetoothConnection = BluetoothConnectionService.getBcsInstance();

        // Bundle boi

        Bundle PasswordActivityData = this.getIntent().getExtras();
        if (PasswordActivityData != null) {

            try {
                // Gets Sender details
                SenderDetails = PasswordActivityData.getParcelable("senderData");
                Log.i(TAG, "Sender Data: Name: " + SenderDetails.getName() + ", E-mail: " + SenderDetails.getEmailAddress());
            } catch (NullPointerException e) {
                Log.i(TAG, "No Sender Data: " + e.getMessage());
            }

            try {
                // Gets Sender details
                RecipientDetails = PasswordActivityData.getParcelable("recipientData");
                Log.i(TAG, "Recipient Data: Name: " + RecipientDetails.getName() + ", E-mail: " + RecipientDetails.getEmailAddress());
            } catch (NullPointerException e) {
                Log.i(TAG, "No Recipient Data: " + e.getMessage());
            }

        } else {
            Log.i(TAG, "Bundle contains no data, RIP");
            // throw an exception mebbe
        }

        sendEmails();

        // Log that delivery was unsuccessful
        // Notify the computer that the delivery was unsuccessful

        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiverFail, new IntentFilter("failNotification"));
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiverAttemptComplete, new IntentFilter("incomingMessage"));

        // Timer begins thread that runs for 10s then sends a message to say that it is done
        // BR registered to this activity is called when the message is received and triggers transition
        // back to enRouteActivity
        StopWatch activityTimer = new StopWatch(this, StopWatch.TIMER, 10);
    }

    private void toEnRouteActivity() {
        Log.i(TAG, "toEnRouteActivity() method called");

        Intent toEnRouteIntent = new Intent(this, EnRouteActivity_Delivery.class);

        startActivity(toEnRouteIntent);
        finish();
    }

    private void sendEmails() {
        Log.i(TAG, "sendEmails() method called");

        String recipientEmail = SenderDetails.getEmailAddress();
        // send email to recipient

        final ProgressDialog dialog = new ProgressDialog(UnsuccessfulActivity_Delivery.this);
        dialog.setTitle("Sending Delivery Update to Recipient");
        dialog.setMessage("Please wait");
        dialog.show();

        final Thread recipient = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    eMailService sender = new eMailService("mailbot.noreply18@gmail.com", "mailbotHCR");
                    sender.sendMail("We couldn't give you your mail item :(",
                            "Dear " + RecipientDetails.getName() + "\n\nI am MailBot and I messaged you to notify you I received a mail item for you"
                                    + " from " + SenderDetails.getName() + ". However, when I attempted to deliver it I could not reach you. " +
                                    "We will attempt to deliver it again in the next time slot." + "\n\nSee you soon!\n\nMailBot",
                            SenderDetails.getEmailAddress(),
                            RecipientDetails.getEmailAddress());
                    dialog.dismiss();
                } catch (Exception e) {
                    Log.e("recipient error: ", "Error: " + e.getMessage());
                }
            }
        });
        recipient.start();

        //
        Log.i(TAG, "Email sent to: " + recipientEmail);

        String senderEmail = RecipientDetails.getEmailAddress();

        // send email to sender

        final ProgressDialog dialog2 = new ProgressDialog(UnsuccessfulActivity_Delivery.this);
        dialog2.setTitle("Sending Delivery Update to Sender");
        dialog2.setMessage("Please wait");
        dialog2.show();

        Thread sender = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    eMailService sender = new eMailService("mailbot.noreply18@gmail.com", "mailbotHCR");
                    sender.sendMail("Delivery of your mail item to " + RecipientDetails.getName() + " was unsuccessful :(",
                            "Hey " + SenderDetails.getName() + ",\n\nThis is Mailbot! I'm just messaging you to inform"
                                    + " you that you gave me a mail item that you wanted me to deliver to " +
                                    RecipientDetails.getName() + ". However, when I attempted to deliver it I could not "+
                                    "reach " + RecipientDetails.getName() + ". I will attempt to deliver it again in the next " +
                                    "delivery slot. \n\nI will let you know if it goes well," + "\n\nMailBot",
                            RecipientDetails.getEmailAddress(),
                            SenderDetails.getEmailAddress());
                    dialog2.dismiss();
                } catch (Exception e) {
                    Log.e("E-mail failed: ", "Error: " + e.getMessage());
                }
            }
        });
        sender.start();

        //
        Log.i(TAG, "Email sent to: " + senderEmail);
    }

    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Broadcast Receiver unregistered.");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiverFail);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiverAttemptComplete);
    }
}
