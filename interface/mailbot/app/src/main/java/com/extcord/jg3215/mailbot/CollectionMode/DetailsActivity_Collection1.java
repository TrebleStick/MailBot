package com.extcord.jg3215.mailbot.CollectionMode;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.extcord.jg3215.mailbot.PackageData;
import com.extcord.jg3215.mailbot.R;

public class DetailsActivity_Collection1 extends AppCompatActivity {

    // TODO: Change text of speech bubble

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

    private static final String[] inputDetailList = {"Sender Details", "Recipient Details"};

    // Sends the user back to the main menu
    Button cancelButton;

    // Sends the user to the next state
    Button goButton;

    // User selects this button if there is an error with the details entered
    Button problemButton;

    // Used for sender name, recipient name -> not in confirmation state
    EditText topEntry;

    // Used for sender email address, recipient email address -> not in confirmation state
    EditText midEntry;

    // Used to give recipient location -> 2nd state only
    EditText btmEntry;

    // Bottom field
    TextView btmField;

    // Used to allow the sender to say whether or not they want to take a photo -> 1st state only
    // What are you going to do if they check this option?
    // TODO: Check if photo is still happening?
    CheckBox takePhotoOption;

    // TextViews shown when confirming given details
    TextView confirmTopEnt;
    TextView confirmMidEnt;
    TextView confirmBtmEnt;

    Spinner inputDetails;
    private int spinnerListItem;

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
        setContentView(R.layout.collection1_activity_details);

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

        try {
            senderData = this.getIntent().getParcelableExtra("senderData");
        } catch (NullPointerException e) {
            Log.i(TAG, "No recipientData object available: " + e.getMessage());
        }

        topEntry = (EditText) findViewById(R.id.topEntry);
        midEntry = (EditText) findViewById(R.id.midEntry);
        btmEntry = (EditText) findViewById(R.id.deliveryLocation);

        // Bottom field in state 1 -> Take Photo?
        takePhotoOption = (CheckBox) findViewById(R.id.choosePhoto);
        // Bottom field in state 2 -> Delivery Location
        // Bottom field only visible for recipient details spinner
        btmField = (TextView) findViewById(R.id.bottomField);

        confirmTopEnt = (TextView) findViewById(R.id.confirmTopEntry);
        confirmMidEnt = (TextView) findViewById(R.id.confirmMidEntry);
        confirmBtmEnt = (TextView) findViewById(R.id.confirmBtmEntry);

        inputDetails = (Spinner) findViewById(R.id.detailSpinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, inputDetailList);
        inputDetails.setAdapter(adapter);
        inputDetails.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                // i refers to the index of the adapter list

