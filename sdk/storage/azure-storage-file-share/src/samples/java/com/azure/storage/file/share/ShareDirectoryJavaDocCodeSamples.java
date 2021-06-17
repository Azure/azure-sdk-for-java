// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file.share;

import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.file.share.models.CloseHandlesInfo;
import com.azure.storage.file.share.models.ShareDirectoryInfo;
import com.azure.storage.file.share.models.ShareDirectoryProperties;
import com.azure.storage.file.share.models.ShareDirectorySetMetadataInfo;
import com.azure.storage.file.share.models.ShareFileHttpHeaders;
import com.azure.storage.file.share.models.HandleItem;
import com.azure.storage.file.share.models.NtfsFileAttributes;
import com.azure.storage.file.share.models.ShareRequestConditions;
import com.azure.storage.file.share.options.ShareListFilesAndDirectoriesOptions;
import com.azure.storage.file.share.sas.ShareFileSasPermission;
import com.azure.storage.file.share.sas.ShareServiceSasSignatureValues;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;

/**
 * Contains code snippets when generating javadocs through doclets for {@link ShareDirectoryClient}.
 */
public class ShareDirectoryJavaDocCodeSamples {

    private String key1 = "key1";
    private String value1 = "val1";

    private String leaseId = "leaseId";
    ShareDirectoryClient client = createClientWithSASToken();
    private Duration timeout = Duration.ofSeconds(30);

    /**
     * Generates code sample for {@link ShareDirectoryClient} instantiation.
     */
    public void initialization() {
        // BEGIN: com.azure.storage.file.share.ShareDirectoryClient.instantiation
        ShareDirectoryClient client = new ShareFileClientBuilder()
            .connectionString("${connectionString}")
            .endpoint("${endpoint}")
            .buildDirectoryClient();
        // END: com.azure.storage.file.share.ShareDirectoryClient.instantiation
    }

    /**
     * Generates code sample for creating a {@link ShareDirectoryClient} with SAS token
     *
     * @return An instance of {@link ShareDirectoryClient}
     */
    public ShareDirectoryClient createClientWithSASToken() {
        // BEGIN: com.azure.storage.file.share.ShareDirectoryClient.instantiation.sastoken
        ShareDirectoryClient shareDirectoryClient = new ShareFileClientBuilder()
            .endpoint("https://${accountName}.file.core.windows.net?${SASToken}")
            .shareName("myshare")
            .resourcePath("mydirectory")
            .buildDirectoryClient();
        // END: com.azure.storage.file.share.ShareDirectoryClient.instantiation.sastoken
        return shareDirectoryClient;
    }

    /**
     * Generates code sample for creating a {@link ShareDirectoryClient} with SAS token
     *
     * @return An instance of {@link ShareDirectoryClient}
     */
    public ShareDirectoryClient createClientWithCredential() {

        // BEGIN: com.azure.storage.file.share.ShareDirectoryClient.instantiation.credential
        ShareDirectoryClient shareDirectoryClient = new ShareFileClientBuilder()
            .endpoint("https://${accountName}.file.core.windows.net")
            .sasToken("${SASTokenQueryParams}")
            .shareName("myshare")
            .resourcePath("mydirectory")
            .buildDirectoryClient();
        // END: com.azure.storage.file.share.ShareDirectoryClient.instantiation.credential
        return shareDirectoryClient;
    }

    /**
     * Generates code sample for creating a {@link ShareDirectoryClient} with {@code connectionString} which turns into
     * {@link StorageSharedKeyCredential}
     *
     * @return An instance of {@link ShareDirectoryClient}
     */
    public ShareDirectoryClient createClientWithConnectionString() {
        // BEGIN: com.azure.storage.file.share.ShareDirectoryClient.instantiation.connectionstring
        String connectionString = "DefaultEndpointsProtocol=https;AccountName={name};AccountKey={key}"
            + ";EndpointSuffix={core.windows.net}";
        ShareDirectoryClient shareDirectoryClient = new ShareFileClientBuilder()
            .connectionString(connectionString)
            .shareName("myshare")
            .resourcePath("mydirectory")
            .buildDirectoryClient();
        // END: com.azure.storage.file.share.ShareDirectoryClient.instantiation.connectionstring
        return shareDirectoryClient;
    }

