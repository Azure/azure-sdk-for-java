// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file;

import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.VoidResponse;
import com.azure.core.util.Context;
import com.azure.storage.common.Utility;
import com.azure.storage.common.credentials.SASTokenCredential;
import com.azure.storage.common.credentials.SharedKeyCredential;
import com.azure.storage.file.models.DirectoryInfo;
import com.azure.storage.file.models.DirectoryProperties;
import com.azure.storage.file.models.DirectorySetMetadataInfo;
import com.azure.storage.file.models.FileHTTPHeaders;
import com.azure.storage.file.models.HandleItem;
import com.azure.storage.file.models.NtfsFileAttributes;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;

/**
 * Contains code snippets when generating javadocs through doclets for {@link DirectoryClient}.
 */
public class DirectoryJavaDocCodeSamples {

    private String key1 = "key1";
    private String value1 = "val1";

    /**
     * Generates code sample for {@link DirectoryClient} instantiation.
     */
    public void initialization() {
        // BEGIN: com.azure.storage.file.directoryClient.instantiation
        DirectoryClient client = new FileClientBuilder()
            .connectionString("${connectionString}")
            .endpoint("${endpoint}")
            .buildDirectoryClient();
        // END: com.azure.storage.file.directoryClient.instantiation
    }

    /**
     * Generates code sample for creating a {@link DirectoryClient} with {@link SASTokenCredential}
     *
     * @return An instance of {@link DirectoryClient}
     */
    public DirectoryClient createClientWithSASToken() {
        // BEGIN: com.azure.storage.file.directoryClient.instantiation.sastoken
        DirectoryClient directoryClient = new FileClientBuilder()
            .endpoint("https://${accountName}.file.core.windows.net?${SASToken}")
            .shareName("myshare")
            .resourcePath("mydirectory")
            .buildDirectoryClient();
        // END: com.azure.storage.file.directoryClient.instantiation.sastoken
        return directoryClient;
    }

    /**
     * Generates code sample for creating a {@link DirectoryClient} with {@link SASTokenCredential}
     *
     * @return An instance of {@link DirectoryClient}
     */
    public DirectoryClient createClientWithCredential() {

        // BEGIN: com.azure.storage.file.directoryClient.instantiation.credential
        DirectoryClient directoryClient = new FileClientBuilder()
            .endpoint("https://${accountName}.file.core.windows.net")
            .credential(SASTokenCredential.fromQueryParameters(Utility.parseQueryString("${SASTokenQueryParams}")))
            .shareName("myshare")
            .resourcePath("mydirectory")
            .buildDirectoryClient();
        // END: com.azure.storage.file.directoryClient.instantiation.credential
        return directoryClient;
    }

    /**
     * Generates code sample for creating a {@link DirectoryClient} with {@code connectionString} which turns into
     * {@link SharedKeyCredential}
     *
     * @return An instance of {@link DirectoryClient}
     */
    public DirectoryClient createClientWithConnectionString() {
        // BEGIN: com.azure.storage.file.directoryClient.instantiation.connectionstring
        String connectionString = "DefaultEndpointsProtocol=https;AccountName={name};AccountKey={key}"
            + ";EndpointSuffix={core.windows.net}";
        DirectoryClient directoryClient = new FileClientBuilder()
            .connectionString(connectionString)
            .shareName("myshare")
            .resourcePath("mydirectory")
            .buildDirectoryClient();
        // END: com.azure.storage.file.directoryClient.instantiation.connectionstring
        return directoryClient;
    }

    /**
     * Generates a code sample for using {@link DirectoryClient#create()}
     */
    public void createDirectory() {
        DirectoryClient directoryClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryClient.createDirectory
        directoryClient.create();
        System.out.println("Completed creating the directory. ");
        // END: com.azure.storage.file.directoryClient.createDirectory
    }

    /**
     * Generates a code sample for using {@link DirectoryClient#createWithResponse(FileSmbProperties, String, Map,
     * Duration, Context)}
     */
    public void createWithResponse() {
        DirectoryClient directoryClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryClient.createWithResponse#filesmbproperties-string-map-duration-context
        FileSmbProperties smbProperties = new FileSmbProperties();
        String filePermission = "filePermission";
        Response<DirectoryInfo> response = directoryClient.createWithResponse(smbProperties, filePermission,
            Collections.singletonMap("directory", "metadata"), Duration.ofSeconds(1), new Context(key1, value1));
        System.out.println("Completed creating the directory with status code: " + response.getStatusCode());
        // END: com.azure.storage.file.directoryClient.createWithResponse#filesmbproperties-string-map-duration-context
    }

