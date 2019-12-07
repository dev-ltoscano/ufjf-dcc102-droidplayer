package br.com.ltoscano.droidplayer.media.event.listener;

import br.com.ltoscano.droidplayer.event.IEventListener;
import br.com.ltoscano.droidplayer.media.info.MediaInfo;

public interface IMediaStateChangedEventListener extends IEventListener
{
    public void onPlayMedia(MediaInfo mediaInfo);
    public void onPauseMedia(MediaInfo mediaInfo);
    public void onResumeMedia(MediaInfo mediaInfo);
}