    /**
     * Code snippets for {@link ShareDirectoryClient#exists()} and {@link ShareDirectoryClient#existsWithResponse(
     * Duration, Context)}
     */
    public void exists() {
        // BEGIN: com.azure.storage.file.share.ShareDirectoryClient.exists
        System.out.printf("Exists? %b%n", client.exists());
        // END: com.azure.storage.file.share.ShareDirectoryClient.exists

        // BEGIN: com.azure.storage.file.share.ShareDirectoryClient.existsWithResponse#Duration-Context
        Context context = new Context("Key", "Value");
        System.out.printf("Exists? %b%n", client.existsWithResponse(timeout, context).getValue());
        // END: com.azure.storage.file.share.ShareDirectoryClient.existsWithResponse#Duration-Context
    }

    /**
     * Generates a code sample for using {@link ShareDirectoryClient#create()}
     */
    public void createDirectory() {
        ShareDirectoryClient shareDirectoryClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareDirectoryClient.createDirectory
        shareDirectoryClient.create();
        System.out.println("Completed creating the directory. ");
        // END: com.azure.storage.file.share.ShareDirectoryClient.createDirectory
    }

    /**
     * Generates a code sample for using {@link ShareDirectoryClient#createWithResponse(FileSmbProperties, String, Map,
     * Duration, Context)}
     */
    public void createWithResponse() {
        ShareDirectoryClient shareDirectoryClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareDirectoryClient.createWithResponse#FileSmbProperties-String-Map-Duration-Context
        FileSmbProperties smbProperties = new FileSmbProperties();
        String filePermission = "filePermission";
        Response<ShareDirectoryInfo> response = shareDirectoryClient.createWithResponse(smbProperties, filePermission,
            Collections.singletonMap("directory", "metadata"), Duration.ofSeconds(1), new Context(key1, value1));
        System.out.println("Completed creating the directory with status code: " + response.getStatusCode());
        // END: com.azure.storage.file.share.ShareDirectoryClient.createWithResponse#FileSmbProperties-String-Map-Duration-Context
    }

    /**
     * Generates a code sample for using {@link ShareDirectoryClient#createSubdirectory(String)}
     */
    public void createSubdirectory() {
        ShareDirectoryClient shareDirectoryClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareDirectoryClient.createSubdirectory#string
        shareDirectoryClient.createSubdirectory("subdir");
        System.out.println("Completed creating the subdirectory.");
        // END: com.azure.storage.file.share.ShareDirectoryClient.createSubdirectory#string
    }

    /**
     * Generates a code sample for using {@link ShareDirectoryClient#createSubdirectoryWithResponse(String,
     * FileSmbProperties, String, Map, Duration, Context)}
     */
    public void createSubdirectoryMaxOverload() {
        ShareDirectoryClient shareDirectoryClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareDirectoryClient.createSubdirectoryWithResponse#String-FileSmbProperties-String-Map-Duration-Context
        FileSmbProperties smbProperties = new FileSmbProperties();
        String filePermission = "filePermission";
        Response<ShareDirectoryClient> response = shareDirectoryClient.createSubdirectoryWithResponse("subdir",
            smbProperties, filePermission, Collections.singletonMap("directory", "metadata"),
            Duration.ofSeconds(1), new Context(key1, value1));
        System.out.printf("Creating the sub directory completed with status code %d", response.getStatusCode());
        // END: com.azure.storage.file.share.ShareDirectoryClient.createSubdirectoryWithResponse#String-FileSmbProperties-String-Map-Duration-Context
    }

    /**
     * Generates a code sample for using {@link ShareDirectoryClient#createFile(String, long)}
     */
    public void createFile() {
        ShareDirectoryClient shareDirectoryClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareDirectoryClient.createFile#string-long
        ShareFileClient response = shareDirectoryClient.createFile("myfile", 1024);
        System.out.println("Completed creating the file: " + response);
        // END: com.azure.storage.file.share.ShareDirectoryClient.createFile#string-long
    }

