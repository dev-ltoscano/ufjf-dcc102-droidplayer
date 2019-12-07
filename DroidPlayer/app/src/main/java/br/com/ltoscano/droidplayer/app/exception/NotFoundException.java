package br.com.ltoscano.droidplayer.app.exception;

public class NotFoundException extends BaseException
{
    public NotFoundException(String msg)
    {
        super(msg);
    }

    public NotFoundException(Exception ex)
    {
        super(ex);
    }

    public NotFoundException(String msg, Exception ex)
    {
        super(msg, ex);
    }
}
