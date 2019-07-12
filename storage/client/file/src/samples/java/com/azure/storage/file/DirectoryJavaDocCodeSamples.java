// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file;

import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.VoidResponse;
import com.azure.storage.common.credentials.SASTokenCredential;
import com.azure.storage.common.credentials.SharedKeyCredential;
import com.azure.storage.file.models.DirectoryInfo;

/**
 * Contains code snippets when generating javadocs through doclets for {@link DirectoryClient} and {@link DirectoryAsyncClient}.
 */
public class DirectoryJavaDocCodeSamples {

    /**
     * Generates code sample for creating a {@link DirectoryClient} with {@link SASTokenCredential}
     * @return An instance of {@link DirectoryClient}
     */
    public DirectoryClient createClientWithSASToken() {
        // BEGIN: com.azure.storage.file.directoryClient.instantiation.sastoken
        DirectoryClient directoryClient = new DirectoryClientBuilder()
            .endpoint("https://${accountName}.file.core.windows.net?${SASToken}")
            .shareName("myshare")
            .directoryName("mydirectory")
            .buildClient();
        // END: com.azure.storage.file.directoryClient.instantiation.sastoken
        return directoryClient;
    }

    /**
     * Generates code sample for creating a {@link DirectoryAsyncClient} with {@link SASTokenCredential}
     * @return An instance of {@link DirectoryAsyncClient}
     */
    public DirectoryAsyncClient createAsyncClientWithSASToken() {
        // BEGIN: com.azure.storage.file.directoryAsyncClient.instantiation.sastoken
        DirectoryAsyncClient directoryAsyncClient = new DirectoryClientBuilder()
            .endpoint("https://{accountName}.file.core.windows.net?{SASToken}")
            .shareName("myshare")
            .directoryName("mydirectory")
            .buildAsyncClient();
        // END: com.azure.storage.file.directoryAsyncClient.instantiation.sastoken
        return directoryAsyncClient;
    }

    /**
     * Generates code sample for creating a {@link DirectoryClient} with {@code connectionString} which turns into {@link SharedKeyCredential}
     * @return An instance of {@link DirectoryClient}
     */
    public DirectoryClient createClientWithConnectionString() {
        // BEGIN: com.azure.storage.file.directoryClient.instantiation.connectionstring
        String connectionString = "DefaultEndpointsProtocol=https;AccountName={name};AccountKey={key}"
            + ";EndpointSuffix={core.windows.net}";
        DirectoryClient directoryClient = new DirectoryClientBuilder()
            .connectionString(connectionString).shareName("myshare").directoryName("mydirectory")
            .buildClient();
        // END: com.azure.storage.file.directoryClient.instantiation.connectionstring
        return directoryClient;
    }

    /**
     * Generates code sample for creating a {@link DirectoryAsyncClient} with {@code connectionString} which turns into {@link SharedKeyCredential}
     * @return An instance of {@link DirectoryAsyncClient}
     */
    public DirectoryAsyncClient createAsyncClientWithConnectionString() {
        // BEGIN: com.azure.storage.file.directoryAsyncClient.instantiation.connectionstring
        String connectionString = "DefaultEndpointsProtocol=https;AccountName={name};AccountKey={key};"
            + "EndpointSuffix={core.windows.net}";
        DirectoryAsyncClient directoryAsyncClient = new DirectoryClientBuilder()
            .connectionString(connectionString).shareName("myshare").directoryName("mydirectory")
            .buildAsyncClient();
        // END: com.azure.storage.file.directoryAsyncClient.instantiation.connectionstring
        return directoryAsyncClient;
    }

    /**
     * Generates a code sample for using {@link DirectoryClient#create()}
     */
    public void createDirectory() {
        DirectoryClient directoryClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryClient.createDirectory
        Response<DirectoryInfo> response = directoryClient.create();
        System.out.println("Complete creating the directory with status code: " + response.statusCode());
        // END: com.azure.storage.file.directoryClient.createDirectory
    }

    /**
     * Generates a code sample for using {@link DirectoryAsyncClient#create}
     */
    public void createDirectoryAsync() {
        DirectoryAsyncClient directoryAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryAsyncClient.create
        directoryAsyncClient.create().subscribe(
            response -> { },
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete creating the directory!")
        );
        // END: com.azure.storage.file.directoryAsyncClient.create
    }

    /**
     * Generates a code sample for using {@link DirectoryClient#createSubDirectory(String)
     */
    public void createSubDirectory() {
        DirectoryClient directoryClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryClient.createSubDirectory#string
        Response<DirectoryClient> response = directoryClient.createSubDirectory("mysubdirectory");
        System.out.println("Complete creating the subdirectory with status code " + response.statusCode());
        // END: com.azure.storage.file.directoryClient.createSubDirectory#string
    }

    /**
     * Generates a code sample for using {@link DirectoryAsyncClient#createSubDirectory(String)}
     */
    public void createSubDirectoryAsync() {
        DirectoryAsyncClient directoryAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryAsyncClient.createSubDirectory#string
        directoryAsyncClient.createSubDirectory("mysubdirectory").subscribe(
            response -> System.out.println("Successfully creating the subdirectory with status code: "
                + response.statusCode()),
            error -> System.err.println(error.toString()),
            () -> System.out.println("Complete creating the sub directory.")
        );
        // END: com.azure.storage.file.directoryAsyncClient.createSubDirectory#string
    }

