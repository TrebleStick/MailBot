package com.extcord.jg3215.mailbot.delivery_mode;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.extcord.jg3215.mailbot.R;

/**
 * Created by javigeis on 12/11/2018.
 */

public class UnsuccessfulActivity_Delivery extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.delivery_activity_unsuccessful);

        // Log that delivery was unsuccessful
        // Notify the computer that the delivery was unsuccessful

        // Start a timer and after 20s, return toEnRouteActivity()
    }
}
