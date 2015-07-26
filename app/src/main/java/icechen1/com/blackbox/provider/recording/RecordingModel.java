package icechen1.com.blackbox.provider.recording;

import icechen1.com.blackbox.provider.base.BaseModel;

import java.util.Date;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * A recoding made in BlackBox.
 */
public interface RecordingModel extends BaseModel {

    /**
     * Name of the recording
     * Can be {@code null}.
     */
    @Nullable
    String getName();

    /**
     * Filename of the recording
     * Can be {@code null}.
     */
    @Nullable
    String getFilename();

    /**
     * Duration of the recording
     */
    long getDuration();

    /**
     * Timestamp of the recording
     */
    long getTimestamp();

    /**
     * Is Favorite?
     */
    Boolean getFavorite();
}
