// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.Context;
import com.azure.core.util.polling.SyncPoller;
import com.azure.storage.file.share.models.HandleItem;
import com.azure.storage.file.share.models.ShareAccessPolicy;
import com.azure.storage.file.share.models.ShareFileCopyInfo;
import com.azure.storage.file.share.models.ShareFileHttpHeaders;
import com.azure.storage.file.share.models.ShareFileRange;
import com.azure.storage.file.share.models.ShareServiceProperties;
import com.azure.storage.file.share.models.ShareSignedIdentifier;
import com.azure.storage.file.share.models.ShareStorageException;
import com.azure.storage.file.share.options.ShareSetPropertiesOptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WARNING: MODIFYING THIS FILE WILL REQUIRE CORRESPONDING UPDATES TO README.md FILE. LINE NUMBERS
 * ARE USED TO EXTRACT APPROPRIATE CODE SEGMENTS FROM THIS FILE. ADD NEW CODE AT THE BOTTOM TO AVOID CHANGING
 * LINE NUMBERS OF EXISTING CODE SAMPLES.
 *
 * Code samples for the README.md
 */
@SuppressWarnings("unused")
public class ReadmeSamples {
    private static final String ACCOUNT_NAME = System.getenv("AZURE_STORAGE_ACCOUNT_NAME");
    private static final String SAS_TOKEN = System.getenv("PRIMARY_SAS_TOKEN");
    private static final String CONNECTION_STRING = System.getenv("AZURE_CONNECTION_STRING");

    ShareServiceClient shareServiceClient = new ShareServiceClientBuilder().buildClient();
    ShareClient shareClient = new ShareClientBuilder().buildClient();
    ShareDirectoryClient directoryClient = new ShareFileClientBuilder().buildDirectoryClient();
    ShareFileClient fileClient = new ShareFileClientBuilder().buildFileClient();

    private Logger logger = LoggerFactory.getLogger(ReadmeSamples.class);

    public void handleException() {
        // BEGIN: readme-sample-handleException
        try {
            shareServiceClient.createShare("myShare");
        } catch (ShareStorageException e) {
            logger.error("Failed to create a share with error code: " + e.getErrorCode());
        }
        // END: readme-sample-handleException
    }

    public void createShareServiceClient() {
        // BEGIN: readme-sample-createShareServiceClient
        String shareServiceURL = String.format("https://%s.file.core.windows.net", ACCOUNT_NAME);
        ShareServiceClient shareServiceClient = new ShareServiceClientBuilder().endpoint(shareServiceURL)
            .sasToken(SAS_TOKEN).buildClient();
        // END: readme-sample-createShareServiceClient
    }

    public void createShareClient() {
        String shareName = "testshare";

        // BEGIN: readme-sample-createShareClient
        String shareURL = String.format("https://%s.file.core.windows.net", ACCOUNT_NAME);
        ShareClient shareClient = new ShareClientBuilder().endpoint(shareURL)
            .sasToken(SAS_TOKEN).shareName(shareName).buildClient();
        // END: readme-sample-createShareClient
    }

    public void createShareClientWithConnectionString() {
        String shareName = "testshare";

        // BEGIN: readme-sample-createShareClientWithConnectionString
        String shareURL = String.format("https://%s.file.core.windows.net", ACCOUNT_NAME);
        ShareClient shareClient = new ShareClientBuilder().endpoint(shareURL)
            .connectionString(CONNECTION_STRING).shareName(shareName).buildClient();
        // END: readme-sample-createShareClientWithConnectionString
    }

    public void createDirectoryClient() {
        String shareName = "testshare";
        String directoryPath = "directoryPath";

        // BEGIN: readme-sample-createDirectoryClient
        String directoryURL = String.format("https://%s.file.core.windows.net", ACCOUNT_NAME);
        ShareDirectoryClient directoryClient = new ShareFileClientBuilder().endpoint(directoryURL)
            .sasToken(SAS_TOKEN).shareName(shareName).resourcePath(directoryPath).buildDirectoryClient();
        // END: readme-sample-createDirectoryClient
    }

    public void createFileClient() {
        String shareName = "testshare";
        String directoryPath = "directoryPath";
        String fileName = "fileName";

        // BEGIN: readme-sample-createFileClient
        String fileURL = String.format("https://%s.file.core.windows.net", ACCOUNT_NAME);
        ShareFileClient fileClient = new ShareFileClientBuilder().connectionString(CONNECTION_STRING)
            .endpoint(fileURL).shareName(shareName).resourcePath(directoryPath + "/" + fileName).buildFileClient();
        // END: readme-sample-createFileClient
    }

