package br.com.ltoscano.droidplayer.ui.fragment.player.list;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import br.com.ltoscano.droidplayer.R;

public class AvailableListItemHolder extends RecyclerView.ViewHolder
{
    private TextView txtMusicTitle;
    private TextView txtMusicArtist;

    public AvailableListItemHolder(@NonNull View itemView)
    {
        super(itemView);

        txtMusicTitle = itemView.findViewById(R.id.music_title);
        txtMusicArtist = itemView.findViewById(R.id.music_artist);
    }

    public String getMusicTitle()
    {
        return txtMusicTitle.getText().toString();
    }

    public void setMusicTitle(String musicTitle)
    {
        txtMusicTitle.setText(musicTitle);
    }

    public String getMusicArtist()
    {
        return txtMusicArtist.getText().toString();
    }

    public void setMusicArtist(String musicArtist)
    {
        txtMusicArtist.setText(musicArtist);
    }
}
