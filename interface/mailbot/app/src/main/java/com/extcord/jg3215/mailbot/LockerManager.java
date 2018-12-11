package com.extcord.jg3215.mailbot;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * Created by IChinweze on 22/11/2018.
 * LockerManager objects allow manipulation of a seven digit string used to represent locker state
 * from any activity. The digits are stored in a file that can be accessed from each activity (called
 * a sharedPreferences file). There are seven lockers so each digit refers to a specific locker.
 * There are 4 small lockers, 2 medium lockers and 1 large one.
 */

public class LockerManager implements SharedPreferences.OnSharedPreferenceChangeListener {

    private final static String TAG = "LockerManager";

    // Gets the activity that this object belongs to
    private Context mContext;

    // Used to access file that stores locker state data
    private SharedPreferences sharedPreferences;

    // Preferences are stored in key-value pairs. Key is used to retrieve availability string
    private String key = "key";

    private String previousLockerState;

    // Variable that will contain the string representing locker state
    private String lockerState;

    public final static String EMPTY_LOCKER = "0000000";
    public final static String FULL_LOCKER = "1111111";

    // Integers used to represent the type of mail that is being sent
    public static final int LETTER_STANDARD = 1;
    public static final int LETTER_LARGE = 2;
    public static final int PARCEL = 3;

    // Edit the contents of the sharedPreference file
    private SharedPreferences.Editor editor;

    // Registers whether or not the app is being used in test mode or nah
    // NOTE: SharedPreferences will remain 0000000 unless this is false
    // boolean isFirstTime;

    // Get the locker that has been chosen for mail item
    private int selectLockerIndex;

    public LockerManager(Context context) {
        mContext = context;
        String filename = "lockerPrefs";

        sharedPreferences = mContext.getSharedPreferences(filename, Context.MODE_PRIVATE);
        lockerState = sharedPreferences.getString(key, null);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    // Gets the data from the availability string
    public int getAvailability(int packageType) {

        Log.i(TAG, mContext.getClass().getSimpleName() + ": getAvailability() method called");

        if (lockerState != null) {
            Log.i(TAG, mContext.getClass().getSimpleName() + ": lockerState not null");

            // Counts how many lockers are available
            int aCount = 0;

            // 4 small, 2 medium, 1 big
            if (packageType == LETTER_STANDARD) {
                for (int i = 0; i < 4; i++) {
                    if (lockerState.charAt(i) == '0') {
                        // if the character at that index is '0', the locker is free
                        aCount++;
                    }
                }

                Log.i(TAG, "Lockers available: " + String.valueOf(aCount));
                return aCount;
            } else if(packageType == LETTER_LARGE) {
                for (int i = 4; i < 6; i++) {
                    if (lockerState.charAt(i) == '0') {
                        aCount++;
                    }
                }

                Log.i(TAG, "Lockers available: " + String.valueOf(aCount));
                return aCount;
            } else if (packageType == PARCEL) {
                if (lockerState.charAt(6) == '0') {
                    aCount++;
                }

                Log.i(TAG, "Lockers available: " + String.valueOf(aCount));
                return aCount;
            }

        } else {
            // Might need to throw an exception
            Log.i(TAG, "sharedPreference file does not contain any values at this key");
        }

        // should not really get down here
        return 0;
    }

    // Updates the availability string
    public void updateAvailability(int lockerIndex, boolean isInserting) {

        Log.i(TAG, mContext.getClass().getSimpleName() + ": updateAvailability() method called");
        StringBuilder changeLockerState = new StringBuilder(lockerState);
        Log.i(TAG, "isInserting = " + String.valueOf(isInserting) + " at locker index = " + String.valueOf(lockerIndex));

        if (isInserting) {
            // Is inserting a mail item into the locker
            changeLockerState.setCharAt(lockerIndex, '1');
        } else {
            // Is removing a mail item from the locker
            changeLockerState.setCharAt(lockerIndex, '0');
        }

        // Update lockerState string
        previousLockerState = lockerState;
        lockerState = changeLockerState.toString();

        Log.i(TAG, mContext.getClass().getSimpleName() + ": Writing " + lockerState + " to sharedPreferences file");
        editor = sharedPreferences.edit();
        editor.putString(key, lockerState);
        editor.apply();

        Log.i(TAG, mContext.getClass().getSimpleName() + ": SharedPreference file contains: " + sharedPreferences.getString(key, null));
    }

    public void setSelectLockerIndex(int packageType) {
        Log.i(TAG, mContext.getClass().getSimpleName() + ": setSelectLockerIndex() method called");

        switch (packageType) {
            case LETTER_STANDARD:
                for (int i = 0; i < 4; i++) {
                    if (this.lockerState.charAt(i) == '0') {
                        selectLockerIndex = i;
                        Log.i(TAG, mContext.getClass().getSimpleName() + ": setSelectLockerIndex(): Locker available at index " + String.valueOf(i));
                        // Gets the first index at which locker state is 'free' for that mail item
                        break;
                    }
                }
                break;
            case LETTER_LARGE:
                for (int i = 4; i < 6; i++) {
                    if (lockerState.charAt(i) == '0') {
                        selectLockerIndex = i;
                        Log.i(TAG, mContext.getClass().getSimpleName() + ": setSelectLockerIndex(): Locker available at index " + String.valueOf(i));
                        // Gets the first index at which locker state is 'free' for that mail item
                        break;
                    }
                }
                break;
            case PARCEL:
                // should only call this function if a large locker is available so if-statement is redundant
                Log.i(TAG, mContext.getClass().getSimpleName() + ": setSelectLockerIndex(): Locker available at index 6");
                selectLockerIndex = 6;
                break;
        }
    }

    public int getSelectLockerIndex() { return this.selectLockerIndex; }

    public String getLockerState() { return this.lockerState; }

    public void setLockerState(String state) {
        this.lockerState = state;

        Log.i(TAG, mContext.getClass().getSimpleName() + ": Writing " + this.lockerState + " to sharedPreferences file");
        editor = sharedPreferences.edit();
        editor.putString(key, lockerState);
        editor.apply();
    }

    // SharedPreference listener dynamically registers a change in the sharedPreference file
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String prefKey) {
        if (prefKey.equals(this.key)) {
            Log.i(TAG, mContext.getClass().getSimpleName() + ": SharedPreference listener called");
            lockerState = sharedPreferences.getString(key, null);
            Log.i(TAG, mContext.getClass().getSimpleName() + ": Locker state changed from: " + previousLockerState + " to: " + lockerState);

            previousLockerState = lockerState;

            if (lockerState.equals(FULL_LOCKER)) {
                Log.i(TAG, mContext.getClass().getSimpleName() + ": Locker is full");
                String fullLockerString = "toEnRouteActivity";
                Intent incomingMessageIntent = new Intent("lockerFull");
                incomingMessageIntent.putExtra("lockerFull", fullLockerString);
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(incomingMessageIntent);
            }
        }
    }

    public void unregisterListener() {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }
}
