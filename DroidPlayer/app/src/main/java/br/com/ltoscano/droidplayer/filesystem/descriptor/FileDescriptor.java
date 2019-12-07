package br.com.ltoscano.droidplayer.filesystem.descriptor;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import br.com.ltoscano.droidplayer.app.exception.InvalidPathException;
import br.com.ltoscano.droidplayer.app.exception.NotFoundException;
import br.com.ltoscano.droidplayer.filesystem.helper.IdHelper;
import br.com.ltoscano.droidplayer.app.helper.json.JSONHelper;
import br.com.ltoscano.droidplayer.filesystem.helper.PathHelper;

public class FileDescriptor
{
    private String id;
    private String parentId;

    private String path;
    private String name;
    private long size;
    private final Map<String, String> attributeMap;

    private final long creationTimestamp;
    private long modificationTimestamp;

    private final Map<String, BlockDescriptor> blockDescriptorMap;

    public FileDescriptor() throws InvalidPathException
    {
        this.id = null;
        this.parentId = null;

        this.path = null;
        this.name = null;
        this.size = -1;
        this.attributeMap = null;

        this.creationTimestamp = -1;
        this.modificationTimestamp = -1;

        this.blockDescriptorMap = null;
    }

    public FileDescriptor(String path) throws InvalidPathException
    {
        this.id = IdHelper.generate(path);
        this.parentId = IdHelper.generate(PathHelper.getBasePath(path));

        this.path = path;
        this.name = PathHelper.getName(path);
        this.size = 0;
        this.attributeMap = new HashMap<>();

        this.creationTimestamp = System.currentTimeMillis();
        this.modificationTimestamp = creationTimestamp;

        this.blockDescriptorMap = new HashMap<>();
    }

    public String getId()
    {
        return id;
    }

    public String getParentId() {
        return parentId;
    }

    public String getPath()
    {
        return path;
    }

    public void setPath(String path) throws InvalidPathException
    {
        this.id = IdHelper.generate(path);
        this.parentId = IdHelper.generate(PathHelper.getBasePath(path));

        this.path = path;
        this.name = PathHelper.getName(path);

        updateModificationTimestamp();
    }

    public String getName()
    {
        return name;
    }

    public long getSize()
    {
        return size;
    }

    public boolean isAvailable()
    {
        for(BlockDescriptor blockDescriptor : blockDescriptorMap.values())
        {
            File blockFile = new File(blockDescriptor.getLocalPath());

            if(!blockFile.exists())
            {
                return false;
            }
        }

        return true;
    }

    public void addAttribute(String attName, String attValue)
    {
        attributeMap.put(attName, attValue);
    }

    public void removeAttribute(String attName)
    {
        attributeMap.remove(attName);
    }

    public boolean containsAttribute(String attName)
    {
        return attributeMap.containsKey(attName);
    }

    public String getAttribute(String attName) throws NotFoundException
    {
        if(!containsAttribute(attName))
        {
            throw new NotFoundException("The attribute '" + attName + "' was not found");
        }

        return attributeMap.get(attName);
    }

    public Map<String, String> getAttributes()
    {
        return attributeMap;
    }

    public void addBlockDescriptor(BlockDescriptor blockDescriptor)
    {
        if(!containsBlock(blockDescriptor.getId()))
        {
            blockDescriptorMap.put(blockDescriptor.getId(), blockDescriptor);
            size += blockDescriptor.getSize();

            updateModificationTimestamp();
        }
    }

    public void removeBlockDescriptor(String blockId)
    {
        if(blockDescriptorMap.containsKey(blockId))
        {
            BlockDescriptor blockDescriptor = blockDescriptorMap.remove(blockId);
            size -= blockDescriptor.getSize();

            updateModificationTimestamp();
        }
    }

    public BlockDescriptor getBlockDescriptor(String blockId) throws NotFoundException
    {
        if(!containsBlock(blockId))
        {
            throw new NotFoundException("The block not found");
        }

        return blockDescriptorMap.get(blockId);
    }

    public Map<String, BlockDescriptor> getBlockDescriptorMap()
    {
        return blockDescriptorMap;
    }

    public Collection<BlockDescriptor> getBlockDescriptorList()
    {
        return blockDescriptorMap.values();
    }

    public boolean containsBlock(String blockId)
    {
        return blockDescriptorMap.containsKey(blockId);
    }

    public void clearBlockDescriptorList()
    {
        blockDescriptorMap.clear();
    }

    public long getCreationTimestamp()
    {
        return creationTimestamp;
    }

    public long getModificationTimestamp()
    {
        return modificationTimestamp;
    }

    private void updateModificationTimestamp()
    {
        this.modificationTimestamp = System.currentTimeMillis();
    }

    @Override
    public String toString()
    {
        return JSONHelper.objToJson(this);
    }
}
