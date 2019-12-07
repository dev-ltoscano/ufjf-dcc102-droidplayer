package br.com.ltoscano.droidplayer.media.event.dispatcher;

import java.util.List;

import br.com.ltoscano.droidplayer.event.EventDispatcher;
import br.com.ltoscano.droidplayer.event.IEventListener;
import br.com.ltoscano.droidplayer.media.event.listener.IPlaylistChangedEventListener;
import br.com.ltoscano.droidplayer.media.info.MediaInfo;

public class PlaylistChangedEventDispatcher extends EventDispatcher
{
    private final int MEDIA_REMOVED_FROM_PLAYLIST = 0;

    public enum PlaylistChangedEventType { MEDIA_REMOVED_FROM_PLAYLIST }

    public PlaylistChangedEventDispatcher()
    {
        super("PlaylistChangedEvent");
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
                case MEDIA_REMOVED_FROM_PLAYLIST:
                {
                    ((IPlaylistChangedEventListener)eventListener).onMediaRemovedFromPlaylist(mediaInfo);
                    break;
                }
            }
        }
    }
}
