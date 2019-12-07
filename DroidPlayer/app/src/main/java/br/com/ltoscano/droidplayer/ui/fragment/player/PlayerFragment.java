package br.com.ltoscano.droidplayer.ui.fragment.player;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.chibde.visualizer.CircleBarVisualizer;

import java.io.File;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import br.com.ltoscano.droidplayer.app.exception.InvalidPathException;
import br.com.ltoscano.droidplayer.app.exception.NotFoundException;
import br.com.ltoscano.droidplayer.filesystem.FileSystem;
import br.com.ltoscano.droidplayer.filesystem.descriptor.FileDescriptor;
import br.com.ltoscano.droidplayer.app.log.AppLogger;
import br.com.ltoscano.droidplayer.ui.fragment.player.list.AvailableListAdapter;
import br.com.ltoscano.droidplayer.ui.fragment.player.list.PlayListAdapter;
import br.com.ltoscano.droidplayer.ui.fragment.player.list.SwipeToDeleteCallback;
import br.com.ltoscano.droidplayer.media.event.listener.IPlaylistChangedEventListener;
import br.com.ltoscano.droidplayer.media.event.listener.IMediaStateChangedEventListener;
import br.com.ltoscano.droidplayer.media.helper.MediaHelper;
import br.com.ltoscano.droidplayer.media.service.MediaService;
import br.com.ltoscano.droidplayer.R;
import br.com.ltoscano.droidplayer.app.helper.permission.PermissionHelper;
import br.com.ltoscano.droidplayer.media.info.MediaInfo;

