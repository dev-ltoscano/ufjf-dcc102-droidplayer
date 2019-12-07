package br.com.ltoscano.droidplayer.app.exception;

public class NotAvailableException extends BaseException
{
    public NotAvailableException(String msg)
    {
        super(msg);
    }

    public NotAvailableException(Exception ex)
    {
        super(ex);
    }

    public NotAvailableException(String msg, Exception ex)
    {
        super(msg, ex);
    }
}
