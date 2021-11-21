// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file.share;

import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.file.share.models.CloseHandlesInfo;
import com.azure.storage.file.share.models.DownloadRetryOptions;
import com.azure.storage.file.share.models.FileRange;
import com.azure.storage.file.share.models.PermissionCopyModeType;
import com.azure.storage.file.share.models.ShareFileCopyInfo;
import com.azure.storage.file.share.models.ShareFileHttpHeaders;
import com.azure.storage.file.share.models.ShareFileInfo;
import com.azure.storage.file.share.models.ShareFileMetadataInfo;
import com.azure.storage.file.share.models.ShareFileProperties;
import com.azure.storage.file.share.models.ShareFileRange;
import com.azure.storage.file.share.models.ShareFileRangeList;
import com.azure.storage.file.share.models.ShareFileUploadOptions;
import com.azure.storage.file.share.models.ShareFileUploadInfo;
import com.azure.storage.file.share.models.ShareFileUploadRangeFromUrlInfo;
import com.azure.storage.file.share.models.NtfsFileAttributes;
import com.azure.storage.file.share.models.ShareFileUploadRangeOptions;
import com.azure.storage.file.share.models.ShareRequestConditions;
import com.azure.storage.file.share.options.ShareFileDownloadOptions;
import com.azure.storage.file.share.options.ShareFileListRangesDiffOptions;
import com.azure.storage.file.share.options.ShareFileUploadRangeFromUrlOptions;
import com.azure.storage.file.share.sas.ShareFileSasPermission;
import com.azure.storage.file.share.sas.ShareServiceSasSignatureValues;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;

/**
 * Contains code snippets when generating javadocs through doclets for {@link ShareFileClient} and {@link ShareFileAsyncClient}.
 */
public class ShareFileJavaDocCodeSamples {

    private String key1 = "key1";
    private String value1 = "val1";
    private String leaseId = "leaseId";
    ShareFileClient client = createClientWithSASToken();
    private Duration timeout = Duration.ofSeconds(30);

    /**
     * Generates code sample for {@link ShareFileClient} instantiation.
     */
    public void initialization() {
        // BEGIN: com.azure.storage.file.share.ShareFileClient.instantiation
        ShareFileClient client = new ShareFileClientBuilder()
            .connectionString("${connectionString}")
            .endpoint("${endpoint}")
            .buildFileClient();
        // END: com.azure.storage.file.share.ShareFileClient.instantiation
    }

    /**
     * Generates code sample for creating a {@link ShareFileClient} with SAS token.
     *
     * @return An instance of {@link ShareFileClient}
     */
    public ShareFileClient createClientWithSASToken() {

        // BEGIN: com.azure.storage.file.share.ShareFileClient.instantiation.sastoken
        ShareFileClient fileClient = new ShareFileClientBuilder()
            .endpoint("https://${accountName}.file.core.windows.net?${SASToken}")
            .shareName("myshare")
            .resourcePath("myfilepath")
            .buildFileClient();
        // END: com.azure.storage.file.share.ShareFileClient.instantiation.sastoken
        return fileClient;
    }

    /**
     * Generates code sample for creating a {@link ShareFileClient} with SAS token.
     *
     * @return An instance of {@link ShareFileClient}
     */
    public ShareFileClient createClientWithCredential() {

        // BEGIN: com.azure.storage.file.share.ShareFileClient.instantiation.credential
        ShareFileClient fileClient = new ShareFileClientBuilder()
            .endpoint("https://${accountName}.file.core.windows.net")
            .sasToken("${SASTokenQueryParams}")
            .shareName("myshare")
            .resourcePath("myfilepath")
            .buildFileClient();
        // END: com.azure.storage.file.share.ShareFileClient.instantiation.credential
        return fileClient;
    }

    /**
     * Generates code sample for creating a {@link ShareFileClient} with {@code connectionString} which turns into {@link
     * StorageSharedKeyCredential}
     *
     * @return An instance of {@link ShareFileClient}
     */
    public ShareFileClient createClientWithConnectionString() {
        // BEGIN: com.azure.storage.file.share.ShareFileClient.instantiation.connectionstring
        String connectionString = "DefaultEndpointsProtocol=https;AccountName={name};AccountKey={key};"
            + "EndpointSuffix={core.windows.net}";
        ShareFileClient fileClient = new ShareFileClientBuilder()
            .connectionString(connectionString).shareName("myshare").resourcePath("myfilepath")
            .buildFileClient();
        // END: com.azure.storage.file.share.ShareFileClient.instantiation.connectionstring
        return fileClient;
    }

    /**
     * Code snippets for {@link ShareFileClient#exists()} and {@link ShareFileClient#existsWithResponse(
     * Duration, Context)}
     */
    public void exists() {
        // BEGIN: com.azure.storage.file.share.ShareFileClient.exists
        System.out.printf("Exists? %b%n", client.exists());
        // END: com.azure.storage.file.share.ShareFileClient.exists

        // BEGIN: com.azure.storage.file.share.ShareFileClient.existsWithResponse#Duration-Context
        Context context = new Context("Key", "Value");
        System.out.printf("Exists? %b%n", client.existsWithResponse(timeout, context).getValue());
        // END: com.azure.storage.file.share.ShareFileClient.existsWithResponse#Duration-Context
    }

    /**
     * Generates a code sample for using {@link ShareFileClient#create(long)}
     */
    public void createFile() {
        ShareFileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileClient.create
        ShareFileInfo response = fileClient.create(1024);
        System.out.println("Complete creating the file.");
        // END: com.azure.storage.file.share.ShareFileClient.create
    }

    /**
     * Generates a code sample for using {@link ShareFileClient#createWithResponse(long, ShareFileHttpHeaders, FileSmbProperties,
     * String, Map, Duration, Context)}
     */
    public void createWithResponse() {
        ShareFileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileClient.createWithResponse#long-ShareFileHttpHeaders-FileSmbProperties-String-Map-Duration-Context
        ShareFileHttpHeaders httpHeaders = new ShareFileHttpHeaders()
            .setContentType("text/html")
            .setContentEncoding("gzip")
            .setContentLanguage("en")
            .setCacheControl("no-transform")
            .setContentDisposition("attachment");
        FileSmbProperties smbProperties = new FileSmbProperties()
            .setNtfsFileAttributes(EnumSet.of(NtfsFileAttributes.READ_ONLY))
            .setFileCreationTime(OffsetDateTime.now())
            .setFileLastWriteTime(OffsetDateTime.now())
            .setFilePermissionKey("filePermissionKey");
        String filePermission = "filePermission";
        // NOTE: filePermission and filePermissionKey should never be both set
        Response<ShareFileInfo> response = fileClient.createWithResponse(1024, httpHeaders, smbProperties,
            filePermission, Collections.singletonMap("directory", "metadata"), Duration.ofSeconds(1),
            new Context(key1, value1));
        System.out.printf("Creating the file completed with status code %d", response.getStatusCode());
        // END: com.azure.storage.file.share.ShareFileClient.createWithResponse#long-ShareFileHttpHeaders-FileSmbProperties-String-Map-Duration-Context
    }

