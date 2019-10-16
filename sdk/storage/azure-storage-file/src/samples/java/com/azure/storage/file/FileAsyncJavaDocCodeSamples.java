// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file;

import com.azure.core.util.polling.Poller;
import com.azure.storage.common.credentials.SharedKeyCredential;
import com.azure.storage.file.models.FileCopyInfo;
import com.azure.storage.file.models.FileHttpHeaders;
import com.azure.storage.file.models.FileProperties;
import com.azure.storage.file.models.FileRange;
import com.azure.storage.file.models.NtfsFileAttributes;
import reactor.core.publisher.Flux;

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
public class FileAsyncJavaDocCodeSamples {

    /**
     * Generates code sample for {@link FileAsyncClient} instantiation.
     */
    public void asyncInitialization() {
        // BEGIN: com.azure.storage.file.fileAsyncClient.instantiation
        FileAsyncClient client = new FileClientBuilder()
            .connectionString("${connectionString}")
            .endpoint("${endpoint}")
            .buildFileAsyncClient();
        // END: com.azure.storage.file.fileAsyncClient.instantiation
    }

    /**
     * Generates code sample for creating a {@link FileAsyncClient} with SAS token.
     * @return An instance of {@link FileAsyncClient}
     */
    public FileAsyncClient createAsyncClientWithSASToken() {
        // BEGIN: com.azure.storage.file.fileAsyncClient.instantiation.sastoken
        FileAsyncClient fileAsyncClient = new FileClientBuilder()
            .endpoint("https://{accountName}.file.core.windows.net?{SASToken}")
            .shareName("myshare")
            .resourcePath("myfilepath")
            .buildFileAsyncClient();
        // END: com.azure.storage.file.fileAsyncClient.instantiation.sastoken
        return fileAsyncClient;
    }

    /**
     * Generates code sample for creating a {@link FileAsyncClient} with SAS token.
     * @return An instance of {@link FileAsyncClient}
     */
    public FileAsyncClient createAsyncClientWithCredential() {
        // BEGIN: com.azure.storage.file.fileAsyncClient.instantiation.credential
        FileAsyncClient fileAsyncClient = new FileClientBuilder()
            .endpoint("https://{accountName}.file.core.windows.net")
            .sasToken("${SASTokenQueryParams}")
            .shareName("myshare")
            .resourcePath("myfilepath")
            .buildFileAsyncClient();
        // END: com.azure.storage.file.fileAsyncClient.instantiation.credential
        return fileAsyncClient;
    }

    /**
     * Generates code sample for creating a {@link FileAsyncClient} with {@code connectionString}
     * which turns into {@link SharedKeyCredential}
     * @return An instance of {@link FileAsyncClient}
     */
    public FileAsyncClient createAsyncClientWithConnectionString() {
        // BEGIN: com.azure.storage.file.fileAsyncClient.instantiation.connectionstring
        String connectionString = "DefaultEndpointsProtocol=https;AccountName={name};AccountKey={key};"
            + "EndpointSuffix={core.windows.net}";
        FileAsyncClient fileAsyncClient = new FileClientBuilder()
            .connectionString(connectionString).shareName("myshare").resourcePath("myfilepath")
            .buildFileAsyncClient();
        // END: com.azure.storage.file.fileAsyncClient.instantiation.connectionstring
        return fileAsyncClient;
    }

    /**
     * Generates a code sample for using {@link FileAsyncClient#create(long)}
     */
    public void createFileAsync() {
        FileAsyncClient fileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileAsyncClient.create
        fileAsyncClient.create(1024).subscribe(
            response -> { },
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete creating the file!")
        );
        // END: com.azure.storage.file.fileAsyncClient.create
    }

