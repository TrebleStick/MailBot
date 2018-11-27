package com.extcord.jg3215.mailbot.bluetooth;

// import android.app.ProgressDialog;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;

// NOTE: Android imported a logging Handler. Not what we want for bluetooth!!

public class BluetoothConnectionService {

    private static final String TAG = "BluetoothConnectionServ";
    private static final String appName = "MYAPP";

    // UUID is a unique identifier for your communication protocol. Needed for devices to recognise each other?
    // NOTE: Copied from the inter-webs. I think this specific UUID is a generic one for Bluetooth?
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private final BluetoothAdapter mBluetoothAdapter;

    Context mContext;

    private AcceptThread mInsecureAcceptThread;

    private ConnectThread mConnectThread;
    private BluetoothDevice mmDevice;

    private UUID deviceUUID;

    // Note: Progress dialogs have been deprecated. What do they do? - Show amounts of stuff that has been done
    // ProgressDialog mProgressDialog;

    private ConnectedThread mConnectedThread;

    private Handler mHandler;

    // Defines some constants to be used in this class for message transmissions states
    private interface MessageConstants {
        public static final int MESSAGE_READ = 0;
        public static final int MESSAGE_WRITE = 1;
    }

    public BluetoothConnectionService(Context context) {

        setmContext(context);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        mHandler = new Handler();

        // executes the synchronised void method. Creates our accept thread and begins cascade of processes
        start();
    }

    public void setmContext(Context context) {
        this.mContext = context.getApplicationContext();
    }

    // This thread runs while listening for incoming connections
    // It behaves like a server-side client. It runs until a connection is accepted or cancelled
    // Connect as a server
    private class AcceptThread extends Thread {
        // The local server socket
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;

            // Create a new listening server socket
            try {
                tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(appName, MY_UUID);
                Log.d(TAG, "Accept Thread: Initialising server settings: " + MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "AcceptThread: listen method failed. " + e.getMessage());
            }
            mmServerSocket = tmp;
        }

