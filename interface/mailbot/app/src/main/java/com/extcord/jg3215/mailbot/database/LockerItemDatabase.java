package com.extcord.jg3215.mailbot.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.util.Log;

/**
 * NAME:        LockerItemDatabase.java
 * PURPOSE:     This class specifies the database object as a singleton object (ie only one instance
 *              for the lifetime of the application). It creates the instance of the database that is
 *              accessed by the main UI thread.
 *
 * AUTHORS:     Ifeanyi Chinweze, Javi Geis
 * NOTES:       Abstract class
 * REVISION:    13/12/2018
 */

@Database(entities = LockerItem.class, version = 1)
public abstract class LockerItemDatabase extends RoomDatabase{

    private final static String TAG = "LockerItemDatabase";

    // Access RoomDatabase functions by inheritance
    public abstract LockerDataAccessObject lockerDataAccessObject();

    // Used to make the Database a singleton - cannot create multiple instances of this object
    private static LockerItemDatabase instance;

    // synchronized means that only one thread at a time can access the instance -> multiple instances
    // not created when mutiple threads attempt to access the object
    public static synchronized LockerItemDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(), LockerItemDatabase.class, "lockerDB")
                    .fallbackToDestructiveMigration() // Tells app to destroy previous version of DB and migrate to new one
                    .build(); // gets this instance of the database
        }
        return instance;
    }

    public static synchronized void destroyDatabase() {
        instance = null;
        Log.i(TAG, "Destroyed database");
    }
}
