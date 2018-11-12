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
        setContentView(R.layout.collection1_activity_locker);

        Log.i(TAG, "onCreate: Locker Activity started");

        // Get data from previous activity stored in a bundle
        Bundle detailActivityData = this.getIntent().getExtras();

        if (detailActivityData != null) {
            packageType = detailActivityData.getInt("packageType");
            Log.i(TAG, "Package Type: " + String.valueOf(packageType));

            senderData = detailActivityData.getParcelable("senderData");
            Log.i(TAG, "Sender data: User Name: " + senderData.getName() + ", User Email: " + senderData.getEmailAddress());

            recipientData = detailActivityData.getParcelable("recipientData");
            Log.i(TAG, " data: Recipient Name: " + recipientData.getName() + ", Recipient Email: " + recipientData.getEmailAddress() + ", Recipient Location: " + recipientData.getDeliveryLocation());
        } else {
            // throw some exception/error
            Log.i(TAG, "No data sent from previous activity");
        }

        // packageType = this.getIntent().getIntExtra("packageType", 0);
        // senderData = this.getIntent().getParcelableExtra("senderData");
        // recipientData = this.getIntent().getParcelableExtra("recipientData");

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

        // Create a bundle for holding the extras
        Bundle extras = new Bundle();

        // Adds this extra detail to the intent which indicates:
        // The kind of package the user is sending
        // The data given to MailBot about the sender
        // The data given to MailBot about the recipient
        extras.putInt(packageTag, packageType);
        extras.putParcelable(senderDataTag, senderData);
        extras.putParcelable(recipientDataTag, recipientData);

        // Add all the extras content to the intent
        toEndActivity.putExtras(extras);
        startActivity(toEndActivity);

        finish();
    }
}
