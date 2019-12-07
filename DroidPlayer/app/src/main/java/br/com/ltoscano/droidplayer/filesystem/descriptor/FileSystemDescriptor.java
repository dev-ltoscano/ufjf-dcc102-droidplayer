package br.com.ltoscano.droidplayer.filesystem.descriptor;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import br.com.ltoscano.droidplayer.app.exception.AlreadyExistsException;
import br.com.ltoscano.droidplayer.app.exception.InvalidPathException;
import br.com.ltoscano.droidplayer.app.exception.NotFoundException;
import br.com.ltoscano.droidplayer.app.exception.UnexpectedException;
import br.com.ltoscano.droidplayer.filesystem.helper.IdHelper;
import br.com.ltoscano.droidplayer.app.helper.json.JSONHelper;
import br.com.ltoscano.droidplayer.filesystem.helper.PathHelper;

public class FileSystemDescriptor
{
    private String fsDirPath;
    private String blockDirPath;
    private String cacheDirPath;

    private final Map<String, DirectoryDescriptor> directoryList;
    private final Map<String, FileDescriptor> fileList;

    private final long creationTimestamp;
    private long modificationTimestamp;

    public FileSystemDescriptor(String baseDirPath)
    {
        this.fsDirPath = baseDirPath + File.separator + "fs";
        this.blockDirPath = fsDirPath + File.separator + "block";
        this.cacheDirPath = fsDirPath + File.separator + "cache";

        File blockDir = new File(blockDirPath);

        if(!blockDir.exists())
        {
            blockDir.mkdirs();
        }

        File cacheDir = new File(cacheDirPath);

        if(!cacheDir.exists())
        {
            cacheDir.mkdirs();
        }

        this.directoryList = new HashMap<>();
        this.fileList = new HashMap<>();

        this.creationTimestamp = System.currentTimeMillis();
        this.modificationTimestamp = creationTimestamp;

        try
        {
            this.createRootDirectory();
        }
        catch (InvalidPathException ex)
        {
            throw new UnexpectedException("Unable to create root directory");
        }
    }

    private void createRootDirectory() throws InvalidPathException
    {
        DirectoryDescriptor rootDir = new DirectoryDescriptor("/");
        this.directoryList.put(rootDir.getId(), rootDir);
    }

    public String getFileSystemDirectoryPath()
    {
        return fsDirPath;
    }

    public String getBlockDirectoryPath()
    {
        return blockDirPath;
    }

    public String getCacheDirectoryPath()
    {
        return cacheDirPath;
    }

    public DirectoryDescriptor getRootDirectory()
    {
        try
        {
            return getDirectory("/");
        }
        catch (InvalidPathException | NotFoundException ex)
        {
            throw new UnexpectedException("Unable to get root directory");
        }
    }

    public DirectoryDescriptor addDirectory(String path) throws InvalidPathException, AlreadyExistsException
    {
        if(!PathHelper.isValidPath(path))
        {
            throw new InvalidPathException("The path '" + path + "' is invalid");
        }

        String parentPath = PathHelper.getBasePath(path);

        if(!containsDirectory(parentPath))
        {
            throw new InvalidPathException("The parent path '" + parentPath + "' not exists");
        }

        if(containsDirectory(path))
        {
            throw new AlreadyExistsException("The path '" + path + "' already exists");
        }

        DirectoryDescriptor directoryDescriptor = new DirectoryDescriptor(path);
        directoryList.put(directoryDescriptor.getId(), directoryDescriptor);

        String parentId = IdHelper.generate(parentPath);

        DirectoryDescriptor parentDirectoryDescriptor = directoryList.get(parentId);
        parentDirectoryDescriptor.addDirectory(directoryDescriptor.getId());

        updateModificationTimestamp();

        return directoryDescriptor;
    }

    public void addDirectory(DirectoryDescriptor directoryDescriptor) throws InvalidPathException
    {
        directoryList.put(directoryDescriptor.getId(), directoryDescriptor);

        String parentId = IdHelper.generate(PathHelper.getBasePath(directoryDescriptor.getPath()));

        DirectoryDescriptor parentDirectoryDescriptor = directoryList.get(parentId);
        parentDirectoryDescriptor.addDirectory(directoryDescriptor.getId());

        updateModificationTimestamp();
    }

    public void removeDirectory(String path) throws InvalidPathException
    {
        if(!PathHelper.isValidPath(path))
        {
            throw new InvalidPathException("The path '" + path + "' is invalid");
        }

        if(containsDirectory(path))
        {
            String directoryId = IdHelper.generate(path);
            String parentId = IdHelper.generate(PathHelper.getBasePath(path));

            DirectoryDescriptor parentDirectoryDescriptor = directoryList.get(parentId);
            parentDirectoryDescriptor.removeDirectory(directoryId);

            directoryList.remove(directoryId);

            updateModificationTimestamp();
        }
    }

