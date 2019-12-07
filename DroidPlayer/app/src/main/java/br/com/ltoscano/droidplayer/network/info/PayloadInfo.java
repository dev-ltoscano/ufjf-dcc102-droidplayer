package br.com.ltoscano.droidplayer.network.info;

import com.google.android.gms.nearby.connection.Payload;

public class PayloadInfo
{
    public enum PayloadType { BYTE, FILE, STREAM}

    private String remoteEndpointId;
    private long payloadId;
    private PayloadType payloadType;

    private byte[] payloadBytes;
    private Payload.File payloadFile;
    private Payload.Stream payloadStream;

    public PayloadInfo(String remoteEndpointId, long payloadId, int payloadType, Object payload)
    {
        this.remoteEndpointId = remoteEndpointId;
        this.payloadId = payloadId;

        switch (payloadType)
        {
            case 1:
            {
                this.payloadType = PayloadType.BYTE;
                this.payloadBytes = (byte[])payload;
                break;
            }
            case 2:
            {
                this.payloadType = PayloadType.FILE;
                this.payloadFile = (Payload.File)payload;
                break;
            }
            case 3:
            {
                this.payloadType = PayloadType.STREAM;
                this.payloadStream = (Payload.Stream)payload;
                break;
            }
        }
    }

    public String getRemoteEndpointId()
    {
        return remoteEndpointId;
    }

    public long getPayloadId()
    {
        return payloadId;
    }

    public PayloadType getPayloadType()
    {
        return payloadType;
    }

    public byte[] getPayloadBytes()
    {
        return payloadBytes;
    }

    public Payload.File getPayloadFile()
    {
        return payloadFile;
    }

    public Payload.Stream getPayloadStream()
    {
        return payloadStream;
    }
}
