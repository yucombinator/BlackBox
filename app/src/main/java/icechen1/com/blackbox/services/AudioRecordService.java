package icechen1.com.blackbox.services;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

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
}
