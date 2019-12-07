package br.com.ltoscano.droidplayer.ui.fragment.player.list;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import br.com.ltoscano.droidplayer.R;
import br.com.ltoscano.droidplayer.media.info.MediaInfo;

public class AvailableListAdapter extends RecyclerView.Adapter<AvailableListItemHolder>
{
    private Activity activity;
    private Map<String, MediaInfo> mediaInfoMap;

    public AvailableListAdapter(Activity activity)
    {
        this.activity = activity;
        this.mediaInfoMap = new LinkedHashMap<>();
    }

    public void addMediaInfo(MediaInfo mediaInfo)
    {
        if(!mediaInfoMap.containsKey(mediaInfo.getTitle()))
        {
            mediaInfoMap.put(mediaInfo.getTitle(), mediaInfo);
            notifyDataSetChanged();
        }
    }

    public void clearAllMediaInfo()
    {
        mediaInfoMap.clear();
    }

    public List<MediaInfo> getMediaInfoList()
    {
        return new ArrayList<>(mediaInfoMap.values());
    }

    @NonNull
    @Override
    public AvailableListItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.available_list_item, parent, false);
        activity.registerForContextMenu(itemView);
        return new AvailableListItemHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull AvailableListItemHolder holder, int position)
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
