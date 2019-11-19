// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import com.azure.storage.file.datalake.models.DataLakeRequestConditions;
import com.azure.storage.file.datalake.models.PathHttpHeaders;

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
     * Code snippet for {@link DataLakeDirectoryAsyncClient#getSubdirectoryAsyncClient(String)}
     */
    public void getDirectoryClient() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.getSubdirectoryAsyncClient#String
        DataLakeDirectoryAsyncClient dataLakeDirectoryClient = client.getSubdirectoryAsyncClient(directoryName);
        // END: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.getSubdirectoryAsyncClient#String
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
     * {@link DataLakeDirectoryAsyncClient#createFileWithResponse(String, String, String, PathHttpHeaders, Map, DataLakeRequestConditions)}
     */
    public void createFileCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.createFile#String
        DataLakeFileAsyncClient fileClient = client.createFile(fileName).block();
        // END: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.createFile#String

        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.createFileWithResponse#String-String-String-PathHttpHeaders-Map-DataLakeRequestConditions
        PathHttpHeaders httpHeaders = new PathHttpHeaders()
            .setContentLanguage("en-US")
            .setContentType("binary");
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions()
            .setLeaseId(leaseId);
        String permissions = "permissions";
        String umask = "umask";
        DataLakeFileAsyncClient newFileClient = client.createFileWithResponse(fileName,
            permissions, umask, httpHeaders, Collections.singletonMap("metadata", "value"), requestConditions
        ).block().getValue();
        // END: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.createFileWithResponse#String-String-String-PathHttpHeaders-Map-DataLakeRequestConditions
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
     * Code snippets for {@link DataLakeDirectoryAsyncClient#createSubdirectory(String)} and
     * {@link DataLakeDirectoryAsyncClient#createSubdirectoryWithResponse(String, String, String, PathHttpHeaders, Map, DataLakeRequestConditions)}
     */
    public void createSubdirectoryCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.createSubdirectory#String
        DataLakeDirectoryAsyncClient directoryClient = client.createSubdirectory(fileName).block();
        // END: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.createSubdirectory#String

        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.createSubdirectoryWithResponse#String-String-String-PathHttpHeaders-Map-DataLakeRequestConditions
        PathHttpHeaders httpHeaders = new PathHttpHeaders()
            .setContentLanguage("en-US")
            .setContentType("binary");
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions()
            .setLeaseId(leaseId);
        String permissions = "permissions";
        String umask = "umask";
        DataLakeDirectoryAsyncClient newDirectoryClient = client.createSubdirectoryWithResponse(
            fileName, permissions, umask, httpHeaders, Collections.singletonMap("metadata", "value"), requestConditions
        ).block().getValue();
        // END: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.createSubdirectoryWithResponse#String-String-String-PathHttpHeaders-Map-DataLakeRequestConditions
    }

    /**
     * Code snippets for {@link DataLakeDirectoryAsyncClient#deleteSubdirectory(String)} and
     * {@link DataLakeDirectoryAsyncClient#deleteSubdirectoryWithResponse(String, boolean, DataLakeRequestConditions)}
     */
    public void deleteSubdirectoryCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.deleteSubdirectory#String
        client.deleteSubdirectory(directoryName).subscribe(response ->
            System.out.println("Delete request completed"));
        // END: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.deleteSubdirectory#String

        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.deleteSubdirectoryWithResponse#String-boolean-DataLakeRequestConditions
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions()
            .setLeaseId(leaseId);
        boolean recursive = false; // Default value

        client.deleteSubdirectoryWithResponse(directoryName, recursive, requestConditions)
            .subscribe(response -> System.out.println("Delete request completed"));
        // END: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.deleteSubdirectoryWithResponse#String-boolean-DataLakeRequestConditions
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
