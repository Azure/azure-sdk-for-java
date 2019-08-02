// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file;

import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.VoidResponse;
import com.azure.storage.common.credentials.SASTokenCredential;
import com.azure.storage.common.credentials.SharedKeyCredential;
import com.azure.storage.file.models.DirectoryInfo;
import com.azure.storage.file.models.DirectoryProperties;
import com.azure.storage.file.models.DirectorySetMetadataInfo;
import com.azure.storage.file.models.FileHTTPHeaders;
import com.azure.storage.file.models.HandleItem;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Map;

/**
 * Contains code snippets when generating javadocs through doclets for {@link DirectoryClient} and {@link DirectoryAsyncClient}.
 */
public class DirectoryJavaDocCodeSamples {
    /**
     * Generates code sample for {@link DirectoryClient} instantiation.
     */
    public void initialization() {
        // BEGIN: com.azure.storage.file.directoryClient.instantiation
        DirectoryClient client = new DirectoryClientBuilder()
            .connectionString("${connectionString}")
            .endpoint("${endpoint}")
            .buildClient();
        // END: com.azure.storage.file.directoryClient.instantiation
    }

    /**
     * Generates code sample for {@link DirectoryAsyncClient} instantiation.
     */
    public void asyncInitialization() {
        // BEGIN: com.azure.storage.file.directoryAsyncClient.instantiation
        DirectoryAsyncClient client = new DirectoryClientBuilder()
            .connectionString("${connectionString}")
            .endpoint("${endpoint}")
            .buildAsyncClient();
        // END: com.azure.storage.file.directoryAsyncClient.instantiation
    }

    /**
     * Generates code sample for creating a {@link DirectoryClient} with {@link SASTokenCredential}
     * @return An instance of {@link DirectoryClient}
     */
    public DirectoryClient createClientWithSASToken() {
        // BEGIN: com.azure.storage.file.directoryClient.instantiation.sastoken
        DirectoryClient directoryClient = new DirectoryClientBuilder()
            .endpoint("https://${accountName}.file.core.windows.net?${SASToken}")
            .shareName("myshare")
            .directoryPath("mydirectory")
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
            .directoryPath("mydirectory")
            .buildAsyncClient();
        // END: com.azure.storage.file.directoryAsyncClient.instantiation.sastoken
        return directoryAsyncClient;
    }

    /**
     * Generates code sample for creating a {@link DirectoryClient} with {@link SASTokenCredential}
     * @return An instance of {@link DirectoryClient}
     */
    public DirectoryClient createClientWithCredential() {

        // BEGIN: com.azure.storage.file.directoryClient.instantiation.credential
        DirectoryClient directoryClient = new DirectoryClientBuilder()
            .endpoint("https://${accountName}.file.core.windows.net")
            .credential(SASTokenCredential.fromQuery("${SASTokenQueryParams}"))
            .shareName("myshare")
            .directoryPath("mydirectory")
            .buildClient();
        // END: com.azure.storage.file.directoryClient.instantiation.credential
        return directoryClient;
    }