public class PlayerFragment extends Fragment
        implements IMediaStateChangedEventListener, IPlaylistChangedEventListener
{
    private static PlayerFragment instance;

    private Context context;

    private MediaService mediaService;
    private boolean serviceBound;

    private ImageView imgMusicAlbum;
    private TextView txtMusicTitle;
    private TextView txtMusicArtist;

    private TextView txtMusicCurrentPosition;
    private TextView txtMusicTotalLength;
    private SeekBar seekBarCurrentMusic;
    private Handler mediaDurationHandler;

    private ImageButton btnPlay;
    private ImageButton btnPrevious;
    private ImageButton btnNext;
    private CircleBarVisualizer circleBarVisualizer;

    private AvailableListAdapter availableListAdapter;
    private RecyclerView availableListView;
    private TextView txtEmptyAvailableList;
    private boolean showingAvailableList;

    private PlayListAdapter playListAdapter;
    private RecyclerView playListView;
    private TextView txtEmptyPlayList;
    private boolean showingPlayList;

    private ImageButton btnPlayListAdd;
    private ImageButton btnPlayList;

    private ServiceConnection playerServiceConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            MediaService.LocalBinder binder = (MediaService.LocalBinder) service;
            mediaService = binder.getService();
            serviceBound = true;

            mediaService.registerMediaStateChangedEventListener(PlayerFragment.this);

            if(mediaService.getPlaybackStatus() == MediaService.PlaybackStatus.PLAYING)
            {
                List<MediaInfo> mediaInfoList = mediaService.getMediaInfoList();

                for(MediaInfo mediaInfo : mediaInfoList)
                {
                    playListAdapter.addMediaInfo(mediaInfo);
                }

                btnPlay.setImageResource(R.drawable.pause);

                MediaInfo mediaInfo = mediaService.getCurrentMediaInfo();

                txtMusicTitle.setText(mediaInfo.getTitle());
                txtMusicArtist.setText(mediaInfo.getArtist());

                int musicDuration = mediaService.getMediaPlayer().getDuration();
                txtMusicTotalLength.setText(MediaHelper.formatMilliseconds(musicDuration));

                if(PermissionHelper.isPermissionGranted(context, Manifest.permission.RECORD_AUDIO))
                {
                    enableAudioVisualizer(true);
                }
                else
                {
                    enableAudioVisualizer(false);
                }
            }
            else
            {
                List<MediaInfo> mediaInfoList = mediaService.getMediaInfoList();

                if(mediaInfoList.isEmpty())
                {
                    if(PermissionHelper.isPermissionGranted(context, Manifest.permission.READ_EXTERNAL_STORAGE))
                    {
                        loadAudio();
                    }
                }
                else
                {
                    for(MediaInfo mediaInfo : mediaInfoList)
                    {
                        playListAdapter.addMediaInfo(mediaInfo);
                    }
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name)
        {
            mediaService = null;
            serviceBound = false;
        }
    };

    private void bindPlayerService()
    {
        if (!serviceBound)
        {
            Intent playerIntent = new Intent(context, MediaService.class);
            context.startService(playerIntent);
            context.bindService(playerIntent, playerServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    public static PlayerFragment getInstance()
    {
        return instance;
    }

    public void enableAudioVisualizer(boolean enable)
    {
        if(enable)
        {
            circleBarVisualizer.setColor(ContextCompat.getColor(context, R.color.white));
            circleBarVisualizer.setPlayer(mediaService.getMediaPlayer().getAudioSessionId());
            circleBarVisualizer.setVisibility(View.VISIBLE);
        }
        else
        {
            circleBarVisualizer.setVisibility(View.GONE);
        }
    }

    public void loadAudio()
    {
        playListAdapter.clearAllMediaInfo();
        mediaService.clearMediaInfoList();

        FileSystem fileSystem = FileSystem.getInstance();

        try
        {
            List<FileDescriptor> fileDescriptorList = fileSystem.ls("/music");

            for(FileDescriptor fileDescriptor : fileDescriptorList)
            {
                if(fileDescriptor.isAvailable())
                {
                    String title = fileDescriptor.getAttribute("musicTitle");
                    String album = fileDescriptor.getAttribute("musicAlbum");
                    String artist = fileDescriptor.getAttribute("musicArtist");
                    String musicCachePath = fileDescriptor.getAttribute("musicCachePath");

                    File musicFile = new File(musicCachePath);

                    if(musicFile.exists())
                    {
                        MediaInfo mediaInfo = new MediaInfo(title, album, artist, musicCachePath);

                        playListAdapter.addMediaInfo(mediaInfo);
                        mediaService.addMediaInfo(mediaInfo);
                    }
                }
            }
        }
        catch (NotFoundException ex)
        {
            AppLogger.logError(ex.getMessage(), ex);
        }
        catch (InvalidPathException ex)
        {
            AppLogger.logError(ex.getMessage(), ex);
        }
        finally
        {
            if(showingPlayList)
            {
                if(playListAdapter.getItemCount() != 0)
                {
                    txtEmptyPlayList.setVisibility(View.GONE);
                    playListView.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    public void loadAvailableAudio()
    {
        availableListAdapter.clearAllMediaInfo();

        FileSystem fileSystem = FileSystem.getInstance();

        try
        {
            List<FileDescriptor> fileDescriptorList = fileSystem.ls("/music");

            for(FileDescriptor fileDescriptor : fileDescriptorList)
            {
                if(!fileDescriptor.isAvailable())
                {
                    String title = fileDescriptor.getAttribute("musicTitle");
                    String album = fileDescriptor.getAttribute("musicAlbum");
                    String artist = fileDescriptor.getAttribute("musicArtist");

                    availableListAdapter.addMediaInfo(new MediaInfo(title, album, artist, null));
                }
            }
        }
        catch (NotFoundException ex)
        {
            AppLogger.logError(ex.getMessage(), ex);
        }
        catch (InvalidPathException ex)
        {
            AppLogger.logError(ex.getMessage(), ex);
        }
    }

    public List<MediaInfo> getAvailableMediaList()
    {
        return availableListAdapter.getMediaInfoList();
    }

    public void playMedia()
    {
        if(!mediaService.mediaInfoListIsEmpty())
        {
            mediaService.playMedia();
            btnPlay.setImageResource(R.drawable.pause);
        }
    }

    public void playMedia(MediaInfo mediaInfo)
    {
        mediaService.playMedia(mediaInfo);
        btnPlay.setImageResource(R.drawable.pause);
    }

    public void pauseMedia()
    {
        mediaService.pauseMedia();
        btnPlay.setImageResource(R.drawable.play);
    }

    public void resumeMedia()
    {
        if(!mediaService.mediaInfoListIsEmpty())
        {
            mediaService.resumeMedia();
            btnPlay.setImageResource(R.drawable.pause);
        }
    }

    public void previousMedia()
    {
        mediaService.previousMedia();

        if(mediaService.getPlaybackStatus() == MediaService.PlaybackStatus.PLAYING)
        {
            btnPlay.setImageResource(R.drawable.pause);
        }
    }

    public void nextMedia()
    {
        mediaService.nextMedia();

        if(mediaService.getPlaybackStatus() == MediaService.PlaybackStatus.PLAYING)
        {
            btnPlay.setImageResource(R.drawable.pause);
        }
    }

    public void clearMedia()
    {
        mediaService.clearMediaInfoList();
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View root = inflater.inflate(R.layout.player_fragment, container, false);

        imgMusicAlbum = root.findViewById(R.id.music_cover);
        txtMusicTitle = root.findViewById(R.id.music_title);
        txtMusicArtist = root.findViewById(R.id.music_artist);

        txtMusicCurrentPosition = root.findViewById(R.id.music_current_position);
        txtMusicTotalLength = root.findViewById(R.id.music_total_length);
        seekBarCurrentMusic = root.findViewById(R.id.music_seek_bar);
        circleBarVisualizer = root.findViewById(R.id.audio_visualizer);
        txtEmptyPlayList = root.findViewById(R.id.empty_playlist);

        mediaDurationHandler = new Handler();

        seekBarCurrentMusic.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int position, boolean fromUser)
            {
                if(fromUser)
                {
                    mediaService.skipToMedia(position);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {

            }
        });

        btnPlay = root.findViewById(R.id.btn_play);
        btnPlay.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(mediaService.getPlaybackStatus() == MediaService.PlaybackStatus.STOPPED)
                {
                    playMedia();
                }
                else
                {
                    if(mediaService.getPlaybackStatus() == MediaService.PlaybackStatus.PAUSED)
                    {
                        resumeMedia();
                    }
                    else
                    {
                        pauseMedia();
                    }
                }
            }
        });

        btnPrevious = root.findViewById(R.id.btn_previous);
        btnPrevious.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                previousMedia();
            }
        });

        btnNext = root.findViewById(R.id.btn_next);
        btnNext.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                nextMedia();
            }
        });

        availableListAdapter = new AvailableListAdapter(getActivity());
        availableListView = root.findViewById(R.id.available_list_view);
        availableListView.setLayoutManager(new LinearLayoutManager(context));
        availableListView.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));
        availableListView.setAdapter(availableListAdapter);
        txtEmptyAvailableList = root.findViewById(R.id.empty_available_list);
        showingAvailableList = false;

        playListView = root.findViewById(R.id.playlist);
        playListView.setLayoutManager(new LinearLayoutManager(context));
        playListView.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));

        playListAdapter = new PlayListAdapter(mediaService);
        playListView.setAdapter(playListAdapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SwipeToDeleteCallback(playListView.getContext(), playListAdapter));
        itemTouchHelper.attachToRecyclerView(playListView);

        btnPlayListAdd = root.findViewById(R.id.btn_playlist_add);
        btnPlayListAdd.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                imgMusicAlbum.setVisibility(View.GONE);

                txtEmptyPlayList.setVisibility(View.GONE);
                playListAdapter.unregisterPlaylistChangedEventListener(PlayerFragment.this);
                playListView.setVisibility(View.GONE);
                btnPlayList.setImageResource(R.drawable.playlist_current);
                showingPlayList = false;

                if(!showingAvailableList)
                {
                    loadAvailableAudio();

                    if(availableListAdapter.getItemCount() == 0)
                    {
                        txtEmptyAvailableList.setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        availableListView.setVisibility(View.VISIBLE);
                    }

                    showingAvailableList = true;
                }
                else
                {
                    imgMusicAlbum.setVisibility(View.VISIBLE);

                    txtEmptyAvailableList.setVisibility(View.GONE);
                    availableListView.setVisibility(View.GONE);

                    showingAvailableList = false;
                }
            }
        });

        btnPlayList = root.findViewById(R.id.btn_playlist_current);
        btnPlayList.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                imgMusicAlbum.setVisibility(View.GONE);

                txtEmptyAvailableList.setVisibility(View.GONE);
                availableListView.setVisibility(View.GONE);
                showingAvailableList = false;

                if(!showingPlayList)
                {
                    btnPlayList.setImageResource(R.drawable.playlist_current_active);
                    playListAdapter.registerPlaylistChangedEventListener(PlayerFragment.this);

                    if(playListAdapter.getItemCount() == 0)
                    {
                        txtEmptyPlayList.setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        playListView.setVisibility(View.VISIBLE);
                    }

                    showingPlayList = true;
                }
                else
                {
                    imgMusicAlbum.setVisibility(View.VISIBLE);

                    btnPlayList.setImageResource(R.drawable.playlist_current);

                    txtEmptyPlayList.setVisibility(View.GONE);
                    playListAdapter.unregisterPlaylistChangedEventListener(PlayerFragment.this);
                    playListView.setVisibility(View.GONE);

                    showingPlayList = false;
                }
            }
        });

        return root;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        instance = this;
        context = getContext();
        serviceBound = false;

        bindPlayerService();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        if (serviceBound)
        {
            context.unbindService(playerServiceConnection);
        }
    }

    @Override
    public void onPlayMedia(MediaInfo mediaInfo)
    {
        txtMusicTitle.setText(mediaInfo.getTitle());
        txtMusicArtist.setText(mediaInfo.getArtist());

        int musicDuration = mediaService.getMediaPlayer().getDuration();
        txtMusicTotalLength.setText(MediaHelper.formatMilliseconds(musicDuration));
        seekBarCurrentMusic.setMax(musicDuration);

        if(PermissionHelper.isPermissionGranted(context, Manifest.permission.RECORD_AUDIO))
        {
            enableAudioVisualizer(true);
        }
        else
        {
            enableAudioVisualizer(false);
        }

        mediaDurationHandler.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                int currentMusicPosition = mediaService.getMediaPlayer().getCurrentPosition();
                txtMusicCurrentPosition.setText(MediaHelper.formatMilliseconds(currentMusicPosition));
                seekBarCurrentMusic.setProgress(currentMusicPosition);

                mediaDurationHandler.postDelayed(this, 1000);
            }
        }, 1000);
    }

    @Override
    public void onPauseMedia(MediaInfo mediaInfo)
    {

    }

    @Override
    public void onResumeMedia(MediaInfo mediaInfo)
    {

    }

    @Override
    public void onMediaRemovedFromPlaylist(MediaInfo mediaInfo)
    {
        mediaService.removeMediaInfo(mediaInfo);

        if(mediaService.mediaInfoListIsEmpty())
        {
            MediaInfo emptyMediaInfo = mediaService.getCurrentMediaInfo();

            txtMusicTitle.setText(emptyMediaInfo.getTitle());
            txtMusicArtist.setText(emptyMediaInfo.getArtist());
            txtMusicCurrentPosition.setText(MediaHelper.formatMilliseconds(0));
            txtMusicTotalLength.setText(MediaHelper.formatMilliseconds(0));
            btnPlay.setImageResource(R.drawable.play);

            enableAudioVisualizer(false);

            txtEmptyPlayList.setVisibility(View.VISIBLE);
            playListView.setVisibility(View.GONE);
        }
    }
}