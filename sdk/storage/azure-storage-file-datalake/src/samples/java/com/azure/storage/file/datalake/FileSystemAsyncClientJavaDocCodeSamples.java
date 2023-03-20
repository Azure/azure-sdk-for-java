// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.storage.file.datalake.models.DataLakeAccessPolicy;
import com.azure.storage.file.datalake.models.DataLakeRequestConditions;
import com.azure.storage.file.datalake.models.DataLakeSignedIdentifier;
import com.azure.storage.file.datalake.models.ListPathsOptions;
import com.azure.storage.file.datalake.models.PathHttpHeaders;
import com.azure.storage.file.datalake.models.PublicAccessType;
import com.azure.storage.file.datalake.models.UserDelegationKey;
import com.azure.storage.file.datalake.options.DataLakePathCreateOptions;
import com.azure.storage.file.datalake.options.DataLakePathDeleteOptions;
import com.azure.storage.file.datalake.sas.DataLakeServiceSasSignatureValues;
import com.azure.storage.file.datalake.sas.FileSystemSasPermission;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
    private String accountName = "accountName";
    private UserDelegationKey userDelegationKey = JavaDocCodeSnippetsHelpers.getUserDelegationKey();

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
     * Code snippet for {@link DataLakeFileSystemAsyncClient#getRootDirectoryAsyncClient}
     */
    public void getRootDirectoryAsyncClient() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.getRootDirectoryAsyncClient
        DataLakeDirectoryAsyncClient dataLakeDirectoryAsyncClient =
            client.getDirectoryAsyncClient(DataLakeFileSystemAsyncClient.ROOT_DIRECTORY_NAME);
        // END: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.getRootDirectoryAsyncClient
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
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions()
            .setLeaseId(leaseId)
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));

        client.deleteWithResponse(requestConditions).subscribe(response ->
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
                response.getDataLakePublicAccess(),
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
                response.getValue().getDataLakePublicAccess(),
                response.getValue().hasLegalHold(),
                response.getValue().hasImmutabilityPolicy()));
        // END: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.getPropertiesWithResponse#String
    }

    /**
     * Code snippet for {@link DataLakeFileSystemAsyncClient#exists()}
     */
    public void existsCodeSnippet() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.exists
        client.exists().subscribe(response -> System.out.printf("Exists? %b%n", response));
        // END: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.exists
    }

    /**
     * Code snippet for {@link DataLakeFileSystemAsyncClient#existsWithResponse()}
     */
    public void existsWithResponseCodeSnippet() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.existsWithResponse
        client.existsWithResponse().subscribe(response -> System.out.printf("Exists? %b%n", response.getValue()));
        // END: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.existsWithResponse
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
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions()
            .setLeaseId(leaseId)
            .setIfModifiedSince(OffsetDateTime.now().minusDays(3));

        client.setMetadataWithResponse(metadata, requestConditions).subscribe(response ->
            System.out.printf("Set metadata completed with status %d%n", response.getStatusCode()));
        // END: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.setMetadataWithResponse#Map-DataLakeRequestConditions
    }

    /**
     * Code snippets for {@link DataLakeFileSystemAsyncClient#createFile(String)},
     * {@link DataLakeFileSystemAsyncClient#createFile(String, boolean)} and
     * {@link DataLakeFileSystemAsyncClient#createFileWithResponse(String, String, String, PathHttpHeaders, Map, DataLakeRequestConditions)}
     */
    public void createFileCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.createFile#String
        Mono<DataLakeFileAsyncClient> fileClient = client.createFile(fileName);
        // END: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.createFile#String

        // BEGIN: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.createFile#String-boolean
        boolean overwrite = false; /* Default value. */
        Mono<DataLakeFileAsyncClient> fClient = client.createFile(fileName, overwrite);
        // END: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.createFile#String-boolean

        // BEGIN: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.createFileWithResponse#String-String-String-PathHttpHeaders-Map-DataLakeRequestConditions
        PathHttpHeaders httpHeaders = new PathHttpHeaders()
            .setContentLanguage("en-US")
            .setContentType("binary");
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions()
            .setLeaseId(leaseId);
        String permissions = "permissions";
        String umask = "umask";
        Mono<Response<DataLakeFileAsyncClient>> newFileClient = client.createFileWithResponse(fileName, permissions,
            umask, httpHeaders, Collections.singletonMap("metadata", "value"), requestConditions);
        // END: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.createFileWithResponse#String-String-String-PathHttpHeaders-Map-DataLakeRequestConditions
    }

    /**
     * Code snippets for {@link DataLakeFileSystemAsyncClient#createFileWithResponse(String, DataLakePathCreateOptions)}
     */
    public void createFileWithOptionsCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.createFileWithResponse#String-DataLakePathCreateOptions
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

        Mono<Response<DataLakeFileAsyncClient>> newFileClient = client.createFileWithResponse(fileName, options);
        // END: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.createFileWithResponse#String-DataLakePathCreateOptions
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
     * Code snippets for {@link DataLakeFileSystemAsyncClient#createDirectory(String)},
     * {@link DataLakeFileSystemAsyncClient#createDirectory(String, boolean)} and
     * {@link DataLakeFileSystemAsyncClient#createDirectoryWithResponse(String, String, String, PathHttpHeaders, Map, DataLakeRequestConditions)}
     */
    public void createDirectoryCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.createDirectory#String
        Mono<DataLakeDirectoryAsyncClient> directoryClient = client.createDirectory(directoryName);
        // END: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.createDirectory#String

        // BEGIN: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.createDirectory#String-boolean
        boolean overwrite = false; /* Default value. */
        Mono<DataLakeDirectoryAsyncClient> dClient = client.createDirectory(directoryName, overwrite);
        // END: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.createDirectory#String-boolean

        // BEGIN: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.createDirectoryWithResponse#String-String-String-PathHttpHeaders-Map-DataLakeRequestConditions
        PathHttpHeaders httpHeaders = new PathHttpHeaders()
            .setContentLanguage("en-US")
            .setContentType("binary");
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions()
            .setLeaseId(leaseId);
        String permissions = "permissions";
        String umask = "umask";
        Mono<Response<DataLakeDirectoryAsyncClient>> newDirectoryClient = client.createDirectoryWithResponse(
            directoryName, permissions, umask, httpHeaders, Collections.singletonMap("metadata", "value"),
            requestConditions);
        // END: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.createDirectoryWithResponse#String-String-String-PathHttpHeaders-Map-DataLakeRequestConditions
    }

    /**
     * Code snippets for {@link DataLakeFileSystemAsyncClient#createDirectoryWithResponse(String, DataLakePathCreateOptions)}
     */
    public void createDirectoryWithOptionsCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.createDirectoryWithResponse#String-DataLakePathCreateOptions
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

        Mono<Response<DataLakeDirectoryAsyncClient>> newDirectoryClient = client.createDirectoryWithResponse(
            directoryName, options);
        // END: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.createDirectoryWithResponse#String-DataLakePathCreateOptions
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
     * Code snippet for {@link DataLakeFileSystemAsyncClient#undeletePath(String, String)}
     */
    public void restorePathCodeSnippet() {
        String deletedPath = null;
        String deletionId = null;

        // BEGIN: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.undeletePath#String-String
        client.undeletePath(deletedPath, deletionId).doOnSuccess(response -> System.out.println("Completed undelete"));
        // END: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.undeletePath#String-String

        // BEGIN: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.undeletePathWithResponse#String-String
        client.undeletePathWithResponse(deletedPath, deletionId)
            .doOnSuccess(response -> System.out.println("Completed undelete"));
        // END: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.undeletePathWithResponse#String-String
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

    /**
     * Code snippets for {@link DataLakeFileSystemAsyncClient#listDeletedPaths()} and
     * {@link DataLakeFileSystemAsyncClient#listDeletedPaths(String)}
     */
    public void listDeletedPaths() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.listDeletedPaths
        client.listDeletedPaths().subscribe(path -> System.out.printf("Name: %s%n", path.getPath()));
        // END: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.listDeletedPaths

        // BEGIN: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.listDeletedPaths#String
        int pageSize = 10;
        client.listDeletedPaths("PathNamePrefixToMatch")
            .byPage(pageSize)
            .subscribe(page ->
                page.getValue().forEach(path ->
                    System.out.printf("Name: %s%n", path.getPath())));
        // END: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.listDeletedPaths#String
    }

    /**
     * Code snippet for {@link DataLakeFileSystemAsyncClient#getAccessPolicy()}
     */
    public void getAccessPolicy() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.getAccessPolicy
        client.getAccessPolicy().subscribe(response -> {
            System.out.printf("Data Lake Access Type: %s%n", response.getDataLakeAccessType());

            for (DataLakeSignedIdentifier identifier : response.getIdentifiers()) {
                System.out.printf("Identifier Name: %s, Permissions %s%n",
                    identifier.getId(),
                    identifier.getAccessPolicy().getPermissions());
            }
        });
        // END: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.getAccessPolicy
    }

    /**
     * Code snippet for {@link DataLakeFileSystemAsyncClient#getAccessPolicyWithResponse(String)}
     */
    public void getAccessPolicy2() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.getAccessPolicyWithResponse#String
        client.getAccessPolicyWithResponse(leaseId).subscribe(response -> {
            System.out.printf("Data Lake Access Type: %s%n", response.getValue().getDataLakeAccessType());

            for (DataLakeSignedIdentifier identifier : response.getValue().getIdentifiers()) {
                System.out.printf("Identifier Name: %s, Permissions %s%n",
                    identifier.getId(),
                    identifier.getAccessPolicy().getPermissions());
            }
        });
        // END: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.getAccessPolicyWithResponse#String
    }

    /**
     * Code snippet for {@link DataLakeFileSystemAsyncClient#setAccessPolicy(PublicAccessType, List)}
     */
    public void setAccessPolicy() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.setAccessPolicy#PublicAccessType-List
        DataLakeSignedIdentifier identifier = new DataLakeSignedIdentifier()
            .setId("name")
            .setAccessPolicy(new DataLakeAccessPolicy()
                .setStartsOn(OffsetDateTime.now())
                .setExpiresOn(OffsetDateTime.now().plusDays(7))
                .setPermissions("permissionString"));

        client.setAccessPolicy(PublicAccessType.CONTAINER, Collections.singletonList(identifier)).subscribe(
            response -> System.out.printf("Set access policy completed%n"),
            error -> System.out.printf("Set access policy failed: %s%n", error));
        // END: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.setAccessPolicy#PublicAccessType-List
    }

    /**
     * Code snippet for {@link DataLakeFileSystemAsyncClient#setAccessPolicyWithResponse(PublicAccessType, List, DataLakeRequestConditions)}
     */
    public void setAccessPolicy2() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.setAccessPolicyWithResponse#PublicAccessType-List-DataLakeRequestConditions
        DataLakeSignedIdentifier identifier = new DataLakeSignedIdentifier()
            .setId("name")
            .setAccessPolicy(new DataLakeAccessPolicy()
                .setStartsOn(OffsetDateTime.now())
                .setExpiresOn(OffsetDateTime.now().plusDays(7))
                .setPermissions("permissionString"));

        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions()
            .setLeaseId(leaseId)
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));

        client.setAccessPolicyWithResponse(PublicAccessType.CONTAINER, Collections.singletonList(identifier), requestConditions)
            .subscribe(response ->
                System.out.printf("Set access policy completed with status %d%n", response.getStatusCode()));
        // END: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.setAccessPolicyWithResponse#PublicAccessType-List-DataLakeRequestConditions
    }

    /**
     * Code snippet for {@link DataLakeFileSystemAsyncClient#generateUserDelegationSas(DataLakeServiceSasSignatureValues, UserDelegationKey)}
     * and {@link DataLakeFileSystemAsyncClient#generateSas(DataLakeServiceSasSignatureValues)}
     */
    public void generateSas() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.generateSas#DataLakeServiceSasSignatureValues
        OffsetDateTime expiryTime = OffsetDateTime.now().plusDays(1);
        FileSystemSasPermission permission = new FileSystemSasPermission().setReadPermission(true);

        DataLakeServiceSasSignatureValues values = new DataLakeServiceSasSignatureValues(expiryTime, permission)
            .setStartTime(OffsetDateTime.now());

        client.generateSas(values); // Client must be authenticated via StorageSharedKeyCredential
        // END: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.generateSas#DataLakeServiceSasSignatureValues

        // BEGIN: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.generateUserDelegationSas#DataLakeServiceSasSignatureValues-UserDelegationKey
        OffsetDateTime myExpiryTime = OffsetDateTime.now().plusDays(1);
        FileSystemSasPermission myPermission = new FileSystemSasPermission().setReadPermission(true);

        DataLakeServiceSasSignatureValues myValues = new DataLakeServiceSasSignatureValues(expiryTime, permission)
            .setStartTime(OffsetDateTime.now());

        client.generateUserDelegationSas(values, userDelegationKey);
        // END: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.generateUserDelegationSas#DataLakeServiceSasSignatureValues-UserDelegationKey
    }

    /**
     * Code snippet for {@link DataLakeFileSystemAsyncClient#generateUserDelegationSas(DataLakeServiceSasSignatureValues, UserDelegationKey, String, Context)}
     * and {@link DataLakeFileSystemAsyncClient#generateSas(DataLakeServiceSasSignatureValues, Context)}
     */
    public void generateSasWithContext() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.generateSas#DataLakeServiceSasSignatureValues-Context
        OffsetDateTime expiryTime = OffsetDateTime.now().plusDays(1);
        FileSystemSasPermission permission = new FileSystemSasPermission().setReadPermission(true);

        DataLakeServiceSasSignatureValues values = new DataLakeServiceSasSignatureValues(expiryTime, permission)
            .setStartTime(OffsetDateTime.now());

        // Client must be authenticated via StorageSharedKeyCredential
        client.generateSas(values, new Context("key", "value"));
        // END: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.generateSas#DataLakeServiceSasSignatureValues-Context

        // BEGIN: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.generateUserDelegationSas#DataLakeServiceSasSignatureValues-UserDelegationKey-String-Context
        OffsetDateTime myExpiryTime = OffsetDateTime.now().plusDays(1);
        FileSystemSasPermission myPermission = new FileSystemSasPermission().setReadPermission(true);

        DataLakeServiceSasSignatureValues myValues = new DataLakeServiceSasSignatureValues(expiryTime, permission)
            .setStartTime(OffsetDateTime.now());

        client.generateUserDelegationSas(values, userDelegationKey, accountName, new Context("key", "value"));
        // END: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.generateUserDelegationSas#DataLakeServiceSasSignatureValues-UserDelegationKey-String-Context
    }

    /**
     * Code snippet for {@link DataLakeFileSystemAsyncClient#createIfNotExists()} and
     * for {@link DataLakeFileSystemAsyncClient#createIfNotExistsWithResponse(Map, PublicAccessType)}
     */
    public void createIfNotExistsCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.createIfNotExists
        client.createIfNotExists().subscribe(created -> {
            if (created) {
                System.out.println("Successfully created.");
            } else {
                System.out.println("Already exists.");
            }
        });
        // END: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.createIfNotExists

        // BEGIN: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.createIfNotExistsWithResponse#Map-PublicAccessType
        Map<String, String> metadata = Collections.singletonMap("metadata", "value");
        client.createIfNotExistsWithResponse(metadata, PublicAccessType.CONTAINER).subscribe(response -> {
            if (response.getStatusCode() == 409) {
                System.out.println("Already exists.");
            } else {
                System.out.println("successfully created.");
            }
        });
        // END: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.createIfNotExistsWithResponse#Map-PublicAccessType
    }

    /**
     * Code snippet for {@link DataLakeFileSystemAsyncClient#deleteIfExists()} and
     * for {@link DataLakeFileSystemAsyncClient#deleteIfExistsWithResponse(DataLakePathDeleteOptions)}
     */
    public void deleteIfExistsCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.deleteIfExists
        client.deleteIfExists().subscribe(deleted -> {
            if (deleted) {
                System.out.println("Successfully deleted.");
            } else {
                System.out.println("Does not exist.");
            }
        });
        // END: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.deleteIfExists

        // BEGIN: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.deleteIfExistsWithResponse#DataLakePathDeleteOptions
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions()
            .setLeaseId(leaseId)
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));
        DataLakePathDeleteOptions options = new DataLakePathDeleteOptions().setIsRecursive(false)
            .setRequestConditions(requestConditions);

        client.deleteIfExistsWithResponse(options).subscribe(response -> {
            if (response.getStatusCode() == 404) {
                System.out.println("Does not exist.");
            } else {
                System.out.println("successfully deleted.");
            }
        });
        // END: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.deleteIfExistsWithResponse#DataLakePathDeleteOptions
    }

    /**
     * Code snippets for {@link DataLakeFileSystemAsyncClient#createFileIfNotExists(String)} and
     * {@link DataLakeFileSystemAsyncClient#createFileIfNotExistsWithResponse(String, DataLakePathCreateOptions)}
     */
    public void createFileIfNotExistsCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.createFileIfNotExists#String
        DataLakeFileAsyncClient fileAsyncClient = client.createFileIfNotExists(fileName).block();
        // END: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.createFileIfNotExists#String

        // BEGIN: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.createFileIfNotExistsWithResponse#String-DataLakePathCreateOptions
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
        // END: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.createFileIfNotExistsWithResponse#String-DataLakePathCreateOptions
    }

    /**
     * Code snippets for {@link DataLakeFileSystemAsyncClient#deleteFileIfExists(String)} and
     * {@link DataLakeFileSystemAsyncClient#deleteFileIfExistsWithResponse(String, DataLakePathDeleteOptions)}
     */
    public void deleteFileIfExistsCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.deleteFileIfExists#String
        client.deleteFileIfExists(fileName).subscribe(deleted -> {
            if (deleted) {
                System.out.println("Successfully deleted.");
            } else {
                System.out.println("Does not exist.");
            }
        });
        // END: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.deleteFileIfExists#String

        // BEGIN: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.deleteFileIfExistsWithResponse#String-DataLakePathDeleteOptions
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
        // END: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.deleteFileIfExistsWithResponse#String-DataLakePathDeleteOptions
    }

    /**
     * Code snippets for {@link DataLakeFileSystemAsyncClient#createDirectoryIfNotExists(String)} and
     * {@link DataLakeFileSystemAsyncClient#createDirectoryIfNotExistsWithResponse(String, DataLakePathCreateOptions)}
     */
    public void createDirectoryIfNotExistsCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.createDirectoryIfNotExists#String
        DataLakeDirectoryAsyncClient directoryAsyncClient = client.createDirectoryIfNotExists(directoryName).block();
        // END: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.createDirectoryIfNotExists#String

        // BEGIN: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.createDirectoryIfNotExistsWithResponse#String-DataLakePathCreateOptions
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

        client.createDirectoryIfNotExistsWithResponse(directoryName, options).subscribe(response -> {
            if (response.getStatusCode() == 409) {
                System.out.println("Already exists.");
            } else {
                System.out.println("successfully created.");
            }
        });
        // END: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.createDirectoryIfNotExistsWithResponse#String-DataLakePathCreateOptions
    }

    /**
     * Code snippets for {@link DataLakeFileSystemAsyncClient#deleteDirectoryIfExists(String)} and
     * {@link DataLakeFileSystemAsyncClient#deleteDirectoryIfExistsWithResponse(String, DataLakePathDeleteOptions)}
     */
    public void deleteDirectoryIfExistsCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.deleteDirectoryIfExists#String
        client.deleteDirectoryIfExists(fileName).subscribe(deleted -> {
            if (deleted) {
                System.out.println("Successfully deleted.");
            } else {
                System.out.println("Does not exist.");
            }
        });
        // END: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.deleteDirectoryIfExists#String

        // BEGIN: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.deleteDirectoryIfExistsWithResponse#String-DataLakePathDeleteOptions
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions()
            .setLeaseId(leaseId);
        boolean recursive = false; // Default value
        DataLakePathDeleteOptions options = new DataLakePathDeleteOptions().setIsRecursive(recursive)
            .setRequestConditions(requestConditions);

        client.deleteDirectoryIfExistsWithResponse(directoryName, options).subscribe(response -> {
            if (response.getStatusCode() == 404) {
                System.out.println("Does not exist.");
            } else {
                System.out.println("successfully deleted.");
            }
        });
        // END: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.deleteDirectoryIfExistsWithResponse#String-DataLakePathDeleteOptions
    }

//    /**
//     * Code snippet for {@link DataLakeFileSystemAsyncClient#rename(String)}
//     */
//    public void renameFileSystem() {
//        // BEGIN: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.rename#String
//        DataLakeFileSystemAsyncClient fileSystemAsyncClient =
//            client.rename("newFileSystemName")
//                .block();
//        // END: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.rename#String
//    }

//    /**
//     * Code snippet for {@link DataLakeFileSystemAsyncClient#renameWithResponse(FileSystemRenameOptions)}
//     */
//    public void renameFileSystemWithResponse() {
//        // BEGIN: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.renameWithResponse#FileSystemRenameOptions
//        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions().setLeaseId("lease-id");
//        DataLakeFileSystemAsyncClient fileSystemAsyncClient = client
//            .renameWithResponse(new FileSystemRenameOptions("newFileSystemName")
//                .setRequestConditions(requestConditions)).block().getValue();
//        // END: com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient.renameWithResponse#FileSystemRenameOptions
//    }

}
