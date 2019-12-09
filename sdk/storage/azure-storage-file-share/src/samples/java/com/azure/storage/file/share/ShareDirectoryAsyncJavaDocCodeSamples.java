// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file.share;

import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.file.share.models.ShareFileHttpHeaders;
import com.azure.storage.file.share.models.NtfsFileAttributes;
import com.azure.storage.file.share.sas.ShareFileSasPermission;
import com.azure.storage.file.share.sas.ShareServiceSasSignatureValues;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;

/**
 * Contains code snippets when generating javadocs through doclets for {@link ShareDirectoryClient} and {@link ShareDirectoryAsyncClient}.
 */
public class ShareDirectoryAsyncJavaDocCodeSamples {

    /**
     * Generates code sample for {@link ShareDirectoryAsyncClient} instantiation.
     */
    public void asyncInitialization() {
        // BEGIN: com.azure.storage.file.share.ShareDirectoryAsyncClient.instantiation
        ShareDirectoryAsyncClient client = new ShareFileClientBuilder()
            .connectionString("${connectionString}")
            .endpoint("${endpoint}")
            .buildDirectoryAsyncClient();
        // END: com.azure.storage.file.share.ShareDirectoryAsyncClient.instantiation
    }

    /**
     * Generates code sample for creating a {@link ShareDirectoryAsyncClient} with SAS token.
     * @return An instance of {@link ShareDirectoryAsyncClient}
     */
    public ShareDirectoryAsyncClient createAsyncClientWithSASToken() {
        // BEGIN: com.azure.storage.file.share.ShareDirectoryAsyncClient.instantiation.sastoken
        ShareDirectoryAsyncClient shareDirectoryAsyncClient = new ShareFileClientBuilder()
            .endpoint("https://{accountName}.file.core.windows.net?{SASToken}")
            .shareName("myshare")
            .resourcePath("mydirectory")
            .buildDirectoryAsyncClient();
        // END: com.azure.storage.file.share.ShareDirectoryAsyncClient.instantiation.sastoken
        return shareDirectoryAsyncClient;
    }

    /**
     * Generates code sample for creating a {@link ShareDirectoryAsyncClient} with SAS token.
     * @return An instance of {@link ShareDirectoryAsyncClient}
     */
    public ShareDirectoryAsyncClient createAsyncClientWithCredential() {
        // BEGIN: com.azure.storage.file.share.ShareDirectoryAsyncClient.instantiation.credential
        ShareDirectoryAsyncClient direcotryAsyncClient = new ShareFileClientBuilder()
            .endpoint("https://{accountName}.file.core.windows.net")
            .sasToken("${SASTokenQueryParams}")
            .shareName("myshare")
            .resourcePath("mydirectory")
            .buildDirectoryAsyncClient();
        // END: com.azure.storage.file.share.ShareDirectoryAsyncClient.instantiation.credential
        return direcotryAsyncClient;
    }

    /**
     * Generates code sample for creating a {@link ShareDirectoryAsyncClient} with {@code connectionString} which turns into {@link StorageSharedKeyCredential}
     * @return An instance of {@link ShareDirectoryAsyncClient}
     */
    public ShareDirectoryAsyncClient createAsyncClientWithConnectionString() {
        // BEGIN: com.azure.storage.file.share.ShareDirectoryAsyncClient.instantiation.connectionstring
        String connectionString = "DefaultEndpointsProtocol=https;AccountName={name};AccountKey={key};"
            + "EndpointSuffix={core.windows.net}";
        ShareDirectoryAsyncClient shareDirectoryAsyncClient = new ShareFileClientBuilder()
            .connectionString(connectionString).shareName("myshare").resourcePath("mydirectory")
            .buildDirectoryAsyncClient();
        // END: com.azure.storage.file.share.ShareDirectoryAsyncClient.instantiation.connectionstring
        return shareDirectoryAsyncClient;
    }


    /**
     * Generates a code sample for using {@link ShareDirectoryAsyncClient#create}
     */
    public void createDirectoryAsync() {
        ShareDirectoryAsyncClient shareDirectoryAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareDirectoryAsyncClient.create
        shareDirectoryAsyncClient.create().subscribe(
            response -> { },
            error -> System.err.print(error.toString()),
            () -> System.out.println("Completed creating the directory!")
        );
        // END: com.azure.storage.file.share.ShareDirectoryAsyncClient.create
    }