    /**
     * Generates a code sample for using {@link ShareFileClient#createWithResponse(long, ShareFileHttpHeaders, FileSmbProperties,
     * String, Map, ShareRequestConditions, Duration, Context)}
     */
    public void createWithLease() {
        ShareFileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileClient.createWithResponse#long-ShareFileHttpHeaders-FileSmbProperties-String-Map-ShareRequestConditions-Duration-Context
        ShareFileHttpHeaders httpHeaders = new ShareFileHttpHeaders()
            .setContentType("text/html")
            .setContentEncoding("gzip")
            .setContentLanguage("en")
            .setCacheControl("no-transform")
            .setContentDisposition("attachment");
        FileSmbProperties smbProperties = new FileSmbProperties()
            .setNtfsFileAttributes(EnumSet.of(NtfsFileAttributes.READ_ONLY))
            .setFileCreationTime(OffsetDateTime.now())
            .setFileLastWriteTime(OffsetDateTime.now())
            .setFilePermissionKey("filePermissionKey");
        String filePermission = "filePermission";
        // NOTE: filePermission and filePermissionKey should never be both set

        ShareRequestConditions requestConditions = new ShareRequestConditions().setLeaseId(leaseId);

        Response<ShareFileInfo> response = fileClient.createWithResponse(1024, httpHeaders, smbProperties,
            filePermission, Collections.singletonMap("directory", "metadata"), requestConditions, Duration.ofSeconds(1),
            new Context(key1, value1));
        System.out.printf("Creating the file completed with status code %d", response.getStatusCode());
        // END: com.azure.storage.file.share.ShareFileClient.createWithResponse#long-ShareFileHttpHeaders-FileSmbProperties-String-Map-ShareRequestConditions-Duration-Context
    }

    /**
     * Generates a code sample for using {@link ShareFileClient#beginCopy(String, Map, Duration)}
     */
    public void beginCopy() {
        ShareFileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileClient.beginCopy#string-map-duration
        SyncPoller<ShareFileCopyInfo, Void> poller = fileClient.beginCopy(
            "https://{accountName}.file.core.windows.net?{SASToken}",
            Collections.singletonMap("file", "metadata"), Duration.ofSeconds(2));

        final PollResponse<ShareFileCopyInfo> pollResponse = poller.poll();
        final ShareFileCopyInfo value = pollResponse.getValue();
        System.out.printf("Copy source: %s. Status: %s.%n", value.getCopySourceUrl(), value.getCopyStatus());

        // END: com.azure.storage.file.share.ShareFileClient.beginCopy#string-map-duration
    }

    /**
     * Generates a code sample for using {@link ShareFileClient#beginCopy(String, FileSmbProperties, String, PermissionCopyModeType, Boolean, Boolean, Map, Duration, ShareRequestConditions)}
     */
    public void beginCopy2() {
        ShareFileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileClient.beginCopy#string-filesmbproperties-string-permissioncopymodetype-boolean-boolean-map-duration-ShareRequestConditions
        FileSmbProperties smbProperties = new FileSmbProperties()
            .setNtfsFileAttributes(EnumSet.of(NtfsFileAttributes.READ_ONLY))
            .setFileCreationTime(OffsetDateTime.now())
            .setFileLastWriteTime(OffsetDateTime.now())
            .setFilePermissionKey("filePermissionKey");
        String filePermission = "filePermission";
        // NOTE: filePermission and filePermissionKey should never be both set
        boolean ignoreReadOnly = false; // Default value
        boolean setArchiveAttribute = true; // Default value
        ShareRequestConditions requestConditions = new ShareRequestConditions().setLeaseId(leaseId);

        SyncPoller<ShareFileCopyInfo, Void> poller = fileClient.beginCopy(
            "https://{accountName}.file.core.windows.net?{SASToken}", smbProperties, filePermission,
            PermissionCopyModeType.SOURCE, ignoreReadOnly, setArchiveAttribute,
            Collections.singletonMap("file", "metadata"), Duration.ofSeconds(2), requestConditions);

        final PollResponse<ShareFileCopyInfo> pollResponse = poller.poll();
        final ShareFileCopyInfo value = pollResponse.getValue();
        System.out.printf("Copy source: %s. Status: %s.%n", value.getCopySourceUrl(), value.getCopyStatus());
        // END: com.azure.storage.file.share.ShareFileClient.beginCopy#string-filesmbproperties-string-permissioncopymodetype-boolean-boolean-map-duration-ShareRequestConditions
    }

    /**
     * Generates a code sample for using {@link ShareFileClient#abortCopy(String)}
     */
    public void abortCopyFile() {
        ShareFileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileClient.abortCopy#string
        fileClient.abortCopy("someCopyId");
        System.out.println("Abort copying the file completed.");
        // END: com.azure.storage.file.share.ShareFileClient.abortCopy#string
    }

    /**
     * Generates a code sample for using {@link ShareFileClient#abortCopyWithResponse(String, Duration, Context)}
     */
    public void abortCopyWithResponse() {
        ShareFileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileClient.abortCopyWithResponse#string-duration-context
        Response<Void> response = fileClient.abortCopyWithResponse("someCopyId", Duration.ofSeconds(1),
            new Context(key1, value1));
        System.out.printf("Abort copying the file completed with status code %d", response.getStatusCode());
        // END: com.azure.storage.file.share.ShareFileClient.abortCopyWithResponse#string-duration-context
    }

    /**
     * Generates a code sample for using {@link ShareFileClient#abortCopyWithResponse(String, ShareRequestConditions, Duration, Context)}
     */
    public void abortCopyWithLease() {
        ShareFileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileClient.abortCopyWithResponse#string-ShareRequestConditions-duration-context
        ShareRequestConditions requestConditions = new ShareRequestConditions().setLeaseId(leaseId);
        Response<Void> response = fileClient.abortCopyWithResponse("someCopyId", requestConditions,
            Duration.ofSeconds(1), new Context(key1, value1));
        System.out.printf("Abort copying the file completed with status code %d", response.getStatusCode());
        // END: com.azure.storage.file.share.ShareFileClient.abortCopyWithResponse#string-ShareRequestConditions-duration-context
    }


    /**
     * Generates a code sample for using {@link ShareFileClient#upload(InputStream, long)}
     */
    public void uploadData() {
        ShareFileClient fileClient = createClientWithSASToken();
        byte[] data = "default".getBytes(StandardCharsets.UTF_8);

        // BEGIN: com.azure.storage.file.share.ShareFileClient.upload#InputStream-long
        InputStream uploadData = new ByteArrayInputStream(data);
        ShareFileUploadInfo response = fileClient.upload(uploadData, data.length);
        System.out.println("Complete uploading the data with eTag: " + response.getETag());
        // END: com.azure.storage.file.share.ShareFileClient.upload#InputStream-long
    }

