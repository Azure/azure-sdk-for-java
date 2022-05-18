// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.storage.file.datalake.models.DataLakeRequestConditions;
import com.azure.storage.file.datalake.models.PathHttpHeaders;
import com.azure.storage.file.datalake.options.DataLakePathCreateOptions;
import com.azure.storage.file.datalake.options.DataLakePathDeleteOptions;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

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
    private String fileSystemName = "fileSystemName";

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
     * Code snippets for {@link DataLakeDirectoryClient#createFile(String)},
     * {@link DataLakeDirectoryClient#createFile(String, boolean)} and
     * {@link DataLakeDirectoryClient#createFileWithResponse(String, String, String, PathHttpHeaders, Map, DataLakeRequestConditions, Duration, Context)}
     */
    public void createFileCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryClient.createFile#String
        DataLakeFileClient fileClient = client.createFile(fileName);
        // END: com.azure.storage.file.datalake.DataLakeDirectoryClient.createFile#String

        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryClient.createFile#String-boolean
        boolean overwrite = false; /* Default value. */
        DataLakeFileClient fClient = client.createFile(fileName, overwrite);
        // END: com.azure.storage.file.datalake.DataLakeDirectoryClient.createFile#String-boolean

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
     * Code snippets for {@link DataLakeDirectoryClient#createFileWithResponse(String, DataLakePathCreateOptions, Duration, Context)}
     */
    public void createFileWithOptionsCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryClient.createFileWithResponse#String-DataLakePathCreateOptions-Duration-Context
        PathHttpHeaders httpHeaders = new PathHttpHeaders()
            .setContentLanguage("en-US")
            .setContentType("binary");
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions()
            .setLeaseId(leaseId);
        Map<String, String> metadata = Collections.singletonMap("metadata", "value");
        String permissions = "permissions";
        String umask = "umask";
        String owner = "rwx";
        String group = "r--";
        String leaseId = UUID.randomUUID().toString();
        Long duration = 15L;
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setPathHttpHeaders(httpHeaders)
            .setRequestConditions(requestConditions).setMetadata(metadata).setPermissions(permissions).setUmask(umask)
            .setOwner(owner).setGroup(group).setProposedLeaseId(leaseId).setLeaseDuration(duration);

        Response<DataLakeFileClient> newFileClient = client.createFileWithResponse(fileName, options, timeout,
            new Context(key1, value1));
        // END: com.azure.storage.file.datalake.DataLakeDirectoryClient.createFileWithResponse#String-DataLakePathCreateOptions-Duration-Context

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
     * Code snippets for {@link DataLakeDirectoryClient#createSubdirectory(String)},
     * {@link DataLakeDirectoryClient#createSubdirectory(String, boolean)} and
     * {@link DataLakeDirectoryClient#createSubdirectoryWithResponse(String, String, String, PathHttpHeaders, Map, DataLakeRequestConditions, Duration, Context)}
     */
    public void createSubdirectoryCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryClient.createSubdirectory#String
        DataLakeDirectoryClient directoryClient = client.createSubdirectory(directoryName);
        // END: com.azure.storage.file.datalake.DataLakeDirectoryClient.createSubdirectory#String

        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryClient.createSubdirectory#String-boolean
        boolean overwrite = false; /* Default value. */
        DataLakeDirectoryClient dClient = client.createSubdirectory(fileName, overwrite);
        // END: com.azure.storage.file.datalake.DataLakeDirectoryClient.createSubdirectory#String-boolean

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
     * Code snippets for {@link DataLakeDirectoryClient#createSubdirectoryWithResponse(String, DataLakePathCreateOptions, Duration, Context)}
     */
    public void createSubdirectoryWithOptionsCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryClient.createSubdirectoryWithResponse#String-DataLakePathCreateOptions-Duration-Context
        PathHttpHeaders httpHeaders = new PathHttpHeaders()
            .setContentLanguage("en-US")
            .setContentType("binary");
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions()
            .setLeaseId(leaseId);
        Map<String, String> metadata = Collections.singletonMap("metadata", "value");
        String permissions = "permissions";
        String umask = "umask";
        String owner = "rwx";
        String group = "r--";
        String leaseId = UUID.randomUUID().toString();
        Long duration = 15L;
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setPathHttpHeaders(httpHeaders)
            .setRequestConditions(requestConditions).setMetadata(metadata).setPermissions(permissions).setUmask(umask)
            .setOwner(owner).setGroup(group).setProposedLeaseId(leaseId).setLeaseDuration(duration);

        Response<DataLakeDirectoryClient> newDirectoryClient = client.createSubdirectoryWithResponse(directoryName,
            options, timeout, new Context(key1, value1));
        // END: com.azure.storage.file.datalake.DataLakeDirectoryClient.createSubdirectoryWithResponse#String-DataLakePathCreateOptions-Duration-Context


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
     * Code snippets for {@link DataLakeDirectoryClient#rename(String, String)} and
     * {@link DataLakeDirectoryClient#renameWithResponse(String, String, DataLakeRequestConditions, DataLakeRequestConditions, Duration, Context)}
     */
    public void renameCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryClient.rename#String-String
        DataLakeDirectoryClient renamedClient = client.rename(fileSystemName, destinationPath);
        System.out.println("Directory Client has been renamed");
        // END: com.azure.storage.file.datalake.DataLakeDirectoryClient.rename#String-String

        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryClient.renameWithResponse#String-String-DataLakeRequestConditions-DataLakeRequestConditions-Duration-Context
        DataLakeRequestConditions sourceRequestConditions = new DataLakeRequestConditions()
            .setLeaseId(leaseId);
        DataLakeRequestConditions destinationRequestConditions = new DataLakeRequestConditions();

        DataLakeDirectoryClient newRenamedClient = client.renameWithResponse(fileSystemName, destinationPath,
            sourceRequestConditions, destinationRequestConditions, timeout, new Context(key1, value1)).getValue();
        System.out.println("Directory Client has been renamed");
        // END: com.azure.storage.file.datalake.DataLakeDirectoryClient.renameWithResponse#String-String-DataLakeRequestConditions-DataLakeRequestConditions-Duration-Context
    }

    /**
     * Code snippets for {@link DataLakeDirectoryClient#listPaths()} and
     * {@link DataLakeDirectoryClient#listPaths(boolean, boolean, Integer, Duration)}
     */
    public void listPaths() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryClient.listPaths
        client.listPaths().forEach(path -> System.out.printf("Name: %s%n", path.getName()));
        // END: com.azure.storage.file.datalake.DataLakeDirectoryClient.listPaths

        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryClient.listPaths#boolean-boolean-Integer-Duration
        client.listPaths(false, false, 10, timeout)
            .forEach(path -> System.out.printf("Name: %s%n", path.getName()));
        // END: com.azure.storage.file.datalake.DataLakeDirectoryClient.listPaths#boolean-boolean-Integer-Duration
    }

    /**
     * Code snippets for {@link DataLakeDirectoryClient#deleteIfExists()} and
     * {@link DataLakeDirectoryClient#deleteIfExistsWithResponse(DataLakePathDeleteOptions, Duration, Context)}
     */
    public void deleteIfExistsCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryClient.deleteIfExists
        boolean result = client.deleteIfExists();
        System.out.println("Delete request completed: " + result);
        // END: com.azure.storage.file.datalake.DataLakeDirectoryClient.deleteIfExists

        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryClient.deleteIfExistsWithResponse#DataLakePathDeleteOptions-Duration-Context
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions()
            .setLeaseId(leaseId);
        boolean recursive = false; // Default value
        DataLakePathDeleteOptions options = new DataLakePathDeleteOptions().setIsRecursive(recursive)
            .setRequestConditions(requestConditions);

        Response<Void> response = client.deleteIfExistsWithResponse(options, timeout, new Context(key1, value1));
        if (response.getStatusCode() == 404) {
            System.out.println("Does not exist.");
        } else {
            System.out.printf("Delete completed with status %d%n", response.getStatusCode());
        }
        // END: com.azure.storage.file.datalake.DataLakeDirectoryClient.deleteIfExistsWithResponse#DataLakePathDeleteOptions-Duration-Context
    }

    /**
     * Code snippets for {@link DataLakeDirectoryClient#createFileIfNotExists(String)} and
     * {@link DataLakeDirectoryClient#createFileIfNotExistsWithResponse(String, DataLakePathCreateOptions, Duration, Context)}
     */
    public void createFileIfNotExistsCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryClient.createFileIfNotExists#String
        DataLakeFileClient fileClient = client.createFileIfNotExists(fileName);
        // END: com.azure.storage.file.datalake.DataLakeDirectoryClient.createFileIfNotExists#String

        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryClient.createFileIfNotExistsWithResponse#String-DataLakePathCreateOptions-Duration-Context
        PathHttpHeaders headers = new PathHttpHeaders().setContentLanguage("en-US").setContentType("binary");
        String permissions = "permissions";
        String umask = "umask";
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setPathHttpHeaders(headers)
            .setPermissions(permissions).setUmask(umask).setMetadata(Collections.singletonMap("metadata", "value"));

        Response<DataLakeFileClient> response = client.createFileIfNotExistsWithResponse(fileName, options, timeout,
            new Context(key1, value1));
        if (response.getStatusCode() == 409) {
            System.out.println("Already existed.");
        } else {
            System.out.printf("Create completed with status %d%n", response.getStatusCode());
        }
        // END: com.azure.storage.file.datalake.DataLakeDirectoryClient.createFileIfNotExistsWithResponse#String-DataLakePathCreateOptions-Duration-Context
    }

    /**
     * Code snippets for {@link DataLakeDirectoryClient#deleteFileIfExists(String)} and
     * {@link DataLakeDirectoryClient#deleteFileIfExistsWithResponse(String, DataLakePathDeleteOptions, Duration, Context)}
     */
    public void deleteFileIfExistsCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryClient.deleteFileIfExists#String
        boolean result = client.deleteFileIfExists(fileName);
        System.out.println("Delete request completed: " + result);
        // END: com.azure.storage.file.datalake.DataLakeDirectoryClient.deleteFileIfExists#String

        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryClient.deleteFileIfExistsWithResponse#String-DataLakePathDeleteOptions-Duration-Context
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions()
            .setLeaseId(leaseId);
        DataLakePathDeleteOptions options = new DataLakePathDeleteOptions().setIsRecursive(false)
            .setRequestConditions(requestConditions);

        Response<Void> response = client.deleteFileIfExistsWithResponse(fileName, options, timeout,
            new Context(key1, value1));
        if (response.getStatusCode() == 404) {
            System.out.println("Does not exist.");
        } else {
            System.out.printf("Delete completed with status %d%n", response.getStatusCode());
        }
        // END: com.azure.storage.file.datalake.DataLakeDirectoryClient.deleteFileIfExistsWithResponse#String-DataLakePathDeleteOptions-Duration-Context
    }

    /**
     * Code snippets for {@link DataLakeDirectoryClient#createSubdirectoryIfNotExists(String)} and
     * {@link DataLakeDirectoryClient#createSubdirectoryIfNotExistsWithResponse(String, DataLakePathCreateOptions, Duration, Context)}
     */
    public void createSubdirectoryIfNotExistsCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryClient.createSubdirectoryIfNotExists#String
        DataLakeDirectoryClient directoryClient = client.createSubdirectoryIfNotExists(directoryName);
        // END: com.azure.storage.file.datalake.DataLakeDirectoryClient.createSubdirectoryIfNotExists#String

        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryClient.createSubdirectoryIfNotExistsWithResponse#String-DataLakePathCreateOptions-Duration-Context
        PathHttpHeaders headers = new PathHttpHeaders()
            .setContentLanguage("en-US")
            .setContentType("binary");
        String permissions = "permissions";
        String umask = "umask";
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setPathHttpHeaders(headers)
            .setPermissions(permissions).setUmask(umask).setMetadata(Collections.singletonMap("metadata", "value"));

        Response<DataLakeDirectoryClient> response = client.createSubdirectoryIfNotExistsWithResponse(directoryName,
            options, timeout, new Context(key1, value1));
        if (response.getStatusCode() == 409) {
            System.out.println("Already existed.");
        } else {
            System.out.printf("Create completed with status %d%n", response.getStatusCode());
        }
        // END: com.azure.storage.file.datalake.DataLakeDirectoryClient.createSubdirectoryIfNotExistsWithResponse#String-DataLakePathCreateOptions-Duration-Context
    }

    /**
     * Code snippets for {@link DataLakeDirectoryClient#deleteSubdirectoryIfExists(String)}  and
     * {@link DataLakeDirectoryClient#deleteSubdirectoryIfExistsWithResponse(String, DataLakePathDeleteOptions, Duration, Context)}
     */
    public void deleteSubdirectoryIfExistsCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryClient.deleteSubdirectoryIfExists#String
        boolean result = client.deleteSubdirectoryIfExists(directoryName);
        System.out.println("Delete request completed: " + result);
        // END: com.azure.storage.file.datalake.DataLakeDirectoryClient.deleteSubdirectoryIfExists#String

        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryClient.deleteSubdirectoryIfExistsWithResponse#String-DataLakePathDeleteOptions-Duration-Context
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions()
            .setLeaseId(leaseId);
        boolean recursive = false; // Default value
        DataLakePathDeleteOptions options = new DataLakePathDeleteOptions().setIsRecursive(recursive)
            .setRequestConditions(requestConditions);

        Response<Void> response = client.deleteSubdirectoryIfExistsWithResponse(directoryName, options,
            timeout, new Context(key1, value1));
        if (response.getStatusCode() == 404) {
            System.out.println("Does not exist.");
        } else {
            System.out.printf("Delete completed with status %d%n", response.getStatusCode());
        }
        // END: com.azure.storage.file.datalake.DataLakeDirectoryClient.deleteSubdirectoryIfExistsWithResponse#String-DataLakePathDeleteOptions-Duration-Context
    }
}
