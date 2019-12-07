package br.com.ltoscano.droidplayer.network.event.listener;

import br.com.ltoscano.droidplayer.network.info.EndpointInfo;
import br.com.ltoscano.droidplayer.event.IEventListener;

public interface IDiscoveryEventListener extends IEventListener
{
    public void onEndpointFound(final EndpointInfo endpointInfo);
    public void onEndpointLost(final String endpointId);
}
