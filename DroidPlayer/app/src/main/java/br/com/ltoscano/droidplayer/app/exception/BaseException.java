package br.com.ltoscano.droidplayer.app.exception;

public class BaseException extends Exception
{
    public BaseException(String msg)
    {
        this(msg, null);
    }

    public BaseException(Exception ex)
    {
        this(ex.getMessage(), ex);
    }

    public BaseException(String msg, Exception ex)
    {
        super(msg, ex);
    }
}
