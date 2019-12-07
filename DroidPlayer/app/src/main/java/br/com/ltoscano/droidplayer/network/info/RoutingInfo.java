package br.com.ltoscano.droidplayer.network.info;

public class RoutingInfo
{
    private String toEndpointName;
    private String forwardEndpointName;
    private int numberOfHops;

    public RoutingInfo(String toEndpointName, String forwardEndpointName, int numberOfHops)
    {
        this.toEndpointName = toEndpointName;
        this.forwardEndpointName = forwardEndpointName;
        this.numberOfHops = numberOfHops;
    }

    public String getToEndpointName()
    {
        return toEndpointName;
    }

    public void setToEndpointName(String toEndpointName)
    {
        this.toEndpointName = toEndpointName;
    }

    public String getForwardEndpointName()
    {
        return forwardEndpointName;
    }

    public void setForwardEndpointName(String forwardEndpointName)
    {
        this.forwardEndpointName = forwardEndpointName;
    }

    public int getNumberOfHops()
    {
        return numberOfHops;
    }

    public void setNumberOfHops(int numberOfHops)
    {
        this.numberOfHops = numberOfHops;
    }
}
