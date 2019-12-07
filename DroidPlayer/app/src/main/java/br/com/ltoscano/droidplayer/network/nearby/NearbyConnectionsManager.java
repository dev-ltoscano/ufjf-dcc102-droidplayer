package br.com.ltoscano.droidplayer.network.nearby;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import br.com.ltoscano.droidplayer.network.event.dispatcher.ConnectionEventDispatcher;
import br.com.ltoscano.droidplayer.network.event.dispatcher.DiscoveryEventDispatcher;
import br.com.ltoscano.droidplayer.network.event.dispatcher.PayloadEventDispatcher;
import br.com.ltoscano.droidplayer.network.event.listener.IConnectionEventListener;
import br.com.ltoscano.droidplayer.network.event.listener.IDiscoveryEventListener;
import br.com.ltoscano.droidplayer.network.event.listener.IPayloadEventListener;
import br.com.ltoscano.droidplayer.network.info.EndpointConnectionInfo;
import br.com.ltoscano.droidplayer.network.info.PayloadInfo;
import br.com.ltoscano.droidplayer.network.info.PayloadTransferInfo;
import br.com.ltoscano.droidplayer.network.info.EndpointInfo;
import br.com.ltoscano.droidplayer.network.info.RoutingInfo;
import br.com.ltoscano.droidplayer.app.log.AppLogger;
import br.com.ltoscano.droidplayer.network.helper.DeviceNameGenerator;
import br.com.ltoscano.droidplayer.network.receiver.StartAdvertisingAndDiscoveryReceiver;
import br.com.ltoscano.droidplayer.network.receiver.StopAdvertisingAndDiscoveryReceiver;

public class NearbyConnectionsManager
{
    private static NearbyConnectionsManager instance;

    private String localEndpointName;
    private String endpointServiceId;

    private Map<String, EndpointInfo> endpointInfoMap;
    private Map<String, RoutingInfo> routingInfoMap;

    private boolean initialized;
    private ConnectionsClient nearbyConnectionsClient;
    private EndpointDiscoveryCallback endpointDiscoveryCallback;
    private ConnectionLifecycleCallback connectionLifecycleCallback;
    private PayloadCallback payloadCallback;

    private AlarmManager alarmManager;
    private PendingIntent startAdvertisingAndDiscoveryPendingIntent;
    private PendingIntent stopAdvertisingAndDiscoveryPendingIntent;

    private boolean advertisingStarted;
    private boolean discoveryStarted;

    private DiscoveryEventDispatcher discoveryEventDispatcher;
    private ConnectionEventDispatcher connectionEventDispatcher;
    private PayloadEventDispatcher payloadEventDispatcher;

