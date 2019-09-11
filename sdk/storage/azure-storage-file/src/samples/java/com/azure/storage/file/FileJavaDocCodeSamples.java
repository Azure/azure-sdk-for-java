// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file;

import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.VoidResponse;
import com.azure.storage.common.Constants;
import com.azure.storage.common.IPRange;
import com.azure.storage.common.SASProtocol;
import com.azure.storage.common.Utility;
import com.azure.core.util.Context;
import com.azure.storage.common.credentials.SASTokenCredential;
import com.azure.storage.common.credentials.SharedKeyCredential;
import com.azure.storage.file.models.FileCopyInfo;
import com.azure.storage.file.models.FileDownloadInfo;
import com.azure.storage.file.models.FileHTTPHeaders;
import com.azure.storage.file.models.FileInfo;
import com.azure.storage.file.models.FileMetadataInfo;
import com.azure.storage.file.models.FileProperties;
import com.azure.storage.file.models.FileRange;
import com.azure.storage.file.models.FileUploadInfo;
import com.azure.storage.file.models.FileUploadRangeFromURLInfo;
import com.azure.storage.file.models.NtfsFileAttributes;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
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
     * Generates code sample for creating a {@link FileClient} with {@link SASTokenCredential}
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
     * Generates code sample for creating a {@link FileClient} with {@link SASTokenCredential}
     * @return An instance of {@link FileClient}
     */
    public FileClient createClientWithCredential() {

        // BEGIN: com.azure.storage.file.fileClient.instantiation.credential
        FileClient fileClient = new FileClientBuilder()
            .endpoint("https://${accountName}.file.core.windows.net")
            .credential(SASTokenCredential.fromQueryParameters(Utility.parseQueryString("${SASTokenQueryParams}")))
            .shareName("myshare")
            .resourcePath("myfilepath")
            .buildFileClient();
        // END: com.azure.storage.file.fileClient.instantiation.credential
        return fileClient;
    }

    /**
     * Generates code sample for creating a {@link FileClient} with {@code connectionString}
     * which turns into {@link SharedKeyCredential}
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
     * Generates a code sample for using {@link FileClient#createWithResponse(long, FileHTTPHeaders, FileSmbProperties,
     * String, Map, Duration, Context)}
     */
    public void createWithResponse() {
        FileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileClient.createWithResponse#long-filehttpheaders-filesmbproperties-string-map-duration-context
        FileHTTPHeaders httpHeaders = new FileHTTPHeaders()
            .setFileContentType("text/html")
            .setFileContentEncoding("gzip")
            .setFileContentLanguage("en")
            .setFileCacheControl("no-transform")
            .setFileContentDisposition("attachment");
        FileSmbProperties smbProperties = new FileSmbProperties()
            .ntfsFileAttributes(EnumSet.of(NtfsFileAttributes.READ_ONLY))
            .fileCreationTime(OffsetDateTime.now())
            .fileLastWriteTime(OffsetDateTime.now())
            .filePermissionKey("filePermissionKey");
        String filePermission = "filePermission";
        // NOTE: filePermission and filePermissionKey should never be both set
        Response<FileInfo> response = fileClient.createWithResponse(1024, httpHeaders, smbProperties, filePermission,
            Collections.singletonMap("directory", "metadata"), Duration.ofSeconds(1), new Context(key1, value1));
        System.out.printf("Creating the file completed with status code %d", response.statusCode());
        // END: com.azure.storage.file.fileClient.createWithResponse#long-filehttpheaders-filesmbproperties-string-map-duration-context
    }

    /**
     * Generates a code sample for using {@link FileClient#startCopy(String, Map)}
     */
    public void startCopy() {
        FileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileClient.startCopy#string-map
        FileCopyInfo response = fileClient.startCopy(
            "https://{accountName}.file.core.windows.net?{SASToken}",
            Collections.singletonMap("file", "metadata"));
        System.out.println("Complete copying the file with copy Id: " + response.copyId());
        // END: com.azure.storage.file.fileClient.startCopy#string-map
    }

    /**
     * Generates a code sample for using {@link FileClient#startCopyWithResponse(String, Map, Duration, Context)}
     */
    public void startCopyWithResponse() {
        FileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileClient.startCopyWithResponse#string-map-duration-context
        Response<FileCopyInfo> response = fileClient.startCopyWithResponse(
            "https://{accountName}.file.core.windows.net?{SASToken}",
            Collections.singletonMap("file", "metadata"), Duration.ofSeconds(1), new Context(key1, value1));
        System.out.println("Complete copying the file with copy Id: " + response.value().copyId());
        // END: com.azure.storage.file.fileClient.startCopyWithResponse#string-map-duration-context
    }

    /**
     * Generates a code sample for using {@link FileClient#abortCopy(String)}
     */
    public void abortCopyFile() {
        FileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileClient.abortCopy#string
        fileClient.abortCopy("someCopyId");
        System.out.printf("Abort copying the file completed.");
        // END: com.azure.storage.file.fileClient.abortCopy#string
    }

    /**
     * Generates a code sample for using {@link FileClient#abortCopyWithResponse(String, Duration, Context)}
     */
    public void abortCopyWithResponse() {
        FileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileClient.abortCopyWithResponse#string-duration-context
        VoidResponse response = fileClient.abortCopyWithResponse("someCopyId", Duration.ofSeconds(1),
            new Context(key1, value1));
        System.out.printf("Abort copying the file completed with status code %d", response.statusCode());
        // END: com.azure.storage.file.fileClient.abortCopyWithResponse#string-duration-context
    }

    /**
     * Generates a code sample for using {@link FileClient#upload(ByteBuffer, long)}
     */
    public void uploadData() {
        FileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileClient.upload#bytebuffer-long
        ByteBuffer defaultData = ByteBuffer.wrap("default".getBytes(StandardCharsets.UTF_8));
        FileUploadInfo response = fileClient.upload(defaultData, defaultData.remaining());
        System.out.println("Complete uploading the data with eTag: " + response.eTag());
        // END: com.azure.storage.file.fileClient.upload#bytebuffer-long
    }

    /**
     * Generates a code sample for using {@link FileClient#uploadWithResponse(ByteBuffer, long, Duration, Context)}
     */
    public void uploadWithResponse() {
        FileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.FileClient.uploadWithResponse#ByteBuffer-long-Duration-Context
        ByteBuffer defaultData = ByteBuffer.wrap("default".getBytes(StandardCharsets.UTF_8));
        Response<FileUploadInfo> response = fileClient.uploadWithResponse(defaultData, defaultData.remaining(),
            Duration.ofSeconds(1), new Context(key1, value1));
        System.out.println("Complete uploading the data with status code: " + response.statusCode());
        // END: com.azure.storage.file.FileClient.uploadWithResponse#ByteBuffer-long-Duration-Context
    }

    /**
     * Generates a code sample for using {@link FileClient#uploadWithResponse(ByteBuffer, long, long,
     * Duration, Context)}
     */
    public void uploadWithResponseMaxOverload() {
        FileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.FileClient.uploadWithResponse#ByteBuffer-long-long-Duration-Context
        ByteBuffer defaultData = ByteBuffer.wrap("default".getBytes(StandardCharsets.UTF_8));
        Response<FileUploadInfo> response = fileClient.uploadWithResponse(defaultData, defaultData.remaining(),
            1024, Duration.ofSeconds(1), new Context(key1, value1));
        System.out.println("Complete uploading the data with status code: " + response.statusCode());
        // END: com.azure.storage.file.FileClient.uploadWithResponse#ByteBuffer-long-long-Duration-Context
    }

    /**
     * Generates a code sample for using {@link FileClient#clearRange(long)}
     */
    public void clearRange() {
        FileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileClient.clearRange#long
        FileUploadInfo response = fileClient.clearRange(1024);
        System.out.println("Complete clearing the range with eTag: " + response.eTag());
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
        System.out.println("Complete clearing the range with status code: " + response.statusCode());
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
                fileClient.getProperties().contentLength());
        }
        // END: com.azure.storage.file.fileClient.uploadFromFile#string-filerangewritetype
    }

    /**
     * Generates a code sample for using {@link FileClient#downloadWithProperties()}
     */
    public void downloadData() {
        FileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileClient.downloadWithProperties
        FileDownloadInfo response = fileClient.downloadWithProperties();
        System.out.println("Complete downloading the data.");
        response.body().subscribe(
            byteBuffer ->  System.out.println("Complete downloading the data with body: "
                + new String(byteBuffer.array(), StandardCharsets.UTF_8)),
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete downloading the data!")
        );
        // END: com.azure.storage.file.fileClient.downloadWithProperties
    }

    /**
     * Generates a code sample for using {@link FileClient#downloadWithPropertiesWithResponse(
     * FileRange, Boolean, Duration, Context)}
     */
    public void downloadWithPropertiesWithResponse() {
        FileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.FileClient.downloadWithPropertiesWithResponse#FileRange-Boolean-Duration-Context
        Response<FileDownloadInfo> response = fileClient.downloadWithPropertiesWithResponse(new FileRange(1024, 2047L),
            false, Duration.ofSeconds(1), new Context(key1, value1));
        System.out.println("Complete downloading the data with status code: " + response.statusCode());
        response.value().body().subscribe(
            byteBuffer ->  System.out.println("Complete downloading the data with body: "
                + new String(byteBuffer.array(), StandardCharsets.UTF_8)),
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete downloading the data!")
        );
        // END: com.azure.storage.file.FileClient.downloadWithPropertiesWithResponse#FileRange-Boolean-Duration-Context
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
     * Generates a code sample for using {@link FileClient#downloadToFile(String, FileRange)}
     */
    public void downloadFileMaxOverload() {
        FileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileClient.downloadToFile#string-filerange
        fileClient.downloadToFile("somelocalfilepath", new FileRange(1024, 2047L));
        if (Files.exists(Paths.get("somelocalfilepath"))) {
            System.out.println("Complete downloading the file.");
        }
        // END: com.azure.storage.file.fileClient.downloadToFile#string-filerange
    }

    /**
     * Generates a code sample for using {@link FileClient#uploadRangeFromURL(long, long, long, URI)}
     * @throws URISyntaxException when the URI is invalid
     */
    public void uploadFileFromURLAsync() throws URISyntaxException {
        FileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileClient.uploadRangeFromURL#long-long-long-uri
        FileUploadRangeFromURLInfo response = fileClient.uploadRangeFromURL(6, 8, 0, new URI("filewithSAStoken"));
        System.out.println("Completed upload range from url!");
        // END: com.azure.storage.file.fileClient.uploadRangeFromURL#long-long-long-uri
    }

    /**
     * Generates a code sample for using {@link FileClient#uploadRangeFromURLWithResponse(long, long, long, URI, Duration, Context)}
     * @throws URISyntaxException when the URI is invalid
     */
    public void uploadFileFromURLWithResponseAsync() throws URISyntaxException {
        FileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileClient.uploadRangeFromURLWithResponse#long-long-long-uri-duration-context
        Response<FileUploadRangeFromURLInfo> response = fileClient.uploadRangeFromURLWithResponse(6,
            8, 0, new URI("filewithSAStoken"), Duration.ofSeconds(1), Context.NONE);
        System.out.println("Completed upload range from url!");
        // END: com.azure.storage.file.fileClient.uploadRangeFromURLWithResponse#long-long-long-uri-duration-context
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
        VoidResponse response = fileClient.deleteWithResponse(Duration.ofSeconds(1), new Context(key1, value1));
        System.out.println("Complete deleting the file with status code: " + response.statusCode());
        // END: com.azure.storage.file.fileClient.deleteWithResponse#duration-context
    }

    /**
     * Generates a code sample for using {@link FileClient#getProperties()}
     */
    public void getProperties() {
        FileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileClient.getProperties
        FileProperties properties = fileClient.getProperties();
        System.out.printf("File latest modified date is %s.", properties.lastModified());
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
        System.out.printf("File latest modified date is %s.", response.value().lastModified());
        // END: com.azure.storage.file.fileClient.getPropertiesWithResponse#duration-context
    }

    /**
     * Generates a code sample for using {@link FileClient#setMetadata(Map)}
     */
    public void setMetadata() {
        FileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileClient.setMetadata#map
        fileClient.setMetadata(Collections.singletonMap("file", "updatedMetadata"));
        System.out.printf("Setting the file metadata completed.");
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
        System.out.printf("Setting the file metadata completed with status code %d", response.statusCode());
        // END: com.azure.storage.file.fileClient.setMetadataWithResponse#map-duration-context
    }

    /**
     * Generates a code sample for using {@link FileClient#setMetadataWithResponse(Map,
     * Duration, Context)} to clear metadata.
     */
    public void clearMetadataWithResponse() {
        FileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileClient.setMetadataWithResponse#map-duration-context.clearMetadata
        Response<FileMetadataInfo> response = fileClient.setMetadataWithResponse(null,
            Duration.ofSeconds(1), new Context(key1, value1));
        System.out.printf("Setting the file metadata completed with status code %d", response.statusCode());
        // END: com.azure.storage.file.fileClient.setMetadataWithResponse#map-duration-context.clearMetadata
    }

    /**
     * Generates a code sample for using {@link FileClient#setMetadata(Map)} to clear metadata.
     */
    public void clearMetadata() {
        FileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileClient.setMetadata#map.clearMetadata
        fileClient.setMetadata(null);
        System.out.printf("Setting the file metadata completed.");
        // END: com.azure.storage.file.fileClient.setMetadata#map.clearMetadata
    }

    /**
     * Generates a code sample for using {@link FileClient#setProperties(long, FileHTTPHeaders, FileSmbProperties, String)}
     */
    public void setHTTPHeaders() {
        FileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileClient.setProperties#long-filehttpheaders-filesmbproperties-string
        FileHTTPHeaders httpHeaders = new FileHTTPHeaders()
            .setFileContentType("text/html")
            .setFileContentEncoding("gzip")
            .setFileContentLanguage("en")
            .setFileCacheControl("no-transform")
            .setFileContentDisposition("attachment");
        FileSmbProperties smbProperties = new FileSmbProperties()
            .ntfsFileAttributes(EnumSet.of(NtfsFileAttributes.READ_ONLY))
            .fileCreationTime(OffsetDateTime.now())
            .fileLastWriteTime(OffsetDateTime.now())
            .filePermissionKey("filePermissionKey");
        String filePermission = "filePermission";
        // NOTE: filePermission and filePermissionKey should never be both set
        fileClient.setProperties(1024, httpHeaders, smbProperties, filePermission);
        System.out.printf("Setting the file httpHeaders completed.");
        // END: com.azure.storage.file.fileClient.setProperties#long-filehttpheaders-filesmbproperties-string
    }

    /**
     * Generates a code sample for using {@link FileClient#setProperties(long, FileHTTPHeaders, FileSmbProperties, String)}
     * to clear httpHeaders and preserve SMB properties.
     */
    public void clearSyncHTTPHeaders() {
        FileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileClient.setProperties#long-filehttpheaders-filesmbproperties-string.clearHttpHeaderspreserveSMBProperties
        FileInfo response = fileClient.setProperties(1024, null, null, null);
        System.out.printf("Setting the file httpHeaders completed.");
        // END: com.azure.storage.file.fileClient.setProperties#long-filehttpheaders-filesmbproperties-string.clearHttpHeaderspreserveSMBProperties
    }

    /**
     * Generates a code sample for using {@link FileClient#setPropertiesWithResponse(long, FileHTTPHeaders,
     * FileSmbProperties, String, Duration, Context)}
     */
    public void setHttpHeadersWithResponse() {
        FileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileClient.setPropertiesWithResponse#long-filehttpheaders-filesmbproperties-string-duration-Context
        FileHTTPHeaders httpHeaders = new FileHTTPHeaders()
            .setFileContentType("text/html")
            .setFileContentEncoding("gzip")
            .setFileContentLanguage("en")
            .setFileCacheControl("no-transform")
            .setFileContentDisposition("attachment");
        FileSmbProperties smbProperties = new FileSmbProperties()
            .ntfsFileAttributes(EnumSet.of(NtfsFileAttributes.READ_ONLY))
            .fileCreationTime(OffsetDateTime.now())
            .fileLastWriteTime(OffsetDateTime.now())
            .filePermissionKey("filePermissionKey");
        String filePermission = "filePermission";
        // NOTE: filePermission and filePermissionKey should never be both set
        Response<FileInfo> response = fileClient.setPropertiesWithResponse(1024, httpHeaders, smbProperties,
            filePermission, Duration.ofSeconds(1), new Context(key1, value1));
        System.out.printf("Setting the file httpHeaders completed with status code %d", response.statusCode());
        // END: com.azure.storage.file.fileClient.setPropertiesWithResponse#long-filehttpheaders-filesmbproperties-string-duration-Context
    }

    /**
     * Generates a code sample for using {@link FileClient#setPropertiesWithResponse(long, FileHTTPHeaders, FileSmbProperties, String, Duration, Context)}
     * (long, FileHTTPHeaders)} to clear httpHeaders.
     */
    public void clearHTTPHeaders() {
        FileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileClient.setPropertiesWithResponse#long-filehttpheaders-filesmbproperties-string-duration-Context.clearHttpHeaderspreserveSMBProperties
        Response<FileInfo> response = fileClient.setPropertiesWithResponse(1024, null, null, null,
            Duration.ofSeconds(1), new Context(key1, value1));
        System.out.printf("Setting the file httpHeaders completed with status code %d", response.statusCode());
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
            System.out.printf("List ranges completed with start: %d, end: %d", range.start(), range.end()));
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
            System.out.printf("List ranges completed with start: %d, end: %d", range.start(), range.end()));
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
     * Generates a code sample for using {@link FileClient#forceCloseHandles(String, Duration, Context)}
     */
    public void forceCloseHandles() {
        FileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileClient.forceCloseHandles#string-duration-context
        fileClient.listHandles(10, Duration.ofSeconds(1), new Context(key1, value1))
            .forEach(result ->
                fileClient.forceCloseHandles(result.getHandleId(), Duration.ofSeconds(1),
                    new Context(key1, value1)).forEach(numOfClosedHandles ->
                    System.out.printf("Close %d handles.", numOfClosedHandles)
                ));
        // END: com.azure.storage.file.fileClient.forceCloseHandles#string-duration-context
    }

    /**
     * Generates a code sample for using {@link FileClient#generateSAS(String, FileSASPermission, OffsetDateTime,
     * OffsetDateTime, String, SASProtocol, IPRange, String, String, String, String, String)}
     */
    public void generateSAS() {
        FileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.FileClient.generateSAS#String-FileSASPermission-OffsetDateTime-OffsetDateTime-String-SASProtocol-IPRange-String-String-String-String-String
        String identifier = "identifier";
        FileSASPermission permissions = new FileSASPermission()
            .read(true)
            .create(true)
            .delete(true)
            .write(true);
        OffsetDateTime startTime = OffsetDateTime.now().minusDays(1);
        OffsetDateTime expiryTime = OffsetDateTime.now().plusDays(1);
        IPRange ipRange = new IPRange()
            .ipMin("0.0.0.0")
            .ipMax("255.255.255.255");
        SASProtocol sasProtocol = SASProtocol.HTTPS_HTTP;
        String cacheControl = "cache";
        String contentDisposition = "disposition";
        String contentEncoding = "encoding";
        String contentLanguage = "language";
        String contentType = "type";
        String version = Constants.HeaderConstants.TARGET_STORAGE_VERSION;
        String sas = fileClient.generateSAS(identifier, permissions, expiryTime, startTime, version, sasProtocol,
            ipRange, cacheControl, contentDisposition, contentEncoding, contentLanguage, contentType);
        // END: com.azure.storage.file.FileClient.generateSAS#String-FileSASPermission-OffsetDateTime-OffsetDateTime-String-SASProtocol-IPRange-String-String-String-String-String
    }

     /**
     * Generates a code sample for using {@link FileClient#getShareSnapshotId()}
     */
    public void getShareSnapshotId() {
        // BEGIN: com.azure.storage.file.fileClient.getShareSnapshotId
        OffsetDateTime currentTime = OffsetDateTime.of(LocalDateTime.now(), ZoneOffset.UTC);
        FileClient fileClient = new FileClientBuilder()
            .endpoint("https://${accountName}.file.core.windows.net")
            .credential(SASTokenCredential.fromSASTokenString("${SASToken}"))
            .shareName("myshare")
            .resourcePath("myfile")
            .snapshot(currentTime.toString())
            .buildFileClient();

        System.out.printf("Snapshot ID: %s%n", fileClient.getShareSnapshotId());
        // END: com.azure.storage.file.fileClient.getShareSnapshotId
    }
}