    /**
     * Code snippet for {@link ShareFileClient#uploadWithResponse(InputStream, long, Long, Duration, Context)}.
     */
    public void uploadWithResponse() {
        ShareFileClient fileClient = createClientWithCredential();
        byte[] data = "default".getBytes(StandardCharsets.UTF_8);

        // BEGIN: com.azure.storage.file.share.ShareFileClient.uploadWithResponse#InputStream-long-Long-Duration-Context
        InputStream uploadData = new ByteArrayInputStream(data);
        Response<ShareFileUploadInfo> response = fileClient.uploadWithResponse(uploadData, data.length, 0L,
            Duration.ofSeconds(30), null);
        System.out.printf("Completed uploading the data with response %d%n.", response.getStatusCode());
        System.out.printf("ETag of the file is %s%n", response.getValue().getETag());
        // END: com.azure.storage.file.share.ShareFileClient.uploadWithResponse#InputStream-long-Long-Duration-Context
    }

    /**
     * Generates a code sample for using {@link ShareFileClient#uploadRange(InputStream, long)}
     */
    public void uploadRange() {
        ShareFileClient shareFileClient = createClientWithSASToken();
        byte[] data = "default".getBytes(StandardCharsets.UTF_8);
        // BEGIN: com.azure.storage.file.share.ShareFileClient.uploadRange#InputStream-long
        InputStream uploadData = new ByteArrayInputStream(data);
        ShareFileUploadInfo response = shareFileClient.uploadRange(uploadData, data.length);
        System.out.println("Complete uploading the data with eTag: " + response.getETag());
        // END: com.azure.storage.file.share.ShareFileClient.uploadRange#InputStream-long
    }

    /**
     * Generates a code sample for using
     * {@link ShareFileClient#uploadRangeWithResponse(ShareFileUploadRangeOptions, Duration, Context)}
     */
    public void uploadRangeWithResponse() {
        ShareFileClient shareFileClient = createClientWithSASToken();
        byte[] data = "default".getBytes(StandardCharsets.UTF_8);
        // BEGIN: com.azure.storage.file.share.ShareFileClient.uploadRangeWithResponse#ShareFileUploadRangeOptions-Duration-Context
        InputStream uploadData = new ByteArrayInputStream(data);
        Response<ShareFileUploadInfo> response = shareFileClient.uploadRangeWithResponse(
            new ShareFileUploadRangeOptions(uploadData, data.length), Duration.ofSeconds(30), null);
        System.out.printf("Completed uploading the data with response %d%n.", response.getStatusCode());
        System.out.printf("ETag of the file is %s%n", response.getValue().getETag());
        // END: com.azure.storage.file.share.ShareFileClient.uploadRangeWithResponse#ShareFileUploadRangeOptions-Duration-Context
    }

    /**
     * Generates a code sample for using
     * {@link ShareFileClient#upload(InputStream, long, com.azure.storage.common.ParallelTransferOptions)}
     */
    public void uploadBufferedRange() {
        ShareFileClient shareFileClient = createClientWithSASToken();
        byte[] data = "default".getBytes(StandardCharsets.UTF_8);
        // BEGIN: com.azure.storage.file.share.ShareFileClient.upload#InputStream-long-ParallelTransferOptions
        InputStream uploadData = new ByteArrayInputStream(data);
        ShareFileUploadInfo response = shareFileClient.upload(uploadData, data.length, null);
        System.out.println("Complete uploading the data with eTag: " + response.getETag());
        // END: com.azure.storage.file.share.ShareFileClient.upload#InputStream-long-ParallelTransferOptions
    }

    /**
     * Generates a code sample for using
     * {@link ShareFileClient#uploadWithResponse(ShareFileUploadOptions, Duration, Context)}
     */
    public void uploadBufferedRangeWithResponse() {
        ShareFileClient shareFileAsyncClient = createClientWithSASToken();
        byte[] data = "default".getBytes(StandardCharsets.UTF_8);
        // BEGIN: com.azure.storage.file.share.ShareFileClient.uploadWithResponse#ShareFileUploadOptions-Duration-Context
        InputStream uploadData = new ByteArrayInputStream(data);
        Response<ShareFileUploadInfo> response = shareFileAsyncClient.uploadWithResponse(
            new ShareFileUploadOptions(uploadData, data.length), Duration.ofSeconds(30), null);
        System.out.printf("Completed uploading the data with response %d%n.", response.getStatusCode());
        System.out.printf("ETag of the file is %s%n", response.getValue().getETag());
        // END: com.azure.storage.file.share.ShareFileClient.uploadWithResponse#ShareFileUploadOptions-Duration-Context
    }

    /**
     * Code snippet for {@link ShareFileClient#uploadWithResponse(InputStream, long, Long, ShareRequestConditions, Duration, Context)}.
     */
    public void uploadWithLease() {
        ShareFileClient fileClient = createClientWithCredential();
        byte[] data = "default".getBytes(StandardCharsets.UTF_8);

        // BEGIN: com.azure.storage.file.share.ShareFileClient.uploadWithResponse#InputStream-long-Long-ShareRequestConditions-Duration-Context
        InputStream uploadData = new ByteArrayInputStream(data);
        ShareRequestConditions requestConditions = new ShareRequestConditions().setLeaseId(leaseId);
        Response<ShareFileUploadInfo> response = fileClient.uploadWithResponse(uploadData, data.length, 0L,
            requestConditions, Duration.ofSeconds(30), null);
        System.out.printf("Completed uploading the data with response %d%n.", response.getStatusCode());
        System.out.printf("ETag of the file is %s%n", response.getValue().getETag());
        // END: com.azure.storage.file.share.ShareFileClient.uploadWithResponse#InputStream-long-Long-ShareRequestConditions-Duration-Context
    }

    /**
     * Generates a code sample for using {@link ShareFileClient#clearRange(long)}
     */
    public void clearRange() {
        ShareFileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileClient.clearRange#long
        ShareFileUploadInfo response = fileClient.clearRange(1024);
        System.out.println("Complete clearing the range with eTag: " + response.getETag());
        // END: com.azure.storage.file.share.ShareFileClient.clearRange#long
    }

    /**
     * Generates a code sample for using {@link ShareFileClient#clearRangeWithResponse(long, long, Duration, Context)}
     */
    public void clearRangeMaxOverload() {
        ShareFileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileClient.clearRangeWithResponse#long-long-Duration-Context
        Response<ShareFileUploadInfo> response = fileClient.clearRangeWithResponse(1024, 1024,
            Duration.ofSeconds(1), new Context(key1, value1));
        System.out.println("Complete clearing the range with status code: " + response.getStatusCode());
        // END: com.azure.storage.file.share.ShareFileClient.clearRangeWithResponse#long-long-Duration-Context
    }

    /**
     * Generates a code sample for using {@link ShareFileClient#clearRangeWithResponse(long, long, ShareRequestConditions, Duration, Context)}
     */
    public void clearRangeWithLease() {
        ShareFileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileClient.clearRangeWithResponse#long-long-ShareRequestConditions-Duration-Context
        ShareRequestConditions requestConditions = new ShareRequestConditions().setLeaseId(leaseId);
        Response<ShareFileUploadInfo> response = fileClient.clearRangeWithResponse(1024, 1024, requestConditions,
            Duration.ofSeconds(1), new Context(key1, value1));
        System.out.println("Complete clearing the range with status code: " + response.getStatusCode());
        // END: com.azure.storage.file.share.ShareFileClient.clearRangeWithResponse#long-long-ShareRequestConditions-Duration-Context
    }