    /**
     * Generates a code sample for using {@link FileAsyncClient#createWithResponse(long, FileHttpHeaders, FileSmbProperties, String, Map)}
     */
    public void createWithResponse() {
        FileAsyncClient fileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileAsyncClient.createWithResponse#long-filehttpheaders-filesmbproperties-string-map
        FileHttpHeaders httpHeaders = new FileHttpHeaders()
            .setFileContentType("text/html")
            .setFileContentEncoding("gzip")
            .setFileContentLanguage("en")
            .setFileCacheControl("no-transform")
            .setFileContentDisposition("attachment");
        FileSmbProperties smbProperties = new FileSmbProperties()
            .setNtfsFileAttributes(EnumSet.of(NtfsFileAttributes.READ_ONLY))
            .setFileCreationTime(OffsetDateTime.now())
            .setFileLastWriteTime(OffsetDateTime.now())
            .setFilePermissionKey("filePermissionKey");
        String filePermission = "filePermission";
        // NOTE: filePermission and filePermissionKey should never be both set
        fileAsyncClient.createWithResponse(1024, httpHeaders, smbProperties, filePermission,
            Collections.singletonMap("directory", "metadata"))
            .subscribe(response -> System.out.printf("Creating the file completed with status code %d",
                response.getStatusCode()));
        // END: com.azure.storage.file.fileAsyncClient.createWithResponse#long-filehttpheaders-filesmbproperties-string-map
    }

    /**
     * Generates a code sample for using {@link FileAsyncClient#beginCopy(String, Map, Duration)}
     */
    public void beginCopy() {
        FileAsyncClient fileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileAsyncClient.beginCopy#string-map-duration
        Poller<FileCopyInfo, Void> poller = fileAsyncClient.beginCopy(
            "https://{accountName}.file.core.windows.net?{SASToken}",
            Collections.singletonMap("file", "metadata"), Duration.ofSeconds(2));

        poller.getObserver().subscribe(response -> {
                final FileCopyInfo value = response.getValue();
                System.out.printf("Copy source: %s. Status: %s.%n", value.getCopySourceUrl(), value.getCopyStatus());
            }, error -> System.err.println("Error: " + error),
            () -> System.out.println("Complete copying the file."));
        // END: com.azure.storage.file.fileAsyncClient.beginCopy#string-map-duration
    }

    /**
     * Generates a code sample for using {@link FileAsyncClient#abortCopy(String)}
     */
    public void abortCopyFileAsync() {
        FileAsyncClient fileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileAsyncClient.abortCopy#string
        fileAsyncClient.abortCopy("someCopyId")
            .doOnSuccess(response -> System.out.println("Abort copying the file completed."));
        // END: com.azure.storage.file.fileAsyncClient.abortCopy#string
    }

    /**
     * Generates a code sample for using {@link FileAsyncClient#abortCopyWithResponse(String)}
     */
    public void abortCopyWithResponse() {
        FileAsyncClient fileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileAsyncClient.abortCopyWithResponse#string
        fileAsyncClient.abortCopyWithResponse("someCopyId")
            .subscribe(response -> System.out.printf("Abort copying the file completed with status code %d",
                response.getStatusCode()));
        // END: com.azure.storage.file.fileAsyncClient.abortCopyWithResponse#string
    }

    /**
     * Generates a code sample for using {@link FileAsyncClient#upload(Flux, long)}
     */
    public void uploadDataAsync() {
        FileAsyncClient fileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileAsyncClient.upload#flux-long
        ByteBuffer defaultData = ByteBuffer.wrap("default".getBytes(StandardCharsets.UTF_8));
        fileAsyncClient.upload(Flux.just(defaultData), defaultData.remaining()).subscribe(
            response -> { },
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete deleting the file!")
        );
        // END: com.azure.storage.file.fileAsyncClient.upload#flux-long
    }

