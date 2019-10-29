// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file;

import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.file.share.ShareFileSmbProperties;
import com.azure.storage.file.share.ShareFileClient;
import com.azure.storage.file.share.ShareFileAsyncClient;
import com.azure.storage.file.share.ShareFileClientBuilder;
import com.azure.storage.file.share.models.FileCopyInfo;
import com.azure.storage.file.share.models.FileHttpHeaders;
import com.azure.storage.file.share.models.FileInfo;
import com.azure.storage.file.share.models.FileMetadataInfo;
import com.azure.storage.file.share.models.FileProperties;
import com.azure.storage.file.share.models.FileRange;
import com.azure.storage.file.share.models.FileUploadInfo;
import com.azure.storage.file.share.models.FileUploadRangeFromUrlInfo;
import com.azure.storage.file.share.models.NtfsFileAttributes;

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
     * Generates a code sample for using {@link ShareFileClient#create(long)}
     */
    public void createFile() {
        ShareFileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileClient.create
        FileInfo response = fileClient.create(1024);
        System.out.println("Complete creating the file.");
        // END: com.azure.storage.file.share.ShareFileClient.create
    }

    /**
     * Generates a code sample for using {@link ShareFileClient#createWithResponse(long, FileHttpHeaders, ShareFileSmbProperties,
     * String, Map, Duration, Context)}
     */
    public void createWithResponse() {
        ShareFileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileClient.createWithResponse#long-FileHttpHeaders-ShareFileSmbProperties-String-Map-Duration-Context
        FileHttpHeaders httpHeaders = new FileHttpHeaders()
            .setContentType("text/html")
            .setContentEncoding("gzip")
            .setContentLanguage("en")
            .setCacheControl("no-transform")
            .setContentDisposition("attachment");
        ShareFileSmbProperties smbProperties = new ShareFileSmbProperties()
            .setNtfsFileAttributes(EnumSet.of(NtfsFileAttributes.READ_ONLY))
            .setFileCreationTime(OffsetDateTime.now())
            .setFileLastWriteTime(OffsetDateTime.now())
            .setFilePermissionKey("filePermissionKey");
        String filePermission = "filePermission";
        // NOTE: filePermission and filePermissionKey should never be both set
        Response<FileInfo> response = fileClient.createWithResponse(1024, httpHeaders, smbProperties, filePermission,
            Collections.singletonMap("directory", "metadata"), Duration.ofSeconds(1), new Context(key1, value1));
        System.out.printf("Creating the file completed with status code %d", response.getStatusCode());
        // END: com.azure.storage.file.share.ShareFileClient.createWithResponse#long-FileHttpHeaders-ShareFileSmbProperties-String-Map-Duration-Context
    }

    /**
     * Generates a code sample for using {@link ShareFileClient#beginCopy(String, Map, Duration)}
     */
    public void beginCopy() {
        ShareFileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileClient.beginCopy#string-map-duration
        SyncPoller<FileCopyInfo, Void> poller = fileClient.beginCopy(
            "https://{accountName}.file.core.windows.net?{SASToken}",
            Collections.singletonMap("file", "metadata"), Duration.ofSeconds(2));

        final PollResponse<FileCopyInfo> pollResponse = poller.poll();
        final FileCopyInfo value = pollResponse.getValue();
        System.out.printf("Copy source: %s. Status: %s.%n", value.getCopySourceUrl(), value.getCopyStatus());

        // END: com.azure.storage.file.share.ShareFileClient.beginCopy#string-map-duration
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
     * Generates a code sample for using {@link ShareFileClient#upload(InputStream, long)}
     */
    public void uploadData() {
        ShareFileClient fileClient = createClientWithSASToken();
        byte[] data = "default".getBytes(StandardCharsets.UTF_8);

        // BEGIN: com.azure.storage.file.share.ShareFileClient.upload#InputStream-long
        InputStream uploadData = new ByteArrayInputStream(data);
        FileUploadInfo response = fileClient.upload(uploadData, data.length);
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
        Response<FileUploadInfo> response = fileClient.uploadWithResponse(uploadData, data.length, 0L,
            Duration.ofSeconds(30), null);
        System.out.printf("Completed uploading the data with response %d%n.", response.getStatusCode());
        System.out.printf("ETag of the file is %s%n", response.getValue().getETag());
        // END: com.azure.storage.file.share.ShareFileClient.uploadWithResponse#InputStream-long-Long-Duration-Context
    }

    /**
     * Generates a code sample for using {@link ShareFileClient#clearRange(long)}
     */
    public void clearRange() {
        ShareFileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileClient.clearRange#long
        FileUploadInfo response = fileClient.clearRange(1024);
        System.out.println("Complete clearing the range with eTag: " + response.getETag());
        // END: com.azure.storage.file.share.ShareFileClient.clearRange#long
    }

    /**
     * Generates a code sample for using {@link ShareFileClient#clearRangeWithResponse(long, long, Duration, Context)}
     */
    public void clearRangeMaxOverload() {
        ShareFileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileClient.clearRangeWithResponse#long-long-Duration-Context
        Response<FileUploadInfo> response = fileClient.clearRangeWithResponse(1024, 1024,
            Duration.ofSeconds(1), new Context(key1, value1));
        System.out.println("Complete clearing the range with status code: " + response.getStatusCode());
        // END: com.azure.storage.file.share.ShareFileClient.clearRangeWithResponse#long-long-Duration-Context
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
     * Generates a code sample for using {@link ShareFileClient#downloadWithResponse(OutputStream, FileRange, Boolean,
     * Duration, Context)}
     */
    public void downloadWithPropertiesWithResponse() {
        ShareFileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileClient.downloadWithResponse#OutputStream-FileRange-Boolean-Duration-Context
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            Response<Void> response = fileClient.downloadWithResponse(stream, new FileRange(1024, 2047L), false,
                Duration.ofSeconds(30), new Context(key1, value1));

            System.out.printf("Completed downloading file with status code %d%n", response.getStatusCode());
            System.out.printf("Content of the file is: %n%s%n",
                new String(stream.toByteArray(), StandardCharsets.UTF_8));
        } catch (Throwable throwable) {
            System.err.printf("Downloading failed with exception. Message: %s%n", throwable.getMessage());
        }
        // END: com.azure.storage.file.share.ShareFileClient.downloadWithResponse#OutputStream-FileRange-Boolean-Duration-Context
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
     * Generates a code sample for using {@link ShareFileClient#downloadToFileWithResponse(String, FileRange, Duration,
     * Context)}
     */
    public void downloadFileMaxOverload() {
        ShareFileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileClient.downloadToFileWithResponse#string-filerange-duration-context
        Response<FileProperties> response =
            fileClient.downloadToFileWithResponse("somelocalfilepath", new FileRange(1024, 2047L),
                Duration.ofSeconds(1), Context.NONE);
        if (Files.exists(Paths.get("somelocalfilepath"))) {
            System.out.println("Complete downloading the file with status code " + response.getStatusCode());
        }
        // END: com.azure.storage.file.share.ShareFileClient.downloadToFileWithResponse#string-filerange-duration-context
    }

    /**
     * Generates a code sample for using {@link ShareFileClient#uploadRangeFromUrl(long, long, long, String)}
     */
    public void uploadFileFromURLAsync() {
        ShareFileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileClient.uploadRangeFromUrl#long-long-long-String
        FileUploadRangeFromUrlInfo response = fileClient.uploadRangeFromUrl(6, 8, 0, "sourceUrl");
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
        Response<FileUploadRangeFromUrlInfo> response = fileClient.uploadRangeFromUrlWithResponse(6, 8, 0, "sourceUrl",
            Duration.ofSeconds(1), Context.NONE);
        System.out.println("Completed upload range from url!");
        // END: com.azure.storage.file.share.ShareFileClient.uploadRangeFromUrlWithResponse#long-long-long-String-Duration-Context
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
     * Generates a code sample for using {@link ShareFileClient#getProperties()}
     */
    public void getProperties() {
        ShareFileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileClient.getProperties
        FileProperties properties = fileClient.getProperties();
        System.out.printf("File latest modified date is %s.", properties.getLastModified());
        // END: com.azure.storage.file.share.ShareFileClient.getProperties
    }

    /**
     * Generates a code sample for using {@link ShareFileClient#getPropertiesWithResponse(Duration, Context)}
     */
    public void getPropertiesWithResponse() {
        ShareFileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileClient.getPropertiesWithResponse#duration-context
        Response<FileProperties> response = fileClient.getPropertiesWithResponse(
            Duration.ofSeconds(1), new Context(key1, value1));
        System.out.printf("File latest modified date is %s.", response.getValue().getLastModified());
        // END: com.azure.storage.file.share.ShareFileClient.getPropertiesWithResponse#duration-context
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
        Response<FileMetadataInfo> response = fileClient.setMetadataWithResponse(
            Collections.singletonMap("file", "updatedMetadata"), Duration.ofSeconds(1), new Context(key1, value1));
        System.out.printf("Setting the file metadata completed with status code %d", response.getStatusCode());
        // END: com.azure.storage.file.share.ShareFileClient.setMetadataWithResponse#map-duration-context
    }

    /**
     * Generates a code sample for using {@link ShareFileClient#setMetadataWithResponse(Map, Duration, Context)} to clear
     * metadata.
     */
    public void clearMetadataWithResponse() {
        ShareFileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileClient.setMetadataWithResponse#map-duration-context.clearMetadata
        Response<FileMetadataInfo> response = fileClient.setMetadataWithResponse(null,
            Duration.ofSeconds(1), new Context(key1, value1));
        System.out.printf("Setting the file metadata completed with status code %d", response.getStatusCode());
        // END: com.azure.storage.file.share.ShareFileClient.setMetadataWithResponse#map-duration-context.clearMetadata
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
     * Generates a code sample for using {@link ShareFileClient#setProperties(long, FileHttpHeaders, ShareFileSmbProperties,
     * String)}
     */
    public void setHTTPHeaders() {
        ShareFileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileClient.setProperties#long-FileHttpHeaders-ShareFileSmbProperties-String
        FileHttpHeaders httpHeaders = new FileHttpHeaders()
            .setContentType("text/html")
            .setContentEncoding("gzip")
            .setContentLanguage("en")
            .setCacheControl("no-transform")
            .setContentDisposition("attachment");
        ShareFileSmbProperties smbProperties = new ShareFileSmbProperties()
            .setNtfsFileAttributes(EnumSet.of(NtfsFileAttributes.READ_ONLY))
            .setFileCreationTime(OffsetDateTime.now())
            .setFileLastWriteTime(OffsetDateTime.now())
            .setFilePermissionKey("filePermissionKey");
        String filePermission = "filePermission";
        // NOTE: filePermission and filePermissionKey should never be both set
        fileClient.setProperties(1024, httpHeaders, smbProperties, filePermission);
        System.out.println("Setting the file httpHeaders completed.");
        // END: com.azure.storage.file.share.ShareFileClient.setProperties#long-FileHttpHeaders-ShareFileSmbProperties-String
    }

    /**
     * Generates a code sample for using {@link ShareFileClient#setProperties(long, FileHttpHeaders, ShareFileSmbProperties,
     * String)} to clear httpHeaders and preserve SMB properties.
     */
    public void clearSyncHTTPHeaders() {
        ShareFileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileClient.setProperties#long-FileHttpHeaders-ShareFileSmbProperties-String.clearHttpHeaderspreserveSMBProperties
        FileInfo response = fileClient.setProperties(1024, null, null, null);
        System.out.println("Setting the file httpHeaders completed.");
        // END: com.azure.storage.file.share.ShareFileClient.setProperties#long-FileHttpHeaders-ShareFileSmbProperties-String.clearHttpHeaderspreserveSMBProperties
    }

    /**
     * Generates a code sample for using {@link ShareFileClient#setPropertiesWithResponse(long, FileHttpHeaders,
     * ShareFileSmbProperties, String, Duration, Context)}
     */
    public void setHttpHeadersWithResponse() {
        ShareFileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileClient.setPropertiesWithResponse#long-FileHttpHeaders-ShareFileSmbProperties-String-Duration-Context
        FileHttpHeaders httpHeaders = new FileHttpHeaders()
            .setContentType("text/html")
            .setContentEncoding("gzip")
            .setContentLanguage("en")
            .setCacheControl("no-transform")
            .setContentDisposition("attachment");
        ShareFileSmbProperties smbProperties = new ShareFileSmbProperties()
            .setNtfsFileAttributes(EnumSet.of(NtfsFileAttributes.READ_ONLY))
            .setFileCreationTime(OffsetDateTime.now())
            .setFileLastWriteTime(OffsetDateTime.now())
            .setFilePermissionKey("filePermissionKey");
        String filePermission = "filePermission";
        // NOTE: filePermission and filePermissionKey should never be both set
        Response<FileInfo> response = fileClient.setPropertiesWithResponse(1024, httpHeaders, smbProperties,
            filePermission, Duration.ofSeconds(1), new Context(key1, value1));
        System.out.printf("Setting the file httpHeaders completed with status code %d", response.getStatusCode());
        // END: com.azure.storage.file.share.ShareFileClient.setPropertiesWithResponse#long-FileHttpHeaders-ShareFileSmbProperties-String-Duration-Context
    }

    /**
     * Generates a code sample for using {@link ShareFileClient#setPropertiesWithResponse(long, FileHttpHeaders,
     * ShareFileSmbProperties, String, Duration, Context)} (long, FileHTTPHeaders)} to clear httpHeaders.
     */
    public void clearHTTPHeaders() {
        ShareFileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileClient.setPropertiesWithResponse#long-FileHttpHeaders-ShareFileSmbProperties-String-Duration-Context.clearHttpHeaderspreserveSMBProperties
        Response<FileInfo> response = fileClient.setPropertiesWithResponse(1024, null, null, null,
            Duration.ofSeconds(1), new Context(key1, value1));
        System.out.printf("Setting the file httpHeaders completed with status code %d", response.getStatusCode());
        // END: com.azure.storage.file.share.ShareFileClient.setPropertiesWithResponse#long-FileHttpHeaders-ShareFileSmbProperties-String-Duration-Context.clearHttpHeaderspreserveSMBProperties
    }

    /**
     * Generates a code sample for using {@link ShareFileClient#listRanges()}
     */
    public void listRanges() {
        ShareFileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileClient.listRanges
        Iterable<FileRange> ranges = fileClient.listRanges();
        ranges.forEach(range ->
            System.out.printf("List ranges completed with start: %d, end: %d", range.getStart(), range.getEnd()));
        // END: com.azure.storage.file.share.ShareFileClient.listRanges
    }

    /**
     * Generates a code sample for using {@link ShareFileClient#listRanges(FileRange, Duration, Context)}
     */
    public void listRangesMaxOverload() {
        ShareFileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareFileClient.listRanges#filerange-duration-context
        Iterable<FileRange> ranges = fileClient.listRanges(new FileRange(1024, 2048L), Duration.ofSeconds(1),
            new Context(key1, value1));
        ranges.forEach(range ->
            System.out.printf("List ranges completed with start: %d, end: %d", range.getStart(), range.getEnd()));
        // END: com.azure.storage.file.share.ShareFileClient.listRanges#filerange-duration-context
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
            Response<Void> closeResponse = fileClient
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
        int closedHandleCount = fileClient.forceCloseAllHandles(Duration.ofSeconds(30), Context.NONE);
        System.out.printf("Closed %d open handles on the file%n", closedHandleCount);
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
}
