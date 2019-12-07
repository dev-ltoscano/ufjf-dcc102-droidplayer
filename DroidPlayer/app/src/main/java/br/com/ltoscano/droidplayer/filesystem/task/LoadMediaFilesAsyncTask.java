package br.com.ltoscano.droidplayer.filesystem.task;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import br.com.ltoscano.droidplayer.R;
import br.com.ltoscano.droidplayer.app.exception.AlreadyExistsException;
import br.com.ltoscano.droidplayer.app.exception.InvalidPathException;
import br.com.ltoscano.droidplayer.app.exception.NotAvailableException;
import br.com.ltoscano.droidplayer.app.exception.NotFoundException;
import br.com.ltoscano.droidplayer.filesystem.FileSystem;
import br.com.ltoscano.droidplayer.filesystem.descriptor.FileDescriptor;
import br.com.ltoscano.droidplayer.media.info.MediaInfo;
import br.com.ltoscano.droidplayer.app.log.AppLogger;
import br.com.ltoscano.droidplayer.ui.fragment.player.PlayerFragment;

public class LoadMediaFilesAsyncTask extends AsyncTask<Void, Void, Void>
{
    private Context context;
    private ProgressDialog progressDialog;

    public LoadMediaFilesAsyncTask(Context context)
    {
        this.context = context;
    }

    private List<MediaInfo> loadAudioList()
    {
        List<MediaInfo> mediaInfoList = new ArrayList<>();

        ContentResolver contentResolver = context.getContentResolver();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";

        Cursor cursor = contentResolver.query(uri, null, selection, null, sortOrder);

        if ((cursor != null) && (cursor.getCount() > 0))
        {
            while (cursor.moveToNext())
            {
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));

                mediaInfoList.add(new MediaInfo(title, album, artist, path));
            }
        }

        cursor.close();

        return mediaInfoList;
    }

    @Override
    protected void onPreExecute()
    {
        super.onPreExecute();

        progressDialog = new ProgressDialog(context, R.style.alert_dialog_style);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setTitle("Por favor, aguarde...");
        progressDialog.setMessage("Carregando músicas");
        progressDialog.show();
    }

    @Override
    protected Void doInBackground(Void... voids)
    {
        FileSystem fileSystem = FileSystem.getInstance();

        SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.app_preferences), Context.MODE_PRIVATE);

        if(preferences.getBoolean("FirstRun", true))
        {
            try
            {
                fileSystem.mkdir("/music");
                fileSystem.mkdir("/cover");
            }
            catch (InvalidPathException ex)
            {
                AppLogger.logError(ex.getMessage(), ex);
            }
            catch (AlreadyExistsException ex)
            {
                AppLogger.logError(ex.getMessage(), ex);
            }
        }

        List<MediaInfo> mediaInfoList = loadAudioList();

        for(MediaInfo mediaInfo : mediaInfoList)
        {
            String musicPath =
                    "/music"
                            + File.separator
                            + mediaInfo.getTitle()
                            + ".mp3";

            String musicCachePath =
                    fileSystem.getFileSystemDescriptor().getCacheDirectoryPath()
                            + File.separator
                            + mediaInfo.getTitle()
                            + ".mp3";

            try
            {
                FileDescriptor fileDescriptor = fileSystem.touch(musicPath);

                fileDescriptor.addAttribute("musicTitle", mediaInfo.getTitle());
                fileDescriptor.addAttribute("musicAlbum", mediaInfo.getAlbum());
                fileDescriptor.addAttribute("musicArtist", mediaInfo.getArtist());
                fileDescriptor.addAttribute("musicCachePath", musicCachePath);

                fileSystem.split(mediaInfo.getPath(), musicPath);
                fileSystem.merge(musicPath, musicCachePath);
            }
            catch (InvalidPathException ex)
            {
                AppLogger.logError(ex.getMessage(), ex);
            }
            catch (NotFoundException ex)
            {
                AppLogger.logError(ex.getMessage(), ex);
            }
            catch (AlreadyExistsException ex)
            {
                AppLogger.logError(ex.getMessage(), ex);
            }
            catch (IOException ex)
            {
                AppLogger.logError(ex.getMessage(), ex);
            }
            catch (NotAvailableException ex)
            {
                AppLogger.logError(ex.getMessage(), ex);
            }
        }

        try
        {
            fileSystem.save(context.getFilesDir().getPath().toString() + "/fs/droidplayer_fs.json");

            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("FirstRun", false);
            editor.commit();
        }
        catch (IOException ex)
        {
            AppLogger.logError(ex.getMessage(), ex);
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void voids)
    {
        PlayerFragment.getInstance().loadAudio();
        progressDialog.dismiss();
    }
}