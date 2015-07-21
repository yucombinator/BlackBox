package icechen1.com.blackbox.provider;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.DefaultDatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;

import icechen1.com.blackbox.BuildConfig;
import icechen1.com.blackbox.provider.recording.RecordingColumns;

public class BlackBoxSQLiteOpenHelper extends SQLiteOpenHelper {
    private static final String TAG = BlackBoxSQLiteOpenHelper.class.getSimpleName();

    public static final String DATABASE_FILE_NAME = "blackbox.db";
    private static final int DATABASE_VERSION = 1;
    private static BlackBoxSQLiteOpenHelper sInstance;
    private final Context mContext;
    private final BlackBoxSQLiteOpenHelperCallbacks mOpenHelperCallbacks;

    // @formatter:off
    public static final String SQL_CREATE_TABLE_RECORDING = "CREATE TABLE IF NOT EXISTS "
            + RecordingColumns.TABLE_NAME + " ( "
            + RecordingColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + RecordingColumns.NAME + " TEXT DEFAULT 'NoName', "
            + RecordingColumns.FILENAME + " TEXT DEFAULT 'NoFileName', "
            + RecordingColumns.DURATION + " INTEGER NOT NULL DEFAULT 0, "
            + RecordingColumns.TIMESTAMP + " INTEGER NOT NULL DEFAULT 0 "
 //           + ", CONSTRAINT unique_name UNIQUE timestamp ON CONFLICT REPLACE"
            + " );";

    // @formatter:on

    public static BlackBoxSQLiteOpenHelper getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = newInstance(context.getApplicationContext());
        }
        return sInstance;
    }

    private static BlackBoxSQLiteOpenHelper newInstance(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            return newInstancePreHoneycomb(context);
        }
        return newInstancePostHoneycomb(context);
    }


    /*
     * Pre Honeycomb.
     */
    private static BlackBoxSQLiteOpenHelper newInstancePreHoneycomb(Context context) {
        return new BlackBoxSQLiteOpenHelper(context);
    }

    private BlackBoxSQLiteOpenHelper(Context context) {
        super(context, DATABASE_FILE_NAME, null, DATABASE_VERSION);
        mContext = context;
        mOpenHelperCallbacks = new BlackBoxSQLiteOpenHelperCallbacks();
    }


    /*
     * Post Honeycomb.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static BlackBoxSQLiteOpenHelper newInstancePostHoneycomb(Context context) {
        return new BlackBoxSQLiteOpenHelper(context, new DefaultDatabaseErrorHandler());
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private BlackBoxSQLiteOpenHelper(Context context, DatabaseErrorHandler errorHandler) {
        super(context, DATABASE_FILE_NAME, null, DATABASE_VERSION, errorHandler);
        mContext = context;
        mOpenHelperCallbacks = new BlackBoxSQLiteOpenHelperCallbacks();
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreate");
        mOpenHelperCallbacks.onPreCreate(mContext, db);
        db.execSQL(SQL_CREATE_TABLE_RECORDING);
        mOpenHelperCallbacks.onPostCreate(mContext, db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            setForeignKeyConstraintsEnabled(db);
        }
        mOpenHelperCallbacks.onOpen(mContext, db);
    }

    private void setForeignKeyConstraintsEnabled(SQLiteDatabase db) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            setForeignKeyConstraintsEnabledPreJellyBean(db);
        } else {
            setForeignKeyConstraintsEnabledPostJellyBean(db);
        }
    }

    private void setForeignKeyConstraintsEnabledPreJellyBean(SQLiteDatabase db) {
        db.execSQL("PRAGMA foreign_keys=ON;");
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setForeignKeyConstraintsEnabledPostJellyBean(SQLiteDatabase db) {
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        mOpenHelperCallbacks.onUpgrade(mContext, db, oldVersion, newVersion);
    }
}
