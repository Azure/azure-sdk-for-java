// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file;

import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.VoidResponse;
import com.azure.storage.common.credentials.SASTokenCredential;
import com.azure.storage.common.credentials.SharedKeyCredential;
import com.azure.storage.file.models.FileCopyInfo;
import com.azure.storage.file.models.FileDownloadInfo;
import com.azure.storage.file.models.FileHTTPHeaders;
import com.azure.storage.file.models.FileInfo;
import com.azure.storage.file.models.FileMetadataInfo;
import com.azure.storage.file.models.FileProperties;
import com.azure.storage.file.models.FileRange;
import com.azure.storage.file.models.FileRangeWriteType;
import com.azure.storage.file.models.FileUploadInfo;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Map;
import reactor.core.publisher.Flux;

/**
 * Contains code snippets when generating javadocs through doclets for {@link FileClient} and {@link FileAsyncClient}.
 */
public class FileJavaDocCodeSamples {
    /**
     * Generates code sample for {@link FileClient} instantiation.
     */
    public void initialization() {
        // BEGIN: com.azure.storage.file.fileClient.instantiation
        FileClient client = new FileClientBuilder()
            .connectionString("${connectionString}")
            .endpoint("${endpoint}")
            .buildClient();
        // END: com.azure.storage.file.fileClient.instantiation
    }

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
     * Generates code sample for creating a {@link FileClient} with {@link SASTokenCredential}
     * @return An instance of {@link FileClient}
     */
    public FileClient createClientWithSASToken() {

        // BEGIN: com.azure.storage.file.fileClient.instantiation.sastoken
        FileClient fileClient = new FileClientBuilder()
            .endpoint("https://${accountName}.file.core.windows.net?${SASToken}")
            .shareName("myshare")
            .filePath("myfilepath")
            .buildClient();
        // END: com.azure.storage.file.fileClient.instantiation.sastoken
        return fileClient;
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
     * Generates code sample for creating a {@link FileClient} with {@link SASTokenCredential}
     * @return An instance of {@link FileClient}
     */
    public FileClient createClientWithCredential() {

        // BEGIN: com.azure.storage.file.fileClient.instantiation.credential
        FileClient fileClient = new FileClientBuilder()
            .endpoint("https://${accountName}.file.core.windows.net")
            .credential(SASTokenCredential.fromQuery("${SASTokenQueryParams}"))
            .shareName("myshare")
            .filePath("myfilepath")
            .buildClient();
        // END: com.azure.storage.file.fileClient.instantiation.credential
        return fileClient;
    }

    /**
     * Generates code sample for creating a {@link FileAsyncClient} with {@link SASTokenCredential}
     * @return An instance of {@link FileAsyncClient}
     */
    public FileAsyncClient createAsyncClientWithCredential() {
        // BEGIN: com.azure.storage.file.fileAsyncClient.instantiation.credential
        FileAsyncClient fileAsyncClient = new FileClientBuilder()
            .endpoint("https://{accountName}.file.core.windows.net")
            .credential(SASTokenCredential.fromQuery("${SASTokenQueryParams}"))
            .shareName("myshare")
            .filePath("myfilepath")
            .buildAsyncClient();
        // END: com.azure.storage.file.fileAsyncClient.instantiation.credential
        return fileAsyncClient;
    }

    /**
     * Generates code sample for creating a {@link FileClient} with {@code connectionString} which turns into {@link SharedKeyCredential}
     * @return An instance of {@link FileClient}
     */
    public FileClient createClientWithConnectionString() {
        // BEGIN: com.azure.storage.file.fileClient.instantiation.connectionstring
        String connectionString = "DefaultEndpointsProtocol=https;AccountName={name};AccountKey={key};"
            + "EndpointSuffix={core.windows.net}";
        FileClient fileClient = new FileClientBuilder()
            .connectionString(connectionString).shareName("myshare").filePath("myfilepath")
            .buildClient();
        // END: com.azure.storage.file.fileClient.instantiation.connectionstring
        return fileClient;
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
     * Generates a code sample for using {@link FileClient#create(long)}
     */
    public void createFile() {
        FileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileClient.create
        Response<FileInfo> response = fileClient.create(1024);
        System.out.println("Complete creating the file with status code: " + response.statusCode());
        // END: com.azure.storage.file.fileClient.create
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
     * Generates a code sample for using {@link FileClient#create(long, FileHTTPHeaders, Map)}
     */
    public void createFileMaxOverload() {
        FileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileClient.create#long-filehttpheaders-map
        FileHTTPHeaders httpHeaders = new FileHTTPHeaders().fileContentType("text/plain");
        Response<FileInfo> response = fileClient.create(1024, httpHeaders,
            Collections.singletonMap("file", "updatedMetadata"));
        System.out.printf("Creating the file completed with status code %d", response.statusCode());
        // END: com.azure.storage.file.fileClient.create#long-filehttpheaders-map
    }

    /**
     * Generates a code sample for using {@link FileAsyncClient#create(long, FileHTTPHeaders, Map)}
     */
    public void createFileAsyncMaxOverload() {
        FileAsyncClient fileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileAsyncClient.create#long-filehttpheaders-map
        FileHTTPHeaders httpHeaders = new FileHTTPHeaders().fileContentType("text/plain");
        fileAsyncClient.create(1024, httpHeaders, Collections.singletonMap("file", "updatedMetadata"))
            .subscribe(response -> System.out.printf("Creating the file completed with status code %d",
                response.statusCode()));
        // END: com.azure.storage.file.fileAsyncClient.create#long-filehttpheaders-map
    }

    /**
     * Generates a code sample for using {@link FileClient#startCopy(String, Map)}
     */
    public void copyFile() {
        FileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileClient.startCopy#string-map
        Response<FileCopyInfo> response = fileClient.startCopy(
            "https://{accountName}.file.core.windows.net?{SASToken}",
            Collections.singletonMap("file", "metadata"));
        System.out.println("Complete copying the file with status code: " + response.statusCode());
        // END: com.azure.storage.file.fileClient.startCopy#string-map
    }

    /**
     * Generates a code sample for using {@link FileClient#startCopy(String, Map)}
     */
    public void copyFileAsync() {
        FileAsyncClient fileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileAsyncClient.startCopy#string-map
        fileAsyncClient.startCopy("https://{accountName}.file.core.windows.net?{SASToken}",
            Collections.singletonMap("file", "metadata")).subscribe(
                response -> System.out.println("Successfully copying the file with status code: " + response.statusCode()),
                error -> System.err.println(error.toString()),
                () -> System.out.println("Complete copying the file.")
        );
        // END: com.azure.storage.file.fileAsyncClient.startCopy#string-map
    }

    /**
     * Generates a code sample for using {@link FileClient#abortCopy(String)}
     */
    public void abortCopyFile() {
        FileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileClient.abortCopy#string
        VoidResponse response = fileClient.abortCopy("someCopyId");
        System.out.printf("Abort copying the file completed with status code %d", response.statusCode());
        // END: com.azure.storage.file.fileClient.abortCopy#string
    }

    /**
     * Generates a code sample for using {@link FileClient#abortCopy(String)}
     */
    public void abortCopyFileAsync() {
        FileAsyncClient fileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileAsyncClient.abortCopy#string
        fileAsyncClient.abortCopy("someCopyId")
            .subscribe(response -> System.out.printf("Abort copying the file completed with status code %d",
                response.statusCode()));
        // END: com.azure.storage.file.fileAsyncClient.abortCopy#string
    }

    /**
     * Generates a code sample for using {@link FileClient#upload(ByteBuf, long)}
     */
    public void uploadData() {
        FileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileClient.upload#flux-long
        ByteBuf defaultData = Unpooled.wrappedBuffer("default".getBytes(StandardCharsets.UTF_8));
        Response<FileUploadInfo> response = fileClient.upload(defaultData, defaultData.readableBytes());
        System.out.println("Complete uploading the data with status code: " + response.statusCode());
        // END: com.azure.storage.file.fileClient.upload#flux-long
    }

    /**
     * Generates a code sample for using {@link FileAsyncClient#upload(Flux, long)}
     */
    public void uploadDataAsync() {
        FileAsyncClient fileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileAsyncClient.upload#flux-long
        ByteBuf defaultData = Unpooled.wrappedBuffer("default".getBytes(StandardCharsets.UTF_8));
        fileAsyncClient.upload(Flux.just(defaultData), defaultData.readableBytes()).subscribe(
            response -> { },
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete deleting the file!")
        );
        // END: com.azure.storage.file.fileAsyncClient.upload#flux-long
    }

    /**
     * Generates a code sample for using {@link FileClient#upload(ByteBuf, long, int, FileRangeWriteType)}
     */
    public void uploadDataMaxOverload() {
        FileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileClient.upload#bytebuf-long-int-filerangewritetype
        ByteBuf defaultData = Unpooled.wrappedBuffer("default".getBytes(StandardCharsets.UTF_8));
        Response<FileUploadInfo> response = fileClient.upload(defaultData, defaultData.readableBytes(), 1024,
            FileRangeWriteType.UPDATE);
        System.out.println("Complete uploading the data with status code: " + response.statusCode());
        // END: com.azure.storage.file.fileClient.upload#bytebuf-long-int-filerangewritetype
    }

    /**
     * Generates a code sample for using {@link FileAsyncClient#upload(Flux, long)}
     */
    public void uploadDataAsyncMaxOverload() {
        FileAsyncClient fileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileAsyncClient.upload#bytebuf-long-int-filerangewritetype
        ByteBuf defaultData = Unpooled.wrappedBuffer("default".getBytes(StandardCharsets.UTF_8));
        fileAsyncClient.upload(Flux.just(defaultData), defaultData.readableBytes(), 1024,
            FileRangeWriteType.UPDATE).subscribe(
                response -> { },
                error -> System.err.print(error.toString()),
                () -> System.out.println("Complete deleting the file!")
        );
        // END: com.azure.storage.file.fileAsyncClient.upload#bytebuf-long-int-filerangewritetype
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
     * Generates a code sample for using {@link FileClient#uploadFromFile(String, FileRangeWriteType)}
     */
    public void uploadFileMaxOverload() {
        FileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileClient.uploadFromFile#string-filerangewritetype
        fileClient.uploadFromFile("someFilePath", FileRangeWriteType.UPDATE);
        if (fileClient.getProperties() != null) {
            System.out.printf("Upload the file with length of %d completed",
                fileClient.getProperties().value().contentLength());
        }
        // END: com.azure.storage.file.fileClient.uploadFromFile#string-filerangewritetype
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
                        fileAsyncClient.getProperties().block().value().contentLength());
                }
            });
        // END: com.azure.storage.file.fileAsyncClient.uploadFromFile#string-filerangewritetype
    }

    /**
     * Generates a code sample for using {@link FileClient#downloadWithProperties()}
     */
    public void downloadData() {
        FileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileClient.downloadWithProperties
        Response<FileDownloadInfo> response = fileClient.downloadWithProperties();
        System.out.println("Complete downloading the data with status code: " + response.statusCode());
        response.value().body().subscribe(
            byteBuf ->  System.out.println("Complete downloading the data with body: "
                + byteBuf.toString(StandardCharsets.UTF_8)),
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete downloading the data!")
        );
        // END: com.azure.storage.file.fileClient.downloadWithProperties
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
     * Generates a code sample for using {@link FileClient#downloadWithProperties(FileRange, Boolean)}
     */
    public void downloadDataMaxOverload() {
        FileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileClient.downloadWithProperties#filerange-boolean
        Response<FileDownloadInfo> response = fileClient.downloadWithProperties(new FileRange(1024, 2047L),
            false);
        System.out.println("Complete downloading the data with status code: " + response.statusCode());
        response.value().body().subscribe(
            byteBuf ->  System.out.println("Complete downloading the data with body: "
                + byteBuf.toString(StandardCharsets.UTF_8)),
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete downloading the data!")
        );
        // END: com.azure.storage.file.fileClient.downloadWithProperties#filerange-boolean
    }

    /**
     * Generates a code sample for using {@link FileAsyncClient#downloadWithProperties(FileRange, Boolean)}
     */
    public void downloadDataAsyncMaxOverload() {
        FileAsyncClient fileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileAsyncClient.downloadWithProperties#filerange-boolean
        fileAsyncClient.downloadWithProperties(new FileRange(1024, 2047L), false).subscribe(
            response -> { },
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete downloading the data!")
        );
        // END: com.azure.storage.file.fileAsyncClient.downloadWithProperties#filerange-boolean
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
     * Generates a code sample for using {@link FileClient#delete()}
     */
    public void deleteFile() {
        FileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileClient.delete
        VoidResponse response = fileClient.delete();
        System.out.println("Complete deleting the file with status code: " + response.statusCode());
        // END: com.azure.storage.file.fileClient.delete
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
     * Generates a code sample for using {@link FileClient#getProperties()}
     */
    public void getProperties() {
        FileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileClient.getProperties
        Response<FileProperties> response = fileClient.getProperties();
        FileProperties properties = response.value();
        System.out.printf("File latest modified date is %s.", properties.lastModified());
        // END: com.azure.storage.file.fileClient.getProperties
    }

    /**
     * Generates a code sample for using {@link FileAsyncClient#getProperties()}
     */
    public void getPropertiesAsync() {
        FileAsyncClient fileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileAsyncClient.getProperties
        fileAsyncClient.getProperties()
            .subscribe(response -> {
                FileProperties properties = response.value();
                System.out.printf("File latest modified date is %s.", properties.lastModified());
            });
        // END: com.azure.storage.file.fileAsyncClient.getProperties
    }

    /**
     * Generates a code sample for using {@link FileClient#setMetadata(Map)}
     */
    public void setMetadata() {
        FileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileClient.setMetadata#map
        Response<FileMetadataInfo> response = fileClient.setMetadata(
            Collections.singletonMap("file", "updatedMetadata"));
        System.out.printf("Setting the file metadata completed with status code %d", response.statusCode());
        // END: com.azure.storage.file.fileClient.setMetadata#map
    }

    /**
     * Generates a code sample for using {@link FileAsyncClient#setMetadata(Map)}
     */
    public void setMetadataAsync() {
        FileAsyncClient fileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileAsyncClient.setMetadata#map
        fileAsyncClient.setMetadata(Collections.singletonMap("file", "updatedMetadata"))
            .subscribe(response -> System.out.printf("Setting the file metadata completed with status code %d",
                response.statusCode()));
        // END: com.azure.storage.file.fileAsyncClient.setMetadata#map
    }

    /**
     * Generates a code sample for using {@link FileClient#setMetadata(Map)} to clear metadata.
     */
    public void clearMetadata() {
        FileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileClient.setMetadata#map.clearMetadata
        Response<FileMetadataInfo> response = fileClient.setMetadata(null);
        System.out.printf("Setting the file metadata completed with status code %d", response.statusCode());
        // END: com.azure.storage.file.fileClient.setMetadata#map.clearMetadata
    }

    /**
     * Generates a code sample for using {@link FileAsyncClient#setMetadata(Map)} to clear metadata.
     */
    public void clearMetadataAsync() {
        FileAsyncClient fileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileAsyncClient.setMetadata#map.clearMetadata
        fileAsyncClient.setMetadata(null)
            .subscribe(response -> System.out.printf("Setting the file metadata completed with status code %d",
                response.statusCode()));
        // END: com.azure.storage.file.fileAsyncClient.setMetadata#map.clearMetadata
    }

    /**
     * Generates a code sample for using {@link FileClient#setHttpHeaders(long, FileHTTPHeaders)}
     */
    public void setHTTPHeaders() {
        FileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileClient.setHttpHeaders#long-filehttpheaders
        FileHTTPHeaders httpHeaders = new FileHTTPHeaders().fileContentType("text/plain");
        Response<FileInfo> response = fileClient.setHttpHeaders(1024, httpHeaders);
        System.out.printf("Setting the file httpHeaders completed with status code %d", response.statusCode());
        // END: com.azure.storage.file.fileClient.setHttpHeaders#long-filehttpheaders
    }

    /**
     * Generates a code sample for using {@link FileAsyncClient#setHttpHeaders(long, FileHTTPHeaders)}
     */
    public void setHTTPHeadersAsync() {
        FileAsyncClient fileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileAsyncClient.setHttpHeaders#long-filehttpheaders
        FileHTTPHeaders httpHeaders = new FileHTTPHeaders().fileContentType("text/plain");
        fileAsyncClient.setHttpHeaders(1024, httpHeaders)
            .subscribe(response -> System.out.printf("Setting the file httpHeaders completed with status code %d",
                response.statusCode()));
        // END: com.azure.storage.file.fileAsyncClient.setHttpHeaders#long-filehttpheaders
    }

    /**
     * Generates a code sample for using {@link FileClient#setHttpHeaders(long, FileHTTPHeaders)} to clear httpHeaders.
     */
    public void clearHTTPHeaders() {
        FileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileClient.setHttpHeaders#long-filehttpheaders.clearHttpHeaders
        Response<FileInfo> response = fileClient.setHttpHeaders(1024, null);
        System.out.printf("Setting the file httpHeaders completed with status code %d", response.statusCode());
        // END: com.azure.storage.file.fileClient.setHttpHeaders#long-filehttpheaders.clearHttpHeaders
    }

    /**
     * Generates a code sample for using {@link FileAsyncClient#setHttpHeaders(long, FileHTTPHeaders)} to clear httpHeaders.
     */
    public void clearHTTPHeadersAsync() {
        FileAsyncClient fileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileAsyncClient.setHttpHeaders#long-filehttpheaders.clearHttpHeaders
        fileAsyncClient.setHttpHeaders(1024, null)
            .subscribe(response -> System.out.printf("Setting the file httpHeaders completed with status code %d",
                response.statusCode()));
        // END: com.azure.storage.file.fileAsyncClient.setHttpHeaders#long-filehttpheaders.clearHttpHeaders
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
     * Generates a code sample for using {@link FileClient#listRanges(FileRange)}
     */
    public void listRangesMaxOverload() {
        FileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileClient.listRanges#filerange
        Iterable<FileRange> ranges = fileClient.listRanges(new FileRange(1024, 2048L));
        ranges.forEach(range ->
            System.out.printf("List ranges completed with start: %d, end: %d", range.start(), range.end()));
        // END: com.azure.storage.file.fileClient.listRanges#filerange
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
     * Generates a code sample for using {@link FileClient#listHandles()}
     */
    public void listHandles() {
        FileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileClient.listHandles
        fileClient.listHandles()
            .forEach(handleItem -> System.out.printf("List handles completed with handleId %d",
                handleItem.handleId()));
        // END: com.azure.storage.file.fileClient.listHandles
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
     * Generates a code sample for using {@link FileClient#listHandles(Integer)}
     */
    public void listHandlesWithOverload() {
        FileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileClient.listHandles#integer
        fileClient.listHandles(10)
            .forEach(handleItem -> System.out.printf("List handles completed with handleId %d",
                handleItem.handleId()));
        // END: com.azure.storage.file.fileClient.listHandles#integer
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
     * Generates a code sample for using {@link FileClient#forceCloseHandles(String)}
     */
    public void forceCloseHandles() {
        FileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileClient.forceCloseHandles#string
        fileClient.listHandles(10)
            .forEach(result ->
                fileClient.forceCloseHandles(result.handleId()).forEach(numOfClosedHandles ->
                    System.out.printf("Close %d handles.", numOfClosedHandles)
                ));
        // END: com.azure.storage.file.fileClient.forceCloseHandles#string
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
     * Generates a code sample for using {@link FileClient#getShareSnapshotId()}
     */
    public void getShareSnapshotId() {
        // BEGIN: com.azure.storage.file.fileClient.getShareSnapshotId
        OffsetDateTime currentTime = OffsetDateTime.of(LocalDateTime.now(), ZoneOffset.UTC);
        FileClient fileClient = new FileClientBuilder()
            .endpoint("https://${accountName}.file.core.windows.net")
            .credential(SASTokenCredential.fromQuery("${SASToken}"))
            .shareName("myshare")
            .filePath("myfile")
            .snapshot(currentTime.toString())
            .buildClient();
        fileClient.getShareSnapshotId();
        // END: com.azure.storage.file.fileClient.getShareSnapshotId
    }

    /**
     * Generates a code sample for using {@link FileAsyncClient#getShareSnapshotId()}
     */
    public void getShareSnapshotIdAsync() {
        // BEGIN: com.azure.storage.file.fileAsyncClient.getShareSnapshotId
        OffsetDateTime currentTime = OffsetDateTime.of(LocalDateTime.now(), ZoneOffset.UTC);
        FileAsyncClient fileAsyncClient = new FileClientBuilder()
            .endpoint("https://${accountName}.file.core.windows.net")
            .credential(SASTokenCredential.fromQuery("${SASToken}"))
            .shareName("myshare")
            .filePath("myfiile")
            .snapshot(currentTime.toString())
            .buildAsyncClient();
        fileAsyncClient.getShareSnapshotId();
        // END: com.azure.storage.file.fileAsyncClient.getShareSnapshotId
    }
}