    public void createShare() {
        // BEGIN: readme-sample-createShare
        String shareName = "testshare";
        shareServiceClient.createShare(shareName);
        // END: readme-sample-createShare
    }

    public void createSnapshotOnShare() {
        // BEGIN: readme-sample-createSnapshotOnShare
        String shareName = "testshare";
        ShareClient shareClient = shareServiceClient.getShareClient(shareName);
        shareClient.createSnapshot();
        // END: readme-sample-createSnapshotOnShare
    }

    public void createDirectory() {
        // BEGIN: readme-sample-createDirectory
        String dirName = "testdir";
        shareClient.createDirectory(dirName);
        // END: readme-sample-createDirectory
    }

    public void createSubDirectory() {
        // BEGIN: readme-sample-createSubDirectory
        String subDirName = "testsubdir";
        directoryClient.createSubdirectory(subDirName);
        // END: readme-sample-createSubDirectory
    }

    public void createFile() {
        // BEGIN: readme-sample-createFile
        String fileName = "testfile";
        long maxSize = 1024;
        directoryClient.createFile(fileName, maxSize);
        // END: readme-sample-createFile
    }

    public void getShareList() {
        // BEGIN: readme-sample-getShareList
        shareServiceClient.listShares();
        // END: readme-sample-getShareList
    }

    public void getSubDirectoryAndFileList() {
        // BEGIN: readme-sample-getSubDirectoryAndFileList
        directoryClient.listFilesAndDirectories();
        // END: readme-sample-getSubDirectoryAndFileList
    }

    public void getRangeList() {
        // BEGIN: readme-sample-getRangeList
        fileClient.listRanges();
        // END: readme-sample-getRangeList
    }

    public void deleteShare() {
        // BEGIN: readme-sample-deleteShare
        shareClient.delete();
        // END: readme-sample-deleteShare
    }

    public void deleteDirectory() {
        // BEGIN: readme-sample-deleteDirectory
        String dirName = "testdir";
        shareClient.deleteDirectory(dirName);
        // END: readme-sample-deleteDirectory
    }

    public void deleteSubDirectory() {
        // BEGIN: readme-sample-deleteSubDirectory
        String subDirName = "testsubdir";
        directoryClient.deleteSubdirectory(subDirName);
        // END: readme-sample-deleteSubDirectory
    }

    public void deleteFile() {
        // BEGIN: readme-sample-deleteFile
        String fileName = "testfile";
        directoryClient.deleteFile(fileName);
        // END: readme-sample-deleteFile
    }

    public void copyFile() {
        // BEGIN: readme-sample-copyFile
        String sourceURL = "https://myaccount.file.core.windows.net/myshare/myfile";
        Duration pollInterval = Duration.ofSeconds(2);
        SyncPoller<ShareFileCopyInfo, Void> poller = fileClient.beginCopy(sourceURL, (Map<String, String>) null, pollInterval);
        // END: readme-sample-copyFile
    }

    public void abortCopyFile() {
        // BEGIN: readme-sample-abortCopyFile
        fileClient.abortCopy("copyId");
        // END: readme-sample-abortCopyFile
    }

    public void uploadDataToStorage() {
        // BEGIN: readme-sample-uploadDataToStorage
        String uploadText = "default";
        InputStream data = new ByteArrayInputStream(uploadText.getBytes(StandardCharsets.UTF_8));
        fileClient.upload(data, uploadText.length());
        // END: readme-sample-uploadDataToStorage
    }

    public void uploadDataToStorageBiggerThan4MB() {
        // BEGIN: readme-sample-uploadDataToStorageBiggerThan4MB
        byte[] data = "Hello, data sample!".getBytes(StandardCharsets.UTF_8);

        long chunkSize = ShareFileAsyncClient.FILE_DEFAULT_BLOCK_SIZE;
        if (data.length > chunkSize) {
            for (int offset = 0; offset < data.length; offset += chunkSize) {
                try {
                    // the last chunk size is smaller than the others
                    chunkSize = Math.min(data.length - offset, chunkSize);

                    // select the chunk in the byte array
                    byte[] subArray = Arrays.copyOfRange(data, offset, (int) (offset + chunkSize));

                    // upload the chunk
                    fileClient.uploadWithResponse(new ByteArrayInputStream(subArray), chunkSize, (long) offset, null, Context.NONE);
                } catch (RuntimeException e) {
                    logger.error("Failed to upload the file", e);
                    if (Boolean.TRUE.equals(fileClient.exists())) {
                        fileClient.delete();
                    }
                    throw e;
                }
            }
        } else {
            fileClient.upload(new ByteArrayInputStream(data), data.length);
        }
        // END: readme-sample-uploadDataToStorageBiggerThan4MB
    }

