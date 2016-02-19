package icechen1.com.blackbox;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import icechen1.com.blackbox.services.AudioRecordService;

/**
 * Implementation of App Widget functionality.
 */
public class RewindWidget extends AppWidgetProvider {

    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId, boolean started) {

        CharSequence widgetText;
        Intent intent = new Intent(context, AudioRecordService.class);

        if (started){
            widgetText = context.getString(R.string.appwidget_stop);
            intent.putExtra("mode", AudioRecordService.MODE_STOP);
        } else {
            widgetText = context.getString(R.string.appwidget_start);
            intent.putExtra("mode", AudioRecordService.MODE_START);
        }

        PendingIntent pIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.rewind_widget);
        views.setTextViewText(R.id.appwidget_text, widgetText);
        views.setOnClickPendingIntent(R.id.appwidget, pIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, false); //Default to false
        }
    }

    @Override
    public void onEnabled(Context context) {
    }

    @Override
    public void onDisabled(Context context) {
    }
}

