package com.extcord.jg3215.mailbot;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * NAME:        StopWatch.java
 * PURPOSE:     This class runs a stopwatch timer thread that can be used to signal the end of some
 *              timed period. It is currently used to signal:
 *                  1) Standard countdown timer
 *                  2) A timeout for the bluetooth scan
 *                  3) A timeout for waiting for user response after knocking in EnRouteActivity
 *
 *              This thread can be stopped from the main UI thread by altering the volatile isExecuting
 *              boolean variable.
 *
 * AUTHORS:     Ifeanyi Chinweze, Javi Geis
 * NOTES:       Runs a separate thread
 * REVISION:    13/12/2018
 */

public class StopWatch {

    private long StartTime;
    private long UpdateTime;

    private long timerDuration;

    private int Seconds;
    private int Minutes;
    private int Milliseconds;

    private String stopWatchTime;

    private Handler handler;

    private final static String TAG = "Stopwatch";

    private Context mContext;

    // Volatile is used to indicate that the variable's value will be modified by different threads
    private volatile boolean isExecuting;

    // Timer uses
    public final static int BT_TIMER = 1;
    public final static int KNOCK_TIMER = 2;
    public final static int TIMER = 3;

    private int state;

    // These might not actually need to be volatile
    private boolean firstKnock;
    private boolean secondKnock;
    private boolean thirdKnock;
    private boolean fourthKnock;

    // StopWatch intended for use a bluetooth timeout monitor
    public StopWatch(long duration, Context context, int state) {
        handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(runnable, 0);

        this.StartTime = SystemClock.uptimeMillis();
        this.UpdateTime = 0L;

        Seconds = 0;
        Minutes = 0;
        Milliseconds = 0;

        this.isExecuting = true;

        timerDuration = duration;
        mContext = context;

        this.state = state;
    }

    // StopWatch is used to time the event of knocking sounds
    public StopWatch(Context context, int state, long timerDuration) {
        handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(runnable, 0);

        StartTime = SystemClock.uptimeMillis();
        UpdateTime = 0L;

        this.mContext = context;
        this.isExecuting = true;

        this.state = state;

        if (state == KNOCK_TIMER) {
            firstKnock = false;
            secondKnock = false;
            thirdKnock = false;

            this.timerDuration = 135000;
        } else if (state == TIMER) {
            // Convert duration to seconds
            this.timerDuration = (timerDuration * 1000);
        }
    }

    public void setStopWatchTime(int minutes, int seconds, int millis) {
        this.stopWatchTime = String.valueOf(minutes) + ":" + String.valueOf(seconds) + ":" + String.valueOf(millis);
    }

    public String getStopWatchTime() {
        return this.stopWatchTime;
    }

    public long getUpdateTime() {
        return this.UpdateTime;
    }

    public boolean getExecuteStatus() {
        return this.isExecuting;
    }

    public void stopExecuting() {
        Log.i(TAG, "Stop executing thread");
        this.isExecuting = false;
    }

    private void knockBroadcast() {
        Log.i(TAG, "knockBroadcast() method called and knock broadcast sent");
        Intent knockIntent = new Intent("playKnock");
        knockIntent.putExtra("knockRequest", "do");
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(knockIntent);
    }

    private void unsuccessfulBroadcast() {
        Log.i(TAG, "unsuccessfulBroadcast() method called.");
        Intent UnsuccessfulIntent = new Intent("failNotification");
        UnsuccessfulIntent.putExtra("DeliveryAttempt", "fail");
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(UnsuccessfulIntent);
    }

    private void setFirstKnock(boolean setKnock) {
        this.firstKnock = setKnock;
    }

    private void setSecondKnock(boolean setKnock) {
        this.secondKnock = setKnock;
    }

    private void setThirdKnock(boolean setKnock) {
        this.thirdKnock = setKnock;
    }

    private void setFourthKnock(boolean setKnock) {
        this.fourthKnock = setKnock;
    }

    // Thread runs in the background
    // Make sure there are no issues with the removeCallbacks() method being in the else bit
    public Runnable runnable = new Runnable() {
        @Override
        public void run() {

            if (getExecuteStatus()) {
                UpdateTime = SystemClock.uptimeMillis() - StartTime;
                handler.postDelayed(this, 100);

                switch(state) {
                    case BT_TIMER:
                        if (timerDuration != 0L && UpdateTime > timerDuration) {
                            stopExecuting();

                            Seconds = ((int) (UpdateTime / 1000));
                            Minutes = Seconds / 60;
                            Milliseconds = (int) (UpdateTime % 1000);

                            setStopWatchTime(Minutes, (Seconds % 60), Milliseconds);
                            Log.i(TAG, "Stopwatch stopped at time = " + getStopWatchTime());
                            Log.i(TAG, "UpdateTime: " + String.valueOf(getUpdateTime()));

                            Intent timedBluetoothScanIntent = new Intent("timedBluetoothScan");
                            timedBluetoothScanIntent.putExtra("scanStatus", "complete");
                            LocalBroadcastManager.getInstance(mContext).sendBroadcast(timedBluetoothScanIntent);
                        }
                        break;
                    case KNOCK_TIMER:
                        if (!firstKnock && UpdateTime > 3000) {
                            Log.i(TAG, "First knock played at UpdateTime = " + String.valueOf(getUpdateTime()));
                            knockBroadcast();
                            setFirstKnock(true);
                        } else if (!secondKnock && UpdateTime > 20000) {
                            Log.i(TAG, "Second knock played at UpdateTime = " + String.valueOf(getUpdateTime()));
                            knockBroadcast();
                            setSecondKnock(true);
                        } else if (!thirdKnock && UpdateTime > 60000) {
                            Log.i(TAG, "Third knock played at UpdateTime = " + String.valueOf(getUpdateTime()));
                            knockBroadcast();
                            setThirdKnock(true);
                        } else if (!fourthKnock && UpdateTime > 120000) {
                            Log.i(TAG, "Fourth knock played at UpdateTime = " + String.valueOf(getUpdateTime()));
                            setFourthKnock(true);
                            knockBroadcast();
                        } else if (UpdateTime > timerDuration) {
                            Log.i(TAG, "Timer complete.");
                            stopExecuting();
                            unsuccessfulBroadcast();
                            Log.i(TAG, "Thread stopped at UpdateTime = " + String.valueOf(getUpdateTime()));
                        }
                        break;
                    case TIMER:
                        if (UpdateTime > timerDuration) {
                            Log.i(TAG, "Thread stopped at UpdateTime = " + String.valueOf(getUpdateTime()));
                            stopExecuting();
                            unsuccessfulBroadcast();
                        }
                        break;
                }
            } else {
                handler.removeCallbacks(runnable);
            }
        }
    };
}
