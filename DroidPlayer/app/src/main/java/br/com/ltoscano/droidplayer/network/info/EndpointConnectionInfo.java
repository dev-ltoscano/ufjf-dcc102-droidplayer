package br.com.ltoscano.droidplayer.network.info;

public class EndpointConnectionInfo
{
    private String endpointId;
    private String authToken;
    private boolean incomingConnection;
    private boolean connected;

    public EndpointConnectionInfo(String endpointId, String authToken, boolean incomingConnection)
    {
        this.endpointId = endpointId;
        this.authToken = authToken;
        this.incomingConnection = incomingConnection;
        this.connected = false;
    }

    public String getEndpointId()
    {
        return endpointId;
    }

    public String getAuthToken()
    {
        return authToken;
    }

    public void setAuthToken(String authToken)
    {
        this.authToken = authToken;
    }

    public boolean isIncomingConnection()
    {
        return incomingConnection;
    }

    public void setIncomingConnection(boolean incomingConnection)
    {
        this.incomingConnection = incomingConnection;
    }

    public boolean isConnected()
    {
        return connected;
    }

    public void setConnected(boolean connected)
    {
        this.connected = connected;
    }
}
