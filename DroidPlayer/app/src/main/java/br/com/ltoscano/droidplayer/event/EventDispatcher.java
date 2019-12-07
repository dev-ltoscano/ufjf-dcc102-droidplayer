package br.com.ltoscano.droidplayer.event;

import java.util.ArrayList;
import java.util.List;

public abstract class EventDispatcher
{
    private String eventName;
    private List<IEventListener> eventListenerList;

    public EventDispatcher(String eventName)
    {
        this.eventName = eventName;
        this.eventListenerList = new ArrayList<>();
    }

    public String getEventName()
    {
        return eventName;
    }

    public void registerEventListener(IEventListener eventListener)
    {
        if(!eventListenerList.contains(eventListener))
        {
            eventListenerList.add(eventListener);
        }
    }

    public void unregisterEventListener(IEventListener eventListener)
    {
        eventListenerList.remove(eventListener);
    }

    public void unregisterAllEventListeners()
    {
        eventListenerList.clear();
    }

    public List<IEventListener> getEventListenerList()
    {
        return eventListenerList;
    }

    public void dispatch()
    {
        dispatch(-1, null);
    }

    public void dispatch(int eventType)
    {
        dispatch(eventType, null);
    }

    public void dispatchAsync(final int eventType, final Object eventParam)
    {
        Thread dispatchThread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                dispatch(eventType, eventParam);
            }
        });

        dispatchThread.start();
    }

    public abstract void dispatch(int eventType, Object eventParam);
}
