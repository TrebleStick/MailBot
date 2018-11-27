package com.extcord.jg3215.mailbot.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

/**
 * Created by IChinweze on 27/11/2018.
 */

@Database(entities = LockerItem.class, version = 1)
public abstract class LockerItemDatabase extends RoomDatabase{
    // Access RoomDatabase functions by inheritance
    public abstract LockerDataAccessObject lockerDataAccessObject();
}
