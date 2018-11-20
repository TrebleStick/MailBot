package com.extcord.jg3215.mailbot;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

/**
 * Created by IChinweze on 19/11/2018.
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

    private String TAG = "Stopwatch";

    private Context mContext;

    // Volatile is used to indicate that the variable's value will be modified by different threads
    private volatile boolean isExecuting;

    public StopWatch(long duration, Context context) {
        handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(runnable, 0);

        StartTime = SystemClock.uptimeMillis();
        UpdateTime = 0L;

        Seconds = 0;
        Minutes = 0;
        Milliseconds = 0;

        isExecuting = true;

        timerDuration = duration;
        mContext = context;
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

        this.isExecuting = false;
    }

    // Thread runs in the background
    public Runnable runnable = new Runnable() {
        @Override
        public void run() {

            if (getExecuteStatus()) {
                UpdateTime = SystemClock.uptimeMillis() - StartTime;
                handler.postDelayed(this, 100);

                if (timerDuration != 0L && UpdateTime > timerDuration) {
                    stopExecuting();

                    Seconds = ((int) (UpdateTime / 1000));
                    Minutes = Seconds / 60;
                    Milliseconds = (int) (UpdateTime % 1000);

                    setStopWatchTime(Minutes, (Seconds % 60), Milliseconds);
                    Log.i(TAG, "Stopwatch stopped at time = " + getStopWatchTime());
                    Log.i(TAG, "UpdateTime: " + String.valueOf(getUpdateTime()));

                    handler.removeCallbacks(runnable);
                }
            }
        }
    };
}
