package br.com.ltoscano.droidplayer.filesystem.helper;

import java.io.File;
import java.util.regex.Pattern;

import br.com.ltoscano.droidplayer.app.exception.InvalidPathException;
import br.com.ltoscano.droidplayer.app.helper.string.StringHelper;

public class PathHelper
{
    private static final String UNIX_PATH_PATTERN = "^/|^\\.|^\\.\\.|^~|(^\\.|^\\.\\.|^\\~)?(/\\.\\.)*(/[a-zA-Z0-9_]+)*([a-zA-Z0-9_\\-\\s]+/?)*((/|(\\.[a-zA-Z0-9]+)+)$)?";

    private PathHelper()
    {

    }

    public static boolean isValidPath(String path)
    {
        if(StringHelper.isNullOrEmpty(path))
        {
            return false;
        }

        return Pattern.compile(UNIX_PATH_PATTERN).matcher(path).matches();
    }

    public static String getBasePath(String path) throws InvalidPathException
    {
        if(!isValidPath(path))
        {
            throw new InvalidPathException("The path '" + path + "' is invalid");
        }

        int index = path.lastIndexOf('/');

        if(index == 0)
        {
            if(path.length() == 1)
            {
                return null;
            }
            else
            {
                return "/";
            }
        }
        else
        {
            return path.substring(0, index);
        }
    }

    public static String getName(String path) throws InvalidPathException
    {
        if(!isValidPath(path))
        {
            throw new InvalidPathException("The path '" + path + "' is invalid");
        }

        return new File(path).getName();
    }
}
