package icechen1.com.blackbox.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import icechen1.com.blackbox.R;
import icechen1.com.blackbox.RewindWidget;
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
    private int mPriority;

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

        mPriority = Notification.PRIORITY_MAX;
        if (getPrefs.getBoolean("stealth_notifications", false)) {
            mPriority = Notification.PRIORITY_MIN;
        }

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
        mAudio = new AudioBufferManager(this, mRecordingLength, this);
        mAudio.start();
        startForeground(1995, buildNotification());
        mNotificationManager.cancel(1996); //remove passive notif
        updateWidgets(true);
    }

    private void stopRecording(){
        if(mAudio != null) {
            updateSavingNotification();
            mAudio.close();
        }

        //set up notif
        setUpPassiveNotification();
        //emit message
        EventBus.getDefault().post(new RecordStatusMessage(RecordStatusMessage.JUST_STOPPED));
        updateWidgets(false);
    }

    public void updateWidgets(boolean started) {
        AppWidgetManager manager = AppWidgetManager.getInstance(getApplication());
        int ids[] = manager.getAppWidgetIds(new ComponentName(getApplication(), RewindWidget.class));

        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : ids) {
            RewindWidget.updateAppWidget(this, manager, appWidgetId, started);
        }
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
            .addAction(R.drawable.ic_more_horiz_white_24dp, getResources().getString(R.string.open_inapp), activityPIntent)
            .setTicker(getResources().getString(R.string.saving_recording))
            .setContentTitle(getResources().getString(R.string.app_name))
            .setContentText(getResources().getString(R.string.saving_recording))
            .setContentIntent(activityPIntent)
            .setPriority(mPriority)
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
            .setPriority(mPriority)
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
        if(mAudio == null || !mAudio.isRecording()){
            stopForeground(true);
            stopSelf();
        }
    }

    @Override
    public void onRecordingError(final String s, Exception e) {
        Log.e("Rewind", "Error", e);
        Handler h = new Handler(getMainLooper());
        h.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
                EventBus.getDefault().post(new RecordStatusMessage(RecordStatusMessage.JUST_STOPPED));
            }
        });
        stopForeground(true);
        stopSelf();
    }

    @Subscribe
    public void onEvent(final GetRecordingStatusMessage event) {
        if(mAudio != null && mAudio.isRecording()) {
            EventBus.getDefault().post(new RecordStatusMessage(RecordStatusMessage.STARTED,  mAudio.getBufferSize(), mAudio.getSampleRate()));
        } else {
            EventBus.getDefault().post(new RecordStatusMessage(RecordStatusMessage.STOPPED));
        }
    }
}
