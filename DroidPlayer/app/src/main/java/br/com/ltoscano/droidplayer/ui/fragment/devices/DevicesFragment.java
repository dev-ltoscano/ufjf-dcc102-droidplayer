package br.com.ltoscano.droidplayer.ui.fragment.devices;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Collection;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import br.com.ltoscano.droidplayer.R;
import br.com.ltoscano.droidplayer.network.event.listener.IConnectionEventListener;
import br.com.ltoscano.droidplayer.network.event.listener.IDiscoveryEventListener;
import br.com.ltoscano.droidplayer.network.info.EndpointConnectionInfo;
import br.com.ltoscano.droidplayer.network.info.EndpointInfo;
import br.com.ltoscano.droidplayer.ui.fragment.devices.list.DeviceListAdapter;
import br.com.ltoscano.droidplayer.network.service.NetworkService;

public class DevicesFragment extends Fragment
    implements IDiscoveryEventListener, IConnectionEventListener
{
    private DeviceListAdapter deviceListAdapter;
    private RecyclerView deviceListView;
    private TextView txtEmptyDeviceList;

    private NetworkService networkService;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        networkService = NetworkService.getInstance();

        networkService.registerDiscoveryEventListener(this);
        networkService.registerConnectionEventListener(this);
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View root = inflater.inflate(R.layout.devices_fragment, container, false);

        txtEmptyDeviceList = root.findViewById(R.id.txtEmptyDeviceList);

        deviceListView = root.findViewById(R.id.deviceListView);
        deviceListView.addItemDecoration(new DividerItemDecoration(root.getContext(), DividerItemDecoration.VERTICAL));
        deviceListView.setLayoutManager(new LinearLayoutManager(root.getContext()));

        deviceListAdapter = new DeviceListAdapter();
        deviceListView.setAdapter(deviceListAdapter);

        Collection<EndpointInfo> endpointInfoList = networkService.getEndpointInfoList();

        if(!endpointInfoList.isEmpty())
        {
            txtEmptyDeviceList.setVisibility(View.GONE);
            deviceListView.setVisibility(View.VISIBLE);

            for(EndpointInfo endpointInfo : endpointInfoList)
            {
                deviceListAdapter.addPeerInfo(endpointInfo);
            }
        }

        return root;
    }

    @Override
    public void onPause()
    {
        super.onPause();

        networkService.unregisterDiscoveryEventListener(this);
        networkService.unregisterConnectionEventListener(this);
    }

    @Override
    public void onResume()
    {
        super.onResume();

        networkService.registerDiscoveryEventListener(this);
        networkService.registerConnectionEventListener(this);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        networkService.unregisterDiscoveryEventListener(this);
        networkService.unregisterConnectionEventListener(this);
    }

    @Override
    public void onEndpointFound(final EndpointInfo endpointInfo)
    {
        getActivity().runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                deviceListAdapter.addPeerInfo(endpointInfo);

                if(deviceListAdapter.getItemCount() > 0)
                {
                    txtEmptyDeviceList.setVisibility(View.GONE);
                    deviceListView.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public void onEndpointLost(final String endpointId)
    {
        getActivity().runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                deviceListAdapter.removePeerInfo(endpointId);

                if(deviceListAdapter.getItemCount() == 0)
                {
                    txtEmptyDeviceList.setVisibility(View.VISIBLE);
                    deviceListView.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void onConnectionInstantiated(final EndpointConnectionInfo endpointConnectionInfo)
    {

    }

    @Override
    public void onConnectionResult(final EndpointConnectionInfo endpointConnectionInfo)
    {
        getActivity().runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                deviceListAdapter.updateConnectionStatus(endpointConnectionInfo);
            }
        });
    }

    @Override
    public void onDisconnected(final EndpointConnectionInfo endpointConnectionInfo)
    {
        getActivity().runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                deviceListAdapter.updateConnectionStatus(endpointConnectionInfo);
            }
        });
    }
}