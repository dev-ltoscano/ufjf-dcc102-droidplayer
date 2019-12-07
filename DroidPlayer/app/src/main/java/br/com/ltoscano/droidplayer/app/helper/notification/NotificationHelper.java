package br.com.ltoscano.droidplayer.app.helper.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;

import java.util.List;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationHelper
{
    private static NotificationManagerCompat notificationManager;

    private NotificationHelper()
    {

    }

    private static void initializeNotificationManager(Context ctx)
    {
        if(notificationManager == null)
        {
            notificationManager = NotificationManagerCompat.from(ctx);
        }
    }

    public static void createNotificationChannel(Context ctx, String id, String name, String description, int importance)
    {
        initializeNotificationManager(ctx);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationChannel notificationChannel = new NotificationChannel(id, name, importance);
            notificationChannel.setDescription(description);

            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    public static void deleteNotificationChannel(Context ctx, String id)
    {
        initializeNotificationManager(ctx);
        notificationManager.deleteNotificationChannel(id);
    }

    public static Notification createNotification(Context ctx,
                                                  String channelId,
                                                  int icon,
                                                  String title,
                                                  String text,
                                                  PendingIntent intent,
                                                  List<NotificationAction> actionList,
                                                  int priority,
                                                  int visibility,
                                                  boolean showWhen,
                                                  boolean autoCancel)
    {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(ctx, channelId)
                .setSmallIcon(icon)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(intent)
                .setAutoCancel(autoCancel)
                .setPriority(priority)
                .setVisibility(visibility)
                .setShowWhen(showWhen);

        if(actionList != null)
        {
            for(NotificationAction action : actionList)
            {
                notificationBuilder.addAction(action.getIcon(), action.getTitle(), action.getIntent());
            }
        }

        return notificationBuilder.build();
    }

    public static void showNotification(Context ctx, int id, Notification notification)
    {
        initializeNotificationManager(ctx);
        notificationManager.notify(id, notification);
    }
}
