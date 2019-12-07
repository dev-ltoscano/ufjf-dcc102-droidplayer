package br.com.ltoscano.droidplayer.network.event.listener;

import br.com.ltoscano.droidplayer.event.IEventListener;
import br.com.ltoscano.droidplayer.network.info.PayloadInfo;
import br.com.ltoscano.droidplayer.network.info.PayloadTransferInfo;

public interface IPayloadEventListener extends IEventListener
{
    public void onPayloadReceived(final PayloadInfo payloadInfo);
    public void onPayloadTransferUpdate(final PayloadTransferInfo payloadTransferInfo);
    public void onPayloadTransferSuccess(final PayloadTransferInfo payloadTransferInfo);
    public void onPayloadTransferFailure(final PayloadTransferInfo payloadTransferInfo);
}
