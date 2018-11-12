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

        senderData = this.getIntent().getParcelableExtra("userData");
        Log.i(TAG, "Sender data: User Name: " + senderData.getName() + ", User Email: " + senderData.getEmailAddress());

        recipientData = this.getIntent().getParcelableExtra("recipientData");
        Log.i(TAG, " data: Recipient Name: " + recipientData.getName() + ", Recipient Email: " + recipientData.getEmailAddress());

        toSameRecipientButton = (Button) findViewById(R.id.btnToSameRecipient);
        toSameRecipientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String senderDataTag = "senderData";
                String recipientDataTag = "recipientData";
                Intent returnToMainActivityIntent = new Intent(EndActivity_Collection1.this, DetailsActivity_Collection1.class);

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
