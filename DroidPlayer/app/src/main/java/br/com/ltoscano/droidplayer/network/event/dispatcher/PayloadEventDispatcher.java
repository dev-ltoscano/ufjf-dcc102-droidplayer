package br.com.ltoscano.droidplayer.network.event.dispatcher;

import java.util.List;

import br.com.ltoscano.droidplayer.event.EventDispatcher;
import br.com.ltoscano.droidplayer.event.IEventListener;
import br.com.ltoscano.droidplayer.network.event.listener.IPayloadEventListener;
import br.com.ltoscano.droidplayer.network.info.PayloadInfo;
import br.com.ltoscano.droidplayer.network.info.PayloadTransferInfo;

public class PayloadEventDispatcher extends EventDispatcher
{
    public static final int PAYLOAD_RECEIVED = 0;
    public static final int PAYLOAD_TRANSFER_UPDATE = 1;
    public static final int PAYLOAD_TRANSFER_SUCCESS = 2;
    public static final int PAYLOAD_TRANSFER_FAILURE = 3;

    public PayloadEventDispatcher()
    {
        super("PayloadEvent");
    }

    @Override
    public void dispatch(int eventType, Object eventParam)
    {
        List<IEventListener> eventListenerList = getEventListenerList();

        for(IEventListener eventListener : eventListenerList)
        {
            switch (eventType)
            {
                case PAYLOAD_RECEIVED:
                {
                    ((IPayloadEventListener)eventListener).onPayloadReceived((PayloadInfo) eventParam);
                    break;
                }
                case PAYLOAD_TRANSFER_UPDATE:
                {
                    ((IPayloadEventListener)eventListener).onPayloadTransferUpdate((PayloadTransferInfo) eventParam);
                    break;
                }
                case PAYLOAD_TRANSFER_SUCCESS:
                {
                    ((IPayloadEventListener)eventListener).onPayloadTransferSuccess((PayloadTransferInfo) eventParam);
                    break;
                }
                case PAYLOAD_TRANSFER_FAILURE:
                {
                    ((IPayloadEventListener)eventListener).onPayloadTransferFailure((PayloadTransferInfo) eventParam);
                    break;
                }
            }
        }
    }
}
