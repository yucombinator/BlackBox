package icechen1.com.blackbox.messages;

import icechen1.com.blackbox.provider.recording.RecordingContentValues;
import icechen1.com.blackbox.provider.recording.RecordingModel;

/**
 * Created by yuchen.hou on 15-07-12.
 */
public class RecordingSavedMessage {
    public RecordingContentValues obj;
    public RecordingSavedMessage(RecordingContentValues p){
        obj = p;
    }
}