    /**
     * Generates a code sample for using {@link FileAsyncClient#uploadWithResponse(Flux, long, Long)}
     */
    public void uploadDataMaxOverloadAsync() {
        FileAsyncClient fileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileAsyncClient.uploadWithResponse#flux-long-long
        ByteBuffer defaultData = ByteBuffer.wrap("default".getBytes(StandardCharsets.UTF_8));
        fileAsyncClient.upload(Flux.just(defaultData), defaultData.remaining()).subscribe(
            response -> { },
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete deleting the file!")
        );
        // END: com.azure.storage.file.fileAsyncClient.uploadWithResponse#flux-long-long
    }

    /**
     * Generates a code sample for using {@link FileAsyncClient#clearRange(long)}
     */
    public void clearRangeAsync() {
        FileAsyncClient fileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileAsyncClient.clearRange#long
        fileAsyncClient.clearRange(1024).subscribe(
            response -> { },
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete clearing the range!")
        );
        // END: com.azure.storage.file.fileAsyncClient.clearRange#long
    }

    /**
     * Generates a code sample for using {@link FileAsyncClient#clearRangeWithResponse(long, long)}
     */
    public void clearRangeAsyncMaxOverload() {
        FileAsyncClient fileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileAsyncClient.clearRange#long-long
        fileAsyncClient.clearRangeWithResponse(1024, 1024).subscribe(
            response -> { },
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete clearing the range!")
        );
        // END: com.azure.storage.file.fileAsyncClient.clearRange#long-long
    }

    /**
     * Generates a code sample for using {@link FileAsyncClient#uploadFromFile(String)}
     */
    public void uploadFileAsync() {
        FileAsyncClient fileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileAsyncClient.uploadFromFile#string
        fileAsyncClient.uploadFromFile("someFilePath").subscribe(
            response -> { },
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete deleting the file!")
        );
        // END: com.azure.storage.file.fileAsyncClient.uploadFromFile#string
    }

    /**
     * Generates a code sample for using {@link FileAsyncClient#uploadRangeFromUrl(long, long, long, URI)}
     * @throws URISyntaxException when the URI is invalid
     */
    public void uploadFileFromURLAsync() throws URISyntaxException {
        FileAsyncClient fileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileAsyncClient.uploadRangeFromUrl#long-long-long-uri
        fileAsyncClient.uploadRangeFromUrl(6, 8, 0, new URI("filewithSAStoken")).subscribe(
            response -> { },
            error -> System.err.print(error.toString()),
            () -> System.out.println("Completed upload range from url!")
        );
        // END: com.azure.storage.file.fileAsyncClient.uploadRangeFromUrl#long-long-long-uri
    }

    /**
     * Generates a code sample for using {@link FileAsyncClient#uploadRangeFromUrlWithResponse(long, long, long, URI)}
     * @throws URISyntaxException when the URI is invalid
     */
    public void uploadFileFromURLWithResponseAsync() throws URISyntaxException {
        FileAsyncClient fileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileAsyncClient.uploadRangeFromUrlWithResponse#long-long-long-uri
        fileAsyncClient.uploadRangeFromUrlWithResponse(6, 8, 0, new URI("filewithSAStoken")).subscribe(
            response -> { },
            error -> System.err.print(error.toString()),
            () -> System.out.println("Completed upload range from url!")
        );
        // END: com.azure.storage.file.fileAsyncClient.uploadRangeFromUrlWithResponse#long-long-long-uri
    }

    /**
     * Generates a code sample for using {@link FileAsyncClient#download()}
     */
    public void downloadDataAsync() {
        FileAsyncClient fileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileAsyncClient.download
        fileAsyncClient.download().subscribe(
            response -> { },
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete downloading the data!")
        );
        // END: com.azure.storage.file.fileAsyncClient.download
    }