    /**
     * Generates a code sample for using {@link DirectoryClient#createSubDirectory(String)}
     */
    public void createSubDirectory() {
        DirectoryClient directoryClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryClient.createSubDirectory#string
        directoryClient.createSubDirectory("subdir");
        System.out.println("Completed creating the subdirectory.");
        // END: com.azure.storage.file.directoryClient.createSubDirectory#string
    }

    /**
     * Generates a code sample for using {@link DirectoryClient#createSubDirectoryWithResponse(String,
     * FileSmbProperties, String, Map, Duration, Context)}
     */
    public void createSubDirectoryMaxOverload() {
        DirectoryClient directoryClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryClient.createSubDirectoryWithResponse#string-filesmbproperties-string-map-duration-context
        FileSmbProperties smbProperties = new FileSmbProperties();
        String filePermission = "filePermission";
        Response<DirectoryClient> response = directoryClient.createSubDirectoryWithResponse("subdir",
            smbProperties, filePermission, Collections.singletonMap("directory", "metadata"),
            Duration.ofSeconds(1), new Context(key1, value1));
        System.out.printf("Creating the sub directory completed with status code %d", response.getStatusCode());
        // END: com.azure.storage.file.directoryClient.createSubDirectoryWithResponse#string-filesmbproperties-string-map-duration-context
    }

    /**
     * Generates a code sample for using {@link DirectoryClient#createFile(String, long)}
     */
    public void createFile() {
        DirectoryClient directoryClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryClient.createFile#string-long
        FileClient response = directoryClient.createFile("myfile", 1024);
        System.out.println("Completed creating the file: " + response);
        // END: com.azure.storage.file.directoryClient.createFile#string-long
    }

    /**
     * Generates a code sample for using {@link DirectoryClient#createFileWithResponse(String, long, FileHTTPHeaders,
     * FileSmbProperties, String, Map, Duration, Context)}
     */
    public void createFileMaxOverload() {
        DirectoryClient directoryClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryClient.createFile#string-long-filehttpheaders-filesmbproperties-string-map-duration-context
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
        Response<FileClient> response = directoryClient.createFileWithResponse("myFile", 1024,
            httpHeaders, smbProperties, filePermission, Collections.singletonMap("directory", "metadata"),
            Duration.ofSeconds(1), new Context(key1, value1));
        System.out.println("Completed creating the file with status code: " + response.getStatusCode());
        // END: com.azure.storage.file.directoryClient.createFile#string-long-filehttpheaders-filesmbproperties-string-map-duration-context
    }

    /**
     * Generates a code sample for using {@link DirectoryClient#listFilesAndDirectories()}
     */
    public void listDirectoriesAndFiles() {
        DirectoryClient directoryClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryClient.listFilesAndDirectories
        directoryClient.listFilesAndDirectories().forEach(
            fileRef -> System.out.printf("Is the resource a directory? %b. The resource name is: %s.",
                fileRef.isDirectory(), fileRef.getName())
        );
        // END: com.azure.storage.file.directoryClient.listFilesAndDirectories
    }

    /**
     * Generates a code sample for using {@link DirectoryClient#listFilesAndDirectories(String, Integer, Duration,
     * Context)}
     */
    public void listDirectoriesAndFilesMaxOverload() {
        DirectoryClient directoryClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryClient.listFilesAndDirectories#string-integer-duration-context
        directoryClient.listFilesAndDirectories("subdir", 10, Duration.ofSeconds(1),
            new Context(key1, value1)).forEach(
                fileRef -> System.out.printf("Is the resource a directory? %b. The resource name is: %s.",
                    fileRef.isDirectory(), fileRef.getName())
        );
        // END: com.azure.storage.file.directoryClient.listFilesAndDirectories#string-integer-duration-context
    }

    /**
     * Generates a code sample for using {@link DirectoryClient#deleteFile(String)} ()}
     */
    public void deleteFile() {
        DirectoryClient directoryClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryClient.deleteFile#string
        directoryClient.deleteFile("myfile");
        System.out.println("Completed deleting the file.");
        // END: com.azure.storage.file.directoryClient.deleteFile#string
    }

    /**
     * Generates a code sample for using {@link DirectoryClient#deleteFileWithResponse(String, Duration, Context)}
     */
    public void deleteFileWithResponse() {
        DirectoryClient directoryClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.DirectoryClient.deleteFileWithResponse#string-duration-context
        VoidResponse response = directoryClient.deleteFileWithResponse("myfile",
            Duration.ofSeconds(1), new Context(key1, value1));
        System.out.println("Completed deleting the file with status code: " + response.getStatusCode());
        // END: com.azure.storage.file.DirectoryClient.deleteFileWithResponse#string-duration-context
    }

