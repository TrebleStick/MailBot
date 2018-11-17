package com.extcord.jg3215.mailbot.CollectionMode;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.extcord.jg3215.mailbot.R;

/**
 * Created by IChinweze on 16/11/2018.
 */

public class EndActivityDialogFragment extends DialogFragment {

    private final static String TAG = "EndActivityDialogFrag";

    /* The activity creating an instance of the dialog fragment must implement this interface in order
    to receive event callbacks. Each method passes the DialogFragment in case the host needs to query it. */
    public interface EndActivityDialogListener {
        public void onSmallLockerSelect(DialogFragment dialogFragment);
        public void onMediumLockerSelect(DialogFragment dialogFragment);
        public void onLargeLockerSelect(DialogFragment dialogFragment);
        public void onDialogNegativeClick(DialogFragment dialogFragment);
    }

    // Use this instance of the interface to deliver action events
    EndActivityDialogListener mListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.collection1_activity_end_dialog, null);

        // Attaches a listener to this image in the fragment view
        final ImageView letterImage = (ImageView) view.findViewById(R.id.smallLockerImageView);
        letterImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // letter Image has been selected
                // Notifies the host activity that the event of small locker being selected has occurred
                Log.i(TAG, "onClick() method called from dialogFragment");
                mListener.onSmallLockerSelect(EndActivityDialogFragment.this);
            }
        });

        final ImageView largeLetterImage = (ImageView) view.findViewById(R.id.medLockerImageView);
        largeLetterImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Large letter Image has been selected
                Log.i(TAG, "onClick() method called from dialogFragment");
                mListener.onMediumLockerSelect(EndActivityDialogFragment.this);
            }
        });

        final ImageView parcelImage = (ImageView) view.findViewById(R.id.largeLockerImageView);
        parcelImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Parcel Image has been selected
                Log.i(TAG, "onClick() method called from dialogFragment");
                mListener.onLargeLockerSelect(EndActivityDialogFragment.this);
            }
        });

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because it is going into the dialog layout
        builder.setView(view)
        // Add action buttons
            .setTitle(R.string.popupTitle)
            .setNegativeButton(R.string.btnCancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // User cancelled
                    mListener.onDialogNegativeClick(EndActivityDialogFragment.this);
                }
            });
        return builder.create();
    }

    // Used to instantiate the implementation of the DialogListener interface above
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            // Instantiate the EndActivityDialogListener so we can send events to the host activity
            mListener = (EndActivityDialogListener) context;
        } catch (ClassCastException e) {
            // The activity does not implement the interface -> throw an exception
            throw new ClassCastException(getActivity().toString() + " must implement EndActivityDialogListener");
        }
    }
}
