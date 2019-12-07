package br.com.ltoscano.droidplayer.network.helper;

import java.util.Random;

public class DeviceNameGenerator
{
    private static final Random RANDOM_GENERATOR = new Random();

    private DeviceNameGenerator()
    {

    }

    public static String generate()
    {
        return String.valueOf(RANDOM_GENERATOR.nextInt(Integer.MAX_VALUE));
    }
}
