package com.extcord.jg3215.mailbot.CollectionMode;

import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.extcord.jg3215.mailbot.PackageData;
import com.extcord.jg3215.mailbot.R;

public class EndActivity_Collection1 extends FragmentActivity implements EndActivityDialogFragment.EndActivityDialogListener {

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
                // TODO: Check if there is still space

                showDialog();

                /* Go back to main screen and wait for user to select a locker size
                Intent returnToMainActivityIntent = new Intent(EndActivity_Collection1.this, MainActivity_Collection1.class);
                startActivityForResult(returnToMainActivityIntent, 1); */
            }
        });

        returnButton = (Button) findViewById(R.id.btnStartAgain);
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                senderData = null;
                recipientData = null;
                finish();
            }
        });
    }

    // Produces the dialogFragment on the screen
    // IT WORKS HOW YOU EXPECTED YAY
    public void showDialog() {
        // Create an instance of the dialog fragment and show it
        DialogFragment dialogFragment = new EndActivityDialogFragment();
        dialogFragment.show(getFragmentManager(), "EndActivityDialogFragment");
    }

    // The dialog fragment receives a reference to this Activity through the Fragment.onAttach()
    // It uses the reference to call the following methods defined by the EndActivityDialogFragment listener interface
    @Override
    public void onSmallLockerSelect(DialogFragment dialogFragment) {
        Log.i(TAG, "Small locker selected to send to the same recipient");
        // TODO: Check if there is space for small locker
        // TODO: Take the user to the confirmation screen
            // TODO: Add a field that tells the user how many items they are delivering
    }

    @Override
    public void onMediumLockerSelect(DialogFragment dialogFragment) {
        Log.i(TAG, "Medium locker selected to send to the same recipient");
        // TODO: Check if there is space for small locker
        // TODO: Take the user to the confirmation screen
            // TODO: Add a field that tells the user how many items they are delivering
    }

    @Override
    public void onLargeLockerSelect(DialogFragment dialogFragment) {
        Log.i(TAG, "Large locker selected to send to the same recipient");
        // TODO: Check if there is space for small locker
        // TODO: Take the user to the confirmation screen
            // TODO: Add a field that tells the user how many items they are delivering
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialogFragment) {
        // TODO: Take the user to the confirmation screen
    }
}
