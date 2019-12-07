package br.com.ltoscano.droidplayer.app.helper.random;

import java.util.Random;

public class RandomHelper
{
    private static Random RANDOM_GENERATOR = new Random();

    public static int randomInteger(int minValue, int maxValue)
    {
        return RANDOM_GENERATOR.nextInt((maxValue - minValue) + 1) + minValue;
    }
}