    private NearbyConnectionsManager()
    {
        this.instance = null;

        this.localEndpointName = DeviceNameGenerator.generate();
        this.endpointServiceId = "droidplayer";

        this.endpointInfoMap = new HashMap<>();
        this.routingInfoMap = new HashMap<>();

        this.initialized = false;
        this.nearbyConnectionsClient = null;

        this.endpointDiscoveryCallback =
                new EndpointDiscoveryCallback()
                {
                    @Override
                    public void onEndpointFound(String endpointId, DiscoveredEndpointInfo discoveredEndpointInfo)
                    {
                        AppLogger.logDebug("NearbyConnectionsManager.onEndpointFound()");

                        if(!endpointInfoMap.containsKey(endpointId))
                        {
                            EndpointInfo remoteEndpointInfo = new EndpointInfo(
                                    endpointId,
                                    discoveredEndpointInfo.getEndpointName(),
                                    discoveredEndpointInfo.getServiceId());

                            endpointInfoMap.put(remoteEndpointInfo.getEndpointId(), remoteEndpointInfo);

                            routingInfoMap.put(
                                    remoteEndpointInfo.getEndpointName(),
                                    new RoutingInfo(
                                            remoteEndpointInfo.getEndpointName(),
                                            localEndpointName,
                                            0));

                            discoveryEventDispatcher.dispatchAsync(
                                    DiscoveryEventDispatcher.ENDPOINT_FOUND,
                                    remoteEndpointInfo);
                        }
                    }

                    @Override
                    public void onEndpointLost(String endpointId)
                    {
                        AppLogger.logDebug("NearbyConnectionsManager.onEndpointLost()");

                        if(endpointInfoMap.containsKey(endpointId))
                        {
                            endpointInfoMap.remove(endpointId);

                            Iterator<RoutingInfo> routingInfoIterator = routingInfoMap.values().iterator();

                            while(routingInfoIterator.hasNext())
                            {
                                RoutingInfo routingInfo = routingInfoIterator.next();

                                if(routingInfo.getToEndpointName().equalsIgnoreCase(endpointId)
                                        || routingInfo.getForwardEndpointName().equalsIgnoreCase(endpointId))
                                {
                                    routingInfoIterator.remove();
                                }
                            }

                            discoveryEventDispatcher.dispatchAsync(
                                    DiscoveryEventDispatcher.ENDPOINT_LOST,
                                    endpointId);
                        }
                    }
                };

        this.connectionLifecycleCallback =
                new ConnectionLifecycleCallback()
                {
                    @Override
                    public void onConnectionInitiated(String endpointId, ConnectionInfo connectionInfo)
                    {
                        AppLogger.logDebug("NearbyConnectionsManager.onConnectionInstantiated()");

                        if(endpointInfoMap.containsKey(endpointId))
                        {
                            EndpointInfo remoteEndpointInfo = endpointInfoMap.get(endpointId);

                            remoteEndpointInfo.setAuthToken(connectionInfo.getAuthenticationToken());
                            remoteEndpointInfo.setIncomingConnection(connectionInfo.isIncomingConnection());

                            try
                            {
                                Thread.sleep(200);
                            }
                            catch (InterruptedException ex)
                            {
                                ex.printStackTrace();
                            }

                            acceptConnection(remoteEndpointInfo.getEndpointId());

                            connectionEventDispatcher.dispatchAsync(
                                    ConnectionEventDispatcher.CONNECTION_INSTANTIATED,
                                    remoteEndpointInfo.getEndpointConnectionInfo());
                        }
                    }

                    @Override
                    public void onConnectionResult(String endpointId, ConnectionResolution connectionResolution)
                    {
                        AppLogger.logDebug("NearbyConnectionsManager.onConnectionResult()");

                        if(connectionResolution.getStatus().isSuccess())
                        {
                            AppLogger.logInfo("Connection established with " + endpointId);

                            if(endpointInfoMap.containsKey(endpointId))
                            {
                                EndpointInfo remoteEndpointInfo = endpointInfoMap.get(endpointId);

                                remoteEndpointInfo.getEndpointConnectionInfo().setConnected(true);

                                connectionEventDispatcher.dispatchAsync(
                                        ConnectionEventDispatcher.CONNECTION_RESULT,
                                        remoteEndpointInfo.getEndpointConnectionInfo());
                            }
                        }
                        else
                        {
                            AppLogger.logInfo("Could not connect to " + endpointId);

                            if(endpointInfoMap.containsKey(endpointId))
                            {
                                EndpointInfo remoteEndpointInfo = endpointInfoMap.get(endpointId);

                                remoteEndpointInfo.getEndpointConnectionInfo().setConnected(false);

                                connectionEventDispatcher.dispatchAsync(
                                        ConnectionEventDispatcher.CONNECTION_RESULT,
                                        remoteEndpointInfo.getEndpointConnectionInfo());
                            }
                        }
                    }

                    @Override
                    public void onDisconnected(String endpointId)
                    {
                        AppLogger.logDebug("NearbyConnectionsManager.onDisconnected()");

                        if(endpointInfoMap.containsKey(endpointId))
                        {
                            AppLogger.logInfo("Disconnected from " + endpointId);

                            EndpointInfo remoteEndpointInfo = endpointInfoMap.get(endpointId);

                            remoteEndpointInfo.getEndpointConnectionInfo().setConnected(false);

                            connectionEventDispatcher.dispatch(
                                    ConnectionEventDispatcher.DISCONNECTED,
                                    remoteEndpointInfo.getEndpointConnectionInfo());
                        }
                    }
                };

        this.payloadCallback =
                new PayloadCallback()
                {
                    @Override
                    public void onPayloadReceived(String endpointId, Payload payload)
                    {
                        AppLogger.logDebug("NearbyConnectionsManager.onDisconnected()");

                        Object objPayload = null;

                        switch (payload.getType())
                        {
                            case Payload.Type.BYTES:
                            {
                                objPayload = payload.asBytes();
                                break;
                            }
                            case Payload.Type.FILE:
                            {
                                objPayload = payload.asFile();
                                break;
                            }
                            case Payload.Type.STREAM:
                            {
                                objPayload = payload.asStream();
                                break;
                            }
                        }

                        payloadEventDispatcher.dispatchAsync(
                                PayloadEventDispatcher.PAYLOAD_RECEIVED,
                                new PayloadInfo(
                                        endpointId,
                                        payload.getId(),
                                        payload.getType(),
                                        objPayload));
                    }

                    @Override
                    public void onPayloadTransferUpdate(String endpointId, PayloadTransferUpdate payloadTransferUpdate)
                    {
                        AppLogger.logDebug("NearbyConnectionsManager.onDisconnected()");

                        if (payloadTransferUpdate.getStatus() == PayloadTransferUpdate.Status.SUCCESS)
                        {
                            payloadEventDispatcher.dispatchAsync(
                                    PayloadEventDispatcher.PAYLOAD_TRANSFER_SUCCESS,
                                    new PayloadTransferInfo(
                                            payloadTransferUpdate.getPayloadId(),
                                            payloadTransferUpdate.getTotalBytes(),
                                            payloadTransferUpdate.getBytesTransferred(),
                                            payloadTransferUpdate.getStatus()));
                        }
                        else if ((payloadTransferUpdate.getStatus() == PayloadTransferUpdate.Status.FAILURE)
                            || (payloadTransferUpdate.getStatus() == PayloadTransferUpdate.Status.CANCELED))
                        {
                            payloadEventDispatcher.dispatchAsync(
                                    PayloadEventDispatcher.PAYLOAD_TRANSFER_FAILURE,
                                    new PayloadTransferInfo(
                                            payloadTransferUpdate.getPayloadId(),
                                            payloadTransferUpdate.getTotalBytes(),
                                            payloadTransferUpdate.getBytesTransferred(),
                                            payloadTransferUpdate.getStatus()));
                        }
                        else
                        {
                            payloadEventDispatcher.dispatchAsync(
                                    PayloadEventDispatcher.PAYLOAD_TRANSFER_UPDATE,
                                    new PayloadTransferInfo(
                                            payloadTransferUpdate.getPayloadId(),
                                            payloadTransferUpdate.getTotalBytes(),
                                            payloadTransferUpdate.getBytesTransferred(),
                                            payloadTransferUpdate.getStatus()));
                        }
                    }
                };

        this.advertisingStarted = false;
        this.discoveryStarted = false;

        this.discoveryEventDispatcher = new DiscoveryEventDispatcher();
        this.connectionEventDispatcher = new ConnectionEventDispatcher();
        this.payloadEventDispatcher = new PayloadEventDispatcher();
    }

