package br.com.ltoscano.droidplayer.app.helper.wifi;

import android.content.Context;
import android.net.wifi.WifiManager;

public class WifiHelper
{
    private static WifiManager wifiManager;

    private WifiHelper()
    {

    }

    private static WifiManager getWifiManagerInstance(Context ctx)
    {
        if(wifiManager == null)
        {
            wifiManager = (WifiManager)ctx.getSystemService(Context.WIFI_SERVICE);
        }

        return wifiManager;
    }

    public static void enableWifi(Context ctx)
    {
        WifiManager manager = getWifiManagerInstance(ctx);

        if(!manager.isWifiEnabled())
        {
            manager.setWifiEnabled(true);
        }
    }

    public static void disableWifi(Context ctx)
    {
        WifiManager manager = getWifiManagerInstance(ctx);

        if(manager.isWifiEnabled())
        {
            manager.setWifiEnabled(false);
        }
    }
}
