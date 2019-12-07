package br.com.ltoscano.droidplayer.filesystem.task;

import android.os.AsyncTask;

import java.util.Collection;

import br.com.ltoscano.droidplayer.app.exception.InvalidPathException;
import br.com.ltoscano.droidplayer.app.exception.NotFoundException;
import br.com.ltoscano.droidplayer.app.exception.RoutingException;
import br.com.ltoscano.droidplayer.app.log.AppLogger;
import br.com.ltoscano.droidplayer.filesystem.FileSystem;
import br.com.ltoscano.droidplayer.filesystem.descriptor.BlockDescriptor;
import br.com.ltoscano.droidplayer.filesystem.descriptor.FileDescriptor;
import br.com.ltoscano.droidplayer.media.info.MediaInfo;
import br.com.ltoscano.droidplayer.network.info.MessageInfo;
import br.com.ltoscano.droidplayer.network.service.NetworkService;
import br.com.ltoscano.droidplayer.ui.fragment.player.PlayerFragment;

public class StreamAsyncTask extends AsyncTask<Void, Void, Void>
{
    private MediaInfo mediaInfo;

    public StreamAsyncTask(MediaInfo mediaInfo)
    {
        this.mediaInfo = mediaInfo;
    }

    @Override
    protected Void doInBackground(Void... voids)
    {
        PlayerFragment playerFragment = PlayerFragment.getInstance();
        playerFragment.clearMedia();

        String musicPath = "/music/" + mediaInfo.getTitle();

        FileSystem fileSystem = FileSystem.getInstance();
        NetworkService networkService = NetworkService.getInstance();

        try
        {
            FileDescriptor fileDescriptor = fileSystem.getFileSystemDescriptor().getFile(musicPath);
            Collection<BlockDescriptor> blockDescriptorList = fileDescriptor.getBlockDescriptorList();

            for(BlockDescriptor blockDescriptor : blockDescriptorList)
            {
                MessageInfo messageInfo = new MessageInfo("FILESYSTEM_GET_BLOCK");
                messageInfo.setParam("FILE_ID", fileDescriptor.getId());
                messageInfo.setParam("BLOCK_ID", blockDescriptor.getId());

                try
                {
                    networkService.sendMessage(messageInfo);
                }
                catch (RoutingException ex)
                {
                    AppLogger.logError(ex.getMessage(), ex);
                }
            }
        }
        catch (InvalidPathException ex)
        {
            AppLogger.logError(ex.getMessage(), ex);
        }
        catch (NotFoundException ex)
        {
            AppLogger.logError(ex.getMessage(), ex);
        }

        return null;
    }
}
