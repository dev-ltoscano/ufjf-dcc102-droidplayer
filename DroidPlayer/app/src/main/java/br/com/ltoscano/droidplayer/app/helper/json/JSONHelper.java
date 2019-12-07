package br.com.ltoscano.droidplayer.app.helper.json;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import br.com.ltoscano.droidplayer.app.exception.JSONException;

public class JSONHelper
{
    private static final Gson JSON_PARSER = new Gson();

    private JSONHelper()
    {

    }

    public static String objToJson(Object obj)
    {
        return JSON_PARSER.toJson(obj);
    }

    public static <T> T jsonToObj(String json, Class<T> objClass) throws JSONException
    {
        return jsonToObj(JSON_PARSER, json, objClass);
    }

    public static <T> T jsonToObj(Gson jsonParser, String json, Class<T> objClass) throws JSONException
    {
        try
        {
            return jsonParser.fromJson(json, objClass);
        }
        catch(JsonSyntaxException ex)
        {
            throw new JSONException(ex.getMessage(), ex);
        }
    }
}
