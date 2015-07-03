package icechen1.com.blackbox.provider.recording;

import java.util.Date;

import android.content.ContentResolver;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import icechen1.com.blackbox.provider.base.AbstractContentValues;

/**
 * Content values wrapper for the {@code recording} table.
 */
public class RecordingContentValues extends AbstractContentValues {
    @Override
    public Uri uri() {
        return RecordingColumns.CONTENT_URI;
    }

    /**
     * Update row(s) using the values stored by this object and the given selection.
     *
     * @param contentResolver The content resolver to use.
     * @param where The selection to use (can be {@code null}).
     */
    public int update(ContentResolver contentResolver, @Nullable RecordingSelection where) {
        return contentResolver.update(uri(), values(), where == null ? null : where.sel(), where == null ? null : where.args());
    }

    /**
     * Name of the recording
     */
    public RecordingContentValues putName(@Nullable String value) {
        mContentValues.put(RecordingColumns.NAME, value);
        return this;
    }

    public RecordingContentValues putNameNull() {
        mContentValues.putNull(RecordingColumns.NAME);
        return this;
    }

    /**
     * Filename of the recording
     */
    public RecordingContentValues putFilename(@Nullable String value) {
        mContentValues.put(RecordingColumns.FILENAME, value);
        return this;
    }

    public RecordingContentValues putFilenameNull() {
        mContentValues.putNull(RecordingColumns.FILENAME);
        return this;
    }

    /**
     * Duration of the recording
     */
    public RecordingContentValues putDuration(long value) {
        mContentValues.put(RecordingColumns.DURATION, value);
        return this;
    }


    /**
     * Timestamp of the recording
     */
    public RecordingContentValues putTimestamp(long value) {
        mContentValues.put(RecordingColumns.TIMESTAMP, value);
        return this;
    }

}
