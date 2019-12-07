package br.com.ltoscano.droidplayer.app.exception;

public class InvalidPathException extends BaseException
{
    public InvalidPathException(String msg)
    {
        super(msg);
    }

    public InvalidPathException(Exception ex)
    {
        super(ex);
    }

    public InvalidPathException(String msg, Exception ex)
    {
        super(msg, ex);
    }
}