    /**
     * Generates a code sample for using {@link ShareDirectoryClient#createFileWithResponse(String, long, ShareFileHttpHeaders,
     * FileSmbProperties, String, Map, Duration, Context)}
     */
    public void createFileMaxOverload() {
        ShareDirectoryClient shareDirectoryClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareDirectoryClient.createFile#String-long-ShareFileHttpHeaders-FileSmbProperties-String-Map-duration-context
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
        Response<ShareFileClient> response = shareDirectoryClient.createFileWithResponse("myFile", 1024,
            httpHeaders, smbProperties, filePermission, Collections.singletonMap("directory", "metadata"),
            Duration.ofSeconds(1), new Context(key1, value1));
        System.out.println("Completed creating the file with status code: " + response.getStatusCode());
        // END: com.azure.storage.file.share.ShareDirectoryClient.createFile#String-long-ShareFileHttpHeaders-FileSmbProperties-String-Map-duration-context
    }

    /**
     * Generates a code sample for using {@link ShareDirectoryClient#createFileWithResponse(String, long, ShareFileHttpHeaders,
     * FileSmbProperties, String, Map, ShareRequestConditions, Duration, Context)}
     */
    public void createFileWithLease() {
        ShareDirectoryClient shareDirectoryClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareDirectoryClient.createFile#String-long-ShareFileHttpHeaders-FileSmbProperties-String-Map-ShareRequestConditions-duration-context
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

        ShareRequestConditions requestConditions = new ShareRequestConditions().setLeaseId(leaseId);

        Response<ShareFileClient> response = shareDirectoryClient.createFileWithResponse("myFile", 1024,
            httpHeaders, smbProperties, filePermission, Collections.singletonMap("directory", "metadata"),
            requestConditions, Duration.ofSeconds(1), new Context(key1, value1));
        System.out.println("Completed creating the file with status code: " + response.getStatusCode());
        // END: com.azure.storage.file.share.ShareDirectoryClient.createFile#String-long-ShareFileHttpHeaders-FileSmbProperties-String-Map-ShareRequestConditions-duration-context
    }

    /**
     * Generates a code sample for using {@link ShareDirectoryClient#listFilesAndDirectories()}
     */
    public void listDirectoriesAndFiles() {
        ShareDirectoryClient shareDirectoryClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareDirectoryClient.listFilesAndDirectories
        shareDirectoryClient.listFilesAndDirectories().forEach(
            fileRef -> System.out.printf("Is the resource a directory? %b. The resource name is: %s.",
                fileRef.isDirectory(), fileRef.getName())
        );
        // END: com.azure.storage.file.share.ShareDirectoryClient.listFilesAndDirectories
    }

    /**
     * Generates a code sample for using {@link ShareDirectoryClient#listFilesAndDirectories(String, Integer, Duration,
     * Context)}
     */
    public void listDirectoriesAndFilesMaxOverload() {
        ShareDirectoryClient shareDirectoryClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareDirectoryClient.listFilesAndDirectories#string-integer-duration-context
        shareDirectoryClient.listFilesAndDirectories("subdir", 10, Duration.ofSeconds(1),
            new Context(key1, value1)).forEach(
                fileRef -> System.out.printf("Is the resource a directory? %b. The resource name is: %s.",
                    fileRef.isDirectory(), fileRef.getName())
        );
        // END: com.azure.storage.file.share.ShareDirectoryClient.listFilesAndDirectories#string-integer-duration-context
    }

    /**
     * Generates a code sample for using {@link ShareDirectoryClient#listFilesAndDirectories(
     * ShareListFilesAndDirectoriesOptions, Duration, Context)}
     */
    public void listDirectoriesAndFilesOptionsBagOverload() {
        ShareDirectoryClient shareDirectoryClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareDirectoryClient.listFilesAndDirectories#ShareListFilesAndDirectoriesOptions-duration-context
        shareDirectoryClient.listFilesAndDirectories(new ShareListFilesAndDirectoriesOptions()
                .setPrefix("subdir").setMaxResultsPerPage(10), Duration.ofSeconds(1), new Context(key1, value1))
            .forEach(fileRef -> System.out.printf("Is the resource a directory? %b. The resource name is: %s.",
                fileRef.isDirectory(), fileRef.getName()));
        // END: com.azure.storage.file.share.ShareDirectoryClient.listFilesAndDirectories#ShareListFilesAndDirectoriesOptions-duration-context
    }

