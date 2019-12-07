package br.com.ltoscano.droidplayer.app.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import br.com.ltoscano.droidplayer.app.log.AppLogger;
import br.com.ltoscano.droidplayer.network.service.NetworkService;

public class BootCompletedReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context ctx, Intent intent)
    {
        AppLogger.logDebug("BootCompletedReceiver.onReceive()");
        ctx.startService(new Intent(ctx, NetworkService.class));
    }
}
