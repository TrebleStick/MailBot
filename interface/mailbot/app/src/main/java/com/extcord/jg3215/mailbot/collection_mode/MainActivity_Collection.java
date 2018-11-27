package com.extcord.jg3215.mailbot.collection_mode;

import android.arch.persistence.room.Room;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.extcord.jg3215.mailbot.LockerManager;
import com.extcord.jg3215.mailbot.R;
import com.extcord.jg3215.mailbot.database.LockerItem;
import com.extcord.jg3215.mailbot.database.LockerItemDatabase;
import com.extcord.jg3215.mailbot.delivery_mode.EnRouteActivity_Delivery;

import java.util.List;
import java.util.concurrent.locks.Lock;

public class MainActivity_Collection extends AppCompatActivity {

    // Database is a public, static object so can be accessed by all activities
    // I assume Android Studio provides security against memory leaks for Rooms
    public static LockerItemDatabase lockerItemDatabase;

    // The image views that are being used like buttons
    ImageView letterView;
    ImageView largeLetterView;
    ImageView parcelView;

    // Shows lockerState string
    TextView lockerTextView;

    // Integers used to represent the type of mail that is being sent
    private static final int LETTER_STANDARD = 1;
    private static final int LETTER_LARGE = 2;
    private static final int PARCEL = 3;

    // Tag for debugging
    private static final String TAG = "MainActivity";

    // Listens for message that lockers are full
    BroadcastReceiver mBroadcastReceiverFullLocker = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String text = intent.getStringExtra("lockerFull");
            Log.i(TAG, "MainActivity: Broadcast received: " + text);

            // Go to EnRouteActivity
            toEnRouteActivity();
        }
    };

    private LockerManager mLockerManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.collection1_activity_main);

        View decorView = getWindow().getDecorView();
        // Hide both the navigation bar and the status bar.
        // SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
        // a general rule, you should design your app to hide the status bar whenever you
        // hide the navigation bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        lockerItemDatabase = Room.databaseBuilder(getApplicationContext(), LockerItemDatabase.class, "lockerInfo").allowMainThreadQueries().build();

        // TODO: Make handling of lockerState more robust
        mLockerManager = new LockerManager(this);
        mLockerManager.setLockerState("0000000");

        // TODO: Make handling of database more robust
        clearDatabase();

        // Register broadcast receiver to this instance of the activity
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiverFullLocker, new IntentFilter("lockerFull"));

        letterView = (ImageView) findViewById(R.id.letter);
        letterView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "letter View selected");

                // Determines whether there is a locker free
                int aLockers = mLockerManager.getAvailability(LETTER_STANDARD);
                if (aLockers > 0) {
                    Log.i(TAG, "Lockers available = " + String.valueOf(aLockers));
                    toDetailsActivity(LETTER_STANDARD);
                } else {
                    Log.i(TAG, "No lockers available");
                    Toast.makeText(MainActivity_Collection.this, getResources().getString(R.string.lockerSizeUnavailable), Toast.LENGTH_SHORT).show();
                }
            }
        });

        lockerTextView = (TextView) findViewById(R.id.testLockerManager);
        lockerTextView.setText(mLockerManager.getLockerState());

        largeLetterView = (ImageView) findViewById(R.id.largeLetter);
        largeLetterView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "Large letter View selected");

                // Determines whether there is a locker free
                int aLockers = mLockerManager.getAvailability(LETTER_LARGE);
                if (aLockers > 0) {
                    Log.i(TAG, "Lockers available = " + String.valueOf(aLockers));
                    toDetailsActivity(LETTER_LARGE);
                } else {
                    Log.i(TAG, "No lockers available");
                    Toast.makeText(MainActivity_Collection.this, getResources().getString(R.string.lockerSizeUnavailable), Toast.LENGTH_SHORT).show();
                }
            }
        });

        parcelView = (ImageView) findViewById(R.id.largeParcel);
        parcelView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "parcel View selected");

                // Determines whether there is a locker free
                int aLockers = mLockerManager.getAvailability(PARCEL);
                if (aLockers > 0) {
                    Log.i(TAG, "Lockers available = " + String.valueOf(aLockers));
                    toDetailsActivity(PARCEL);
                } else {
                    Log.i(TAG, "No lockers available");
                    Toast.makeText(MainActivity_Collection.this, getResources().getString(R.string.lockerSizeUnavailable), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void toDetailsActivity(int packageType) {
        Log.i(TAG, "toDetailsActivity() method called");
        String packageTag = "packageType";

        Intent toDetailActivity = new Intent(this, DetailsActivity_Collection.class);
        Bundle extras = new Bundle();

        // Adds this extra detail to bundle
        extras.putInt(packageTag, packageType);

        // Bundle added to the intent and contains data indicating what kind of package the user is sending
        toDetailActivity.putExtras(extras);
        startActivity(toDetailActivity);

        Log.i(TAG, "Detail Activity started with the extra: " + packageTag + ": " + String.valueOf(packageType));
    }

    private void toEnRouteActivity() {
        Log.i(TAG, "toEnRouteActivity() method called");
        String locationsTag = "locations";
        String recipientEmailTag = "rEmail";
        String senderEmailTag = "sEmail";

        Intent toEnRouteActivityIntent = new Intent(this, EnRouteActivity_Delivery.class);
        Bundle extras = new Bundle();

        // TODO: Figure out extras to send
        extras.putStringArray(locationsTag, deliveryLocations);
        extras.putStringArray(recipientEmailTag, recipientEmailAddresses);
        extras.putStringArray(senderEmailTag, senderEmailAddresses);

        startActivity(toEnRouteActivityIntent);
    }

    protected void onResume() {
        super.onResume();

        Log.i(TAG, "Locker state = " + mLockerManager.getLockerState());
        lockerTextView.setText(mLockerManager.getLockerState());
    }

    private void clearDatabase() {
        Log.i(TAG, "clearDatabase() method called");
        List<LockerItem> lockerItemList = MainActivity_Collection.lockerItemDatabase.lockerDataAccessObject().readLockerItem();
        int itemListLength = lockerItemList.size();

        for (int i = 0; i < itemListLength; i++) {
            int primaryKey = lockerItemList.get(i).getLockerNo();
            LockerItem delItem = new LockerItem();
            delItem.setLockerNo(primaryKey);

            Log.i(TAG, "Item to delete has index = " + String.valueOf(primaryKey));
            MainActivity_Collection.lockerItemDatabase.lockerDataAccessObject().deleteLockerItem(delItem);
            Log.i(TAG, "Item successfully deleted");
        }
    }

    protected void onDestroy() {
        mLockerManager.unregisterListener();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiverFullLocker);
        super.onDestroy();
    }
}