    /**
     * Generates a code sample for using {@link ShareDirectoryClient#deleteFile(String)} ()}
     */
    public void deleteFile() {
        ShareDirectoryClient shareDirectoryClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareDirectoryClient.deleteFile#string
        shareDirectoryClient.deleteFile("myfile");
        System.out.println("Completed deleting the file.");
        // END: com.azure.storage.file.share.ShareDirectoryClient.deleteFile#string
    }

    /**
     * Generates a code sample for using {@link ShareDirectoryClient#deleteFileWithResponse(String, Duration, Context)}
     */
    public void deleteFileWithResponse() {
        ShareDirectoryClient shareDirectoryClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareDirectoryClient.deleteFileWithResponse#string-duration-context
        Response<Void> response = shareDirectoryClient.deleteFileWithResponse("myfile",
            Duration.ofSeconds(1), new Context(key1, value1));
        System.out.println("Completed deleting the file with status code: " + response.getStatusCode());
        // END: com.azure.storage.file.share.ShareDirectoryClient.deleteFileWithResponse#string-duration-context
    }

    /**
     * Generates a code sample for using {@link ShareDirectoryClient#deleteFileWithResponse(String,ShareRequestConditions, Duration, Context)}
     */
    public void deleteFileWithLease() {
        ShareDirectoryClient shareDirectoryClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareDirectoryClient.deleteFileWithResponse#string-ShareRequestConditions-duration-context
        ShareRequestConditions requestConditions = new ShareRequestConditions().setLeaseId(leaseId);
        Response<Void> response = shareDirectoryClient.deleteFileWithResponse("myfile", requestConditions,
            Duration.ofSeconds(1), new Context(key1, value1));
        System.out.println("Completed deleting the file with status code: " + response.getStatusCode());
        // END: com.azure.storage.file.share.ShareDirectoryClient.deleteFileWithResponse#string-ShareRequestConditions-duration-context
    }

    /**
     * Generates a code sample for using {@link ShareDirectoryClient#deleteSubdirectory(String)}
     */
    public void deleteSubdirectory() {
        ShareDirectoryClient shareDirectoryClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareDirectoryClient.deleteSubdirectory#string
        shareDirectoryClient.deleteSubdirectory("mysubdirectory");
        System.out.println("Complete deleting the subdirectory.");
        // END: com.azure.storage.file.share.ShareDirectoryClient.deleteSubdirectory#string
    }

    /**
     * Generates a code sample for using {@link ShareDirectoryClient#deleteSubdirectoryWithResponse(String, Duration,
     * Context)}
     */
    public void deleteSubdirectoryWithResponse() {
        ShareDirectoryClient shareDirectoryClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareDirectoryClient.deleteSubdirectoryWithResponse#string-duration-context
        Response<Void> response = shareDirectoryClient.deleteSubdirectoryWithResponse("mysubdirectory",
            Duration.ofSeconds(1), new Context(key1, value1));
        System.out.println("Completed deleting the subdirectory with status code: " + response.getStatusCode());
        // END: com.azure.storage.file.share.ShareDirectoryClient.deleteSubdirectoryWithResponse#string-duration-context
    }

    /**
     * Generates a code sample for using {@link ShareDirectoryClient#delete()}
     */
    public void deleteDirectory() {
        ShareDirectoryClient shareDirectoryClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareDirectoryClient.delete
        shareDirectoryClient.delete();
        System.out.println("Completed deleting the file.");
        // END: com.azure.storage.file.share.ShareDirectoryClient.delete
    }

    /**
     * Generates a code sample for using {@link ShareDirectoryClient#deleteWithResponse(Duration, Context)}
     */
    public void deleteWithResponse() {
        ShareDirectoryClient shareDirectoryClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareDirectoryClient.deleteWithResponse#duration-context
        Response<Void> response = shareDirectoryClient.deleteWithResponse(Duration.ofSeconds(1), new Context(key1, value1));
        System.out.println("Completed deleting the file with status code: " + response.getStatusCode());
        // END: com.azure.storage.file.share.ShareDirectoryClient.deleteWithResponse#duration-context
    }

