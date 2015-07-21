package icechen1.com.blackbox.messages;

import android.content.Context;
import android.net.Uri;

/**
 * Created by yuchen.hou on 15-07-11.
 */
public class AudioBufferMessage {
    public byte[] bytes;
    public AudioBufferMessage(byte[] b){
        bytes = b;
    }
}
