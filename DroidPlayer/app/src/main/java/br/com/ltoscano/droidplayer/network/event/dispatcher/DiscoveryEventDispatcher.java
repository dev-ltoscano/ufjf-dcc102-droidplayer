package br.com.ltoscano.droidplayer.network.event.dispatcher;

import java.util.List;

import br.com.ltoscano.droidplayer.network.info.EndpointInfo;
import br.com.ltoscano.droidplayer.event.EventDispatcher;
import br.com.ltoscano.droidplayer.network.event.listener.IDiscoveryEventListener;
import br.com.ltoscano.droidplayer.event.IEventListener;

public class DiscoveryEventDispatcher extends EventDispatcher
{
    public static final int ENDPOINT_FOUND = 0;
    public static final int ENDPOINT_LOST = 1;

    public DiscoveryEventDispatcher()
    {
        super("DiscoveryEvent");
    }

    @Override
    public void dispatch(int eventType, Object eventParam)
    {
        List<IEventListener> eventListenerList = getEventListenerList();

        for(IEventListener eventListener : eventListenerList)
        {
            switch (eventType)
            {
                case ENDPOINT_FOUND:
                {
                    ((IDiscoveryEventListener)eventListener).onEndpointFound((EndpointInfo)eventParam);
                    break;
                }
                case ENDPOINT_LOST:
                {
                    ((IDiscoveryEventListener)eventListener).onEndpointLost((String) eventParam);
                    break;
                }
            }
        }
    }
}
