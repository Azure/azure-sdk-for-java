// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import com.azure.core.http.rest.Response;
import com.azure.storage.file.datalake.models.DataLakeRequestConditions;
import com.azure.storage.file.datalake.models.ListPathsOptions;
import com.azure.storage.file.datalake.models.PathHttpHeaders;
import com.azure.storage.file.datalake.models.PublicAccessType;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Map;

/**
 * Code snippets for {@link FileSystemAsyncClient}
 */
@SuppressWarnings({"unused"})
public class FileSystemAsyncClientJavaDocCodeSamples {

    private FileSystemAsyncClient client = JavaDocCodeSnippetsHelpers.getFileSystemAsyncClient();
    private String fileName = "fileName";
    private String directoryName = "directoryName";
    private String leaseId = "leaseId";
    private String proposedId = "proposedId";
    private int leaseDuration = (int) Duration.ofSeconds(30).getSeconds();

    /**
     * Code snippet for {@link FileSystemAsyncClient#getFileAsyncClient(String)}
     */
    public void getFileAsyncClient() {
        // BEGIN: com.azure.storage.file.datalake.FileSystemAsyncClient.getFileAsyncClient#String
        DataLakeFileAsyncClient dataLakeFileAsyncClient = client.getFileAsyncClient(fileName);
        // END: com.azure.storage.file.datalake.FileSystemAsyncClient.getFileAsyncClient#String
    }

    /**
     * Code snippet for {@link FileSystemAsyncClient#getDirectoryAsyncClient(String)}
     */
    public void getDirectoryAsyncClient() {
        // BEGIN: com.azure.storage.file.datalake.FileSystemAsyncClient.getDirectoryAsyncClient#String
        DataLakeDirectoryAsyncClient dataLakeDirectoryAsyncClient = client.getDirectoryAsyncClient(directoryName);
        // END: com.azure.storage.file.datalake.FileSystemAsyncClient.getDirectoryAsyncClient#String
    }

    /**
     * Generates a code sample for using {@link FileSystemAsyncClient#getFileSystemName()}
     */
    public void getFileSystemName() {
        // BEGIN: com.azure.storage.file.datalake.FileSystemAsyncClient.getFileSystemName
        String fileSystemName = client.getFileSystemName();
        System.out.println("The name of the file system is " + fileSystemName);
        // END: com.azure.storage.file.datalake.FileSystemAsyncClient.getFileSystemName
    }

    /**
     * Code snippet for {@link FileSystemAsyncClient#create()}
     */
    public void create() {
        // BEGIN: com.azure.storage.file.datalake.FileSystemAsyncClient.create
        client.create().subscribe(
            response -> System.out.printf("Create completed%n"),
            error -> System.out.printf("Error while creating file system %s%n", error));
        // END: com.azure.storage.file.datalake.FileSystemAsyncClient.create
    }

    /**
     * Code snippet for {@link FileSystemAsyncClient#createWithResponse(Map, PublicAccessType)}
     */
    public void create2() {
        // BEGIN: com.azure.storage.file.datalake.FileSystemAsyncClient.createWithResponse#Map-PublicAccessType
        Map<String, String> metadata = Collections.singletonMap("metadata", "value");
        client.createWithResponse(metadata, PublicAccessType.CONTAINER).subscribe(response ->
            System.out.printf("Create completed with status %d%n", response.getStatusCode()));
        // END: com.azure.storage.file.datalake.FileSystemAsyncClient.createWithResponse#Map-PublicAccessType
    }

    /**
     * Code snippet for {@link FileSystemAsyncClient#delete()}
     */
    public void delete() {
        // BEGIN: com.azure.storage.file.datalake.FileSystemAsyncClient.delete
        client.delete().subscribe(
            response -> System.out.printf("Delete completed%n"),
            error -> System.out.printf("Delete failed: %s%n", error));
        // END: com.azure.storage.file.datalake.FileSystemAsyncClient.delete
    }

