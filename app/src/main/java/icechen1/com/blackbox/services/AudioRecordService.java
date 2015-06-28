package icechen1.com.blackbox.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import icechen1.com.blackbox.R;
import icechen1.com.blackbox.activities.RecordActivity;
import icechen1.com.blackbox.audio.AudioBufferManager;

/**
 * Created by yuchen.hou on 15-06-27.
 */
public class AudioRecordService extends Service {
    private AudioBufferManager mAudio;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();
        startRecording();
        Handler mHandler = new Handler();
        mHandler.postDelayed(new Runnable(){
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "service stopping", Toast.LENGTH_SHORT).show();
                stopRecording();
            }
        }, 10000);

        // If we get killed, after returning from here, restart

        startForeground(0, buildNotification());
        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    void startRecording(){

        mAudio = new AudioBufferManager(100, new AudioBufferManager.BufferCallBack(){
            @Override
            public void onBufferUpdate(int[] b) {
                //throw UnsupportedOperationException()
            }

        });
        mAudio.start();

    }
    void stopRecording(){
        mAudio.close();
    }

    Notification buildNotification(){
        Intent intent = new Intent(this, RecordActivity.class);
        PendingIntent pendIntent = PendingIntent.getActivity(this, 0, intent, 0);

        Notification notif = new Notification.Builder(this)
                .setUsesChronometer(true)
                .addAction(R.mipmap.ic_launcher, "TEST", pendIntent)
                .setTicker("Recording")
                .setSubText("Recording")
                .setContentTitle("BlackBox")
                .setContentText("Recording")
                .setContentIntent(pendIntent)
                .build();

        return notif;
    }
}
