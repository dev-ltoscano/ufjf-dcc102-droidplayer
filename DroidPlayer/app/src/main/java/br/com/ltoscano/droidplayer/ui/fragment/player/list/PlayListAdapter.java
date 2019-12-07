package br.com.ltoscano.droidplayer.ui.fragment.player.list;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.LinkedHashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import br.com.ltoscano.droidplayer.R;
import br.com.ltoscano.droidplayer.ui.fragment.player.PlayerFragment;
import br.com.ltoscano.droidplayer.media.event.dispatcher.PlaylistChangedEventDispatcher;
import br.com.ltoscano.droidplayer.media.event.listener.IPlaylistChangedEventListener;
import br.com.ltoscano.droidplayer.media.event.dispatcher.PlaylistChangedEventDispatcher.PlaylistChangedEventType;
import br.com.ltoscano.droidplayer.media.info.MediaInfo;
import br.com.ltoscano.droidplayer.media.service.MediaService;

public class PlayListAdapter extends RecyclerView.Adapter<PlayListItemHolder>
{
    private MediaService mediaService;
    private Map<String, MediaInfo> mediaInfoMap;
    private PlaylistChangedEventDispatcher playlistChangedEventDispatcher;

    public PlayListAdapter(MediaService mediaService)
    {
        this.mediaService = mediaService;
        this.mediaInfoMap = new LinkedHashMap<>();
        this.playlistChangedEventDispatcher = new PlaylistChangedEventDispatcher();
    }

    public void registerPlaylistChangedEventListener(IPlaylistChangedEventListener listener)
    {
        playlistChangedEventDispatcher.registerEventListener(listener);
    }

    public void unregisterPlaylistChangedEventListener(IPlaylistChangedEventListener listener)
    {
        playlistChangedEventDispatcher.unregisterEventListener(listener);
    }

    public void addMediaInfo(MediaInfo mediaInfo)
    {
        if(!mediaInfoMap.containsKey(mediaInfo.getTitle()))
        {
            mediaInfoMap.put(mediaInfo.getTitle(), mediaInfo);
            notifyDataSetChanged();
        }
    }

    public void removeMediaInfo(String mediaTitle)
    {
        if(mediaInfoMap.containsKey(mediaTitle))
        {
            playlistChangedEventDispatcher.dispatch(PlaylistChangedEventType.MEDIA_REMOVED_FROM_PLAYLIST.ordinal(), mediaInfoMap.remove(mediaTitle));
            notifyDataSetChanged();
        }
    }

    public void clearAllMediaInfo()
    {
        mediaInfoMap.clear();
    }

    @NonNull
    @Override
    public PlayListItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        final View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.media_list_item, parent, false);

        itemView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                String musicTitle = ((TextView)itemView.findViewById(R.id.music_title)).getText().toString();

                MediaInfo mediaInfo = mediaInfoMap.get(musicTitle);
                PlayerFragment.getInstance().playMedia(mediaInfo);
            }
        });

        return new PlayListItemHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PlayListItemHolder holder, int position)
    {
        String mediaInfoKey = mediaInfoMap.keySet().toArray(new String[0])[position];
        MediaInfo mediaInfo = mediaInfoMap.get(mediaInfoKey);

        holder.setMusicTitle(mediaInfo.getTitle());
        holder.setMusicArtist(mediaInfo.getArtist());
    }

    @Override
    public int getItemCount()
    {
        return mediaInfoMap.size();
    }
}
