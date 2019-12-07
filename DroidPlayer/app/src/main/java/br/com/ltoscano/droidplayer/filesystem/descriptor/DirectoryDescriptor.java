package br.com.ltoscano.droidplayer.filesystem.descriptor;

import java.util.ArrayList;
import java.util.List;

import br.com.ltoscano.droidplayer.app.exception.InvalidPathException;
import br.com.ltoscano.droidplayer.filesystem.helper.IdHelper;
import br.com.ltoscano.droidplayer.app.helper.json.JSONHelper;
import br.com.ltoscano.droidplayer.filesystem.helper.PathHelper;

public class DirectoryDescriptor
{
    private String id;
    private String parentId;

    private String path;
    private String name;

    private final long creationTimestamp;
    private long modificationTimestamp;

    private final List<String> childDirectoryList;
    private final List<String> childFileList;

    public DirectoryDescriptor()
    {
        this.id = null;
        this.parentId = null;

        this.path = null;
        this.name = null;

        this.creationTimestamp = -1;
        this.modificationTimestamp = -1;

        this.childDirectoryList = null;
        this.childFileList = null;
    }

    public DirectoryDescriptor(String path) throws InvalidPathException
    {
        this.id = IdHelper.generate(path);
        this.parentId = IdHelper.generate(PathHelper.getBasePath(path));

        this.path = path;
        this.name = PathHelper.getName(path);

        this.creationTimestamp = System.currentTimeMillis();
        this.modificationTimestamp = creationTimestamp;

        this.childDirectoryList = new ArrayList<>();
        this.childFileList = new ArrayList<>();
    }

    public void addDirectory(String directoryId)
    {
        if(!containsDirectory(directoryId))
        {
            childDirectoryList.add(directoryId);
            updateModificationTimestamp();
        }
    }

    public void removeDirectory(String directoryId)
    {
        if(containsDirectory(directoryId))
        {
            childDirectoryList.remove(directoryId);
            updateModificationTimestamp();
        }
    }

    public boolean containsDirectory(String directoryId)
    {
        return childDirectoryList.contains(directoryId);
    }

    public void addFile(String fileId)
    {
        if(!containsFile(fileId))
        {
            childFileList.add(fileId);
            updateModificationTimestamp();
        }
    }

    public void removeFile(String fileId)
    {
        if(containsFile(fileId))
        {
            childFileList.remove(fileId);
            updateModificationTimestamp();
        }
    }

    public boolean containsFile(String fileId)
    {
        return childFileList.contains(fileId);
    }

    public String getId()
    {
        return id;
    }

    public String getParentId()
    {
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

    public List<String> getChildDirectoryList()
    {
        return childDirectoryList;
    }

    public List<String> getChildFileList()
    {
        return childFileList;
    }

    @Override
    public String toString()
    {
        return JSONHelper.objToJson(this);
    }
}
