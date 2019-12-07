package br.com.ltoscano.droidplayer.ui.fragment.devices.list;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import br.com.ltoscano.droidplayer.R;
import br.com.ltoscano.droidplayer.network.info.EndpointConnectionInfo;
import br.com.ltoscano.droidplayer.network.info.EndpointInfo;
import br.com.ltoscano.droidplayer.app.log.AppLogger;
import br.com.ltoscano.droidplayer.network.nearby.NearbyConnectionsManager;

public class DeviceListAdapter extends RecyclerView.Adapter<DeviceListItemHolder>
{
    private Map<String, EndpointInfo> endpointInfoMap;

    public DeviceListAdapter()
    {
        this.endpointInfoMap = new HashMap<>();
    }

    public void addPeerInfo(EndpointInfo endpointInfo)
    {
        AppLogger.logDebug("DeviceListAdapter.addPeerInfo()");

        if(!endpointInfoMap.containsKey(endpointInfo.getEndpointId()))
        {
            endpointInfoMap.put(endpointInfo.getEndpointId(), endpointInfo);
            notifyDataSetChanged();
        }
    }

    public void updateConnectionStatus(EndpointConnectionInfo endpointConnectionInfo)
    {
        AppLogger.logDebug("DeviceListAdapter.updatePeerInfo()");

        if(endpointInfoMap.containsKey(endpointConnectionInfo.getEndpointId()))
        {
            endpointInfoMap.get(endpointConnectionInfo.getEndpointId()).getEndpointConnectionInfo().setConnected(endpointConnectionInfo.isConnected());
            notifyDataSetChanged();
        }
    }

    public void removePeerInfo(String endpointId)
    {
        AppLogger.logDebug("DeviceListAdapter.removePeerInfo()");

        if(endpointInfoMap.containsKey(endpointId))
        {
            endpointInfoMap.remove(endpointId);
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public DeviceListItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        final View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.device_list_item, parent, false);

        itemView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                String remoteEndpointId = ((TextView)itemView.findViewById(R.id.txtPeerId)).getText().toString();

                EndpointInfo remoteEndpointInfo = NearbyConnectionsManager.getInstance().getEndpointInfo(remoteEndpointId);

                if(remoteEndpointInfo.getEndpointConnectionInfo().isConnected())
                {
                    NearbyConnectionsManager.getInstance().disconnect(remoteEndpointId);

                    remoteEndpointInfo.getEndpointConnectionInfo().setConnected(false);
                    notifyDataSetChanged();
                }
                else
                {
                    NearbyConnectionsManager.getInstance().requestConnection(remoteEndpointId);
                }
            }
        });

        return new DeviceListItemHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceListItemHolder holder, int position)
    {
        EndpointInfo endpointInfo = (EndpointInfo)(endpointInfoMap.values().toArray()[position]);

        holder.setPeerId(endpointInfo.getEndpointId());
        holder.setPeerName(endpointInfo.getEndpointName());
        holder.setServiceId(endpointInfo.getServiceId());

        if(endpointInfo.getEndpointConnectionInfo() != null)
        {
            holder.setPeerStatus(endpointInfo.getEndpointConnectionInfo().isConnected());
        }
        else
        {
            holder.setPeerStatus(false);
        }
    }

    @Override
    public int getItemCount()
    {
        return endpointInfoMap.size();
    }
}
