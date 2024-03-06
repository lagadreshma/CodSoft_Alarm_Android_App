package com.example.codsoftalarm;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlarmManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView textDate;
    private TextView textTime;
    private AppCompatButton btnSetAlarm;
    private RecyclerView recyclerView;
    FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private ArrayList<AlarmDataModel> alarmsList;
    private AlarmAdapter adapter;

    private Handler handler = new Handler();
    private Runnable updateTimeRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textDate = findViewById(R.id.textDate);
        textTime = findViewById(R.id.textTime);
        btnSetAlarm = findViewById(R.id.setAlarmBtn);

        recyclerView = findViewById(R.id.recyclerView);
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("alarms");
        alarmsList = new ArrayList<>();
        adapter = new AlarmAdapter(alarmsList, databaseReference);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Update the TextViews with current date and time
        updateDateTime();

        // Schedule a task to update time TextView every second
        updateTimeRunnable = new Runnable() {
            @Override
            public void run() {
                updateDateTime();
                handler.postDelayed(this, 1000); // Schedule again in 1 second
            }
        };
        handler.post(updateTimeRunnable);

        // Handle button click to set alarm
        btnSetAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(), SetAlarmActivity.class);
                startActivity(intent);

            }
        });


        // Retrieve data from Firebase Realtime Database
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                alarmsList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    AlarmDataModel alarm = dataSnapshot.getValue(AlarmDataModel.class);
                    alarmsList.add(alarm);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Failed to retrieve data.", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void updateDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss a", Locale.getDefault());

        String currentDate = dateFormat.format(new Date());
        String currentTime = timeFormat.format(new Date());

        textDate.setText(currentDate);
        textTime.setText(currentTime);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove the update time Runnable from the handler
        handler.removeCallbacks(updateTimeRunnable);
    }

}