package br.com.ltoscano.droidplayer.network.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.Collection;

import androidx.annotation.NonNull;
import br.com.ltoscano.droidplayer.network.info.EndpointInfo;
import br.com.ltoscano.droidplayer.app.log.AppLogger;
import br.com.ltoscano.droidplayer.network.nearby.NearbyConnectionsManager;
import br.com.ltoscano.droidplayer.network.service.NetworkService;

public class ConnectionReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        AppLogger.logDebug("ConnectionReceiver.onReceive()");

        NetworkService networkService = NetworkService.getInstance();
        NearbyConnectionsManager nearbyConnectionsManager = NearbyConnectionsManager.getInstance();

        Collection<EndpointInfo> endpointInfoList = networkService.getEndpointInfoList();

        for(final EndpointInfo remoteEndpointInfo : endpointInfoList)
        {
            if(remoteEndpointInfo.getEndpointConnectionInfo().isConnected())
            {
                AppLogger.logInfo("Conexão ativa com " + remoteEndpointInfo.getEndpointId());
            }
            else
            {
                int remoteEndpointId = Integer.valueOf(remoteEndpointInfo.getEndpointName());
                int localEndpointId = Integer.valueOf(networkService.getLocalEndpointName());

                if(localEndpointId > remoteEndpointId)
                {
                    nearbyConnectionsManager.requestConnection(
                            remoteEndpointInfo.getEndpointId(),
                            new OnSuccessListener<Void>()
                            {
                                @Override
                                public void onSuccess(Void aVoid)
                                {
                                    AppLogger.logInfo("Conexão feita com " + remoteEndpointInfo.getEndpointId());
                                }
                            },
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception ex)
                                {
                                    AppLogger.logError("Falha na conexão com " + remoteEndpointInfo.getEndpointId(), ex);
                                }
                            });
                }
            }
        }
    }
}
