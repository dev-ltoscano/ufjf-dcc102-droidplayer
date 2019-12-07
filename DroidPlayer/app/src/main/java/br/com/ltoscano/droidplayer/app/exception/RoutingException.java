package br.com.ltoscano.droidplayer.app.exception;

public class RoutingException extends BaseException
{
    public RoutingException(String msg) {
        super(msg);
    }

    public RoutingException(Exception ex) {
        super(ex);
    }

    public RoutingException(String msg, Exception ex) {
        super(msg, ex);
    }
}
