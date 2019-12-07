package br.com.ltoscano.droidplayer.app.exception;

public class JSONException extends BaseException
{
    public JSONException(String msg)
    {
        super(msg);
    }

    public JSONException(Exception ex)
    {
        super(ex);
    }

    public JSONException(String msg, Exception ex)
    {
        super(msg, ex);
    }
}