    public static NearbyConnectionsManager getInstance()
    {
        if(instance == null)
        {
            instance = new NearbyConnectionsManager();
        }

        return instance;
    }

    public String getLocalEndpointName()
    {
        return localEndpointName;
    }

    public EndpointInfo getEndpointInfo(String endpointId)
    {
        return endpointInfoMap.get(endpointId);
    }

    public Collection<EndpointInfo> getEndpointInfoList()
    {
        return endpointInfoMap.values();
    }

    public Map<String, EndpointInfo> getEndpointInfoMap()
    {
        return endpointInfoMap;
    }

    public EndpointInfo getRoutingInfo(String endpointId)
    {
        return endpointInfoMap.get(endpointId);
    }

    public Collection<RoutingInfo> getRoutingInfoList()
    {
        return routingInfoMap.values();
    }

    public Map<String, RoutingInfo> getRoutingInfoMap()
    {
        return routingInfoMap;
    }

    public void initialize(Context ctx)
    {
        AppLogger.logDebug("NearbyConnectionsManager.initialize()");

        if(isInitialized())
            return;

        nearbyConnectionsClient = Nearby.getConnectionsClient(ctx);

        alarmManager = (AlarmManager)ctx.getSystemService(Context.ALARM_SERVICE);
        startAdvertisingAndDiscoveryPendingIntent = PendingIntent.getBroadcast(ctx, 0, new Intent(ctx, StartAdvertisingAndDiscoveryReceiver.class), 0);
        stopAdvertisingAndDiscoveryPendingIntent = PendingIntent.getBroadcast(ctx, 0, new Intent(ctx, StopAdvertisingAndDiscoveryReceiver.class), 0);

        initialized = true;
    }

    public boolean isInitialized()
    {
        return initialized;
    }

    public void unitialize()
    {
        alarmManager.cancel(startAdvertisingAndDiscoveryPendingIntent);
        alarmManager.cancel(stopAdvertisingAndDiscoveryPendingIntent);

        nearbyConnectionsClient.stopAllEndpoints();

        endpointInfoMap.clear();
        routingInfoMap.clear();

        initialized = false;
    }

