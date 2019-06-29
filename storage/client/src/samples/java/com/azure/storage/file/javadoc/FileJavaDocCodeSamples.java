// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file.javadoc;

import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.VoidResponse;
import com.azure.storage.common.credentials.SASTokenCredential;
import com.azure.storage.common.credentials.SharedKeyCredential;
import com.azure.storage.file.FileAsyncClient;
import com.azure.storage.file.FileClient;
import com.azure.storage.file.models.FileCopyInfo;
import com.azure.storage.file.models.FileDownloadInfo;
import com.azure.storage.file.models.FileInfo;
import com.azure.storage.file.models.FileUploadInfo;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import reactor.core.publisher.Flux;

/**
 * Contains code snippets when generating javadocs through doclets for {@link FileClient} and {@link FileAsyncClient}.
 */
public class FileJavaDocCodeSamples {
    /**
     * Generates code sample for creating a {@link FileClient} with {@link SASTokenCredential}
     * @return An instance of {@link FileClient}
     */
    public FileClient createClientWithSASToken() {

        // BEGIN: com.azure.storage.file.fileClient.instantiation.sastoken
        FileClient fileClient = FileClient.builder()
            .endpoint("https://${accountName}.file.core.windows.net?${SASToken}")
            .shareName("myshare")
            .filePath("myfilepath")
            .build();
        // END: com.azure.storage.file.fileClient.instantiation.sastoken
        return fileClient;
    }

    /**
     * Generates code sample for creating a {@link FileAsyncClient} with {@link SASTokenCredential}
     * @return An instance of {@link FileAsyncClient}
     */
    public FileAsyncClient createAsyncClientWithSASToken() {
        // BEGIN: com.azure.storage.file.fileAsyncClient.instantiation.sastoken
        FileAsyncClient fileAsyncClient = FileAsyncClient.builder()
            .endpoint("https://{accountName}.file.core.windows.net?{SASToken}")
            .shareName("myshare")
            .filePath("myfilepath")
            .buildAsync();
        // END: com.azure.storage.file.fileAsyncClient.instantiation.sastoken
        return fileAsyncClient;
    }

    /**
     * Generates code sample for creating a {@link FileClient} with {@code connectionString} which turns into {@link SharedKeyCredential}
     * @return An instance of {@link FileClient}
     */
    public FileClient createClientWithConnectionString() {
        // BEGIN: com.azure.storage.file.fileClient.instantiation.connectionstring
        String connectionString = "DefaultEndpointsProtocol=https;AccountName={name};AccountKey={key};EndpointSuffix={core.windows.net}";
        FileClient fileClient = FileClient.builder()
            .connectionString(connectionString).shareName("myshare").filePath("myfilepath")
            .build();
        // END: com.azure.storage.file.fileClient.instantiation.connectionstring
        return fileClient;
    }