    /**
     * Code snippet for {@link FileSystemAsyncClient#deleteWithResponse(DataLakeRequestConditions)}
     */
    public void delete2() {
        // BEGIN: com.azure.storage.file.datalake.FileSystemAsyncClient.deleteWithResponse#DataLakeRequestConditions
        DataLakeRequestConditions accessConditions = new DataLakeRequestConditions()
            .setLeaseId(leaseId)
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));

        client.deleteWithResponse(accessConditions).subscribe(response ->
            System.out.printf("Delete completed with status %d%n", response.getStatusCode()));
        // END: com.azure.storage.file.datalake.FileSystemAsyncClient.deleteWithResponse#DataLakeRequestConditions
    }

    /**
     * Code snippet for {@link FileSystemAsyncClient#getProperties()}
     */
    public void getProperties() {
        // BEGIN: com.azure.storage.file.datalake.FileSystemAsyncClient.getProperties
        client.getProperties().subscribe(response ->
            System.out.printf("Public Access Type: %s, Legal Hold? %b, Immutable? %b%n",
                response.getPublicAccess(),
                response.hasLegalHold(),
                response.hasImmutabilityPolicy()));
        // END: com.azure.storage.file.datalake.FileSystemAsyncClient.getProperties
    }

    /**
     * Code snippet for {@link FileSystemAsyncClient#getPropertiesWithResponse(String)}
     */
    public void getProperties2() {
        // BEGIN: com.azure.storage.file.datalake.FileSystemAsyncClient.getPropertiesWithResponse#String

        client.getPropertiesWithResponse(leaseId).subscribe(response ->
            System.out.printf("Public Access Type: %s, Legal Hold? %b, Immutable? %b%n",
                response.getValue().getPublicAccess(),
                response.getValue().hasLegalHold(),
                response.getValue().hasImmutabilityPolicy()));
        // END: com.azure.storage.file.datalake.FileSystemAsyncClient.getPropertiesWithResponse#String
    }

    /**
     * Code snippet for {@link FileSystemAsyncClient#setMetadata(Map)}
     */
    public void setMetadata() {
        // BEGIN: com.azure.storage.file.datalake.FileSystemAsyncClient.setMetadata#Map
        Map<String, String> metadata = Collections.singletonMap("metadata", "value");
        client.setMetadata(metadata).subscribe(
            response -> System.out.printf("Set metadata completed%n"),
            error -> System.out.printf("Set metadata failed: %s%n", error));
        // END: com.azure.storage.file.datalake.FileSystemAsyncClient.setMetadata#Map
    }

    /**
     * Code snippet for {@link FileSystemAsyncClient#setMetadataWithResponse(Map, DataLakeRequestConditions)}
     */
    public void setMetadata2() {
        // BEGIN: com.azure.storage.file.datalake.FileSystemAsyncClient.setMetadataWithResponse#Map-DataLakeRequestConditions
        Map<String, String> metadata = Collections.singletonMap("metadata", "value");
        DataLakeRequestConditions accessConditions = new DataLakeRequestConditions()
            .setLeaseId(leaseId)
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));

        client.setMetadataWithResponse(metadata, accessConditions).subscribe(response ->
            System.out.printf("Set metadata completed with status %d%n", response.getStatusCode()));
        // END: com.azure.storage.file.datalake.FileSystemAsyncClient.setMetadataWithResponse#Map-DataLakeRequestConditions
    }

    /**
     * Code snippets for {@link FileSystemAsyncClient#createFile(String)} and
     * {@link FileSystemAsyncClient#createFileWithResponse(String, PathHttpHeaders, Map, DataLakeRequestConditions, String, String)}
     */
    public void createFileCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.FileSystemAsyncClient.createFile#String
        Mono<DataLakeFileAsyncClient> fileClient = client.createFile(fileName);
        // END: com.azure.storage.file.datalake.FileSystemAsyncClient.createFile#String

        // BEGIN: com.azure.storage.file.datalake.FileSystemAsyncClient.createFileWithResponse#String-PathHttpHeaders-Map-DataLakeRequestConditions-String-String
        PathHttpHeaders httpHeaders = new PathHttpHeaders()
            .setContentLanguage("en-US")
            .setContentType("binary");
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions()
            .setLeaseId(leaseId);
        String permissions = "permissions";
        String umask = "umask";
        Mono<Response<DataLakeFileAsyncClient>> newFileClient = client.createFileWithResponse(fileName, httpHeaders,
            Collections.singletonMap("metadata", "value"), requestConditions, permissions, umask);
        // END: com.azure.storage.file.datalake.FileSystemAsyncClient.createFileWithResponse#String-PathHttpHeaders-Map-DataLakeRequestConditions-String-String
    }

    /**
     * Code snippets for {@link FileSystemAsyncClient#deleteFile(String)} and
     * {@link FileSystemAsyncClient#deleteFileWithResponse(String, DataLakeRequestConditions)}
     */
    public void deleteFileCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.FileSystemAsyncClient.deleteFile#String
        client.deleteFile(fileName).subscribe(response ->
            System.out.println("Delete request completed"));
        // END: com.azure.storage.file.datalake.FileSystemAsyncClient.deleteFile#String

        // BEGIN: com.azure.storage.file.datalake.FileSystemAsyncClient.deleteFileWithResponse#String-DataLakeRequestConditions
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions()
            .setLeaseId(leaseId);

        client.deleteFileWithResponse(fileName, requestConditions)
            .subscribe(response -> System.out.println("Delete request completed"));
        // END: com.azure.storage.file.datalake.FileSystemAsyncClient.deleteFileWithResponse#String-DataLakeRequestConditions
    }

    /**
     * Code snippets for {@link FileSystemAsyncClient#createDirectory(String)} and
     * {@link FileSystemAsyncClient#createDirectoryWithResponse(String, PathHttpHeaders, Map, DataLakeRequestConditions, String, String)}
     */
    public void createDirectoryCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.FileSystemAsyncClient.createDirectory#String
        Mono<DataLakeDirectoryAsyncClient> directoryClient = client.createDirectory(fileName);
        // END: com.azure.storage.file.datalake.FileSystemAsyncClient.createDirectory#String

        // BEGIN: com.azure.storage.file.datalake.FileSystemAsyncClient.createDirectoryWithResponse#String-PathHttpHeaders-Map-DataLakeRequestConditions-String-String
        PathHttpHeaders httpHeaders = new PathHttpHeaders()
            .setContentLanguage("en-US")
            .setContentType("binary");
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions()
            .setLeaseId(leaseId);
        String permissions = "permissions";
        String umask = "umask";
        Mono<Response<DataLakeDirectoryAsyncClient>> newDirectoryClient = client.createDirectoryWithResponse(fileName, httpHeaders,
            Collections.singletonMap("metadata", "value"), requestConditions, permissions, umask);
        // END: com.azure.storage.file.datalake.FileSystemAsyncClient.createDirectoryWithResponse#String-PathHttpHeaders-Map-DataLakeRequestConditions-String-String
    }

    /**
     * Code snippets for {@link FileSystemAsyncClient#deleteDirectory(String)} and
     * {@link FileSystemAsyncClient#deleteDirectoryWithResponse(String, boolean, DataLakeRequestConditions)}
     */
    public void deleteDirectoryCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.FileSystemAsyncClient.deleteDirectory#String
        client.deleteDirectory(directoryName).subscribe(response ->
            System.out.println("Delete request completed"));
        // END: com.azure.storage.file.datalake.FileSystemAsyncClient.deleteDirectory#String

        // BEGIN: com.azure.storage.file.datalake.FileSystemAsyncClient.deleteDirectoryWithResponse#String-boolean-DataLakeRequestConditions
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions()
            .setLeaseId(leaseId);
        boolean recursive = false; // Default value

        client.deleteDirectoryWithResponse(directoryName, recursive, requestConditions)
            .subscribe(response -> System.out.println("Delete request completed"));
        // END: com.azure.storage.file.datalake.FileSystemAsyncClient.deleteDirectoryWithResponse#String-boolean-DataLakeRequestConditions
    }

    /**
     * Code snippets for {@link FileSystemAsyncClient#listPaths()} and
     * {@link FileSystemAsyncClient#listPaths(ListPathsOptions)}
     */
    public void listPaths() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeServiceAsyncClient.listPaths
        client.listPaths().subscribe(path -> System.out.printf("Name: %s%n", path.getName()));
        // END: com.azure.storage.file.datalake.DataLakeServiceAsyncClient.listPaths

        // BEGIN: com.azure.storage.file.datalake.DataLakeServiceAsyncClient.listPaths#ListPathsOptions
        ListPathsOptions options = new ListPathsOptions()
            .setPath("PathNamePrefixToMatch")
            .setMaxResults(10);

        client.listPaths(options).subscribe(path -> System.out.printf("Name: %s%n", path.getName()));
        // END: com.azure.storage.file.datalake.DataLakeServiceAsyncClient.listPaths#ListPathsOptions
    }

}