    /**
     * Generates a code sample for using {@link FileAsyncClient#downloadWithResponse(FileRange, Boolean)}
     */
    public void downloadWithProperties() {
        FileAsyncClient fileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileAsyncClient.downloadWithResponse#filerange-boolean
        fileAsyncClient.downloadWithResponse(new FileRange(1024, 2047L), false)
            .subscribe(response ->
                    System.out.printf("Complete downloading the data with status code %d%n", response.getStatusCode()),
                error -> System.err.println(error.getMessage())
            );
        // END: com.azure.storage.file.fileAsyncClient.downloadWithResponse#filerange-boolean
    }

    /**
     * Generates a code sample for using {@link FileAsyncClient#downloadToFile(String)}
     */
    public void downloadFileAsync() {
        FileAsyncClient fileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileAsyncClient.downloadToFile#string
        fileAsyncClient.downloadToFile("somelocalfilepath").subscribe(
            response -> {
                if (Files.exists(Paths.get("somelocalfilepath"))) {
                    System.out.println("Successfully downloaded the file.");
                }
            },
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete downloading the file!")
        );
        // END: com.azure.storage.file.fileAsyncClient.downloadToFile#string
    }

    /**
     * Generates a code sample for using {@link FileAsyncClient#downloadToFileWithResponse(String, FileRange)}
     */
    public void downloadFileAsyncMaxOverload() {
        FileAsyncClient fileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileAsyncClient.downloadToFileWithResponse#string-filerange
        fileAsyncClient.downloadToFileWithResponse("somelocalfilepath", new FileRange(1024, 2047L))
            .subscribe(
                response -> {
                    if (Files.exists(Paths.get("somelocalfilepath"))) {
                        System.out.println("Successfully downloaded the file with status code "
                            + response.getStatusCode());
                    }
                },
                error -> System.err.print(error.toString()),
                () -> System.out.println("Complete downloading the file!")
            );
        // END: com.azure.storage.file.fileAsyncClient.downloadToFileWithResponse#string-filerange
    }

    /**
     * Generates a code sample for using {@link FileAsyncClient#delete()}
     */
    public void deleteFileAsync() {
        FileAsyncClient fileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileAsyncClient.delete
        fileAsyncClient.delete().subscribe(
            response -> { },
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete deleting the file!")
        );
        // END: com.azure.storage.file.fileAsyncClient.delete
    }

    /**
     * Generates a code sample for using {@link FileAsyncClient#deleteWithResponse()}
     */
    public void deleteWithResponse() {
        FileAsyncClient fileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileAsyncClient.deleteWithResponse
        fileAsyncClient.deleteWithResponse().subscribe(
            response -> System.out.println("Complete deleting the file with status code:" + response.getStatusCode()),
            error -> System.err.print(error.toString())
        );
        // END: com.azure.storage.file.fileAsyncClient.deleteWithResponse
    }

    /**
     * Generates a code sample for using {@link FileAsyncClient#getProperties()}
     */
    public void getPropertiesAsync() {
        FileAsyncClient fileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileAsyncClient.getProperties
        fileAsyncClient.getProperties()
            .subscribe(properties -> {
                System.out.printf("File latest modified date is %s.", properties.getLastModified());
            });
        // END: com.azure.storage.file.fileAsyncClient.getProperties
    }

    /**
     * Generates a code sample for using {@link FileAsyncClient#getPropertiesWithResponse()}
     */
    public void getPropertiesWithResponse() {
        FileAsyncClient fileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileAsyncClient.getPropertiesWithResponse
        fileAsyncClient.getPropertiesWithResponse()
            .subscribe(response -> {
                FileProperties properties = response.getValue();
                System.out.printf("File latest modified date is %s.", properties.getLastModified());
            });
        // END: com.azure.storage.file.fileAsyncClient.getPropertiesWithResponse
    }

    /**
     * Generates a code sample for using {@link FileAsyncClient#setMetadata(Map)}
     */
    public void setMetadataAsync() {
        FileAsyncClient fileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileAsyncClient.setMetadata#map
        fileAsyncClient.setMetadata(Collections.singletonMap("file", "updatedMetadata"))
            .doOnSuccess(response -> System.out.println("Setting the file metadata completed."));
        // END: com.azure.storage.file.fileAsyncClient.setMetadata#map
    }

