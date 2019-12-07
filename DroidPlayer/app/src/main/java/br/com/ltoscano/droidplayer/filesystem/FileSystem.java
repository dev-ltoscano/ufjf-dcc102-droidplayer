package br.com.ltoscano.droidplayer.filesystem;

import android.os.Environment;
import android.os.Message;

import com.google.android.gms.nearby.connection.Payload;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.ltoscano.droidplayer.app.exception.AlreadyExistsException;
import br.com.ltoscano.droidplayer.app.exception.FileSystemException;
import br.com.ltoscano.droidplayer.app.exception.InvalidPathException;
import br.com.ltoscano.droidplayer.app.exception.JSONException;
import br.com.ltoscano.droidplayer.app.exception.NotAvailableException;
import br.com.ltoscano.droidplayer.app.exception.NotFoundException;
import br.com.ltoscano.droidplayer.app.exception.RoutingException;
import br.com.ltoscano.droidplayer.filesystem.descriptor.BlockDescriptor;
import br.com.ltoscano.droidplayer.filesystem.descriptor.DirectoryDescriptor;
import br.com.ltoscano.droidplayer.filesystem.descriptor.FileDescriptor;
import br.com.ltoscano.droidplayer.filesystem.descriptor.FileSystemDescriptor;
import br.com.ltoscano.droidplayer.app.helper.json.JSONHelper;
import br.com.ltoscano.droidplayer.app.log.AppLogger;
import br.com.ltoscano.droidplayer.filesystem.helper.IdHelper;
import br.com.ltoscano.droidplayer.filesystem.helper.PathHelper;
import br.com.ltoscano.droidplayer.network.event.listener.IMessageEventListener;
import br.com.ltoscano.droidplayer.network.info.MessageInfo;
import br.com.ltoscano.droidplayer.network.service.NetworkService;

public class FileSystem implements IMessageEventListener
{
    private static FileSystem instance;
    private FileSystemDescriptor fileSystemDescriptor;

    private DirectoryDescriptor currentDirectory;

    private FileSystem()
    {
        this.fileSystemDescriptor = null;
        this.currentDirectory = null;
    }

    private FileSystem(String localBasePath)
    {
        this.fileSystemDescriptor = new FileSystemDescriptor(localBasePath);
        this.currentDirectory = fileSystemDescriptor.getRootDirectory();
    }

    public static FileSystem createInstance(String localBasePath)
    {
        if(instance == null)
        {
            instance = new FileSystem(localBasePath);
        }

        return instance;
    }

    public static FileSystem loadInstance(String fileSystemPath) throws FileSystemException
    {
        if(instance == null)
        {
            instance = new FileSystem();
            instance.load(fileSystemPath);
        }

        return instance;
    }

    public static FileSystem loadFrom(String fileSystemPath) throws FileSystemException
    {
        FileSystem fileSystem = new FileSystem();
        fileSystem.load(fileSystemPath);

        return fileSystem;
    }

    public static FileSystem getInstance()
    {
        return instance;
    }

    public File getFileSystemFile(String fsName) throws NotFoundException
    {
        File fileSystemFile = new File(getFileSystemDescriptor().getFileSystemDirectoryPath() + "/" + fsName + ".json");

        if(!fileSystemFile.exists())
        {
            throw new NotFoundException("The filesystem file '" + fileSystemFile.getAbsolutePath() + "' was not found");
        }

        return fileSystemFile;
    }

    public FileSystemDescriptor getFileSystemDescriptor()
    {
        return fileSystemDescriptor;
    }

    public FileDescriptor touch(String filePath) throws InvalidPathException, AlreadyExistsException
    {
        return fileSystemDescriptor.addFile(filePath);
    }