    /**
     * Generates a code sample for using {@link ShareDirectoryAsyncClient#createWithResponse(FileSmbProperties, String, Map)}
     */
    public void createDirectoryWithResponseAsync() {
        ShareDirectoryAsyncClient shareDirectoryAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareDirectoryAsyncClient.createWithResponse#FileSmbProperties-String-Map
        FileSmbProperties smbProperties = new FileSmbProperties();
        String filePermission = "filePermission";
        Map<String, String> metadata = Collections.singletonMap("directory", "metadata");
        shareDirectoryAsyncClient.createWithResponse(smbProperties, filePermission, metadata).subscribe(
            response ->
                System.out.println("Completed creating the directory with status code:" + response.getStatusCode()),
            error -> System.err.print(error.toString())
        );
        // END: com.azure.storage.file.share.ShareDirectoryAsyncClient.createWithResponse#FileSmbProperties-String-Map
    }

    /**
     * Generates code sample for creating a subdirectory with {@link ShareDirectoryAsyncClient}
     */
    public void createSubdirectory() {
        ShareDirectoryAsyncClient shareDirectoryAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareDirectoryAsyncClient.createSubdirectory#string
        shareDirectoryAsyncClient.createSubdirectory("subdir")
            .doOnSuccess(response -> System.out.println("Completed creating the subdirectory."));
        // END: com.azure.storage.file.share.ShareDirectoryAsyncClient.createSubdirectory#string
    }

    /**
     * Generates a code sample for using {@link ShareDirectoryAsyncClient#createSubdirectoryWithResponse(String, FileSmbProperties, String, Map)}
     */
    public void createSubdirectoryAsyncMaxOverload() {
        ShareDirectoryAsyncClient shareDirectoryAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareDirectoryAsyncClient.createSubdirectoryWithResponse#String-FileSmbProperties-String-Map
        FileSmbProperties smbProperties = new FileSmbProperties();
        String filePermission = "filePermission";
        Map<String, String> metadata = Collections.singletonMap("directory", "metadata");
        shareDirectoryAsyncClient.createSubdirectoryWithResponse("subdir", smbProperties, filePermission, metadata).subscribe(
            response ->
                System.out.println("Successfully creating the subdirectory with status code: "
                    + response.getStatusCode()),
            error -> System.err.println(error.toString())
        );
        // END: com.azure.storage.file.share.ShareDirectoryAsyncClient.createSubdirectoryWithResponse#String-FileSmbProperties-String-Map
    }

    /**
     * Generates a code sample for using {@link ShareDirectoryAsyncClient#createFile(String, long)}
     */
    public void createFileAsync() {
        ShareDirectoryAsyncClient shareDirectoryAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareDirectoryAsyncClient.createFile#string-long
        shareDirectoryAsyncClient.createFile("myfile", 1024).subscribe(
            response -> { },
            error -> System.err.println(error.toString()),
            () -> System.out.println("Completed creating the file.")
        );
        // END: com.azure.storage.file.share.ShareDirectoryAsyncClient.createFile#string-long
    }

    /**
     * Generates a code sample for using {@link ShareDirectoryAsyncClient#createFileWithResponse(String, long, ShareFileHttpHeaders, FileSmbProperties, String, Map)}
     */
    public void createFileWithResponse() {
        ShareDirectoryAsyncClient shareDirectoryAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareDirectoryAsyncClient.createFileWithResponse#com.azure.storage.file.share.ShareDirectoryAsyncClient.createFileWithResponse#String-long-ShareFileHttpHeaders-FileSmbProperties-String-Map
        ShareFileHttpHeaders httpHeaders = new ShareFileHttpHeaders()
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
        shareDirectoryAsyncClient.createFileWithResponse("myFile", 1024, httpHeaders, smbProperties, filePermission,
            Collections.singletonMap("directory", "metadata")).subscribe(
                response ->  System.out.printf("Creating the file completed with status code %d", response.getStatusCode()),
                error -> System.err.println(error.toString()),
                () -> System.out.println("Completed creating the file.")
        );
        // END: com.azure.storage.file.share.ShareDirectoryAsyncClient.createFileWithResponse#com.azure.storage.file.share.ShareDirectoryAsyncClient.createFileWithResponse#String-long-ShareFileHttpHeaders-FileSmbProperties-String-Map
    }

