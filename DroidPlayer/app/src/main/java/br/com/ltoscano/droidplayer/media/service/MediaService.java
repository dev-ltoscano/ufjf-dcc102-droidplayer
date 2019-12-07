package br.com.ltoscano.droidplayer.media.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import br.com.ltoscano.droidplayer.R;
import br.com.ltoscano.droidplayer.app.log.AppLogger;
import br.com.ltoscano.droidplayer.app.helper.notification.NotificationHelper;
import br.com.ltoscano.droidplayer.ui.activity.MainActivity;
import br.com.ltoscano.droidplayer.media.event.dispatcher.MediaStateChangedEventDispatcher;
import br.com.ltoscano.droidplayer.media.event.listener.IMediaStateChangedEventListener;
;
import br.com.ltoscano.droidplayer.media.info.MediaInfo;

public class MediaService extends Service implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
                                                        MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener,
                                                        AudioManager.OnAudioFocusChangeListener
{
    public static final String ACTION_PLAY = "br.com.ltoscano.droidplayer.player.service.ACTION_PLAY";
    public static final String ACTION_PAUSE = "br.com.ltoscano.droidplayer.player.service.ACTION_PAUSE";
    public static final String ACTION_RESUME = "br.com.ltoscano.droidplayer.player.service.ACTION_RESUME";
    public static final String ACTION_PREVIOUS = "br.com.ltoscano.droidplayer.player.service.ACTION_PREVIOUS";
    public static final String ACTION_NEXT = "br.com.ltoscano.droidplayer.player.service.ACTION_NEXT";
    public static final String ACTION_STOP = "br.com.ltoscano.droidplayer.player.service.ACTION_STOP";

    public enum PlaybackStatus { PLAYING, PAUSED, STOPPED }

    public class LocalBinder extends Binder
    {
        public MediaService getService()
        {
            return MediaService.this;
        }
    }

    private final IBinder binder;

    private MediaPlayer mediaPlayer;
    private List<MediaInfo> mediaInfoList;
    private int currentMediaIndex;
    private int currentMediaPosition;
    private PlaybackStatus playbackStatus;

    private AudioManager audioManager;
    private AudioFocusRequest audioFocusRequest;

    private MediaSessionCompat mediaSession;
    private MediaControllerCompat.TransportControls mediaTransportControls;

    private MediaStateChangedEventDispatcher mediaStateChangedEventDispatcher;

    private final String NOTIFICATION_CHANNEL_ID = "DroidMediaPlayerService";

    public MediaService()
    {
        this.binder = new LocalBinder();

        this.mediaPlayer = null;
        this.mediaInfoList = new ArrayList<>();
        this.currentMediaIndex = 0;
        this.currentMediaPosition = 0;
        this.playbackStatus = PlaybackStatus.STOPPED;

        this.audioManager = null;

        this.mediaStateChangedEventDispatcher = new MediaStateChangedEventDispatcher();
    }

    private void initializeMediaPlayer()
    {
        if(mediaPlayer == null)
        {
            mediaPlayer = new MediaPlayer();

            mediaPlayer.setOnBufferingUpdateListener(this);
            mediaPlayer.setOnSeekCompleteListener(this);
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.setOnInfoListener(this);
            mediaPlayer.setOnErrorListener(this);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }
    }

    private void initializeMediaSession() throws RemoteException
    {
        if (mediaSession != null)
            return;

        mediaSession = new MediaSessionCompat(getApplicationContext(), "DroidPlayer");
        mediaSession.setActive(true);
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        mediaTransportControls = mediaSession.getController().getTransportControls();

        mediaSession.setCallback(new MediaSessionCompat.Callback()
        {
            @Override
            public void onPlay()
            {
                super.onPlay();

                if(playbackStatus == PlaybackStatus.STOPPED)
                {
                    playMedia();
                }
                else
                {
                    resumeMedia();
                }
            }

            @Override
            public void onPause()
            {
                super.onPause();
                pauseMedia();
            }

            @Override
            public void onSkipToPrevious()
            {
                super.onSkipToPrevious();
                previousMedia();
            }

            @Override
            public void onSkipToNext()
            {
                super.onSkipToNext();
                nextMedia();
            }

            @Override
            public void onStop()
            {
                super.onStop();
                stopMedia();
                stopSelf();
            }

            @Override
            public void onSeekTo(long position)
            {
                super.onSeekTo(position);
            }
        });
    }

    private void handleIncomingActions(Intent playbackAction)
    {
        if ((playbackAction == null) || (playbackAction.getAction() == null))
            return;

        String actionString = playbackAction.getAction();

        switch (actionString)
        {
            case ACTION_PLAY:
            {
                mediaTransportControls.play();
                break;
            }
            case ACTION_PAUSE:
            {
                mediaTransportControls.pause();
                break;
            }
            case ACTION_RESUME:
            {
                mediaTransportControls.play();
                break;
            }
            case ACTION_PREVIOUS:
            {
                mediaTransportControls.skipToPrevious();
                break;
            }
            case ACTION_NEXT:
            {
                mediaTransportControls.skipToNext();
                break;
            }
            case ACTION_STOP:
            {
                mediaTransportControls.stop();
                break;
            }
        }
    }

    public MediaPlayer getMediaPlayer()
    {
        return mediaPlayer;
    }

    public void registerMediaStateChangedEventListener(IMediaStateChangedEventListener eventListener)
    {
        mediaStateChangedEventDispatcher.registerEventListener(eventListener);
    }

    public void unregisterMediaStateChangedEventListener(IMediaStateChangedEventListener eventListener)
    {
        mediaStateChangedEventDispatcher.unregisterEventListener(eventListener);
    }

    public PlaybackStatus getPlaybackStatus()
    {
        return playbackStatus;
    }

    public void addMediaInfoList(List<MediaInfo> mediaInfoList)
    {
        for(MediaInfo mediaInfo : mediaInfoList)
        {
            addMediaInfo(mediaInfo);
        }
    }

    public void addMediaInfo(MediaInfo mediaInfo)
    {
        mediaInfoList.add(mediaInfo);
    }

    public void removeMediaInfo(MediaInfo mediaInfo)
    {
        mediaInfoList.remove(mediaInfo);

        if(mediaInfoList.isEmpty())
        {
            stopMedia();
            currentMediaIndex = 0;
        }
        else
        {
            currentMediaIndex = currentMediaIndex % mediaInfoList.size();

            if(playbackStatus == PlaybackStatus.PLAYING)
            {
                playMedia();
            }
        }
    }

    public MediaInfo getCurrentMediaInfo()
    {
        if(!mediaInfoList.isEmpty())
        {
            return mediaInfoList.get(currentMediaIndex);
        }

        return new MediaInfo("Nenhuma música", "", "Nenhum artista", "");
    }

    public List<MediaInfo> getMediaInfoList()
    {
        return mediaInfoList;
    }

    public boolean mediaInfoListIsEmpty()
    {
        return mediaInfoList.isEmpty();
    }

    public void clearMediaInfoList()
    {
        stopMedia();

        mediaInfoList.clear();
        currentMediaIndex = 0;
        currentMediaPosition = 0;
    }

    public void playMedia(MediaInfo mediaInfo)
    {
        currentMediaIndex = mediaInfoList.indexOf(mediaInfo);
        playMedia();
    }

    public void playMedia()
    {
        if (mediaPlayer == null)
            initializeMediaPlayer();

        if(mediaInfoListIsEmpty())
            stopMedia();

        if(playbackStatus == PlaybackStatus.PAUSED)
        {
            resumeMedia();
        }
        else if(playbackStatus == PlaybackStatus.PLAYING)
        {
            stopMedia();
        }

        if(!mediaInfoList.isEmpty())
        {
            mediaPlayer.reset();

            try
            {
                mediaPlayer.setDataSource(getCurrentMediaInfo().getPath());
                mediaPlayer.prepareAsync();
            }
            catch (IOException ex)
            {
                ex.printStackTrace();
                stopSelf();
            }
        }
    }

    public void pauseMedia()
    {
        if (mediaPlayer == null)
            return;

        if (mediaPlayer.isPlaying())
        {
            mediaPlayer.pause();
            currentMediaPosition = mediaPlayer.getCurrentPosition();
            playbackStatus = PlaybackStatus.PAUSED;

            stopForeground(false);

            MediaInfo currentMediaInfo = getCurrentMediaInfo();

            Notification mediaNotification = createMediaNotification(
                    PlaybackStatus.PAUSED,
                    R.drawable.play,
                    R.drawable.cover,
                    currentMediaInfo.getTitle(),
                    currentMediaInfo.getArtist());

            NotificationHelper.showNotification(this, R.integer.media_service_notification_id, mediaNotification);

            mediaStateChangedEventDispatcher.dispatch(MediaStateChangedEventDispatcher.MEDIA_PAUSE, currentMediaInfo);
        }
    }

    public void resumeMedia()
    {
        if (mediaPlayer == null)
            return;

        if(mediaInfoList.isEmpty())
        {
            mediaPlayer.stop();
            playbackStatus = PlaybackStatus.STOPPED;

            stopForeground(false);

            MediaInfo currentMediaInfo = getCurrentMediaInfo();

            Notification mediaNotification = createMediaNotification(
                    PlaybackStatus.PAUSED,
                    R.drawable.play,
                    R.drawable.cover,
                    currentMediaInfo.getTitle(),
                    currentMediaInfo.getArtist());

            NotificationHelper.showNotification(this, R.integer.media_service_notification_id, mediaNotification);
        }
        else if (!mediaPlayer.isPlaying())
        {
            mediaPlayer.seekTo(currentMediaPosition);
        }
    }

    public void skipToMedia(int position)
    {
        if (mediaPlayer == null)
            return;

        if (mediaPlayer.isPlaying())
        {
            mediaPlayer.seekTo(position);
        }
    }

    public void previousMedia()
    {
        if(!mediaInfoList.isEmpty())
        {
            int mediaInfoListSize = mediaInfoList.size();
            currentMediaIndex = (((currentMediaIndex - 1) % mediaInfoListSize + mediaInfoListSize) % mediaInfoListSize);

            playMedia();
        }
    }

    public void nextMedia()
    {
        if(!mediaInfoList.isEmpty())
        {
            int mediaInfoListSize = mediaInfoList.size();
            currentMediaIndex = (((currentMediaIndex + 1) % mediaInfoListSize + mediaInfoListSize) % mediaInfoListSize);

            playMedia();
        }
    }

    public void stopMedia()
    {
        if (mediaPlayer == null)
            return;

        if (mediaPlayer.isPlaying())
        {
            mediaPlayer.stop();
            playbackStatus = PlaybackStatus.STOPPED;

            stopForeground(false);

            Notification mediaNotification = createMediaNotification(
                    PlaybackStatus.PAUSED,
                    R.drawable.play,
                    R.drawable.cover,
                    "Nenhuma música",
                    "Nenhum artista");

            NotificationHelper.showNotification(this, R.integer.media_service_notification_id, mediaNotification);
        }
    }

    private boolean requestAudioFocus()
    {
        if(audioManager == null)
        {
            audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            AudioFocusRequest audioFocusRequest =
                    new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                            .setOnAudioFocusChangeListener(this)
                            .build();

            return (audioManager.requestAudioFocus(audioFocusRequest) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED);
        }
        else
        {
            return (audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED);
        }
    }

    private boolean removeAudioFocus()
    {
        if(audioManager == null)
            return true;

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            audioFocusRequest =
                    new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                            .setOnAudioFocusChangeListener(this)
                            .build();

            return (audioManager.abandonAudioFocusRequest(audioFocusRequest) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED);
        }
        else
        {
            return (audioManager.abandonAudioFocus(this) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED);
        }
    }

    private Notification createMediaNotification(PlaybackStatus playbackStatus,
                                                 int notificationSmallIcon,
                                                 int notificationLargeIcon,
                                                 String notificationTitle,
                                                 String notificationText)
    {
        int playPauseIcon;
        PendingIntent playPauseAction;

        if (playbackStatus == PlaybackStatus.PLAYING)
        {
            playPauseIcon = android.R.drawable.ic_media_pause;
            playPauseAction = createPlaybackAction(1);
        }
        else
        {
            playPauseIcon = android.R.drawable.ic_media_play;
            playPauseAction = createPlaybackAction(0);
        }

        Intent actionIntent = new Intent(this, MainActivity.class);
        actionIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent notificationActionIntent = PendingIntent.getActivity(this, 0, actionIntent, 0);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setShowWhen(false)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSession.getSessionToken())
                        .setShowActionsInCompactView(0, 1, 2))
                .setSmallIcon(notificationSmallIcon)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), notificationLargeIcon))
                .setContentTitle(notificationTitle)
                .setContentText(notificationText)
                .setContentIntent(notificationActionIntent)
                .addAction(android.R.drawable.ic_media_previous, "previous", createPlaybackAction(4))
                .addAction(playPauseIcon, "pause", playPauseAction)
                .addAction(android.R.drawable.ic_media_next, "next", createPlaybackAction(3));

        return notificationBuilder.build();
    }

    private PendingIntent createPlaybackAction(int actionNumber)
    {
        Intent playbackAction = new Intent(this, MediaService.class);

        switch (actionNumber)
        {
            case 0:
                playbackAction.setAction(ACTION_PLAY);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 1:
                playbackAction.setAction(ACTION_PAUSE);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 2:
                playbackAction.setAction(ACTION_RESUME);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 3:
                playbackAction.setAction(ACTION_NEXT);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 4:
                playbackAction.setAction(ACTION_PREVIOUS);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            default:
                break;
        }

        return null;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();

        try
        {
            initializeMediaSession();
        }
        catch (RemoteException ex)
        {
            ex.printStackTrace();
            stopSelf();
        }

        NotificationHelper.createNotificationChannel(
                this,
                NOTIFICATION_CHANNEL_ID,
                "Serviço de mídia DroidPlayer",
                "Notificações do serviço de mídia do DroidPlayer",
                NotificationManager.IMPORTANCE_LOW);

        Notification mediaNotification = createMediaNotification(
                PlaybackStatus.PAUSED,
                R.drawable.play,
                R.drawable.cover,
                "Nenhuma música",
                "Nenhum artista");

        NotificationHelper.showNotification(this, R.integer.media_service_notification_id, mediaNotification);
        stopForeground(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        handleIncomingActions(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        if (mediaPlayer != null)
        {
            stopMedia();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        removeAudioFocus();

        stopForeground(true);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return binder;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int percent)
    {

    }

    @Override
    public void onSeekComplete(MediaPlayer mediaPlayer)
    {
        if (!requestAudioFocus())
        {
            stopSelf();
        }

        mediaPlayer.start();
        playbackStatus = PlaybackStatus.PLAYING;

        MediaInfo currentMediaInfo = getCurrentMediaInfo();

        Notification mediaNotification = createMediaNotification(
                PlaybackStatus.PLAYING,
                R.drawable.play,
                R.drawable.cover,
                currentMediaInfo.getTitle(),
                currentMediaInfo.getArtist());

        startForeground(R.integer.media_service_notification_id, mediaNotification);

        mediaStateChangedEventDispatcher.dispatch(MediaStateChangedEventDispatcher.MEDIA_PLAY, currentMediaInfo);
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer)
    {
        if (!requestAudioFocus())
        {
            stopSelf();
        }

        mediaPlayer.start();
        playbackStatus = PlaybackStatus.PLAYING;

        MediaInfo currentMediaInfo = getCurrentMediaInfo();

        Notification mediaNotification = createMediaNotification(
                PlaybackStatus.PLAYING,
                R.drawable.play,
                R.drawable.cover,
                currentMediaInfo.getTitle(),
                currentMediaInfo.getArtist());

        startForeground(R.integer.media_service_notification_id, mediaNotification);

        mediaStateChangedEventDispatcher.dispatch(MediaStateChangedEventDispatcher.MEDIA_PLAY, currentMediaInfo);
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer)
    {
        nextMedia();
    }

    @Override
    public void onAudioFocusChange(int focusState)
    {
        switch (focusState)
        {
            case AudioManager.AUDIOFOCUS_GAIN:
            {
                if (mediaPlayer == null)
                    initializeMediaPlayer();

                if(!mediaPlayer.isPlaying())
                {
                    mediaPlayer.setVolume(1.0f, 1.0f);
                    mediaPlayer.start();
                }
                break;
            }
            case AudioManager.AUDIOFOCUS_LOSS:
            {
                if (mediaPlayer.isPlaying())
                {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer = null;
                }
                break;
            }
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
            {
                if (mediaPlayer.isPlaying())
                    mediaPlayer.pause();
                break;
            }
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
            {
                if (mediaPlayer.isPlaying())
                    mediaPlayer.setVolume(0.1f, 0.1f);
                break;
            }
        }
    }

    @Override
    public boolean onInfo(MediaPlayer mediaPlayer, int what, int extra)
    {
        return false;
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int what, int extra)
    {
        switch (what)
        {
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                AppLogger.logError("MediaPlayer Error", new Exception("MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK " + extra));
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                AppLogger.logError("MediaPlayer Error", new Exception("MEDIA ERROR SERVER DIED " + extra));
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                AppLogger.logError("MediaPlayer Error", new Exception("MEDIA ERROR UNKNOWN " + extra));
                break;
        }

        return false;
    }
}