    public boolean containsDirectory(String path) throws InvalidPathException
    {
        if(!PathHelper.isValidPath(path))
        {
            throw new InvalidPathException("The path '" + path + "' is invalid");
        }

        return directoryList.containsKey(IdHelper.generate(path));
    }

    public boolean containsDirectoryById(String directoryId)
    {
        return directoryList.containsKey(directoryId);
    }

    public DirectoryDescriptor getDirectory(String path) throws InvalidPathException,  NotFoundException
    {
        if(!PathHelper.isValidPath(path))
        {
            throw new InvalidPathException("The path '" + path + "' is invalid");
        }

        if(!containsDirectory(path))
        {
            throw new NotFoundException("The directory '" + path + "' was not found");
        }

        return directoryList.get(IdHelper.generate(path));
    }

    public DirectoryDescriptor getDirectoryById(String directoryId) throws NotFoundException
    {
        if(!containsDirectoryById(directoryId))
        {
            throw new NotFoundException("The directory with id='" + directoryId + "' was not found");
        }

        return directoryList.get(directoryId);
    }

    public Collection<DirectoryDescriptor> getDirectoryList()
    {
        return directoryList.values();
    }

    public FileDescriptor addFile(String path) throws InvalidPathException, AlreadyExistsException
    {
        if(!PathHelper.isValidPath(path))
        {
            throw new InvalidPathException("The path '" + path + "' is invalid");
        }

        String parentPath = PathHelper.getBasePath(path);

        if(!containsDirectory(parentPath))
        {
            throw new InvalidPathException("The parent path '" + parentPath + "' not exists");
        }

        if(containsFile(path))
        {
            throw new AlreadyExistsException("The path '" + path + "' already exists");
        }

        FileDescriptor fileDescriptor = new FileDescriptor(path);
        fileList.put(fileDescriptor.getId(), fileDescriptor);

        String parentId = IdHelper.generate(parentPath);

        DirectoryDescriptor parentDirectoryDescriptor = directoryList.get(parentId);
        parentDirectoryDescriptor.addFile(fileDescriptor.getId());

        updateModificationTimestamp();

        return fileDescriptor;
    }

    public void addFile(FileDescriptor fileDescriptor) throws InvalidPathException
    {
        fileList.put(fileDescriptor.getId(), fileDescriptor);

            String parentId = IdHelper.generate(PathHelper.getBasePath(fileDescriptor.getPath()));

        DirectoryDescriptor parentDirectoryDescriptor = directoryList.get(parentId);
        parentDirectoryDescriptor.addFile(fileDescriptor.getId());

        updateModificationTimestamp();
    }

    public void removeFile(String path) throws InvalidPathException
    {
        if(!PathHelper.isValidPath(path))
        {
            throw new InvalidPathException("The path '" + path + "' is invalid");
        }

        if(containsFile(path))
        {
            String fileId = IdHelper.generate(path);
            String parentId = IdHelper.generate(PathHelper.getBasePath(path));

            DirectoryDescriptor parentDirectoryDescriptor = directoryList.get(parentId);
            parentDirectoryDescriptor.removeFile(fileId);

            fileList.remove(fileId);

            updateModificationTimestamp();
        }
    }

    public boolean containsFile(String path) throws InvalidPathException
    {
        if(!PathHelper.isValidPath(path))
        {
            throw new InvalidPathException("The path '" + path + "' is invalid");
        }

        return containsFileById(IdHelper.generate(path));
    }

    public boolean containsFileById(String fileId)
    {
        return fileList.containsKey(fileId);
    }

    public FileDescriptor getFile(String path) throws InvalidPathException, NotFoundException
    {
        if(!PathHelper.isValidPath(path))
        {
            throw new InvalidPathException("The path '" + path + "' is invalid");
        }

        if(!containsFile(path))
        {
            throw new NotFoundException("The file '" + path + "' was not found");
        }

        return fileList.get(IdHelper.generate(path));
    }

    public FileDescriptor getFileById(String fileId) throws NotFoundException
    {
        if(!containsFileById(fileId))
        {
            throw new NotFoundException("The file with id='" + fileId + "' was not found");
        }

        return fileList.get(fileId);
    }

    public Collection<FileDescriptor> getFileList()
    {
        return fileList.values();
    }

    public long getCreationTimestamp()
    {
        return creationTimestamp;
    }

    public long getModificationTimestamp()
    {
        return modificationTimestamp;
    }

    public void updateModificationTimestamp()
    {
        this.modificationTimestamp = System.currentTimeMillis();
    }

    @Override
    public String toString()
    {
        return JSONHelper.objToJson(this);
    }
}
