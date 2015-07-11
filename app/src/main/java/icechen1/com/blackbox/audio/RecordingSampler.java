package icechen1.com.blackbox.audio;

import android.media.AudioRecord;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import icechen1.com.blackbox.views.VisualizerView;

/**
 * Created by yuchen.hou on 15-07-11.
 */
public class RecordingSampler {
    private static final int RECORDING_SAMPLE_RATE = 44100;
    private boolean mIsRecording;
    private int mBufSize;
    private RecordingSampler.CalculateVolumeListener mVolumeListener;
    private List<VisualizerView> mVisualizerViews = new ArrayList();

    public RecordingSampler() {
        this.initAudioRecord();
    }

    public void link(VisualizerView visualizerView) {
        this.mVisualizerViews.add(visualizerView);
    }

    public void setVolumeListener(RecordingSampler.CalculateVolumeListener volumeListener) {
        this.mVolumeListener = volumeListener;
    }

    public boolean isRecording() {
        return this.mIsRecording;
    }

    private void initAudioRecord() {
        int bufferSize = AudioRecord.getMinBufferSize('걄', 16, 2);
        this.mBufSize = bufferSize;
    }

    public void startRecording() {
        this.mIsRecording = true;
    }

    public void stopRecording() {
        if(this.mVisualizerViews != null && !this.mVisualizerViews.isEmpty()) {
            for(int i = 0; i < this.mVisualizerViews.size(); ++i) {
                ((VisualizerView)this.mVisualizerViews.get(i)).receive(0);
            }
        }
    }

    public void process(byte[] buf) {
        int decibel = RecordingSampler.this.calculateDecibel(buf);
        if(RecordingSampler.this.mVisualizerViews != null && !RecordingSampler.this.mVisualizerViews.isEmpty()) {
            for(int i = 0; i < RecordingSampler.this.mVisualizerViews.size(); ++i) {
                ((VisualizerView)RecordingSampler.this.mVisualizerViews.get(i)).receive(decibel);
            }
        }

        if(RecordingSampler.this.mVolumeListener != null) {
            RecordingSampler.this.mVolumeListener.onCalculateVolume(decibel);
        }
    }

    private int calculateDecibel(byte[] buf) {
        int sum = 0;

        for(int i = 0; i < this.mBufSize; ++i) {
            sum += Math.abs(buf[i]);
        }

        return sum / this.mBufSize;
    }

    public void release() {
        this.stopRecording();
    }

    public interface CalculateVolumeListener {
        void onCalculateVolume(int var1);
    }
}
