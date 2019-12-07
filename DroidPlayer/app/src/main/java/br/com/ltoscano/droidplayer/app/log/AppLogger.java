package br.com.ltoscano.droidplayer.app.log;

import android.util.Log;

public final class AppLogger
{
    public static final String TAG = "DROIDPLAYER";

    private AppLogger() { }

    public static void logDebug(String msg)
    {
        Log.d(TAG, msg);
    }

    public static void logInfo(String msg)
    {
        Log.i(TAG, msg);
    }

    public static void logWarn(String msg)
    {
        Log.w(TAG, msg);
    }

    public static void logError(String msg, Throwable ex)
    {
        Log.e(TAG, msg, ex);
    }
}