    /**
     * Generates a code sample for using {@link ShareDirectoryClient#getProperties()}
     */
    public void getProperties() {
        ShareDirectoryClient shareDirectoryClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareDirectoryClient.getProperties
        ShareDirectoryProperties response = shareDirectoryClient.getProperties();
        System.out.printf("Directory latest modified date is %s.", response.getLastModified());
        // END: com.azure.storage.file.share.ShareDirectoryClient.getProperties
    }

    /**
     * Generates a code sample for using {@link ShareDirectoryClient#getPropertiesWithResponse(Duration, Context)}
     */
    public void getPropertiesWithResponse() {
        ShareDirectoryClient shareDirectoryClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareDirectoryClient.getPropertiesWithResponse#duration-Context
        Response<ShareDirectoryProperties> response = shareDirectoryClient.getPropertiesWithResponse(
            Duration.ofSeconds(1), new Context(key1, value1));
        System.out.printf("Directory latest modified date is %s.", response.getValue().getLastModified());
        // END: com.azure.storage.file.share.ShareDirectoryClient.getPropertiesWithResponse#duration-Context
    }

    /**
     * Generates a code sample for using {@link ShareDirectoryClient#setProperties(FileSmbProperties, String)}
     */
    public void setProperties() {
        ShareDirectoryClient shareDirectoryClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareDirectoryClient.setProperties#FileSmbProperties-String
        FileSmbProperties smbProperties = new FileSmbProperties();
        String filePermission = "filePermission";
        ShareDirectoryInfo response = shareDirectoryClient.setProperties(smbProperties, filePermission);
        System.out.printf("Directory latest modified date is %s.", response.getLastModified());
        // END: com.azure.storage.file.share.ShareDirectoryClient.setProperties#FileSmbProperties-String
    }

    /**
     * Generates a code sample for using {@link ShareDirectoryClient#setPropertiesWithResponse(FileSmbProperties, String,
     * Duration, Context)}
     */
    public void setPropertiesWithResponse() {
        ShareDirectoryClient shareDirectoryClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareDirectoryClient.setPropertiesWithResponse#FileSmbProperties-String-Duration-Context
        FileSmbProperties smbProperties = new FileSmbProperties();
        String filePermission = "filePermission";
        Response<ShareDirectoryInfo> response = shareDirectoryClient.setPropertiesWithResponse(smbProperties, filePermission,
            Duration.ofSeconds(1), new Context(key1, value1));
        System.out.printf("Directory latest modified date is %s.", response.getValue().getLastModified());
        // END: com.azure.storage.file.share.ShareDirectoryClient.setPropertiesWithResponse#FileSmbProperties-String-Duration-Context
    }

    /**
     * Generates a code sample for using {@link ShareDirectoryClient#setMetadata(Map)}
     */
    public void setMetadata() {
        ShareDirectoryClient shareDirectoryClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareDirectoryClient.setMetadata#map
        ShareDirectorySetMetadataInfo response =
            shareDirectoryClient.setMetadata(Collections.singletonMap("directory", "updatedMetadata"));
        System.out.printf("Setting the directory metadata completed with updated etag %s", response.getETag());
        // END: com.azure.storage.file.share.ShareDirectoryClient.setMetadata#map
    }

    /**
     * Generates a code sample for using {@link ShareDirectoryClient#setMetadataWithResponse(Map, Duration, Context)}
     */
    public void setMetadataWithResponse() {
        ShareDirectoryClient shareDirectoryClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareDirectoryClient.setMetadataWithResponse#map-duration-context
        Response<ShareDirectorySetMetadataInfo> response =
            shareDirectoryClient.setMetadataWithResponse(Collections.singletonMap("directory", "updatedMetadata"),
                Duration.ofSeconds(1), new Context(key1, value1));
        System.out.printf("Setting the directory metadata completed with updated etag %d", response.getStatusCode());
        // END: com.azure.storage.file.share.ShareDirectoryClient.setMetadataWithResponse#map-duration-context
    }

