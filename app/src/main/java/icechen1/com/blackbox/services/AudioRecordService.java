package icechen1.com.blackbox.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import icechen1.com.blackbox.R;
import icechen1.com.blackbox.activities.RecordActivity;
import icechen1.com.blackbox.audio.AudioBufferManager;
import icechen1.com.blackbox.messages.GetRecordingStatusMessage;
import icechen1.com.blackbox.messages.RecordStatusMessage;


/**
 * Created by yuchen.hou on 15-06-27.
 */
public class AudioRecordService extends Service implements AudioBufferManager.OnAudioRecordStateUpdate {
    public static final int MODE_START = 1;
    public static final int MODE_STOP = 2;
    public static final int MODE_SET_PASSIVE_NOTIF = 3;
    private AudioBufferManager mAudio;

    NotificationManager mNotificationManager;

    int mRecordingLength;
    int mMode = MODE_START;

    @Override
    public void onCreate(){
        super.onCreate();
        EventBus.getDefault().register(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if(intent == null) {
            stopSelf();
            return Service.START_NOT_STICKY;
        }

        //Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();
        SharedPreferences getPrefs = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());
        int default_length = Integer.valueOf(getPrefs.getString("default_length", "300"));
        mRecordingLength = default_length;

        Bundle extras = intent.getExtras();
        if(extras != null){
            mMode = extras.getInt("mode", MODE_START); //in seconds
            mRecordingLength = extras.getInt("length", default_length); //in seconds
        }
        if(mMode == MODE_START){
            Toast.makeText(this, getResources().getString(R.string.started_listening), Toast.LENGTH_SHORT).show();
            startRecording();
        }
        else if (mMode == MODE_STOP){
            Toast.makeText(this, getResources().getString(R.string.stopped_listening), Toast.LENGTH_SHORT).show();
            stopRecording();
        }else if(mMode == MODE_SET_PASSIVE_NOTIF){
            setUpPassiveNotification();
        }

        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startRecording(){
        mAudio = AudioBufferManager.getInstance(this, mRecordingLength, this);
        mAudio.start();
        startForeground(1995, buildNotification());
        mNotificationManager.cancel(1996); //remove passive notif

        EventBus.getDefault().post(new RecordStatusMessage(RecordStatusMessage.JUST_STARTED));
    }

    private void stopRecording(){
        updateSavingNotification();
        mAudio.close();
        //set up notif
        setUpPassiveNotification();
        //emit message
        EventBus.getDefault().post(new RecordStatusMessage(RecordStatusMessage.JUST_STOPPED));
    }

    @Override
    public void onDestroy(){
        stopForeground(true);
    }

    private void updateSavingNotification(){
        Intent activityIntent = new Intent(this, RecordActivity.class);
        PendingIntent activityPIntent = PendingIntent.getActivity(this, 0, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notif = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_mic_white_36dp)
                .setUsesChronometer(true)
                .addAction(R.drawable.ic_more_horiz_white_24dp, getResources().getString(R.string.open_inapp), activityPIntent)
                .setTicker(getResources().getString(R.string.saving_recording))
                .setWhen(System.currentTimeMillis())
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(getResources().getString(R.string.saving_recording))
                .setContentIntent(activityPIntent)
                .setPriority(Notification.PRIORITY_MAX)
                .build();

        mNotificationManager.notify(
                1995,
                notif);
    }

    private Notification buildNotification(){
        Intent activityIntent = new Intent(this, RecordActivity.class);
        PendingIntent activityPIntent = PendingIntent.getActivity(this, 0, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent stopIntent = new Intent(this, AudioRecordService.class);
        stopIntent.putExtra("mode", MODE_STOP);
        PendingIntent stopPIntent = PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        //TODO Android wear support
        Notification notif = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_mic_white_36dp)
                .setUsesChronometer(true)
                .addAction(R.drawable.ic_save_white_24dp, getResources().getString(R.string.save), stopPIntent)
                .addAction(R.drawable.ic_more_horiz_white_24dp, getResources().getString(R.string.open_inapp), activityPIntent)
                .setTicker(getResources().getString(R.string.notif_recording_text))
                //.setSubText(getResources().getString(R.string.notif_recording_text))
                .setWhen(System.currentTimeMillis())
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(getResources().getString(R.string.notif_recording_text))
                .setContentIntent(activityPIntent)
                .setPriority(Notification.PRIORITY_MAX)
                .build();
        return notif;
    }

    private void setUpPassiveNotification() {
        SharedPreferences getPrefs = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());
        if(!getPrefs.getBoolean("always_on_notification", true)){
            mNotificationManager.cancel(1996); //remove passive notif
            return;
        }

        Intent activityIntent = new Intent(this, RecordActivity.class);
        PendingIntent activityPIntent = PendingIntent.getActivity(this, 0, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent startIntent = new Intent(this, AudioRecordService.class);
        startIntent.putExtra("mode", MODE_START);
        PendingIntent startPIntent = PendingIntent.getService(this, 0, startIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        //TODO Android wear support
        Notification notif = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_mic_white_36dp)
                .addAction(R.drawable.ic_save_white_24dp, getResources().getString(R.string.start_listening), startPIntent)
                .addAction(R.drawable.ic_more_horiz_white_24dp, getResources().getString(R.string.open_inapp), activityPIntent)
                .setTicker(getResources().getString(R.string.notif_ready))
                        //.setSubText(getResources().getString(R.string.notif_recording_text))
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(getResources().getString(R.string.notif_ready))
                .setContentIntent(activityPIntent)
                .setPriority(Notification.PRIORITY_MIN)
                .build();
        mNotificationManager.notify(1996,notif);
    }

    @Override
    public void onRecordingSaved() {
        stopForeground(true);
        stopSelf();
    }

    @Override
    public void onRecordingError(Exception e) {
        Log.e("Rewind", "Error", e);
        stopForeground(true);
        stopSelf();
    }

    @Subscribe
    public void onEvent(final GetRecordingStatusMessage event) {
        EventBus.getDefault().post(new RecordStatusMessage(
                (mAudio!= null && mAudio.isRecording()) ?
                RecordStatusMessage.STARTED :
                RecordStatusMessage.STOPPED));
    }
}
