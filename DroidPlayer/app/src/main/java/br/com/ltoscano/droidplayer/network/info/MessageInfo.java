package br.com.ltoscano.droidplayer.network.info;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import br.com.ltoscano.droidplayer.app.exception.JSONException;
import br.com.ltoscano.droidplayer.app.exception.NotFoundException;
import br.com.ltoscano.droidplayer.app.helper.json.JSONHelper;
import br.com.ltoscano.droidplayer.app.helper.string.StringHelper;

public class MessageInfo
{
    private final Map<String, Object> params;

    public MessageInfo(String msgType)
    {
        this.params = new HashMap<>();
        this.params.put("MSG_TYPE", StringHelper.capitalize(msgType));
    }

    public String getMessageType()
    {
        return params.get("MSG_TYPE").toString();
    }

    public Object getParam(String paramKey) throws NotFoundException
    {
        paramKey = StringHelper.capitalize(paramKey);

        if(!params.containsKey(paramKey))
        {
            throw new NotFoundException("The param '" + paramKey + "' not found");
        }

        return params.get(paramKey);
    }

    public void setParam(String paramKey, Object paramValue)
    {
        params.put(StringHelper.capitalize(paramKey), paramValue);
    }

    public Set<Map.Entry<String, Object>> getParams()
    {
        return params.entrySet();
    }

    public static MessageInfo fromJson(String json) throws JSONException
    {
        return JSONHelper.jsonToObj(json, MessageInfo.class);
    }

    @Override
    public String toString()
    {
        return JSONHelper.objToJson(this);
    }
}
