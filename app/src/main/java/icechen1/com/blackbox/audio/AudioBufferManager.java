package icechen1.com.blackbox.audio;

import java.io.IOException;
import java.util.Arrays;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class AudioBufferManager extends Thread{
    public interface BufferCallBack {
        public void onBufferUpdate(int[] b);
    }
    static String LOG_TAG = "SpeechJammer";
    AudioRecord arecord;
    AudioTrack atrack;
    int SAMPLE_RATE;
    static int buffersize;
    private boolean started = true;
    private static BufferCallBack mCallBack;
    double delay;

    public AudioBufferManager(int time, BufferCallBack callback) {
        mCallBack = callback;
        delay=time;
        // Prepare the AudioRecord & AudioTrack
        buffersize = 3584;
        try {
            //Find the best supported sample rate
            for (int rate : new int[] {8000, 11025, 16000, 22050, 44100}) {  // add the rates you wish to check against
                int bufferSize = AudioRecord.getMinBufferSize(rate, AudioFormat.CHANNEL_IN_DEFAULT , AudioFormat.ENCODING_PCM_16BIT);
                if (bufferSize != AudioRecord.ERROR_BAD_VALUE) {
                    // buffer size is valid, Sample rate supported
                    SAMPLE_RATE = rate;
                    buffersize = bufferSize;
                    Log.i(LOG_TAG,"Recording sample rate:" + SAMPLE_RATE + " with buffer size:"+ buffersize);
                }
            }

            Log.i(LOG_TAG,"Final sample rate:" + SAMPLE_RATE + " with buffer size:"+ buffersize);

            Log.i(LOG_TAG,"Initializing Audio Record and Audio Playing objects");
            Log.i(LOG_TAG,"Delay time is: " + delay + " ms");

        } catch (Throwable t) {
            Log.e(LOG_TAG, "Initializing Audio Record and Play objects Failed "+t.getLocalizedMessage());
        }
        //Set up the recorder and the player
        arecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, buffersize * 1);
        int _audioTrackSize = android.media.AudioTrack.getMinBufferSize(
                SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        atrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, _audioTrackSize,
                AudioTrack.MODE_STREAM);
        atrack.setPlaybackRate(SAMPLE_RATE);
    }

    int getAudioSessionID(){
        return atrack.getAudioSessionId();
    }

    @Override
    public void run() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
        //AudioRecord recorder = null;

        //Create our buffers
        byte[] buffer  = new byte[buffersize];
        //A circular buffer
        CircularByteBuffer circBuffer = new CircularByteBuffer(SAMPLE_RATE*10);
        //Add an offset to the circular buffer
        int emptySamples = (int)(SAMPLE_RATE * (delay/1000)); //ms to secs

        if((emptySamples%2)==0){
            //Even number for emptySamples, do nothing
        }else{
            //BUG odd emptySamples value produce weird noise, so we add one to it.
            emptySamples += 1;
        }
        Log.i(LOG_TAG, "Empty Sample: "+emptySamples);

        byte[] emptyBuf = new byte[emptySamples];
        Arrays.fill(emptyBuf, (byte)Byte.MIN_VALUE );
        try {
            circBuffer.getOutputStream().write(emptyBuf, 0, emptySamples);
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        arecord.startRecording();
        atrack.play();
        int i = 0;
        // start recording and playing back
        while(started) {
            try {
                //Write
                arecord.read(buffer, 0, buffersize);
                //Read the byte array data to the circular buffer
                circBuffer.getOutputStream().write(buffer, 0, buffersize);
                //Read the beginning of the circular buffer to the normal byte array until one sample rate of content
                circBuffer.getInputStream().read(buffer, 0, buffersize);
                //Play the byte array content
                atrack.write(buffer, 0, buffersize);
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

        arecord.stop();
        arecord.release();
        atrack.stop();

        Log.i(LOG_TAG, "loopback exit");
        return;
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
