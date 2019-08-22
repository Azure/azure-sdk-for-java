// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file;

import com.azure.storage.common.Utility;
import com.azure.storage.common.credentials.SASTokenCredential;
import com.azure.storage.common.credentials.SharedKeyCredential;
import com.azure.storage.file.models.FileHTTPHeaders;
import com.azure.storage.file.models.FileProperties;
import com.azure.storage.file.models.FileRange;
import com.azure.storage.file.models.FileRangeWriteType;
import reactor.core.publisher.Flux;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
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
            .buildAsyncClient();
        // END: com.azure.storage.file.fileAsyncClient.instantiation
    }


    /**
     * Generates code sample for creating a {@link FileAsyncClient} with {@link SASTokenCredential}
     * @return An instance of {@link FileAsyncClient}
     */
    public FileAsyncClient createAsyncClientWithSASToken() {
        // BEGIN: com.azure.storage.file.fileAsyncClient.instantiation.sastoken
        FileAsyncClient fileAsyncClient = new FileClientBuilder()
            .endpoint("https://{accountName}.file.core.windows.net?{SASToken}")
            .shareName("myshare")
            .filePath("myfilepath")
            .buildAsyncClient();
        // END: com.azure.storage.file.fileAsyncClient.instantiation.sastoken
        return fileAsyncClient;
    }

    /**
     * Generates code sample for creating a {@link FileAsyncClient} with {@link SASTokenCredential}
     * @return An instance of {@link FileAsyncClient}
     */
    public FileAsyncClient createAsyncClientWithCredential() {
        // BEGIN: com.azure.storage.file.fileAsyncClient.instantiation.credential
        FileAsyncClient fileAsyncClient = new FileClientBuilder()
            .endpoint("https://{accountName}.file.core.windows.net")
            .credential(SASTokenCredential.fromQueryParameters(Utility.parseQueryString("${SASTokenQueryParams}")))
            .shareName("myshare")
            .filePath("myfilepath")
            .buildAsyncClient();
        // END: com.azure.storage.file.fileAsyncClient.instantiation.credential
        return fileAsyncClient;
    }

    /**
     * Generates code sample for creating a {@link FileAsyncClient} with {@code connectionString} which turns into {@link SharedKeyCredential}
     * @return An instance of {@link FileAsyncClient}
     */
    public FileAsyncClient createAsyncClientWithConnectionString() {
        // BEGIN: com.azure.storage.file.fileAsyncClient.instantiation.connectionstring
        String connectionString = "DefaultEndpointsProtocol=https;AccountName={name};AccountKey={key};"
            + "EndpointSuffix={core.windows.net}";
        FileAsyncClient fileAsyncClient = new FileClientBuilder()
            .connectionString(connectionString).shareName("myshare").filePath("myfilepath")
            .buildAsyncClient();
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
     * Generates a code sample for using {@link FileAsyncClient#create(long)}
     */
    public void createFileAsyncMaxOverload() {
        FileAsyncClient fileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileAsyncClient.create#long-filehttpheaders-map
        FileHTTPHeaders httpHeaders = new FileHTTPHeaders().fileContentType("text/plain");
        fileAsyncClient.create(1024)
            .doOnSuccess(response -> System.out.printf("Creating the file completed."));
        // END: com.azure.storage.file.fileAsyncClient.create#long-filehttpheaders-map
    }

    /**
     * Generates a code sample for using {@link FileAsyncClient#createWithResponse(long, FileHTTPHeaders, Map)}
     */
    public void createWithResponse() {
        FileAsyncClient fileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileAsyncClient.createWithResponse#long-filehttpheaders-map
        FileHTTPHeaders httpHeaders = new FileHTTPHeaders().fileContentType("text/plain");
        fileAsyncClient.createWithResponse(1024, httpHeaders, Collections.singletonMap("file", "updatedMetadata"))
            .subscribe(response -> System.out.printf("Creating the file completed with status code %d",
                response.statusCode()));
        // END: com.azure.storage.file.fileAsyncClient.createWithResponse#long-filehttpheaders-map
    }

    /**
     * Generates a code sample for using {@link FileAsyncClient#startCopy(String, Map)}
     */
    public void copyFileAsync() {
        FileAsyncClient fileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileAsyncClient.startCopy#string-map
        fileAsyncClient.startCopy("https://{accountName}.file.core.windows.net?{SASToken}",
            Collections.singletonMap("file", "metadata")).subscribe(
                response -> System.out.println("Successfully copied the file;"),
                error -> System.err.println(error.toString()),
                () -> System.out.println("Complete copying the file.")
        );
        // END: com.azure.storage.file.fileAsyncClient.startCopy#string-map
    }

    /**
     * Generates a code sample for using {@link FileAsyncClient#startCopyWithResponse(String, Map)}
     */
    public void startCopyWithResponse() {
        FileAsyncClient fileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileAsyncClient.startCopyWithResponse#string-map
        fileAsyncClient.startCopyWithResponse("https://{accountName}.file.core.windows.net?{SASToken}",
            Collections.singletonMap("file", "metadata")).subscribe(
                response ->
                    System.out.println("Successfully copying the file with status code: " + response.statusCode()),
                error -> System.err.println(error.toString())
        );
        // END: com.azure.storage.file.fileAsyncClient.startCopyWithResponse#string-map
    }

    /**
     * Generates a code sample for using {@link FileAsyncClient#abortCopy(String)}
     */
    public void abortCopyFileAsync() {
        FileAsyncClient fileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileAsyncClient.abortCopy#string
        fileAsyncClient.abortCopy("someCopyId")
            .doOnSuccess(response -> System.out.printf("Abort copying the file completed."));
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
                response.statusCode()));
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
     * Generates a code sample for using {@link FileAsyncClient#uploadWithResponse(Flux, long)}
     */
    public void uploadWithResponse() {
        FileAsyncClient fileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileAsyncClient.uploadWithResponse#flux-long
        ByteBuffer defaultData = ByteBuffer.wrap("default".getBytes(StandardCharsets.UTF_8));
        fileAsyncClient.uploadWithResponse(Flux.just(defaultData), defaultData.remaining()).subscribe(
            response -> System.out.println("Complete deleting the file with status code:" + response.statusCode()),
            error -> System.err.print(error.toString())
        );
        // END: com.azure.storage.file.fileAsyncClient.uploadWithResponse#flux-long
    }


    /**
     * Generates a code sample for using {@link FileAsyncClient#uploadWithResponse(Flux, long, long, FileRangeWriteType)}
     */
    public void uploadWithResponseOverload() {
        FileAsyncClient fileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileAsyncClient.uploadWithResponse#ByteBuffer-long-int-filerangewritetype
        ByteBuffer defaultData = ByteBuffer.wrap("default".getBytes(StandardCharsets.UTF_8));
        fileAsyncClient.uploadWithResponse(Flux.just(defaultData), defaultData.remaining(), 1024,
            FileRangeWriteType.UPDATE).subscribe(
                response -> System.out.println("Complete deleting the file with status code" + response.statusCode()),
                error -> System.err.print(error.toString())
        );
        // END: com.azure.storage.file.fileAsyncClient.uploadWithResponse#ByteBuffer-long-int-filerangewritetype
    }

    /**
     * Generates a code sample for using {@link FileAsyncClient#upload(Flux, long)}
     */
    public void uploadDataAsyncMaxOverload() {
        FileAsyncClient fileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileAsyncClient.upload#ByteBuffer-long-int-filerangewritetype
        ByteBuffer defaultData = ByteBuffer.wrap("default".getBytes(StandardCharsets.UTF_8));
        fileAsyncClient.upload(Flux.just(defaultData), defaultData.remaining(), 1024,
            FileRangeWriteType.UPDATE).subscribe(
                response -> { },
                error -> System.err.print(error.toString()),
                () -> System.out.println("Complete deleting the file!")
        );
        // END: com.azure.storage.file.fileAsyncClient.upload#ByteBuffer-long-int-filerangewritetype
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
     * Generates a code sample for using {@link FileAsyncClient#uploadFromFile(String, FileRangeWriteType)}
     */
    public void uploadFileAsyncMaxOverload() {
        FileAsyncClient fileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileAsyncClient.uploadFromFile#string-filerangewritetype
        fileAsyncClient.uploadFromFile("someFilePath", FileRangeWriteType.UPDATE)
            .subscribe(response -> {
                if (fileAsyncClient.getProperties() != null) {
                    System.out.printf("Upload the file with length of %d completed",
                        fileAsyncClient.getPropertiesWithResponse().block().value().contentLength());
                }
            });
        // END: com.azure.storage.file.fileAsyncClient.uploadFromFile#string-filerangewritetype
    }

    /**
     * Generates a code sample for using {@link FileAsyncClient#downloadWithProperties()}
     */
    public void downloadDataAsync() {
        FileAsyncClient fileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileAsyncClient.downloadWithProperties
        fileAsyncClient.downloadWithProperties().subscribe(
            response -> { },
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete downloading the data!")
        );
        // END: com.azure.storage.file.fileAsyncClient.downloadWithProperties
    }

    /**
     * Generates a code sample for using {@link FileAsyncClient#downloadWithPropertiesWithResponse(FileRange, Boolean)}
     */
    public void downloadDataAsyncMaxOverload() {
        FileAsyncClient fileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileAsyncClient.downloadWithProperties#filerange-boolean
        fileAsyncClient.downloadWithPropertiesWithResponse(new FileRange(1024, 2047L), false).
            subscribe(
                response -> { },
                error -> System.err.print(error.toString()),
                () -> System.out.println("Complete downloading the data!")
            );
        // END: com.azure.storage.file.fileAsyncClient.downloadWithProperties#filerange-boolean
    }

    /**
     * Generates a code sample for using {@link FileAsyncClient#downloadWithPropertiesWithResponse(FileRange, Boolean)}
     */
    public void downloadWithPropertiesWithResponse() {
        FileAsyncClient fileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileAsyncClient.downloadWithPropertiesWithResponse#filerange-boolean
        fileAsyncClient.downloadWithPropertiesWithResponse(new FileRange(1024, 2047L), false).subscribe(
            response -> System.out.println("Complete downloading the data with status code: " + response.statusCode()),
            error -> System.err.print(error.toString())
        );
        // END: com.azure.storage.file.fileAsyncClient.downloadWithPropertiesWithResponse#filerange-boolean
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
     * Generates a code sample for using {@link FileAsyncClient#downloadToFile(String, FileRange)}
     */
    public void downloadFileAsyncMaxOverload() {
        FileAsyncClient fileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileAsyncClient.downloadToFile#string-filerange
        fileAsyncClient.downloadToFile("somelocalfilepath", new FileRange(1024, 2047L)).subscribe(
            response -> {
                if (Files.exists(Paths.get("somelocalfilepath"))) {
                    System.out.println("Successfully downloaded the file.");
                }
            },
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete downloading the file!")
        );
        // END: com.azure.storage.file.fileAsyncClient.downloadToFile#string-filerange
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
            response -> System.out.println("Complete deleting the file with status code:" + response.statusCode()),
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
                System.out.printf("File latest modified date is %s.", properties.lastModified());
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
                FileProperties properties = response.value();
                System.out.printf("File latest modified date is %s.", properties.lastModified());
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
            .doOnSuccess(response -> System.out.printf("Setting the file metadata completed."));
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
                response.statusCode()));
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
                response.statusCode()));
        // END: com.azure.storage.file.fileAsyncClient.setMetadataWithResponse#map.clearMetadata
    }

    /**
     * Generates a code sample for using {@link FileAsyncClient#setMetadata(Map)} to clear metadata.
     */
    public void clearMetadata() {
        FileAsyncClient fileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileAsyncClient.setMetadata#map.clearMetadata
        fileAsyncClient.setMetadata(null).subscribe(
            response -> System.out.printf("Setting the file metadata completed.")
        );
        // END: com.azure.storage.file.fileAsyncClient.setMetadata#map.clearMetadata
    }

    /**
     * Generates a code sample for using {@link FileAsyncClient#setHttpHeaders(long, FileHTTPHeaders)}
     */
    public void setHTTPHeadersAsync() {
        FileAsyncClient fileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileAsyncClient.setHttpHeaders#long-filehttpheaders
        FileHTTPHeaders httpHeaders = new FileHTTPHeaders().fileContentType("text/plain");
        fileAsyncClient.setHttpHeaders(1024, httpHeaders)
            .doOnSuccess(response -> System.out.printf("Setting the file httpHeaders completed."));
        // END: com.azure.storage.file.fileAsyncClient.setHttpHeaders#long-filehttpheaders
    }

    /**
     * Generates a code sample for using {@link FileAsyncClient#setHttpHeadersWithResponse(long, FileHTTPHeaders)}
     */
    public void setHttpHeadersWithResponse() {
        FileAsyncClient fileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileAsyncClient.setHttpHeadersWithResponse#long-filehttpheaders
        FileHTTPHeaders httpHeaders = new FileHTTPHeaders().fileContentType("text/plain");
        fileAsyncClient.setHttpHeadersWithResponse(1024, httpHeaders)
            .subscribe(response -> System.out.printf("Setting the file httpHeaders completed with status code %d",
                response.statusCode()));
        // END: com.azure.storage.file.fileAsyncClient.setHttpHeadersWithResponse#long-filehttpheaders
    }

    /**
     * Generates a code sample for using {@link FileAsyncClient#setHttpHeadersWithResponse(long, FileHTTPHeaders)} to clear httpHeaders.
     */
    public void clearHTTPHeadersAsync() {
        FileAsyncClient fileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileAsyncClient.setHttpHeadersWithResponse#long-filehttpheaders.clearHttpHeaders
        fileAsyncClient.setHttpHeadersWithResponse(1024, null)
            .subscribe(response -> System.out.printf("Setting the file httpHeaders completed with status code %d",
                response.statusCode()));
        // END: com.azure.storage.file.fileAsyncClient.setHttpHeadersWithResponse#long-filehttpheaders.clearHttpHeaders
    }

    /**
     * Generates a code sample for using {@link FileAsyncClient#setHttpHeaders(long, FileHTTPHeaders)} to clear httpHeaders.
     */
    public void clearHTTPHeaders() {
        FileAsyncClient fileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileAsyncClient.setHttpHeaders#long-filehttpheaders.clearHttpHeaders
        fileAsyncClient.setHttpHeaders(1024, null)
            .subscribe(response -> System.out.printf("Setting the file httpHeaders completed."));
        // END: com.azure.storage.file.fileAsyncClient.setHttpHeaders#long-filehttpheaders.clearHttpHeaders
    }

    /**
     * Generates a code sample for using {@link FileAsyncClient#listRanges()}
     */
    public void listRangesAsync() {
        FileAsyncClient fileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileAsyncClient.listRanges
        fileAsyncClient.listRanges().subscribe(range ->
            System.out.printf("List ranges completed with start: %d, end: %d", range.start(), range.end()));
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
                result.start(), result.end()));
        // END: com.azure.storage.file.fileAsyncClient.listRanges#filerange
    }

    /**
     * Generates a code sample for using {@link FileAsyncClient#listHandles()}
     */
    public void listHandlesAsync() {
        FileAsyncClient fileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileAsyncClient.listHandles
        fileAsyncClient.listHandles()
            .subscribe(result -> System.out.printf("List handles completed with handle id %s", result.handleId()));
        // END: com.azure.storage.file.fileAsyncClient.listHandles
    }

    /**
     * Generates a code sample for using {@link FileAsyncClient#listHandles(Integer)}
     */
    public void listHandlesAsyncMaxOverload() {
        FileAsyncClient fileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileAsyncClient.listHandles#integer
        fileAsyncClient.listHandles(10)
            .subscribe(result -> System.out.printf("List handles completed with handle id %s", result.handleId()));
        // END: com.azure.storage.file.fileAsyncClient.listHandles#integer
    }

    /**
     * Generates a code sample for using {@link FileAsyncClient#forceCloseHandles(String)}
     */
    public void forceCloseHandlesAsync() {
        FileAsyncClient fileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileAsyncClient.forceCloseHandles#string
        fileAsyncClient.listHandles(10)
            .subscribe(result -> {
                fileAsyncClient.forceCloseHandles(result.handleId()).subscribe(numOfClosedHandles ->
                    System.out.printf("Close %d handles.", numOfClosedHandles));
            });
        // END: com.azure.storage.file.fileAsyncClient.forceCloseHandles#string
    }

    /**
     * Generates a code sample for using {@link FileAsyncClient#getShareSnapshotId()}
     */
    public void getShareSnapshotIdAsync() {
        // BEGIN: com.azure.storage.file.fileAsyncClient.getShareSnapshotId
        OffsetDateTime currentTime = OffsetDateTime.of(LocalDateTime.now(), ZoneOffset.UTC);
        FileAsyncClient fileAsyncClient = new FileClientBuilder()
            .endpoint("https://${accountName}.file.core.windows.net")
            .credential(SASTokenCredential.fromSASTokenString("${SASToken}"))
            .shareName("myshare")
            .filePath("myfiile")
            .snapshot(currentTime.toString())
            .buildAsyncClient();
        fileAsyncClient.getShareSnapshotId();
        // END: com.azure.storage.file.fileAsyncClient.getShareSnapshotId
    }
}
