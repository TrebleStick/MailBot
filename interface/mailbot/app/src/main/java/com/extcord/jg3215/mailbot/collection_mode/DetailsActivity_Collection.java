package com.extcord.jg3215.mailbot.collection_mode;

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

import com.extcord.jg3215.mailbot.LockerManager;
import com.extcord.jg3215.mailbot.PackageData;
import com.extcord.jg3215.mailbot.R;

public class DetailsActivity_Collection extends AppCompatActivity {

    // TODO: Fix appearance of "Search" in the place of "Delivery Location" in state 2
        // It is a bug that seems to appear in random places and I cannot figure out why
        // Can be fixed by changing the string's value and then changing it back after building

    // TODO: Get rid of setting textView to "Delivery Location: "
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

    // indicates whether user is in a state because they came from problem button listener
    private boolean fromProblemButtonListener = false;

    // Used for sender name, recipient name -> not in confirmation state
    EditText topEntry;

    // Used for sender email address, recipient email address -> not in confirmation state
    EditText midEntry;

    // Used to give recipient location -> 2nd state only
    EditText btmEntry;

    // Bottom field
    TextView btmField;

    // TextViews shown when confirming given details
    TextView confirmTopEnt;
    TextView confirmMidEnt;
    TextView confirmBtmEnt;

    // Spinner is a dropdown list that presents loads up details associated with selected person (sender or recipient)
    Spinner inputDetails;
    private int spinnerListItem;

    ArrayAdapter<String> adapter;
    // data obtained from the user about the sender and the recipient
    // senderName, senderEmail, recipientName, recipientEmail, recipientLocation
    private String[] storedData = new String [5];

    // Package details - could make this one class
    private PackageData senderData;
    private PackageData recipientData;

    private LockerManager mLockerManager;

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

        // TODO: Change the mode to false
        mLockerManager = new LockerManager(this, true);

        topEntry = (EditText) findViewById(R.id.topEntry);
        midEntry = (EditText) findViewById(R.id.midEntry);
        btmEntry = (EditText) findViewById(R.id.deliveryLocation);

        // Bottom field in state 2 -> Delivery Location
        // Bottom field only visible for recipient details spinner
        btmField = (TextView) findViewById(R.id.bottomField);

        confirmTopEnt = (TextView) findViewById(R.id.confirmTopEntry);
        confirmMidEnt = (TextView) findViewById(R.id.confirmMidEntry);
        confirmBtmEnt = (TextView) findViewById(R.id.confirmBtmEntry);