        // A blocking call. This will only show a successful connection or an exception
        // This method is automatically executed in a thread
        public void run() {
            Log.d(TAG, "Accept Thread: run method called");
            BluetoothSocket socket = null;

            // Runs until loop is broken with break; keyword
            while (true) {
                try {
                    // mmServerSocket.accept() returns null until a connection is established
                    Log.d(TAG, "Accept Thread: run RFComm server socket start...");
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "Accept Thread: IOException " + e.getMessage());
                    break;
                }

                // if socket is no longer null, a connection has been established and therefore a connected thread is made
                if (socket != null) {
                    connected(socket, mmDevice);
                    cancel();
                    break;
                }
            }
            Log.i(TAG, "End of thread");
        }

        // NOTE: Should make use of this in some exception catcher
        public void cancel() {
            Log.d(TAG, "Cancel: Cancelling AcceptThread. Accepting no more connections.");

            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Cancel: Close of AcceptThread ServerSocket Failed. " + e.getMessage());
            }
        }
    }

    // Connect as a client
    private class ConnectThread extends Thread {
        private BluetoothSocket mmSocket;

        public ConnectThread(BluetoothDevice device, UUID uuid) {
            Log.d(TAG, "ConnectThread: started");
            mmDevice = device;
            deviceUUID = uuid;
        }

        // Should just run first without tmp being = null when you createRfComm...() methinks
        // run() method is automatically executed in a thread
        public void run() {
            BluetoothSocket tmp = null;
            Log.i(TAG, "Run mConnectThread");

            // Get a BluetoothSocket for a connection with the given BluetoothDevice
            try {
                Log.d(TAG, "ConnectThread: Trying to create InsecureRfCommSocket using UUID: " + MY_UUID);
                tmp = mmDevice.createRfcommSocketToServiceRecord(deviceUUID);
            } catch (IOException e) {
                Log.e(TAG, "ConnectThread: Could not create InsecureRfCommSocket. " + e.getMessage());
            }

            mmSocket = tmp;

            // Cancel discovery because it will slow down connection. It isn't stopped directly in the main activity.
            mBluetoothAdapter.cancelDiscovery();

            try {
                // Connect to the remote device through the socket. Blocking call until it succeeds or returns an exception
                mmSocket.connect();
                Log.d(TAG, "ConnectThread: Connected!");
            } catch (IOException e) {
                // Close the socket
                try {
                    mmSocket.close();
                    Log.d(TAG, "ConnectThread: run: Socket closed.");
                } catch (IOException e1) {
                    Log.e(TAG, "mConnectThread: run: Unable to close the connection in socket. " + e1.getMessage());
                }
                Log.d(TAG, "run: ConnectThread: Could not connect to UUID: " + MY_UUID);
                return;
            }

            // Should only be called if socket connects. Connection work is done in a separate thread.
            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            Log.d(TAG, "Cancel: Closing client socket");

            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Cancel: Close of ConnectThread Socket Failed. " + e.getMessage());
            }
        }
    }

    // NOTE: Synchronized method -> only one thread can access it at a time
    // NOTE: This method initiates the AcceptThread
    public synchronized void start() {
        Log.d(TAG, "start");

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if (mInsecureAcceptThread == null) {
            mInsecureAcceptThread = new AcceptThread();

            // start() is native to all Thread objects. It causes a thread to start
            mInsecureAcceptThread.start();
        }
    }

    // AcceptThread starts and sits in wait for a connection
    // Then ConnectThread starts and attempts to make a connection with the other device's AcceptThread
    public void startClient(BluetoothDevice device, UUID uuid) {
        Log.d(TAG, "startClient: started");

        // Would initiate progress Dialog here but I don't really care much for it
        // mProgressDialog = ProgressDialog.show(mContext, "Connecting Bluetooth", "Please Wait...", true);

        mConnectThread = new ConnectThread(device, uuid);
        mConnectThread.start();
    }

    // This thread handles the connection. The connection should have been established between the app and target
    private class ConnectedThread extends Thread {

        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "ConnectedThread: Starting... ");

            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Would dismiss progress dialog box here as a connection has been established
            // mProgressDialog.dismiss();

            try {
                tmpIn = mmSocket.getInputStream();
                tmpOut = mmSocket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "ConnectedThread: Attempt to get I/O Stream failed.");
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            // byte array gets data from the input stream and stores it
            byte[] buffer = new byte[1024];

            // THEORY: Stores the number of bytes stored in buffer
            int bytes;

            // Keep listening to the InputStream until an exception occurs
            // Gives us a constantly updating stream of data
            while (true) {
                // read from the InputStream
                try {
                    // bytes is assigned the value of the number of bytes
                    bytes = mmInStream.read(buffer);

                    // incomingMessage is constructed by reading from element 0 to element bytes of the buffer byte array
                    String incomingMessage = new String(buffer, 0, bytes);
                    mHandler.obtainMessage(MessageConstants.MESSAGE_READ, bytes, -1, incomingMessage).sendToTarget();

                    Log.d(TAG, "InputStream: " + incomingMessage);

                    // Broadcasts the fact that something has been written to the I/O Stream
                    // Also broadcasts the information that has been written to the stream
                    Intent incomingMessageIntent = new Intent("incomingMessage");
                    incomingMessageIntent.putExtra("theMessage", incomingMessage);
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(incomingMessageIntent);

                    Log.d(TAG, "InputStream: Message read from target");
                } catch (IOException e) {
                    Log.e(TAG, "ConnectedThread: Error reading from InputStream: " + e.getMessage());
                    break;
                    // break leaves the while loop
                }
            }
        }

        // Call this activity from the main activity to send data to the remote device
        public void write(byte[] bytes) {
            String text = new String(bytes, Charset.defaultCharset());

            try {
                Log.d(TAG, "ConnectedThread: Writing to OutputStream (1): " + text);
                mmOutStream.write(bytes);

                // Share sent message with the UI Activity
                Message writeMsg = mHandler.obtainMessage(MessageConstants.MESSAGE_WRITE, -1, -1, bytes);

                Log.d(TAG, "ConnectedThread: Writing to OutputStream (2): " + writeMsg);
                // writeMsg.sendToTarget();
                Log.d(TAG, "InputStream: Message written to target");
            } catch (IOException e) {
                Log.e(TAG, "ConnectedThread: Error writing to OutputStream: " + e.getMessage());
            }
        }

        // Call from main activity to shutdown the connection
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "ConnectedThread: Attempt to shut down connection failed.");
            }
        }
    }

    // Seems like this just initiates an instance of the ConnectedThread ?
    private void connected(BluetoothSocket mmSocket, BluetoothDevice mmDevice) {
        Log.d(TAG, "Connected method: Devices connected. Starting... ");

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(mmSocket);
        mConnectedThread.start();

        // Once the connected thread is ready, it will communicate this to the main thread
        Intent connectedThreadAvailablilityIntent = new Intent("connectedThreadStatusUpdate");
        connectedThreadAvailablilityIntent.putExtra("Status", true);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(connectedThreadAvailablilityIntent);
    }

    // Seems to be an indirect way of calling ConnectedThread.write(byte[] byte)?
    // As this method cannot be directly accessed from main?
    public void write(byte[] out) {
        // Synchronise a copy of the ConnectedThread
        Log.d(TAG, "External write method called: Write called.");

        // Perform the write operation
        mConnectedThread.write(out);
    }
}
