package com.extcord.jg3215.mailbot.delivery_mode;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.extcord.jg3215.mailbot.BluetoothConnectionService;
import com.extcord.jg3215.mailbot.LockerManager;
import com.extcord.jg3215.mailbot.R;
import com.extcord.jg3215.mailbot.collection_mode.MainActivity_Collection;

import java.nio.charset.Charset;

// import static com.extcord.jg3215.mailbot.collection_mode.MainActivity_Collection.mBluetoothConnection;

/**
 * Created by javigeis on 12/11/2018.
 */

public class OpenLockerActivity_Delivery extends AppCompatActivity {

    private final static String TAG = "OpenLockerActivity";

    private LockerManager mLockerManager;

    Button doneButton;

    private BluetoothConnectionService mBluetoothConnection;

    private BroadcastReceiver mBroadcastReceiverDeliverySuccess = new BroadcastReceiver() {
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

        mBluetoothConnection = BluetoothConnectionService.getBcsInstance();

        mLockerManager = new LockerManager(this);

        mBluetoothConnection.setmContext(this);
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
            }
        });
    }

    private void toEnRouteActivity() {
        Log.i(TAG, "toEnRouteActivity() method called");

        Intent toEnRouteIntent = new Intent(this, EnRouteActivity_Delivery.class);

        // Change speech bubble text to "Thank you" for ~20s

        // Add extras?

        startActivity(toEnRouteIntent);
        finish();
    }

    private void returnToMainActivity() {
        Log.i(TAG, "returnToMainActivity() method called");

        Intent returnToMainIntent = new Intent(this, MainActivity_Collection.class);
        startActivity(returnToMainIntent);
        finish();
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
