// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file;

import com.azure.storage.common.Utility;
import com.azure.storage.common.credentials.SASTokenCredential;
import com.azure.storage.common.credentials.SharedKeyCredential;
import com.azure.storage.file.models.FileHTTPHeaders;
import com.azure.storage.file.models.NtfsFileAttributes;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;

/**
 * Contains code snippets when generating javadocs through doclets for {@link DirectoryClient} and {@link DirectoryAsyncClient}.
 */
public class DirectoryAsyncJavaDocCodeSamples {

    /**
     * Generates code sample for {@link DirectoryAsyncClient} instantiation.
     */
    public void asyncInitialization() {
        // BEGIN: com.azure.storage.file.directoryAsyncClient.instantiation
        DirectoryAsyncClient client = new FileClientBuilder()
            .connectionString("${connectionString}")
            .endpoint("${endpoint}")
            .buildDirectoryAsyncClient();
        // END: com.azure.storage.file.directoryAsyncClient.instantiation
    }

    /**
     * Generates code sample for creating a {@link DirectoryAsyncClient} with {@link SASTokenCredential}
     * @return An instance of {@link DirectoryAsyncClient}
     */
    public DirectoryAsyncClient createAsyncClientWithSASToken() {
        // BEGIN: com.azure.storage.file.directoryAsyncClient.instantiation.sastoken
        DirectoryAsyncClient directoryAsyncClient = new FileClientBuilder()
            .endpoint("https://{accountName}.file.core.windows.net?{SASToken}")
            .shareName("myshare")
            .resourcePath("mydirectory")
            .buildDirectoryAsyncClient();
        // END: com.azure.storage.file.directoryAsyncClient.instantiation.sastoken
        return directoryAsyncClient;
    }

    /**
     * Generates code sample for creating a {@link DirectoryAsyncClient} with {@link SASTokenCredential}
     * @return An instance of {@link DirectoryAsyncClient}
     */
    public DirectoryAsyncClient createAsyncClientWithCredential() {
        // BEGIN: com.azure.storage.file.directoryAsyncClient.instantiation.credential
        DirectoryAsyncClient direcotryAsyncClient = new FileClientBuilder()
            .endpoint("https://{accountName}.file.core.windows.net")
            .credential(SASTokenCredential.fromQueryParameters(Utility.parseQueryString("${SASTokenQueryParams}")))
            .shareName("myshare")
            .resourcePath("mydirectory")
            .buildDirectoryAsyncClient();
        // END: com.azure.storage.file.directoryAsyncClient.instantiation.credential
        return direcotryAsyncClient;
    }