    /**
     * Generates a code sample for using {@link FileAsyncClient#setMetadata(Map)}
     */
    public void setMetadataWithResponse() {
        FileAsyncClient fileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileAsyncClient.setMetadataWithResponse#map
        fileAsyncClient.setMetadataWithResponse(Collections.singletonMap("file", "updatedMetadata"))
            .subscribe(response -> System.out.printf("Setting the file metadata completed with status code %d",
                response.getStatusCode()));
        // END: com.azure.storage.file.fileAsyncClient.setMetadataWithResponse#map
    }

    /**
     * Generates a code sample for using {@link FileAsyncClient#setMetadataWithResponse(Map)} to clear metadata.
     */
    public void clearMetadataAsync() {
        FileAsyncClient fileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileAsyncClient.setMetadataWithResponse#map.clearMetadata
        fileAsyncClient.setMetadataWithResponse(null).subscribe(
            response -> System.out.printf("Setting the file metadata completed with status code %d",
                response.getStatusCode()));
        // END: com.azure.storage.file.fileAsyncClient.setMetadataWithResponse#map.clearMetadata
    }

    /**
     * Generates a code sample for using {@link FileAsyncClient#setMetadata(Map)} to clear metadata.
     */
    public void clearMetadata() {
        FileAsyncClient fileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileAsyncClient.setMetadata#map.clearMetadata
        fileAsyncClient.setMetadata(null).subscribe(
            response -> System.out.println("Setting the file metadata completed.")
        );
        // END: com.azure.storage.file.fileAsyncClient.setMetadata#map.clearMetadata
    }

    /**
     * Generates a code sample for using {@link FileAsyncClient#setProperties(long, FileHttpHeaders, FileSmbProperties, String)}
     */
    public void setFilePropertiesAsync() {
        FileAsyncClient fileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileAsyncClient.setProperties#long-filehttpheaders-filesmbproperties-string
        FileHttpHeaders httpHeaders = new FileHttpHeaders()
            .setFileContentType("text/html")
            .setFileContentEncoding("gzip")
            .setFileContentLanguage("en")
            .setFileCacheControl("no-transform")
            .setFileContentDisposition("attachment");
        FileSmbProperties smbProperties = new FileSmbProperties()
            .setNtfsFileAttributes(EnumSet.of(NtfsFileAttributes.READ_ONLY))
            .setFileCreationTime(OffsetDateTime.now())
            .setFileLastWriteTime(OffsetDateTime.now())
            .setFilePermissionKey("filePermissionKey");
        String filePermission = "filePermission";
        // NOTE: filePermission and filePermissionKey should never be both set
        fileAsyncClient.setProperties(1024, httpHeaders, smbProperties, filePermission)
            .doOnSuccess(response -> System.out.println("Setting the file properties completed."));
        // END: com.azure.storage.file.fileAsyncClient.setProperties#long-filehttpheaders-filesmbproperties-string
    }

    /**
     * Generates a code sample for using {@link FileAsyncClient#setPropertiesWithResponse(long, FileHttpHeaders, FileSmbProperties, String)}
     */
    public void setHttpHeadersWithResponse() {
        FileAsyncClient fileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileAsyncClient.setPropertiesWithResponse#long-filehttpheaders-filesmbproperties-string
        FileHttpHeaders httpHeaders = new FileHttpHeaders()
            .setFileContentType("text/html")
            .setFileContentEncoding("gzip")
            .setFileContentLanguage("en")
            .setFileCacheControl("no-transform")
            .setFileContentDisposition("attachment");
        FileSmbProperties smbProperties = new FileSmbProperties()
            .setNtfsFileAttributes(EnumSet.of(NtfsFileAttributes.READ_ONLY))
            .setFileCreationTime(OffsetDateTime.now())
            .setFileLastWriteTime(OffsetDateTime.now())
            .setFilePermissionKey("filePermissionKey");
        String filePermission = "filePermission";
        // NOTE: filePermission and filePermissionKey should never be both set
        fileAsyncClient.setPropertiesWithResponse(1024, httpHeaders, smbProperties, filePermission)
            .subscribe(response -> System.out.printf("Setting the file properties completed with status code %d",
                response.getStatusCode()));
        // END: com.azure.storage.file.fileAsyncClient.setPropertiesWithResponse#long-filehttpheaders-filesmbproperties-string
    }

