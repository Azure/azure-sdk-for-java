// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.storage.file.datalake.models.DataLakeRequestConditions;
import com.azure.storage.file.datalake.models.PathHttpHeaders;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;

/**
 * Code snippets for {@link DataLakeDirectoryClient}
 */
@SuppressWarnings({"unused"})
public class DataLakeDirectoryClientJavaDocSamples {
    private String fileName = "fileName";
    private String directoryName = "directoryName";
    private DataLakeDirectoryClient client = JavaDocCodeSnippetsHelpers.getDirectoryClient(directoryName);
    private String leaseId = "leaseId";
    private Duration timeout = Duration.ofSeconds(30);
    private String key1 = "key1";
    private String value1 = "val1";
    private String destinationPath = "destinationPath";

    /**
     * Code snippet for {@link DataLakeDirectoryClient#getSubdirectoryClient(String)}
     */
    public void getDirectoryClient() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryClient.getSubdirectoryClient#String
        DataLakeDirectoryClient dataLakeDirectoryClient = client.getSubdirectoryClient(directoryName);
        // END: com.azure.storage.file.datalake.DataLakeDirectoryClient.getSubdirectoryClient#String
    }

    /**
     * Code snippet for {@link DataLakeDirectoryClient#getFileClient(String)}
     */
    public void getFileClient() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryClient.getFileClient#String
        DataLakeFileClient dataLakeFileClient = client.getFileClient(fileName);
        // END: com.azure.storage.file.datalake.DataLakeDirectoryClient.getFileClient#String
    }

    /**
     * Code snippets for {@link DataLakeDirectoryClient#delete()} and
     * {@link DataLakeDirectoryClient#deleteWithResponse(boolean, DataLakeRequestConditions, Duration, Context)}
     */
    public void deleteCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryClient.delete
        client.delete();
        System.out.println("Delete request completed");
        // END: com.azure.storage.file.datalake.DataLakeDirectoryClient.delete

        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryClient.deleteWithResponse#boolean-DataLakeRequestConditions-Duration-Context
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions()
            .setLeaseId(leaseId);
        boolean recursive = false; // Default value

        client.deleteWithResponse(recursive, requestConditions, timeout, new Context(key1, value1));
        System.out.println("Delete request completed");
        // END: com.azure.storage.file.datalake.DataLakeDirectoryClient.deleteWithResponse#boolean-DataLakeRequestConditions-Duration-Context
    }

    /**
     * Code snippets for {@link DataLakeDirectoryClient#createFile(String)} and
     * {@link DataLakeDirectoryClient#createFileWithResponse(String, String, String, PathHttpHeaders, Map, DataLakeRequestConditions, Duration, Context)}
     */
    public void createFileCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryClient.createFile#String
        DataLakeFileClient fileClient = client.createFile(fileName);
        // END: com.azure.storage.file.datalake.DataLakeDirectoryClient.createFile#String

        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryClient.createFileWithResponse#String-String-String-PathHttpHeaders-Map-DataLakeRequestConditions-Duration-Context
        PathHttpHeaders httpHeaders = new PathHttpHeaders()
            .setContentLanguage("en-US")
            .setContentType("binary");
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions()
            .setLeaseId(leaseId);
        String permissions = "permissions";
        String umask = "umask";
        Response<DataLakeFileClient> newFileClient = client.createFileWithResponse(fileName, permissions, umask, httpHeaders,
            Collections.singletonMap("metadata", "value"), requestConditions,
            timeout, new Context(key1, value1));
        // END: com.azure.storage.file.datalake.DataLakeDirectoryClient.createFileWithResponse#String-String-String-PathHttpHeaders-Map-DataLakeRequestConditions-Duration-Context
    }

    /**
     * Code snippets for {@link DataLakeDirectoryClient#deleteFile(String)} and
     * {@link DataLakeDirectoryClient#deleteFileWithResponse(String, DataLakeRequestConditions, Duration, Context)}
     */
    public void deleteFileCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryClient.deleteFile#String
        client.deleteFile(fileName);
        System.out.println("Delete request completed");
        // END: com.azure.storage.file.datalake.DataLakeDirectoryClient.deleteFile#String

        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryClient.deleteFileWithResponse#String-DataLakeRequestConditions-Duration-Context
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions()
            .setLeaseId(leaseId);

        client.deleteFileWithResponse(fileName, requestConditions, timeout, new Context(key1, value1));
        System.out.println("Delete request completed");
        // END: com.azure.storage.file.datalake.DataLakeDirectoryClient.deleteFileWithResponse#String-DataLakeRequestConditions-Duration-Context
    }

    /**
     * Code snippets for {@link DataLakeDirectoryClient#createSubdirectory(String)} and
     * {@link DataLakeDirectoryClient#createSubdirectoryWithResponse(String, String, String, PathHttpHeaders, Map, DataLakeRequestConditions, Duration, Context)}
     */
    public void createSubdirectoryCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryClient.createSubdirectory#String
        DataLakeDirectoryClient directoryClient = client.createSubdirectory(directoryName);
        // END: com.azure.storage.file.datalake.DataLakeDirectoryClient.createSubdirectory#String

        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryClient.createSubdirectoryWithResponse#String-String-String-PathHttpHeaders-Map-DataLakeRequestConditions-Duration-Context
        PathHttpHeaders httpHeaders = new PathHttpHeaders()
            .setContentLanguage("en-US")
            .setContentType("binary");
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions()
            .setLeaseId(leaseId);
        String permissions = "permissions";
        String umask = "umask";
        Response<DataLakeDirectoryClient> newDirectoryClient = client.createSubdirectoryWithResponse(directoryName,
            permissions, umask, httpHeaders, Collections.singletonMap("metadata", "value"), requestConditions, timeout,
            new Context(key1, value1));
        // END: com.azure.storage.file.datalake.DataLakeDirectoryClient.createSubdirectoryWithResponse#String-String-String-PathHttpHeaders-Map-DataLakeRequestConditions-Duration-Context
    }

    /**
     * Code snippets for {@link DataLakeDirectoryClient#deleteSubdirectory(String)} and
     * {@link DataLakeDirectoryClient#deleteSubdirectoryWithResponse(String, boolean, DataLakeRequestConditions, Duration, Context)}
     */
    public void deleteSubdirectoryCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryClient.deleteSubdirectory#String
        client.deleteSubdirectory(directoryName);
        System.out.println("Delete request completed");
        // END: com.azure.storage.file.datalake.DataLakeDirectoryClient.deleteSubdirectory#String

        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryClient.deleteSubdirectoryWithResponse#String-boolean-DataLakeRequestConditions-Duration-Context
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions()
            .setLeaseId(leaseId);
        boolean recursive = false; // Default value

        client.deleteSubdirectoryWithResponse(directoryName, recursive, requestConditions, timeout,
            new Context(key1, value1));
        System.out.println("Delete request completed");
        // END: com.azure.storage.file.datalake.DataLakeDirectoryClient.deleteSubdirectoryWithResponse#String-boolean-DataLakeRequestConditions-Duration-Context
    }

    /**
     * Code snippets for {@link DataLakeDirectoryClient#rename(String)} and
     * {@link DataLakeDirectoryClient#renameWithResponse(String, DataLakeRequestConditions, DataLakeRequestConditions, Duration, Context)}
     */
    public void renameCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryClient.rename#String
        DataLakeDirectoryClient renamedClient = client.rename(destinationPath);
        System.out.println("Directory Client has been renamed");
        // END: com.azure.storage.file.datalake.DataLakeDirectoryClient.rename#String

        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryClient.renameWithResponse#String-DataLakeRequestConditions-DataLakeRequestConditions-Duration-Context
        DataLakeRequestConditions sourceRequestConditions = new DataLakeRequestConditions()
            .setLeaseId(leaseId);
        DataLakeRequestConditions destinationRequestConditions = new DataLakeRequestConditions();

        DataLakeDirectoryClient newRenamedClient = client.renameWithResponse(destinationPath, sourceRequestConditions,
            destinationRequestConditions, timeout, new Context(key1, value1)).getValue();
        System.out.println("Directory Client has been renamed");
        // END: com.azure.storage.file.datalake.DataLakeDirectoryClient.renameWithResponse#String-DataLakeRequestConditions-DataLakeRequestConditions-Duration-Context
    }
}
