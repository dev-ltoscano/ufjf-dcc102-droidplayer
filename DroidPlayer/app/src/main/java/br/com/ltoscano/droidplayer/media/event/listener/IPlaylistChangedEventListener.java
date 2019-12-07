package br.com.ltoscano.droidplayer.media.event.listener;

import br.com.ltoscano.droidplayer.event.IEventListener;
import br.com.ltoscano.droidplayer.media.info.MediaInfo;

public interface IPlaylistChangedEventListener extends IEventListener
{
    public void onMediaRemovedFromPlaylist(MediaInfo mediaInfo);
}
