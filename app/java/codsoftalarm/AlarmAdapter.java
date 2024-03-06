package com.example.codsoftalarm;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder> {

    private final ArrayList<AlarmDataModel> alarmsList;

    DatabaseReference databaseReference;

    public AlarmAdapter(ArrayList<AlarmDataModel> alarmsList, DatabaseReference databaseReference){
        this.alarmsList = alarmsList;
        this.databaseReference = databaseReference;
    }

    @NonNull
    @Override
    public AlarmAdapter.AlarmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_alarm_set_card, parent, false);
        return new AlarmAdapter.AlarmViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlarmAdapter.AlarmViewHolder holder, int position) {

        AlarmDataModel alarm = alarmsList.get(position);

        // Format hour and minute into HH:mm format
        String timeString = String.format("%02d:%02d %s", alarm.getHour(), alarm.getMinute(), alarm.getAmPm());
        holder.textAlarmTime.setText(timeString);
        holder.alarmToggleBtn.setChecked(alarm.isSwitchEnabled());


        // Implement toggle button functionality
        holder.alarmToggleBtn.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Update alarm status in Firebase database
            // Assuming each alarm has a unique ID, you can use that ID to update the status
            String alarmId = alarm.getAlarmId();
            DatabaseReference alarmRef = FirebaseDatabase.getInstance().getReference("alarms").child(alarmId);
            alarmRef.child("switchEnabled").setValue(isChecked)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Status updated successfully
                        } else {
                            // Failed to update status
                        }
                    });
        });

    }

    @Override
    public int getItemCount() {
        return alarmsList.size();
    }

    public class AlarmViewHolder extends RecyclerView.ViewHolder {

        TextView textAlarmTime;
        SwitchMaterial alarmToggleBtn;

        public AlarmViewHolder(@NonNull View itemView) {
            super(itemView);
            textAlarmTime = itemView.findViewById(R.id.textAlarmTime);
            alarmToggleBtn = itemView.findViewById(R.id.alarmToggleBtn);
        }

    }
}
