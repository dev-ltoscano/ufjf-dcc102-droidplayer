package br.com.ltoscano.droidplayer.network.event.listener;

import br.com.ltoscano.droidplayer.network.info.EndpointConnectionInfo;
import br.com.ltoscano.droidplayer.event.IEventListener;

public interface IConnectionEventListener extends IEventListener
{
    public void onConnectionInstantiated(final EndpointConnectionInfo endpointConnectionInfo);
    public void onConnectionResult(final EndpointConnectionInfo endpointConnectionInfo);
    public void onDisconnected(final EndpointConnectionInfo endpointConnectionInfo);
}
