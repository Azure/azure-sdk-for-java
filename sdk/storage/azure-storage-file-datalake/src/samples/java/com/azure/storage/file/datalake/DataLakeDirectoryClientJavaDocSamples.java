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
     * Code snippet for {@link DataLakeDirectoryClient#getSubDirectoryClient(String)}
     */
    public void getDirectoryClient() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryClient.getSubDirectoryClient#String
        DataLakeDirectoryClient dataLakeDirectoryClient = client.getSubDirectoryClient(directoryName);
        // END: com.azure.storage.file.datalake.DataLakeDirectoryClient.getSubDirectoryClient#String
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

        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryClient.createFileWithResponse#String-PathHttpHeaders-Map-DataLakeRequestConditions-String-String-Duration-Context
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
        // END: com.azure.storage.file.datalake.DataLakeDirectoryClient.createFileWithResponse#String-PathHttpHeaders-Map-DataLakeRequestConditions-String-String-Duration-Context
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
     * Code snippets for {@link DataLakeDirectoryClient#createSubDirectory(String)} and
     * {@link DataLakeDirectoryClient#createSubDirectoryWithResponse(String, PathHttpHeaders, Map, DataLakeRequestConditions, String, String, Duration, Context)}
     */
    public void createSubDirectoryCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryClient.createSubDirectory#String
        DataLakeDirectoryClient directoryClient = client.createSubDirectory(directoryName);
        // END: com.azure.storage.file.datalake.DataLakeDirectoryClient.createSubDirectory#String

        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryClient.createSubDirectoryWithResponse#String-PathHttpHeaders-Map-DataLakeRequestConditions-String-String-Duration-Context
        PathHttpHeaders httpHeaders = new PathHttpHeaders()
            .setContentLanguage("en-US")
            .setContentType("binary");
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions()
            .setLeaseId(leaseId);
        String permissions = "permissions";
        String umask = "umask";
        Response<DataLakeDirectoryClient> newDirectoryClient = client.createSubDirectoryWithResponse(directoryName,
            httpHeaders, Collections.singletonMap("metadata", "value"), requestConditions, permissions, umask, timeout,
            new Context(key1, value1));
        // END: com.azure.storage.file.datalake.DataLakeDirectoryClient.createSubDirectoryWithResponse#String-PathHttpHeaders-Map-DataLakeRequestConditions-String-String-Duration-Context
    }

    /**
     * Code snippets for {@link DataLakeDirectoryClient#deleteSubDirectory(String)} and
     * {@link DataLakeDirectoryClient#deleteSubDirectoryWithResponse(String, boolean, DataLakeRequestConditions, Duration, Context)}
     */
    public void deleteSubDirectoryCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryClient.deleteSubDirectory#String
        client.deleteSubDirectory(directoryName);
        System.out.println("Delete request completed");
        // END: com.azure.storage.file.datalake.DataLakeDirectoryClient.deleteSubDirectory#String

        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryClient.deleteSubDirectoryWithResponse#String-boolean-DataLakeRequestConditions-Duration-Context
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions()
            .setLeaseId(leaseId);
        boolean recursive = false; // Default value

        client.deleteSubDirectoryWithResponse(directoryName, recursive, requestConditions, timeout,
            new Context(key1, value1));
        System.out.println("Delete request completed");
        // END: com.azure.storage.file.datalake.DataLakeDirectoryClient.deleteSubDirectoryWithResponse#String-boolean-DataLakeRequestConditions-Duration-Context
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