    /**
     * Generates code sample for creating a {@link DirectoryAsyncClient} with {@code connectionString} which turns into {@link SharedKeyCredential}
     * @return An instance of {@link DirectoryAsyncClient}
     */
    public DirectoryAsyncClient createAsyncClientWithConnectionString() {
        // BEGIN: com.azure.storage.file.directoryAsyncClient.instantiation.connectionstring
        String connectionString = "DefaultEndpointsProtocol=https;AccountName={name};AccountKey={key};"
            + "EndpointSuffix={core.windows.net}";
        DirectoryAsyncClient directoryAsyncClient = new FileClientBuilder()
            .connectionString(connectionString).shareName("myshare").resourcePath("mydirectory")
            .buildDirectoryAsyncClient();
        // END: com.azure.storage.file.directoryAsyncClient.instantiation.connectionstring
        return directoryAsyncClient;
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
            () -> System.out.println("Completed creating the directory!")
        );
        // END: com.azure.storage.file.directoryAsyncClient.create
    }

    /**
     * Generates a code sample for using {@link DirectoryAsyncClient#createWithResponse(FileSmbProperties, String, Map)}
     */
    public void createDirectoryWithResponseAsync() {
        DirectoryAsyncClient directoryAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryAsyncClient.createWithResponse#filesmbproperties-string-map
        FileSmbProperties smbProperties = new FileSmbProperties();
        String filePermission = "filePermission";
        Map<String, String> metadata = Collections.singletonMap("directory", "metadata");
        directoryAsyncClient.createWithResponse(smbProperties, filePermission, metadata).subscribe(
            response ->
                System.out.println("Completed creating the directory with status code:" + response.getStatusCode()),
            error -> System.err.print(error.toString())
        );
        // END: com.azure.storage.file.directoryAsyncClient.createWithResponse#filesmbproperties-string-map
    }

    /**
     * Generates code sample for creating a subdirectory with {@link DirectoryAsyncClient}
     */
    public void createSubDirectory() {
        DirectoryAsyncClient directoryAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryAsyncClient.createSubDirectory#string
        directoryAsyncClient.createSubDirectory("subdir")
            .doOnSuccess(response -> System.out.println("Completed creating the subdirectory."));
        // END: com.azure.storage.file.directoryAsyncClient.createSubDirectory#string
    }

    /**
     * Generates a code sample for using {@link DirectoryAsyncClient#createSubDirectoryWithResponse(String, FileSmbProperties, String, Map)}
     */
    public void createSubDirectoryAsyncMaxOverload() {
        DirectoryAsyncClient directoryAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryAsyncClient.createSubDirectoryWithResponse#string-filesmbproperties-string-map
        FileSmbProperties smbProperties = new FileSmbProperties();
        String filePermission = "filePermission";
        Map<String, String> metadata = Collections.singletonMap("directory", "metadata");
        directoryAsyncClient.createSubDirectoryWithResponse("subdir", smbProperties, filePermission, metadata).subscribe(
            response ->
                System.out.println("Successfully creating the subdirectory with status code: "
                    + response.getStatusCode()),
            error -> System.err.println(error.toString())
        );
        // END: com.azure.storage.file.directoryAsyncClient.createSubDirectoryWithResponse#string-filesmbproperties-string-map
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
            () -> System.out.println("Completed creating the file.")
        );
        // END: com.azure.storage.file.directoryAsyncClient.createFile#string-long
    }

    /**
     * Generates a code sample for using {@link DirectoryAsyncClient#createFileWithResponse(String, long, FileHTTPHeaders, FileSmbProperties, String, Map)}
     */
    public void createFileWithResponse() {
        DirectoryAsyncClient directoryAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryAsyncClient.createFileWithResponse#string-long-filehttpheaders-filesmbproperties-string-map
        FileHTTPHeaders httpHeaders = new FileHTTPHeaders()
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
        directoryAsyncClient.createFileWithResponse("myFile", 1024, httpHeaders, smbProperties, filePermission,
            Collections.singletonMap("directory", "metadata")).subscribe(
                response ->  System.out.printf("Creating the file completed with status code %d", response.getStatusCode()),
                error -> System.err.println(error.toString()),
                () -> System.out.println("Completed creating the file.")
        );
        // END: com.azure.storage.file.directoryAsyncClient.createFileWithResponse#string-long-filehttpheaders-filesmbproperties-string-map
    }

    /**
     * Generates a code sample for using {@link DirectoryAsyncClient#listFilesAndDirectories()}
     */
    public void listDirectoriesAndFilesAsync() {
        DirectoryAsyncClient directoryAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryAsyncClient.listFilesAndDirectories
        directoryAsyncClient.listFilesAndDirectories().subscribe(
            fileRef -> System.out.printf("Is the resource a directory? %b. The resource name is: %s.",
                fileRef.isDirectory(), fileRef.getName()),
            error -> System.err.println(error.toString()),
            () -> System.out.println("Completed listing the directories and files.")
        );
        // END: com.azure.storage.file.directoryAsyncClient.listFilesAndDirectories
    }

    /**
     * Generates a code sample for using {@link DirectoryAsyncClient#listFilesAndDirectories(String, Integer)}
     */
    public void listDirectoriesAndFilesAsyncMaxOverload() {
        DirectoryAsyncClient directoryAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryAsyncClient.listFilesAndDirectories#string-integer
        directoryAsyncClient.listFilesAndDirectories("subdir", 10).subscribe(
            fileRef -> System.out.printf("Is the resource a directory? %b. The resource name is: %s.",
                fileRef.isDirectory(), fileRef.getName()),
            error -> System.err.println(error.toString()),
            () -> System.out.println("Completed listing the directories and files.")
        );
        // END: com.azure.storage.file.directoryAsyncClient.listFilesAndDirectories#string-integer
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
            () -> System.out.println("Completed deleting the file.")
        );
        // END: com.azure.storage.file.directoryAsyncClient.deleteFile#string
    }

    /**
     * Generates a code sample for using {@link DirectoryAsyncClient#deleteFileWithResponse(String)}
     */
    public void deleteFileWithResponse() {
        DirectoryAsyncClient directoryAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryAsyncClient.deleteFileWithResponse#string
        directoryAsyncClient.deleteFileWithResponse("myfile").subscribe(
            response ->  System.out.printf("Delete file completed with status code %d", response.getStatusCode()),
            error -> System.err.println(error.toString()),
            () -> System.out.println("Completed deleting the file.")
        );
        // END: com.azure.storage.file.directoryAsyncClient.deleteFileWithResponse#string
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
            () -> System.out.println("Completed deleting the subdirectory.")
        );
        // END: com.azure.storage.file.directoryAsyncClient.deleteSubDirectory#string
    }

    /**
     * Generates a code sample for using {@link DirectoryAsyncClient#deleteSubDirectoryWithResponse(String)} ()}
     */
    public void deleteSubDirectoryWithResponse() {
        DirectoryAsyncClient directoryAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryAsyncClient.deleteSubDirectoryWithResponse#string
        directoryAsyncClient.deleteSubDirectoryWithResponse("mysubdirectory").subscribe(
            response ->  System.out.printf("Delete subdirectory completed with status code %d", response.getStatusCode()),
            error -> System.err.println(error.toString()),
            () -> System.out.println("Completed deleting the subdirectory.")
        );
        // END: com.azure.storage.file.directoryAsyncClient.deleteSubDirectoryWithResponse#string
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
            () -> System.out.println("Completed deleting the file.")
        );
        // END: com.azure.storage.file.directoryAsyncClient.delete
    }

    /**
     * Generates a code sample for using {@link DirectoryAsyncClient#deleteWithResponse()}
     */
    public void deleteWithResponse() {
        DirectoryAsyncClient directoryAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryAsyncClient.deleteWithResponse
        directoryAsyncClient.deleteWithResponse().subscribe(
            response ->  System.out.printf("Delete completed with status code %d", response.getStatusCode()),
            error -> System.err.println(error.toString())
        );
        // END: com.azure.storage.file.directoryAsyncClient.deleteWithResponse
    }

    /**
     * Generates a code sample for using {@link DirectoryAsyncClient#getProperties()}
     */
    public void getPropertiesAsync() {
        DirectoryAsyncClient directoryAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryAsyncClient.getProperties
        directoryAsyncClient.getProperties().subscribe(properties -> {
            System.out.printf("Directory latest modified date is %s.", properties.getLastModified());
        });
        // END: com.azure.storage.file.directoryAsyncClient.getProperties
    }

    /**
     * Generates a code sample for using {@link DirectoryAsyncClient#getPropertiesWithResponse()}
     */
    public void getPropertiesWithResponse() {
        DirectoryAsyncClient directoryAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryAsyncClient.getPropertiesWithResponse
        directoryAsyncClient.getPropertiesWithResponse().subscribe(properties -> {
            System.out.printf("Directory latest modified date is %s:", properties.getValue().getLastModified());
        });
        // END: com.azure.storage.file.directoryAsyncClient.getPropertiesWithResponse
    }

    /**
     * Generates a code sample for using {@link DirectoryAsyncClient#setProperties(FileSmbProperties, String)}
     */
    public void setPropertiesAsync() {
        DirectoryAsyncClient directoryAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryAsyncClient.setProperties#filesmbproperties-string
        FileSmbProperties smbProperties = new FileSmbProperties();
        String filePermission = "filePermission";
        directoryAsyncClient.setProperties(smbProperties, filePermission).subscribe(properties -> {
            System.out.printf("Directory latest modified date is %s:", properties.getLastModified());
        });
        // END: com.azure.storage.file.directoryAsyncClient.setProperties#filesmbproperties-string
    }

    /**
     * Generates a code sample for using {@link DirectoryAsyncClient#setPropertiesWithResponse(FileSmbProperties, String)}
     */
    public void setPropertiesWithResponse() {
        DirectoryAsyncClient directoryAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryAsyncClient.setPropertiesWithResponse#filesmbproperties-string
        FileSmbProperties smbProperties = new FileSmbProperties();
        String filePermission = "filePermission";
        directoryAsyncClient.setPropertiesWithResponse(smbProperties, filePermission).subscribe(properties -> {
            System.out.printf("Directory latest modified date is %s:", properties.getValue().getLastModified());
        });
        // END: com.azure.storage.file.directoryAsyncClient.setPropertiesWithResponse#filesmbproperties-string
    }

    /**
     * Generates a code sample for using {@link DirectoryAsyncClient#setMetadata(Map)}
     */
    public void setMetadataAsync() {
        DirectoryAsyncClient directoryAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryAsyncClient.setMetadata#map
        directoryAsyncClient.setMetadata(Collections.singletonMap("directory", "updatedMetadata"))
            .subscribe(response -> System.out.println("Setting the directory metadata completed."));
        // END: com.azure.storage.file.directoryAsyncClient.setMetadata#map
    }

    /**
     * Generates a code sample for using {@link DirectoryAsyncClient#setMetadata(Map)} to clear the metadata.
     */
    public void setMetadataClear() {
        DirectoryAsyncClient directoryAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryAsyncClient.setMetadata#map.clearMetadata
        directoryAsyncClient.setMetadata(null)
            .doOnSuccess(response ->  System.out.println("Clearing the directory metadata completed"));
        // END: com.azure.storage.file.directoryAsyncClient.setMetadata#map.clearMetadata
    }

    /**
     * Generates a code sample for using {@link DirectoryAsyncClient#setMetadataWithResponse(Map)} to clear the metadata.
     */
    public void setMetadataWithResponseClear() {
        DirectoryAsyncClient directoryAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryAsyncClient.setMetadataWithResponse#map.clearMetadata
        directoryAsyncClient.setMetadataWithResponse(null).subscribe(
            response ->  System.out.printf("Clearing the directory metadata completed with status code %d",
                response.getStatusCode()));
        // END: com.azure.storage.file.directoryAsyncClient.setMetadataWithResponse#map.clearMetadata
    }

    /**
     * Generates a code sample for using {@link DirectoryAsyncClient#setMetadataWithResponse(Map)}
     */
    public void setMetadataWithResponse() {
        DirectoryAsyncClient directoryAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryAsyncClient.setMetadataWithResponse#map
        directoryAsyncClient.setMetadataWithResponse(Collections.singletonMap("directory", "updatedMetadata"))
            .subscribe(response -> System.out.println("Setting the directory metadata completed with status code:"
                + response.getStatusCode()));
        // END: com.azure.storage.file.directoryAsyncClient.setMetadataWithResponse#map
    }

    /**
     * Generates a code sample for using {@link DirectoryAsyncClient#listHandles(Integer, boolean)}
     */
    public void listHandlesAsync() {
        DirectoryAsyncClient directoryAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryAsyncClient.listHandles#integer-boolean
        directoryAsyncClient.listHandles(10, true)
            .subscribe(handleItem -> System.out.printf("Get handles completed with handle id %s",
                handleItem.getHandleId()));
        // END: com.azure.storage.file.directoryAsyncClient.listHandles#integer-boolean
    }

    /**
     * Generates a code sample for using {@link DirectoryAsyncClient#forceCloseHandles(String, boolean)}
     */
    public void forceCloseHandlesAsync() {
        DirectoryAsyncClient directoryAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryAsyncClient.forceCloseHandles
        directoryAsyncClient.listHandles(10, true)
            .subscribe(handleItem -> directoryAsyncClient.forceCloseHandles(handleItem.getHandleId(), true)
                .subscribe(numOfClosedHandles -> System.out.printf("Closed %d handles.", numOfClosedHandles)));
        // END: com.azure.storage.file.directoryAsyncClient.forceCloseHandles
    }

    /**
     * Generates a code sample for using {@link DirectoryAsyncClient#getShareSnapshotId()}
     */
    public void getShareSnapshotIdAsync() {
        // BEGIN: com.azure.storage.file.directoryAsyncClient.getShareSnapshotId
        OffsetDateTime currentTime = OffsetDateTime.of(LocalDateTime.now(), ZoneOffset.UTC);
        DirectoryAsyncClient directoryAsyncClient = new FileClientBuilder()
            .endpoint("https://${accountName}.file.core.windows.net")
            .credential(SASTokenCredential.fromSASTokenString("${SASToken}"))
            .shareName("myshare")
            .resourcePath("mydirectory")
            .snapshot(currentTime.toString())
            .buildDirectoryAsyncClient();

        System.out.printf("Snapshot ID: %s%n", directoryAsyncClient.getShareSnapshotId());
        // END: com.azure.storage.file.directoryAsyncClient.getShareSnapshotId
    }
}
