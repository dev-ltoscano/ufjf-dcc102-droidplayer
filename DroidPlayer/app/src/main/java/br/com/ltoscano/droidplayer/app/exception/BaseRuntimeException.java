package br.com.ltoscano.droidplayer.app.exception;

public class BaseRuntimeException extends RuntimeException
{
    public BaseRuntimeException(String msg)
    {
        this(msg, null);
    }

    public BaseRuntimeException(Exception ex)
    {
        this(ex.getMessage(), ex);
    }

    public BaseRuntimeException(String msg, Exception ex)
    {
        super(msg, ex);
    }
}
