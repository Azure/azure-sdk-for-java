// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file;

import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.VoidResponse;
import com.azure.storage.common.Utility;
import com.azure.core.util.Context;
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
     * Generates code sample for creating a {@link DirectoryClient} with {@code connectionString} which turns into {@link SharedKeyCredential}
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
     * Generates a code sample for using {@link DirectoryClient#createWithResponse(FileSmbProperties, String, Map, Context)}
     */
    public void createWithResponse() {
        DirectoryClient directoryClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryClient.createWithResponse#filesmbproperties-string-map-context
        FileSmbProperties smbProperties = new FileSmbProperties();
        String filePermission = "filePermission";
        Response<DirectoryInfo> response = directoryClient.createWithResponse(smbProperties, filePermission,
            Collections.singletonMap("directory", "metadata"), new Context(key1, value1));
        System.out.println("Completed creating the directory with status code: " + response.statusCode());
        // END: com.azure.storage.file.directoryClient.createWithResponse#filesmbproperties-string-map-context
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
     * Generates a code sample for using {@link DirectoryClient#createSubDirectoryWithResponse(String, FileSmbProperties, String, Map, Context)}
     */
    public void createSubDirectoryMaxOverload() {
        DirectoryClient directoryClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryClient.createSubDirectoryWithResponse#string-filesmbproperties-string-map-context
        FileSmbProperties smbProperties = new FileSmbProperties();
        String filePermission = "filePermission";
        Response<DirectoryClient> response = directoryClient.createSubDirectoryWithResponse("subdir", smbProperties,
            filePermission, Collections.singletonMap("directory", "metadata"), new Context(key1, value1));
        System.out.printf("Creating the sub directory completed with status code %d", response.statusCode());
        // END: com.azure.storage.file.directoryClient.createSubDirectoryWithResponse#string-filesmbproperties-string-map-context
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
     * Generates a code sample for using {@link DirectoryClient#createFileWithResponse(String, long, FileHTTPHeaders, FileSmbProperties, String, Map, Context)}
     */
    public void createFileMaxOverload() {
        DirectoryClient directoryClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryClient.createFile#string-long-fileHTTPHeaders-fileSMBProperties-string-map-Context
        FileHTTPHeaders httpHeaders = new FileHTTPHeaders().fileContentType("text/plain");
        FileSmbProperties smbProperties = new FileSmbProperties();
        String filePermission = "filePermission";
        Response<FileClient> response = directoryClient.createFileWithResponse("myFile", 1024, httpHeaders,
            smbProperties, filePermission, Collections.singletonMap("directory", "metadata"), new Context(key1, value1));
        System.out.println("Completed creating the file with status code: " + response.statusCode());
        // END: com.azure.storage.file.directoryClient.createFile#string-long-fileHTTPHeaders-fileSMBProperties-string-map-Context
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
     * Generates a code sample for using {@link DirectoryClient#deleteFile(String)} ()}
     */
    public void deleteFile() {
        DirectoryClient directoryClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.DirectoryClient.deleteFileWithResponse#String-Context
        directoryClient.deleteFile("myfile");
        System.out.println("Completed deleting the file.");
        // END: com.azure.storage.file.DirectoryClient.deleteFileWithResponse#String-Context
    }

    /**
     * Generates a code sample for using {@link DirectoryClient#deleteFileWithResponse(String, Context)}
     */
    public void deleteFileWithResponse() {
        DirectoryClient directoryClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryClient.deleteFileWithResponse#string-Context
        VoidResponse response = directoryClient.deleteFileWithResponse("myfile", new Context(key1, value1));
        System.out.println("Completed deleting the file with status code: " + response.statusCode());
        // END: com.azure.storage.file.directoryClient.deleteFileWithResponse#string-Context
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
     * Generates a code sample for using {@link DirectoryClient#deleteSubDirectoryWithResponse(String, Context)}
     */
    public void deleteSubDirectoryWithResponse() {
        DirectoryClient directoryClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryClient.deleteSubDirectoryWithResponse#string-Context
        VoidResponse response = directoryClient.deleteSubDirectoryWithResponse("mysubdirectory",
            new Context(key1, value1));
        System.out.println("Completed deleting the subdirectory with status code: " + response.statusCode());
        // END: com.azure.storage.file.directoryClient.deleteSubDirectoryWithResponse#string-Context
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
     * Generates a code sample for using {@link DirectoryClient#deleteWithResponse(Context)}
     */
    public void deleteWithResponse() {
        DirectoryClient directoryClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.DirectoryClient.deleteWithResponse#Context
        VoidResponse response = directoryClient.deleteWithResponse(new Context(key1, value1));
        System.out.println("Completed deleting the file with status code: " + response.statusCode());
        // END: com.azure.storage.file.DirectoryClient.deleteWithResponse#Context
    }

    /**
     * Generates a code sample for using {@link DirectoryClient#getProperties()}
     */
    public void getProperties() {
        DirectoryClient directoryClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryClient.getProperties
        DirectoryProperties response = directoryClient.getProperties();
        System.out.printf("Directory latest modified date is %s.", response.lastModified());
        // END: com.azure.storage.file.directoryClient.getProperties
    }

    /**
     * Generates a code sample for using {@link DirectoryClient#getPropertiesWithResponse(Context)}
     */
    public void getPropertiesWithResponse() {
        DirectoryClient directoryClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryClient.getPropertiesWithResponse#Context
        Response<DirectoryProperties> response = directoryClient.getPropertiesWithResponse(new Context(key1, value1));
        System.out.printf("Directory latest modified date is %s.", response.value().lastModified());
        // END: com.azure.storage.file.directoryClient.getPropertiesWithResponse#Context
    }

    /**
     * Generates a code sample for using {@link DirectoryClient#setProperties(FileSmbProperties, String)}
     */
    public void setProperties() {
        DirectoryClient directoryClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryClient.setProperties#filesmbproperties-string
        FileSmbProperties smbProperties = new FileSmbProperties();
        String filePermission = "filePermission";
        FileSmbProperties response = directoryClient.setProperties(smbProperties, filePermission);
        System.out.printf("Directory change time is %s.", response.fileChangeTime());
        // END: com.azure.storage.file.directoryClient.setProperties#filesmbproperties-string
    }

    /**
     * Generates a code sample for using {@link DirectoryClient#setPropertiesWithResponse(FileSmbProperties, String,Context)}
     */
    public void setPropertiesWithResponse() {
        DirectoryClient directoryClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryClient.setPropertiesWithResponse#filesmbproperties-string-Context
        FileSmbProperties smbProperties = new FileSmbProperties();
        String filePermission = "filePermission";
        Response<FileSmbProperties> response = directoryClient.setPropertiesWithResponse(smbProperties, filePermission, new Context(key1, value1));
        System.out.printf("Directory change time is %s.", response.value().fileChangeTime());
        // END: com.azure.storage.file.directoryClient.setPropertiesWithResponse#filesmbproperties-string-Context
    }

    /**
     * Generates a code sample for using {@link DirectoryClient#setMetadata(Map)}
     */
    public void setMetadata() {
        DirectoryClient directoryClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryClient.setMetadata#map
        DirectorySetMetadataInfo response =
            directoryClient.setMetadata(Collections.singletonMap("directory", "updatedMetadata"));
        System.out.printf("Setting the directory metadata completed with updated etag %d", response.eTag());
        // END: com.azure.storage.file.directoryClient.setMetadata#map
    }

    /**
     * Generates a code sample for using {@link DirectoryClient#setMetadataWithResponse(Map, Context)}
     */
    public void setMetadataWithResponse() {
        DirectoryClient directoryClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryClient.setMetadataWithResponse#map-Context
        Response<DirectorySetMetadataInfo> response =
            directoryClient.setMetadataWithResponse(Collections.singletonMap("directory", "updatedMetadata"),
                new Context(key1, value1));
        System.out.printf("Setting the directory metadata completed with updated etag %d", response.statusCode());
        // END: com.azure.storage.file.directoryClient.setMetadataWithResponse#map-Context
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
        // BEGIN: com.azure.storage.file.DirectoryClient.setMetadataWithResponse#Map-Context.clearMetadata
        Response<DirectorySetMetadataInfo> response = directoryClient.setMetadataWithResponse(null,
            new Context(key1, value1));
        System.out.printf("Directory latest modified date is %s.", response.statusCode());
        // END: com.azure.storage.file.DirectoryClient.setMetadataWithResponse#Map-Context.clearMetadata
    }

    /**
     * Generates a code sample for using {@link DirectoryClient#listHandles(Integer, boolean)}
     */
    public void listHandles() {
        DirectoryClient directoryClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryClient.listHandles#Integer-boolean
        Iterable<HandleItem> result = directoryClient.listHandles(10, true);
        System.out.printf("Get handles completed with handle id %s", result.iterator().next().handleId());
        // END: com.azure.storage.file.directoryClient.listHandles#Integer-boolean
    }

    /**
     * Generates a code sample for using {@link DirectoryClient#forceCloseHandles(String, boolean)}
     */
    public void forceCloseHandles() {
        DirectoryClient directoryClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.file.directoryClient.forceCloseHandles
        Iterable<HandleItem> result = directoryClient.listHandles(10, true);
        result.forEach(handleItem ->  {
            directoryClient.forceCloseHandles(handleItem.handleId(), true).forEach(numOfClosedHandles ->
                System.out.printf("Get handles completed with handle id %s", handleItem.handleId()));
        });
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
}
