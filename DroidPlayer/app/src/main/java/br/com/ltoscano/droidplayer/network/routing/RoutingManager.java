package br.com.ltoscano.droidplayer.network.routing;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.internal.LinkedTreeMap;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import br.com.ltoscano.droidplayer.app.exception.NotFoundException;
import br.com.ltoscano.droidplayer.app.exception.RoutingException;
import br.com.ltoscano.droidplayer.app.helper.string.StringHelper;
import br.com.ltoscano.droidplayer.network.info.EndpointInfo;
import br.com.ltoscano.droidplayer.network.info.RoutingInfo;
import br.com.ltoscano.droidplayer.app.log.AppLogger;
import br.com.ltoscano.droidplayer.network.nearby.NearbyConnectionsManager;
import br.com.ltoscano.droidplayer.network.info.MessageInfo;

public class RoutingManager
{
    public static void sendRoutingInfo(Map<String, RoutingInfo> routingInfoMap, Collection<EndpointInfo> endpointInfoList) throws RoutingException
    {
        AppLogger.logDebug("RoutingManager.sendRoutingInfo()");

        MessageInfo messageInfo = new MessageInfo("ROUTING_INFO");

        String localEndpointName = NearbyConnectionsManager.getInstance().getLocalEndpointName();
        List<RoutingInfo> routingInfoListResponse = new ArrayList<>();

        for(Map.Entry<String, RoutingInfo> routingEntry : routingInfoMap.entrySet())
        {
            routingInfoListResponse.add(
                    new RoutingInfo(
                            routingEntry.getValue().getToEndpointName(),
                            localEndpointName,
                            routingEntry.getValue().getNumberOfHops() + 1));
        }

        messageInfo.setParam("ROUTING", routingInfoListResponse);

        for(EndpointInfo remoteEndpointInfo : endpointInfoList)
        {
            messageInfo.setParam("TO_ENDPOINT_NAME", remoteEndpointInfo.getEndpointName());
            sendMessage(routingInfoMap, endpointInfoList, messageInfo);

            AppLogger.logInfo("Routes sent to " + remoteEndpointInfo.getEndpointName());
        }
    }

    public static void updateRoutingInfo(Map<String, RoutingInfo> routingInfoMap, Collection<EndpointInfo> endpointInfoList, MessageInfo messageInfo) throws RoutingException
    {
        AppLogger.logDebug("RoutingManager.updateRoutingInfo()");

        try
        {
            final NearbyConnectionsManager nearbyConnectionsManager = NearbyConnectionsManager.getInstance();

            List<LinkedTreeMap<String, Object>> remoteRoutingInfoList = (List<LinkedTreeMap<String, Object>>) messageInfo.getParam("ROUTING");

            for(LinkedTreeMap<String, Object> remoteRoutingInfoMap : remoteRoutingInfoList)
            {
                RoutingInfo remoteRoutingInfo = new RoutingInfo(
                        remoteRoutingInfoMap.get("toEndpointName").toString(),
                        remoteRoutingInfoMap.get("forwardEndpointName").toString(),
                        ((Double)remoteRoutingInfoMap.get("numberOfHops")).intValue());

                if(!remoteRoutingInfo.getToEndpointName().equalsIgnoreCase(nearbyConnectionsManager.getLocalEndpointName()))
                {
                    if(!routingInfoMap.containsKey(remoteRoutingInfo.getToEndpointName()))
                    {
                        routingInfoMap.put(remoteRoutingInfo.getToEndpointName(), remoteRoutingInfo);
                        AppLogger.logInfo("New route discovered for " + remoteRoutingInfo.getToEndpointName());
                    }
                    else
                    {
                        RoutingInfo localRoutingInfo = routingInfoMap.get(remoteRoutingInfo.getToEndpointName());

                        if(localRoutingInfo.getNumberOfHops() > remoteRoutingInfo.getNumberOfHops())
                        {
                            routingInfoMap.put(remoteRoutingInfo.getToEndpointName(), remoteRoutingInfo);
                            AppLogger.logInfo("New route discovered for " + remoteRoutingInfo.getToEndpointName());
                        }
                    }
                }
            }
        }
        catch (NotFoundException ex)
        {
            AppLogger.logError(ex.getMessage(), ex);
        }
    }

