package br.com.ltoscano.droidplayer.network.event.listener;

import br.com.ltoscano.droidplayer.event.IEventListener;
import br.com.ltoscano.droidplayer.network.info.MessageInfo;

public interface IMessageEventListener extends IEventListener
{
    public void onNetworkMessageReceived(final MessageInfo messageInfo);
}