    /**
     * Generates a code sample for using {@link FileAsyncClient#setPropertiesWithResponse(long, FileHttpHeaders, FileSmbProperties, String)}
     * to clear httpHeaders.
     */
    public void clearHTTPHeadersAsync() {
        FileAsyncClient fileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileAsyncClient.setPropertiesWithResponse#long-filehttpheaders-filesmbproperties-string.clearHttpHeaderspreserveSMBProperties
        fileAsyncClient.setPropertiesWithResponse(1024, null, null, null)
            .subscribe(response -> System.out.printf("Setting the file httpHeaders completed with status code %d",
                response.getStatusCode()));
        // END: com.azure.storage.file.fileAsyncClient.setPropertiesWithResponse#long-filehttpheaders-filesmbproperties-string.clearHttpHeaderspreserveSMBProperties
    }

    /**
     * Generates a code sample for using {@link FileAsyncClient#setProperties(long, FileHttpHeaders, FileSmbProperties, String)}
     * to clear httpHeaders.
     */
    public void clearHTTPHeaders() {
        FileAsyncClient fileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileAsyncClient.setProperties#long-filehttpheaders-filesmbproperties-string.clearHttpHeaderspreserveSMBProperties
        fileAsyncClient.setProperties(1024, null, null, null)
            .subscribe(response -> System.out.println("Setting the file httpHeaders completed."));
        // END: com.azure.storage.file.fileAsyncClient.setProperties#long-filehttpheaders-filesmbproperties-string.clearHttpHeaderspreserveSMBProperties
    }

    /**
     * Generates a code sample for using {@link FileAsyncClient#listRanges()}
     */
    public void listRangesAsync() {
        FileAsyncClient fileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileAsyncClient.listRanges
        fileAsyncClient.listRanges().subscribe(range ->
            System.out.printf("List ranges completed with start: %d, end: %d", range.getStart(), range.getEnd()));
        // END: com.azure.storage.file.fileAsyncClient.listRanges
    }

    /**
     * Generates a code sample for using {@link FileAsyncClient#listRanges(FileRange)}
     */
    public void listRangesAsyncMaxOverload() {
        FileAsyncClient fileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileAsyncClient.listRanges#filerange
        fileAsyncClient.listRanges(new FileRange(1024, 2048L))
            .subscribe(result -> System.out.printf("List ranges completed with start: %d, end: %d",
                result.getStart(), result.getEnd()));
        // END: com.azure.storage.file.fileAsyncClient.listRanges#filerange
    }

    /**
     * Generates a code sample for using {@link FileAsyncClient#listHandles()}
     */
    public void listHandlesAsync() {
        FileAsyncClient fileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileAsyncClient.listHandles
        fileAsyncClient.listHandles()
            .subscribe(result -> System.out.printf("List handles completed with handle id %s", result.getHandleId()));
        // END: com.azure.storage.file.fileAsyncClient.listHandles
    }

    /**
     * Generates a code sample for using {@link FileAsyncClient#listHandles(Integer)}
     */
    public void listHandlesAsyncMaxOverload() {
        FileAsyncClient fileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileAsyncClient.listHandles#integer
        fileAsyncClient.listHandles(10)
            .subscribe(result -> System.out.printf("List handles completed with handle id %s", result.getHandleId()));
        // END: com.azure.storage.file.fileAsyncClient.listHandles#integer
    }

