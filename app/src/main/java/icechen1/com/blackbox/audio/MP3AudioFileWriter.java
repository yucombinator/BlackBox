package icechen1.com.blackbox.audio;

import android.media.AudioRecord;
import android.util.Log;

import com.naman14.androidlame.AndroidLame;
import com.naman14.androidlame.LameBuilder;

import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by YuChen on 2015-03-30.
 */
public class MP3AudioFileWriter extends AudioFileWriter {
    private AndroidLame mAndroidLame;
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
            byte[] bytebuffer = new byte[mBufferSize];
            Log.d("Rewind", "mBufferSize " + mBufferSize);
            while(!buffer.isEmpty()) {
                for(int i = 0; i < mBufferSize; i++) {
                    bytebuffer[i] = buffer.get();
                }
                ByteBuffer.wrap(bytebuffer).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(copybuffer);
                int bytesEncoded = mAndroidLame.encode(copybuffer, copybuffer, mBufferSize / 2, mp3buffer);
                if (bytesEncoded > 0) {
                    Log.d("BlackBox", "Encoded " + bytesEncoded);
                    os.write(mp3buffer, 0, bytesEncoded);
                }
            }

        }
        catch ( EOFException | OutOfMemoryError e )
        {
            // nothing to do.
            Log.e("Rewind", "Error encoding MP3", e);
            throw e;
        }
    }

    void setupHeader(AudioRecord rec, int length, int bufferSize) throws IOException {
        mBufferSize = bufferSize;
        mp3buffer = new byte[(int) (7200 + bufferSize * 2 * 1.25)];

        LameBuilder builder = new LameBuilder()
                .setId3tagArtist("Rewind")
                .setMode(LameBuilder.Mode.MONO)
                .setInSampleRate(rec.getSampleRate())
                .setOutChannels(1);

        mAndroidLame = builder.build(); //use this
    }

    void close() throws IOException {
        int outputMp3buf = mAndroidLame.flush(mp3buffer);

        if (outputMp3buf > 0) {
            Log.i("Rewind", "Flushed MP3");
            os.write(mp3buffer, 0, outputMp3buf);
        }

        mAndroidLame.close();
        os.flush();
        os.close();
        Log.i("Rewind", "Saved File at " + file.toURI());
    }
}