    public void mv(String srcPath, String dstPath, boolean isDirectory) throws NotFoundException, InvalidPathException, AlreadyExistsException
    {
        String basePath = PathHelper.getBasePath(dstPath);

        if(!fileSystemDescriptor.containsDirectory(basePath))
        {
            throw new NotFoundException("The parent directory '" + basePath + "' was not found");
        }

        if(fileSystemDescriptor.containsDirectory(dstPath) || fileSystemDescriptor.containsFile(dstPath))
        {
            throw new AlreadyExistsException("The path '" + dstPath + "' already exists");
        }

        if(isDirectory)
        {
            DirectoryDescriptor directoryDescriptor = fileSystemDescriptor.getDirectory(srcPath);

            String directoryId = directoryDescriptor.getId();
            String oldParentId = directoryDescriptor.getParentId();
            String newParentId = IdHelper.generate(basePath);

            directoryDescriptor.setPath(dstPath);

            fileSystemDescriptor.getDirectoryById(oldParentId).removeDirectory(directoryId);
            fileSystemDescriptor.getDirectoryById(newParentId).addDirectory(directoryId);

            List<String> childDirIdList = directoryDescriptor.getChildDirectoryList();
            List<String> childFileIdList = directoryDescriptor.getChildFileList();

            for(String childDirId : childDirIdList)
            {
                directoryDescriptor.removeDirectory(childDirId);

                DirectoryDescriptor childDirectoryDescriptor = fileSystemDescriptor .getDirectoryById(childDirId);
                String newPath = dstPath + "/" + childDirectoryDescriptor.getName();
                childDirectoryDescriptor.setPath(newPath);

                directoryDescriptor.addDirectory(childDirectoryDescriptor.getId());
            }

            for(String childFileId : childFileIdList)
            {
                directoryDescriptor.removeFile(childFileId);

                FileDescriptor childFileDescriptor = fileSystemDescriptor .getFileById(childFileId);

                String newPath = dstPath + "/" + childFileDescriptor.getName();
                childFileDescriptor.setPath(newPath);

                directoryDescriptor.addFile(childFileDescriptor.getId());
            }
        }
        else
        {
            FileDescriptor fileDescriptor = fileSystemDescriptor.getFile(srcPath);

            String fileId = fileDescriptor.getId();
            String oldParentId = fileDescriptor.getParentId();
            String newParentId = IdHelper.generate(basePath);

            fileDescriptor.setPath(dstPath);

            fileSystemDescriptor.getDirectoryById(oldParentId).removeFile(fileId);
            fileSystemDescriptor.getDirectoryById(newParentId).addFile(fileId);
        }
    }

    public void cp()
    {

    }

    public void rm(String filePath) throws InvalidPathException
    {
        fileSystemDescriptor.removeFile(filePath);
    }

    public DirectoryDescriptor mkdir(String dirPath) throws InvalidPathException, AlreadyExistsException
    {
        return fileSystemDescriptor.addDirectory(dirPath);
    }

    public void rmdir(String dirPath) throws InvalidPathException
    {
        fileSystemDescriptor.removeDirectory(dirPath);
    }

    public DirectoryDescriptor cd(String dirPath) throws NotFoundException, InvalidPathException
    {
        return fileSystemDescriptor.getDirectory(dirPath);
    }

    public DirectoryDescriptor pwd()
    {
        return currentDirectory;
    }

    public List<FileDescriptor> ls(String dirPath) throws NotFoundException, InvalidPathException
    {
        List<FileDescriptor> allFileDescriptorList = new ArrayList<>();

        DirectoryDescriptor directoryDescriptor = fileSystemDescriptor.getDirectory(dirPath);
        List<String> childFileIdList =  directoryDescriptor.getChildFileList();

        for(String childFileId : childFileIdList)
        {
            allFileDescriptorList.add(fileSystemDescriptor.getFileById(childFileId));
        }

        return allFileDescriptorList;
    }