    /**
     * Code snippet for {@link FileAsyncClient#forceCloseHandle(String)}.
     */
    public void forceCloseHandle() {
        FileAsyncClient fileAsyncClient = createAsyncClientWithConnectionString();
        // BEGIN: com.azure.storage.file.FileAsyncClient.forceCloseHandle#String
        fileAsyncClient.listHandles().subscribe(handleItem ->
            fileAsyncClient.forceCloseHandle(handleItem.getHandleId()).subscribe(ignored ->
                System.out.printf("Closed handle %s on resource %s%n",
                    handleItem.getHandleId(), handleItem.getPath())));
        // END: com.azure.storage.file.FileAsyncClient.forceCloseHandle#String
    }

    /**
     * Code snippet for {@link FileAsyncClient#forceCloseHandleWithResponse(String)}.
     */
    public void forceCloseHandleWithResponse() {
        FileAsyncClient fileAsyncClient = createAsyncClientWithConnectionString();
        // BEGIN: com.azure.storage.file.FileAsyncClient.forceCloseHandleWithResponse#String
        fileAsyncClient.listHandles().subscribe(handleItem ->
            fileAsyncClient.forceCloseHandleWithResponse(handleItem.getHandleId()).subscribe(response ->
                System.out.printf("Closing handle %s on resource %s completed with status code %d%n",
                    handleItem.getHandleId(), handleItem.getPath(), response.getStatusCode())));
        // END: com.azure.storage.file.FileAsyncClient.forceCloseHandleWithResponse#String
    }

    /**
     * Code snippet for {@link FileAsyncClient#forceCloseAllHandles()}.
     */
    public void forceCloseAllHandles() {
        FileAsyncClient fileAsyncClient = createAsyncClientWithConnectionString();
        // BEGIN: com.azure.storage.file.FileAsyncClient.forceCloseAllHandles
        fileAsyncClient.forceCloseAllHandles().subscribe(numberOfHandlesClosed ->
            System.out.printf("Closed %d open handles on the file%n", numberOfHandlesClosed));
        // END: com.azure.storage.file.FileAsyncClient.forceCloseAllHandles
    }

    /**
     * Generates a code sample for using {@link FileAsyncClient#getShareSnapshotId()}
     */
    public void getShareSnapshotIdAsync() {
        // BEGIN: com.azure.storage.file.fileAsyncClient.getShareSnapshotId
        OffsetDateTime currentTime = OffsetDateTime.of(LocalDateTime.now(), ZoneOffset.UTC);
        FileAsyncClient fileAsyncClient = new FileClientBuilder()
            .endpoint("https://${accountName}.file.core.windows.net")
            .sasToken("${SASToken}")
            .shareName("myshare")
            .resourcePath("myfiile")
            .snapshot(currentTime.toString())
            .buildFileAsyncClient();

        System.out.printf("Snapshot ID: %s%n", fileAsyncClient.getShareSnapshotId());
        // END: com.azure.storage.file.fileAsyncClient.getShareSnapshotId
    }

    /**
     * Generates a code sample for using {@link FileAsyncClient#getShareName()}
     */
    public void getShareNameAsync() {
        FileAsyncClient directoryAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileAsyncClient.getShareName
        String shareName = directoryAsyncClient.getShareName();
        System.out.println("The share name of the directory is " + shareName);
        // END: com.azure.storage.file.fileAsyncClient.getShareName
    }

    /**
     * Generates a code sample for using {@link FileAsyncClient#getFilePath()}
     */
    public void getFilePathAsync() {
        FileAsyncClient fileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileAsyncClient.getFilePath
        String filePath = fileAsyncClient.getFilePath();
        System.out.println("The name of the file is " + filePath);
        // END: com.azure.storage.file.fileAsyncClient.getFilePath
    }
}