    /**
     * Generates a code sample for using {@link ShareDirectoryAsyncClient#listFilesAndDirectories()}
     */
    public void listDirectoriesAndFilesAsync() {
        ShareDirectoryAsyncClient shareDirectoryAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareDirectoryAsyncClient.listFilesAndDirectories
        shareDirectoryAsyncClient.listFilesAndDirectories().subscribe(
            fileRef -> System.out.printf("Is the resource a directory? %b. The resource name is: %s.",
                fileRef.isDirectory(), fileRef.getName()),
            error -> System.err.println(error.toString()),
            () -> System.out.println("Completed listing the directories and files.")
        );
        // END: com.azure.storage.file.share.ShareDirectoryAsyncClient.listFilesAndDirectories
    }

    /**
     * Generates a code sample for using {@link ShareDirectoryAsyncClient#listFilesAndDirectories(String, Integer)}
     */
    public void listDirectoriesAndFilesAsyncMaxOverload() {
        ShareDirectoryAsyncClient shareDirectoryAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareDirectoryAsyncClient.listFilesAndDirectories#string-integer
        shareDirectoryAsyncClient.listFilesAndDirectories("subdir", 10).subscribe(
            fileRef -> System.out.printf("Is the resource a directory? %b. The resource name is: %s.",
                fileRef.isDirectory(), fileRef.getName()),
            error -> System.err.println(error.toString()),
            () -> System.out.println("Completed listing the directories and files.")
        );
        // END: com.azure.storage.file.share.ShareDirectoryAsyncClient.listFilesAndDirectories#string-integer
    }

    /**
     * Generates a code sample for using {@link ShareDirectoryAsyncClient#deleteFile(String)} ()}
     */
    public void deleteFileAsync() {
        ShareDirectoryAsyncClient shareDirectoryAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareDirectoryAsyncClient.deleteFile#string
        shareDirectoryAsyncClient.deleteFile("myfile").subscribe(
            response -> { },
            error -> System.err.println(error.toString()),
            () -> System.out.println("Completed deleting the file.")
        );
        // END: com.azure.storage.file.share.ShareDirectoryAsyncClient.deleteFile#string
    }

    /**
     * Generates a code sample for using {@link ShareDirectoryAsyncClient#deleteFileWithResponse(String)}
     */
    public void deleteFileWithResponse() {
        ShareDirectoryAsyncClient shareDirectoryAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareDirectoryAsyncClient.deleteFileWithResponse#string
        shareDirectoryAsyncClient.deleteFileWithResponse("myfile").subscribe(
            response ->  System.out.printf("Delete file completed with status code %d", response.getStatusCode()),
            error -> System.err.println(error.toString()),
            () -> System.out.println("Completed deleting the file.")
        );
        // END: com.azure.storage.file.share.ShareDirectoryAsyncClient.deleteFileWithResponse#string
    }

    /**
     * Generates a code sample for using {@link ShareDirectoryAsyncClient#deleteSubdirectory(String)} ()}
     */
    public void deleteSubdirectoryAsync() {
        ShareDirectoryAsyncClient shareDirectoryAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareDirectoryAsyncClient.deleteSubdirectory#string
        shareDirectoryAsyncClient.deleteSubdirectory("mysubdirectory").subscribe(
            response -> { },
            error -> System.err.println(error.toString()),
            () -> System.out.println("Completed deleting the subdirectory.")
        );
        // END: com.azure.storage.file.share.ShareDirectoryAsyncClient.deleteSubdirectory#string
    }

    /**
     * Generates a code sample for using {@link ShareDirectoryAsyncClient#deleteSubdirectoryWithResponse(String)} ()}
     */
    public void deleteSubdirectoryWithResponse() {
        ShareDirectoryAsyncClient shareDirectoryAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareDirectoryAsyncClient.deleteSubdirectoryWithResponse#string
        shareDirectoryAsyncClient.deleteSubdirectoryWithResponse("mysubdirectory").subscribe(
            response ->  System.out.printf("Delete subdirectory completed with status code %d", response.getStatusCode()),
            error -> System.err.println(error.toString()),
            () -> System.out.println("Completed deleting the subdirectory.")
        );
        // END: com.azure.storage.file.share.ShareDirectoryAsyncClient.deleteSubdirectoryWithResponse#string
    }

