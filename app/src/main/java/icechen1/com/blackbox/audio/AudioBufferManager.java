package icechen1.com.blackbox.audio;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import de.greenrobot.event.EventBus;
import icechen1.com.blackbox.common.AppUtils;
import icechen1.com.blackbox.common.DatabaseHelper;
import icechen1.com.blackbox.messages.AudioBufferMessage;
import icechen1.com.blackbox.messages.RecordStatusMessage;

public class AudioBufferManager extends Thread{
    private final Context mContext;

    static String LOG_TAG = "BlackBox";
    AudioRecord arecord;
    //AudioTrack atrack;
    int sampleRate;
    static int buffersize;
    private boolean started = true;
    int bufferDuration;

    public AudioBufferManager(Context cxt, int time) {
        mContext = cxt;
        bufferDuration=time;
        // Prepare the AudioRecord & AudioTrack
        buffersize = 3584;
        try {
            //Find the best supported sample rate
            for (int rate : new int[] {8000, 11025, 16000, 22050, 44100}) {  // add the rates you wish to check against
                int bufferSize = AudioRecord.getMinBufferSize(rate, AudioFormat.CHANNEL_IN_STEREO , AudioFormat.ENCODING_PCM_16BIT);
                if (bufferSize != AudioRecord.ERROR_BAD_VALUE) {
                    // buffer size is valid, Sample rate supported
                    sampleRate = rate;
                    buffersize = bufferSize;
                    Log.i(LOG_TAG,"Recording sample rate:" + sampleRate + " with buffer size:"+ buffersize);
                }
            }

            Log.i(LOG_TAG,"Final sample rate:" + sampleRate + " with buffer size:"+ buffersize);

            Log.i(LOG_TAG,"Initializing Audio Record and Audio Playing objects");
            Log.i(LOG_TAG,"Length of time is: " + bufferDuration + " s");

        } catch (Throwable t) {
            Log.e(LOG_TAG, "Initializing Audio Record and Play objects Failed "+t.getLocalizedMessage());
        }
        //Set up the recorder and the player
        arecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                sampleRate, AudioFormat.CHANNEL_IN_STEREO,
                AudioFormat.ENCODING_PCM_16BIT, buffersize * 2);
    }
/*
    int getAudioSessionID(){
        return atrack.getAudioSessionId();
    }
    */

    @Override
    public void run() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

        //Create our buffers
        byte[] buffer  = new byte[buffersize];
        //A circular buffer
        CircularByteBuffer circBuffer = new CircularByteBuffer(buffersize * bufferDuration);
        arecord.startRecording();
        int i = 0;
        //get timestamp
        long startedTime = System.currentTimeMillis();
        // start recording and playing back
        while(started) {
            try {
                //Write
                arecord.read(buffer, 0, buffersize);
                //Read the byte array data to the circular buffer
                int inserted = circBuffer.put(buffer, 0, buffersize);
                if(i%3 == 0){
                    EventBus.getDefault().post(new AudioBufferMessage(buffer));
                }
                i++;
            } catch (Exception e) {
                e.printStackTrace();
                started = false;
            }finally{
                //If the user has clicked stop
                if(interrupted()){
                    started = false;
                }
            }
        }

        try {
            AudioFileWriter writer = new AudioFileWriter(null);
            writer.setupHeader(arecord, circBuffer.length());

            Log.i(LOG_TAG, "buffer length " + circBuffer.length());

            byte[] readbuffer = new byte[circBuffer.length()];
            int retrieved = circBuffer.get(readbuffer, 0, circBuffer.length());

            Log.i(LOG_TAG, "retrieved length " + retrieved);

            writer.write(readbuffer, retrieved);

            writer.close();

            long currentMillis = System.currentTimeMillis();
            long actualTime = AppUtils.getBufferSavedTime(startedTime, currentMillis, bufferDuration);
            //save entry to the database
            DatabaseHelper.saveRecording(mContext, String.valueOf(currentMillis), writer.getPath(), actualTime, currentMillis);

        } catch (IOException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "I/O error", e);
        } catch (Exception e){
            e.printStackTrace();
        }

        arecord.stop();
        arecord.release();

        Log.i(LOG_TAG, "loopback exit");
    }
    public void close() {
        started = false;
        //arecord.release();
    }
}