    /**
     * Generates a code sample for using {@link ShareDirectoryClient#setMetadata(Map)} to clear the metadata.
     */
    public void clearSetMetadata() {
        ShareDirectoryClient shareDirectoryClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareDirectoryClient.setMetadata#map.clearMetadata
        ShareDirectorySetMetadataInfo response = shareDirectoryClient.setMetadata(null);
        System.out.printf("Cleared metadata.");
        // END: com.azure.storage.file.share.ShareDirectoryClient.setMetadata#map.clearMetadata
    }

    /**
     * Generates a code sample for using {@link ShareDirectoryClient#setMetadata(Map)} to clear the metadata.
     */
    public void clearMetadata() {
        ShareDirectoryClient shareDirectoryClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareDirectoryClient.setMetadataWithResponse#map-duration-context.clearMetadata
        Response<ShareDirectorySetMetadataInfo> response = shareDirectoryClient.setMetadataWithResponse(null,
            Duration.ofSeconds(1), new Context(key1, value1));
        System.out.printf("Directory latest modified date is %s.", response.getStatusCode());
        // END: com.azure.storage.file.share.ShareDirectoryClient.setMetadataWithResponse#map-duration-context.clearMetadata
    }

    /**
     * Generates a code sample for using {@link ShareDirectoryClient#listHandles(Integer, boolean, Duration, Context)}
     */
    public void listHandles() {
        ShareDirectoryClient shareDirectoryClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareDirectoryClient.listHandles#Integer-boolean-duration-context
        Iterable<HandleItem> result = shareDirectoryClient.listHandles(10, true, Duration.ofSeconds(1),
            new Context(key1, value1));
        System.out.printf("Get handles completed with handle id %s", result.iterator().next().getHandleId());
        // END: com.azure.storage.file.share.ShareDirectoryClient.listHandles#Integer-boolean-duration-context
    }

    /**
     * Code snippet for {@link ShareDirectoryClient#forceCloseHandle(String)}
     */
    public void forceCloseHandle() {
        ShareDirectoryClient shareDirectoryClient = createClientWithConnectionString();
        // BEGIN: com.azure.storage.file.share.ShareDirectoryClient.forceCloseHandle#String
        shareDirectoryClient.listHandles(null, true, Duration.ofSeconds(30), Context.NONE).forEach(handleItem -> {
            shareDirectoryClient.forceCloseHandle(handleItem.getHandleId());
            System.out.printf("Closed handle %s on resource %s%n", handleItem.getHandleId(), handleItem.getPath());
        });
        // END: com.azure.storage.file.share.ShareDirectoryClient.forceCloseHandle#String
    }

    /**
     * Code snippet for {@link ShareDirectoryClient#forceCloseHandleWithResponse(String, Duration, Context)}.
     */
    public void forceCloseHandleWithResponse() {
        ShareDirectoryClient shareDirectoryClient = createClientWithConnectionString();
        // BEGIN: com.azure.storage.file.share.ShareDirectoryClient.forceCloseHandleWithResponse#String-Duration-Context
        shareDirectoryClient.listHandles(null, true, Duration.ofSeconds(30), Context.NONE).forEach(handleItem -> {
            Response<CloseHandlesInfo> closeResponse = shareDirectoryClient.forceCloseHandleWithResponse(
                handleItem.getHandleId(), Duration.ofSeconds(30), Context.NONE);
            System.out.printf("Closing handle %s on resource %s completed with status code %d%n",
                handleItem.getHandleId(), handleItem.getPath(), closeResponse.getStatusCode());
        });
        // END: com.azure.storage.file.share.ShareDirectoryClient.forceCloseHandleWithResponse#String-Duration-Context
    }

    /**
     * Code snippet for {@link ShareDirectoryClient#forceCloseAllHandles(boolean, Duration, Context)}.
     */
    public void forceCloseAllHandles() {
        ShareDirectoryClient shareDirectoryClient = createClientWithConnectionString();
        // BEGIN: com.azure.storage.file.share.ShareDirectoryClient.forceCloseAllHandles#boolean-Duration-Context
        CloseHandlesInfo closeHandlesInfo = shareDirectoryClient.forceCloseAllHandles(true, Duration.ofSeconds(30),
            Context.NONE);
        System.out.printf("Closed %d open handles on the directory%n", closeHandlesInfo.getClosedHandles());
        System.out.printf("Failed to close %d open handles on the directory%n", closeHandlesInfo.getFailedHandles());
        // END: com.azure.storage.file.share.ShareDirectoryClient.forceCloseAllHandles#boolean-Duration-Context
    }

