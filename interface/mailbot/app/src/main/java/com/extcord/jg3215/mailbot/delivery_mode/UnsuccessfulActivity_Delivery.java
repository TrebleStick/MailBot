package com.extcord.jg3215.mailbot.delivery_mode;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.extcord.jg3215.mailbot.R;
import com.extcord.jg3215.mailbot.StopWatch;

/**
 * Created by javigeis on 12/11/2018.
 */

public class UnsuccessfulActivity_Delivery extends AppCompatActivity {

    private final static String TAG = "UnsuccessfulActivity";

    private StopWatch activityTimer;

    private BroadcastReceiver mBroadcastReceiverFail = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Timer has timed out. Delivery considered unsuccessful.");
            toEnRouteActivity();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.delivery_activity_unsuccessful);

        // Log that delivery was unsuccessful
        // Notify the computer that the delivery was unsuccessful

        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiverFail, new IntentFilter("failNotification"));

        // Timer begins thread that runs for 15s then sends a message to say that it is done
        // BR registered to this activity is called when the message is received and triggers transition
        // back to enRouteActivity
        activityTimer = new StopWatch(this, StopWatch.TIMER, 15);
    }

    private void toEnRouteActivity() {
        Log.i(TAG, "toEnRouteActivity() method called");

        Intent toEnRouteIntent = new Intent(this, EnRouteActivity_Delivery.class);

        // Add extras?

        startActivity(toEnRouteIntent);
        finish();
    }

    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Broadcast Receiver unregistered.");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiverFail);
    }
}