    /**
     * Generates code sample for creating a {@link DirectoryAsyncClient} with {@link SASTokenCredential}
     * @return An instance of {@link DirectoryAsyncClient}
     */
    public DirectoryAsyncClient createAsyncClientWithCredential() {
        // BEGIN: com.azure.storage.file.directoryAsyncClient.instantiation.credential
        DirectoryAsyncClient direcotryAsyncClient = new DirectoryClientBuilder()
            .endpoint("https://{accountName}.file.core.windows.net")
            .credential(SASTokenCredential.fromQuery("${SASTokenQueryParams}"))
            .shareName("myshare")
            .directoryPath("mydirectory")
            .buildAsyncClient();
        // END: com.azure.storage.file.directoryAsyncClient.instantiation.credential
        return direcotryAsyncClient;
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
            .connectionString(connectionString).shareName("myshare").directoryPath("mydirectory")
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
            .connectionString(connectionString).shareName("myshare").directoryPath("mydirectory")
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
     * Generates a code sample for using {@link DirectoryClient#create(Map)}
     */
    public void createDirectoryWithOverload() {
        DirectoryClient directoryClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryClient.create#map
        Response<DirectoryInfo> response = directoryClient.create(Collections.singletonMap("directory", "metadata"));
        System.out.println("Complete creating the directory with status code: " + response.statusCode());
        // END: com.azure.storage.file.directoryClient.create#map
    }

    /**
     * Generates a code sample for using {@link DirectoryAsyncClient#create(Map)}
     */
    public void createDirectoryAsyncWithOverload() {
        DirectoryAsyncClient directoryAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryAsyncClient.create#map
        directoryAsyncClient.create(Collections.singletonMap("directory", "metadata")).subscribe(
            response -> System.out.printf("Creating the directory completed with status code %d",
                response.statusCode()),
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete creating the directory!")
        );
        // END: com.azure.storage.file.directoryAsyncClient.create#map
    }

    /**
     * Generates a code sample for using {@link DirectoryClient#createSubDirectory(String)
     */
    public void createSubDirectory() {
        DirectoryClient directoryClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryClient.createSubDirectory#string
        Response<DirectoryClient> response = directoryClient.createSubDirectory("subdir");
        System.out.println("Complete creating the subdirectory with status code " + response.statusCode());
        // END: com.azure.storage.file.directoryClient.createSubDirectory#string
    }

    /**
     * Generates a code sample for using {@link DirectoryAsyncClient#createSubDirectory(String)}
     */
    public void createSubDirectoryAsync() {
        DirectoryAsyncClient directoryAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryAsyncClient.createSubDirectory#string
        directoryAsyncClient.createSubDirectory("subdir").subscribe(
            response -> System.out.println("Successfully creating the subdirectory with status code: "
                + response.statusCode()),
            error -> System.err.println(error.toString()),
            () -> System.out.println("Complete creating the sub directory.")
        );
        // END: com.azure.storage.file.directoryAsyncClient.createSubDirectory#string
    }

    /**
     * Generates a code sample for using {@link DirectoryClient#createSubDirectory(String, Map)}
     */
    public void createSubDirectoryMaxOverload() {
        DirectoryClient directoryClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryClient.createSubDirectory#string-map
        Response<DirectoryClient> response = directoryClient.createSubDirectory("subdir",
            Collections.singletonMap("directory", "metadata"));
        System.out.printf("Creating the sub directory completed with status code %d", response.statusCode());
        // END: com.azure.storage.file.directoryClient.createSubDirectory#string-map
    }

    /**
     * Generates a code sample for using {@link DirectoryAsyncClient#createSubDirectory(String, Map)}
     */
    public void createSubDirectoryAsyncMaxOverload() {
        DirectoryAsyncClient directoryAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryAsyncClient.createSubDirectory#string-map
        directoryAsyncClient.createSubDirectory("subdir",
            Collections.singletonMap("directory", "metadata"))
            .subscribe(response -> System.out.printf("Creating the subdirectory completed with status code %d",
                response.statusCode()));
        // END: com.azure.storage.file.directoryAsyncClient.createSubDirectory#string-map
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
     * Generates a code sample for using {@link DirectoryClient#createFile(String, long, FileHTTPHeaders, Map)}
     */
    public void createFileMaxOverload() {
        DirectoryClient directoryClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryClient.createFile#string-long-fileHTTPHeaders-map
        FileHTTPHeaders httpHeaders = new FileHTTPHeaders().fileContentType("text/plain");
        Response<FileClient> response = directoryClient.createFile("myFile", 1024,
            httpHeaders, Collections.singletonMap("directory", "metadata"));
        System.out.println("Complete creating the file with status code: " + response.statusCode());
        // END: com.azure.storage.file.directoryClient.createFile#string-long-fileHTTPHeaders-map
    }

    /**
     * Generates a code sample for using {@link DirectoryAsyncClient#createFile(String, long, FileHTTPHeaders, Map)}
     */
    public void createFileAsyncMaxOverload() {
        DirectoryAsyncClient directoryAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryAsyncClient.createFile#string-long-fileHTTPHeaders-map
        FileHTTPHeaders httpHeaders = new FileHTTPHeaders().fileContentType("text/plain");
        directoryAsyncClient.createFile("myFile", 1024, httpHeaders,
            Collections.singletonMap("directory", "metadata")).subscribe(
                response ->  System.out.printf("Creating the file completed with status code %d", response.statusCode()),
                error -> System.err.println(error.toString()),
                () -> System.out.println("Complete creating the file.")
        );
        // END: com.azure.storage.file.directoryAsyncClient.createFile#string-long-fileHTTPHeaders-map
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
     * Generates a code sample for using {@link DirectoryClient#listFilesAndDirectories(String, Integer)}
     */
    public void listDirectoriesAndFilesMaxOverload() {
        DirectoryClient directoryClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryClient.listFilesAndDirectories#string-integer
        directoryClient.listFilesAndDirectories("subdir", 10).forEach(
            fileRef -> System.out.printf("Is the resource a directory? %b. The resource name is: %s.",
                fileRef.isDirectory(), fileRef.name())
        );
        // END: com.azure.storage.file.directoryClient.listFilesAndDirectories#string-integer
    }


    /**
     * Generates a code sample for using {@link DirectoryAsyncClient#listFilesAndDirectories(String, Integer)}
     */
    public void listDirectoriesAndFilesAsyncMaxOverload() {
        DirectoryAsyncClient directoryAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryAsyncClient.listFilesAndDirectories#string-integer
        directoryAsyncClient.listFilesAndDirectories("subdir", 10).subscribe(
            fileRef -> System.out.printf("Is the resource a directory? %b. The resource name is: %s.",
                fileRef.isDirectory(), fileRef.name()),
            error -> System.err.println(error.toString()),
            () -> System.out.println("Complete listing the directories and files.")
        );
        // END: com.azure.storage.file.directoryAsyncClient.listFilesAndDirectories#string-integer
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

    /**
     * Generates a code sample for using {@link DirectoryClient#getProperties()}
     */
    public void getProperties() {
        DirectoryClient directoryClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryClient.getProperties
        Response<DirectoryProperties> response = directoryClient.getProperties();
        System.out.printf("Directory latest modified date is %s.", response.value().lastModified());
        // END: com.azure.storage.file.directoryClient.getProperties
    }

    /**
     * Generates a code sample for using {@link DirectoryAsyncClient#getProperties()}
     */
    public void getPropertiesAsync() {
        DirectoryAsyncClient directoryAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryAsyncClient.getProperties
        directoryAsyncClient.getProperties()
            .subscribe(response -> {
                DirectoryProperties properties = response.value();
                System.out.printf("Directory latest modified date is %s.", properties.lastModified());
            });
        // END: com.azure.storage.file.directoryAsyncClient.getProperties
    }

    /**
     * Generates a code sample for using {@link DirectoryClient#setMetadata(Map)}
     */
    public void setMetadata() {
        DirectoryClient directoryClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryClient.setMetadata#map
        Response<DirectorySetMetadataInfo> response =
            directoryClient.setMetadata(Collections.singletonMap("directory", "updatedMetadata"));
        System.out.printf("Setting the directory metadata completed with status code %d", response.statusCode());
        // END: com.azure.storage.file.directoryClient.setMetadata#map
    }

    /**
     * Generates a code sample for using {@link DirectoryAsyncClient#setMetadata(Map)}
     */
    public void setMetadataAsync() {
        DirectoryAsyncClient directoryAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryAsyncClient.setMetadata#map
        directoryAsyncClient.setMetadata(Collections.singletonMap("directory", "updatedMetadata"))
            .subscribe(response -> System.out.printf("Setting the directory metadata completed with status code %d",
                response.statusCode()));
        // END: com.azure.storage.file.directoryAsyncClient.setMetadata#map
    }

    /**
     * Generates a code sample for using {@link DirectoryClient#setMetadata(Map)} to clear the metadata.
     */
    public void clearMetadata() {
        DirectoryClient directoryClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryClient.setMetadata#map.clearMetadata
        Response<DirectorySetMetadataInfo> response = directoryClient.setMetadata(null);
        System.out.printf("Directory latest modified date is %s.", response.statusCode());
        // END: com.azure.storage.file.directoryClient.setMetadata#map.clearMetadata
    }

    /**
     * Generates a code sample for using {@link DirectoryAsyncClient#setMetadata(Map)} to clear the metadata.
     */
    public void clearMetadataAsync() {
        DirectoryAsyncClient directoryAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryAsyncClient.setMetadata#map.clearMetadata
        directoryAsyncClient.setMetadata(null)
            .subscribe(response ->  System.out.printf("Clearing the directory metadata completed with status code %d",
                response.statusCode()));
        // END: com.azure.storage.file.directoryAsyncClient.setMetadata#map.clearMetadata
    }

    /**
     * Generates a code sample for using {@link DirectoryClient#getHandles(Integer, boolean)}
     */
    public void getHandles() {
        DirectoryClient directoryClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryClient.getHandles
        Iterable<HandleItem> result = directoryClient.getHandles(10, true);
        System.out.printf("Get handles completed with handle id %s", result.iterator().next().handleId());
        // END: com.azure.storage.file.directoryClient.getHandles
    }

    /**
     * Generates a code sample for using {@link DirectoryAsyncClient#getHandles(Integer, boolean)}
     */
    public void getHandlesAsync() {
        DirectoryAsyncClient directoryAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryAsyncClient.getHandles
        directoryAsyncClient.getHandles(10, true)
            .subscribe(handleItem -> System.out.printf("Get handles completed with handle id %s",
                handleItem.handleId()));
        // END: com.azure.storage.file.directoryAsyncClient.getHandles
    }

    /**
     * Generates a code sample for using {@link DirectoryClient#forceCloseHandles(String, boolean)}
     */
    public void forceCloseHandles() {
        DirectoryClient directoryClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryClient.forceCloseHandles
        Iterable<HandleItem> result = directoryClient.getHandles(10, true);
        result.forEach(handleItem ->  {
            directoryClient.forceCloseHandles(handleItem.handleId(), true).forEach(numOfClosedHandles ->
                System.out.printf("Get handles completed with handle id %s", handleItem.handleId()));
        });
        // END: com.azure.storage.file.directoryClient.forceCloseHandles
    }

    /**
     * Generates a code sample for using {@link DirectoryAsyncClient#forceCloseHandles(String, boolean)}
     */
    public void forceCloseHandlesAsync() {
        DirectoryAsyncClient directoryAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryAsyncClient.forceCloseHandles
        directoryAsyncClient.getHandles(10, true)
            .subscribe(handleItem -> {
                directoryAsyncClient.forceCloseHandles(handleItem.handleId(), true)
                    .subscribe(numOfClosedHandles ->
                    System.out.printf("Close %d handles.", numOfClosedHandles));
            });
        // END: com.azure.storage.file.directoryAsyncClient.forceCloseHandles
    }

    /**
     * Generates a code sample for using {@link DirectoryClient#getShareSnapshotId()}
     */
    public void getShareSnapshotId() {
        // BEGIN: com.azure.storage.file.directoryClient.getShareSnapshotId
        OffsetDateTime currentTime = OffsetDateTime.of(LocalDateTime.now(), ZoneOffset.UTC);
        DirectoryClient directoryClient = new DirectoryClientBuilder()
            .endpoint("https://${accountName}.file.core.windows.net")
            .credential(SASTokenCredential.fromQuery("${SASToken}"))
            .shareName("myshare")
            .directoryPath("mydirectory")
            .snapshot(currentTime.toString())
            .buildClient();
        directoryClient.getShareSnapshotId();
        // END: com.azure.storage.file.directoryClient.getShareSnapshotId
    }

    /**
     * Generates a code sample for using {@link DirectoryAsyncClient#getShareSnapshotId()}
     */
    public void getShareSnapshotIdAsync() {
        // BEGIN: com.azure.storage.file.directoryAsyncClient.getShareSnapshotId
        OffsetDateTime currentTime = OffsetDateTime.of(LocalDateTime.now(), ZoneOffset.UTC);
        DirectoryAsyncClient directoryAsyncClient = new DirectoryClientBuilder()
            .endpoint("https://${accountName}.file.core.windows.net")
            .credential(SASTokenCredential.fromQuery("${SASToken}"))
            .shareName("myshare")
            .directoryPath("mydirectory")
            .snapshot(currentTime.toString())
            .buildAsyncClient();
        directoryAsyncClient.getShareSnapshotId();
        // END: com.azure.storage.file.directoryAsyncClient.getShareSnapshotId
    }
}
