package com.example.hangoverassistent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;

//import SessionManager;

public class ActivityRecognitionBroadcastReceiver extends BroadcastReceiver {
//    private SessionManager sessionManager;

    public static final String INTENT_ACTION = "com.package.name" +
            ".ACTION_PROCESS_ACTIVITY_TRANSITIONS";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent!= null) {
//            sessionManager = new SessionManager(context);
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            ArrayList<DetectedActivity> detectedActivities = (ArrayList) result.getProbableActivities();

            for (DetectedActivity activity : detectedActivities) {
                getType(activity,context);
            }
        }
    }

    public void getType(DetectedActivity detectedActivity,Context context)
    {
        String label = context.getString(R.string.activity_unknown);

        switch (detectedActivity.getType()) {
            case DetectedActivity.IN_VEHICLE: {
                label = context.getString(R.string.activity_in_vehicle);
                break;
            }
            case DetectedActivity.ON_BICYCLE: {
                label = context.getString(R.string.activity_on_bicycle);
                break;
            }
            case DetectedActivity.ON_FOOT: {
                label = context.getString(R.string.activity_on_foot);
                break;
            }
            case DetectedActivity.TILTING: {
                label = context.getString(R.string.activity_tilting);
                break;
            }
            case DetectedActivity.RUNNING: {
                label = context.getString(R.string.activity_running);
                break;
            }
            case DetectedActivity.STILL: {
                label =  context.getString(R.string.activity_still);
                break;
            }
            case DetectedActivity.WALKING: {
                label = context.getString(R.string.activity_walking);
                break;
            }
            case DetectedActivity.UNKNOWN: {
                label = context.getString(R.string.activity_unknown);
                break;
            }
        }
    }
}