    /**
     * Generates a code sample for using {@link ShareFileClient#uploadFromFile(String)}
     */
    public void uploadFile() {
        ShareFileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileClient.uploadFromFile#string
        fileClient.uploadFromFile("someFilePath");
        // END: com.azure.storage.file.share.ShareFileClient.uploadFromFile#string
    }

    /**
     * Generates a code sample for using {@link ShareFileClient#uploadFromFile(String, ShareRequestConditions)}
     */
    public void uploadFileWithLease() {
        ShareFileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileClient.uploadFromFile#string-ShareRequestConditions
        ShareRequestConditions requestConditions = new ShareRequestConditions().setLeaseId(leaseId);
        fileClient.uploadFromFile("someFilePath", requestConditions);
        // END: com.azure.storage.file.share.ShareFileClient.uploadFromFile#string-ShareRequestConditions
    }

    /**
     * Generates a code sample for using {@link ShareFileClient#download(OutputStream)}
     */
    public void download() {
        ShareFileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileClient.download#OutputStream
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            fileClient.download(stream);
            System.out.printf("Completed downloading the file with content: %n%s%n",
                new String(stream.toByteArray(), StandardCharsets.UTF_8));
        } catch (Throwable throwable) {
            System.err.printf("Downloading failed with exception. Message: %s%n", throwable.getMessage());
        }
        // END: com.azure.storage.file.share.ShareFileClient.download#OutputStream
    }

    /**
     * Generates a code sample for using {@link ShareFileClient#downloadWithResponse(OutputStream, ShareFileRange, Boolean,
     * Duration, Context)}
     */
    public void downloadWithPropertiesWithResponse() {
        ShareFileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileClient.downloadWithResponse#OutputStream-ShareFileRange-Boolean-Duration-Context
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            Response<Void> response = fileClient.downloadWithResponse(stream, new ShareFileRange(1024, 2047L), false,
                Duration.ofSeconds(30), new Context(key1, value1));

            System.out.printf("Completed downloading file with status code %d%n", response.getStatusCode());
            System.out.printf("Content of the file is: %n%s%n",
                new String(stream.toByteArray(), StandardCharsets.UTF_8));
        } catch (Throwable throwable) {
            System.err.printf("Downloading failed with exception. Message: %s%n", throwable.getMessage());
        }
        // END: com.azure.storage.file.share.ShareFileClient.downloadWithResponse#OutputStream-ShareFileRange-Boolean-Duration-Context
    }

    /**
     * Generates a code sample for using {@link ShareFileClient#downloadWithResponse(OutputStream, ShareFileRange, Boolean,
     * ShareRequestConditions, Duration, Context)}
     */
    public void downloadWithPropertiesWithLease() {
        ShareFileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileClient.downloadWithResponse#OutputStream-ShareFileRange-Boolean-ShareRequestConditions-Duration-Context
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            ShareRequestConditions requestConditions = new ShareRequestConditions().setLeaseId(leaseId);
            Response<Void> response = fileClient.downloadWithResponse(stream, new ShareFileRange(1024, 2047L), false,
                requestConditions, Duration.ofSeconds(30), new Context(key1, value1));

            System.out.printf("Completed downloading file with status code %d%n", response.getStatusCode());
            System.out.printf("Content of the file is: %n%s%n",
                new String(stream.toByteArray(), StandardCharsets.UTF_8));
        } catch (Throwable throwable) {
            System.err.printf("Downloading failed with exception. Message: %s%n", throwable.getMessage());
        }
        // END: com.azure.storage.file.share.ShareFileClient.downloadWithResponse#OutputStream-ShareFileRange-Boolean-ShareRequestConditions-Duration-Context
    }

    /**
     * Generates a code sample for using {@link ShareFileClient#downloadWithResponse(OutputStream, ShareFileDownloadOptions, Duration, Context)}
     */
    public void downloadWithOptions() {
        ShareFileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileClient.downloadWithResponse#OutputStream-ShareFileDownloadOptions-Duration-Context
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            ShareRequestConditions requestConditions = new ShareRequestConditions().setLeaseId(leaseId);
            ShareFileRange range = new ShareFileRange(1024, 2047L);
            DownloadRetryOptions retryOptions = new DownloadRetryOptions().setMaxRetryRequests(3);
            ShareFileDownloadOptions options = new ShareFileDownloadOptions().setRange(range)
                .setRequestConditions(requestConditions)
                .setRangeContentMd5Requested(false)
                .setRetryOptions(retryOptions);
            Response<Void> response = fileClient.downloadWithResponse(stream, options, Duration.ofSeconds(30),
                new Context(key1, value1));

            System.out.printf("Completed downloading file with status code %d%n", response.getStatusCode());
            System.out.printf("Content of the file is: %n%s%n",
                new String(stream.toByteArray(), StandardCharsets.UTF_8));
        } catch (Throwable throwable) {
            System.err.printf("Downloading failed with exception. Message: %s%n", throwable.getMessage());
        }
        // END: com.azure.storage.file.share.ShareFileClient.downloadWithResponse#OutputStream-ShareFileDownloadOptions-Duration-Context
    }

    /**
     * Generates a code sample for using {@link ShareFileClient#downloadToFile(String)}
     */
    public void downloadFile() {
        ShareFileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileClient.downloadToFile#string
        fileClient.downloadToFile("somelocalfilepath");
        if (Files.exists(Paths.get("somelocalfilepath"))) {
            System.out.println("Complete downloading the file.");
        }
        // END: com.azure.storage.file.share.ShareFileClient.downloadToFile#string
    }

    /**
     * Generates a code sample for using {@link ShareFileClient#downloadToFileWithResponse(String, ShareFileRange, Duration,
     * Context)}
     */
    public void downloadFileMaxOverload() {
        ShareFileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileClient.downloadToFileWithResponse#String-ShareFileRange-Duration-Context
        Response<ShareFileProperties> response =
            fileClient.downloadToFileWithResponse("somelocalfilepath", new ShareFileRange(1024, 2047L),
                Duration.ofSeconds(1), Context.NONE);
        if (Files.exists(Paths.get("somelocalfilepath"))) {
            System.out.println("Complete downloading the file with status code " + response.getStatusCode());
        }
        // END: com.azure.storage.file.share.ShareFileClient.downloadToFileWithResponse#String-ShareFileRange-Duration-Context
    }

    /**
     * Generates a code sample for using {@link ShareFileClient#downloadToFileWithResponse(String, ShareFileRange, ShareRequestConditions,
     * Duration, Context)}
     */
    public void downloadFileWithLease() {
        ShareFileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileClient.downloadToFileWithResponse#String-ShareFileRange-ShareRequestConditions-Duration-Context
        ShareRequestConditions requestConditions = new ShareRequestConditions().setLeaseId(leaseId);
        Response<ShareFileProperties> response =
            fileClient.downloadToFileWithResponse("somelocalfilepath", new ShareFileRange(1024, 2047L),
                requestConditions, Duration.ofSeconds(1), Context.NONE);
        if (Files.exists(Paths.get("somelocalfilepath"))) {
            System.out.println("Complete downloading the file with status code " + response.getStatusCode());
        }
        // END: com.azure.storage.file.share.ShareFileClient.downloadToFileWithResponse#String-ShareFileRange-ShareRequestConditions-Duration-Context
    }

    /**
     * Generates a code sample for using {@link ShareFileClient#uploadRangeFromUrl(long, long, long, String)}
     */
    public void uploadFileFromURLAsync() {
        ShareFileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileClient.uploadRangeFromUrl#long-long-long-String
        ShareFileUploadRangeFromUrlInfo response = fileClient.uploadRangeFromUrl(6, 8, 0, "sourceUrl");
        System.out.println("Completed upload range from url!");
        // END: com.azure.storage.file.share.ShareFileClient.uploadRangeFromUrl#long-long-long-String
    }

    /**
     * Generates a code sample for using {@link ShareFileClient#uploadRangeFromUrlWithResponse(long, long, long, String,
     * Duration, Context)}
     */
    public void uploadFileFromURLWithResponseAsync() {
        ShareFileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileClient.uploadRangeFromUrlWithResponse#long-long-long-String-Duration-Context
        Response<ShareFileUploadRangeFromUrlInfo> response =
            fileClient.uploadRangeFromUrlWithResponse(6, 8, 0, "sourceUrl", Duration.ofSeconds(1), Context.NONE);
        System.out.println("Completed upload range from url!");
        // END: com.azure.storage.file.share.ShareFileClient.uploadRangeFromUrlWithResponse#long-long-long-String-Duration-Context
    }

    /**
     * Generates a code sample for using {@link ShareFileClient#uploadRangeFromUrlWithResponse(ShareFileUploadRangeFromUrlOptions,
     * Duration, Context)}
     */
    public void uploadFileFromURLOptionsBagWithResponseAsync() {
        ShareFileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileClient.uploadRangeFromUrlWithResponse#ShareFileUploadRangeFromUrlOptions-Duration-Context
        Response<ShareFileUploadRangeFromUrlInfo> response =
            fileClient.uploadRangeFromUrlWithResponse(new ShareFileUploadRangeFromUrlOptions(6, "sourceUrl")
                .setDestinationOffset(8), Duration.ofSeconds(1), Context.NONE);
        System.out.println("Completed upload range from url!");
        // END: com.azure.storage.file.share.ShareFileClient.uploadRangeFromUrlWithResponse#ShareFileUploadRangeFromUrlOptions-Duration-Context
    }

    /**
     * Generates a code sample for using {@link ShareFileClient#uploadRangeFromUrlWithResponse(long, long, long, String, ShareRequestConditions,
     * Duration, Context)}
     */
    public void uploadFileFromURLWithLeaseAsync() {
        ShareFileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileClient.uploadRangeFromUrlWithResponse#long-long-long-String-ShareRequestConditions-Duration-Context
        ShareRequestConditions requestConditions = new ShareRequestConditions().setLeaseId(leaseId);
        Response<ShareFileUploadRangeFromUrlInfo> response = fileClient.uploadRangeFromUrlWithResponse(6, 8, 0,
            "sourceUrl", requestConditions, Duration.ofSeconds(1), Context.NONE);
        System.out.println("Completed upload range from url!");
        // END: com.azure.storage.file.share.ShareFileClient.uploadRangeFromUrlWithResponse#long-long-long-String-ShareRequestConditions-Duration-Context
    }

    /**
     * Generates a code sample for using {@link ShareFileClient#delete()}
     */
    public void deleteFile() {
        ShareFileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileClient.delete
        fileClient.delete();
        System.out.println("Complete deleting the file.");
        // END: com.azure.storage.file.share.ShareFileClient.delete
    }

    /**
     * Generates a code sample for using {@link ShareFileClient#deleteWithResponse(Duration, Context)}
     */
    public void deleteWithResponse() {
        ShareFileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileClient.deleteWithResponse#duration-context
        Response<Void> response = fileClient.deleteWithResponse(Duration.ofSeconds(1), new Context(key1, value1));
        System.out.println("Complete deleting the file with status code: " + response.getStatusCode());
        // END: com.azure.storage.file.share.ShareFileClient.deleteWithResponse#duration-context
    }

    /**
     * Generates a code sample for using {@link ShareFileClient#deleteWithResponse(ShareRequestConditions, Duration, Context)}
     */
    public void deleteWithLease() {
        ShareFileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileClient.deleteWithResponse#ShareRequestConditions-duration-context
        ShareRequestConditions requestConditions = new ShareRequestConditions().setLeaseId(leaseId);
        Response<Void> response = fileClient.deleteWithResponse(requestConditions, Duration.ofSeconds(1),
            new Context(key1, value1));
        System.out.println("Complete deleting the file with status code: " + response.getStatusCode());
        // END: com.azure.storage.file.share.ShareFileClient.deleteWithResponse#ShareRequestConditions-duration-context
    }

    /**
     * Generates a code sample for using {@link ShareFileClient#getProperties()}
     */
    public void getProperties() {
        ShareFileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileClient.getProperties
        ShareFileProperties properties = fileClient.getProperties();
        System.out.printf("File latest modified date is %s.", properties.getLastModified());
        // END: com.azure.storage.file.share.ShareFileClient.getProperties
    }

    /**
     * Generates a code sample for using {@link ShareFileClient#getPropertiesWithResponse(Duration, Context)}
     */
    public void getPropertiesWithResponse() {
        ShareFileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileClient.getPropertiesWithResponse#duration-context
        Response<ShareFileProperties> response = fileClient.getPropertiesWithResponse(
            Duration.ofSeconds(1), new Context(key1, value1));
        System.out.printf("File latest modified date is %s.", response.getValue().getLastModified());
        // END: com.azure.storage.file.share.ShareFileClient.getPropertiesWithResponse#duration-context
    }

    /**
     * Generates a code sample for using {@link ShareFileClient#getPropertiesWithResponse(ShareRequestConditions, Duration, Context)}
     */
    public void getPropertiesWithLease() {
        ShareFileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileClient.getPropertiesWithResponse#ShareRequestConditions-duration-context
        ShareRequestConditions requestConditions = new ShareRequestConditions().setLeaseId(leaseId);
        Response<ShareFileProperties> response = fileClient.getPropertiesWithResponse(requestConditions,
            Duration.ofSeconds(1), new Context(key1, value1));
        System.out.printf("File latest modified date is %s.", response.getValue().getLastModified());
        // END: com.azure.storage.file.share.ShareFileClient.getPropertiesWithResponse#ShareRequestConditions-duration-context
    }


    /**
     * Generates a code sample for using {@link ShareFileClient#setMetadata(Map)}
     */
    public void setMetadata() {
        ShareFileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileClient.setMetadata#map
        fileClient.setMetadata(Collections.singletonMap("file", "updatedMetadata"));
        System.out.println("Setting the file metadata completed.");
        // END: com.azure.storage.file.share.ShareFileClient.setMetadata#map
    }

    /**
     * Generates a code sample for using {@link ShareFileClient#setMetadataWithResponse(Map, Duration, Context)}
     */
    public void setMetadataWithResponse() {
        ShareFileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileClient.setMetadataWithResponse#map-duration-context
        Response<ShareFileMetadataInfo> response = fileClient.setMetadataWithResponse(
            Collections.singletonMap("file", "updatedMetadata"), Duration.ofSeconds(1), new Context(key1, value1));
        System.out.printf("Setting the file metadata completed with status code %d", response.getStatusCode());
        // END: com.azure.storage.file.share.ShareFileClient.setMetadataWithResponse#map-duration-context
    }

    /**
     * Generates a code sample for using {@link ShareFileClient#setMetadataWithResponse(Map, ShareRequestConditions, Duration, Context)}
     */
    public void setMetadataWithLease() {
        ShareFileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileClient.setMetadataWithResponse#map-ShareRequestConditions-duration-context
        ShareRequestConditions requestConditions = new ShareRequestConditions().setLeaseId(leaseId);
        Response<ShareFileMetadataInfo> response = fileClient.setMetadataWithResponse(
            Collections.singletonMap("file", "updatedMetadata"), requestConditions, Duration.ofSeconds(1),
            new Context(key1, value1));
        System.out.printf("Setting the file metadata completed with status code %d", response.getStatusCode());
        // END: com.azure.storage.file.share.ShareFileClient.setMetadataWithResponse#map-ShareRequestConditions-duration-context
    }

    /**
     * Generates a code sample for using {@link ShareFileClient#setMetadataWithResponse(Map, Duration, Context)} to clear
     * metadata.
     */
    public void clearMetadataWithResponse() {
        ShareFileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileClient.setMetadataWithResponse#map-duration-context.clearMetadata
        Response<ShareFileMetadataInfo> response = fileClient.setMetadataWithResponse(null,
            Duration.ofSeconds(1), new Context(key1, value1));
        System.out.printf("Setting the file metadata completed with status code %d", response.getStatusCode());
        // END: com.azure.storage.file.share.ShareFileClient.setMetadataWithResponse#map-duration-context.clearMetadata
    }

    /**
     * Generates a code sample for using {@link ShareFileClient#setMetadataWithResponse(Map, ShareRequestConditions, Duration, Context)} to clear
     * metadata.
     */
    public void clearMetadataWithLease() {
        ShareFileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileClient.setMetadataWithResponse#map-ShareRequestConditions-duration-context.clearMetadata
        ShareRequestConditions requestConditions = new ShareRequestConditions().setLeaseId(leaseId);
        Response<ShareFileMetadataInfo> response = fileClient.setMetadataWithResponse(null, requestConditions,
            Duration.ofSeconds(1), new Context(key1, value1));
        System.out.printf("Setting the file metadata completed with status code %d", response.getStatusCode());
        // END: com.azure.storage.file.share.ShareFileClient.setMetadataWithResponse#map-ShareRequestConditions-duration-context.clearMetadata
    }

    /**
     * Generates a code sample for using {@link ShareFileClient#setMetadata(Map)} to clear metadata.
     */
    public void clearMetadata() {
        ShareFileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileClient.setMetadata#map.clearMetadata
        fileClient.setMetadata(null);
        System.out.println("Setting the file metadata completed.");
        // END: com.azure.storage.file.share.ShareFileClient.setMetadata#map.clearMetadata
    }

    /**
     * Generates a code sample for using {@link ShareFileClient#setProperties(long, ShareFileHttpHeaders, FileSmbProperties,
     * String)}
     */
    public void setHTTPHeaders() {
        ShareFileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileClient.setProperties#long-ShareFileHttpHeaders-FileSmbProperties-String
        ShareFileHttpHeaders httpHeaders = new ShareFileHttpHeaders()
            .setContentType("text/html")
            .setContentEncoding("gzip")
            .setContentLanguage("en")
            .setCacheControl("no-transform")
            .setContentDisposition("attachment");
        FileSmbProperties smbProperties = new FileSmbProperties()
            .setNtfsFileAttributes(EnumSet.of(NtfsFileAttributes.READ_ONLY))
            .setFileCreationTime(OffsetDateTime.now())
            .setFileLastWriteTime(OffsetDateTime.now())
            .setFilePermissionKey("filePermissionKey");
        String filePermission = "filePermission";
        // NOTE: filePermission and filePermissionKey should never be both set
        fileClient.setProperties(1024, httpHeaders, smbProperties, filePermission);
        System.out.println("Setting the file httpHeaders completed.");
        // END: com.azure.storage.file.share.ShareFileClient.setProperties#long-ShareFileHttpHeaders-FileSmbProperties-String
    }

    /**
     * Generates a code sample for using {@link ShareFileClient#setPropertiesWithResponse(long, ShareFileHttpHeaders,
     * FileSmbProperties, String, ShareRequestConditions, Duration, Context)}
     */
    public void setHttpHeadersWithLease() {
        ShareFileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileClient.setPropertiesWithResponse#long-ShareFileHttpHeaders-FileSmbProperties-String-ShareRequestConditions-Duration-Context
        ShareRequestConditions requestConditions = new ShareRequestConditions().setLeaseId(leaseId);
        ShareFileHttpHeaders httpHeaders = new ShareFileHttpHeaders()
            .setContentType("text/html")
            .setContentEncoding("gzip")
            .setContentLanguage("en")
            .setCacheControl("no-transform")
            .setContentDisposition("attachment");
        FileSmbProperties smbProperties = new FileSmbProperties()
            .setNtfsFileAttributes(EnumSet.of(NtfsFileAttributes.READ_ONLY))
            .setFileCreationTime(OffsetDateTime.now())
            .setFileLastWriteTime(OffsetDateTime.now())
            .setFilePermissionKey("filePermissionKey");
        String filePermission = "filePermission";
        // NOTE: filePermission and filePermissionKey should never be both set
        fileClient.setPropertiesWithResponse(1024, httpHeaders, smbProperties, filePermission, requestConditions, null,
            null);
        System.out.println("Setting the file httpHeaders completed.");
        // END: com.azure.storage.file.share.ShareFileClient.setPropertiesWithResponse#long-ShareFileHttpHeaders-FileSmbProperties-String-ShareRequestConditions-Duration-Context
    }

    /**
     * Generates a code sample for using {@link ShareFileClient#setProperties(long, ShareFileHttpHeaders, FileSmbProperties,
     * String)} to clear httpHeaders and preserve SMB properties.
     */
    public void clearSyncHTTPHeaders() {
        ShareFileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileClient.setProperties#long-ShareFileHttpHeaders-FileSmbProperties-String.clearHttpHeaderspreserveSMBProperties
        ShareFileInfo response = fileClient.setProperties(1024, null, null, null);
        System.out.println("Setting the file httpHeaders completed.");
        // END: com.azure.storage.file.share.ShareFileClient.setProperties#long-ShareFileHttpHeaders-FileSmbProperties-String.clearHttpHeaderspreserveSMBProperties
    }

    /**
     * Generates a code sample for using {@link ShareFileClient#setPropertiesWithResponse(long, ShareFileHttpHeaders,
     * FileSmbProperties, String, Duration, Context)}
     */
    public void setHttpHeadersWithResponse() {
        ShareFileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileClient.setPropertiesWithResponse#long-ShareFileHttpHeaders-FileSmbProperties-String-Duration-Context
        ShareFileHttpHeaders httpHeaders = new ShareFileHttpHeaders()
            .setContentType("text/html")
            .setContentEncoding("gzip")
            .setContentLanguage("en")
            .setCacheControl("no-transform")
            .setContentDisposition("attachment");
        FileSmbProperties smbProperties = new FileSmbProperties()
            .setNtfsFileAttributes(EnumSet.of(NtfsFileAttributes.READ_ONLY))
            .setFileCreationTime(OffsetDateTime.now())
            .setFileLastWriteTime(OffsetDateTime.now())
            .setFilePermissionKey("filePermissionKey");
        String filePermission = "filePermission";
        // NOTE: filePermission and filePermissionKey should never be both set
        Response<ShareFileInfo> response = fileClient.setPropertiesWithResponse(1024, httpHeaders, smbProperties,
            filePermission, Duration.ofSeconds(1), new Context(key1, value1));
        System.out.printf("Setting the file httpHeaders completed with status code %d", response.getStatusCode());
        // END: com.azure.storage.file.share.ShareFileClient.setPropertiesWithResponse#long-ShareFileHttpHeaders-FileSmbProperties-String-Duration-Context
    }

    /**
     * Generates a code sample for using {@link ShareFileClient#setPropertiesWithResponse(long, ShareFileHttpHeaders,
     * FileSmbProperties, String, Duration, Context)} (long, FileHTTPHeaders)} to clear httpHeaders.
     */
    public void clearHTTPHeaders() {
        ShareFileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileClient.setPropertiesWithResponse#long-ShareFileHttpHeaders-FileSmbProperties-String-Duration-Context.clearHttpHeaderspreserveSMBProperties
        Response<ShareFileInfo> response = fileClient.setPropertiesWithResponse(1024, null, null, null,
            Duration.ofSeconds(1), new Context(key1, value1));
        System.out.printf("Setting the file httpHeaders completed with status code %d", response.getStatusCode());
        // END: com.azure.storage.file.share.ShareFileClient.setPropertiesWithResponse#long-ShareFileHttpHeaders-FileSmbProperties-String-Duration-Context.clearHttpHeaderspreserveSMBProperties
    }

    /**
     * Generates a code sample for using {@link ShareFileClient#setPropertiesWithResponse(long, ShareFileHttpHeaders,
     * FileSmbProperties, String, ShareRequestConditions, Duration, Context)} (long, FileHTTPHeaders)} to clear httpHeaders.
     */
    public void clearHTTPHeadersWithLease() {
        ShareFileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileClient.setPropertiesWithResponse#long-ShareFileHttpHeaders-FileSmbProperties-String-ShareRequestConditions-Duration-Context.clearHttpHeaderspreserveSMBProperties
        ShareRequestConditions requestConditions = new ShareRequestConditions().setLeaseId(leaseId);
        Response<ShareFileInfo> response = fileClient.setPropertiesWithResponse(1024, null, null, null, requestConditions,
            Duration.ofSeconds(1), new Context(key1, value1));
        System.out.printf("Setting the file httpHeaders completed with status code %d", response.getStatusCode());
        // END: com.azure.storage.file.share.ShareFileClient.setPropertiesWithResponse#long-ShareFileHttpHeaders-FileSmbProperties-String-ShareRequestConditions-Duration-Context.clearHttpHeaderspreserveSMBProperties
    }

    /**
     * Generates a code sample for using {@link ShareFileClient#listRanges()}
     */
    public void listRanges() {
        ShareFileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileClient.listRanges
        Iterable<ShareFileRange> ranges = fileClient.listRanges();
        ranges.forEach(range ->
            System.out.printf("List ranges completed with start: %d, end: %d", range.getStart(), range.getEnd()));
        // END: com.azure.storage.file.share.ShareFileClient.listRanges
    }

    /**
     * Generates a code sample for using {@link ShareFileClient#listRanges(ShareFileRange, Duration, Context)}
     */
    public void listRangesMaxOverload() {
        ShareFileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileClient.listRanges#ShareFileRange-Duration-Context
        Iterable<ShareFileRange> ranges = fileClient.listRanges(new ShareFileRange(1024, 2048L), Duration.ofSeconds(1),
            new Context(key1, value1));
        ranges.forEach(range ->
            System.out.printf("List ranges completed with start: %d, end: %d", range.getStart(), range.getEnd()));
        // END: com.azure.storage.file.share.ShareFileClient.listRanges#ShareFileRange-Duration-Context
    }

    /**
     * Generates a code sample for using {@link ShareFileClient#listRangesDiff(String)}
     */
    public void listRangesDiffOverload() {
        ShareFileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileClient.listRangesDiff#String
        ShareFileRangeList rangeList = fileClient.listRangesDiff("previoussnapshot");
        System.out.println("Valid Share File Ranges are:");
        for (FileRange range : rangeList.getRanges()) {
            System.out.printf("Start: %s, End: %s%n", range.getStart(), range.getEnd());
        }
        // END: com.azure.storage.file.share.ShareFileClient.listRangesDiff#String
    }

    /**
     * Generates a code sample for using {@link ShareFileClient#listRangesDiffWithResponse(ShareFileListRangesDiffOptions, Duration, Context)}
     */
    public void listRangesDiffOptionalOverload() {
        ShareFileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileClient.listRangesDiffWithResponse#ShareFileListRangesDiffOptions-Duration-Context
        ShareFileRangeList rangeList = fileClient.listRangesDiffWithResponse(
            new ShareFileListRangesDiffOptions("previoussnapshot")
            .setRange(new ShareFileRange(1024, 2048L)), Duration.ofSeconds(1), new Context(key1, value1)).getValue();
        System.out.println("Valid Share File Ranges are:");
        for (FileRange range : rangeList.getRanges()) {
            System.out.printf("Start: %s, End: %s%n", range.getStart(), range.getEnd());
        }
        // END: com.azure.storage.file.share.ShareFileClient.listRangesDiffWithResponse#ShareFileListRangesDiffOptions-Duration-Context
    }

    /**
     * Generates a code sample for using {@link ShareFileClient#listRanges(ShareFileRange, ShareRequestConditions, Duration, Context)}
     */
    public void listRangesWithLease() {
        ShareFileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileClient.listRanges#ShareFileRange-ShareRequestConditions-Duration-Context
        ShareRequestConditions requestConditions = new ShareRequestConditions().setLeaseId(leaseId);
        Iterable<ShareFileRange> ranges = fileClient.listRanges(new ShareFileRange(1024, 2048L), requestConditions,
            Duration.ofSeconds(1), new Context(key1, value1));
        ranges.forEach(range ->
            System.out.printf("List ranges completed with start: %d, end: %d", range.getStart(), range.getEnd()));
        // END: com.azure.storage.file.share.ShareFileClient.listRanges#ShareFileRange-ShareRequestConditions-Duration-Context
    }

    /**
     * Generates a code sample for using {@link ShareFileClient#listHandles()}
     */
    public void listHandles() {
        ShareFileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileClient.listHandles
        fileClient.listHandles()
            .forEach(handleItem -> System.out.printf("List handles completed with handleId %s",
                handleItem.getHandleId()));
        // END: com.azure.storage.file.share.ShareFileClient.listHandles
    }

    /**
     * Generates a code sample for using {@link ShareFileClient#listHandles(Integer, Duration, Context)}
     */
    public void listHandlesWithOverload() {
        ShareFileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileClient.listHandles#integer-duration-context
        fileClient.listHandles(10, Duration.ofSeconds(1), new Context(key1, value1))
            .forEach(handleItem -> System.out.printf("List handles completed with handleId %s",
                handleItem.getHandleId()));
        // END: com.azure.storage.file.share.ShareFileClient.listHandles#integer-duration-context
    }

    /**
     * Code snippet for {@link ShareFileClient#forceCloseHandle(String)}.
     */
    public void forceCloseHandle() {
        ShareFileClient fileClient = createClientWithCredential();
        // BEGIN: com.azure.storage.file.share.ShareFileClient.forceCloseHandle#String
        fileClient.listHandles().forEach(handleItem -> {
            fileClient.forceCloseHandle(handleItem.getHandleId());
            System.out.printf("Closed handle %s on resource %s%n", handleItem.getHandleId(), handleItem.getPath());
        });
        // END: com.azure.storage.file.share.ShareFileClient.forceCloseHandle#String
    }

    /**
     * Code snippet for {@link ShareFileClient#forceCloseHandleWithResponse(String, Duration, Context)}.
     */
    public void forceCloseHandleWithResponse() {
        ShareFileClient fileClient = createClientWithCredential();
        // BEGIN: com.azure.storage.file.share.ShareFileClient.forceCloseHandleWithResponse#String
        fileClient.listHandles().forEach(handleItem -> {
            Response<CloseHandlesInfo> closeResponse = fileClient
                .forceCloseHandleWithResponse(handleItem.getHandleId(), Duration.ofSeconds(30), Context.NONE);
            System.out.printf("Closing handle %s on resource %s completed with status code %d%n",
                handleItem.getHandleId(), handleItem.getPath(), closeResponse.getStatusCode());
        });
        // END: com.azure.storage.file.share.ShareFileClient.forceCloseHandleWithResponse#String
    }

    /**
     * Code snippet for {@link ShareFileClient#forceCloseAllHandles(Duration, Context)}.
     */
    public void forceCloseAllHandles() {
        ShareFileClient fileClient = createClientWithCredential();
        // BEGIN: com.azure.storage.file.share.ShareFileClient.forceCloseAllHandles#Duration-Context
        CloseHandlesInfo closeHandlesInfo = fileClient.forceCloseAllHandles(Duration.ofSeconds(30), Context.NONE);
        System.out.printf("Closed %d open handles on the file%n", closeHandlesInfo.getClosedHandles());
        System.out.printf("Failed to close %d open handles on the file%n", closeHandlesInfo.getFailedHandles());
        // END: com.azure.storage.file.share.ShareFileClient.forceCloseAllHandles#Duration-Context
    }

    /**
     * Generates a code sample for using {@link ShareFileClient#getShareSnapshotId()}
     */
    public void getShareSnapshotId() {
        // BEGIN: com.azure.storage.file.share.ShareFileClient.getShareSnapshotId
        OffsetDateTime currentTime = OffsetDateTime.of(LocalDateTime.now(), ZoneOffset.UTC);
        ShareFileClient fileClient = new ShareFileClientBuilder()
            .endpoint("https://${accountName}.file.core.windows.net")
            .sasToken("${SASToken}")
            .shareName("myshare")
            .resourcePath("myfile")
            .snapshot(currentTime.toString())
            .buildFileClient();

        System.out.printf("Snapshot ID: %s%n", fileClient.getShareSnapshotId());
        // END: com.azure.storage.file.share.ShareFileClient.getShareSnapshotId
    }

    /**
     * Generates a code sample for using {@link ShareFileClient#getShareName()}
     */
    public void getShareName() {
        ShareFileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileClient.getShareName
        String shareName = fileClient.getShareName();
        System.out.println("The share name of the directory is " + shareName);
        // END: com.azure.storage.file.share.ShareFileClient.getShareName
    }

    /**
     * Generates a code sample for using {@link ShareFileClient#getFilePath()}
     */
    public void getName() {
        ShareFileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileClient.getFilePath
        String filePath = fileClient.getFilePath();
        System.out.println("The name of the file is " + filePath);
        // END: com.azure.storage.file.share.ShareFileClient.getFilePath
    }

    /**
     * Code snippet for {@link ShareFileClient#generateSas(ShareServiceSasSignatureValues)}
     */
    public void generateSas() {
        ShareFileClient shareFileClient = createClientWithCredential();
        // BEGIN: com.azure.storage.file.share.ShareFileClient.generateSas#ShareServiceSasSignatureValues
        OffsetDateTime expiryTime = OffsetDateTime.now().plusDays(1);
        ShareFileSasPermission permission = new ShareFileSasPermission().setReadPermission(true);

        ShareServiceSasSignatureValues values = new ShareServiceSasSignatureValues(expiryTime, permission)
            .setStartTime(OffsetDateTime.now());

        shareFileClient.generateSas(values); // Client must be authenticated via StorageSharedKeyCredential
        // END: com.azure.storage.file.share.ShareFileClient.generateSas#ShareServiceSasSignatureValues
    }

    /**
     * Code snippet for {@link ShareFileClient#generateSas(ShareServiceSasSignatureValues, Context)}
     */
    public void generateSasWithContext() {
        ShareFileClient shareFileClient = createClientWithCredential();
        // BEGIN: com.azure.storage.file.share.ShareFileClient.generateSas#ShareServiceSasSignatureValues-Context
        OffsetDateTime expiryTime = OffsetDateTime.now().plusDays(1);
        ShareFileSasPermission permission = new ShareFileSasPermission().setReadPermission(true);

        ShareServiceSasSignatureValues values = new ShareServiceSasSignatureValues(expiryTime, permission)
            .setStartTime(OffsetDateTime.now());

        // Client must be authenticated via StorageSharedKeyCredential
        shareFileClient.generateSas(values, new Context("key", "value"));
        // END: com.azure.storage.file.share.ShareFileClient.generateSas#ShareServiceSasSignatureValues-Context
    }
}
