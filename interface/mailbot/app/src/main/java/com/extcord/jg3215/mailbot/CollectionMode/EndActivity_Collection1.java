package com.extcord.jg3215.mailbot.CollectionMode;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.extcord.jg3215.mailbot.PackageData;
import com.extcord.jg3215.mailbot.R;

public class EndActivity_Collection1 extends AppCompatActivity {

    // TODO: Implement a way for user to send another mail item from the same person? -> send that data as extra too

    private int packageType;

    private PackageData senderData;
    private PackageData recipientData;

    Button toSameRecipientButton;
    Button returnButton;

    private static final String TAG = "EndActvity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.collection1_activity_end);

        /* View decorView = getWindow().getDecorView();
        // Hide both the navigation bar and the status bar.
        // SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
        // a general rule, you should design your app to hide the status bar whenever you
        // hide the navigation bar.
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions); */

        // Get data from previous activity stored in a bundle
        Bundle lockerActivityData = this.getIntent().getExtras();

        if (lockerActivityData != null) {
            packageType = lockerActivityData.getInt("packageType");
            Log.i(TAG, "Package Type: " + String.valueOf(packageType));

            senderData = lockerActivityData.getParcelable("senderData");
            Log.i(TAG, "Sender data: User Name: " + senderData.getName() + ", User Email: " + senderData.getEmailAddress());

            recipientData = lockerActivityData.getParcelable("recipientData");
            Log.i(TAG, " data: Recipient Name: " + recipientData.getName() + ", Recipient Email: " + recipientData.getEmailAddress() + ", Recipient Location: " + recipientData.getDeliveryLocation());
        } else {
            // throw some exception/error
            Log.i(TAG, "No data sent from previous activity");
        }

        toSameRecipientButton = (Button) findViewById(R.id.btnToSameRecipient);
        toSameRecipientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String senderDataTag = "senderData";
                String recipientDataTag = "recipientData";
                Intent returnToMainActivityIntent = new Intent(EndActivity_Collection1.this, MainActivity_Collection1.class);

                // Create a bundle for holding the extras
                Bundle extras = new Bundle();

                // Adds this extra detail to the intent which indicates:
                // The kind of package the user is sending
                // The data given to MailBot about the sender
                // The data given to MailBot about the recipient
                extras.putParcelable(senderDataTag, senderData);
                extras.putParcelable(recipientDataTag, recipientData);

                // Add all the extras content to the intent
                returnToMainActivityIntent.putExtras(extras);

                // Loads up sender and recipientData
                returnToMainActivityIntent.putExtra(recipientDataTag, recipientData);
                returnToMainActivityIntent.putExtra(senderDataTag, senderData);

                // Go back to main activity
                startActivity(returnToMainActivityIntent);
                finish();
            }
        });

        returnButton = (Button) findViewById(R.id.btnStartAgain);
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
