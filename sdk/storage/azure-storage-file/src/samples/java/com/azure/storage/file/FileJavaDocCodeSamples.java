// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file;

import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.polling.Poller;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.file.models.FileCopyInfo;
import com.azure.storage.file.models.FileHttpHeaders;
import com.azure.storage.file.models.FileInfo;
import com.azure.storage.file.models.FileMetadataInfo;
import com.azure.storage.file.models.FileProperties;
import com.azure.storage.file.models.FileRange;
import com.azure.storage.file.models.FileUploadInfo;
import com.azure.storage.file.models.FileUploadRangeFromUrlInfo;
import com.azure.storage.file.models.NtfsFileAttributes;

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
 * Contains code snippets when generating javadocs through doclets for {@link FileClient} and {@link FileAsyncClient}.
 */
public class FileJavaDocCodeSamples {

    private String key1 = "key1";
    private String value1 = "val1";

    /**
     * Generates code sample for {@link FileClient} instantiation.
     */
    public void initialization() {
        // BEGIN: com.azure.storage.file.fileClient.instantiation
        FileClient client = new FileClientBuilder()
            .connectionString("${connectionString}")
            .endpoint("${endpoint}")
            .buildFileClient();
        // END: com.azure.storage.file.fileClient.instantiation
    }

    /**
     * Generates code sample for creating a {@link FileClient} with SAS token.
     *
     * @return An instance of {@link FileClient}
     */
    public FileClient createClientWithSASToken() {

        // BEGIN: com.azure.storage.file.fileClient.instantiation.sastoken
        FileClient fileClient = new FileClientBuilder()
            .endpoint("https://${accountName}.file.core.windows.net?${SASToken}")
            .shareName("myshare")
            .resourcePath("myfilepath")
            .buildFileClient();
        // END: com.azure.storage.file.fileClient.instantiation.sastoken
        return fileClient;
    }


    /**
     * Generates code sample for creating a {@link FileClient} with SAS token.
     *
     * @return An instance of {@link FileClient}
     */
    public FileClient createClientWithCredential() {

        // BEGIN: com.azure.storage.file.fileClient.instantiation.credential
        FileClient fileClient = new FileClientBuilder()
            .endpoint("https://${accountName}.file.core.windows.net")
            .sasToken("${SASTokenQueryParams}")
            .shareName("myshare")
            .resourcePath("myfilepath")
            .buildFileClient();
        // END: com.azure.storage.file.fileClient.instantiation.credential
        return fileClient;
    }

    /**
     * Generates code sample for creating a {@link FileClient} with {@code connectionString} which turns into {@link
     * StorageSharedKeyCredential}
     *
     * @return An instance of {@link FileClient}
     */
    public FileClient createClientWithConnectionString() {
        // BEGIN: com.azure.storage.file.fileClient.instantiation.connectionstring
        String connectionString = "DefaultEndpointsProtocol=https;AccountName={name};AccountKey={key};"
            + "EndpointSuffix={core.windows.net}";
        FileClient fileClient = new FileClientBuilder()
            .connectionString(connectionString).shareName("myshare").resourcePath("myfilepath")
            .buildFileClient();
        // END: com.azure.storage.file.fileClient.instantiation.connectionstring
        return fileClient;
    }

    /**
     * Generates a code sample for using {@link FileClient#create(long)}
     */
    public void createFile() {
        FileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileClient.create
        FileInfo response = fileClient.create(1024);
        System.out.println("Complete creating the file.");
        // END: com.azure.storage.file.fileClient.create
    }

    /**
     * Generates a code sample for using {@link FileClient#createWithResponse(long, FileHttpHeaders, FileSmbProperties,
     * String, Map, Duration, Context)}
     */
    public void createWithResponse() {
        FileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileClient.createWithResponse#long-filehttpheaders-filesmbproperties-string-map-duration-context
        FileHttpHeaders httpHeaders = new FileHttpHeaders()
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
        Response<FileInfo> response = fileClient.createWithResponse(1024, httpHeaders, smbProperties, filePermission,
            Collections.singletonMap("directory", "metadata"), Duration.ofSeconds(1), new Context(key1, value1));
        System.out.printf("Creating the file completed with status code %d", response.getStatusCode());
        // END: com.azure.storage.file.fileClient.createWithResponse#long-filehttpheaders-filesmbproperties-string-map-duration-context
    }