    /**
     * Generates a code sample for using {@link ShareDirectoryAsyncClient#delete()}
     */
    public void deleteDirectoryAsync() {
        ShareDirectoryAsyncClient shareDirectoryAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareDirectoryAsyncClient.delete
        shareDirectoryAsyncClient.delete().subscribe(
            response -> { },
            error -> System.err.println(error.toString()),
            () -> System.out.println("Completed deleting the file.")
        );
        // END: com.azure.storage.file.share.ShareDirectoryAsyncClient.delete
    }

    /**
     * Generates a code sample for using {@link ShareDirectoryAsyncClient#deleteWithResponse()}
     */
    public void deleteWithResponse() {
        ShareDirectoryAsyncClient shareDirectoryAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareDirectoryAsyncClient.deleteWithResponse
        shareDirectoryAsyncClient.deleteWithResponse().subscribe(
            response ->  System.out.printf("Delete completed with status code %d", response.getStatusCode()),
            error -> System.err.println(error.toString())
        );
        // END: com.azure.storage.file.share.ShareDirectoryAsyncClient.deleteWithResponse
    }

    /**
     * Generates a code sample for using {@link ShareDirectoryAsyncClient#getProperties()}
     */
    public void getPropertiesAsync() {
        ShareDirectoryAsyncClient shareDirectoryAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareDirectoryAsyncClient.getProperties
        shareDirectoryAsyncClient.getProperties().subscribe(properties -> {
            System.out.printf("Directory latest modified date is %s.", properties.getLastModified());
        });
        // END: com.azure.storage.file.share.ShareDirectoryAsyncClient.getProperties
    }

    /**
     * Generates a code sample for using {@link ShareDirectoryAsyncClient#getPropertiesWithResponse()}
     */
    public void getPropertiesWithResponse() {
        ShareDirectoryAsyncClient shareDirectoryAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareDirectoryAsyncClient.getPropertiesWithResponse
        shareDirectoryAsyncClient.getPropertiesWithResponse().subscribe(properties -> {
            System.out.printf("Directory latest modified date is %s:", properties.getValue().getLastModified());
        });
        // END: com.azure.storage.file.share.ShareDirectoryAsyncClient.getPropertiesWithResponse
    }

    /**
     * Generates a code sample for using {@link ShareDirectoryAsyncClient#setProperties(FileSmbProperties, String)}
     */
    public void setPropertiesAsync() {
        ShareDirectoryAsyncClient shareDirectoryAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareDirectoryAsyncClient.setProperties#FileSmbProperties-String
        FileSmbProperties smbProperties = new FileSmbProperties();
        String filePermission = "filePermission";
        shareDirectoryAsyncClient.setProperties(smbProperties, filePermission).subscribe(properties -> {
            System.out.printf("Directory latest modified date is %s:", properties.getLastModified());
        });
        // END: com.azure.storage.file.share.ShareDirectoryAsyncClient.setProperties#FileSmbProperties-String
    }

    /**
     * Generates a code sample for using {@link ShareDirectoryAsyncClient#setPropertiesWithResponse(FileSmbProperties, String)}
     */
    public void setPropertiesWithResponse() {
        ShareDirectoryAsyncClient shareDirectoryAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareDirectoryAsyncClient.setPropertiesWithResponse#FileSmbProperties-String
        FileSmbProperties smbProperties = new FileSmbProperties();
        String filePermission = "filePermission";
        shareDirectoryAsyncClient.setPropertiesWithResponse(smbProperties, filePermission).subscribe(properties -> {
            System.out.printf("Directory latest modified date is %s:", properties.getValue().getLastModified());
        });
        // END: com.azure.storage.file.share.ShareDirectoryAsyncClient.setPropertiesWithResponse#FileSmbProperties-String
    }

