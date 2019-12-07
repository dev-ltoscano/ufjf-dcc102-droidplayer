package br.com.ltoscano.droidplayer.app.helper.notification;

import android.app.PendingIntent;

public class NotificationAction
{
    private int icon;
    private String title;
    private PendingIntent intent;

    public NotificationAction(int icon, String title, PendingIntent intent)
    {
        this.icon = icon;
        this.title = title;
        this.intent = intent;
    }

    public int getIcon()
    {
        return icon;
    }

    public String getTitle()
    {
        return title;
    }

    public PendingIntent getIntent()
    {
        return intent;
    }
}
