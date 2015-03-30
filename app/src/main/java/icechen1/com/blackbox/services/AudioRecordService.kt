package icechen1.com.blackbox.services

import android.app.IntentService
import android.app.Service
import android.content.Intent
import android.os.IBinder
import icechen1.com.blackbox.audio.AudioBufferManager

public class AudioRecordService : IntentService("AudioRecordService") {
        fun startRecording(){
        var mAudio = AudioBufferManager(1000, object : AudioBufferManager.BufferCallBack{
            override fun onBufferUpdate(b: IntArray?) {
                throw UnsupportedOperationException()
            }

        });

    }
    fun stopRecording(){

    }
    override fun onHandleIntent(p0: Intent?) {
        throw UnsupportedOperationException()
    }
}
