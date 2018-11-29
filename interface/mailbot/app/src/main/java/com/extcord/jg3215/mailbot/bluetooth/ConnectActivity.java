package com.extcord.jg3215.mailbot.bluetooth;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.extcord.jg3215.mailbot.StopWatch;

import java.util.ArrayList;
import java.util.UUID;

public class ConnectActivity extends AppCompatActivity {

    private final String TAG = "ConnectActivity";
    private final static int REQUEST_ENABLE_BT = 1;

    // TODO: Get MAC Address of computer that is being communicated with
    // In command line window, use ipconfig/all and look for physical address for Bluetooth Network Connection
    private final String btModuleAddress = "60-57-18-E7-39-E2";

    public ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();

    // UUID is a unique identifier for your communication protocol. You need the UUID of whatever machine
    // you are communicating with via Bluetooth from the app.
    // NOTE: I got the UUID for my computer by typing this into command line window:
        // wmic csproduct get name, identifyingnumber, uuid
        // Valerie UUID = "4C4C4544-0053-3810-8058-B5C04F423332"
    // TODO: Get the UUID of whatever computer you are communicating with
    private static final UUID MY_UUID = UUID.fromString("4C4C4544-0053-3810-8058-B5C04F423332");

    private StopWatch scanTimer;
    private boolean startTimer;

    // TODO: Send these to subsequent activities as parcelable extras?
    private BluetoothDevice deviceConnected;
    private BluetoothConnectionService mBluetoothConnection;
    private BluetoothAdapter mBluetoothAdapter;

    // Listens for new devices picked up by the bluetooth scan
    private BroadcastReceiver mBroadcastReceiverScanResult = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.i(TAG, "onReceive: ACTION FOUND");

            if (action != null) {
                if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                    // Get the discovered bluetooth device
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                    // Add it to the list of devices
                    mBTDevices.add(device);

                    // NOTE: In some cases, device.getName() will return null even though the device has been picked up
                    // This does not cause any errors and in any case, you really want the address of the device.
                    Log.i(TAG, "onReceive: " + device.getName() + ": " + device.getAddress());

                    if (device.getAddress().equals(btModuleAddress)) {
                        Log.i(TAG, "Newly discovered device is the target Bluetooth module.");

                        scanTimer.stopExecuting();
                        deviceConnected = device;
                        beginDeviceConnection();
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
            String status = intent.getStringExtra("scanStatus");
            Log.d(TAG, "Scan timeout broadcast received.");

            if (status.equals("complete")) {
                Log.i(TAG, "Bluetooth scan complete with no matching results found.");

                if (mBluetoothAdapter.isDiscovering()) {
                    Log.d(TAG, "Cancelling discovery");
                    Log.d(TAG, String.valueOf(scanTimer.getUpdateTime()));
                    mBluetoothAdapter.cancelDiscovery();
                }
            }
        }
    };

    private BroadcastReceiver mBroadcastReceiverConnectedThreadAvailable = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean connThreadStatus = intent.getBooleanExtra("Status", true);
            Log.i(TAG, "Connected Thread Update Received: Connection made = " + String.valueOf(connThreadStatus));

