package icechen1.com.blackbox.audio;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import de.greenrobot.event.EventBus;
import icechen1.com.blackbox.common.AppUtils;
import icechen1.com.blackbox.common.DatabaseHelper;
import icechen1.com.blackbox.messages.AudioBufferMessage;
import icechen1.com.blackbox.messages.RecordingSavedMessage;
import icechen1.com.blackbox.provider.recording.RecordingContentValues;

import static humanize.Humanize.prettyTimeFormat;

public class AudioBufferManager extends Thread{
    private static AudioBufferManager mInstance = null;
    private final Context mContext;

    static String LOG_TAG = "BlackBox";
    private final OnAudioRecordStateUpdate mCallback;
    AudioRecord arecord;
    int sampleRate;
    static int buffersize;
    private boolean started = true;
    int mBufferDuration;

    public interface OnAudioRecordStateUpdate{
        void onRecordingSaved();
        void onRecordingError(Exception e);
    }

    public static AudioBufferManager getInstance(Context cxt, int time, OnAudioRecordStateUpdate l){
        if(mInstance != null){
            return mInstance;
        }else{
            mInstance = new AudioBufferManager(cxt, time, l);
            return mInstance;
        }
    }

    public static AudioBufferManager getInstanceIfExisting(){
        return mInstance;
    }

    public boolean isRecording(){
        return started;
    }

    public int getDuration(){
        return mBufferDuration;
    }

    public AudioBufferManager(Context cxt, int time, OnAudioRecordStateUpdate l) {
        mCallback = l;
        mContext = cxt;
        mBufferDuration =time;
        // Prepare the AudioRecord & AudioTrack
        buffersize = 3584;
        try {
            //Find the best supported sample rate
            for (int rate : new int[] {8000, 11025, 16000, 22050, 44100}) {  // add the rates you wish to check against
                //TODO Stereo support requires changing some buffer sizes
                int bufferSize = AudioRecord.getMinBufferSize(rate, AudioFormat.CHANNEL_IN_MONO , AudioFormat.ENCODING_PCM_16BIT);
                if (bufferSize != AudioRecord.ERROR_BAD_VALUE) {
                    // buffer size is valid, Sample rate supported
                    sampleRate = rate;
                    buffersize = bufferSize;
                    Log.i(LOG_TAG,"Recording sample rate:" + sampleRate + " with buffer size:"+ buffersize);
                }
            }

            Log.i(LOG_TAG,"Final sample rate:" + sampleRate + " with buffer size:"+ buffersize);

            Log.i(LOG_TAG,"Initializing Audio Record and Audio Playing objects");
            Log.i(LOG_TAG,"Length of time is: " + mBufferDuration + " s");

        } catch (Throwable t) {
            Log.e(LOG_TAG, "Initializing Audio Record and Play objects Failed "+t.getLocalizedMessage());
        }
        //Set up the recorder and the player
        arecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                sampleRate, AudioFormat.CHANNEL_IN_MONO,
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
        CircularByteBuffer circBuffer = new CircularByteBuffer(sampleRate * mBufferDuration * 2);  //2 is a magic number
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
            long actualTime = AppUtils.getBufferSavedTime(startedTime, currentMillis, mBufferDuration);
            //save entry to the database
            RecordingContentValues saved = DatabaseHelper.saveRecording(mContext, "Recorded on " + new SimpleDateFormat("dd MMM").format(new Date(currentMillis)), writer.getPath(), actualTime, currentMillis);
            EventBus.getDefault().post(new RecordingSavedMessage(saved));


        } catch (IOException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "I/O error", e);
            if(mCallback != null){
                mCallback.onRecordingError(e);
            }
        } catch (Exception e){
            e.printStackTrace();
            if(mCallback != null){
                mCallback.onRecordingError(e);
            }
        }

        arecord.stop();
        arecord.release();

        Log.i(LOG_TAG, "loopback exit");

        mInstance = null; //remove the instance reference


        if(mCallback != null){
            mCallback.onRecordingSaved();
        }
    }
    public void close() {
        started = false;
        //arecord.release();
    }
}
