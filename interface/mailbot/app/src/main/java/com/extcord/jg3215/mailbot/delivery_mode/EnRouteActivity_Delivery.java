package com.extcord.jg3215.mailbot.delivery_mode;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.extcord.jg3215.mailbot.R;

/**
 * Created by javigeis on 12/11/2018.
 */

public class EnRouteActivity_Delivery extends AppCompatActivity{

    private final static String TAG = "EnRouteActivity";

    private boolean personDetected = false;

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
    }

    private void toUnsuccessfulActivity() {
        Log.i(TAG, "toUnsuccessfulActivity() method called");
        finish();
    }

    private void toPasswordActivity() {
        Log.i(TAG, "toPasswordActivity() method called");
        finish();
    }
}