        inputDetails = (Spinner) findViewById(R.id.detailSpinner);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, inputDetailList);
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
                        Log.i(TAG, "Sender data, Name: " + senderData.getName() + ", Email Address: " + senderData.getEmailAddress());

                        confirmBtmEnt.setVisibility(View.GONE);
                        btmField.setVisibility(View.GONE);

                        confirmTopEnt.setText(storedData[0]);
                        confirmMidEnt.setText(storedData[1]);
                        break;
                    case 1:
                        // item 1 in the adapter list should present the recipient details
                        spinnerListItem = 1;
                        Log.i(TAG, "Spinner List Item Selected = " + String.valueOf(spinnerListItem) + ", " + String.valueOf(i));
                        Log.i(TAG, "Recipient data, Name: " + recipientData.getName() + ", Email Address: " + recipientData.getEmailAddress() + ", Delivery Location: " + recipientData.getDeliveryLocation());

                        confirmBtmEnt.setVisibility(View.VISIBLE);
                        btmField.setVisibility(View.VISIBLE);

                        confirmTopEnt.setText(storedData[2]);
                        confirmMidEnt.setText(storedData[3]);
                        confirmBtmEnt.setText(storedData[4]);
                        break;
                    default:
                        Log.i(TAG, "Item list exception: i = " + String.valueOf(i));
                        break;
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
                // TODO: Tell Robot that the process has been cancelled -> Is this necessary?
                // Takes the user back to the main menu - not the previous state
                finish();
            }
        });

        problemButton = (Button) findViewById(R.id.buttonProblem);
        problemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                TextView bubbleText = (TextView) findViewById(R.id.speechBubbleText);
                bubbleText.setText(getResources().getString(R.string.infoDetailsActivity));
                fromProblemButtonListener = true;

                switch (spinnerListItem) {
                    case 0:
                        state = 1;
                        toLayoutStateOne(3);
                        break;
                    case 1:
                        state = 2;
                        toLayoutStateTwo(3);
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
                        String senderName = topEntry.getText().toString();
                        String senderEmailAddress = midEntry.getText().toString();

                        if (!entryChecks(senderName, senderEmailAddress)) {
                            return;
                        }

                        storedData[0] = senderName;
                        storedData[1] = senderEmailAddress;

                        if (!fromProblemButtonListener) {
                            // not entering this state from the problem button listener method
                            Log.i(TAG, "State not accessed from Problem button");

                            // Create class containing data about the user
                            senderData = new PackageData(senderName, senderEmailAddress);

                            // Change view to what you expect next
                            toLayoutStateTwo(1);
                            Log.i(TAG, "toLayoutStateTwo() method called.");
                        } else {
                            Log.i(TAG, "State accessed from Problem button");

                            // senderData class instance already exists
                            senderData.setName(senderName);
                            senderData.setEmailAddress(senderEmailAddress);

                            // Notify spinner that list data set has been altered
                            adapter.notifyDataSetChanged();

                            // return to final state
                            toLayoutStateThree(1);
                        }

                        // Change state of the activity
                        Log.i(TAG, "State variable updated. State = " + String.valueOf(state));
                        break;
                    case STATE_RECIPIENT_DETAILS:
                        Log.i(TAG, "Recipient details entered");
                        String recipientName = topEntry.getText().toString();
                        String recipientEmailAddress = midEntry.getText().toString();

                        // TODO: Ensure that delivery Location is in the right format
                        String recipientLocation = btmEntry.getText().toString();

                        if (!entryChecks(recipientName, recipientEmailAddress)) {
                            return;
                        }

                        storedData[2] = recipientName;
                        storedData[3] = recipientEmailAddress;
                        storedData[4] = recipientLocation;

                        if (!fromProblemButtonListener) {
                            // Create a class containing data about the recipient
                            recipientData = new PackageData(recipientName, recipientEmailAddress);
                            recipientData.setDeliveryLocation(recipientLocation);
                        } else {
                            // not entering this state from the problem button listener method
                            // senderData class instance already exists
                            recipientData.setName(recipientName);
                            recipientData.setEmailAddress(recipientEmailAddress);
                            recipientData.setDeliveryLocation(recipientLocation);

                            // Notify the spinner list that values have been updated
                            adapter.notifyDataSetChanged();
                        }

                        // Change view to what you expect next
                        toLayoutStateThree(2);
                        Log.i(TAG, "toLayoutStateThree() method called");

                        // Change state of the activity
                        Log.i(TAG, "State variable updated. State = " + String.valueOf(state));
                        break;
                    case STATE_CONFIRMATION:
                        Log.i(TAG, "Details confirmed");

                        mLockerManager.setSelectLockerIndex(packageType);
                        Log.i(TAG, "Locker chosen for mail item = " + String.valueOf(mLockerManager.getSelectLockerIndex() + 1));

                        // TODO: Send a serial message to the computer to request locker opening
                        // Assume locker opens once requested
                        toLockerActivity();
                        break;
                }
            }
        });

        Bundle endActivityData = this.getIntent().getExtras();
        if (endActivityData != null) {
            // Gets the package type that is being delivered from the intent
            try {
                packageType = endActivityData.getInt("packageType");
                Log.i(TAG, "Package Type: " + String.valueOf(packageType));
            } catch (NullPointerException e) {
                Log.i(TAG, "No package data provided from endActivity: " + e.getMessage());
            }

            if (endActivityData.getBoolean("dataProvided")) {
                // Object data already provided by the user
                Log.i(TAG, "Data has already been provided by the user");

                try {
                    senderData = endActivityData.getParcelable("senderData");

                    if (senderData != null) {
                        Log.i(TAG, "Sender data: User Name: " + senderData.getName() + ", User Email: " + senderData.getEmailAddress());

                        // Update string array that is used to present object data
                        storedData[0] = senderData.getName();
                        storedData[1] = senderData.getEmailAddress();
                    }
                } catch (NullPointerException e) {
                    throw new NullPointerException("No sender data provided from endActivity: " + e.getMessage());
                }

                try {
                    recipientData = endActivityData.getParcelable("recipientData");

                    if (recipientData != null) {
                        Log.i(TAG, " data: Recipient Name: " + recipientData.getName() + ", Recipient Email: " + recipientData.getEmailAddress() + ", Recipient Location: " + recipientData.getDeliveryLocation());

                        // Update string array that is used to present object data
                        storedData[2] = recipientData.getName();
                        storedData[3] = recipientData.getEmailAddress();
                        storedData[4] = recipientData.getDeliveryLocation();
                    }
                } catch (NullPointerException e) {
                    throw new NullPointerException("No recipient data provided from endActivity: " + e.getMessage());
                }

                // Takes the user to the third state of this activity
                toLayoutStateThree(1);
            }
        } else {
            throw new NullPointerException("No data provided from endActivity");
        }
    }

    private void toLockerActivity() {
        Log.i(TAG, "toLockerActivity() method called");
        String packageTag = "packageType";
        String senderDataTag = "senderData";
        String recipientDataTag = "recipientData";

        Intent lockerActivityIntent = new Intent(this, LockerActivity_Collection.class);

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
        lockerActivityIntent.putExtras(extras);

        startActivity(lockerActivityIntent);
        Log.i(TAG, "To Locker Activity with the extras: " + packageTag + ", " + senderDataTag + ", " + recipientDataTag);
        finish();
    }

    // Checks whether the user has inputted a valid email String
    private boolean isValidEmail(String inputEmailAddress) {
        return (inputEmailAddress != null) && (Patterns.EMAIL_ADDRESS.matcher(inputEmailAddress).matches());
    }

    private boolean entryChecks(String name, String emailAddress) {
        if (name.equals("")) {
            Log.i(TAG, "Name Field is empty");
            Toast.makeText(DetailsActivity_Collection.this, getResources().getString(R.string.emptyNameField), Toast.LENGTH_LONG).show();
            return false;
        }

        if (emailAddress.equals("")) {
            Log.i(TAG, "Email Address Field is empty");
            Toast.makeText(DetailsActivity_Collection.this, getResources().getString(R.string.emptyEmailField), Toast.LENGTH_LONG).show();
            return false;
        }

        // The user will have to insert a valid email address before they can progress beyond this point
        if (!isValidEmail(emailAddress)) {
            Log.i(TAG, "Invalid email address format given");
            Toast.makeText(DetailsActivity_Collection.this, getResources().getString(R.string.emailError), Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    private void toLayoutStateOne(int callCase) {
        // Bottom field textView should not be visible
        btmField.setVisibility(View.GONE);

        // Bottom entry editText should not be visible
        btmEntry.setVisibility(View.GONE);

        // Problem button should not be visible
        problemButton.setVisibility(View.GONE);

        // Spinner should not be visible
        inputDetails.setVisibility(View.GONE);

        switch (callCase) {
            case 2:
                // go to state 1 from state 2
                break;
            case 3:
                // go to state 1 from state 3

                // Entry TextViews should not be visible
                confirmTopEnt.setVisibility(View.GONE);
                confirmMidEnt.setVisibility(View.GONE);
                confirmBtmEnt.setVisibility(View.GONE);

                // Top and Mid EditTexts need to be made visible and should display name and email address data
                topEntry.setText(senderData.getName());
                topEntry.setVisibility(View.VISIBLE);

                midEntry.setText(senderData.getEmailAddress());
                midEntry.setVisibility(View.VISIBLE);
                break;
        }
        state = 1;
        Log.i(TAG, "Layout changed to state: " + String.valueOf(state));
    }

    private void toLayoutStateTwo(int callCase) {
        // callCase refers to which state you call this method from
        // toLayoutStateTwo means that you are preparing the activity to receive recipient details
        TextView bubbleText = (TextView) findViewById(R.id.speechBubbleText);
        bubbleText.setText(getResources().getString(R.string.bubbleRecipientDetails));

        switch (callCase) {
            case 1:
                // go to state 2 from state 1
                topEntry.setText("");
                midEntry.setText("");

                // Bottom field set to read "Delivery Location: "
                btmField.setText(getResources().getString(R.string.fieldLocation));
                Log.i(TAG, "Bottom Field set to: " + getResources().getString(R.string.fieldLocation));

                btmField.setVisibility(View.VISIBLE);

                // Brings up EditText for inserting delivery location
                btmEntry.setVisibility(View.VISIBLE);
                break;
            case 3:
                // go to state 2 from state 3 (recipient details)

                // Confirmation textViews should not be visible
                confirmTopEnt.setVisibility(View.GONE);
                confirmMidEnt.setVisibility(View.GONE);
                confirmBtmEnt.setVisibility(View.GONE);

                // Make top, middle and bottom EditTexts visible with existing data
                topEntry.setText(recipientData.getName());
                topEntry.setVisibility(View.VISIBLE);

                midEntry.setText(recipientData.getEmailAddress());
                midEntry.setVisibility(View.VISIBLE);

                btmEntry.setText(recipientData.getDeliveryLocation());
                btmEntry.setVisibility(View.VISIBLE);

                // Problem button should not be visible
                problemButton.setVisibility(View.GONE);

                // Spinner should not be visible
                inputDetails.setVisibility(View.GONE);
                break;
        }

        state = 2;
        Log.i(TAG, "Layout changed to state: " + String.valueOf(state));
    }

    private void toLayoutStateThree(int callCase) {
        // callCase refers to which state ou call this method from
        fromProblemButtonListener = false;

        // When confirming details, user cannot edit their entries -> EditTexts are hidden
        topEntry.setVisibility(View.GONE);
        midEntry.setVisibility(View.GONE);
        btmEntry.setVisibility(View.GONE);

        // Make confirmation TextViews visible
        confirmTopEnt.setVisibility(View.VISIBLE);
        confirmMidEnt.setVisibility(View.VISIBLE);

        // Makes spinner visible
        inputDetails.setVisibility(View.VISIBLE);

        // Button allows user to flag an issue with a set of details given (sender or recipient)
        problemButton.setVisibility(View.VISIBLE);

        // Edits the text of the speech bubble
        TextView bubbleText = (TextView) findViewById(R.id.speechBubbleText);
        bubbleText.setText(getResources().getString(R.string.confirmSpeechBubble));

        switch(callCase) {
            case 1:
                // go to state 3 from state 1
                Log.i(TAG, "toLayoutStateThree() method called from state: " + String.valueOf(callCase));

                // Bottom field text altered to be "Delivery Location"
                btmField.setText(getResources().getString(R.string.fieldLocation));
                // If you are going to state three from state one, you registered a problem in Sender details
                // Bottom field should not be visible in this case
                btmField.setVisibility(View.GONE);

                // Manually update these textViews
                confirmTopEnt.setText(senderData.getName());
                confirmMidEnt.setText(senderData.getEmailAddress());

                // This textView should not be visible if you are coming from state 1 (user details)
                confirmBtmEnt.setVisibility(View.GONE);
                break;
            case 2:
                // go to state 3 from state 2
                Log.i(TAG, "toLayoutStateThree() method called from state: " + String.valueOf(callCase));

                // Manually update the textViews
                confirmTopEnt.setText(recipientData.getName());
                confirmMidEnt.setText(recipientData.getEmailAddress());

                confirmBtmEnt.setText(recipientData.getDeliveryLocation());
                confirmBtmEnt.setVisibility(View.VISIBLE);

                btmField.setVisibility(View.VISIBLE);
                break;
        }
        state = 3;
        Log.i(TAG, "Layout changed to state: "+ String.valueOf(state));
    }

    protected void onDestroy() {
        super.onDestroy();

        // Here to make sure that there is no remaining object data when new instances are made and to free up memory
        recipientData = null;
        senderData = null;
        mLockerManager = null;
    }
}
