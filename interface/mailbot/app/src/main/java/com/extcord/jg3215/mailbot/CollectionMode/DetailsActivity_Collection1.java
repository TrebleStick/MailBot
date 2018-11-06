package com.extcord.jg3215.mailbot.CollectionMode;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.extcord.jg3215.mailbot.PackageData;
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

    // Used for sender name, recipient name -> not in confirmation state
    EditText topEntry;

    // Used for sender email address, recipient email address -> not in confirmation state
    EditText midEntry;

    // Used to give recipient location -> 2nd state only
    EditText btmEntry;

    // Used to allow the sender to say whether or not they want to take a photo -> 1st state only
    // What are you going to do if they check this option?
    // TODO: Check if photo is still happening?
    CheckBox takePhotoOption;

    // Package details - could make this one class
    private PackageData senderData;
    private PackageData recipientData;

    // Listen for response (Serial communication) to open locker command
    // TODO: Register and unregister Broadcast Receivers br0
    BroadcastReceiver mBroadcastReceiverLockerOpen = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String text = intent.getStringExtra("openLocker");
            Log.i(TAG, "Received: " + text);

            // TODO: Go to the next activity once the locker has been opened
            // toLockerActivity();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_collection1);

        // Removed the navigation bar code because it makes this activity look weird as it is created

        // Keeps track of which mode the activity is in:
            // 1. User entering their own details
            // 2. User entering recipient details
            // 3. Confirming details with user
        state = 1;

        // Gets the package type that is being delivered from the intent
        packageType = this.getIntent().getIntExtra("packageType", 0);
        Log.i(TAG, "Package Type: " + String.valueOf(packageType));

        try {
            // If a null point exception does not occur, user has requested to mail another item to same recipient
            recipientData = this.getIntent().getParcelableExtra("recipientData");

        } catch (NullPointerException e) {
            Log.i(TAG, "No recipientData object available: " + e.getMessage());
        }

        topEntry = (EditText) findViewById(R.id.topEntry);
        midEntry = (EditText) findViewById(R.id.midEntry);
        takePhotoOption = (CheckBox) findViewById(R.id.choosePhoto);

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
                        Log.i(TAG, "Sender details entered");

                        String userName = topEntry.getText().toString();
                        String userEmailAddress = midEntry.getText().toString();

                        // TODO: Check that this forces you to quit the switch as expected
                        if (userName.equals("")) {
                            Log.i(TAG, "Name Field is empty");
                            Toast.makeText(DetailsActivity_Collection1.this, getResources().getString(R.string.emptyNameField), Toast.LENGTH_LONG).show();
                            return;
                        }

                        if (userEmailAddress.equals("")) {
                            Log.i(TAG, "Email Address Field is empty");
                            Toast.makeText(DetailsActivity_Collection1.this, getResources().getString(R.string.emptyEmailField), Toast.LENGTH_LONG).show();
                            return;
                        }

                        // The user will have to insert a valid email address before they can progress beyond this point
                        if (!isValidEmail(userEmailAddress)) {
                            Log.i(TAG, "Invalid email address format given");
                            Toast.makeText(DetailsActivity_Collection1.this, getResources().getString(R.string.emailError), Toast.LENGTH_LONG).show();

                            // TODO: Check that this makes you escape the switch statement as expected - alternatively, use break;
                            return;
                        }

                        // Create class containing data about the user
                        senderData = new PackageData(userName, userEmailAddress);

                        // Change view to what you expect next
                        btmEntry = (EditText) findViewById(R.id.deliveryLocation);
                        toLayoutStateTwo(topEntry, midEntry, takePhotoOption, btmEntry);

                        // Change state of the activity
                        state++;
                        break;
                    case STATE_RECIPIENT_DETAILS:
                        Log.i(TAG, "Recipient details entered");
                        String recipientName = topEntry.getText().toString();
                        String recipientEmailAddress = midEntry.getText().toString();

                        // The user will have to insert a valid email address before they can progress beyond this point
                        if (!isValidEmail(recipientEmailAddress)) {
                            Log.i(TAG, "Invalid email address format given");
                            Toast.makeText(DetailsActivity_Collection1.this, getResources().getString(R.string.emailError), Toast.LENGTH_LONG).show();

                            // TODO: Check that this makes you escape the switch statement as expected - alternatively, use break;
                            return;
                        }

                        // Create a class containing data about the recipient
                        recipientData = new PackageData(recipientName, recipientEmailAddress);

                        // Change view to what you expect next
                        toLayoutStateThree(topEntry, midEntry, btmEntry);

                        // Change state of the activity
                        state++;
                        break;
                    case STATE_CONFIRMATION:
                        Log.i(TAG, "Details confirmed");
                        // TODO: Send a serial message to the computer to request locker opening
                        break;
                }
            }
        });
    }

    private void toLockerActivity() {
        Log.i(TAG, "toLockerActivity() method called");
        String packageTag = "packageType";
        String senderDataTag = "userData";
        String recipientDataTag = "recipientData";

        Intent lockerActivityIntent = new Intent(this, LockerActivity_Collection1.class);

        // Adds this extra detail to the intent which indicates:
            // The kind of package the user is sending
            // The data given to MailBot about the sender
            // The data given to MailBot about the recipient
        lockerActivityIntent.putExtra(packageTag, packageType);
        lockerActivityIntent.putExtra(senderDataTag, senderData);
        lockerActivityIntent.putExtra(recipientDataTag, recipientData);

        startActivity(lockerActivityIntent);
        Log.i(TAG, "Locker Activity started with the extra=packageType");
        finish();
    }

    // Checks whether the user has inputted a valid email String
    private boolean isValidEmail(String inputEmailAddress) {
        return (inputEmailAddress != null) && (Patterns.EMAIL_ADDRESS.matcher(inputEmailAddress).matches());
    }

    private void toLayoutStateTwo(EditText topText, EditText midText, CheckBox photoOption, EditText btmText) {
        topText.setText("");
        midText.setText("");

        photoOption.setVisibility(View.GONE);
        btmText.setVisibility(View.VISIBLE);
        Log.i(TAG, "Layout changed to state two.");
    }

    private void toLayoutStateThree(EditText topText, EditText midText, EditText btmText) {
        topText.setText("");
        topText.setVisibility(View.GONE);

        midText.setText("");
        midText.setVisibility(View.GONE);

        btmText.setVisibility(View.GONE);

        // TODO: Make the TextViews confirming the details visible

        Log.i(TAG, "Layout changed to state three.");
    }

    protected void onDestroy() {
        super.onDestroy();

        // TODO: Check that this does not mess up what is sent to next activity
        // Here to make sure that there is no remaining object data when new instances are made
        recipientData = null;
        senderData = null;
    }
}
