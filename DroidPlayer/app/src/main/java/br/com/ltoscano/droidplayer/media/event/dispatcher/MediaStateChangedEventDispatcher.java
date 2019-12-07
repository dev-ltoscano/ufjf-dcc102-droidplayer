package br.com.ltoscano.droidplayer.media.event.dispatcher;

import java.util.List;

import br.com.ltoscano.droidplayer.event.EventDispatcher;
import br.com.ltoscano.droidplayer.event.IEventListener;
import br.com.ltoscano.droidplayer.media.event.listener.IMediaStateChangedEventListener;
import br.com.ltoscano.droidplayer.media.info.MediaInfo;

public class MediaStateChangedEventDispatcher extends EventDispatcher
{
    public static final int MEDIA_PLAY = 0;
    public static final int MEDIA_PAUSE = 1;
    public static final int MEDIA_RESUME = 2;

    public MediaStateChangedEventDispatcher()
    {
        super("MediaStateChangedEvent");
    }

    @Override
    public void dispatch(int eventType, Object eventParam)
    {
        final MediaInfo mediaInfo = (MediaInfo)eventParam;
        List<IEventListener> eventListenerList = getEventListenerList();

        for(IEventListener eventListener : eventListenerList)
        {
            switch (eventType)
            {
                case MEDIA_PLAY:
                {
                    ((IMediaStateChangedEventListener)eventListener).onPlayMedia(mediaInfo);
                    break;
                }
                case MEDIA_PAUSE:
                {
                    ((IMediaStateChangedEventListener)eventListener).onPauseMedia(mediaInfo);
                    break;
                }
                case MEDIA_RESUME:
                {
                    ((IMediaStateChangedEventListener)eventListener).onResumeMedia(mediaInfo);
                    break;
                }
            }
        }
    }
}
