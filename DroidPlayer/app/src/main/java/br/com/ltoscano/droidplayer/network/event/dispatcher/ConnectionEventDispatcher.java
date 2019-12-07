package br.com.ltoscano.droidplayer.network.event.dispatcher;

import java.util.List;

import br.com.ltoscano.droidplayer.network.info.EndpointConnectionInfo;
import br.com.ltoscano.droidplayer.event.EventDispatcher;
import br.com.ltoscano.droidplayer.network.event.listener.IConnectionEventListener;
import br.com.ltoscano.droidplayer.event.IEventListener;

public class ConnectionEventDispatcher extends EventDispatcher
{
    public static final int CONNECTION_INSTANTIATED = 0;
    public static final int CONNECTION_RESULT = 1;
    public static final int DISCONNECTED = 2;

    public ConnectionEventDispatcher()
    {
        super("ConnectionEvent");
    }

    @Override
    public void dispatch(int eventType, Object eventParam)
    {
        List<IEventListener> eventListenerList = getEventListenerList();

        for(IEventListener eventListener : eventListenerList)
        {
            switch (eventType)
            {
                case CONNECTION_INSTANTIATED:
                {
                    ((IConnectionEventListener)eventListener).onConnectionInstantiated((EndpointConnectionInfo) eventParam);
                    break;
                }
                case CONNECTION_RESULT:
                {
                    ((IConnectionEventListener)eventListener).onConnectionResult((EndpointConnectionInfo) eventParam);
                    break;
                }
                case DISCONNECTED:
                {
                    ((IConnectionEventListener)eventListener).onDisconnected((EndpointConnectionInfo) eventParam);
                    break;
                }
            }
        }
    }
}
