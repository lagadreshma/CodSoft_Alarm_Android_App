package com.example.codsoftalarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class SetAlarmActivity extends AppCompatActivity {

    private TimePicker timePicker;
    private TextView btnChooseAlarmTone;
    private AppCompatButton btnSetAlarm;
    private static final int PICK_ALARM_TONE_REQUEST = 1;
    private String selectedRingtoneUri;

    AlarmManager alarmManager;
    static final int ALARM_REQ_CODE = 100;
    private String alarmId;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_alarm);

        timePicker = findViewById(R.id.timePicker);
        btnChooseAlarmTone = findViewById(R.id.btnChooseAlarmTone);
        btnSetAlarm = findViewById(R.id.setAlarmBtn);

        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("alarms");

        btnChooseAlarmTone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open the file picker
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("audio/*");
                startActivityForResult(intent, PICK_ALARM_TONE_REQUEST);
            }
        });

        btnSetAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(validateInput()){

                    int hour = timePicker.getHour();
                    int minute = timePicker.getMinute();
                    String amPm = getAmPm(hour); // Get AM/PM information

                    setAlarm(hour, minute, amPm); // Pass AM/PM information to setAlarm method
                    saveAlarmToFirebase(hour, minute); // Pass AM/PM information to saveAlarmToFirebase method

                }

            }
        });
    }

    private boolean validateInput(){

        int hour = timePicker.getHour();
        int minute = timePicker.getMinute();

        // Check if the hour and minute values are within valid ranges
        if (hour >= 0 && hour < 24 && minute >= 0 && minute < 60) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_ALARM_TONE_REQUEST && resultCode == RESULT_OK) {
            if (data != null) {
                // Get the selected alarm tone file URI
                selectedRingtoneUri = data.getData().toString();
            }
        }
    }

    private String getDefaultRingtoneUrl() {
        return "default_ringtone_url";
    }

    private void saveAlarmToFirebase(int hour, int minute) {

        String ringtoneUrl;
        if (selectedRingtoneUri != null) {
            ringtoneUrl = selectedRingtoneUri;
        } else {
            ringtoneUrl = getDefaultRingtoneUrl();
        }

        // Convert 24-hour format to 12-hour format
        int hour12 = hour % 12;
        if (hour12 == 0) {
            hour12 = 12;
        }

        // Determine if it's AM or PM
        String amPm = hour < 12 ? "AM" : "PM";

        alarmId = databaseReference.push().getKey();

        AlarmDataModel alarmDataModel = new AlarmDataModel(alarmId, hour12, minute, amPm, ringtoneUrl, true);
        databaseReference.child(alarmId).setValue(alarmDataModel).
                addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                });

        }

    private void setAlarm(int hour, int minute, String amPm) {
        // AlarmManager service
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        // Calculate the time until the alarm goes off
        long currentTime = Calendar.getInstance().getTimeInMillis();
        long alarmTime = calendar.getTimeInMillis();
        long timeUntilAlarm = alarmTime - currentTime;
        long hoursUntilAlarm = timeUntilAlarm / (60 * 60 * 1000);
        long minutesUntilAlarm = (timeUntilAlarm % (60 * 60 * 1000)) / (60 * 1000);

        // Convert hour and minute to milliseconds
        long triggerTime = calculateTriggerTime(hour, minute);

        Intent iIntent = new Intent(SetAlarmActivity.this, AlarmReceiver.class);
        iIntent.putExtra("ALARM_ID", alarmId);
        PendingIntent pIntent = PendingIntent.getBroadcast(getApplicationContext(), ALARM_REQ_CODE, iIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pIntent);

        // Show a toast message indicating when the alarm will go off
        String toastMessage;
        if (hoursUntilAlarm == 0) {
            toastMessage = "Alarm will go off in " + minutesUntilAlarm + " minutes";
        } else if (hoursUntilAlarm == 1) {
            toastMessage = "Alarm will go off in 1 hour and " + minutesUntilAlarm + " minutes";
        } else {
            toastMessage = "Alarm will go off in " + hoursUntilAlarm + " hours and " + minutesUntilAlarm + " minutes";
        }
        Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        onBackPressed();
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity(); // Finish the current activity and all activities immediately below it in the current task's stack
    }

    // Method to calculate the trigger time in milliseconds based on hour and minute
    private long calculateTriggerTime(int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // Calculate the trigger time in milliseconds
        return calendar.getTimeInMillis();
    }

    private String getAmPm(int hourOfDay) {
        if (hourOfDay >= 0 && hourOfDay < 12) {
            return "AM";
        } else {
            return "PM";
        }
    }
}
