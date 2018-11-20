package com.extcord.jg3215.mailbot.delivery_mode;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.extcord.jg3215.mailbot.R;

/**
 * Created by javigeis on 12/11/2018.
 */

public class OpenLockerActivity_Delivery extends AppCompatActivity {

    private final static String TAG = "OpenLockerActivity";

    Button doneButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.delivery_activity_openlocker);
        Log.i(TAG, "onCreate() method called");

        doneButton = (Button) findViewById(R.id.btnStartAgain);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick() button listener method called");

                // Tell computer to take MailBot to the next location
                // Tell computer that delivery was successful

                toEnRouteActivity();
            }
        });
    }

    private void toEnRouteActivity() {
        Log.i(TAG, "toEnRouteActivity() method called");

        // Change speech bubble text to "Thank you" for ~20s
        finish();
    }
}