            if (connThreadStatus) {
                Log.i(TAG, "Establishing communication between device and app");

                // Do something now you have a connection established
            }
        }
    };

    // Watch the stream for any incoming data
    BroadcastReceiver mBroadcastReceiverMicInput = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String text = intent.getStringExtra("theMessage");

            Log.i(TAG, "Received: " + text);
            // TODO: Log data. The String 'text' will contain data from BT I/O stream
        }
    };

    // Listens for an intent to pair with the device
    // NOTE: I was using this for automatic pin entry. It is not needed if manual entry is preferred
    // for first time of using device OR if the device does not need a pin
    private BroadcastReceiver mBroadcastReceiverPairRequest = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action != null) {
                if (action.equals(BluetoothDevice.ACTION_PAIRING_REQUEST)) {
                    try {
                        Log.d(TAG, "Device has been found");
                        deviceConnected.createBond();
                        Log.d(TAG, "Creating bond");

                        // TODO: Observe what happens when the device tries to make a bond

                        // If it needs a pin, you can try using the below
                        /* int pin = 1234;

                        Log.d(TAG, "Begin auto-pairing. PIN = " + pin);
                        byte[] pinBytes;
                        pinBytes = ("" + pin).getBytes("UTF-8");
                        deviceConnected.setPin(pinBytes); */

                        beginDeviceConnection();
                    } catch (Exception e) {
                        Log.e(TAG, "Error occurred when trying to auto pair. Possibly attempted to join the wrong device");
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.activity_connect);
        // TODO: Give this activity a content view or it will not build

        Log.i(TAG, "onCreate(): ConnectActivity");
        deviceConnected = null;

        // Requires API level 18 and higher - currently miniSDK is 21
        final BluetoothManager bluetoothManager;
        try {
             bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

            if (bluetoothManager != null) {
                mBluetoothAdapter = bluetoothManager.getAdapter();
            }
        } catch (NullPointerException e) {
            throw new NullPointerException("No BluetoothManager found: " + e.getMessage());
        }

        // List of BT Devices in range of the phone/tablet
        mBTDevices = new ArrayList<>();

        // Broadcasts when a bluetooth scan result has been detected
        IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mBroadcastReceiverScanResult, discoverDevicesIntent);

        // Broadcasts when the phone/device receives a request to pair with it
        IntentFilter pairRequestFilter = new IntentFilter(BluetoothDevice.ACTION_PAIRING_REQUEST);
        registerReceiver(mBroadcastReceiverPairRequest, pairRequestFilter);

        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiverScanComplete, new IntentFilter("timedBluetoothScan"));

        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiverConnectedThreadAvailable, new IntentFilter("connectedThreadStatusUpdate"));

        startTimer = false;
        Log.d(TAG, "onCreate method completed");
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

    @Override
    protected void onResume() {
        super.onResume();

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

            if (!startTimer) {
                scanTimer = new StopWatch(15000, this, true);

                Log.i(TAG, "Timer started");
                if (deviceConnected == null) {
                    mBluetoothAdapter.startDiscovery();
                }
                startTimer = true;
            }
        }
    }

    public void startBTConnection(BluetoothDevice device, UUID uuid) {
        Log.d(TAG, "startBTConnection: Initialising RFComm Bluetooth connection.");

        // starts the Connect thread
        mBluetoothConnection.startClient(device, uuid);
    }

    // Create the method for starting a connection.
    // This will fail and app will crash if there is no paired device
    public void startConnection() {
        startBTConnection(deviceConnected, MY_UUID);
    }

    @Override
    protected void onDestroy() {

        unregisterReceiver(mBroadcastReceiverScanResult);
        unregisterReceiver(mBroadcastReceiverPairRequest);

        // You need to specify that it is a local broadcast manager or it will have issues
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiverScanComplete);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiverConnectedThreadAvailable);

        Log.i(TAG, "Broadcast Receivers unregistered");

        Log.i(TAG, "ConnectActivity finished");

        super.onDestroy();
    }

    // Can ignore the error lines beneath the functions
    private void checkBTPermissions() {
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

    private void beginDeviceConnection() {
        Log.d(TAG, "Creating Bluetooth Connection Service");
        mBluetoothConnection = new BluetoothConnectionService(ConnectActivity.this);
        startConnection();
    }
}

// Check the list of bonded devices
// Automatically bonds with the HC-05 if it is already bonded. Otherwise, it waits for connection with HC-05 to be requested
// NOTE: ERROR: It had already bonded to the bluetooth module and so mistakenly believed that it had connected to it already
        /*
        Set<BluetoothDevice> bondedDevices = mBluetoothAdapter.getBondedDevices();
        if (bondedDevices.size() > 0) {
            for (BluetoothDevice currentDevice:bondedDevices) {
                Log.i(TAG, "Device: " + currentDevice.getName() + ": " + currentDevice.getAddress());

                if (currentDevice.getAddress().equals(btModuleAddress)) {
                    Log.i(TAG, "Device is already bonded to phone. Begin connecting.");
                    deviceConnected = currentDevice;
                    mBluetoothConnection = new BluetoothConnectionService(ConnectActivity.this);
                    startConnection();

                    setupBeginActivity();
                    endConnectActivity();
                    break;
                }
            }

            // WARNING: Only use with the HC-05!
            // NOTE: Can be used for automatic entry of PIN on pair request with HC-05.
            // The receiver listens for bond/pair request and executes programmed actions in Receiver object

            /* Log.d(TAG, "Device is not bonded to phone. Listen for pairing request to apply automatic pairing methods.");
            IntentFilter pairRequestFilter = new IntentFilter(BluetoothDevice.ACTION_PAIRING_REQUEST);
            pairRequestFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
            registerReceiver(mBroadcastReceiverPairRequest, pairRequestFilter);
            Log.d(TAG, "Phone is bonded to other devices");
        } */



