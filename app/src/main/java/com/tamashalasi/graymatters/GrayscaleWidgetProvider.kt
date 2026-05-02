package com.tamashalasi.graymatters

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.Toast

class GrayscaleWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_TOGGLE) {
            val isEnabled = GrayscaleUtils.checkGrayscaleStatus(context)
            if (!isEnabled) {
                if (GrayscaleUtils.setGrayscale(context, true)) {
                    Toast.makeText(context, "Grayscale Enabled", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Permission Denied. Open app for info.", Toast.LENGTH_LONG).show()
                }
                // Update widgets
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val componentName = ComponentName(context, GrayscaleWidgetProvider::class.java)
                onUpdate(context, appWidgetManager, appWidgetManager.getAppWidgetIds(componentName))
            } else {
                // If already enabled, open the app to perform the challenge
                val launchIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(launchIntent)
            }
        }
    }

    companion object {
        private const val ACTION_TOGGLE = "com.tamashalasi.graymatters.ACTION_TOGGLE"

        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.grayscale_widget_layout)
            
            val intent = Intent(context, GrayscaleWidgetProvider::class.java).apply {
                action = ACTION_TOGGLE
            }
            
            val pendingIntent = PendingIntent.getBroadcast(
                context, 
                0, 
                intent, 
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            views.setOnClickPendingIntent(R.id.widget_button, pendingIntent)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
