package br.com.ltoscano.droidplayer.filesystem.helper;

import java.util.UUID;

import br.com.ltoscano.droidplayer.app.helper.string.StringHelper;

public class IdHelper
{
    private IdHelper()
    {

    }

    public static String generate()
    {
        return UUID.randomUUID().toString();
    }

    public static String generate(String input)
    {
        if(StringHelper.isNullOrEmpty(input))
        {
            return null;
        }

        return HashHelper.generateSHA(input, HashHelper.SHAVersion.SHA1);
    }
}
