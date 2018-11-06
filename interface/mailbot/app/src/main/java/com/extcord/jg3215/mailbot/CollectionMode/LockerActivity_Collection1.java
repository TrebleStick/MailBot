package com.extcord.jg3215.mailbot.CollectionMode;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.extcord.jg3215.mailbot.PackageData;
import com.extcord.jg3215.mailbot.R;

public class LockerActivity_Collection1 extends AppCompatActivity {

    // The TextViews for the two options given at the start of this activity
        // 'Parcel fits' vs 'Parcel does not fit'
    TextView badFitView;
    TextView goodFitView;

    private int packageType;

    private final static String TAG = "LockerActivity";

    private PackageData senderData;
    private PackageData recipientData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locker_collection1);

        View decorView = getWindow().getDecorView();
// Hide both the navigation bar and the status bar.
// SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
// a general rule, you should design your app to hide the status bar whenever you
// hide the navigation bar.
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        packageType = this.getIntent().getIntExtra("packageType", 0);
        Log.i(TAG, "Package Type: " + String.valueOf(packageType));

        senderData = this.getIntent().getParcelableExtra("senderData");
        Log.i(TAG, "Sender data: User Name: " + senderData.getName() + ", User Email: " + senderData.getEmailAddress());

        recipientData = this.getIntent().getParcelableExtra("recipientData");
        Log.i(TAG, " data: Recipient Name: " + recipientData.getName() + ", Recipient Email: " + recipientData.getEmailAddress());

        badFitView = (TextView) findViewById(R.id.textBadFit);
        badFitView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view){
                Log.i(TAG, "badFitView text pressed");
                if (packageType != 3) {
                    Log.i(TAG, "Need to attempt to use a bigger locker");
                    // TODO: Check if a bigger size locker is available
                } else {
                    Log.i(TAG, "The mail item will not fit in MailBot");
                    Toast.makeText(LockerActivity_Collection1.this, getResources().getString(R.string.packageTooBig), Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        });

        goodFitView = (TextView) findViewById(R.id.textGoodFit);
        goodFitView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "goodFitView text pressed");
                // TODO: Tell the computer that the locker is closed?
                // The computer updates the availability of that locker
                // Is there any data that needs to passed on as an extra?
                toEndActivity();
            }
        });
    }

    private void toEndActivity() {
        Log.i(TAG, "toEndActivity() method called");
        String packageTag = "packageType";
        String senderDataTag = "senderData";
        String recipientDataTag = "recipientData";

        Intent toEndActivity = new Intent(this, EndActivity_Collection1.class);

        // Adds this extra detail to the intent which indicates:
            // The kind of package the user is sending
            // The data given to MailBot about the sender
            // The data given to MailBot about the recipient
        toEndActivity.putExtra(packageTag, packageType);
        toEndActivity.putExtra(senderDataTag, senderData);
        toEndActivity.putExtra(recipientDataTag, recipientData);

        startActivity(toEndActivity);
        finish();
    }
}
