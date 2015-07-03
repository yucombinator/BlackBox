package icechen1.com.blackbox.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import icechen1.com.blackbox.R;
import icechen1.com.blackbox.activities.RecordActivity;
import icechen1.com.blackbox.audio.AudioBufferManager;

/**
 * Created by yuchen.hou on 15-06-27.
 */
public class AudioRecordService extends Service {
    private static final int LENGTH_DEFAULT = 60;
    public static final int MODE_START = 1;
    public static final int MODE_STOP = 2;
    private AudioBufferManager mAudio;

    int mRecordingLength = LENGTH_DEFAULT;
    int mMode = MODE_START;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();
        Bundle extras = intent.getExtras();
        if(extras != null){
            mMode = extras.getInt("mode", MODE_START); //in seconds
            mRecordingLength = extras.getInt("length", LENGTH_DEFAULT); //in seconds
        }
        if(mMode == MODE_START){
            startRecording();
        }
        else {
            stopRecording();
        }

        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startRecording(){

        mAudio = new AudioBufferManager(this, mRecordingLength, new AudioBufferManager.BufferCallBack(){
            @Override
            public void onBufferUpdate(int[] b) {
                //throw UnsupportedOperationException()
            }

        });
        mAudio.start();
        startForeground(1, buildNotification());
    }
    private void stopRecording(){
        mAudio.close();
        stopForeground(true);
        stopSelf();
    }

    @Override
    public void onDestroy(){
        stopForeground(true);
    }

    private Notification buildNotification(){
        Intent intent = new Intent(this, RecordActivity.class);
        PendingIntent pendIntent = PendingIntent.getActivity(this, 0, intent, 0);

        //TODO Android wear support
        Notification notif = new NotificationCompat.Builder(this)
                .setUsesChronometer(true)
                .addAction(R.mipmap.ic_launcher, "TEST", pendIntent)
                .setTicker("Recording")
                .setSubText("Recording")
                .setWhen(System.currentTimeMillis())
                .setContentTitle("BlackBox")
                .setContentText("Recording")
                .setContentIntent(pendIntent)
                .build();

        return notif;
    }
}