    /**
     * Generates a code sample for using {@link ShareDirectoryAsyncClient#setMetadata(Map)}
     */
    public void setMetadataAsync() {
        ShareDirectoryAsyncClient shareDirectoryAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareDirectoryAsyncClient.setMetadata#map
        shareDirectoryAsyncClient.setMetadata(Collections.singletonMap("directory", "updatedMetadata"))
            .subscribe(response -> System.out.println("Setting the directory metadata completed."));
        // END: com.azure.storage.file.share.ShareDirectoryAsyncClient.setMetadata#map
    }

    /**
     * Generates a code sample for using {@link ShareDirectoryAsyncClient#setMetadata(Map)} to clear the metadata.
     */
    public void setMetadataClear() {
        ShareDirectoryAsyncClient shareDirectoryAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareDirectoryAsyncClient.setMetadata#map.clearMetadata
        shareDirectoryAsyncClient.setMetadata(null)
            .doOnSuccess(response ->  System.out.println("Clearing the directory metadata completed"));
        // END: com.azure.storage.file.share.ShareDirectoryAsyncClient.setMetadata#map.clearMetadata
    }

    /**
     * Generates a code sample for using {@link ShareDirectoryAsyncClient#setMetadataWithResponse(Map)} to clear the metadata.
     */
    public void setMetadataWithResponseClear() {
        ShareDirectoryAsyncClient shareDirectoryAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareDirectoryAsyncClient.setMetadataWithResponse#map.clearMetadata
        shareDirectoryAsyncClient.setMetadataWithResponse(null).subscribe(
            response ->  System.out.printf("Clearing the directory metadata completed with status code %d",
                response.getStatusCode()));
        // END: com.azure.storage.file.share.ShareDirectoryAsyncClient.setMetadataWithResponse#map.clearMetadata
    }

    /**
     * Generates a code sample for using {@link ShareDirectoryAsyncClient#setMetadataWithResponse(Map)}
     */
    public void setMetadataWithResponse() {
        ShareDirectoryAsyncClient shareDirectoryAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareDirectoryAsyncClient.setMetadataWithResponse#map
        shareDirectoryAsyncClient.setMetadataWithResponse(Collections.singletonMap("directory", "updatedMetadata"))
            .subscribe(response -> System.out.println("Setting the directory metadata completed with status code:"
                + response.getStatusCode()));
        // END: com.azure.storage.file.share.ShareDirectoryAsyncClient.setMetadataWithResponse#map
    }

    /**
     * Generates a code sample for using {@link ShareDirectoryAsyncClient#listHandles(Integer, boolean)}
     */
    public void listHandlesAsync() {
        ShareDirectoryAsyncClient shareDirectoryAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareDirectoryAsyncClient.listHandles#integer-boolean
        shareDirectoryAsyncClient.listHandles(10, true)
            .subscribe(handleItem -> System.out.printf("Get handles completed with handle id %s",
                handleItem.getHandleId()));
        // END: com.azure.storage.file.share.ShareDirectoryAsyncClient.listHandles#integer-boolean
    }

    /**
     * Code snippet for {@link ShareDirectoryAsyncClient#forceCloseHandle(String)}.
     */
    public void forceCloseHandle() {
        ShareDirectoryAsyncClient shareDirectoryAsyncClient = createAsyncClientWithConnectionString();
        // BEGIN: com.azure.storage.file.share.ShareDirectoryAsyncClient.forceCloseHandle#String
        shareDirectoryAsyncClient.listHandles(null, true).subscribe(handleItem ->
            shareDirectoryAsyncClient.forceCloseHandle(handleItem.getHandleId()).subscribe(ignored ->
                System.out.printf("Closed handle %s on resource %s%n",
                    handleItem.getHandleId(), handleItem.getPath())));
        // END: com.azure.storage.file.share.ShareDirectoryAsyncClient.forceCloseHandle#String
    }

