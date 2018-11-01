package com.extcord.jg3215.mailbot.CollectionMode;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.extcord.jg3215.mailbot.R;

public class DetailsActivity_Collection1 extends AppCompatActivity {

    // Denotes what kind of package is being sent: small letter, large letter or parcel
    private int packageType;

    // Denotes the state the activity is in at that moment in time
    private int state;

    // Represents the three states this activity can be in:
        // 1. Input user details
        // 2. Input recipient details
        // 3. Confirm details
    private static final int STATE_USER_DETAILS = 1;
    private static final int STATE_RECIPIENT_DETAILS = 2;
    private static final int STATE_CONFIRMATION = 3;

    private static final String TAG = "DetailsActivity";

    // Sends the user back to the main menu
    Button cancelButton;

    // Sends the user to the next state
    Button goButton;

    // Used for sender name, recipient name
    // TODO: Should not be used in confirmation state
    EditText topEntry;

    // Used for sender email address, recipient email address
    // TODO: Should not be used in confirmation state
    EditText midEntry;

    // Used to give recipient location -> 2nd state only
    EditText btmEntry;

    // Used to allow the sender to say whether or not they want to take a photo -> 1st state only
    CheckBox takePhotoOption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_collection1);

        // Removed the navigation bar code because it makes this activity look weird as it is created

        // Gets the package type that is being delivered from the intent
        packageType = this.getIntent().getIntExtra("packageType", 0);
        Log.i(TAG, "Package Type: " + String.valueOf(packageType));

        // Keeps track of which mode the activity is in:
            // 1. User entering their own details
            // 2. User entering recipient details
            // 3. Confirming details with user
        state = 1;

        cancelButton = (Button) findViewById(R.id.buttonCancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            // Method that is called once click listener registers that button has been clicked
            public void onClick(View view) {
            Log.i(TAG, "Cancel button pressed");
            // TODO: Tell Robot that the process has been cancelled

            // Takes the user back to the main menu - not the previous state
            finish();
            }
        });

        goButton = (Button) findViewById(R.id.buttonForward);
        goButton.setOnClickListener(new View.OnClickListener() {
            @Override
            // Method that is called once click listener registers that button has been clicked
            public void onClick(View view) {
                Log.i(TAG, "Go button pressed");

                switch (state) {
                    case STATE_USER_DETAILS:
                        // TODO: Check that name is legitimate
                        // TODO: Check that email address is legitimate (or assume that it is)
                        // Maybe put these in a function

                        // TODO: Change layout to recipient details stuff
                        state++;
                        break;
                    case STATE_RECIPIENT_DETAILS:
                        // TODO: Check that name is legitimate
                        // TODO: Check that email address is legitimate (or assume that it is)
                        // Maybe put these in a function

                        // TODO: Change layout to confirmation
                        state++;
                        break;
                    case STATE_CONFIRMATION:
                        // TODO: Send a serial message to the computer requesting locker opening

                        // TODO: Make a separate Broadcast receiver for the serial communication
                            // Serial communcation to open the MailBot for mail item
                            // TODO: Go to the next activity - pass on details
                            // TODO: Send information to the Robot
                            // TODO: Kill this activity
                        break;
                }
            }
        });
    }
}
