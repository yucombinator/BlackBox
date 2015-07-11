package icechen1.com.blackbox.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import icechen1.com.blackbox.services.AudioRecordService;

/**
 * Created by yuchen.hou on 15-07-11.
 */
public class KickstartReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, AudioRecordService.class);
        i.putExtra("mode", AudioRecordService.MODE_SET_PASSIVE_NOTIF);
        context.startService(i);
    }
}
