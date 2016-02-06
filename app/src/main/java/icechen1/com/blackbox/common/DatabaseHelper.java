package icechen1.com.blackbox.common;

import android.content.Context;

import icechen1.com.blackbox.provider.recording.RecordingColumns;
import icechen1.com.blackbox.provider.recording.RecordingContentValues;

/**
 * Created by yuchen.hou on 15-07-02.
 */
public class DatabaseHelper {
    public static RecordingContentValues saveRecording(Context cxt, String name, String filename, long duration, long timestamp){
        RecordingContentValues values = new RecordingContentValues();
        values.putName(name).putFilename(filename).putDuration(duration).putTimestamp(timestamp);
        cxt.getContentResolver().insert(RecordingColumns.CONTENT_URI, values.values());
        return values;
    }

    public static void editFavoriteforId(Context cxt, long id, boolean state){
        RecordingContentValues values = new RecordingContentValues();
        values.putFavorite(state);
        cxt.getContentResolver().update(RecordingColumns.CONTENT_URI, values.values(), RecordingColumns._ID + " = " + id, null);
    }

    public static void editTitleforId(Context cxt, long id, String name){
        RecordingContentValues values = new RecordingContentValues();
        values.putName(name);
        cxt.getContentResolver().update(RecordingColumns.CONTENT_URI, values.values(), RecordingColumns._ID + " = " + id, null);
    }

    public static void deleteForId(Context cxt, long id){
        cxt.getContentResolver().delete(RecordingColumns.CONTENT_URI, RecordingColumns._ID + " = " + id, null);
    }
}
