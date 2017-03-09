package icechen1.com.blackbox.audio;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Pair;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import icechen1.com.blackbox.R;
import icechen1.com.blackbox.common.AppUtils;
import icechen1.com.blackbox.common.DatabaseHelper;
import icechen1.com.blackbox.messages.AudioBufferMessage;
import icechen1.com.blackbox.messages.DatabaseUpdatedMessage;
import icechen1.com.blackbox.messages.RecordStatusMessage;
import icechen1.com.blackbox.messages.RecordingSavedMessage;
import icechen1.com.blackbox.provider.recording.RecordingContentValues;

public class AudioBufferManager extends Thread {
    private final Context mContext;

    static String LOG_TAG = "BlackBox";
    private final OnAudioRecordStateUpdate mCallback;
    AudioRecord arecord;
    //static int buffersize;
    private boolean started = true;
    int mBufferDuration;
    private boolean mAllocationSuccess = false;
    private CircularByteBuffer mCircularByteBuffer;
    private Pair<Integer, Integer> mBufferSpec;
    private boolean isSaving = false;

    public interface OnAudioRecordStateUpdate{
        void onRecordingSaved();
        void onRecordingError(String s, Exception e);
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
    }

    // Since some devices run on limited memory, we want to try and create a buffer big enough and fits the best quality audio
    // If we do not have enough memory then we want to gracefully fall to a lower quality as much as possible.
    public void tryCreateBestBuffer() {
        int[] sampleRates = new int[] {8000, 11025, 16000, 22050, 44100};

        ArrayList<Pair<Integer, Integer>> bufferSizes = new ArrayList<>();
        //Find the best supported sample rate given memory constraints
        for (int rate : sampleRates) {
            //TODO Stereo support requires changing some buffer sizes
            int bufferSize = AudioRecord.getMinBufferSize(rate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
            if (bufferSize != AudioRecord.ERROR_BAD_VALUE) {
                // buffer size is valid, Sample rate supported
                bufferSizes.add(new Pair<>(rate, bufferSize));
            }
        }

        for (int i = bufferSizes.size() - 1; i >= 0; i--) {
            try {
                CircularByteBuffer circbuffer = tryAllocateCircularBuffer(bufferSizes.get(i).first);
                if(circbuffer != null) {
                    Log.i(LOG_TAG,"Final sample rate: " + bufferSizes.get(i).first + " with buffer size:"+ bufferSizes.get(i).second);
                    Log.i(LOG_TAG,"Length of time is: " + mBufferDuration + " s");
                    mAllocationSuccess = true;
                    mCircularByteBuffer = circbuffer;
                    mBufferSpec = bufferSizes.get(i);
                    return;
                }
            } catch (OutOfMemoryError err) {
                Log.e(LOG_TAG,"Failed to allocate for sample rate:" + bufferSizes.get(i).first + " with buffer size: "+ bufferSizes.get(i).second, err);
            }
        }
        mAllocationSuccess = false;
    }

    public CircularByteBuffer tryAllocateCircularBuffer(int sampleRate){
        int circBufferSize = sampleRate * mBufferDuration * 2; //2 is a magic number
        return new CircularByteBuffer(circBufferSize);
    }

    public int getBufferSize(){
        return mBufferSpec.second;
    }

    public int getSampleRate(){
        return mBufferSpec.first;
    }

    @Override
    public void run() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
        // Prepare the AudioRecord & AudioTrack
        tryCreateBestBuffer();

        if (!mAllocationSuccess) {
            //Failed to allocate memory...
            Log.e(LOG_TAG, "tryCreateBestBuffer error");
            if(mCallback != null){
                mCallback.onRecordingError(mContext.getString(R.string.mem_error), null);
            }
            return;
        }
        int bufferSize = mBufferSpec.second;
        int sampleRate = mBufferSpec.first;

        //Set up the recorder
        arecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                sampleRate, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, bufferSize * 2);

        //Create our buffers
        CircularByteBuffer circBuffer = mCircularByteBuffer;
        byte[] buffer = new byte[bufferSize];

        arecord.startRecording();

        //Check if we acquired the mic
        if(arecord.getState() != AudioRecord.STATE_INITIALIZED){
            Log.e(LOG_TAG, "AudioRecord error");
            if(mCallback != null){
                mCallback.onRecordingError(mContext.getString(R.string.mic_error), null);
            }
            return;
        }

        EventBus.getDefault().post(new RecordStatusMessage(RecordStatusMessage.JUST_STARTED, bufferSize, sampleRate));

        int i = 0;
        //get timestamp
        long startedTime = System.currentTimeMillis();
        // start recording and playing back
        while(started && arecord.getState() == AudioRecord.STATE_INITIALIZED) {
            try {
                //Write
                int bytesread = arecord.read(buffer, 0, bufferSize);
                //Read the byte array data to the circular buffer
                int inserted = circBuffer.put(buffer, 0, bufferSize);
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

        // Done recording, saving data
        if(isSaving) {
            try {
                SharedPreferences getPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
                String path = getPrefs.getString("path", "");
                if (path.equals("")) {
                    // Default path
                    path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Rewind/";
                }

                AudioFileWriter writer;
                if (AppUtils.isMp3Enabled(mContext)) {
                    writer = new MP3AudioFileWriter(null, path);
                } else {
                    writer = new WAVAudioFileWriter(null, path);
                }

                writer.setupHeader(arecord, circBuffer.length(), mBufferSpec.second);

                arecord.stop();
                arecord.release();

                Log.d(LOG_TAG, "buffer length " + circBuffer.length());
                writer.writeFromCircBuffer(circBuffer);
                writer.close();

                long currentMillis = System.currentTimeMillis();
                long actualTime = AppUtils.getBufferSavedTime(startedTime, currentMillis, mBufferDuration);
                //save entry to the database
                RecordingContentValues saved = DatabaseHelper.saveRecording(
                        mContext,
                        mContext.getResources().getString(R.string.recorded_on, new SimpleDateFormat("dd MMM").format(new Date(currentMillis))),
                        writer.getPath(),
                        actualTime,
                        currentMillis);
                EventBus.getDefault().post(new RecordingSavedMessage(saved));
                EventBus.getDefault().post(new DatabaseUpdatedMessage());

            } catch (IOException e) {
                Log.e(LOG_TAG, "I/O error", e);
                if (mCallback != null) {
                    mCallback.onRecordingError(mContext.getString(R.string.io_error), e);
                }
            } catch (Exception e) {
                Log.e(LOG_TAG, "Error", e);
                if (mCallback != null) {
                    mCallback.onRecordingError(mContext.getString(R.string.unknown_error), e);
                }
            }
            if(mCallback != null){
                mCallback.onRecordingSaved();
            }
        } else {
            arecord.stop();
            arecord.release();
        }
    }
    public void close(boolean toSave) {
        started = false;
        isSaving = toSave;
    }


}
