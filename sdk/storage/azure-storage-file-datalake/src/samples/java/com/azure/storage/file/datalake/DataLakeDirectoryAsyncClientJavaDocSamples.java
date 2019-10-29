// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import com.azure.core.http.rest.Response;
import com.azure.storage.file.datalake.models.DataLakeRequestConditions;
import com.azure.storage.file.datalake.models.PathHttpHeaders;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Map;

/**
 * Code snippets for {@link DataLakeDirectoryAsyncClient}
 */
@SuppressWarnings({"unused"})
public class DataLakeDirectoryAsyncClientJavaDocSamples {
    private String fileName = "fileName";
    private String directoryName = "directoryName";
    private DataLakeDirectoryAsyncClient client = JavaDocCodeSnippetsHelpers.getDirectoryAsyncClient(directoryName);
    private String leaseId = "leaseId";
    private String destinationPath = "destinationPath";

    /**
     * Code snippet for {@link DataLakeDirectoryAsyncClient#getSubDirectoryAsyncClient(String)}
     */
    public void getDirectoryClient() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.getSubDirectoryAsyncClient#String
        DataLakeDirectoryAsyncClient dataLakeDirectoryClient = client.getSubDirectoryAsyncClient(directoryName);
        // END: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.getSubDirectoryAsyncClient#String
    }

    /**
     * Code snippet for {@link DataLakeDirectoryAsyncClient#getFileAsyncClient(String)}
     */
    public void getFileClient() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.getFileAsyncClient#String
        DataLakeFileAsyncClient dataLakeFileClient = client.getFileAsyncClient(fileName);
        // END: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.getFileAsyncClient#String
    }

    /**
     * Code snippets for {@link DataLakeDirectoryAsyncClient#create()} and
     * {@link DataLakeDirectoryAsyncClient#createWithResponse(PathHttpHeaders, Map, DataLakeRequestConditions, String, String)}
     */
    public void createCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.create
        client.create().subscribe(response ->
            System.out.printf("Last Modified Time:%s", response.getLastModified()));
        // END: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.create

        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.createWithResponse#PathHttpHeaders-Map-DataLakeRequestConditions-String-String
        PathHttpHeaders httpHeaders = new PathHttpHeaders()
            .setContentLanguage("en-US")
            .setContentType("binary");
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions()
            .setLeaseId(leaseId);
        String permissions = "permissions";
        String umask = "umask";

        client.createWithResponse(httpHeaders, Collections.singletonMap("metadata", "value"), requestConditions,
            permissions, umask)
            .subscribe(response -> System.out.printf("Last Modified Time:%s", response.getValue().getLastModified()));
        // END: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.createWithResponse#PathHttpHeaders-Map-DataLakeRequestConditions-String-String
    }

    /**
     * Code snippets for {@link DataLakeDirectoryAsyncClient#delete()} and
     * {@link DataLakeDirectoryAsyncClient#deleteWithResponse(boolean, DataLakeRequestConditions)}
     */
    public void deleteCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.delete
        client.delete().subscribe(response ->
            System.out.println("Delete request completed"));
        // END: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.delete

        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.deleteWithResponse#boolean-DataLakeRequestConditions
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions()
            .setLeaseId(leaseId);
        boolean recursive = false; // Default value

        client.deleteWithResponse(recursive, requestConditions)
            .subscribe(response -> System.out.println("Delete request completed"));
        // END: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.deleteWithResponse#boolean-DataLakeRequestConditions
    }

    /**
     * Code snippets for {@link DataLakeDirectoryAsyncClient#createFile(String)} and
     * {@link DataLakeDirectoryAsyncClient#createFileWithResponse(String, PathHttpHeaders, Map, DataLakeRequestConditions, String, String)}
     */
    public void createFileCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.createFile#String
        Mono<DataLakeFileAsyncClient> fileClient = client.createFile(fileName);
        // END: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.createFile#String

        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.createFileWithResponse#String-PathHttpHeaders-Map-DataLakeRequestConditions-String-String
        PathHttpHeaders httpHeaders = new PathHttpHeaders()
            .setContentLanguage("en-US")
            .setContentType("binary");
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions()
            .setLeaseId(leaseId);
        String permissions = "permissions";
        String umask = "umask";
        Mono<Response<DataLakeFileAsyncClient>> newFileClient = client.createFileWithResponse(fileName,
            httpHeaders, Collections.singletonMap("metadata", "value"), requestConditions, permissions,
            umask);
        // END: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.createFileWithResponse#String-PathHttpHeaders-Map-DataLakeRequestConditions-String-String
    }

    /**
     * Code snippets for {@link DataLakeDirectoryAsyncClient#deleteFile(String)} and
     * {@link DataLakeDirectoryAsyncClient#deleteFileWithResponse(String, DataLakeRequestConditions)}
     */
    public void deleteFileCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.deleteFile#String
        client.deleteFile(fileName).subscribe(response ->
            System.out.println("Delete request completed"));
        // END: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.deleteFile#String

        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.deleteFileWithResponse#String-DataLakeRequestConditions
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions()
            .setLeaseId(leaseId);

        client.deleteFileWithResponse(fileName, requestConditions)
            .subscribe(response -> System.out.println("Delete request completed"));
        // END: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.deleteFileWithResponse#String-DataLakeRequestConditions
    }

    /**
     * Code snippets for {@link DataLakeDirectoryAsyncClient#createSubDirectory(String)} and
     * {@link DataLakeDirectoryAsyncClient#createSubDirectoryWithResponse(String, PathHttpHeaders, Map, DataLakeRequestConditions, String, String)}
     */
    public void createSubDirectoryCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.createSubDirectory#String
        Mono<DataLakeDirectoryAsyncClient> directoryClient = client.createSubDirectory(fileName);
        // END: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.createSubDirectory#String

        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.createSubDirectoryWithResponse#String-PathHttpHeaders-Map-DataLakeRequestConditions-String-String
        PathHttpHeaders httpHeaders = new PathHttpHeaders()
            .setContentLanguage("en-US")
            .setContentType("binary");
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions()
            .setLeaseId(leaseId);
        String permissions = "permissions";
        String umask = "umask";
        Mono<Response<DataLakeDirectoryAsyncClient>> newDirectoryClient = client.createSubDirectoryWithResponse(
            fileName, httpHeaders, Collections.singletonMap("metadata", "value"), requestConditions, permissions,
            umask);
        // END: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.createSubDirectoryWithResponse#String-PathHttpHeaders-Map-DataLakeRequestConditions-String-String
    }

    /**
     * Code snippets for {@link DataLakeDirectoryAsyncClient#deleteSubDirectory(String)} and
     * {@link DataLakeDirectoryAsyncClient#deleteSubDirectoryWithResponse(String, boolean, DataLakeRequestConditions)}
     */
    public void deleteSubDirectoryCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.deleteSubDirectory#String
        client.deleteSubDirectory(directoryName).subscribe(response ->
            System.out.println("Delete request completed"));
        // END: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.deleteSubDirectory#String

        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.deleteSubDirectoryWithResponse#String-boolean-DataLakeRequestConditions
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions()
            .setLeaseId(leaseId);
        boolean recursive = false; // Default value

        client.deleteSubDirectoryWithResponse(directoryName, recursive, requestConditions)
            .subscribe(response -> System.out.println("Delete request completed"));
        // END: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.deleteSubDirectoryWithResponse#String-boolean-DataLakeRequestConditions
    }

    /**
     * Code snippets for {@link DataLakeDirectoryAsyncClient#rename(String)} and
     * {@link DataLakeDirectoryAsyncClient#renameWithResponse(String, DataLakeRequestConditions, DataLakeRequestConditions)}
     */
    public void renameCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.rename#String
        DataLakeDirectoryAsyncClient renamedClient = client.rename(destinationPath).block();
        System.out.println("Directory Client has been renamed");
        // END: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.rename#String

        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.renameWithResponse#String-DataLakeRequestConditions-DataLakeRequestConditions
        DataLakeRequestConditions sourceRequestConditions = new DataLakeRequestConditions()
            .setLeaseId(leaseId);
        DataLakeRequestConditions destinationRequestConditions = new DataLakeRequestConditions();

        DataLakeDirectoryAsyncClient newRenamedClient = client.renameWithResponse(destinationPath,
            sourceRequestConditions, destinationRequestConditions).block().getValue();
        System.out.println("Directory Client has been renamed");
        // END: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.renameWithResponse#String-DataLakeRequestConditions-DataLakeRequestConditions
    }
}
