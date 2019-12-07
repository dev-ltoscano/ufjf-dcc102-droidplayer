package br.com.ltoscano.droidplayer.filesystem.helper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import br.com.ltoscano.droidplayer.app.exception.UnexpectedException;
import br.com.ltoscano.droidplayer.app.helper.encoding.EncodingHelper;

public class HashHelper
{
    public enum SHAVersion { SHA1, SHA256 }

    private HashHelper()
    {

    }

    public static String generateSHA(String input, SHAVersion shaVersion)
    {
        return generateSHA(input.getBytes(StandardCharsets.UTF_8), shaVersion);
    }

    public static String generateSHA(byte[] byteArray, SHAVersion shaVersion)
    {
        MessageDigest msgDigest = null;

        try
        {
            switch (shaVersion)
            {
                case SHA1:
                {
                    msgDigest = MessageDigest.getInstance("SHA-1");
                    break;
                }
                case SHA256:
                {
                    msgDigest = MessageDigest.getInstance("SHA-256");
                    break;
                }
            }
        }
        catch(NoSuchAlgorithmException ex)
        {
            throw new UnexpectedException("The SHA algorithm version is invalid");
        }

        return EncodingHelper.encodeHexString(msgDigest.digest(byteArray));
    }
}
