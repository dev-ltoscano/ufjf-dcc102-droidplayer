package br.com.ltoscano.droidplayer.network.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;

import com.google.android.gms.nearby.connection.Payload;

import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import br.com.ltoscano.droidplayer.R;
import br.com.ltoscano.droidplayer.network.event.dispatcher.MessageEventDispatcher;
import br.com.ltoscano.droidplayer.network.event.listener.IConnectionEventListener;
import br.com.ltoscano.droidplayer.network.event.listener.IDiscoveryEventListener;
import br.com.ltoscano.droidplayer.network.event.listener.IMessageEventListener;
import br.com.ltoscano.droidplayer.network.event.listener.IPayloadEventListener;
import br.com.ltoscano.droidplayer.app.exception.JSONException;
import br.com.ltoscano.droidplayer.app.exception.NotFoundException;
import br.com.ltoscano.droidplayer.app.exception.RoutingException;
import br.com.ltoscano.droidplayer.network.info.EndpointInfo;
import br.com.ltoscano.droidplayer.network.info.PayloadInfo;
import br.com.ltoscano.droidplayer.network.info.PayloadTransferInfo;
import br.com.ltoscano.droidplayer.network.info.RoutingInfo;
import br.com.ltoscano.droidplayer.app.log.AppLogger;
import br.com.ltoscano.droidplayer.network.nearby.NearbyConnectionsManager;
import br.com.ltoscano.droidplayer.app.helper.notification.NotificationHelper;
import br.com.ltoscano.droidplayer.network.info.MessageInfo;
import br.com.ltoscano.droidplayer.network.routing.RoutingManager;
import br.com.ltoscano.droidplayer.ui.activity.MainActivity;

public class NetworkService extends Service implements IDiscoveryEventListener, IPayloadEventListener
{
    public class LocalBinder extends Binder
    {
        public NetworkService getService()
        {
            return NetworkService.this;
        }
    }

    private static NetworkService serviceInstance;

    private IBinder serviceBinder;
    private ServiceConnection serviceConnection;

    private NearbyConnectionsManager nearbyConnectionsManager;
    private MessageEventDispatcher messageEventDispatcher;

    public NetworkService()
    {
        this.serviceInstance = null;

        this.serviceBinder = new NetworkService.LocalBinder();
        this.serviceConnection = new ServiceConnection()
        {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service)
            {
                NetworkService.LocalBinder binder = (NetworkService.LocalBinder) service;
                serviceInstance = binder.getService();
            }

            @Override
            public void onServiceDisconnected(ComponentName name)
            {
                serviceInstance = null;
            }
        };

