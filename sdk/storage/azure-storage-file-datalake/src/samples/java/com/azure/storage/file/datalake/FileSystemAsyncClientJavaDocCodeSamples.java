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
 * Code snippets for {@link DataLakeFileSystemAsyncClient}
 */
@SuppressWarnings({"unused"})
public class FileSystemAsyncClientJavaDocCodeSamples {

    private DataLakeFileSystemAsyncClient client = JavaDocCodeSnippetsHelpers.getFileSystemAsyncClient();
    private String fileName = "fileName";
    private String directoryName = "directoryName";
    private String leaseId = "leaseId";
    private String proposedId = "proposedId";
    private int leaseDuration = (int) Duration.ofSeconds(30).getSeconds();

    /**
     * Code snippet for {@link DataLakeFileSystemAsyncClient#getFileAsyncClient(String)}
     */
    public void getFileAsyncClient() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.getFileAsyncClient#String
        DataLakeFileAsyncClient dataLakeFileAsyncClient = client.getFileAsyncClient(fileName);
        // END: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.getFileAsyncClient#String
    }

    /**
     * Code snippet for {@link DataLakeFileSystemAsyncClient#getDirectoryAsyncClient(String)}
     */
    public void getDirectoryAsyncClient() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.getDirectoryAsyncClient#String
        DataLakeDirectoryAsyncClient dataLakeDirectoryAsyncClient = client.getDirectoryAsyncClient(directoryName);
        // END: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.getDirectoryAsyncClient#String
    }

    /**
     * Generates a code sample for using {@link DataLakeFileSystemAsyncClient#getFileSystemName()}
     */
    public void getFileSystemName() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.getFileSystemName
        String fileSystemName = client.getFileSystemName();
        System.out.println("The name of the file system is " + fileSystemName);
        // END: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.getFileSystemName
    }

    /**
     * Code snippet for {@link DataLakeFileSystemAsyncClient#create()}
     */
    public void create() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.create
        client.create().subscribe(
            response -> System.out.printf("Create completed%n"),
            error -> System.out.printf("Error while creating file system %s%n", error));
        // END: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.create
    }

    /**
     * Code snippet for {@link DataLakeFileSystemAsyncClient#createWithResponse(Map, PublicAccessType)}
     */
    public void create2() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.createWithResponse#Map-PublicAccessType
        Map<String, String> metadata = Collections.singletonMap("metadata", "value");
        client.createWithResponse(metadata, PublicAccessType.CONTAINER).subscribe(response ->
            System.out.printf("Create completed with status %d%n", response.getStatusCode()));
        // END: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.createWithResponse#Map-PublicAccessType
    }

    /**
     * Code snippet for {@link DataLakeFileSystemAsyncClient#delete()}
     */
    public void delete() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.delete
        client.delete().subscribe(
            response -> System.out.printf("Delete completed%n"),
            error -> System.out.printf("Delete failed: %s%n", error));
        // END: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.delete
    }

    /**
     * Code snippet for {@link DataLakeFileSystemAsyncClient#deleteWithResponse(DataLakeRequestConditions)}
     */
    public void delete2() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.deleteWithResponse#DataLakeRequestConditions
        DataLakeRequestConditions accessConditions = new DataLakeRequestConditions()
            .setLeaseId(leaseId)
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));

        client.deleteWithResponse(accessConditions).subscribe(response ->
            System.out.printf("Delete completed with status %d%n", response.getStatusCode()));
        // END: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.deleteWithResponse#DataLakeRequestConditions
    }

    /**
     * Code snippet for {@link DataLakeFileSystemAsyncClient#getProperties()}
     */
    public void getProperties() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.getProperties
        client.getProperties().subscribe(response ->
            System.out.printf("Public Access Type: %s, Legal Hold? %b, Immutable? %b%n",
                response.getPublicAccess(),
                response.hasLegalHold(),
                response.hasImmutabilityPolicy()));
        // END: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.getProperties
    }

    /**
     * Code snippet for {@link DataLakeFileSystemAsyncClient#getPropertiesWithResponse(String)}
     */
    public void getProperties2() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.getPropertiesWithResponse#String

        client.getPropertiesWithResponse(leaseId).subscribe(response ->
            System.out.printf("Public Access Type: %s, Legal Hold? %b, Immutable? %b%n",
                response.getValue().getPublicAccess(),
                response.getValue().hasLegalHold(),
                response.getValue().hasImmutabilityPolicy()));
        // END: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.getPropertiesWithResponse#String
    }

    /**
     * Code snippet for {@link DataLakeFileSystemAsyncClient#setMetadata(Map)}
     */
    public void setMetadata() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.setMetadata#Map
        Map<String, String> metadata = Collections.singletonMap("metadata", "value");
        client.setMetadata(metadata).subscribe(
            response -> System.out.printf("Set metadata completed%n"),
            error -> System.out.printf("Set metadata failed: %s%n", error));
        // END: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.setMetadata#Map
    }

    /**
     * Code snippet for {@link DataLakeFileSystemAsyncClient#setMetadataWithResponse(Map, DataLakeRequestConditions)}
     */
    public void setMetadata2() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.setMetadataWithResponse#Map-DataLakeRequestConditions
        Map<String, String> metadata = Collections.singletonMap("metadata", "value");
        DataLakeRequestConditions accessConditions = new DataLakeRequestConditions()
            .setLeaseId(leaseId)
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));

        client.setMetadataWithResponse(metadata, accessConditions).subscribe(response ->
            System.out.printf("Set metadata completed with status %d%n", response.getStatusCode()));
        // END: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.setMetadataWithResponse#Map-DataLakeRequestConditions
    }

    /**
     * Code snippets for {@link DataLakeFileSystemAsyncClient#createFile(String)} and
     * {@link DataLakeFileSystemAsyncClient#createFileWithResponse(String, PathHttpHeaders, Map, DataLakeRequestConditions, String, String)}
     */
    public void createFileCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.createFile#String
        Mono<DataLakeFileAsyncClient> fileClient = client.createFile(fileName);
        // END: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.createFile#String

        // BEGIN: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.createFileWithResponse#String-PathHttpHeaders-Map-DataLakeRequestConditions-String-String
        PathHttpHeaders httpHeaders = new PathHttpHeaders()
            .setContentLanguage("en-US")
            .setContentType("binary");
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions()
            .setLeaseId(leaseId);
        String permissions = "permissions";
        String umask = "umask";
        Mono<Response<DataLakeFileAsyncClient>> newFileClient = client.createFileWithResponse(fileName, httpHeaders,
            Collections.singletonMap("metadata", "value"), requestConditions, permissions, umask);
        // END: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.createFileWithResponse#String-PathHttpHeaders-Map-DataLakeRequestConditions-String-String
    }

    /**
     * Code snippets for {@link DataLakeFileSystemAsyncClient#deleteFile(String)} and
     * {@link DataLakeFileSystemAsyncClient#deleteFileWithResponse(String, DataLakeRequestConditions)}
     */
    public void deleteFileCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.deleteFile#String
        client.deleteFile(fileName).subscribe(response ->
            System.out.println("Delete request completed"));
        // END: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.deleteFile#String

        // BEGIN: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.deleteFileWithResponse#String-DataLakeRequestConditions
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions()
            .setLeaseId(leaseId);

        client.deleteFileWithResponse(fileName, requestConditions)
            .subscribe(response -> System.out.println("Delete request completed"));
        // END: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.deleteFileWithResponse#String-DataLakeRequestConditions
    }

    /**
     * Code snippets for {@link DataLakeFileSystemAsyncClient#createDirectory(String)} and
     * {@link DataLakeFileSystemAsyncClient#createDirectoryWithResponse(String, PathHttpHeaders, Map, DataLakeRequestConditions, String, String)}
     */
    public void createDirectoryCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.createDirectory#String
        Mono<DataLakeDirectoryAsyncClient> directoryClient = client.createDirectory(fileName);
        // END: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.createDirectory#String

        // BEGIN: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.createDirectoryWithResponse#String-PathHttpHeaders-Map-DataLakeRequestConditions-String-String
        PathHttpHeaders httpHeaders = new PathHttpHeaders()
            .setContentLanguage("en-US")
            .setContentType("binary");
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions()
            .setLeaseId(leaseId);
        String permissions = "permissions";
        String umask = "umask";
        Mono<Response<DataLakeDirectoryAsyncClient>> newDirectoryClient = client.createDirectoryWithResponse(fileName,
            httpHeaders, Collections.singletonMap("metadata", "value"), requestConditions, permissions, umask);
        // END: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.createDirectoryWithResponse#String-PathHttpHeaders-Map-DataLakeRequestConditions-String-String
    }

    /**
     * Code snippets for {@link DataLakeFileSystemAsyncClient#deleteDirectory(String)} and
     * {@link DataLakeFileSystemAsyncClient#deleteDirectoryWithResponse(String, boolean, DataLakeRequestConditions)}
     */
    public void deleteDirectoryCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.deleteDirectory#String
        client.deleteDirectory(directoryName).subscribe(response ->
            System.out.println("Delete request completed"));
        // END: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.deleteDirectory#String

        // BEGIN: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.deleteDirectoryWithResponse#String-boolean-DataLakeRequestConditions
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions()
            .setLeaseId(leaseId);
        boolean recursive = false; // Default value

        client.deleteDirectoryWithResponse(directoryName, recursive, requestConditions)
            .subscribe(response -> System.out.println("Delete request completed"));
        // END: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.deleteDirectoryWithResponse#String-boolean-DataLakeRequestConditions
    }

    /**
     * Code snippets for {@link DataLakeFileSystemAsyncClient#listPaths()} and
     * {@link DataLakeFileSystemAsyncClient#listPaths(ListPathsOptions)}
     */
    public void listPaths() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.listPaths
        client.listPaths().subscribe(path -> System.out.printf("Name: %s%n", path.getName()));
        // END: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.listPaths

        // BEGIN: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.listPaths#ListPathsOptions
        ListPathsOptions options = new ListPathsOptions()
            .setPath("PathNamePrefixToMatch")
            .setMaxResults(10);

        client.listPaths(options).subscribe(path -> System.out.printf("Name: %s%n", path.getName()));
        // END: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.listPaths#ListPathsOptions
    }

}