    /**
     * Generates a code sample for using {@link FileClient#beginCopy(String, Map, Duration)}
     */
    public void beginCopy() {
        FileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileClient.beginCopy#string-map-duration
        Poller<FileCopyInfo, Void> poller = fileClient.beginCopy(
            "https://{accountName}.file.core.windows.net?{SASToken}",
            Collections.singletonMap("file", "metadata"), Duration.ofSeconds(2));

        poller.getObserver().subscribe(response -> {
                final FileCopyInfo value = response.getValue();
                System.out.printf("Copy source: %s. Status: %s.%n", value.getCopySourceUrl(), value.getCopyStatus());
            });
        // END: com.azure.storage.file.fileClient.beginCopy#string-map-duration
    }

    /**
     * Generates a code sample for using {@link FileClient#abortCopy(String)}
     */
    public void abortCopyFile() {
        FileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileClient.abortCopy#string
        fileClient.abortCopy("someCopyId");
        System.out.println("Abort copying the file completed.");
        // END: com.azure.storage.file.fileClient.abortCopy#string
    }

    /**
     * Generates a code sample for using {@link FileClient#abortCopyWithResponse(String, Duration, Context)}
     */
    public void abortCopyWithResponse() {
        FileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileClient.abortCopyWithResponse#string-duration-context
        Response<Void> response = fileClient.abortCopyWithResponse("someCopyId", Duration.ofSeconds(1),
            new Context(key1, value1));
        System.out.printf("Abort copying the file completed with status code %d", response.getStatusCode());
        // END: com.azure.storage.file.fileClient.abortCopyWithResponse#string-duration-context
    }

    /**
     * Generates a code sample for using {@link FileClient#upload(InputStream, long)}
     */
    public void uploadData() {
        FileClient fileClient = createClientWithSASToken();
        byte[] data = "default".getBytes(StandardCharsets.UTF_8);

        // BEGIN: com.azure.storage.file.FileClient.upload#InputStream-long
        InputStream uploadData = new ByteArrayInputStream(data);
        FileUploadInfo response = fileClient.upload(uploadData, data.length);
        System.out.println("Complete uploading the data with eTag: " + response.getETag());
        // END: com.azure.storage.file.FileClient.upload#InputStream-long
    }

    /**
     * Code snippet for {@link FileClient#uploadWithResponse(InputStream, long, Long, Duration, Context)}.
     */
    public void uploadWithResponse() {
        FileClient fileClient = createClientWithCredential();
        byte[] data = "default".getBytes(StandardCharsets.UTF_8);

        // BEGIN: com.azure.storage.file.FileClient.uploadWithResponse#InputStream-long-Long-Duration-Context
        InputStream uploadData = new ByteArrayInputStream(data);
        Response<FileUploadInfo> response = fileClient.uploadWithResponse(uploadData, data.length, 0L,
            Duration.ofSeconds(30), null);
        System.out.printf("Completed uploading the data with response %d%n.", response.getStatusCode());
        System.out.printf("ETag of the file is %s%n", response.getValue().getETag());
        // END: com.azure.storage.file.FileClient.uploadWithResponse#InputStream-long-Long-Duration-Context
    }

    /**
     * Generates a code sample for using {@link FileClient#clearRange(long)}
     */
    public void clearRange() {
        FileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileClient.clearRange#long
        FileUploadInfo response = fileClient.clearRange(1024);
        System.out.println("Complete clearing the range with eTag: " + response.getETag());
        // END: com.azure.storage.file.fileClient.clearRange#long
    }

