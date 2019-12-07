package br.com.ltoscano.droidplayer.network.event.dispatcher;

import java.util.List;

import br.com.ltoscano.droidplayer.event.EventDispatcher;
import br.com.ltoscano.droidplayer.event.IEventListener;
import br.com.ltoscano.droidplayer.network.event.listener.IMessageEventListener;
import br.com.ltoscano.droidplayer.network.info.MessageInfo;

public class MessageEventDispatcher extends EventDispatcher
{
    public static final int NETWORK_MESSAGE_RECEIVED = 0;

    public MessageEventDispatcher()
    {
        super("NetworkMessageEvent");
    }

    @Override
    public void dispatch(int eventType, Object eventParam)
    {
        final MessageInfo messageInfo = (MessageInfo) eventParam;
        List<IEventListener> eventListenerList = getEventListenerList();

        for(IEventListener eventListener : eventListenerList)
        {
            ((IMessageEventListener)eventListener).onNetworkMessageReceived(messageInfo);
        }
    }
}
