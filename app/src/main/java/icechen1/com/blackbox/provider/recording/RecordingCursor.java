package icechen1.com.blackbox.provider.recording;

import java.util.Date;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import icechen1.com.blackbox.provider.base.AbstractCursor;

/**
 * Cursor wrapper for the {@code recording} table.
 */
public class RecordingCursor extends AbstractCursor implements RecordingModel {
    public RecordingCursor(Cursor cursor) {
        super(cursor);
    }

    /**
     * Primary key.
     */
    public long getId() {
        Long res = getLongOrNull(RecordingColumns._ID);
        if (res == null)
            throw new NullPointerException("The value of '_id' in the database was null, which is not allowed according to the model definition");
        return res;
    }

    /**
     * Name of the recording
     * Can be {@code null}.
     */
    @Nullable
    public String getName() {
        String res = getStringOrNull(RecordingColumns.NAME);
        return res;
    }

    /**
     * Filename of the recording
     * Can be {@code null}.
     */
    @Nullable
    public String getFilename() {
        String res = getStringOrNull(RecordingColumns.FILENAME);
        return res;
    }

    /**
     * Duration of the recording
     */
    public long getDuration() {
        Long res = getLongOrNull(RecordingColumns.DURATION);
        if (res == null)
            throw new NullPointerException("The value of 'duration' in the database was null, which is not allowed according to the model definition");
        return res;
    }

    /**
     * Timestamp of the recording
     */
    public long getTimestamp() {
        Long res = getLongOrNull(RecordingColumns.TIMESTAMP);
        if (res == null)
            throw new NullPointerException("The value of 'timestamp' in the database was null, which is not allowed according to the model definition");
        return res;
    }
}