                switch (i) {
                    case 0:
                        // item 0 in the adapter list should present sender details
                        spinnerListItem = 0;
                        Log.i(TAG, "Spinner List Item Selected = " + String.valueOf(spinnerListItem) + ", " + String.valueOf(i));
                        confirmTopEnt.setText(senderData.getName());
                        confirmTopEnt.setVisibility(View.VISIBLE);

                        confirmMidEnt.setText(senderData.getEmailAddress());
                        confirmMidEnt.setVisibility(View.VISIBLE);

                        btmField.setVisibility(View.GONE);
                        break;
                    case 1:
                        // item 1 in the adapter list should present the recipient details
                        spinnerListItem = 1;
                        Log.i(TAG, "Spinner List Item Selected = " + String.valueOf(spinnerListItem) + ", " + String.valueOf(i));
                        confirmTopEnt.setText(recipientData.getName());
                        confirmTopEnt.setVisibility(View.VISIBLE);

                        confirmMidEnt.setText(recipientData.getEmailAddress());
                        confirmMidEnt.setVisibility(View.VISIBLE);

                        confirmBtmEnt.setText(recipientData.getDeliveryLocation());
                        confirmBtmEnt.setVisibility(View.VISIBLE);
                        btmField.setVisibility(View.VISIBLE);
                        break;
                    default:
                        Log.i(TAG, "Item list exception: i = " + String.valueOf(i));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // do nothing
            }
        });

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

        // TODO: FINISH
        problemButton = (Button) findViewById(R.id.buttonProblem);
        problemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                TextView bubbleText = (TextView) findViewById(R.id.speechBubbleText);
                bubbleText.setText(getResources().getString(R.string.infoDetailsActivity));

                switch (spinnerListItem) {
                    case 0:
                        state = 1;
                        toLayoutStateOne();
                        break;
                    case 1:
                        state = 2;
                        // This method does not need any arguments tbh
                        toLayoutStateTwo(1);
                        break;
                }
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
                        // TODO: Make the checks below a function
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
                        toLayoutStateTwo(0);
                        Log.i(TAG, "toLayoutStateTwo() method called.");

                        // Change state of the activity
                        state++;
                        Log.i(TAG, "State variable updated. State = " + String.valueOf(state));
                        break;
                    case STATE_RECIPIENT_DETAILS:
                        Log.i(TAG, "Recipient details entered");
                        String recipientName = topEntry.getText().toString();
                        String recipientEmailAddress = midEntry.getText().toString();

                        // TODO: Ensure that delivery Location is in the right format
                        String deliveryLocation = btmEntry.getText().toString();

                        if (recipientName.equals("")) {
                            Log.i(TAG, "Name Field is empty");
                            Toast.makeText(DetailsActivity_Collection1.this, getResources().getString(R.string.emptyNameField), Toast.LENGTH_LONG).show();
                            return;
                        }

                        if (recipientEmailAddress.equals("")) {
                            Log.i(TAG, "Email Address Field is empty");
                            Toast.makeText(DetailsActivity_Collection1.this, getResources().getString(R.string.emptyEmailField), Toast.LENGTH_LONG).show();
                            return;
                        }

                        // The user will have to insert a valid email address before they can progress beyond this point
                        if (!isValidEmail(recipientEmailAddress)) {
                            Log.i(TAG, "Invalid email address format given");
                            Toast.makeText(DetailsActivity_Collection1.this, getResources().getString(R.string.emailError), Toast.LENGTH_LONG).show();

                            // TODO: Check that return; makes you escape the switch statement as expected - alternatively, use break;
                            return;
                        }

                        // Create a class containing data about the recipient
                        recipientData = new PackageData(recipientName, recipientEmailAddress);
                        recipientData.setDeliveryLocation(deliveryLocation);

                        // Change view to what you expect next
                        toLayoutStateThree(topEntry, midEntry, btmEntry);
                        Log.i(TAG, "toLayoutStateThree() method called");

                        // Change state of the activity
                        state++;
                        Log.i(TAG, "State variable updated. State = " + String.valueOf(state));
                        break;
                    case STATE_CONFIRMATION:
                        Log.i(TAG, "Details confirmed");
                        // TODO: Send a serial message to the computer to request locker opening

                        toLockerActivity();
                        break;
                }
            }
        });
    }

    private void toLockerActivity() {
        Log.i(TAG, "toLockerActivity() method called");
        String packageTag = "packageType";
        String senderDataTag = "senderData";
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
        Log.i(TAG, "Locker Activity started with the extras: " + packageTag + ", " + senderDataTag + ", " + recipientDataTag);
        finish();
    }

    // Checks whether the user has inputted a valid email String
    private boolean isValidEmail(String inputEmailAddress) {
        return (inputEmailAddress != null) && (Patterns.EMAIL_ADDRESS.matcher(inputEmailAddress).matches());
    }

    private void toLayoutStateOne() {
        // TextViews should not be visible
        confirmTopEnt.setVisibility(View.GONE);
        confirmMidEnt.setVisibility(View.GONE);
        confirmBtmEnt.setVisibility(View.GONE);

        // Bottom TextView text should have take photo
        btmField.setText(getResources().getString(R.string.userChoosePhoto));
        btmField.setVisibility(View.VISIBLE);
        // Take Photo checkBox should be visible
        takePhotoOption.setVisibility(View.VISIBLE);

        // Top and Mid EditTexts should be visible
        topEntry.setText("");
        topEntry.setVisibility(View.VISIBLE);
        midEntry.setText("");
        midEntry.setVisibility(View.VISIBLE);

        // Problem button should not be visible
        problemButton.setVisibility(View.GONE);

        // Spinner should not be visible
        inputDetails.setVisibility(View.GONE);
    }

    // TODO: Change this method so it does not take arguments
    private void toLayoutStateTwo(int callCase) {
        // callCase refers to whether this method is called from problem Button or go Button listener

        // toLayoutStateTwo means that you are preparing the activity to receive recipient details
        TextView bubbleText = (TextView) findViewById(R.id.speechBubbleText);
        bubbleText.setText(getResources().getString(R.string.bubbleRecipientDetails));

        // Bottom field set to read "Delivery Location: "
        btmField.setText(getResources().getString(R.string.recipientLocation));

        switch (callCase) {
            case 0:
                // case 0 is from the go button listener ie from state 1
                topEntry.setText("");
                midEntry.setText("");

                // Gets rid of take photo checkbox
                takePhotoOption.setVisibility(View.GONE);
                // Brings up EditText for inserting delivery location
                btmEntry.setVisibility(View.VISIBLE);
                break;
            case 1:
                // should only be here if this method is called from problem button click listener
                // TODO: Check if the View.VISIBLE commands are necessary for the Entry EditTexts
                topEntry.setText(recipientData.getName());
                topEntry.setVisibility(View.VISIBLE);

                midEntry.setText(recipientData.getEmailAddress());
                midEntry.setVisibility(View.VISIBLE);

                btmEntry.setText(recipientData.getDeliveryLocation());
                btmEntry.setVisibility(View.VISIBLE);

                // TextViews should not be visible
                confirmTopEnt.setVisibility(View.GONE);
                confirmMidEnt.setVisibility(View.GONE);
                confirmBtmEnt.setVisibility(View.GONE);

                // Problem button should not be visible
                problemButton.setVisibility(View.GONE);
                break;
        }
        Log.i(TAG, "Layout changed to state two.");
    }

    private void toLayoutStateThree(EditText topText, EditText midText, EditText btmText) {
        topText.setText("");
        topText.setVisibility(View.GONE);

        midText.setText("");
        midText.setVisibility(View.GONE);

        btmText.setVisibility(View.GONE);
        // When confirming details, user cannot edit their entries -> EditTexts are hidden

        // Button allows user to flag an issue with a set of details given (sender or recipient)
        problemButton.setVisibility(View.VISIBLE);

        // Edits the text of the speech bubble
        TextView bubbleText = (TextView) findViewById(R.id.speechBubbleText);
        bubbleText.setText(getResources().getString(R.string.confirmSpeechBubble));

        // Makes spinner visible
        inputDetails.setVisibility(View.VISIBLE);

        Log.i(TAG, "Layout changed to state three.");
    }

    protected void onDestroy() {
        super.onDestroy();

        // TODO: Check that this does not mess up what is sent to next activity
        // Here to make sure that there is no remaining object data when new instances are made and to free up memory
        recipientData = null;
        senderData = null;
    }
}