    /**
     * Generates a code sample for using {@link DirectoryClient#deleteSubDirectory(String)}
     */
    public void deleteSubDirectory() {
        DirectoryClient directoryClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryClient.deleteSubDirectory#string
        directoryClient.deleteSubDirectory("mysubdirectory");
        System.out.println("Complete deleting the subdirectory.");
        // END: com.azure.storage.file.directoryClient.deleteSubDirectory#string
    }

    /**
     * Generates a code sample for using {@link DirectoryClient#deleteSubDirectoryWithResponse(String, Duration,
     * Context)}
     */
    public void deleteSubDirectoryWithResponse() {
        DirectoryClient directoryClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryClient.deleteSubDirectoryWithResponse#string-duration-context
        VoidResponse response = directoryClient.deleteSubDirectoryWithResponse("mysubdirectory",
            Duration.ofSeconds(1), new Context(key1, value1));
        System.out.println("Completed deleting the subdirectory with status code: " + response.getStatusCode());
        // END: com.azure.storage.file.directoryClient.deleteSubDirectoryWithResponse#string-duration-context
    }

    /**
     * Generates a code sample for using {@link DirectoryClient#delete()}
     */
    public void deleteDirectory() {
        DirectoryClient directoryClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryClient.delete
        directoryClient.delete();
        System.out.println("Completed deleting the file.");
        // END: com.azure.storage.file.directoryClient.delete
    }

    /**
     * Generates a code sample for using {@link DirectoryClient#deleteWithResponse(Duration, Context)}
     */
    public void deleteWithResponse() {
        DirectoryClient directoryClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.DirectoryClient.deleteWithResponse#duration-context
        VoidResponse response = directoryClient.deleteWithResponse(Duration.ofSeconds(1), new Context(key1, value1));
        System.out.println("Completed deleting the file with status code: " + response.getStatusCode());
        // END: com.azure.storage.file.DirectoryClient.deleteWithResponse#duration-context
    }

    /**
     * Generates a code sample for using {@link DirectoryClient#getProperties()}
     */
    public void getProperties() {
        DirectoryClient directoryClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryClient.getProperties
        DirectoryProperties response = directoryClient.getProperties();
        System.out.printf("Directory latest modified date is %s.", response.getLastModified());
        // END: com.azure.storage.file.directoryClient.getProperties
    }

    /**
     * Generates a code sample for using {@link DirectoryClient#getPropertiesWithResponse(Duration, Context)}
     */
    public void getPropertiesWithResponse() {
        DirectoryClient directoryClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.DirectoryClient.getPropertiesWithResponse#duration-Context
        Response<DirectoryProperties> response = directoryClient.getPropertiesWithResponse(
            Duration.ofSeconds(1), new Context(key1, value1));
        System.out.printf("Directory latest modified date is %s.", response.getValue().getLastModified());
        // END: com.azure.storage.file.DirectoryClient.getPropertiesWithResponse#duration-Context
    }

    /**
     * Generates a code sample for using {@link DirectoryClient#setProperties(FileSmbProperties, String)}
     */
    public void setProperties() {
        DirectoryClient directoryClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryClient.setProperties#filesmbproperties-string
        FileSmbProperties smbProperties = new FileSmbProperties();
        String filePermission = "filePermission";
        DirectoryInfo response = directoryClient.setProperties(smbProperties, filePermission);
        System.out.printf("Directory latest modified date is %s.", response.getLastModified());
        // END: com.azure.storage.file.directoryClient.setProperties#filesmbproperties-string
    }

    /**
     * Generates a code sample for using {@link DirectoryClient#setPropertiesWithResponse(FileSmbProperties, String,
     * Duration, Context)}
     */
    public void setPropertiesWithResponse() {
        DirectoryClient directoryClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryClient.setPropertiesWithResponse#filesmbproperties-string-duration-Context
        FileSmbProperties smbProperties = new FileSmbProperties();
        String filePermission = "filePermission";
        Response<DirectoryInfo> response = directoryClient.setPropertiesWithResponse(smbProperties, filePermission,
            Duration.ofSeconds(1), new Context(key1, value1));
        System.out.printf("Directory latest modified date is %s.", response.getValue().getLastModified());
        // END: com.azure.storage.file.directoryClient.setPropertiesWithResponse#filesmbproperties-string-duration-Context
    }

