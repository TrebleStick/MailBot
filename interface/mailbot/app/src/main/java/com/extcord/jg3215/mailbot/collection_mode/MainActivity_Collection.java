package com.extcord.jg3215.mailbot.collection_mode;

import android.Manifest;
import android.app.Activity;
import android.arch.persistence.room.Room;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.extcord.jg3215.mailbot.LockerManager;
import com.extcord.jg3215.mailbot.R;
import com.extcord.jg3215.mailbot.StopWatch;
import com.extcord.jg3215.mailbot.BluetoothConnectionService;
import com.extcord.jg3215.mailbot.database.LockerItem;
import com.extcord.jg3215.mailbot.database.LockerItemDatabase;
import com.extcord.jg3215.mailbot.delivery_mode.EnRouteActivity_Delivery;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Set;

public class MainActivity_Collection extends AppCompatActivity {

    // I assume Android Studio provides security against memory leaks for Rooms - WRONG LMAO
    // Database is a singleton object
    private final static String DATABASE_NAME = "lockerDB";
    private LockerItemDatabase lockerItemDatabase;

    // The image views that are being used like buttons
    ImageView letterView;
    ImageView largeLetterView;
    ImageView parcelView;

    // Shows lockerState string
    TextView lockerTextView;

    // Integers used to represent the type of mail that is being sent
    /* private static final int LETTER_STANDARD = 1;
    private static final int LETTER_LARGE = 2;
    private static final int PARCEL = 3; */

    // Tag for debugging
    private static final String TAG = "MainActivity";

    private LockerManager mLockerManager;

    private Context mContext;

    // ---- Bluetooth variables ----
    private final static int REQUEST_ENABLE_BT = 1;

    // Computer MAC Address
    private final String compAddress = "B8:8A:60:62:1E:A6";

    // Android server UUID
    // private static final UUID MY_UUID = UUID.fromString("00001800-0000-1000-8000-00805f9b34fb");

    private BluetoothDevice deviceConnected;
    private BluetoothAdapter mBluetoothAdapter;

    // public static BluetoothConnectionService mBluetoothConnection;
    // This object is a singleton
    private BluetoothConnectionService mBluetoothConnection;

    // StopWatch is used to determine when scan times out
    private StopWatch scanTimer;
    // Boolean indicates whether or not the timer has started
    private boolean startTimer;

    // Indicates whether the app processes have been started - used in onResume to determine if a bond exists
    private boolean started;

    // Listens for message that lockers are full
    private BroadcastReceiver mBroadcastReceiverFullLocker = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Broadcast Receiver: Full Locker");
            String text = intent.getStringExtra("lockerFull");
            Log.i(TAG, "MainActivity: Broadcast received: " + text);

            // mBluetoothConnection.setmContext(mContext);
            LocalBroadcastManager.getInstance(mContext).registerReceiver(mBroadcastReceiverComm, new IntentFilter("incomingMessage"));

