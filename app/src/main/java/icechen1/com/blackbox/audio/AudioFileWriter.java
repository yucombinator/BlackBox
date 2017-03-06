package icechen1.com.blackbox.audio;

import android.media.AudioRecord;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

/**
 * Created by yuchen on 2017-03-06.
 */

public abstract class AudioFileWriter {
    protected final File file;
    protected final FileOutputStream os;
    protected final BufferedOutputStream bos;
    protected final DataOutputStream dos;

    protected AudioFileWriter(String url, String path) throws FileNotFoundException {
        Date date = new Date();
        if(url == null){
            url = String.valueOf(date.getTime());
        }
        File dir = new File(path);
        boolean result = dir.mkdirs(); //create folders where write files

        if(!dir.isDirectory()){
            // Try again using defaults
            path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Rewind/";
            dir = new File(path);
            result = dir.mkdirs();
        }

        file = new File(dir, url+"."+getFileExtension());
        os = new FileOutputStream(file);
        bos = new BufferedOutputStream(os);
        dos = new DataOutputStream(bos);
    }

    abstract  String getFileExtension();

    public String getPath(){
        return file.getPath();
    }

    abstract void writeFromCircBuffer(CircularByteBuffer buffer) throws IOException;

    abstract void setupHeader(AudioRecord rec, int length, int bufferSize) throws IOException;

    abstract void close() throws IOException;
}