    /**
     * Generates code sample for creating a {@link FileAsyncClient} with {@code connectionString} which turns into {@link SharedKeyCredential}
     * @return An instance of {@link FileAsyncClient}
     */
    public FileAsyncClient createAsyncClientWithConnectionString() {
        // BEGIN: com.azure.storage.file.fileAsyncClient.instantiation.connectionstring
        String connectionString = "DefaultEndpointsProtocol=https;AccountName={name};AccountKey={key};EndpointSuffix={core.windows.net}";
        FileAsyncClient fileAsyncClient = FileAsyncClient.builder()
            .connectionString(connectionString).shareName("myshare").filePath("myfilepath")
            .buildAsync();
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
            response -> {},
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete creating the file!")
        );
        // END: com.azure.storage.file.fileAsyncClient.create
    }

    /**
     * Generates a code sample for using {@link FileClient#startCopy(String, Map)}
     */
    public void copyFile() {
        FileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileClient.startCopy#string-map
        Response<FileCopyInfo> response = fileClient.startCopy("https://{accountName}.file.core.windows.net?{SASToken}", Collections.singletonMap("file", "metadata"));
        System.out.println("Complete copying the file with status code: " + response.statusCode());
        // END: com.azure.storage.file.fileClient.startCopy#string-map
    }

    /**
     * Generates a code sample for using {@link FileClient#startCopy(String, Map)}
     */
    public void copyFileAsync() {
        FileAsyncClient fileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileAsyncClient.startCopy#string-map
        fileAsyncClient.startCopy("https://{accountName}.file.core.windows.net?{SASToken}", Collections.singletonMap("file", "metadata")).subscribe(
            response -> System.out.println("Successfully copying the file with status code: " + response.statusCode()),
            error -> System.err.println(error.toString()),
            () -> System.out.println("Complete copying the file.")
        );
        // END: com.azure.storage.file.fileAsyncClient.startCopy#string-map
    }

    /**
     * Generates a code sample for using {@link FileClient#upload(ByteBuf, long)}
     */
    public void uploadData() {
        FileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileClient.upload
        ByteBuf defaultData = Unpooled.wrappedBuffer("default".getBytes(StandardCharsets.UTF_8));
        Response<FileUploadInfo> response = fileClient.upload(defaultData, defaultData.readableBytes());
        System.out.println("Complete uploading the data with status code: " + response.statusCode());
        // END: com.azure.storage.file.fileClient.upload
    }

    /**
     * Generates a code sample for using {@link FileAsyncClient#upload(Flux, long)}
     */
    public void uploadDataAsync() {
        FileAsyncClient fileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileAsyncClient.upload
        ByteBuf defaultData = Unpooled.wrappedBuffer("default".getBytes(StandardCharsets.UTF_8));
        fileAsyncClient.upload(Flux.just(defaultData), defaultData.readableBytes()).subscribe(
            response -> {},
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete deleting the file!")
        );
        // END: com.azure.storage.file.fileAsyncClient.upload
    }

    /**
     * Generates a code sample for using {@link FileClient#uploadFromFile(String)}
     */
    public void uploadFile() {
        FileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileClient.uploadFromFile
        fileClient.uploadFromFile("someFilePath");
        // END: com.azure.storage.file.fileClient.uploadFromFile
    }

    /**
     * Generates a code sample for using {@link FileAsyncClient#delete()}
     */
    public void uploadFileAsync() {
        FileAsyncClient fileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileAsyncClient.uploadFromFile
        fileAsyncClient.uploadFromFile("someFilePath").subscribe(
            response -> {},
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete deleting the file!")
        );
        // END: com.azure.storage.file.fileAsyncClient.uploadFromFile
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
            byteBuf ->  System.out.println("Complete downloading the data with body: " + byteBuf.toString(StandardCharsets.UTF_8)),
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete downloading the data!")
        );
       ;
        // END: com.azure.storage.file.fileClient.downloadWithProperties
    }

    /**
     * Generates a code sample for using {@link FileAsyncClient#downloadWithProperties()}
     */
    public void downloadDataAsync() {
        FileAsyncClient fileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileAsyncClient.downloadWithProperties
        fileAsyncClient.downloadWithProperties().subscribe(
            response -> {},
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete downloading the data!")
        );
        // END: com.azure.storage.file.fileAsyncClient.downloadWithProperties
    }
    /**
     * Generates a code sample for using {@link FileClient#downloadToFile(String)}
     */
    public void downloadFile() {
        FileClient fileClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileClient.downloadToFile
        fileClient.downloadToFile("somelocalfilepath");
        if (Files.exists(Paths.get("somelocalfilepath"))) {
           System.out.println("Complete downloading the file.");
        }
        // END: com.azure.storage.file.fileClient.downloadToFile
    }

    /**
     * Generates a code sample for using {@link FileAsyncClient#downloadToFile(String)}
     */
    public void downloadFileAsync() {
        FileAsyncClient fileAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.fileAsyncClient.downloadToFile
        fileAsyncClient.downloadToFile("somelocalfilepath").subscribe(
            response -> {
                if (Files.exists(Paths.get("somelocalfilepath"))) {
                    System.out.println("Successfully downloaded the file.");
                }
            },
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete downloading the file!")
        );
        // END: com.azure.storage.file.fileAsyncClient.downloadToFile
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
            response -> {},
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete deleting the file!")
        );
        // END: com.azure.storage.file.fileAsyncClient.delete
    }

}