    /**
     * Generates a code sample for using {@link FileClient#clearRangeWithResponse(long, long, Duration, Context)}
     */
    public void clearRangeMaxOverload() {
        FileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.FileClient.clearRangeWithResponse#long-long-Duration-Context
        Response<FileUploadInfo> response = fileClient.clearRangeWithResponse(1024, 1024,
            Duration.ofSeconds(1), new Context(key1, value1));
        System.out.println("Complete clearing the range with status code: " + response.getStatusCode());
        // END: com.azure.storage.file.FileClient.clearRangeWithResponse#long-long-Duration-Context
    }

    /**
     * Generates a code sample for using {@link FileClient#uploadFromFile(String)}
     */
    public void uploadFile() {
        FileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileClient.uploadFromFile#string
        fileClient.uploadFromFile("someFilePath");
        // END: com.azure.storage.file.fileClient.uploadFromFile#string
    }

    /**
     * Generates a code sample for using {@link FileClient#uploadFromFile(String)}
     */
    public void uploadFileMaxOverload() {
        FileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileClient.uploadFromFile#string-filerangewritetype
        fileClient.uploadFromFile("someFilePath");
        if (fileClient.getProperties() != null) {
            System.out.printf("Upload the file with length of %d completed",
                fileClient.getProperties().getContentLength());
        }
        // END: com.azure.storage.file.fileClient.uploadFromFile#string-filerangewritetype
    }

