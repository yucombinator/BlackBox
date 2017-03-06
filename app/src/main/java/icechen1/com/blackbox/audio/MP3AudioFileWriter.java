package icechen1.com.blackbox.audio;

import android.media.AudioRecord;
import android.util.Log;

import com.naman14.androidlame.AndroidLame;
import com.naman14.androidlame.LameBuilder;

import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by YuChen on 2015-03-30.
 */
public class MP3AudioFileWriter extends AudioFileWriter {
    private AndroidLame mAndroidLame;
    private boolean isStereo = false;
    private byte[] mp3buffer;
    private int mBufferSize;

    public MP3AudioFileWriter(String url, String path) throws FileNotFoundException {
        super(url, path);
    }

    @Override
    String getFileExtension() {
        return "mp3";
    }

    public String getPath(){
        return file.getPath();
    }

    void writeFromCircBuffer(CircularByteBuffer buffer) throws IOException {
        try
        {
            short[] copybuffer = new short[mBufferSize / 2];
            Log.d("BlackBox", "mBufferSize " + mBufferSize);
            while(!buffer.isEmpty()) {
                for(int i = 0; i < mBufferSize / 2; i++) {
                    copybuffer[i] = (short)( ((buffer.get()&0xFF)<<8) | (buffer.get()&0xFF) );
                }
                int bytesEncoded = mAndroidLame.encode(copybuffer, copybuffer, mBufferSize, mp3buffer);
                if(bytesEncoded > 0) {
                    Log.d("BlackBox", "Encoded " + bytesEncoded);
                    os.write(mp3buffer, 0, bytesEncoded);
                }
            }

        }
        catch ( EOFException e )
        {
            // nothing to do.
            Log.e("BlackBox", "Error encoding MP3", e);
        }
        catch ( OutOfMemoryError e )
        {
            // nothing to do.
            Log.e("BlackBox", "Error encoding MP3", e);
        }
    }

    void setupHeader(AudioRecord rec, int length, int bufferSize) throws IOException {
        int byteRate = RECORDER_BPP * rec.getSampleRate() * rec.getChannelCount()/8;
        isStereo = rec.getChannelCount() == 2;
        mBufferSize = bufferSize;
        mp3buffer = new byte[(int) (7200 + bufferSize * 2 * 1.25)];

        LameBuilder builder = new LameBuilder()
                .setMode(LameBuilder.Mode.MONO)
                .setInSampleRate(rec.getSampleRate())
                .setOutChannels(1);
                //.setOutBitrate(byteRate)
                //.setOutSampleRate(rec.getSampleRate());
                /*
                .setMode(mode)
                .setQuality(quality)
                .setVbrMode(vbrMode)
                .setVbrQuality(vbrQuality)
                .setScaleInput(scaleInput)
                .setId3tagTitle(title)
                .setId3tagAlbum(album)
                .setId3tagArtist(artist)
                .setId3tagYear(year)
                .setId3tagComment(comment)
                .setLowpassFreqency(freq)
                .setHighpassFreqency(freq)
                .setAbrMeanBitrate(meanBitRate);
                */

        mAndroidLame = builder.build(); //use this
    }

    void close() throws IOException {
        int outputMp3buf = mAndroidLame.flush(mp3buffer);

        if (outputMp3buf > 0) {
            try {
                Log.i("BlackBox", "Flushed MP3");
                os.write(mp3buffer, 0, outputMp3buf);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        os.flush();
        os.close();
        Log.i("BlackBox", "Saved File at " + file.toURI());
    }

    private static final int RECORDER_BPP = 16;

}