    /**
     * Generates a code sample for using {@link DirectoryClient#createFile(String, long)}
     */
    public void createFile() {
        DirectoryClient directoryClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryClient.createFile#string-long
        Response<FileClient> response = directoryClient.createFile("myfile", 1024);
        System.out.println("Complete creating the file with status code: " + response.statusCode());
        // END: com.azure.storage.file.directoryClient.createFile#string-long
    }

    /**
     * Generates a code sample for using {@link DirectoryAsyncClient#createFile(String, long)}
     */
    public void createFileAsync() {
        DirectoryAsyncClient directoryAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryAsyncClient.createFile#string-long
        directoryAsyncClient.createFile("myfile", 1024).subscribe(
            response -> { },
            error -> System.err.println(error.toString()),
            () -> System.out.println("Complete creating the file.")
        );
        // END: com.azure.storage.file.directoryAsyncClient.createFile#string-long
    }

    /**
     * Generates a code sample for using {@link DirectoryClient#listFilesAndDirectories()}
     */
    public void listDirectoriesAndFiles() {
        DirectoryClient directoryClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryClient.listFilesAndDirectories
        directoryClient.listFilesAndDirectories().forEach(
            fileRef -> System.out.printf("Is the resource a directory? %b. The resource name is: %s.",
                fileRef.isDirectory(), fileRef.name())
        );
        // END: com.azure.storage.file.directoryClient.listFilesAndDirectories
    }


    /**
     * Generates a code sample for using {@link DirectoryAsyncClient#listFilesAndDirectories()}
     */
    public void listDirectoriesAndFilesAsync() {
        DirectoryAsyncClient directoryAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryAsyncClient.listFilesAndDirectories
        directoryAsyncClient.listFilesAndDirectories().subscribe(
            fileRef -> System.out.printf("Is the resource a directory? %b. The resource name is: %s.",
                fileRef.isDirectory(), fileRef.name()),
            error -> System.err.println(error.toString()),
            () -> System.out.println("Complete listing the directories and files.")
        );
        // END: com.azure.storage.file.directoryAsyncClient.listFilesAndDirectories
    }

    /**
     * Generates a code sample for using {@link DirectoryClient#deleteFile(String)} ()}
     */
    public void deleteFile() {
        DirectoryClient directoryClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryClient.deleteFile#string
        VoidResponse response = directoryClient.deleteFile("myfile");
        System.out.println("Complete deleting the file with status code: " + response.statusCode());
        // END: com.azure.storage.file.directoryClient.deleteFile#string
    }

    /**
     * Generates a code sample for using {@link DirectoryAsyncClient#deleteFile(String)} ()}
     */
    public void deleteFileAsync() {
        DirectoryAsyncClient directoryAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryAsyncClient.deleteFile#string
        directoryAsyncClient.deleteFile("myfile").subscribe(
            response -> { },
            error -> System.err.println(error.toString()),
            () -> System.out.println("Complete deleting the file.")
        );
        // END: com.azure.storage.file.directoryAsyncClient.deleteFile#string
    }

    /**
     * Generates a code sample for using {@link DirectoryClient#deleteSubDirectory(String)}
     */
    public void deleteSubDirectory() {
        DirectoryClient directoryClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryClient.deleteSubDirectory#string
        VoidResponse response = directoryClient.deleteSubDirectory("mysubdirectory");
        System.out.println("Complete deleting the subdirectory with status code: " + response.statusCode());
        // END: com.azure.storage.file.directoryClient.deleteSubDirectory#string
    }

    /**
     * Generates a code sample for using {@link DirectoryAsyncClient#deleteSubDirectory(String)} ()}
     */
    public void deleteSubDirectoryAsync() {
        DirectoryAsyncClient directoryAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryAsyncClient.deleteSubDirectory#string
        directoryAsyncClient.deleteSubDirectory("mysubdirectory").subscribe(
            response -> { },
            error -> System.err.println(error.toString()),
            () -> System.out.println("Complete deleting the subdirectory.")
        );
        // END: com.azure.storage.file.directoryAsyncClient.deleteSubDirectory#string
    }

    /**
     * Generates a code sample for using {@link DirectoryClient#delete()} ()}
     */
    public void deleteDirectory() {
        DirectoryClient directoryClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryClient.delete
        VoidResponse response = directoryClient.delete();
        System.out.println("Complete deleting the file with status code: " + response.statusCode());
        // END: com.azure.storage.file.directoryClient.delete
    }

    /**
     * Generates a code sample for using {@link DirectoryAsyncClient#delete()}
     */
    public void deleteDirectoryAsync() {
        DirectoryAsyncClient directoryAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryAsyncClient.delete
        directoryAsyncClient.delete().subscribe(
            response -> { },
            error -> System.err.println(error.toString()),
            () -> System.out.println("Complete deleting the file.")
        );
        // END: com.azure.storage.file.directoryAsyncClient.delete
    }
}
