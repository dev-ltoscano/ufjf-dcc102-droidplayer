package br.com.ltoscano.droidplayer.media.info;

import java.io.Serializable;

public class MediaInfo implements Serializable
{
    private String title;
    private String album;
    private String artist;
    private String path;

    public MediaInfo(String title, String album, String artist, String path)
    {
        this.title = title;
        this.album = album;
        this.artist = artist;
        this.path = path;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
