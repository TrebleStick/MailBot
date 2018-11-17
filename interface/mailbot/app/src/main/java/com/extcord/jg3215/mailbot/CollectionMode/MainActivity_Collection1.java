package com.extcord.jg3215.mailbot.CollectionMode;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.extcord.jg3215.mailbot.PackageData;
import com.extcord.jg3215.mailbot.R;

public class MainActivity_Collection1 extends AppCompatActivity {

    // TODO: Fix issue - if you send another mail item, you cannot exit or cancel (object persists through intents)

    // The image views that are being used like buttons
    ImageView letterView;
    ImageView largeLetterView;
    ImageView parcelView;

    // Integers used to represent the type of mail that is being sent
    private static final int LETTER_STANDARD = 1;
    private static final int LETTER_LARGE = 2;
    private static final int PARCEL = 3;

    // Tag for debugging
    private static final String TAG = "MainActivity";

    private PackageData recipientData;
    private PackageData senderData;

    private boolean entryFromOtherActivity = false;

    // Listen for response (Serial communication) to space query
    // TODO: Get information on space in all locker types - may come in handy later
    BroadcastReceiver mBroadcastReceiverSpaceQuery = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String text = intent.getStringExtra("spaceQueryResponse");
            Log.i(TAG, "Received: " + text);

        }
    };

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

        letterView = (ImageView) findViewById(R.id.letter);
        letterView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "letter View selected");
                // TODO: Communicate to the robot (via Serial) that a small letter is being delivered

                // Might to put the below in a broadcast receiver
                // if (robot says there is space) {
                    toDetailsActivity(LETTER_STANDARD);
                // else { tell the user that no lockers of that size are available
                    // Toast.makeText(MainActivity_Collection1.this, getResources().getString(R.string.lockerSizeUnavailable), Toast.LENGTH_LONG).show();
            }
        });

        largeLetterView = (ImageView) findViewById(R.id.largeLetter);
        largeLetterView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "Large letter View selected");
                // TODO: Communicate to the robot (via Serial) that a small letter is being delivered

                // Might to put the below in a broadcast receiver
                // if (robot says there is space) {
                    toDetailsActivity(LETTER_LARGE);
                // else { tell the user that no lockers of that size are available
                    // Toast.makeText(MainActivity_Collection1.this, getResources().getString(R.string.lockerSizeUnavailable), Toast.LENGTH_LONG).show();
            }
        });

        parcelView = (ImageView) findViewById(R.id.largeParcel);
        parcelView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "parcel View selected");
                // TODO: Communicate to the robot (via Serial) that a small letter is being delivered

                // Might to put the below in a broadcast receiver
                // if (robot says there is space) {
                    toDetailsActivity(PARCEL);
                // else { tell the user that no lockers of that size are available
                    // Toast.makeText(MainActivity_Collection1.this, getResources().getString(R.string.lockerSizeUnavailable), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void toDetailsActivity(int packageType) {
        // TODO: Check that this activity is starting when you want it to (use Logging)
        Log.i(TAG, "setDetailsIntent() method called");
        String packageTag = "packageType";

        Intent toDetailActivity = new Intent(this, DetailsActivity_Collection1.class);
        Bundle extras = new Bundle();

        // Adds this extra detail to bundle
        // Bundle added to the intent and contains data indicating what kind of package the user is sending
        extras.putInt(packageTag, packageType);

        String dataProvidedTag = "dataProvided";
        if (entryFromOtherActivity) {
            addExtras(extras);
            // TODO: Make sure that it does not get here if the objects are empty
            extras.putBoolean(dataProvidedTag, true);
        } else {
            extras.putBoolean(dataProvidedTag, false);
        }

        toDetailActivity.putExtras(extras);
        startActivity(toDetailActivity);

        // Empty the objects again
        senderData = null;
        recipientData = null;

        Log.i(TAG, "Detail Activity started with the extra=" + packageTag);
    }

    // Add data objects to the bundle that is passed to the next activity
    private void addExtras(Bundle extrasBundle) {
        if (senderData != null) {
            String senderDataTag = "senderData";
            extrasBundle.putParcelable(senderDataTag, senderData);
        } else {
            // throw an exception - this object should not be empty
            Log.i(TAG, "Sender data object empty");
            // finish();
            return;
        }

        if (recipientData != null) {
            String recipientDataTag = "recipientData";
            extrasBundle.putParcelable(recipientDataTag, recipientData);
        } else {
            // throw an exception - this object should not be empty
            Log.i(TAG, "Recipient data object empty");
            // finish();
            return;
        }
    }

    // Accesses data passed to MainActivity from other activities
    @Override
    protected void onResume() {
        super.onResume();

        /*
        // Get data from previous activity stored in a bundle
        Bundle endActivityData = this.getIntent().getExtras();

        if (endActivityData != null) {
            Log.i(TAG, "Entered this activity from another activity intent");

            entryFromOtherActivity = true;

            // Prompt the user to pick a locker size
            Toast.makeText(MainActivity_Collection1.this, getResources().getString(R.string.lockerSizePrompt), Toast.LENGTH_LONG).show();

            // Get the sender data to pass to the Details Activity
            senderData = endActivityData.getParcelable("senderData");
            Log.i(TAG, "Sender data: User Name: " + senderData.getName() + ", User Email: " + senderData.getEmailAddress());

            // Get the recipient data to pass to the Details Activity
            recipientData = endActivityData.getParcelable("recipientData");
            Log.i(TAG, " data: Recipient Name: " + recipientData.getName() + ", Recipient Email: " + recipientData.getEmailAddress() + ", Recipient Location: " + recipientData.getDeliveryLocation());
        } else {
            Log.i(TAG, "No data sent from previous activity: Method called following onCreate().");
        } */
    }
}