    public void uploadFileToStorage() {
        // BEGIN: readme-sample-uploadFileToStorage
        String filePath = "${myLocalFilePath}";
        fileClient.uploadFromFile(filePath);
        // END: readme-sample-uploadFileToStorage
    }

    public void downloadDataFromFileRange() {
        // BEGIN: readme-sample-downloadDataFromFileRange
        ShareFileRange fileRange = new ShareFileRange(0L, 2048L);
        OutputStream stream = new ByteArrayOutputStream();
        fileClient.downloadWithResponse(stream, fileRange, false, null, Context.NONE);
        // END: readme-sample-downloadDataFromFileRange
    }

    public void downloadFileFromFileRange() {
        // BEGIN: readme-sample-downloadFileFromFileRange
        String filePath = "${myLocalFilePath}";
        fileClient.downloadToFile(filePath);
        // END: readme-sample-downloadFileFromFileRange
    }

    public void getShareServiceProperties() {
        // BEGIN: readme-sample-getShareServiceProperties
        shareServiceClient.getProperties();
        // END: readme-sample-getShareServiceProperties
    }

    public void setShareServiceProperties() {
        // BEGIN: readme-sample-setShareServiceProperties
        ShareServiceProperties properties = shareServiceClient.getProperties();

        properties.getMinuteMetrics().setEnabled(true).setIncludeApis(true);
        properties.getHourMetrics().setEnabled(true).setIncludeApis(true);

        shareServiceClient.setProperties(properties);
        // END: readme-sample-setShareServiceProperties
    }

    public void setShareMetadata() {
        // BEGIN: readme-sample-setShareMetadata
        Map<String, String> metadata = Collections.singletonMap("directory", "metadata");
        shareClient.setMetadata(metadata);
        // END: readme-sample-setShareMetadata
    }

    public void getAccessPolicy() {
        // BEGIN: readme-sample-getAccessPolicy
        shareClient.getAccessPolicy();
        // END: readme-sample-getAccessPolicy
    }

    public void setAccessPolicy() {
        // BEGIN: readme-sample-setAccessPolicy
        ShareAccessPolicy accessPolicy = new ShareAccessPolicy().setPermissions("r")
            .setStartsOn(OffsetDateTime.now(ZoneOffset.UTC))
            .setExpiresOn(OffsetDateTime.now(ZoneOffset.UTC).plusDays(10));
        ShareSignedIdentifier permission = new ShareSignedIdentifier().setId("mypolicy").setAccessPolicy(accessPolicy);
        shareClient.setAccessPolicy(Collections.singletonList(permission));
        // END: readme-sample-setAccessPolicy
    }

    public void getHandleList() {
        // BEGIN: readme-sample-getHandleList
        PagedIterable<HandleItem> handleItems = directoryClient.listHandles(null, true, Duration.ofSeconds(30), Context.NONE);
        // END: readme-sample-getHandleList
    }

    public void forceCloseHandleWithResponse() {
        // BEGIN: readme-sample-forceCloseHandleWithResponse
        PagedIterable<HandleItem> handleItems = directoryClient.listHandles(null, true, Duration.ofSeconds(30), Context.NONE);
        String handleId = handleItems.iterator().next().getHandleId();
        directoryClient.forceCloseHandleWithResponse(handleId, Duration.ofSeconds(30), Context.NONE);
        // END: readme-sample-forceCloseHandleWithResponse
    }

    public void setQuotaOnShare() {
        // BEGIN: readme-sample-setQuotaOnShare
        int quotaOnGB = 1;
        shareClient.setPropertiesWithResponse(new ShareSetPropertiesOptions().setQuotaInGb(quotaOnGB), null, Context.NONE);
        // END: readme-sample-setQuotaOnShare
    }

    public void setFileHttpHeaders() {
        // BEGIN: readme-sample-setFileHttpHeaders
        ShareFileHttpHeaders httpHeaders = new ShareFileHttpHeaders().setContentType("text/plain");
        fileClient.setProperties(1024, httpHeaders, null, null);
        // END: readme-sample-setFileHttpHeaders
    }
}