    /**
     * Generates a code sample for using {@link DirectoryClient#setMetadata(Map)}
     */
    public void setMetadata() {
        DirectoryClient directoryClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryClient.setMetadata#map
        DirectorySetMetadataInfo response =
            directoryClient.setMetadata(Collections.singletonMap("directory", "updatedMetadata"));
        System.out.printf("Setting the directory metadata completed with updated etag %s", response.getETag());
        // END: com.azure.storage.file.directoryClient.setMetadata#map
    }

    /**
     * Generates a code sample for using {@link DirectoryClient#setMetadataWithResponse(Map, Duration, Context)}
     */
    public void setMetadataWithResponse() {
        DirectoryClient directoryClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryClient.setMetadataWithResponse#map-duration-context
        Response<DirectorySetMetadataInfo> response =
            directoryClient.setMetadataWithResponse(Collections.singletonMap("directory", "updatedMetadata"),
                Duration.ofSeconds(1), new Context(key1, value1));
        System.out.printf("Setting the directory metadata completed with updated etag %d", response.getStatusCode());
        // END: com.azure.storage.file.directoryClient.setMetadataWithResponse#map-duration-context
    }

    /**
     * Generates a code sample for using {@link DirectoryClient#setMetadata(Map)} to clear the metadata.
     */
    public void clearSetMetadata() {
        DirectoryClient directoryClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryClient.setMetadata#map.clearMetadata
        DirectorySetMetadataInfo response = directoryClient.setMetadata(null);
        System.out.printf("Cleared metadata.");
        // END: com.azure.storage.file.directoryClient.setMetadata#map.clearMetadata
    }

    /**
     * Generates a code sample for using {@link DirectoryClient#setMetadata(Map)} to clear the metadata.
     */
    public void clearMetadata() {
        DirectoryClient directoryClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.DirectoryClient.setMetadataWithResponse#map-duration-context.clearMetadata
        Response<DirectorySetMetadataInfo> response = directoryClient.setMetadataWithResponse(null,
            Duration.ofSeconds(1), new Context(key1, value1));
        System.out.printf("Directory latest modified date is %s.", response.getStatusCode());
        // END: com.azure.storage.file.DirectoryClient.setMetadataWithResponse#map-duration-context.clearMetadata
    }

    /**
     * Generates a code sample for using {@link DirectoryClient#listHandles(Integer, boolean, Duration, Context)}
     */
    public void listHandles() {
        DirectoryClient directoryClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryClient.listHandles#Integer-boolean-duration-context
        Iterable<HandleItem> result = directoryClient.listHandles(10, true, Duration.ofSeconds(1),
            new Context(key1, value1));
        System.out.printf("Get handles completed with handle id %s", result.iterator().next().getHandleId());
        // END: com.azure.storage.file.directoryClient.listHandles#Integer-boolean-duration-context
    }

    /**
     * Generates a code sample for using {@link DirectoryClient#forceCloseHandles(String, boolean, Duration, Context)}
     */
    public void forceCloseHandles() {
        DirectoryClient directoryClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryClient.forceCloseHandles
        Iterable<HandleItem> result = directoryClient.listHandles(10, true, Duration.ofSeconds(1),
            new Context(key1, value1));
        result.forEach(handleItem -> directoryClient
            .forceCloseHandles(handleItem.getHandleId(), true, Duration.ofSeconds(1), new Context(key1, value1))
            .forEach(numOfClosedHandles ->
                System.out.printf("Get handles completed with handle id %s", handleItem.getHandleId())));
        // END: com.azure.storage.file.directoryClient.forceCloseHandles
    }

    /**
     * Generates a code sample for using {@link DirectoryClient#getShareSnapshotId()}
     */
    public void getShareSnapshotId() {
        // BEGIN: com.azure.storage.file.directoryClient.getShareSnapshotId
        OffsetDateTime currentTime = OffsetDateTime.of(LocalDateTime.now(), ZoneOffset.UTC);
        DirectoryClient directoryClient = new FileClientBuilder()
            .endpoint("https://${accountName}.file.core.windows.net")
            .credential(SASTokenCredential.fromSASTokenString("${SASToken}"))
            .shareName("myshare")
            .resourcePath("mydirectory")
            .snapshot(currentTime.toString())
            .buildDirectoryClient();

        System.out.printf("Snapshot ID: %s%n", directoryClient.getShareSnapshotId());
        // END: com.azure.storage.file.directoryClient.getShareSnapshotId
    }

    /**
     * Generates a code sample for using {@link DirectoryClient#getName()}
     */
    public void getNameAsync() {
        DirectoryClient directoryClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryClient.getName
        String directoryPath = directoryClient.getName();
        System.out.println("The name of the directory is " + directoryPath);
        // END: com.azure.storage.file.directoryClient.getName
    }
}
