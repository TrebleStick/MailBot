package com.extcord.jg3215.mailbot.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

/**
 * Created by IChinweze on 27/11/2018.
 */

@Dao
public interface LockerDataAccessObject {
    @Insert
    public void addUser(LockerItem lockerItem);

    // select all columns from [insert tableName]
    @Query("Select * from lockers")
    public List<LockerItem> readLockerItem();

    @Query("Select * from lockers WHERE locker_num = :lockerNum LIMIT 1")
    public LockerItem findLockerByID(int lockerNum);

    @Query("Select * from lockers WHERE delivery_location = :deliveryLocation")
    public List<LockerItem> findLockerByLocation(String deliveryLocation);

    @Delete
    public void deleteLockerItem(LockerItem lockerItem);
}
