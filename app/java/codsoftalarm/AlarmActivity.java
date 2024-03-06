package com.example.codsoftalarm;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AlarmActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    private static final long SNOOZE_TIME = 10 * 60 * 1000; // 10 minutes in milliseconds
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private String alarmId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("alarms");

        // Play the alarm sound
        mediaPlayer = MediaPlayer.create(this, Settings.System.DEFAULT_RINGTONE_URI);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        // Get alarm ID from intent extras
        alarmId = getIntent().getStringExtra("ALARM_ID");

        // Handle stop button click
        AppCompatButton stopButton = findViewById(R.id.stop_button);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopAlarm();

                updateSwitchEnabled(false);
            }
        });

        // Handle snooze button click
        AppCompatButton snoozeButton = findViewById(R.id.snooze_button);
        snoozeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snoozeAlarm(alarmId);
            }
        });
    }

    // Method to stop the alarm
    private void stopAlarm() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        finish();
    }

    // Method to snooze the alarm
    private void snoozeAlarm(String alarmId) {
        // Stop the alarm
        stopAlarm();

        // Set a new alarm for 10 minutes later
        setSnoozeAlarm(alarmId);
    }

    // Method to set a new alarm for 10 minutes later
    // Method to set a new alarm for 10 minutes later
    private void setSnoozeAlarm(String alarmId) {
        long triggerTimeMillis = System.currentTimeMillis() + SNOOZE_TIME;

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("ALARM_ID", alarmId); // Pass the alarm ID to the receiver
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Use setExactAndAllowWhileIdle for precise alarm triggering
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent);
        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent);
        }

        // Display a toast message confirming snooze
        Toast.makeText(this, "Alarm snoozed for 10 minutes", Toast.LENGTH_SHORT).show();
    }

    // Method to update switchEnabled status in the Firebase Realtime Database
    private void updateSwitchEnabled(boolean isEnabled) {
        if (alarmId != null) {
            databaseReference.child(alarmId).child("switchEnabled").setValue(isEnabled);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Release media player resources
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
