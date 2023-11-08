// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import com.azure.storage.file.datalake.models.DataLakeRequestConditions;
import com.azure.storage.file.datalake.models.PathHttpHeaders;
import com.azure.storage.file.datalake.options.DataLakePathCreateOptions;
import com.azure.storage.file.datalake.options.DataLakePathDeleteOptions;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

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
    private String fileSystemName = "fileSystemName";
    private String key1 = "key1";
    private String value1 = "val1";

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
     * Code snippets for {@link DataLakeDirectoryAsyncClient#delete()},
     * {@link DataLakeDirectoryAsyncClient#deleteWithResponse(boolean, DataLakeRequestConditions)},
     * {@link DataLakeDirectoryAsyncClient#deleteRecursively()} and
     * {@link DataLakeDirectoryAsyncClient#deleteRecursivelyWithResponse(DataLakeRequestConditions)}
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

        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.deleteRecursively
        client.deleteRecursively().subscribe(response ->
            System.out.println("Delete request completed"));
        // END: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.deleteRecursively

        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.deleteRecursivelyWithResponse#DataLakeRequestConditions
        DataLakeRequestConditions deleteRequestConditions = new DataLakeRequestConditions()
            .setLeaseId(leaseId);
        client.deleteRecursivelyWithResponse(deleteRequestConditions)
            .subscribe(response -> System.out.println("Delete request completed"));
        // END: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.deleteRecursivelyWithResponse#DataLakeRequestConditions
    }

    /**
     * Code snippets for {@link DataLakeDirectoryAsyncClient#createFile(String)},
     * {@link DataLakeDirectoryAsyncClient#createFile(String, boolean)} and
     * {@link DataLakeDirectoryAsyncClient#createFileWithResponse(String, String, String, PathHttpHeaders, Map, DataLakeRequestConditions)}
     */
    public void createFileCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.createFile#String
        DataLakeFileAsyncClient fileClient = client.createFile(fileName).block();
        // END: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.createFile#String

        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.createFile#String-boolean
        boolean overwrite = false; /* Default value. */
        DataLakeFileAsyncClient fClient = client.createFile(fileName, overwrite).block();
        // END: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.createFile#String-boolean

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
     * Code snippets for {@link DataLakeDirectoryAsyncClient#createFileWithResponse(String, DataLakePathCreateOptions)}
     */
    public void createFileWithOptionsCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.createFileWithResponse#String-DataLakePathCreateOptions
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
        Integer duration = 15;
        DataLakePathCreateOptions options = new DataLakePathCreateOptions()
            .setPermissions(permissions)
            .setUmask(umask)
            .setOwner(owner)
            .setGroup(group)
            .setPathHttpHeaders(httpHeaders)
            .setRequestConditions(requestConditions)
            .setMetadata(metadata)
            .setProposedLeaseId(leaseId)
            .setLeaseDuration(duration);

        DataLakeFileAsyncClient newFileClient = client.createFileWithResponse(fileName, options).block().getValue();
        // END: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.createFileWithResponse#String-DataLakePathCreateOptions

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
     * Code snippets for {@link DataLakeDirectoryAsyncClient#createSubdirectory(String)},
     * {@link DataLakeDirectoryAsyncClient#createSubdirectory(String, boolean)} and
     * {@link DataLakeDirectoryAsyncClient#createSubdirectoryWithResponse(String, String, String, PathHttpHeaders, Map, DataLakeRequestConditions)}
     */
    public void createSubdirectoryCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.createSubdirectory#String
        DataLakeDirectoryAsyncClient directoryClient = client.createSubdirectory(directoryName).block();
        // END: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.createSubdirectory#String

        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.createSubdirectory#String-boolean
        boolean overwrite = false; /* Default value. */
        DataLakeDirectoryAsyncClient dClient = client.createSubdirectory(directoryName, overwrite).block();
        // END: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.createSubdirectory#String-boolean

        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.createSubdirectoryWithResponse#String-String-String-PathHttpHeaders-Map-DataLakeRequestConditions
        PathHttpHeaders httpHeaders = new PathHttpHeaders()
            .setContentLanguage("en-US")
            .setContentType("binary");
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions()
            .setLeaseId(leaseId);
        String permissions = "permissions";
        String umask = "umask";
        DataLakeDirectoryAsyncClient newDirectoryClient = client.createSubdirectoryWithResponse(
            directoryName, permissions, umask, httpHeaders, Collections.singletonMap("metadata", "value"),
            requestConditions
        ).block().getValue();
        // END: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.createSubdirectoryWithResponse#String-String-String-PathHttpHeaders-Map-DataLakeRequestConditions
    }

    /**
     * Code snippets for {@link DataLakeDirectoryAsyncClient#createSubdirectoryIfNotExistsWithResponse(String, DataLakePathCreateOptions)}
     */
    public void createSubdirectoryWithOptionsCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.createSubdirectoryWithResponse#String-DataLakePathCreateOptions
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
        Integer duration = 15;
        DataLakePathCreateOptions options = new DataLakePathCreateOptions()
            .setPermissions(permissions)
            .setUmask(umask)
            .setOwner(owner)
            .setGroup(group)
            .setPathHttpHeaders(httpHeaders)
            .setRequestConditions(requestConditions)
            .setMetadata(metadata)
            .setProposedLeaseId(leaseId)
            .setLeaseDuration(duration);

        DataLakeDirectoryAsyncClient newDirectoryClient = client.createSubdirectoryWithResponse(directoryName, options)
            .block().getValue();
        // END: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.createSubdirectoryWithResponse#String-DataLakePathCreateOptions


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
     * Code snippets for {@link DataLakeDirectoryAsyncClient#rename(String, String)} and
     * {@link DataLakeDirectoryAsyncClient#renameWithResponse(String, String, DataLakeRequestConditions, DataLakeRequestConditions)}
     */
    public void renameCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.rename#String-String
        DataLakeDirectoryAsyncClient renamedClient = client.rename(fileSystemName, destinationPath).block();
        System.out.println("Directory Client has been renamed");
        // END: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.rename#String-String

        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.renameWithResponse#String-String-DataLakeRequestConditions-DataLakeRequestConditions
        DataLakeRequestConditions sourceRequestConditions = new DataLakeRequestConditions()
            .setLeaseId(leaseId);
        DataLakeRequestConditions destinationRequestConditions = new DataLakeRequestConditions();

        DataLakeDirectoryAsyncClient newRenamedClient = client.renameWithResponse(fileSystemName, destinationPath,
            sourceRequestConditions, destinationRequestConditions).block().getValue();
        System.out.println("Directory Client has been renamed");
        // END: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.renameWithResponse#String-String-DataLakeRequestConditions-DataLakeRequestConditions
    }

    /**
     * Code snippets for {@link DataLakeDirectoryAsyncClient#listPaths()} and
     * {@link DataLakeDirectoryAsyncClient#listPaths(boolean, boolean, Integer)}
     */
    public void listPaths() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.listPaths
        client.listPaths().subscribe(path -> System.out.printf("Name: %s%n", path.getName()));
        // END: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.listPaths

        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.listPaths#boolean-boolean-Integer
        client.listPaths(false, false, 10)
            .subscribe(path -> System.out.printf("Name: %s%n", path.getName()));
        // END: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.listPaths#boolean-boolean-Integer
    }

    /**
     * Code snippets for {@link DataLakeDirectoryAsyncClient#deleteIfExists()} and
     * {@link DataLakeDirectoryAsyncClient#deleteIfExistsWithResponse(DataLakePathDeleteOptions)}
     */
    public void deleteIfExistsCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.deleteIfExists
        client.deleteIfExists().subscribe(deleted -> {
            if (deleted) {
                System.out.println("Successfully deleted.");
            } else {
                System.out.println("Does not exist.");
            }
        });
        // END: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.deleteIfExists

        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.deleteIfExistsWithResponse#DataLakePathDeleteOptions
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions()
            .setLeaseId(leaseId);
        boolean recursive = false; // Default value
        DataLakePathDeleteOptions options = new DataLakePathDeleteOptions().setIsRecursive(recursive)
            .setRequestConditions(requestConditions);

        client.deleteIfExistsWithResponse(options).subscribe(response -> {
            if (response.getStatusCode() == 404) {
                System.out.println("Does not exist.");
            } else {
                System.out.println("successfully deleted.");
            }
        });
        // END: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.deleteIfExistsWithResponse#DataLakePathDeleteOptions
    }

    /**
     * Code snippets for {@link DataLakeDirectoryAsyncClient#createFileIfNotExists(String)} and
     * {@link DataLakeDirectoryAsyncClient#createFileIfNotExistsWithResponse(String, DataLakePathCreateOptions)}
     */
    public void createFileIfNotExistsCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.createFileIfNotExists#String
        DataLakeFileAsyncClient fileClient = client.createFileIfNotExists(fileName).block();
        // END: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.createFileIfNotExists#String

        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.createFileIfNotExistsWithResponse#String-DataLakePathCreateOptions
        PathHttpHeaders headers = new PathHttpHeaders()
            .setContentLanguage("en-US")
            .setContentType("binary");
        String permissions = "permissions";
        String umask = "umask";
        DataLakePathCreateOptions options = new DataLakePathCreateOptions()
            .setPermissions(permissions)
            .setUmask(umask)
            .setPathHttpHeaders(headers)
            .setMetadata(Collections.singletonMap("metadata", "value"));

        client.createFileIfNotExistsWithResponse(fileName, options).subscribe(response -> {
            if (response.getStatusCode() == 409) {
                System.out.println("Already exists.");
            } else {
                System.out.println("successfully created.");
            }
        });
        // END: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.createFileIfNotExistsWithResponse#String-DataLakePathCreateOptions
    }

    /**
     * Code snippets for {@link DataLakeDirectoryAsyncClient#deleteFile(String)} and
     * {@link DataLakeDirectoryAsyncClient#deleteFileIfExistsWithResponse(String, DataLakePathDeleteOptions)}
     */
    public void deleteFileIfExistsCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.deleteFileIfExists#String
        client.deleteFileIfExists(fileName).subscribe(deleted -> {
            if (deleted) {
                System.out.println("successfully deleted.");
            } else {
                System.out.println("Does not exist.");
            }
        });
        // END: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.deleteFileIfExists#String

        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.deleteFileIfExistsWithResponse#String-DataLakePathDeleteOptions
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions()
            .setLeaseId(leaseId);
        DataLakePathDeleteOptions options = new DataLakePathDeleteOptions().setIsRecursive(false)
            .setRequestConditions(requestConditions);

        client.deleteFileIfExistsWithResponse(fileName, options).subscribe(response -> {
            if (response.getStatusCode() == 404) {
                System.out.println("Does not exist.");
            } else {
                System.out.println("successfully deleted.");
            }
        });
        // END: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.deleteFileIfExistsWithResponse#String-DataLakePathDeleteOptions
    }

    /**
     * Code snippets for {@link DataLakeDirectoryAsyncClient#createSubdirectoryIfNotExists(String)} and
     * {@link DataLakeDirectoryAsyncClient#createSubdirectoryIfNotExistsWithResponse(String, DataLakePathCreateOptions)}
     */
    public void createSubdirectoryIfNotExistsCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.createSubdirectoryIfNotExists#String
        DataLakeDirectoryAsyncClient subdirectoryClient = client.createSubdirectoryIfNotExists(directoryName).block();
        // END: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.createSubdirectoryIfNotExists#String

        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.createSubdirectoryIfNotExistsWithResponse#String-DataLakePathCreateOptions
        PathHttpHeaders headers = new PathHttpHeaders()
            .setContentLanguage("en-US")
            .setContentType("binary");
        String permissions = "permissions";
        String umask = "umask";
        DataLakePathCreateOptions options = new DataLakePathCreateOptions()
            .setPermissions(permissions)
            .setUmask(umask)
            .setPathHttpHeaders(headers)
            .setMetadata(Collections.singletonMap("metadata", "value"));

        client.createSubdirectoryIfNotExistsWithResponse(directoryName, options).subscribe(response -> {
            if (response.getStatusCode() == 409) {
                System.out.println("Already exists.");
            } else {
                System.out.println("successfully created.");
            }
        });
        // END: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.createSubdirectoryIfNotExistsWithResponse#String-DataLakePathCreateOptions
    }

    /**
     * Code snippets for {@link DataLakeDirectoryAsyncClient#deleteSubdirectoryIfExists(String)} and
     * {@link DataLakeDirectoryAsyncClient#deleteSubdirectoryIfExistsWithResponse(String, DataLakePathDeleteOptions)}
     */
    public void deleteSubdirectoryIfExistsCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.deleteSubdirectoryIfExists#String
        client.deleteSubdirectoryIfExists(directoryName).subscribe(deleted -> {
            if (deleted) {
                System.out.println("Successfully deleted.");
            } else {
                System.out.println("Does not exist.");
            }
        });
        // END: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.deleteSubdirectoryIfExists#String

        // BEGIN: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.deleteSubdirectoryIfExistsWithResponse#String-DataLakePathDeleteOptions
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions()
            .setLeaseId(leaseId);
        boolean recursive = false; // Default value
        DataLakePathDeleteOptions options = new DataLakePathDeleteOptions().setIsRecursive(recursive)
            .setRequestConditions(requestConditions);

        client.deleteSubdirectoryIfExistsWithResponse(directoryName, options).subscribe(response -> {
            if (response.getStatusCode() == 404) {
                System.out.println("Does not exist.");
            } else {
                System.out.println("successfully deleted.");
            }
        });
        // END: com.azure.storage.file.datalake.DataLakeDirectoryAsyncClient.deleteSubdirectoryIfExistsWithResponse#String-DataLakePathDeleteOptions
    }
}