    public void split(String localFilePath, String filePath) throws NotFoundException, InvalidPathException, IOException
    {
        File localFile = new File(localFilePath);

        if(!localFile.exists())
        {
            throw new NotFoundException("The path '" + localFilePath + "' was not found");
        }

        if(!fileSystemDescriptor.containsFile(filePath))
        {
            throw new NotFoundException("The file '" + filePath + "' was not found");
        }

        FileDescriptor fileDescriptor = fileSystemDescriptor.getFile(filePath);
        fileDescriptor.clearBlockDescriptorList();

        String blockBasePath = fileSystemDescriptor.getBlockDirectoryPath() + File.separator + fileDescriptor.getId();

        File blockDir = new File(blockBasePath);

        if(!blockDir.exists())
        {
            blockDir.mkdirs();
        }

        byte[] buffer = new byte[65536];
        int offset = 0;
        int size;

        try(BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(localFile)))
        {
            while((size = bufferedInputStream.read(buffer)) != -1)
            {
                BlockDescriptor blockDescriptor = new BlockDescriptor(size, offset);

                String blockPath =
                        blockBasePath
                                + File.separator
                                + blockDescriptor.getId();

                File blockFile = new File(blockPath);

                try(FileOutputStream outputStream = new FileOutputStream(blockFile))
                {
                    outputStream.write(buffer);
                }

                blockDescriptor.setLocalPath(blockPath);

                fileDescriptor.addBlockDescriptor(blockDescriptor);
                offset += size;
            }
        }
    }

    public void merge(String filePath, String outputFilePath) throws InvalidPathException, NotFoundException, NotAvailableException, IOException
    {
        FileDescriptor fileDescriptor = fileSystemDescriptor.getFile(filePath);

        if(!fileDescriptor.isAvailable())
        {
            throw new NotAvailableException("The file '" + filePath + " is not available");
        }

        try(RandomAccessFile randomAccessFile = new RandomAccessFile(outputFilePath, "rw"))
        {
            Collection<BlockDescriptor> blockDescriptorList = fileDescriptor.getBlockDescriptorList();

            for(BlockDescriptor blockDescriptor : blockDescriptorList)
            {
                byte[] buffer = new byte[blockDescriptor.getSize()];

                try(FileInputStream blockFileInputStream = new FileInputStream(new File(blockDescriptor.getLocalPath())))
                {
                    blockFileInputStream.read(buffer);
                }

                randomAccessFile.seek(blockDescriptor.getOffset());
                randomAccessFile.write(buffer);
            }
        }
    }

    public void load(String fileSystemPath) throws FileSystemException
    {
        try(BufferedReader bufferedReader = new BufferedReader(new FileReader(fileSystemPath)))
        {
            StringBuilder jsonBuilder = new StringBuilder();
            String tmpLine;

            while((tmpLine = bufferedReader.readLine()) != null)
            {
                jsonBuilder.append(tmpLine);
            }

            fileSystemDescriptor = JSONHelper.jsonToObj(jsonBuilder.toString(), FileSystemDescriptor.class);
        }
        catch (JSONException ex)
        {
            throw new FileSystemException("The stored file system '" + fileSystemPath + "' has invalid format");
        }
        catch (IOException ex)
        {
            AppLogger.logError(ex.getMessage(), ex);
        }
    }

    public void save(String fileSystemPath) throws IOException
    {
        try(FileOutputStream outputStream = new FileOutputStream(new File(fileSystemPath)))
        {
            outputStream.write(fileSystemDescriptor.toString().getBytes(StandardCharsets.UTF_8));
        }
    }

    @Override
    public void onNetworkMessageReceived(MessageInfo messageInfo)
    {
        switch (messageInfo.getMessageType())
        {
            case "FILESYSTEM_SYNC":
            {
                try
                {
                    String newFileSystemPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + messageInfo.getParam("ID").toString();
                    FileSystem newFileSystem = FileSystem.loadFrom(newFileSystemPath);

                    if(newFileSystem.getFileSystemDescriptor().getModificationTimestamp() > instance.getFileSystemDescriptor().getModificationTimestamp())
                    {
                        Collection<DirectoryDescriptor> newDirectoryDescriptorList = newFileSystem.getFileSystemDescriptor().getDirectoryList();

                        for(DirectoryDescriptor newDirectoryDescriptor : newDirectoryDescriptorList)
                        {
                            if(!fileSystemDescriptor.containsDirectoryById(newDirectoryDescriptor.getId()))
                            {
                                try
                                {
                                    fileSystemDescriptor.addDirectory(newDirectoryDescriptor);
                                }
                                catch (InvalidPathException ex)
                                {
                                    AppLogger.logError(ex.getMessage(), ex);
                                }
                            }
                        }

                        Collection<FileDescriptor> newFileDescriptorList = newFileSystem.getFileSystemDescriptor().getFileList();

                        for(FileDescriptor newFileDescriptor : newFileDescriptorList)
                        {
                            if(!fileSystemDescriptor.containsFileById(newFileDescriptor.getId()))
                            {
                                try
                                {
                                    fileSystemDescriptor.addFile(newFileDescriptor);
                                }
                                catch (InvalidPathException ex)
                                {
                                    AppLogger.logError(ex.getMessage(), ex);
                                }
                            }
                        }
                    }
                }
                catch (NotFoundException ex)
                {
                    AppLogger.logError(ex.getMessage(), ex);
                }
                catch (FileSystemException ex)
                {
                    AppLogger.logError(ex.getMessage(), ex);
                }

                break;
            }
            case "FILESYSTEM_GET_BLOCK":
            {
                try
                {
                    Collection<BlockDescriptor> blockDescriptorList = getInstance().getFileSystemDescriptor().getFile(messageInfo.getParam("FILE_ID").toString()).getBlockDescriptorList();
                    String blockId = messageInfo.getParam("BLOCK_ID").toString();

                    for(BlockDescriptor blockDescriptor : blockDescriptorList)
                    {
                        if(blockDescriptor.getId().equalsIgnoreCase(blockId))
                        {
                            File blockFile = new File(blockDescriptor.getLocalPath());

                            NetworkService networkService = NetworkService.getInstance();
                            networkService.sendPayload(messageInfo.getParam("FROM_ENDPOINT_NAME").toString(), Payload.fromFile(blockFile));
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
                catch (FileNotFoundException ex)
                {
                    AppLogger.logError(ex.getMessage(), ex);
                }
                catch (RoutingException ex)
                {
                    AppLogger.logError(ex.getMessage(), ex);
                }

                break;
            }
        }
    }
}
