package br.com.ltoscano.droidplayer.network.info;

public class PayloadTransferInfo
{
    public enum PayloadTransferStatus { SUCCESS, FAILURE, IN_PROGRESS, CANCELED }

    private long payloadId;
    private long totalBytes;
    private long transferredBytes;
    private PayloadTransferStatus transferStatus;

    public PayloadTransferInfo(long payloadId, long totalBytes, long transferredBytes, int status)
    {
        this.payloadId = payloadId;
        this.totalBytes = totalBytes;
        this.transferredBytes = transferredBytes;

        switch (status)
        {
            case 1:
            {
                this.transferStatus = PayloadTransferStatus.SUCCESS;
                break;
            }
            case 2:
            {
                this.transferStatus = PayloadTransferStatus.FAILURE;
                break;
            }
            case 3:
            {
                this.transferStatus = PayloadTransferStatus.IN_PROGRESS;
                break;
            }
            case 4:
            {
                this.transferStatus = PayloadTransferStatus.CANCELED;
                break;
            }
        }
    }

    public long getPayloadId()
    {
        return payloadId;
    }

    public long getTotalBytes()
    {
        return totalBytes;
    }

    public long getTransferredBytes()
    {
        return transferredBytes;
    }

    public void setTransferredBytes(long transferredBytes)
    {
        this.transferredBytes = transferredBytes;
    }

    public PayloadTransferStatus getTransferStatus()
    {
        return transferStatus;
    }

    public void setTransferStatus(PayloadTransferStatus transferStatus)
    {
        this.transferStatus = transferStatus;
    }
}
