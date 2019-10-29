// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.storage.blob.models.BlobErrorCode;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.file.datalake.models.DataLakeRequestConditions;
import com.azure.storage.file.datalake.models.FileSystemProperties;
import com.azure.storage.file.datalake.models.ListPathsOptions;
import com.azure.storage.file.datalake.models.PathHttpHeaders;
import com.azure.storage.file.datalake.models.PublicAccessType;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Map;

/**
 * Code snippets for {@link FileSystemClient}
 */
@SuppressWarnings({"unused"})
public class FileSystemClientJavaDocCodeSamples {

    private FileSystemClient client = JavaDocCodeSnippetsHelpers.getFileSystemClient();
    private String fileName = "fileName";
    private String directoryName = "directoryName";
    private String leaseId = "leaseId";
    private String proposedId = "proposedId";
    private int leaseDuration = (int) Duration.ofSeconds(30).getSeconds();
    private Duration timeout = Duration.ofSeconds(30);
    private String key1 = "key1";
    private String value1 = "value1";

    /**
     * Code snippet for {@link FileSystemClient#getFileClient(String)}
     */
    public void getFileClient() {
        // BEGIN: com.azure.storage.file.datalake.FileSystemClient.getFileClient#String
        DataLakeFileClient dataLakeFileClient = client.getFileClient(fileName);
        // END: com.azure.storage.file.datalake.FileSystemClient.getFileClient#String
    }

    /**
     * Code snippet for {@link FileSystemClient#getDirectoryClient(String)}
     */
    public void getDirectoryClient() {
        // BEGIN: com.azure.storage.file.datalake.FileSystemClient.getDirectoryClient#String
        DataLakeDirectoryClient dataLakeDirectoryClient = client.getDirectoryClient(directoryName);
        // END: com.azure.storage.file.datalake.FileSystemClient.getDirectoryClient#String
    }

    /**
     * Generates a code sample for using {@link FileSystemClient#getFileSystemName()}
     */
    public void getFileSystemName() {
        // BEGIN: com.azure.storage.file.datalake.FileSystemClient.getFileSystemName
        String fileSystemName = client.getFileSystemName();
        System.out.println("The name of the file system is " + fileSystemName);
        // END: com.azure.storage.file.datalake.FileSystemClient.getFileSystemName
    }

    /**
     * Code snippet for {@link FileSystemClient#create()}
     */
    public void create() {
        // BEGIN: com.azure.storage.file.datalake.FileSystemClient.create
        try {
            client.create();
            System.out.printf("Create completed%n");
        } catch (BlobStorageException error) {
            if (error.getErrorCode().equals(BlobErrorCode.CONTAINER_ALREADY_EXISTS)) {
                System.out.printf("Can't create container. It already exists %n");
            }
        }
        // END: com.azure.storage.file.datalake.FileSystemClient.create
    }

    /**
     * Code snippet for {@link FileSystemClient#createWithResponse(Map, PublicAccessType, Duration, Context)}
     */
    public void create2() {
        // BEGIN: com.azure.storage.file.datalake.FileSystemClient.createWithResponse#Map-PublicAccessType-Duration-Context
        Map<String, String> metadata = Collections.singletonMap("metadata", "value");
        Context context = new Context("Key", "Value");

        System.out.printf("Create completed with status %d%n",
            client.createWithResponse(metadata, PublicAccessType.CONTAINER, timeout, context).getStatusCode());
        // END: com.azure.storage.file.datalake.FileSystemClient.createWithResponse#Map-PublicAccessType-Duration-Context
    }

    /**
     * Code snippet for {@link FileSystemClient#delete()}
     */
    public void setDelete() {
        // BEGIN: com.azure.storage.file.datalake.FileSystemClient.delete
        try {
            client.delete();
            System.out.printf("Delete completed%n");
        } catch (BlobStorageException error) {
            if (error.getErrorCode().equals(BlobErrorCode.CONTAINER_NOT_FOUND)) {
                System.out.printf("Delete failed. File System was not found %n");
            }
        }
        // END: com.azure.storage.file.datalake.FileSystemClient.delete
    }

