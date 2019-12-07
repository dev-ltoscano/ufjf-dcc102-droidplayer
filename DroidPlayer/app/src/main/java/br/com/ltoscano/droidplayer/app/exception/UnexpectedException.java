package br.com.ltoscano.droidplayer.app.exception;

public class UnexpectedException extends BaseRuntimeException
{
    public UnexpectedException(String msg)
    {
        super(msg);
    }

    public UnexpectedException(Exception ex)
    {
        super(ex);
    }

    public UnexpectedException(String msg, Exception ex)
    {
        super(msg, ex);
    }
}
