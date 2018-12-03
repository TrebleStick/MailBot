package com.extcord.jg3215.mailbot.collection_mode;

import android.content.Context;
import android.content.Intent;
import android.graphics.LightingColorFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.extcord.jg3215.mailbot.LockerManager;
import com.extcord.jg3215.mailbot.PackageData;
import com.extcord.jg3215.mailbot.R;
import com.extcord.jg3215.mailbot.database.LockerItem;
import com.extcord.jg3215.mailbot.database.LockerItemDatabase;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.Lock;

public class LockerActivity_Collection extends AppCompatActivity {

    // TODO: Send a PIN code for each package to the computer as the mail item is locked away
    // Integers used to represent the type of mail that is being sent
    private static final int LETTER_STANDARD = 1;
    private static final int LETTER_LARGE = 2;
    private static final int PARCEL = 3;

    // The TextViews for the two options given at the start of this activity
        // 'Parcel fits' vs 'Parcel does not fit'
    TextView badFitView;
    TextView goodFitView;

    private int packageType;
    private int lockerIndex;
    private String pinCode;

    private final static String TAG = "LockerActivity";

    private PackageData senderData;
    private PackageData recipientData;

    private LockerManager mLockerManager;

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.collection1_activity_locker);

        Log.i(TAG, "onCreate: Locker Activity started");
        mContext = this;

        // Get data from previous activity stored in a bundle
        Bundle detailActivityData = this.getIntent().getExtras();

        mLockerManager = new LockerManager(this);

        if (detailActivityData != null) {
            packageType = detailActivityData.getInt("packageType");
            Log.i(TAG, "Package Type: " + String.valueOf(packageType));

            senderData = detailActivityData.getParcelable("senderData");
            if (senderData != null) {
                Log.i(TAG, "Sender data: User Name: " + senderData.getName() + ", User Email: " + senderData.getEmailAddress());
            }

            recipientData = detailActivityData.getParcelable("recipientData");
            if (recipientData != null) {
                Log.i(TAG, " data: Recipient Name: " + recipientData.getName() + ", Recipient Email: " + recipientData.getEmailAddress() + ", Recipient Location: " + recipientData.getDeliveryLocation());
            }

            lockerIndex = detailActivityData.getInt("lockerIndex");
            Log.i(TAG, "Locker to open = " + String.valueOf(lockerIndex + 1));

            pinCode = detailActivityData.getString("pinCode");
            Log.i(TAG, "PIN code to open locker = " + pinCode);

            //Set the opened locker number in speech bubble in LockerActivity_Collection
            TextView LockerText = (TextView) findViewById(R.id.textLockerOpened);
            LockerText.setText("I have opened locker " + String.valueOf(lockerIndex + 1) + " for your mail item! Please store it there so I can deliver it, and close the locker before continuing.");

        } else {
            // throw some exception/error -> there should be data from the previous activity
            Log.i(TAG, "No data sent from previous activity");
        }

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
                    Toast.makeText(LockerActivity_Collection.this, getResources().getString(R.string.packageTooBig), Toast.LENGTH_LONG).show();
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
                // Is there any data that needs to passed on as an extra?

                // Starts the asynchronous task
                boolean isEmpty = mLockerManager.getLockerState().equals("0000000");
                new addToDatabase(lockerIndex, senderData, recipientData, mContext, isEmpty, pinCode).execute();

                // Gets the number of available lockers
                int aLockers = mLockerManager.getAvailability(LETTER_STANDARD) + mLockerManager.getAvailability(LETTER_LARGE) + mLockerManager.getAvailability(PARCEL);

                // If no lockers are available, app will go to EnRouteActivity
                if (aLockers > 1) {
                    // Updates lockerState string with one extra full locker
                    Log.i(TAG, "Prior to putting mail item in locker, there were "+ String.valueOf(aLockers) + " available");
                    mLockerManager.updateAvailability(lockerIndex, true);

                    toEndActivity();
                } else {
                    Log.i(TAG, "Locker is full. Closing this activity");
                    mLockerManager.unregisterListener();
                    mLockerManager.updateAvailability(lockerIndex, true);
                    finish();
                }
            }
        });
    }

    // Adding to db needs to be done asynchronously - as do general database queries. Database
    // handling on main thread is inadvisable
    // TODO: Find out what parameters do
    private static class addToDatabase extends AsyncTask<Void, Void, Void> {

        int lockerIndex;
        PackageData senderData;
        PackageData recipientData;
        WeakReference<LockerActivity_Collection> context;
        String pinCode;
        // LockerManager lockerManager;
        boolean empty;

        public addToDatabase(int lockerIndex, PackageData senderData, PackageData recipientData, Context context, Boolean isEmpty, String pinCode) {
            this.lockerIndex = lockerIndex;
            this.senderData = senderData;
            this.recipientData = recipientData;
            this.pinCode = pinCode;

            // using mContext makes the field a location for a memory leak
            // use a Weak reference instead
            // TODO: Look into weak references
            this.context = new WeakReference<>((LockerActivity_Collection) context);

            // this.lockerManager = lockerManager;
            this.empty = isEmpty;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Log.i(TAG, "AsyncTask: Adding database item in the background.");
            LockerItemDatabase lockerItemDatabase = LockerItemDatabase.getInstance(context.get()); //.getApplicationContext());

            if (empty) {
                lockerItemDatabase.lockerDataAccessObject().clearDatabase();
                Log.i(TAG, "Database cleared.");
            }

            // Create a database entry for the senderData, recipientData and locker number associated with this mail item
            LockerItem lockerItem = new LockerItem();
            // lockerItem.setId(0);
            lockerItem.setLockerNo(lockerIndex + 1);

            lockerItem.setSenderName(senderData.getName());
            lockerItem.setSenderEmail(senderData.getEmailAddress());

            lockerItem.setRecipientName(recipientData.getName());
            lockerItem.setRecipientEmail(recipientData.getEmailAddress());

            lockerItem.setDeliveryLocation(recipientData.getDeliveryLocation());

<<<<<<< HEAD
            lockerItem.setPINcode(pinCode);
=======
            // TODO: Make PIN code an extra from details activity -> created each time the confirm button is pressed
            lockerItem.setPINcode(createPIN());
>>>>>>> 31b63a3357779b3411a1b6ad267e3ad17f1046d5
            lockerItemDatabase.lockerDataAccessObject().addUser(lockerItem);

            Log.i(TAG, "Locker item info added to database successfully");
            checkDatabase(lockerItemDatabase);
            return null;
        }

<<<<<<< HEAD
        /*private String createPIN() {
=======
        // TODO: Create some check PIN method to be sure
        private String createPIN() {
>>>>>>> 31b63a3357779b3411a1b6ad267e3ad17f1046d5
            Log.i(TAG, "createPIN() method called");

            StringBuilder createCode = new StringBuilder("");
            for (int i = 0; i < 4; i++) {
                if (createCode.length() < 4) {
                    Random rn = new Random();
                    // Generate random number from 0 to 10
                    int digit = rn.nextInt(11);
                    createCode.append(String.valueOf(digit));
                } else if (createCode.length() == 4) {
                    break;
                }
            }

            Log.i(TAG, "PIN Code: " + createCode.toString());
            return createCode.toString();
        }*/

        private void checkDatabase(LockerItemDatabase lockerItemDatabase) {
            Log.i(TAG, "checkDatabase() method called");
            List<LockerItem> lockerItemList = lockerItemDatabase.lockerDataAccessObject().readLockerItem();
            int count = 0;

            for (LockerItem lockerItems : lockerItemList) {
                int nLocker = lockerItems.getLockerNo();
                String sName = lockerItems.getSenderName();
                String rName = lockerItems.getRecipientName();
                String dLocation = lockerItems.getDeliveryLocation();

                Log.i(TAG, "Mail Item Count = " + String.valueOf(count));
                count++;
                Log.i(TAG, "Locker Number: " + String.valueOf(nLocker) + ", Sender Name: " + sName + ", Recipient Name: " + rName + ", Delivery Location: " + dLocation);
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.i(TAG, "AsyncTask: Complete.");
        }
    }

    private void toEndActivity() {
        Log.i(TAG, "toEndActivity() method called");
        String packageTag = "packageType";
        String senderDataTag = "senderData";
        String recipientDataTag = "recipientData";
        String pinTag = "pinCode";

        Intent toEndActivity = new Intent(this, EndActivity_Collection.class);

        // Create a bundle for holding the extras
        Bundle extras = new Bundle();

        // Adds this extra detail to the intent which indicates:
        // The kind of package the user is sending
        // The data given to MailBot about the sender
        // The data given to MailBot about the recipient
        extras.putInt(packageTag, packageType);
        extras.putParcelable(senderDataTag, senderData);
        extras.putParcelable(recipientDataTag, recipientData);
        extras.putString(pinTag, pinCode);

        // Add all the extras content to the intent
        toEndActivity.putExtras(extras);
        startActivity(toEndActivity);

        finish();
    }

    protected void onDestroy() {
        if (mLockerManager != null) {
            mLockerManager.unregisterListener();
        }
        mLockerManager = null;
        super.onDestroy();
    }
}
