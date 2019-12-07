package br.com.ltoscano.droidplayer.network.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import br.com.ltoscano.droidplayer.app.log.AppLogger;
import br.com.ltoscano.droidplayer.network.nearby.NearbyConnectionsManager;

public class StartAdvertisingAndDiscoveryReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        AppLogger.logDebug("StartAdvertisingAndDiscoveryReceiver.onReceive()");

        NearbyConnectionsManager nearbyConnectionsManager = NearbyConnectionsManager.getInstance();
        nearbyConnectionsManager.startAdvertisingAndDiscovery();
        nearbyConnectionsManager.stopAdvertisingAndDiscoveryScheduled();
    }
}