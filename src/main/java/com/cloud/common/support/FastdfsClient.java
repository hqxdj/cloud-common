package com.cloud.common.support;

import com.github.tobato.fastdfs.domain.fdfs.StorageNode;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.DefaultAppendFileStorageClient;
import com.github.tobato.fastdfs.service.DefaultFastFileStorageClient;
import com.github.tobato.fastdfs.service.DefaultTrackerClient;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.io.OutputStream;

@Component
public class FastdfsClient {

    @Autowired
    private DefaultTrackerClient trackerClient;

    @Autowired
    private DefaultFastFileStorageClient fastFileStorageClient;

    @Autowired
    private DefaultAppendFileStorageClient appendFileStorageClient;

    /**
     * 上传文件
     *
     * @param inputStream
     * @param fileName
     * @param fileSize
     * @return filePath
     */
    public String uploadFile(InputStream inputStream, String fileName, long fileSize) {
        String fileExtName = FilenameUtils.getExtension(fileName);
        StorePath storePath = fastFileStorageClient.uploadFile(inputStream, fileSize, fileExtName, null);
        return storePath.getFullPath();
    }

    /**
     * 上传可追加的文件
     *
     * @param inputStream
     * @param fileName
     * @param fileSize
     * @return filePath
     */
    public String uploadAppendFile(InputStream inputStream, String fileName, long fileSize) {
        StorageNode storageNode = trackerClient.getStoreStorage();
        String fileExtName = FilenameUtils.getExtension(fileName);
        StorePath storePath = appendFileStorageClient.uploadAppenderFile(storageNode.getGroupName(), inputStream, fileSize, fileExtName);
        return storePath.getFullPath();
    }

    /**
     * 追加文件
     *
     * @param inputStream
     * @param filePath
     * @param fileSize
     */
    public void appendFile(InputStream inputStream, String filePath, long fileSize) {
        StorePath storePath = StorePath.parseFromUrl(filePath);
        appendFileStorageClient.appendFile(storePath.getGroup(), storePath.getPath(), inputStream, fileSize);
    }

    /**
     * 下载文件
     *
     * @param outputStream
     * @param filePath
     * @return int
     */
    public int downloadFile(OutputStream outputStream, String filePath) {
        StorePath storePath = StorePath.parseFromUrl(filePath);
        return fastFileStorageClient.downloadFile(storePath.getGroup(), storePath.getPath(), inputStream -> IOUtils.copy(inputStream, outputStream));
    }

    /**
     * 指定开始位置下载文件
     *
     * @param outputStream
     * @param filePath
     * @param fileOffset
     * @return int
     */
    public int downloadFile(OutputStream outputStream, String filePath, long fileOffset) {
        StorePath storePath = StorePath.parseFromUrl(filePath);
        long downloadSize = getFileSize(filePath) - fileOffset;
        return fastFileStorageClient.downloadFile(storePath.getGroup(), storePath.getPath(), fileOffset, downloadSize, inputStream -> IOUtils.copy(inputStream, outputStream));
    }

    /**
     * 删除文件
     *
     * @param filePath
     */
    public void deleteFile(String filePath) {
        fastFileStorageClient.deleteFile(filePath);
    }

    /**
     * 获取文件大小
     *
     * @param filePath
     * @return long
     */
    public long getFileSize(String filePath) {
        StorePath storePath = StorePath.parseFromUrl(filePath);
        return fastFileStorageClient.queryFileInfo(storePath.getGroup(), storePath.getPath()).getFileSize();
    }

}
