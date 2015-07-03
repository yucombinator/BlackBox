package icechen1.com.blackbox.provider.recording;

import android.net.Uri;
import android.provider.BaseColumns;

import icechen1.com.blackbox.provider.BlackBoxProvider;
import icechen1.com.blackbox.provider.recording.RecordingColumns;

/**
 * A recoding made in BlackBox.
 */
public class RecordingColumns implements BaseColumns {
    public static final String TABLE_NAME = "recording";
    public static final Uri CONTENT_URI = Uri.parse(BlackBoxProvider.CONTENT_URI_BASE + "/" + TABLE_NAME);

    /**
     * Primary key.
     */
    public static final String _ID = BaseColumns._ID;

    /**
     * Name of the recording
     */
    public static final String NAME = "name";

    /**
     * Filename of the recording
     */
    public static final String FILENAME = "filename";

    /**
     * Duration of the recording
     */
    public static final String DURATION = "duration";

    /**
     * Timestamp of the recording
     */
    public static final String TIMESTAMP = "timestamp";


    public static final String DEFAULT_ORDER = TABLE_NAME + "." +_ID;

    // @formatter:off
    public static final String[] ALL_COLUMNS = new String[] {
            _ID,
            NAME,
            FILENAME,
            DURATION,
            TIMESTAMP
    };
    // @formatter:on

    public static boolean hasColumns(String[] projection) {
        if (projection == null) return true;
        for (String c : projection) {
            if (c.equals(NAME) || c.contains("." + NAME)) return true;
            if (c.equals(FILENAME) || c.contains("." + FILENAME)) return true;
            if (c.equals(DURATION) || c.contains("." + DURATION)) return true;
            if (c.equals(TIMESTAMP) || c.contains("." + TIMESTAMP)) return true;
        }
        return false;
    }

}
