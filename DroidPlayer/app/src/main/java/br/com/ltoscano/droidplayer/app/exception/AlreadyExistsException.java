package br.com.ltoscano.droidplayer.app.exception;

public class AlreadyExistsException extends BaseException
{
    public AlreadyExistsException(String msg)
    {
        super(msg);
    }

    public AlreadyExistsException(Exception ex)
    {
        super(ex);
    }

    public AlreadyExistsException(String msg, Exception ex)
    {
        super(msg, ex);
    }
}