    /**
     * Generates a code sample for using {@link ShareDirectoryClient#getShareSnapshotId()}
     */
    public void getShareSnapshotId() {
        // BEGIN: com.azure.storage.file.share.ShareDirectoryClient.getShareSnapshotId
        OffsetDateTime currentTime = OffsetDateTime.of(LocalDateTime.now(), ZoneOffset.UTC);
        ShareDirectoryClient shareDirectoryClient = new ShareFileClientBuilder()
            .endpoint("https://${accountName}.file.core.windows.net")
            .sasToken("${SASToken}")
            .shareName("myshare")
            .resourcePath("mydirectory")
            .snapshot(currentTime.toString())
            .buildDirectoryClient();

        System.out.printf("Snapshot ID: %s%n", shareDirectoryClient.getShareSnapshotId());
        // END: com.azure.storage.file.share.ShareDirectoryClient.getShareSnapshotId
    }

    /**
     * Generates a code sample for using {@link ShareDirectoryClient#getShareName()}
     */
    public void getShareName() {
        ShareDirectoryClient directoryAsyncClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareDirectoryClient.getShareName
        String shareName = directoryAsyncClient.getShareName();
        System.out.println("The share name of the directory is " + shareName);
        // END: com.azure.storage.file.share.ShareDirectoryClient.getShareName
    }

    /**
     * Generates a code sample for using {@link ShareDirectoryClient#getDirectoryPath()}
     */
    public void getDirectoryPath() {
        ShareDirectoryClient shareDirectoryClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.share.ShareDirectoryClient.getDirectoryPath
        String directoryPath = shareDirectoryClient.getDirectoryPath();
        System.out.println("The name of the directory is " + directoryPath);
        // END: com.azure.storage.file.share.ShareDirectoryClient.getDirectoryPath
    }

    /**
     * Code snippet for {@link ShareDirectoryClient#generateSas(ShareServiceSasSignatureValues)}
     */
    public void generateSas() {
        ShareDirectoryClient shareDirectoryClient = createClientWithCredential();
        // BEGIN: com.azure.storage.file.share.ShareDirectoryClient.generateSas#ShareServiceSasSignatureValues
        OffsetDateTime expiryTime = OffsetDateTime.now().plusDays(1);
        ShareFileSasPermission permission = new ShareFileSasPermission().setReadPermission(true);

        ShareServiceSasSignatureValues values = new ShareServiceSasSignatureValues(expiryTime, permission)
            .setStartTime(OffsetDateTime.now());

        shareDirectoryClient.generateSas(values); // Client must be authenticated via StorageSharedKeyCredential
        // END: com.azure.storage.file.share.ShareDirectoryClient.generateSas#ShareServiceSasSignatureValues
    }

    /**
     * Code snippet for {@link ShareDirectoryClient#generateSas(ShareServiceSasSignatureValues, Context)}
     */
    public void generateSasWithContext() {
        ShareDirectoryClient shareDirectoryClient = createClientWithCredential();
        // BEGIN: com.azure.storage.file.share.ShareDirectoryClient.generateSas#ShareServiceSasSignatureValues-Context
        OffsetDateTime expiryTime = OffsetDateTime.now().plusDays(1);
        ShareFileSasPermission permission = new ShareFileSasPermission().setReadPermission(true);

        ShareServiceSasSignatureValues values = new ShareServiceSasSignatureValues(expiryTime, permission)
            .setStartTime(OffsetDateTime.now());

        // Client must be authenticated via StorageSharedKeyCredential
        shareDirectoryClient.generateSas(values, new Context("key", "value"));
        // END: com.azure.storage.file.share.ShareDirectoryClient.generateSas#ShareServiceSasSignatureValues-Context
    }
}
