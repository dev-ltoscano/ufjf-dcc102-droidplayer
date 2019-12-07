package br.com.ltoscano.droidplayer.app.exception;

public class FileSystemException extends BaseException
{
    public FileSystemException(String msg)
    {
        super(msg);
    }

    public FileSystemException(Exception ex)
    {
        super(ex);
    }

    public FileSystemException(String msg, Exception ex)
    {
        super(msg, ex);
    }
}
