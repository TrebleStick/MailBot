package com.extcord.jg3215.mailbot.delivery_mode;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.extcord.jg3215.mailbot.R;

/**
 * Created by javigeis on 12/11/2018.
 */

public class PasswordActivity_Delivery extends AppCompatActivity {

    private final static String TAG = "PasswordActivity";

    // User use this button to submit the pin code they have typed in
    Button submitButton;

    // Collects the pin submitted by the user
    EditText pinEditText;

    private int pinAttempt = 0;

    // TODO: Do we want this as a String or an int?
    private String pinCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.delivery_activity_password);
        Log.i(TAG, "onCreate() method called");

        // get pinCode from intent created in EnRouteActivity
        // TODO: Remove this pinCode assignment
        pinCode = "0000";

        pinEditText = (EditText) findViewById(R.id.pinInput);

        submitButton = (Button) findViewById(R.id.buttonSubmit);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pinEditText.getText().toString().equals(pinCode)) {
                    // Ask computer to open locker
                    toOpenLockerActivity();
                } else {
                    pinAttempt++;

                    // Tells user that pin was incorrect and how many attempts they have left
                    Toast.makeText(PasswordActivity_Delivery.this, (getResources().getString(R.string.incorrectPin)) + " " + String.valueOf(3 - pinAttempt), Toast.LENGTH_LONG).show();

                    if (pinAttempt == 3) {
                        toUnsuccessfulActivity();
                    }
                }
            }
        });
    }

    private void toOpenLockerActivity() {
        Intent toOpenLockerActivityIntent = new Intent(this, OpenLockerActivity_Delivery.class);
        startActivity(toOpenLockerActivityIntent);
        finish();
    }

    private void toUnsuccessfulActivity() {
        Intent toUnsuccessfulActivityIntent = new Intent(this, UnsuccessfulActivity_Delivery.class);
        startActivity(toUnsuccessfulActivityIntent);
        finish();
    }
}
