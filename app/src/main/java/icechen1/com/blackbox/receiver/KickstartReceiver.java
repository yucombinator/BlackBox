package icechen1.com.blackbox.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import icechen1.com.blackbox.services.AudioRecordService;

/**
 * Created by yuchen.hou on 15-07-11.
 */
public class KickstartReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, AudioRecordService.class);

        SharedPreferences getPrefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        boolean shouldStart = getPrefs.getBoolean("autostart_recording", false);
        if (shouldStart && intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            i.putExtra("mode", AudioRecordService.MODE_START);
        } else {
            i.putExtra("mode", AudioRecordService.MODE_SET_PASSIVE_NOTIF);
        }
        context.startService(i);
    }
}
