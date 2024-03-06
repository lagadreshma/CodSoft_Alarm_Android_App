package com.example.codsoftalarm;

public class AlarmDataModel {

    private String alarmId;
    private long hour;
    private long minute;
    private String amPm;
    private String ringtoneUrl;
    private boolean isSwitchEnabled;

    public AlarmDataModel() {

    }

    public AlarmDataModel(String alarmId, long hour, long minute, String amPm, String ringtoneUrl, boolean isSwitchEnabled) {
        this.alarmId = alarmId;
        this.hour = hour;
        this.minute = minute;
        this.amPm = amPm;
        this.ringtoneUrl = ringtoneUrl;
        this.isSwitchEnabled = isSwitchEnabled;
    }

    public String getAlarmId() {
        return alarmId;
    }

    public void setAlarmId(String alarmId) {
        this.alarmId = alarmId;
    }

    public long getHour() {
        return hour;
    }

    public long getMinute() {
        return minute;
    }

    public String getAmPm() {
        return amPm;
    }

    public void setAmPm(String amPm) {
        this.amPm = amPm;
    }

    public String getRingtoneUrl() {
        return ringtoneUrl;
    }

    public void setRingtoneUrl(String ringtoneUrl) {
        this.ringtoneUrl = ringtoneUrl;
    }

    public void setHour(long hour) {
        this.hour = hour;
    }

    public void setMinute(long minute) {
        this.minute = minute;
    }

    public boolean isSwitchEnabled() {
        return isSwitchEnabled;
    }

    public void setSwitchEnabled(boolean switchEnabled) {
        isSwitchEnabled = switchEnabled;
    }
}
