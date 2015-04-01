package icechen1.com.blackbox.audio;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class AudioBufferManager extends Thread{
    public interface BufferCallBack {
        public void onBufferUpdate(int[] b);
    }
    static String LOG_TAG = "BlackBox";
    AudioRecord arecord;
    //AudioTrack atrack;
    int sampleRate;
    static int buffersize;
    private boolean started = true;
    private static BufferCallBack mCallBack;
    int bufferDuration;

    public AudioBufferManager(int time, BufferCallBack callback) {
        mCallBack = callback;
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
            Log.i(LOG_TAG,"Length of time is: " + bufferDuration + " ms");

        } catch (Throwable t) {
            Log.e(LOG_TAG, "Initializing Audio Record and Play objects Failed "+t.getLocalizedMessage());
        }
        //Set up the recorder and the player
        arecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                sampleRate, AudioFormat.CHANNEL_IN_STEREO,
                AudioFormat.ENCODING_PCM_16BIT, buffersize * 1);
        /*int _audioTrackSize = android.media.AudioTrack.getMinBufferSize(
                sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        atrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, _audioTrackSize,
                AudioTrack.MODE_STREAM);
        atrack.setPlaybackRate(sampleRate);*/
    }
/*
    int getAudioSessionID(){
        return atrack.getAudioSessionId();
    }
    */

    @Override
    public void run() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
        //AudioRecord recorder = null;

        //Create our buffers
        byte[] buffer  = new byte[buffersize];
        //A circular buffer
        CircularByteBuffer circBuffer = new CircularByteBuffer(sampleRate * bufferDuration);
        arecord.startRecording();
        //atrack.play();
        int i = 0;
        // start recording and playing back
        while(started) {
            try {
                //Write
                arecord.read(buffer, 0, buffersize);
                //Read the byte array data to the circular buffer
                circBuffer.getOutputStream().write(buffer, 0, buffersize);
                //Play the byte array content
                //atrack.write(buffer, 0, buffersize);
                if(i%2 == 0){ //TODO This should be an option
                    Message m = new Message();
                    m.obj  = buffer;
                    uiCallback.sendMessage(m);
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

            InputStream in = circBuffer.getInputStream();
            writer.setupHeader(arecord, circBuffer.getAvailable());

            while(in.read(buffer, 0, buffersize) != -1){
                writer.write(buffer, buffersize);
            }
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
            Log.i(LOG_TAG, "you fucked up" + e.toString());
        }

        arecord.stop();
        arecord.release();
        //atrack.stop();

        Log.i(LOG_TAG, "loopback exit");
    }
    public void close(){
        started = false;
        arecord.release();
    }
    private static Handler uiCallback = new Handler () {
        public void handleMessage (Message msg) {
            byte[] bytes = (byte[]) msg.obj;
            int[] samples = new int[bytes.length/2]; //bytes.length/2
            //Convert bytes into samples
            int sampleIndex = 0;
            for (int t = 0; t < bytes.length;) {
                int low = (int) bytes[t];
                t++;
                int high = (int) bytes[t];
                t++;
                int sample = getSixteenBitSample(high, low);
                samples[sampleIndex] = sample;
                //Log.i("SpeechJammer", "Got " + samples[sampleIndex]);
                //toReturn[sampleIndex] = (byte)low;
                sampleIndex++;
            }

            mCallBack.onBufferUpdate(samples);
        }
    };

    private static int getSixteenBitSample(int high, int low) {
        return (high << 8) + (low & 0x00ff);
    }
}