    /**
     * Generates a code sample for using {@link FileClient#download(OutputStream)}
     */
    public void download() {
        FileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.FileClient.download#OutputStream
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            fileClient.download(stream);
            System.out.printf("Completed downloading the file with content: %n%s%n",
                new String(stream.toByteArray(), StandardCharsets.UTF_8));
        } catch (Throwable throwable) {
            System.err.printf("Downloading failed with exception. Message: %s%n", throwable.getMessage());
        }
        // END: com.azure.storage.file.FileClient.download#OutputStream
    }

    /**
     * Generates a code sample for using {@link FileClient#downloadWithResponse(OutputStream, FileRange, Boolean,
     * Duration, Context)}
     */
    public void downloadWithPropertiesWithResponse() {
        FileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.FileClient.downloadWithResponse#OutputStream-FileRange-Boolean-Duration-Context
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
        // END: com.azure.storage.file.FileClient.downloadWithResponse#OutputStream-FileRange-Boolean-Duration-Context
    }

    /**
     * Generates a code sample for using {@link FileClient#downloadToFile(String)}
     */
    public void downloadFile() {
        FileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileClient.downloadToFile#string
        fileClient.downloadToFile("somelocalfilepath");
        if (Files.exists(Paths.get("somelocalfilepath"))) {
            System.out.println("Complete downloading the file.");
        }
        // END: com.azure.storage.file.fileClient.downloadToFile#string
    }

    /**
     * Generates a code sample for using {@link FileClient#downloadToFileWithResponse(String, FileRange, Duration,
     * Context)}
     */
    public void downloadFileMaxOverload() {
        FileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileClient.downloadToFileWithResponse#string-filerange-duration-context
        Response<FileProperties> response =
            fileClient.downloadToFileWithResponse("somelocalfilepath", new FileRange(1024, 2047L),
                Duration.ofSeconds(1), Context.NONE);
        if (Files.exists(Paths.get("somelocalfilepath"))) {
            System.out.println("Complete downloading the file with status code " + response.getStatusCode());
        }
        // END: com.azure.storage.file.fileClient.downloadToFileWithResponse#string-filerange-duration-context
    }

    /**
     * Generates a code sample for using {@link FileClient#uploadRangeFromUrl(long, long, long, String)}
     */
    public void uploadFileFromURLAsync() {
        FileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.FileClient.uploadRangeFromUrl#long-long-long-String
        FileUploadRangeFromUrlInfo response = fileClient.uploadRangeFromUrl(6, 8, 0, "sourceUrl");
        System.out.println("Completed upload range from url!");
        // END: com.azure.storage.file.FileClient.uploadRangeFromUrl#long-long-long-String
    }

    /**
     * Generates a code sample for using {@link FileClient#uploadRangeFromUrlWithResponse(long, long, long, String,
     * Duration, Context)}
     */
    public void uploadFileFromURLWithResponseAsync() {
        FileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.FileClient.uploadRangeFromUrlWithResponse#long-long-long-String-Duration-Context
        Response<FileUploadRangeFromUrlInfo> response = fileClient.uploadRangeFromUrlWithResponse(6, 8, 0, "sourceUrl",
            Duration.ofSeconds(1), Context.NONE);
        System.out.println("Completed upload range from url!");
        // END: com.azure.storage.file.FileClient.uploadRangeFromUrlWithResponse#long-long-long-String-Duration-Context
    }

    /**
     * Generates a code sample for using {@link FileClient#delete()}
     */
    public void deleteFile() {
        FileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileClient.delete
        fileClient.delete();
        System.out.println("Complete deleting the file.");
        // END: com.azure.storage.file.fileClient.delete
    }

    /**
     * Generates a code sample for using {@link FileClient#deleteWithResponse(Duration, Context)}
     */
    public void deleteWithResponse() {
        FileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileClient.deleteWithResponse#duration-context
        Response<Void> response = fileClient.deleteWithResponse(Duration.ofSeconds(1), new Context(key1, value1));
        System.out.println("Complete deleting the file with status code: " + response.getStatusCode());
        // END: com.azure.storage.file.fileClient.deleteWithResponse#duration-context
    }

    /**
     * Generates a code sample for using {@link FileClient#getProperties()}
     */
    public void getProperties() {
        FileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileClient.getProperties
        FileProperties properties = fileClient.getProperties();
        System.out.printf("File latest modified date is %s.", properties.getLastModified());
        // END: com.azure.storage.file.fileClient.getProperties
    }

    /**
     * Generates a code sample for using {@link FileClient#getPropertiesWithResponse(Duration, Context)}
     */
    public void getPropertiesWithResponse() {
        FileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileClient.getPropertiesWithResponse#duration-context
        Response<FileProperties> response = fileClient.getPropertiesWithResponse(
            Duration.ofSeconds(1), new Context(key1, value1));
        System.out.printf("File latest modified date is %s.", response.getValue().getLastModified());
        // END: com.azure.storage.file.fileClient.getPropertiesWithResponse#duration-context
    }

    /**
     * Generates a code sample for using {@link FileClient#setMetadata(Map)}
     */
    public void setMetadata() {
        FileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileClient.setMetadata#map
        fileClient.setMetadata(Collections.singletonMap("file", "updatedMetadata"));
        System.out.println("Setting the file metadata completed.");
        // END: com.azure.storage.file.fileClient.setMetadata#map
    }

    /**
     * Generates a code sample for using {@link FileClient#setMetadataWithResponse(Map, Duration, Context)}
     */
    public void setMetadataWithResponse() {
        FileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileClient.setMetadataWithResponse#map-duration-context
        Response<FileMetadataInfo> response = fileClient.setMetadataWithResponse(
            Collections.singletonMap("file", "updatedMetadata"), Duration.ofSeconds(1), new Context(key1, value1));
        System.out.printf("Setting the file metadata completed with status code %d", response.getStatusCode());
        // END: com.azure.storage.file.fileClient.setMetadataWithResponse#map-duration-context
    }

    /**
     * Generates a code sample for using {@link FileClient#setMetadataWithResponse(Map, Duration, Context)} to clear
     * metadata.
     */
    public void clearMetadataWithResponse() {
        FileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileClient.setMetadataWithResponse#map-duration-context.clearMetadata
        Response<FileMetadataInfo> response = fileClient.setMetadataWithResponse(null,
            Duration.ofSeconds(1), new Context(key1, value1));
        System.out.printf("Setting the file metadata completed with status code %d", response.getStatusCode());
        // END: com.azure.storage.file.fileClient.setMetadataWithResponse#map-duration-context.clearMetadata
    }

    /**
     * Generates a code sample for using {@link FileClient#setMetadata(Map)} to clear metadata.
     */
    public void clearMetadata() {
        FileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileClient.setMetadata#map.clearMetadata
        fileClient.setMetadata(null);
        System.out.println("Setting the file metadata completed.");
        // END: com.azure.storage.file.fileClient.setMetadata#map.clearMetadata
    }

    /**
     * Generates a code sample for using {@link FileClient#setProperties(long, FileHttpHeaders, FileSmbProperties,
     * String)}
     */
    public void setHTTPHeaders() {
        FileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileClient.setProperties#long-filehttpheaders-filesmbproperties-string
        FileHttpHeaders httpHeaders = new FileHttpHeaders()
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
        // END: com.azure.storage.file.fileClient.setProperties#long-filehttpheaders-filesmbproperties-string
    }

    /**
     * Generates a code sample for using {@link FileClient#setProperties(long, FileHttpHeaders, FileSmbProperties,
     * String)} to clear httpHeaders and preserve SMB properties.
     */
    public void clearSyncHTTPHeaders() {
        FileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileClient.setProperties#long-filehttpheaders-filesmbproperties-string.clearHttpHeaderspreserveSMBProperties
        FileInfo response = fileClient.setProperties(1024, null, null, null);
        System.out.println("Setting the file httpHeaders completed.");
        // END: com.azure.storage.file.fileClient.setProperties#long-filehttpheaders-filesmbproperties-string.clearHttpHeaderspreserveSMBProperties
    }

    /**
     * Generates a code sample for using {@link FileClient#setPropertiesWithResponse(long, FileHttpHeaders,
     * FileSmbProperties, String, Duration, Context)}
     */
    public void setHttpHeadersWithResponse() {
        FileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileClient.setPropertiesWithResponse#long-filehttpheaders-filesmbproperties-string-duration-Context
        FileHttpHeaders httpHeaders = new FileHttpHeaders()
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
        Response<FileInfo> response = fileClient.setPropertiesWithResponse(1024, httpHeaders, smbProperties,
            filePermission, Duration.ofSeconds(1), new Context(key1, value1));
        System.out.printf("Setting the file httpHeaders completed with status code %d", response.getStatusCode());
        // END: com.azure.storage.file.fileClient.setPropertiesWithResponse#long-filehttpheaders-filesmbproperties-string-duration-Context
    }

    /**
     * Generates a code sample for using {@link FileClient#setPropertiesWithResponse(long, FileHttpHeaders,
     * FileSmbProperties, String, Duration, Context)} (long, FileHTTPHeaders)} to clear httpHeaders.
     */
    public void clearHTTPHeaders() {
        FileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileClient.setPropertiesWithResponse#long-filehttpheaders-filesmbproperties-string-duration-Context.clearHttpHeaderspreserveSMBProperties
        Response<FileInfo> response = fileClient.setPropertiesWithResponse(1024, null, null, null,
            Duration.ofSeconds(1), new Context(key1, value1));
        System.out.printf("Setting the file httpHeaders completed with status code %d", response.getStatusCode());
        // END: com.azure.storage.file.fileClient.setPropertiesWithResponse#long-filehttpheaders-filesmbproperties-string-duration-Context.clearHttpHeaderspreserveSMBProperties
    }

    /**
     * Generates a code sample for using {@link FileClient#listRanges()}
     */
    public void listRanges() {
        FileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileClient.listRanges
        Iterable<FileRange> ranges = fileClient.listRanges();
        ranges.forEach(range ->
            System.out.printf("List ranges completed with start: %d, end: %d", range.getStart(), range.getEnd()));
        // END: com.azure.storage.file.fileClient.listRanges
    }

    /**
     * Generates a code sample for using {@link FileClient#listRanges(FileRange, Duration, Context)}
     */
    public void listRangesMaxOverload() {
        FileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileClient.listRanges#filerange-duration-context
        Iterable<FileRange> ranges = fileClient.listRanges(new FileRange(1024, 2048L), Duration.ofSeconds(1),
            new Context(key1, value1));
        ranges.forEach(range ->
            System.out.printf("List ranges completed with start: %d, end: %d", range.getStart(), range.getEnd()));
        // END: com.azure.storage.file.fileClient.listRanges#filerange-duration-context
    }

    /**
     * Generates a code sample for using {@link FileClient#listHandles()}
     */
    public void listHandles() {
        FileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileClient.listHandles
        fileClient.listHandles()
            .forEach(handleItem -> System.out.printf("List handles completed with handleId %s",
                handleItem.getHandleId()));
        // END: com.azure.storage.file.fileClient.listHandles
    }

    /**
     * Generates a code sample for using {@link FileClient#listHandles(Integer, Duration, Context)}
     */
    public void listHandlesWithOverload() {
        FileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileClient.listHandles#integer-duration-context
        fileClient.listHandles(10, Duration.ofSeconds(1), new Context(key1, value1))
            .forEach(handleItem -> System.out.printf("List handles completed with handleId %s",
                handleItem.getHandleId()));
        // END: com.azure.storage.file.fileClient.listHandles#integer-duration-context
    }

    /**
     * Code snippet for {@link FileClient#forceCloseHandle(String)}.
     */
    public void forceCloseHandle() {
        FileClient fileClient = createClientWithCredential();
        // BEGIN: com.azure.storage.file.FileClient.forceCloseHandle#String
        fileClient.listHandles().forEach(handleItem -> {
            fileClient.forceCloseHandle(handleItem.getHandleId());
            System.out.printf("Closed handle %s on resource %s%n", handleItem.getHandleId(), handleItem.getPath());
        });
        // END: com.azure.storage.file.FileClient.forceCloseHandle#String
    }

    /**
     * Code snippet for {@link FileClient#forceCloseHandleWithResponse(String, Duration, Context)}.
     */
    public void forceCloseHandleWithResponse() {
        FileClient fileClient = createClientWithCredential();
        // BEGIN: com.azure.storage.file.FileClient.forceCloseHandleWithResponse#String
        fileClient.listHandles().forEach(handleItem -> {
            Response<Void> closeResponse = fileClient
                .forceCloseHandleWithResponse(handleItem.getHandleId(), Duration.ofSeconds(30), Context.NONE);
            System.out.printf("Closing handle %s on resource %s completed with status code %d%n",
                handleItem.getHandleId(), handleItem.getPath(), closeResponse.getStatusCode());
        });
        // END: com.azure.storage.file.FileClient.forceCloseHandleWithResponse#String
    }

    /**
     * Code snippet for {@link FileClient#forceCloseAllHandles(Duration, Context)}.
     */
    public void forceCloseAllHandles() {
        FileClient fileClient = createClientWithCredential();
        // BEGIN: com.azure.storage.file.FileClient.forceCloseAllHandles#Duration-Context
        int closedHandleCount = fileClient.forceCloseAllHandles(Duration.ofSeconds(30), Context.NONE);
        System.out.printf("Closed %d open handles on the file%n", closedHandleCount);
        // END: com.azure.storage.file.FileClient.forceCloseAllHandles#Duration-Context
    }

    /**
     * Generates a code sample for using {@link FileClient#getShareSnapshotId()}
     */
    public void getShareSnapshotId() {
        // BEGIN: com.azure.storage.file.fileClient.getShareSnapshotId
        OffsetDateTime currentTime = OffsetDateTime.of(LocalDateTime.now(), ZoneOffset.UTC);
        FileClient fileClient = new FileClientBuilder()
            .endpoint("https://${accountName}.file.core.windows.net")
            .sasToken("${SASToken}")
            .shareName("myshare")
            .resourcePath("myfile")
            .snapshot(currentTime.toString())
            .buildFileClient();

        System.out.printf("Snapshot ID: %s%n", fileClient.getShareSnapshotId());
        // END: com.azure.storage.file.fileClient.getShareSnapshotId
    }

    /**
     * Generates a code sample for using {@link FileClient#getShareName()}
     */
    public void getShareName() {
        FileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileClient.getShareName
        String shareName = fileClient.getShareName();
        System.out.println("The share name of the directory is " + shareName);
        // END: com.azure.storage.file.fileClient.getShareName
    }

    /**
     * Generates a code sample for using {@link FileClient#getFilePath()}
     */
    public void getName() {
        FileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileClient.getFilePath
        String filePath = fileClient.getFilePath();
        System.out.println("The name of the file is " + filePath);
        // END: com.azure.storage.file.fileClient.getFilePath
    }
}
