package com.example.codsoftalarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Get alarm ID from intent extras
        String alarmId = intent.getStringExtra("ALARM_ID");

        // Start AlarmActivity when the alarm goes off
        Intent alarmIntent = new Intent(context, AlarmActivity.class);
        alarmIntent.putExtra("ALARM_ID", alarmId); // Corrected spelling of "ALARM_ID"
        alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(alarmIntent);
    }
}
