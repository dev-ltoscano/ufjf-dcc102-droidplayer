package br.com.ltoscano.droidplayer.network.info;

public class EndpointInfo
{
    private String endpointId;
    private String endpointName;
    private String serviceId;

    private EndpointConnectionInfo endpointConnectionInfo;

    public EndpointInfo(String endpointId, String endpointName, String serviceId)
    {
        this.endpointId = endpointId;
        this.endpointName = endpointName;
        this.serviceId = serviceId;
        this.endpointConnectionInfo = new EndpointConnectionInfo(endpointId, null, true);
    }

    public String getEndpointId()
    {
        return endpointId;
    }

    public String getEndpointName()
    {
        return endpointName;
    }

    public String getServiceId()
    {
        return serviceId;
    }

    public EndpointConnectionInfo getEndpointConnectionInfo()
    {
        return endpointConnectionInfo;
    }

    public void setAuthToken(String authToken)
    {
        endpointConnectionInfo.setAuthToken(authToken);
    }

    public void setIncomingConnection(boolean incomingConnection)
    {
        endpointConnectionInfo.setIncomingConnection(incomingConnection);
    }
}