    public static void forwardMessage(Map<String, RoutingInfo> routingInfoMap, Collection<EndpointInfo> endpointInfoList, MessageInfo messageInfo) throws RoutingException
    {
        AppLogger.logDebug("RoutingManager.forwardMessage()");
        sendMessage(routingInfoMap, endpointInfoList, messageInfo);
    }

    public static void sendMessage(Map<String, RoutingInfo> routingInfoMap, Collection<EndpointInfo> endpointInfoList, final MessageInfo messageInfo) throws RoutingException
    {
        AppLogger.logDebug("RoutingManager.sendMessage()");

        try
        {
            final NearbyConnectionsManager nearbyConnectionsManager = NearbyConnectionsManager.getInstance();

            String toEndpointName = messageInfo.getParam("TO_ENDPOINT_NAME").toString();

            if(!routingInfoMap.containsKey(toEndpointName))
            {
                throw new RoutingException("Could not route message to '" + toEndpointName + "'");
            }

            String remoteEndpointName = routingInfoMap.get(toEndpointName).getForwardEndpointName();
            String remoteEndpointId = null;
            boolean isConnected = false;

            if(remoteEndpointName.equalsIgnoreCase(nearbyConnectionsManager.getLocalEndpointName()))
            {
                for(EndpointInfo endpointInfo : endpointInfoList)
                {
                    if(endpointInfo.getEndpointName().equalsIgnoreCase(toEndpointName))
                    {
                        remoteEndpointId = endpointInfo.getEndpointId();
                        isConnected = (endpointInfo.getEndpointConnectionInfo() == null) ? false : endpointInfo.getEndpointConnectionInfo().isConnected();
                        break;
                    }
                }
            }
            else
            {
                for(EndpointInfo endpointInfo : endpointInfoList)
                {
                    if(endpointInfo.getEndpointName().equalsIgnoreCase(remoteEndpointName))
                    {
                        remoteEndpointId = endpointInfo.getEndpointId();
                        isConnected = (endpointInfo.getEndpointConnectionInfo() == null) ? false : endpointInfo.getEndpointConnectionInfo().isConnected();
                        break;
                    }
                }
            }

            if(StringHelper.isNullOrEmpty(remoteEndpointId))
            {
                throw new RoutingException("Could not route message to '" + toEndpointName + "'");
            }

            final String endpointToSent = remoteEndpointId;

            if(!isConnected)
            {
                nearbyConnectionsManager.requestConnection(
                        endpointToSent,
                        new OnSuccessListener<Void>()
                        {
                            @Override
                            public void onSuccess(Void aVoid)
                            {
                                AppLogger.logInfo("Successful connection to " + endpointToSent);

                                nearbyConnectionsManager.sendPayload(
                                        endpointToSent,
                                        messageInfo.toString().getBytes(StandardCharsets.UTF_8),
                                        new OnSuccessListener()
                                        {
                                            @Override
                                            public void onSuccess(Object o)
                                            {
                                                AppLogger.logInfo("Message sent to " + endpointToSent);
                                            }
                                        },
                                        new OnFailureListener()
                                        {
                                            @Override
                                            public void onFailure(@NonNull Exception ex)
                                            {
                                                AppLogger.logError( "Error sending message to " + endpointToSent, ex);
                                            }
                                        });
                            }
                        },
                        new OnFailureListener()
                        {
                            @Override
                            public void onFailure(@NonNull Exception ex)
                            {
                                AppLogger.logError("Error connecting with " + endpointToSent, ex);
                            }
                        });
            }
            else
            {
                nearbyConnectionsManager.sendPayload(
                        endpointToSent,
                        messageInfo.toString().getBytes(StandardCharsets.UTF_8),
                        new OnSuccessListener()
                        {
                            @Override
                            public void onSuccess(Object o)
                            {
                                AppLogger.logInfo("Message sent to " + endpointToSent);
                            }
                        },
                        new OnFailureListener()
                        {
                            @Override
                            public void onFailure(@NonNull Exception ex)
                            {
                                AppLogger.logError( "Error sending message to " + endpointToSent, ex);
                            }
                        });
            }
        }
        catch (NotFoundException ex)
        {
            throw new RoutingException("Could not sendPayload message");
        }
    }
}