    public void registerDiscoveryEventListener(IDiscoveryEventListener eventListener)
    {
        AppLogger.logDebug("NearbyConnectionsManager.registerDiscoveryEventListener()");
        discoveryEventDispatcher.registerEventListener(eventListener);
    }

    public void unregisterDiscoveryEventListener(IDiscoveryEventListener eventListener)
    {
        AppLogger.logDebug("NearbyConnectionsManager.unregisterDiscoveryEventListener()");
        discoveryEventDispatcher.unregisterEventListener(eventListener);
    }

    public void registerConnectionEventListener(IConnectionEventListener eventListener)
    {
        AppLogger.logDebug("NearbyConnectionsManager.registerConnectionEventListener()");
        connectionEventDispatcher.registerEventListener(eventListener);
    }

    public void unregisterConnectionEventListener(IConnectionEventListener eventListener)
    {
        AppLogger.logDebug("NearbyConnectionsManager.unregisterConnectionEventListener()");
        connectionEventDispatcher.unregisterEventListener(eventListener);
    }

    public void registerPayloadEventListener(IPayloadEventListener eventListener)
    {
        AppLogger.logDebug("NearbyConnectionsManager.registerPayloadEventListener()");
        payloadEventDispatcher.registerEventListener(eventListener);
    }

    public void unregisterPayloadEventListener(IPayloadEventListener eventListener)
    {
        AppLogger.logDebug("NearbyConnectionsManager.unregisterPayloadEventListener()");
        payloadEventDispatcher.unregisterEventListener(eventListener);
    }

