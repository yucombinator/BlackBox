package icechen1.com.blackbox.provider.recording;

import java.util.Date;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

import icechen1.com.blackbox.provider.base.AbstractSelection;

/**
 * Selection for the {@code recording} table.
 */
public class RecordingSelection extends AbstractSelection<RecordingSelection> {
    @Override
    protected Uri baseUri() {
        return RecordingColumns.CONTENT_URI;
    }

    /**
     * Query the given content resolver using this selection.
     *
     * @param contentResolver The content resolver to query.
     * @param projection A list of which columns to return. Passing null will return all columns, which is inefficient.
     * @param sortOrder How to order the rows, formatted as an SQL ORDER BY clause (excluding the ORDER BY itself). Passing null will use the default sort
     *            order, which may be unordered.
     * @return A {@code RecordingCursor} object, which is positioned before the first entry, or null.
     */
    public RecordingCursor query(ContentResolver contentResolver, String[] projection, String sortOrder) {
        Cursor cursor = contentResolver.query(uri(), projection, sel(), args(), sortOrder);
        if (cursor == null) return null;
        return new RecordingCursor(cursor);
    }

    /**
     * Equivalent of calling {@code query(contentResolver, projection, null)}.
     */
    public RecordingCursor query(ContentResolver contentResolver, String[] projection) {
        return query(contentResolver, projection, null);
    }

    /**
     * Equivalent of calling {@code query(contentResolver, projection, null, null)}.
     */
    public RecordingCursor query(ContentResolver contentResolver) {
        return query(contentResolver, null, null);
    }


    public RecordingSelection id(long... value) {
        addEquals("recording." + RecordingColumns._ID, toObjectArray(value));
        return this;
    }

    public RecordingSelection name(String... value) {
        addEquals(RecordingColumns.NAME, value);
        return this;
    }

    public RecordingSelection nameNot(String... value) {
        addNotEquals(RecordingColumns.NAME, value);
        return this;
    }

    public RecordingSelection nameLike(String... value) {
        addLike(RecordingColumns.NAME, value);
        return this;
    }

    public RecordingSelection nameContains(String... value) {
        addContains(RecordingColumns.NAME, value);
        return this;
    }

    public RecordingSelection nameStartsWith(String... value) {
        addStartsWith(RecordingColumns.NAME, value);
        return this;
    }

    public RecordingSelection nameEndsWith(String... value) {
        addEndsWith(RecordingColumns.NAME, value);
        return this;
    }

    public RecordingSelection filename(String... value) {
        addEquals(RecordingColumns.FILENAME, value);
        return this;
    }

    public RecordingSelection filenameNot(String... value) {
        addNotEquals(RecordingColumns.FILENAME, value);
        return this;
    }

    public RecordingSelection filenameLike(String... value) {
        addLike(RecordingColumns.FILENAME, value);
        return this;
    }

    public RecordingSelection filenameContains(String... value) {
        addContains(RecordingColumns.FILENAME, value);
        return this;
    }

    public RecordingSelection filenameStartsWith(String... value) {
        addStartsWith(RecordingColumns.FILENAME, value);
        return this;
    }

    public RecordingSelection filenameEndsWith(String... value) {
        addEndsWith(RecordingColumns.FILENAME, value);
        return this;
    }

    public RecordingSelection duration(long... value) {
        addEquals(RecordingColumns.DURATION, toObjectArray(value));
        return this;
    }

    public RecordingSelection durationNot(long... value) {
        addNotEquals(RecordingColumns.DURATION, toObjectArray(value));
        return this;
    }

    public RecordingSelection durationGt(long value) {
        addGreaterThan(RecordingColumns.DURATION, value);
        return this;
    }

    public RecordingSelection durationGtEq(long value) {
        addGreaterThanOrEquals(RecordingColumns.DURATION, value);
        return this;
    }

    public RecordingSelection durationLt(long value) {
        addLessThan(RecordingColumns.DURATION, value);
        return this;
    }

    public RecordingSelection durationLtEq(long value) {
        addLessThanOrEquals(RecordingColumns.DURATION, value);
        return this;
    }

    public RecordingSelection timestamp(long... value) {
        addEquals(RecordingColumns.TIMESTAMP, toObjectArray(value));
        return this;
    }

    public RecordingSelection timestampNot(long... value) {
        addNotEquals(RecordingColumns.TIMESTAMP, toObjectArray(value));
        return this;
    }

    public RecordingSelection timestampGt(long value) {
        addGreaterThan(RecordingColumns.TIMESTAMP, value);
        return this;
    }

    public RecordingSelection timestampGtEq(long value) {
        addGreaterThanOrEquals(RecordingColumns.TIMESTAMP, value);
        return this;
    }

    public RecordingSelection timestampLt(long value) {
        addLessThan(RecordingColumns.TIMESTAMP, value);
        return this;
    }

    public RecordingSelection timestampLtEq(long value) {
        addLessThanOrEquals(RecordingColumns.TIMESTAMP, value);
        return this;
    }
}
