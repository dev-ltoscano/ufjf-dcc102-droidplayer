package br.com.ltoscano.droidplayer.filesystem.descriptor;

import java.util.ArrayList;
import java.util.List;

import br.com.ltoscano.droidplayer.filesystem.helper.IdHelper;
import br.com.ltoscano.droidplayer.network.info.EndpointInfo;

public class BlockDescriptor
{
    private final String id;

    private String localPath;
    private final int size;
    private final int offset;

    private List<EndpointInfo> endpointInfoList;

    public BlockDescriptor(int size, int offset)
    {
        this(null, size, offset);
    }

    public BlockDescriptor(String localPath, int size, int offset)
    {
        this.id = IdHelper.generate();

        this.localPath = localPath;
        this.size = size;
        this.offset = offset;

        this.endpointInfoList = new ArrayList<>();
    }

    public String getId()
    {
        return id;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public int getSize()
    {
        return size;
    }

    public int getOffset()
    {
        return offset;
    }

    public List<EndpointInfo> getEndpointInfoList() {
        return endpointInfoList;
    }

    public void setEndpointInfoList(List<EndpointInfo> endpointInfoList) {
        this.endpointInfoList = endpointInfoList;
    }
}