    /**
     * Code snippet for {@link ShareDirectoryAsyncClient#forceCloseHandleWithResponse(String)}.
     */
    public void forceCloseHandleWithResponse() {
        ShareDirectoryAsyncClient shareDirectoryAsyncClient = createAsyncClientWithConnectionString();
        // BEGIN:com.azure.storage.file.share.ShareDirectoryAsyncClient.forceCloseHandleWithResponse#String
        shareDirectoryAsyncClient.listHandles(null, true).subscribe(handleItem ->
            shareDirectoryAsyncClient.forceCloseHandleWithResponse(handleItem.getHandleId()).subscribe(response ->
                System.out.printf("Closing handle %s on resource %s completed with status code %d%n",
                    handleItem.getHandleId(), handleItem.getPath(), response.getStatusCode())));
        // END: com.azure.storage.file.share.ShareDirectoryAsyncClient.forceCloseHandleWithResponse#String
    }

    /**
     * Code snippet for {@link ShareDirectoryAsyncClient#forceCloseAllHandles(boolean)}.
     */
    public void forceCloseAllHandles() {
        ShareDirectoryAsyncClient shareDirectoryAsyncClient = createAsyncClientWithConnectionString();
        // BEGIN: com.azure.storage.file.share.ShareDirectoryAsyncClient.forceCloseAllHandles#boolean
        shareDirectoryAsyncClient.forceCloseAllHandles(true).subscribe(numberOfHandlesClosed ->
            System.out.printf("Closed %d open handles on the directory%n", numberOfHandlesClosed));
        // END: com.azure.storage.file.share.ShareDirectoryAsyncClient.forceCloseAllHandles#boolean
    }

    /**
     * Generates a code sample for using {@link ShareDirectoryAsyncClient#getShareSnapshotId()}
     */
    public void getShareSnapshotIdAsync() {
        // BEGIN: com.azure.storage.file.share.ShareDirectoryAsyncClient.getShareSnapshotId
        OffsetDateTime currentTime = OffsetDateTime.of(LocalDateTime.now(), ZoneOffset.UTC);
        ShareDirectoryAsyncClient shareDirectoryAsyncClient = new ShareFileClientBuilder()
            .endpoint("https://${accountName}.file.core.windows.net")
            .sasToken("${SASToken}")
            .shareName("myshare")
            .resourcePath("mydirectory")
            .snapshot(currentTime.toString())
            .buildDirectoryAsyncClient();

        System.out.printf("Snapshot ID: %s%n", shareDirectoryAsyncClient.getShareSnapshotId());
        // END: com.azure.storage.file.share.ShareDirectoryAsyncClient.getShareSnapshotId
    }

    /**
     * Generates a code sample for using {@link ShareDirectoryAsyncClient#getShareName()}
     */
    public void getShareNameAsync() {
        ShareDirectoryAsyncClient shareDirectoryAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareDirectoryAsyncClient.getShareName
        String shareName = shareDirectoryAsyncClient.getShareName();
        System.out.println("The share name of the directory is " + shareName);
        // END: com.azure.storage.file.share.ShareDirectoryAsyncClient.getShareName
    }

    /**
     * Generates a code sample for using {@link ShareDirectoryAsyncClient#getDirectoryPath()}
     */
    public void getDirectoryNameAsync() {
        ShareDirectoryAsyncClient shareDirectoryAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareDirectoryAsyncClient.getDirectoryPath
        String directoryPath = shareDirectoryAsyncClient.getDirectoryPath();
        System.out.println("The name of the directory is " + directoryPath);
        // END: com.azure.storage.file.share.ShareDirectoryAsyncClient.getDirectoryPath
    }

    /**
     * Code snippet for {@link ShareDirectoryAsyncClient#generateSas(ShareServiceSasSignatureValues)}
     */
    public void generateSas() {
        ShareDirectoryAsyncClient shareDirectoryAsyncClient = createAsyncClientWithCredential();
        // BEGIN: com.azure.storage.file.share.ShareDirectoryAsyncClient.generateSas#ShareServiceSasSignatureValues
        OffsetDateTime expiryTime = OffsetDateTime.now().plusDays(1);
        ShareFileSasPermission permission = new ShareFileSasPermission().setReadPermission(true);

        ShareServiceSasSignatureValues values = new ShareServiceSasSignatureValues(expiryTime, permission)
            .setStartTime(OffsetDateTime.now());

        shareDirectoryAsyncClient.generateSas(values); // Client must be authenticated via StorageSharedKeyCredential
        // END: com.azure.storage.file.share.ShareDirectoryAsyncClient.generateSas#ShareServiceSasSignatureValues
    }
}