        this.nearbyConnectionsManager = NearbyConnectionsManager.getInstance();
        this.messageEventDispatcher = new MessageEventDispatcher();
    }

    private void initialize()
    {
        nearbyConnectionsManager.initialize(getApplicationContext());
        nearbyConnectionsManager.registerDiscoveryEventListener(this);
        nearbyConnectionsManager.registerPayloadEventListener(this);
        nearbyConnectionsManager.startAdvertisingAndDiscovery();

        NotificationHelper.createNotificationChannel(
                this,
                getString(R.string.network_service_notification_channel_id),
                getString(R.string.network_service_notification_channel_name),
                getString(R.string.network_service_notification_text),
                NotificationManager.IMPORTANCE_LOW);

        Intent serviceNotificationIntent = new Intent(getApplicationContext(), MainActivity.class);
        serviceNotificationIntent.putExtra("openInDevices", true);

        PendingIntent serviceNotificationPendingIntent =
                PendingIntent.getActivity(this, 0, serviceNotificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification serviceNotification = NotificationHelper.createNotification(this,
                getString(R.string.network_service_notification_channel_id),
                R.drawable.network,
                getString(R.string.network_service_notification_title),
                getString(R.string.network_service_notification_text),
                serviceNotificationPendingIntent,
                null,
                NotificationCompat.PRIORITY_DEFAULT,
                NotificationCompat.VISIBILITY_PUBLIC,
                false,
                false);

        startForeground(R.integer.network_service_notification_id, serviceNotification);

        bindService(new Intent(getApplicationContext(), NetworkService.class), serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void uninitialize()
    {
        nearbyConnectionsManager.unregisterPayloadEventListener(this);
        nearbyConnectionsManager.unitialize();

        stopForeground(true);
    }

    public static NetworkService getInstance()
    {
        return serviceInstance;
    }

    public NearbyConnectionsManager getNearbyConnectionsManager()
    {
        return nearbyConnectionsManager;
    }

    public String getLocalEndpointName()
    {
        return nearbyConnectionsManager.getLocalEndpointName();
    }

    public Collection<EndpointInfo> getEndpointInfoList()
    {
        return nearbyConnectionsManager.getEndpointInfoList();
    }

    public Map<String, RoutingInfo> getRoutingInfoMap()
    {
        return nearbyConnectionsManager.getRoutingInfoMap();
    }

    public void registerDiscoveryEventListener(IDiscoveryEventListener eventListener)
    {
        nearbyConnectionsManager.registerDiscoveryEventListener(eventListener);
    }

    public void unregisterDiscoveryEventListener(IDiscoveryEventListener eventListener)
    {
        nearbyConnectionsManager.unregisterDiscoveryEventListener(eventListener);
    }

    public void registerConnectionEventListener(IConnectionEventListener eventListener)
    {
        nearbyConnectionsManager.registerConnectionEventListener(eventListener);
    }

    public void unregisterConnectionEventListener(IConnectionEventListener eventListener)
    {
        nearbyConnectionsManager.unregisterConnectionEventListener(eventListener);
    }

    public void registerNetworkMessageEventListener(IMessageEventListener eventListener)
    {
        messageEventDispatcher.registerEventListener(eventListener);
    }

    public void unregisterNetworkMessageEventListener(IMessageEventListener eventListener)
    {
        messageEventDispatcher.unregisterEventListener(eventListener);
    }

    public void registerPayloadEventListener(IPayloadEventListener eventListener)
    {
        nearbyConnectionsManager.registerPayloadEventListener(eventListener);
    }

    public void unregisterPayloadEventListener(IPayloadEventListener eventListener)
    {
        nearbyConnectionsManager.unregisterPayloadEventListener(eventListener);
    }

    public void sendMessage(MessageInfo messageInfo) throws RoutingException
    {
        RoutingManager.sendMessage(getRoutingInfoMap(), getEndpointInfoList(), messageInfo);
    }

    public void sendPayload(String remoteEndpointId, Payload payload) throws FileNotFoundException, RoutingException
    {
        nearbyConnectionsManager.sendPayload(remoteEndpointId, payload);
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        initialize();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        return START_STICKY;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        uninitialize();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return serviceBinder;
    }

    @Override
    public void onEndpointFound(EndpointInfo endpointInfo)
    {
        try
        {
            RoutingManager.sendRoutingInfo(nearbyConnectionsManager.getRoutingInfoMap(), nearbyConnectionsManager.getEndpointInfoList());
        }
        catch (RoutingException ex)
        {
            AppLogger.logError("Unable to sendPayload routing table", ex);
        }
    }

    @Override
    public void onEndpointLost(String endpointId)
    {
        try
        {
            RoutingManager.sendRoutingInfo(nearbyConnectionsManager.getRoutingInfoMap(), nearbyConnectionsManager.getEndpointInfoList());
        }
        catch (RoutingException ex)
        {
            AppLogger.logError("Unable to sendPayload routing table", ex);
        }
    }

    @Override
    public void onPayloadReceived(PayloadInfo payloadInfo)
    {
        AppLogger.logDebug("NetworkService.onPayloadReceived()");

        switch (payloadInfo.getPayloadType())
        {
            case BYTE:
            {
                try
                {
                    MessageInfo messageInfo = MessageInfo.fromJson(new String(payloadInfo.getPayloadBytes(), StandardCharsets.UTF_8));

                    if(messageInfo.getMessageType().equalsIgnoreCase("ROUTING_INFO"))
                    {
                        AppLogger.logInfo("New routing message received");

                        try
                        {
                            RoutingManager.updateRoutingInfo(getRoutingInfoMap(), getEndpointInfoList(), messageInfo);
                        }
                        catch (RoutingException ex)
                        {
                            AppLogger.logError(ex.getMessage(), ex);
                        }
                    }
                    else
                    {
                        try
                        {
                            String toEndpointName = messageInfo.getParam("TO_ENDPOINT_NAME").toString();

                            if(toEndpointName.equalsIgnoreCase(nearbyConnectionsManager.getLocalEndpointName()))
                            {
                                AppLogger.logInfo("New message received");

                                messageEventDispatcher.dispatch(
                                        MessageEventDispatcher.NETWORK_MESSAGE_RECEIVED,
                                        messageInfo);
                            }
                            else
                            {
                                try
                                {
                                    AppLogger.logInfo("Forwarding message");
                                    RoutingManager.forwardMessage(getRoutingInfoMap(), getEndpointInfoList(), messageInfo);
                                }
                                catch (RoutingException ex)
                                {
                                    AppLogger.logError(ex.getMessage(), ex);
                                }
                            }
                        }
                        catch (NotFoundException ex)
                        {
                            AppLogger.logError(ex.getMessage(), ex);
                        }
                    }
                }
                catch (JSONException ex)
                {
                    AppLogger.logError(ex.getMessage(), ex);
                }
                break;
            }
            default:
                AppLogger.logDebug("NetworkService.onPayloadReceived()");
        }
    }

    @Override
    public void onPayloadTransferUpdate(PayloadTransferInfo payloadTransferInfo)
    {
        AppLogger.logDebug("NetworkService.onPayloadTransferUpdate()");
    }

    @Override
    public void onPayloadTransferSuccess(PayloadTransferInfo payloadTransferInfo)
    {
        AppLogger.logDebug("NetworkService.onPayloadTransferSuccess()");
    }

    @Override
    public void onPayloadTransferFailure(PayloadTransferInfo payloadTransferInfo)
    {
        AppLogger.logDebug("NetworkService.onPayloadTransferFailure()");
    }
}