            String startCommCode = "0203";
            byte[] startBytes = startCommCode.getBytes(Charset.defaultCharset());
            mBluetoothConnection.write(startBytes);
            Log.i(TAG, "Written: " + startCommCode + " to Output Stream");
        }
    };

    private BroadcastReceiver mBroadcastReceiverComm = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Broadcast Receiver: Communication");
            String text = intent.getStringExtra("theMessage");
            Log.i(TAG, "Received: " + text);

            // Removed default case (restarts communication)
            switch (text) {
                case "3020":
                    String sendLL = "SLL";
                    byte[] sllBytes = sendLL.getBytes(Charset.defaultCharset());
                    mBluetoothConnection.write(sllBytes);
                    Log.i(TAG, "Written: " + sendLL + " to Output Stream");
                    break;
                case "list":
                    // Ideally, this should be done asynchronously so the query is done by the time the data is needed
                    lockerItemDatabase = Room.databaseBuilder(getApplicationContext(), LockerItemDatabase.class, DATABASE_NAME).allowMainThreadQueries().build();
                    List<LockerItem> lockerItemList;
                    lockerItemList = lockerItemDatabase.lockerDataAccessObject().readLockerItem();
                    String deliveryLocations = getDeliveryLocations(lockerItemList);

                    Log.i(TAG, "Location List: " + deliveryLocations);
                    byte[] locListBytes = deliveryLocations.getBytes(Charset.defaultCharset());
                    mBluetoothConnection.write(locListBytes);
                    Log.i(TAG, "Written: " + deliveryLocations + " to Output Stream");

                    LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mBroadcastReceiverComm);
                    Log.i(TAG, "Unregistered mBroadcastReceiverComm");

                    // Go to EnRouteActivity
                    toEnRouteActivity();
                    break;
            }
        }
    };

    // Activity is told that Connected thread is available
    private BroadcastReceiver mBroadcastReceiverConnectedThreadAvailable = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Broadcast Receiver: Connected Thread Available");
            boolean connThreadStatus = intent.getBooleanExtra("Status", true);
            Log.i(TAG, "Connected Thread Update Received: Connection made = " + String.valueOf(connThreadStatus));
            started = true;

            if (connThreadStatus) {
                Log.i(TAG, "Establishing communication between device and app");

                // unregister broadcast receivers here as they should not be needed anymore
                unregisterReceiver(mBroadcastReceiverScanResult);
                LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mBroadcastReceiverScanComplete);
                LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mBroadcastReceiverConnectedThreadAvailable);
                Log.i(TAG, "Broadcast receivers unregistered: Scan result, ConnThread, ScanComplete");

                String hwResponse = "hey";
                byte[] hwBytes = hwResponse.getBytes(Charset.defaultCharset());
                mBluetoothConnection.write(hwBytes);
            }
        }
    };

    // Listens for new devices picked up by the bluetooth scan
    private BroadcastReceiver mBroadcastReceiverScanResult = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Broadcast Receiver: Scan Result");
            final String action = intent.getAction();
            Log.i(TAG, "onReceive: ACTION FOUND");

            if (action != null) {
                if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                    // Get the discovered bluetooth device
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                    // NOTE: In some cases, device.getName() will return null even though the device has been picked up
                    // This does not cause any errors and in any case, you really want the address of the device.
                    Log.i(TAG, "onReceive: " + device.getName() + ": " + device.getAddress());

                    if (device.getAddress().equals(compAddress)) {
                        Log.i(TAG, "Newly discovered device is the target Bluetooth module.");

                        scanTimer.stopExecuting();
                        deviceConnected = device;

                        try {
                            // UUID deviceUUID = UUID.fromString(deviceConnected.getUuids()[0].getUuid().toString());
                            Log.i(TAG, "UUID: " + deviceConnected.getUuids()[0].getUuid().toString());
                            beginDeviceConnection();
                        } catch (NullPointerException e) {
                            Log.i(TAG, "Device does not have a UUID??: " + e.getMessage());
                        }
                        // NOTE: mBluetoothAdapter.cancelDiscovery() is called by the BluetoothConnectionService object
                    }
                }
            }
        }
    };

    // StopWatch thread notifies the main thread that the timer Bluetooth scan has timed out
    private BroadcastReceiver mBroadcastReceiverScanComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Broadcast Receiver: Scan Complete");
            String status = intent.getStringExtra("scanStatus");
            Log.d(TAG, "Scan timeout broadcast received.");

            if (status.equals("complete")) {
                Log.i(TAG, "Bluetooth scan complete with no matching results found.");

                if (mBluetoothAdapter.isDiscovering()) {
                    Log.d(TAG, "Cancelling discovery");
                    Log.d(TAG, String.valueOf(scanTimer.getUpdateTime()));
                    mBluetoothAdapter.cancelDiscovery();
                }

                if (!bondExisting()) {
                    Log.i(TAG, "There is no bond between the tablet and computer");
                    scanTimer = new StopWatch(15000, mContext, 1);

                    Log.i(TAG, "Timer started");
                    if (deviceConnected == null) {
                        mBluetoothAdapter.startDiscovery();
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.collection_activity_main);
        Log.i(TAG, "onCreate() method called.");

        View decorView = getWindow().getDecorView();
        // Hide both the navigation bar and the status bar.
        // SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
        // a general rule, you should design your app to hide the status bar whenever you
        // hide the navigation bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        // mBluetoothConnection = BluetoothConnectionService.getBcsInstance();
        lockerItemDatabase = LockerItemDatabase.getInstance(this.getApplicationContext());

        mLockerManager = new LockerManager(this);
        mLockerManager.setLockerState(LockerManager.EMPTY_LOCKER);

        // Solved unique constraint error by adding an 'OnConflictStrategy' in the DAO
        // lockerItemDatabase = Room.databaseBuilder(getApplicationContext(), LockerItemDatabase.class, DATABASE_NAME).allowMainThreadQueries().build();
        mContext = this;

        letterView = (ImageView) findViewById(R.id.letter);
        letterView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "letter View selected");

                // Determines whether there is a locker free
                int aLockers = mLockerManager.getAvailability(LockerManager.LETTER_STANDARD);
                if (aLockers > 0) {
                    Log.i(TAG, "Lockers available = " + String.valueOf(aLockers));
                    toDetailsActivity(LockerManager.LETTER_STANDARD);
                } else {
                    Log.i(TAG, "No lockers available");
                    Toast.makeText(MainActivity_Collection.this, getResources().getString(R.string.lockerSizeUnavailable), Toast.LENGTH_SHORT).show();
                }
            }
        });

        lockerTextView = (TextView) findViewById(R.id.testLockerManager);
        lockerTextView.setText(mLockerManager.getLockerState());

        largeLetterView = (ImageView) findViewById(R.id.largeLetter);
        largeLetterView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "Large letter View selected");

                // Determines whether there is a locker free
                int aLockers = mLockerManager.getAvailability(LockerManager.LETTER_LARGE);
                if (aLockers > 0) {
                    Log.i(TAG, "Lockers available = " + String.valueOf(aLockers));
                    toDetailsActivity(LockerManager.LETTER_LARGE);
                } else {
                    Log.i(TAG, "No lockers available");
                    Toast.makeText(MainActivity_Collection.this, getResources().getString(R.string.lockerSizeUnavailable), Toast.LENGTH_SHORT).show();
                }
            }
        });

        parcelView = (ImageView) findViewById(R.id.largeParcel);
        parcelView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "parcel View selected");

                // Determines whether there is a locker free
                int aLockers = mLockerManager.getAvailability(LockerManager.PARCEL);
                if (aLockers > 0) {
                    Log.i(TAG, "Lockers available = " + String.valueOf(aLockers));
                    toDetailsActivity(LockerManager.PARCEL);
                } else {
                    Log.i(TAG, "No lockers available");
                    Toast.makeText(MainActivity_Collection.this, getResources().getString(R.string.lockerSizeUnavailable), Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Bluetooth setup
        deviceConnected = null;

        // Requires API level 18 and higher - currently miniSDK is 21
        final BluetoothManager bluetoothManager;
        try {
            bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

            if (bluetoothManager != null) {
                mBluetoothAdapter = bluetoothManager.getAdapter();
            }
            Log.i(TAG, "Bluetooth Adapter assigned");
        } catch (NullPointerException e) {
            throw new NullPointerException("No BluetoothManager found: " + e.getMessage());
        }

        // Register broadcast receiver to this instance of the activity
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiverFullLocker, new IntentFilter("lockerFull"));
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiverConnectedThreadAvailable, new IntentFilter("connectedThreadStatusUpdate"));
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiverScanComplete, new IntentFilter("timedBluetoothScan"));

        IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mBroadcastReceiverScanResult, discoverDevicesIntent);

        startTimer = false;
        started = false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "Request to enable Bluetooth made");
        // means that the user did not enable bluetooth and causes the user to quit if they deny bluetooth permissions?
        // if requestCode is the inputted value (REQUEST_ENABLE_BT), a different value was not passed to onActivityResult -> method not executed
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }

        // Needed for API 21 and higher - check BT permissions in the manifest
        Log.i(TAG, "Checking BT Permissions");
        checkBTPermissions();

        Log.i(TAG, "Beginning scan for unpaired devices");
        super.onActivityResult(requestCode, resultCode, data);
    }

    // Can ignore the error lines beneath the functions
    private void checkBTPermissions() {
        Log.i(TAG, "checkBTPermissions() method called");
        // NOTE: I changed the miniSDK version to 21... Can get rid of the if condition here
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0) {
                // You can use any number at the end apparently
                this.requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001);
            }
        } else {
            Log.d(TAG, "checkBTPermissions: No need to check permissions. SDK Version < API 21");
        }
    }

    // TODO: Make the two functions below this a part of BluetoothConnectionService
    private void beginDeviceConnection() {
        Log.i(TAG, "beginDeviceConnection() method called");
        Log.i(TAG, "Creating Bluetooth Connection Service");
        // mBluetoothConnection = new BluetoothConnectionService(MainActivity_Collection.this, MY_UUID); // , deviceUUID);
        mBluetoothConnection = BluetoothConnectionService.getBcsInstance();
        // startConnection();
    }

    private boolean bondExisting() {
        Log.i(TAG, "bondExisting() method called");
        Set<BluetoothDevice> bondedDevices = mBluetoothAdapter.getBondedDevices();
        if (bondedDevices.size() > 0) {
            for (BluetoothDevice currentDevice : bondedDevices) {
                Log.i(TAG, "Device: " + currentDevice.getName() + ": " + currentDevice.getAddress());

                if (currentDevice.getAddress().equals(compAddress)) {
                    Log.i(TAG, "Device is already bonded to phone. Begin connecting.");
                    deviceConnected = currentDevice;
                    // mBluetoothConnection = new BluetoothConnectionService(MainActivity_Collection.this, MY_UUID);
                    mBluetoothConnection = BluetoothConnectionService.getBcsInstance();

                    // startConnection();
                    return true;
                }
            }
        }
        return false;
    }
    // ----------------------------------------

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume() method called");

        // Check if bluetooth is on. if not, then request that the user puts it on
        // Assume that it is kept on after this action.
        // NOTE: NEVER PUT THIS REQUEST IN onCREATE - It causes the app to malfunction
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

            // If the user does not choose to make a change, the activity finishes.
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            Log.i(TAG, "Bluetooth notice: Requested user enable Bluetooth. Try starting the scan again");
        } else {
            Log.i(TAG, "Bluetooth is enabled");

            if (!started) {
                if (!startTimer && !bondExisting()) {
                    Log.i(TAG, "There is no bond between the tablet and computer");
                    scanTimer = new StopWatch(15000, this, StopWatch.BT_TIMER);

                    Log.i(TAG, "Timer started");
                    if (deviceConnected == null) {
                        mBluetoothAdapter.startDiscovery();
                    }
                    startTimer = true;
                }
            }
        }

        Log.i(TAG, "Locker state = " + mLockerManager.getLockerState());
        lockerTextView.setText(mLockerManager.getLockerState());
    }

    private void toDetailsActivity(int packageType) {
        Log.i(TAG, "toDetailsActivity() method called");
        String packageTag = "packageType";

        Intent toDetailActivity = new Intent(this, DetailsActivity_Collection.class);
        Bundle extras = new Bundle();

        // Adds this extra detail to bundle
        extras.putInt(packageTag, packageType);

        // Bundle added to the intent and contains data indicating what kind of package the user is sending
        toDetailActivity.putExtras(extras);
        startActivity(toDetailActivity);
        Log.i(TAG, "Detail Activity started with the extra: " + packageTag + ": " + String.valueOf(packageType));
    }

    // Provides locations but also checks that database do what is supposed to do
    private String getDeliveryLocations(List<LockerItem> itemList) {
        Log.i(TAG, "getDeliveryLocations() method called");

        StringBuilder locationList = new StringBuilder("");
        int index = 0;

        for (LockerItem lockerItems : itemList) {
            int nLocker = lockerItems.getLockerNo();
            String sName = lockerItems.getSenderName();
            String rName = lockerItems.getRecipientName();

            String dLocation = lockerItems.getDeliveryLocation();

            if (index != 6) {
                locationList.append(dLocation);
                locationList.append(" ");
            } else {
                locationList.append(dLocation);
            }

            Log.i(TAG, "Index = " + String.valueOf(index));
            Log.i(TAG, "Location at this index = " + dLocation);
            Log.i(TAG, "Locker Number: " + String.valueOf(nLocker) + ", Sender Name: " + sName + ", Recipient Name: " + rName);
            index++;
        }

        // Extract the delivery locations from the database of lockerItem information
        Log.i(TAG, "Delivery Location list successfully created");
        return locationList.toString();
    }

    private void toEnRouteActivity() {
        Log.i(TAG, "toEnRouteActivity() method called");
        Intent toEnRouteActivityIntent = new Intent(this, EnRouteActivity_Delivery.class);

        startActivity(toEnRouteActivityIntent);
    }

    protected void onDestroy() {
        if (mLockerManager != null) {
            mLockerManager.unregisterListener();
            mLockerManager = null;
        }

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiverFullLocker);

        Log.i(TAG, "Broadcast Receivers unregistered");
        Log.i(TAG, "onDestroy() complete");

        // mContext.deleteDatabase(DATABASE_NAME);
        LockerItemDatabase.destroyDatabase();
        super.onDestroy();
    }
}