    /**
     * Code snippet for {@link FileSystemClient#deleteWithResponse(DataLakeRequestConditions, Duration, Context)}
     */
    public void delete2() {
        // BEGIN: com.azure.storage.file.datalake.FileSystemClient.deleteWithResponse#DataLakeRequestConditions-Duration-Context
        DataLakeRequestConditions accessConditions = new DataLakeRequestConditions()
            .setLeaseId(leaseId)
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));
        Context context = new Context("Key", "Value");

        System.out.printf("Delete completed with status %d%n", client.deleteWithResponse(
            accessConditions, timeout, context).getStatusCode());
        // END: com.azure.storage.file.datalake.FileSystemClient.deleteWithResponse#DataLakeRequestConditions-Duration-Context
    }

    /**
     * Code snippet for {@link FileSystemClient#getProperties()}
     */
    public void getProperties() {
        // BEGIN: com.azure.storage.file.datalake.FileSystemClient.getProperties
        FileSystemProperties properties = client.getProperties();
        System.out.printf("Public Access Type: %s, Legal Hold? %b, Immutable? %b%n",
            properties.getPublicAccess(),
            properties.hasLegalHold(),
            properties.hasImmutabilityPolicy());
        // END: com.azure.storage.file.datalake.FileSystemClient.getProperties
    }

    /**
     * Code snippet for {@link FileSystemClient#getPropertiesWithResponse(String, Duration, Context)}
     */
    public void getProperties2() {
        // BEGIN: com.azure.storage.file.datalake.FileSystemClient.getPropertiesWithResponse#String-Duration-Context
        Context context = new Context("Key", "Value");

        FileSystemProperties properties = client.getPropertiesWithResponse(leaseId, timeout, context)
            .getValue();
        System.out.printf("Public Access Type: %s, Legal Hold? %b, Immutable? %b%n",
            properties.getPublicAccess(),
            properties.hasLegalHold(),
            properties.hasImmutabilityPolicy());
        // END: com.azure.storage.file.datalake.FileSystemClient.getPropertiesWithResponse#String-Duration-Context
    }

    /**
     * Code snippet for {@link FileSystemClient#setMetadata(Map)}
     */
    public void setMetadata() {
        // BEGIN: com.azure.storage.file.datalake.FileSystemClient.setMetadata#Map
        Map<String, String> metadata = Collections.singletonMap("metadata", "value");
        try {
            client.setMetadata(metadata);
            System.out.printf("Set metadata completed with status %n");
        } catch (UnsupportedOperationException error) {
            System.out.printf("Fail while setting metadata %n");
        }
        // END: com.azure.storage.file.datalake.FileSystemClient.setMetadata#Map
    }

    /**
     * Code snippet for {@link FileSystemClient#setMetadataWithResponse(Map, DataLakeRequestConditions, Duration,
     * Context)}
     */
    public void setMetadata2() {
        // BEGIN: com.azure.storage.file.datalake.FileSystemClient.setMetadataWithResponse#Map-DataLakeRequestConditions-Duration-Context
        Map<String, String> metadata = Collections.singletonMap("metadata", "value");
        DataLakeRequestConditions accessConditions = new DataLakeRequestConditions()
            .setLeaseId(leaseId)
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));
        Context context = new Context("Key", "Value");

        System.out.printf("Set metadata completed with status %d%n",
            client.setMetadataWithResponse(metadata, accessConditions, timeout, context).getStatusCode());
        // END: com.azure.storage.file.datalake.FileSystemClient.setMetadataWithResponse#Map-DataLakeRequestConditions-Duration-Context
    }

    /**
     * Code snippets for {@link FileSystemClient#createFile(String)} and
     * {@link FileSystemClient#createFileWithResponse(String, PathHttpHeaders, Map, DataLakeRequestConditions, String, String, Duration, Context)}
     */
    public void createFileCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.FileSystemClient.createFile#String
        DataLakeFileClient fileClient = client.createFile(fileName);
        // END: com.azure.storage.file.datalake.FileSystemClient.createFile#String

        // BEGIN: com.azure.storage.file.datalake.FileSystemClient.createFileWithResponse#String-PathHttpHeaders-Map-DataLakeRequestConditions-String-String-Duration-Context
        PathHttpHeaders httpHeaders = new PathHttpHeaders()
            .setContentLanguage("en-US")
            .setContentType("binary");
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions()
            .setLeaseId(leaseId);
        String permissions = "permissions";
        String umask = "umask";
        Response<DataLakeFileClient> newFileClient = client.createFileWithResponse(fileName, httpHeaders,
            Collections.singletonMap("metadata", "value"), requestConditions,
            permissions, umask, timeout, new Context(key1, value1));
        // END: com.azure.storage.file.datalake.FileSystemClient.createFileWithResponse#String-PathHttpHeaders-Map-DataLakeRequestConditions-String-String-Duration-Context
    }

    /**
     * Code snippets for {@link FileSystemClient#deleteFile(String)} and
     * {@link FileSystemClient#deleteFileWithResponse(String, DataLakeRequestConditions, Duration, Context)}
     */
    public void deleteFileCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.FileSystemClient.deleteFile#String
        client.deleteFile(fileName);
        System.out.println("Delete request completed");
        // END: com.azure.storage.file.datalake.FileSystemClient.deleteFile#String

        // BEGIN: com.azure.storage.file.datalake.FileSystemClient.deleteFileWithResponse#String-DataLakeRequestConditions-Duration-Context
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions()
            .setLeaseId(leaseId);

        client.deleteFileWithResponse(fileName, requestConditions, timeout, new Context(key1, value1));
        System.out.println("Delete request completed");
        // END: com.azure.storage.file.datalake.FileSystemClient.deleteFileWithResponse#String-DataLakeRequestConditions-Duration-Context
    }

    /**
     * Code snippets for {@link FileSystemClient#createDirectory(String)} and
     * {@link FileSystemClient#createDirectoryWithResponse(String, PathHttpHeaders, Map, DataLakeRequestConditions, String, String, Duration, Context)}
     */
    public void createDirectoryCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.FileSystemClient.createDirectory#String
        DataLakeDirectoryClient directoryClient = client.createDirectory(directoryName);
        // END: com.azure.storage.file.datalake.FileSystemClient.createDirectory#String

        // BEGIN: com.azure.storage.file.datalake.FileSystemClient.createDirectoryWithResponse#String-PathHttpHeaders-Map-DataLakeRequestConditions-String-String-Duration-Context
        PathHttpHeaders httpHeaders = new PathHttpHeaders()
            .setContentLanguage("en-US")
            .setContentType("binary");
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions()
            .setLeaseId(leaseId);
        String permissions = "permissions";
        String umask = "umask";
        Response<DataLakeDirectoryClient> newDirectoryClient = client.createDirectoryWithResponse(directoryName,
            httpHeaders, Collections.singletonMap("metadata", "value"), requestConditions,
            permissions, umask, timeout, new Context(key1, value1));
        // END: com.azure.storage.file.datalake.FileSystemClient.createDirectoryWithResponse#String-PathHttpHeaders-Map-DataLakeRequestConditions-String-String-Duration-Context
    }

    /**
     * Code snippets for {@link FileSystemClient#deleteDirectory(String)} and
     * {@link FileSystemClient#deleteDirectoryWithResponse(String, boolean, DataLakeRequestConditions, Duration, Context)}
     */
    public void deleteDirectoryCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.FileSystemClient.deleteDirectory#String
        client.deleteDirectory(directoryName);
        System.out.println("Delete request completed");
        // END: com.azure.storage.file.datalake.FileSystemClient.deleteDirectory#String

        // BEGIN: com.azure.storage.file.datalake.FileSystemClient.deleteDirectoryWithResponse#String-boolean-DataLakeRequestConditions-Duration-Context
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions()
            .setLeaseId(leaseId);
        boolean recursive = false; // Default value

        client.deleteDirectoryWithResponse(directoryName, recursive, requestConditions, timeout,
            new Context(key1, value1));
        System.out.println("Delete request completed");
        // END: com.azure.storage.file.datalake.FileSystemClient.deleteDirectoryWithResponse#String-boolean-DataLakeRequestConditions-Duration-Context
    }

    /**
     * Code snippets for {@link FileSystemClient#listPaths()} and
     * {@link FileSystemClient#listPaths(ListPathsOptions, Duration)}
     */
    public void listPaths() {
        // BEGIN: com.azure.storage.file.datalake.FileSystemClient.listPaths
        client.listPaths().forEach(path -> System.out.printf("Name: %s%n", path.getName()));
        // END: com.azure.storage.file.datalake.FileSystemClient.listPaths

        // BEGIN: com.azure.storage.file.datalake.FileSystemClient.listPaths#ListPathsOptions-Duration
        ListPathsOptions options = new ListPathsOptions()
            .setPath("pathPrefixToMatch")
            .setMaxResults(10);

        client.listPaths(options, timeout).forEach(path -> System.out.printf("Name: %s%n", path.getName()));
        // END: com.azure.storage.file.datalake.FileSystemClient.listPaths#ListPathsOptions-Duration
    }

}
