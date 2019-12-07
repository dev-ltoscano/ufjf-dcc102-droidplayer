package br.com.ltoscano.droidplayer.ui.fragment.devices.list;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import br.com.ltoscano.droidplayer.R;

public class DeviceListItemHolder extends RecyclerView.ViewHolder
{
    private TextView txtPeerId;
    private TextView txtPeerName;
    private TextView txtPeerServiceId;
    private TextView txtPeerStatus;

    public DeviceListItemHolder(@NonNull View itemView)
    {
        super(itemView);

        txtPeerId = itemView.findViewById(R.id.txtPeerId);
        txtPeerName = itemView.findViewById(R.id.txtPeerName);
        txtPeerServiceId = itemView.findViewById(R.id.txtPeerServiceID);
        txtPeerStatus = itemView.findViewById(R.id.txtPeerStatus);
    }

    public String getPeerId()
    {
        return txtPeerId.getText().toString();
    }

    public void setPeerId(String peerId)
    {
        txtPeerId.setText(peerId);
    }

    public String getPeerName()
    {
        return txtPeerName.getText().toString();
    }

    public void setPeerName(String peerName)
    {
        txtPeerName.setText(peerName);
    }

    public String getServiceId()
    {
        return txtPeerServiceId.getText().toString();
    }

    public void setServiceId(String serviceId)
    {
        txtPeerServiceId.setText(serviceId);
    }

    public String getPeerStatus()
    {
        return txtPeerStatus.getText().toString();
    }

    public void setPeerStatus(boolean connected)
    {
        if(connected)
        {
            txtPeerStatus.setText("Conectado");
        }
        else
        {
            txtPeerStatus.setText("Disponível");
        }
    }
}
