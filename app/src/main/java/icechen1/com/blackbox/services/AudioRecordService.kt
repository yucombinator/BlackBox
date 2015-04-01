package icechen1.com.blackbox.services

import android.app.IntentService
import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.widget.Toast
import icechen1.com.blackbox.audio.AudioBufferManager

public class AudioRecordService() : Service() {
    var mAudio: AudioBufferManager? = null
    override fun onBind(p0: Intent): IBinder? {
        throw UnsupportedOperationException()
    }
    override fun onCreate(){
        super.onCreate()


    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int{
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();
        startRecording()
        var mHandler = Handler()
        mHandler.postDelayed(object: Runnable{
            override fun run(){
                Toast.makeText(getApplicationContext(), "service stopping", Toast.LENGTH_SHORT).show();
                stopRecording()
            }
        }, 10000);

        // If we get killed, after returning from here, restart
        return Service.START_STICKY
    }

    fun startRecording(){

        mAudio = AudioBufferManager(100, object : AudioBufferManager.BufferCallBack{
            override fun onBufferUpdate(b: IntArray?) {
                //throw UnsupportedOperationException()
            }

        });
        mAudio!!.start()

    }
    fun stopRecording(){
        mAudio!!.close()
    }

}
