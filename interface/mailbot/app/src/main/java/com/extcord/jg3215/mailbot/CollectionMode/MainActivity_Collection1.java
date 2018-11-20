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
        Log.i(TAG, "setDetailsIntent() method called");
        String packageTag = "packageType";

        Intent toDetailActivity = new Intent(this, DetailsActivity_Collection1.class);
        Bundle extras = new Bundle();

        // Adds this extra detail to bundle
        extras.putInt(packageTag, packageType);

        // Bundle added to the intent and contains data indicating what kind of package the user is sending
        toDetailActivity.putExtras(extras);
        startActivity(toDetailActivity);

        Log.i(TAG, "Detail Activity started with the extra: " + packageTag + ": " + String.valueOf(packageType));
    }
}