    public void startAdvertisingAndDiscoveryScheduled()
    {
        AppLogger.logDebug("NearbyConnectionsManager.startAdvertisingAndDiscoveryScheduled()");

        if(!isInitialized())
            return;

        Calendar calendar = Calendar.getInstance();

        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.MINUTE, 1);
        calendar.set(Calendar.SECOND, 0);

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), startAdvertisingAndDiscoveryPendingIntent);

        AppLogger.logInfo("Start advertising and discovery in " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(calendar.getTime()));
    }

    public void stopAdvertisingAndDiscoveryScheduled()
    {
        AppLogger.logDebug("NearbyConnectionsManager.stopAdvertisingAndDiscoveryScheduled()");

        if(!isInitialized())
            return;

        Calendar calendar = Calendar.getInstance();

        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.SECOND, 30);

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), stopAdvertisingAndDiscoveryPendingIntent);

        AppLogger.logInfo("Stop advertising and discovery in " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(calendar.getTime()));
    }

    public void startAdvertisingAndDiscovery()
    {
        startAdvertisingAndDiscovery(null, null);
    }

    public void startAdvertisingAndDiscovery(OnSuccessListener onSuccessListener, OnFailureListener onFailureListener)
    {
        AppLogger.logDebug("NearbyConnectionsManager.startAdvertisingAndDiscovery()");
        startAdvertising(onSuccessListener, onFailureListener);
        startDiscovery(onSuccessListener, onFailureListener);
    }

    public void startAdvertising()
    {
        startAdvertising(null, null);
    }

    public void startAdvertising(OnSuccessListener onSuccessListener, OnFailureListener onFailureListener)
    {
        AppLogger.logDebug("NearbyConnectionsManager.startAdvertising()");

        if(!isInitialized() || advertisingStarted)
            return;

        nearbyConnectionsClient
                .startAdvertising(
                        localEndpointName,
                        endpointServiceId,
                        connectionLifecycleCallback,
                        new AdvertisingOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build())
                .addOnSuccessListener(onSuccessListener)
                .addOnFailureListener(onFailureListener);

        advertisingStarted = true;
    }

    public void startDiscovery()
    {
        startDiscovery(null, null);
    }

    public void startDiscovery(OnSuccessListener onSuccessListener, OnFailureListener onFailureListener)
    {
        AppLogger.logDebug("NearbyConnectionsManager.startDiscovery()");

        if(!isInitialized() || discoveryStarted)
            return;

        nearbyConnectionsClient
                .startDiscovery(
                        endpointServiceId,
                        endpointDiscoveryCallback,
                        new DiscoveryOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build())
                .addOnSuccessListener(onSuccessListener)
                .addOnFailureListener(onFailureListener);

        discoveryStarted = true;
    }

    public void stopAdvertisingAndDiscovery()
    {
        stopAdvertising();
        stopDiscovery();
    }

    public void stopAdvertising()
    {
        AppLogger.logDebug("NearbyConnectionsManager.stopAdvertising()");

        if(!isInitialized() || !advertisingStarted)
            return;

        nearbyConnectionsClient.stopAdvertising();
        advertisingStarted = false;
    }

    public void stopDiscovery()
    {
        AppLogger.logDebug("NearbyConnectionsManager.stopDiscovery()");

        if(!isInitialized() || !discoveryStarted)
            return;

        nearbyConnectionsClient.stopDiscovery();
        discoveryStarted = false;
    }

    public void requestConnection(String remoteEndpointId)
    {
        requestConnection(remoteEndpointId, null, null);
    }

    public void requestConnection(String remoteEndpointId, OnSuccessListener<Void> onSuccessListener, OnFailureListener onFailureListener)
    {
        AppLogger.logDebug("NearbyConnectionsManager.requestConnection()");

        if(!isInitialized())
            return;

        nearbyConnectionsClient.disconnectFromEndpoint(remoteEndpointId);

        nearbyConnectionsClient
                .requestConnection(
                        localEndpointName,
                        remoteEndpointId,
                        connectionLifecycleCallback)
                .addOnSuccessListener(onSuccessListener)
                .addOnFailureListener(onFailureListener);
    }

    public void acceptConnection(String remoteEndpointId)
    {
        acceptConnection(remoteEndpointId, null, null);
    }

    public void acceptConnection(String remoteEndpointId, OnSuccessListener<Void> onSuccessListener, OnFailureListener onFailureListener)
    {
        AppLogger.logDebug("NearbyConnectionsManager.acceptConnection()");

        if(!isInitialized())
            return;

        nearbyConnectionsClient
                .acceptConnection(
                        remoteEndpointId,
                        payloadCallback)
                .addOnSuccessListener(onSuccessListener)
                .addOnFailureListener(onFailureListener);
    }

    public void disconnect(String remoteEndpointId)
    {
        AppLogger.logDebug("NearbyConnectionsManager.disconnect()");

        if(!isInitialized())
            return;

        nearbyConnectionsClient.disconnectFromEndpoint(remoteEndpointId);
    }

    public void disconnectAll()
    {
        AppLogger.logDebug("NearbyConnectionsManager.disconnectAll()");

        if(!isInitialized())
            return;

        Collection<EndpointInfo> endpointInfoList = endpointInfoMap.values();

        for(EndpointInfo endpointInfo : endpointInfoList)
        {
            EndpointConnectionInfo endpointConnectionInfo = endpointInfo.getEndpointConnectionInfo();

            if((endpointConnectionInfo != null) && endpointConnectionInfo.isConnected())
            {
                nearbyConnectionsClient.disconnectFromEndpoint(endpointInfo.getEndpointId());
            }
        }
    }

    public void sendPayload(String remoteEndpointId, byte[] payloadBytes)
    {
        sendPayload(remoteEndpointId, payloadBytes, null, null);
    }

    public void sendPayload(String remoteEndpointId, byte[] payloadBytes, OnSuccessListener onSuccessListener, OnFailureListener onFailureListener)
    {
        sendPayload(remoteEndpointId, Payload.fromBytes(payloadBytes), onSuccessListener, onFailureListener);
    }

    public void sendPayload(String remoteEndpointId, File payloadFile) throws FileNotFoundException
    {
        sendPayload(remoteEndpointId, payloadFile, null, null);
    }

    public void sendPayload(String remoteEndpointId, File payloadFile, OnSuccessListener onSuccessListener, OnFailureListener onFailureListener) throws FileNotFoundException
    {
        sendPayload(remoteEndpointId, Payload.fromFile(payloadFile), onSuccessListener, onFailureListener);
    }

    public void sendPayload(String remoteEndpointId, InputStream payloadStream)
    {
        sendPayload(remoteEndpointId, payloadStream, null, null);
    }

    public void sendPayload(String remoteEndpointId, InputStream payloadStream, OnSuccessListener onSuccessListener, OnFailureListener onFailureListener)
    {
        sendPayload(remoteEndpointId, Payload.fromStream(payloadStream), onSuccessListener, onFailureListener);
    }

    public void sendPayload(String remoteEndpointId, Payload payload)
    {
        sendPayload(remoteEndpointId, payload, null, null);
    }

    public void sendPayload(String remoteEndpointId, Payload payload, OnSuccessListener onSuccessListener, OnFailureListener onFailureListener)
    {
        AppLogger.logDebug("NearbyConnectionsManager.sendPayload()");

        if(!isInitialized())
            return;

        nearbyConnectionsClient
                .sendPayload(remoteEndpointId, payload)
                .addOnSuccessListener(onSuccessListener)
                .addOnFailureListener(onFailureListener);
    }
}
