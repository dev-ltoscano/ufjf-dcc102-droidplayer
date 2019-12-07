package br.com.ltoscano.droidplayer.filesystem.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.google.android.gms.nearby.connection.Payload;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;

import br.com.ltoscano.droidplayer.R;
import br.com.ltoscano.droidplayer.app.exception.NotFoundException;
import br.com.ltoscano.droidplayer.app.exception.RoutingException;
import br.com.ltoscano.droidplayer.app.log.AppLogger;
import br.com.ltoscano.droidplayer.filesystem.FileSystem;
import br.com.ltoscano.droidplayer.network.info.EndpointInfo;
import br.com.ltoscano.droidplayer.network.info.MessageInfo;
import br.com.ltoscano.droidplayer.network.service.NetworkService;

public class SynchronizeAsyncTask extends AsyncTask<Void, Void, Void>
{
    private Context context;
    private ProgressDialog progressDialog;

    public SynchronizeAsyncTask(Context context)
    {
        this.context = context;
    }

    @Override
    protected void onPreExecute()
    {
        super.onPreExecute();

        progressDialog = new ProgressDialog(context, R.style.alert_dialog_style);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setTitle("Por favor, aguarde...");
        progressDialog.setMessage("Sincronizando");
        progressDialog.show();
    }

    @Override
    protected Void doInBackground(Void... voids)
    {
        try
        {
            FileSystem fileSystem = FileSystem.getInstance();

            File fileSystemFile = fileSystem.getFileSystemFile("droidplayer_fs");
            Payload fileSystemPayload = Payload.fromFile(fileSystemFile);

            NetworkService networkService = NetworkService.getInstance();

            MessageInfo messageInfo = new MessageInfo("FILESYSTEM_SYNC");
            messageInfo.setParam("ID", fileSystemPayload.getId());
            messageInfo.setParam("TIMESTAMP", fileSystem.getFileSystemDescriptor().getModificationTimestamp());

            Collection<EndpointInfo> endpointInfoList = networkService.getEndpointInfoList();

            for(EndpointInfo endpointInfo : endpointInfoList)
            {
                try
                {
                    messageInfo.setParam("TO_ENDPOINT_NAME", endpointInfo.getEndpointName());

                    networkService.sendMessage(messageInfo);
                    networkService.sendPayload(endpointInfo.getEndpointId(), fileSystemPayload);
                }
                catch (RoutingException ex)
                {
                    AppLogger.logError(ex.getMessage(), ex);
                }
            }
        }
        catch (NotFoundException | FileNotFoundException ex)
        {
            AppLogger.logError(ex.getMessage(), ex);
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void voids)
    {
        progressDialog.dismiss();
    }
}
