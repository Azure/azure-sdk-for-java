// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.storage.file.datalake.models.AccessControlChanges;
import com.azure.storage.file.datalake.models.DataLakeRequestConditions;
import com.azure.storage.file.datalake.models.PathAccessControlEntry;
import com.azure.storage.file.datalake.models.PathHttpHeaders;
import com.azure.storage.file.datalake.models.PathPermissions;
import com.azure.storage.file.datalake.models.PathRemoveAccessControlEntry;
import com.azure.storage.file.datalake.models.RolePermissions;
import com.azure.storage.file.datalake.models.UserDelegationKey;
import com.azure.storage.file.datalake.options.PathRemoveAccessControlRecursiveOptions;
import com.azure.storage.file.datalake.options.PathSetAccessControlRecursiveOptions;
import com.azure.storage.file.datalake.options.PathUpdateAccessControlRecursiveOptions;
import com.azure.storage.file.datalake.sas.DataLakeServiceSasSignatureValues;
import com.azure.storage.file.datalake.sas.PathSasPermission;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Code snippets for {@link DataLakeFileSystemClient}
 */
@SuppressWarnings({"unused"})
public class PathAsyncClientJavaDocCodeSamples {
    private String fileName = "fileName";
    private DataLakeFileAsyncClient client = JavaDocCodeSnippetsHelpers.getFileAsyncClient(fileName);
    private String leaseId = "leaseId";
    private String accountName = "accountName";
    private UserDelegationKey userDelegationKey = JavaDocCodeSnippetsHelpers.getUserDelegationKey();

    /**
     * Code snippets for {@link DataLakePathAsyncClient#create()} and
     * {@link DataLakePathAsyncClient#createWithResponse(String, String, PathHttpHeaders, Map, DataLakeRequestConditions)}
     */
    public void createCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakePathAsyncClient.create
        client.create().subscribe(response ->
            System.out.printf("Last Modified Time:%s", response.getLastModified()));
        // END: com.azure.storage.file.datalake.DataLakePathAsyncClient.create

        // BEGIN: com.azure.storage.file.datalake.DataLakePathAsyncClient.create#boolean
        boolean overwrite = true;
        client.create(overwrite).subscribe(response ->
            System.out.printf("Last Modified Time:%s", response.getLastModified()));
        // END: com.azure.storage.file.datalake.DataLakePathAsyncClient.create#boolean

        // BEGIN: com.azure.storage.file.datalake.DataLakePathAsyncClient.createWithResponse#String-String-PathHttpHeaders-Map-DataLakeRequestConditions
        PathHttpHeaders httpHeaders = new PathHttpHeaders()
            .setContentLanguage("en-US")
            .setContentType("binary");
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions()
            .setLeaseId(leaseId);
        String permissions = "permissions";
        String umask = "umask";

        client.createWithResponse(permissions, umask, httpHeaders, Collections.singletonMap("metadata", "value"),
            requestConditions)
            .subscribe(response -> System.out.printf("Last Modified Time:%s", response.getValue().getLastModified()));
        // END: com.azure.storage.file.datalake.DataLakePathAsyncClient.createWithResponse#String-String-PathHttpHeaders-Map-DataLakeRequestConditions
    }

    /**
     * Code snippets for {@link DataLakePathAsyncClient#setMetadata(Map)}
     */
    public void setMetadataCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakePathAsyncClient.setMetadata#Map
        client.setMetadata(Collections.singletonMap("metadata", "value"))
            .subscribe(response -> System.out.println("Set metadata completed"));
        // END: com.azure.storage.file.datalake.DataLakePathAsyncClient.setMetadata#Map
    }

    /**
     * Code snippets for {@link DataLakePathAsyncClient#setMetadataWithResponse(Map, DataLakeRequestConditions)}
     */
    public void setMetadataWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakePathAsyncClient.setMetadata#Map-DataLakeRequestConditions
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions().setLeaseId(leaseId);

        client.setMetadataWithResponse(Collections.singletonMap("metadata", "value"), requestConditions)
            .subscribe(response -> System.out.printf("Set metadata completed with status %d%n",
                response.getStatusCode()));
        // END: com.azure.storage.file.datalake.DataLakePathAsyncClient.setMetadata#Map-DataLakeRequestConditions
    }

    /**
     * Code snippets for {@link DataLakePathAsyncClient#setHttpHeaders(PathHttpHeaders)}
     */
    public void setHTTPHeadersCodeSnippet() {
        // BEGIN: com.azure.storage.file.datalake.DataLakePathAsyncClient.setHttpHeaders#PathHttpHeaders
        client.setHttpHeaders(new PathHttpHeaders()
            .setContentLanguage("en-US")
            .setContentType("binary"));
        // END: com.azure.storage.file.datalake.DataLakePathAsyncClient.setHttpHeaders#PathHttpHeaders
    }

    /**
     * Code snippets for {@link DataLakePathAsyncClient#setHttpHeadersWithResponse(PathHttpHeaders, DataLakeRequestConditions)}
     */
    public void setHTTPHeadersWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakePathAsyncClient.setHttpHeadersWithResponse#PathHttpHeaders-DataLakeRequestConditions
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions().setLeaseId(leaseId);

        client.setHttpHeadersWithResponse(new PathHttpHeaders()
            .setContentLanguage("en-US")
            .setContentType("binary"), requestConditions).subscribe(response ->
            System.out.printf("Set HTTP headers completed with status %d%n", response.getStatusCode()));
        // END: com.azure.storage.file.datalake.DataLakePathAsyncClient.setHttpHeadersWithResponse#PathHttpHeaders-DataLakeRequestConditions
    }

    /**
     * Code snippets for {@link DataLakePathAsyncClient#getProperties()}
     */
    public void getPropertiesCodeSnippet() {
        // BEGIN: com.azure.storage.file.datalake.DataLakePathAsyncClient.getProperties
        client.getProperties().subscribe(response ->
            System.out.printf("Creation Time: %s, Size: %d%n", response.getCreationTime(), response.getFileSize()));
        // END: com.azure.storage.file.datalake.DataLakePathAsyncClient.getProperties
    }

    /**
     * Code snippets for {@link DataLakePathAsyncClient#getPropertiesWithResponse(DataLakeRequestConditions)}
     */
    public void getPropertiesWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakePathAsyncClient.getPropertiesWithResponse#DataLakeRequestConditions
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions().setLeaseId(leaseId);

        client.getPropertiesWithResponse(requestConditions).subscribe(
            response -> System.out.printf("Creation Time: %s, Size: %d%n", response.getValue().getCreationTime(),
                response.getValue().getFileSize()));
        // END: com.azure.storage.file.datalake.DataLakePathAsyncClient.getPropertiesWithResponse#DataLakeRequestConditions
    }

    /**
     * Code snippet for {@link DataLakePathAsyncClient#exists()}
     */
    public void existsCodeSnippet() {
        // BEGIN: com.azure.storage.file.datalake.DataLakePathAsyncClient.exists
        client.exists().subscribe(response -> System.out.printf("Exists? %b%n", response));
        // END: com.azure.storage.file.datalake.DataLakePathAsyncClient.exists
    }

    /**
     * Code snippet for {@link DataLakePathAsyncClient#existsWithResponse()}
     */
    public void existsWithResponseCodeSnippet() {
        // BEGIN: com.azure.storage.file.datalake.DataLakePathAsyncClient.existsWithResponse
        client.existsWithResponse().subscribe(response -> System.out.printf("Exists? %b%n", response.getValue()));
        // END: com.azure.storage.file.datalake.DataLakePathAsyncClient.existsWithResponse
    }

    /**
     * Code snippets for {@link DataLakePathAsyncClient#setAccessControlList(List, String, String)}
     */
    public void setAccessControlCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakePathAsyncClient.setAccessControlList#List-String-String
        PathAccessControlEntry pathAccessControlEntry = new PathAccessControlEntry()
            .setEntityId("entityId")
            .setPermissions(new RolePermissions().setReadPermission(true));
        List<PathAccessControlEntry> pathAccessControlEntries = new ArrayList<>();
        pathAccessControlEntries.add(pathAccessControlEntry);
        String group = "group";
        String owner = "owner";

        client.setAccessControlList(pathAccessControlEntries, group, owner).subscribe(
            response -> System.out.printf("Last Modified Time: %s", response.getLastModified()));
        // END: com.azure.storage.file.datalake.DataLakePathAsyncClient.setAccessControlList#List-String-String
    }

    /**
     * Code snippets for {@link DataLakePathAsyncClient#setAccessControlListWithResponse(List, String, String, DataLakeRequestConditions)}
     */
    public void setAccessControlWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakePathAsyncClient.setAccessControlListWithResponse#List-String-String-DataLakeRequestConditions
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions().setLeaseId(leaseId);
        PathAccessControlEntry pathAccessControlEntry = new PathAccessControlEntry()
            .setEntityId("entityId")
            .setPermissions(new RolePermissions().setReadPermission(true));
        List<PathAccessControlEntry> pathAccessControlEntries = new ArrayList<>();
        pathAccessControlEntries.add(pathAccessControlEntry);
        String group = "group";
        String owner = "owner";

        client.setAccessControlListWithResponse(pathAccessControlEntries, group, owner, requestConditions).subscribe(
            response -> System.out.printf("Last Modified Time: %s", response.getValue().getLastModified()));
        // END: com.azure.storage.file.datalake.DataLakePathAsyncClient.setAccessControlListWithResponse#List-String-String-DataLakeRequestConditions
    }

    /**
     * Code snippets for {@link DataLakePathAsyncClient#setAccessControlRecursive(List)}
     */
    public void setAccessControlRecursiveCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakePathAsyncClient.setAccessControlRecursive#List
        PathAccessControlEntry pathAccessControlEntry = new PathAccessControlEntry()
            .setEntityId("entityId")
            .setPermissions(new RolePermissions().setReadPermission(true));
        List<PathAccessControlEntry> pathAccessControlEntries = new ArrayList<>();
        pathAccessControlEntries.add(pathAccessControlEntry);

        client.setAccessControlRecursive(pathAccessControlEntries).subscribe(
            response -> System.out.printf("Successful changed file operations: %d",
                response.getCounters().getChangedFilesCount()));
        // END: com.azure.storage.file.datalake.DataLakePathAsyncClient.setAccessControlRecursive#List
    }

    /**
     * Code snippets for {@link DataLakePathAsyncClient#setAccessControlRecursiveWithResponse(PathSetAccessControlRecursiveOptions)}
     */
    public void setAccessControlRecursiveWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakePathAsyncClient.setAccessControlRecursiveWithResponse#PathSetAccessControlRecursiveOptions
        PathAccessControlEntry pathAccessControlEntry = new PathAccessControlEntry()
            .setEntityId("entityId")
            .setPermissions(new RolePermissions().setReadPermission(true));
        List<PathAccessControlEntry> pathAccessControlEntries = new ArrayList<>();
        pathAccessControlEntries.add(pathAccessControlEntry);

        Integer batchSize = 2;
        Integer maxBatches = 10;
        boolean continueOnFailure = false;
        String continuationToken = null;
        Consumer<Response<AccessControlChanges>> progressHandler =
            response -> System.out.println("Received response");

        PathSetAccessControlRecursiveOptions options =
            new PathSetAccessControlRecursiveOptions(pathAccessControlEntries)
                .setBatchSize(batchSize)
                .setMaxBatches(maxBatches)
                .setContinueOnFailure(continueOnFailure)
                .setContinuationToken(continuationToken)
                .setProgressHandler(progressHandler);

        client.setAccessControlRecursive(pathAccessControlEntries).subscribe(
            response -> System.out.printf("Successful changed file operations: %d",
                response.getCounters().getChangedFilesCount()));
        // END: com.azure.storage.file.datalake.DataLakePathAsyncClient.setAccessControlRecursiveWithResponse#PathSetAccessControlRecursiveOptions
    }

    /**
     * Code snippets for {@link DataLakePathAsyncClient#updateAccessControlRecursive(List)}
     */
    public void updateAccessControlRecursiveCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakePathAsyncClient.updateAccessControlRecursive#List
        PathAccessControlEntry pathAccessControlEntry = new PathAccessControlEntry()
            .setEntityId("entityId")
            .setPermissions(new RolePermissions().setReadPermission(true));
        List<PathAccessControlEntry> pathAccessControlEntries = new ArrayList<>();
        pathAccessControlEntries.add(pathAccessControlEntry);

        client.updateAccessControlRecursive(pathAccessControlEntries).subscribe(
            response -> System.out.printf("Successful changed file operations: %d",
                response.getCounters().getChangedFilesCount()));
        // END: com.azure.storage.file.datalake.DataLakePathAsyncClient.updateAccessControlRecursive#List
    }

    /**
     * Code snippets for {@link DataLakePathAsyncClient#updateAccessControlRecursiveWithResponse(PathUpdateAccessControlRecursiveOptions)}
     */
    public void updateAccessControlRecursiveWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakePathAsyncClient.updateAccessControlRecursiveWithResponse#PathUpdateAccessControlRecursiveOptions
        PathAccessControlEntry pathAccessControlEntry = new PathAccessControlEntry()
            .setEntityId("entityId")
            .setPermissions(new RolePermissions().setReadPermission(true));
        List<PathAccessControlEntry> pathAccessControlEntries = new ArrayList<>();
        pathAccessControlEntries.add(pathAccessControlEntry);

        Integer batchSize = 2;
        Integer maxBatches = 10;
        boolean continueOnFailure = false;
        String continuationToken = null;
        Consumer<Response<AccessControlChanges>> progressHandler =
            response -> System.out.println("Received response");

        PathUpdateAccessControlRecursiveOptions options =
            new PathUpdateAccessControlRecursiveOptions(pathAccessControlEntries)
                .setBatchSize(batchSize)
                .setMaxBatches(maxBatches)
                .setContinueOnFailure(continueOnFailure)
                .setContinuationToken(continuationToken)
                .setProgressHandler(progressHandler);

        client.updateAccessControlRecursive(pathAccessControlEntries).subscribe(
            response -> System.out.printf("Successful changed file operations: %d",
                response.getCounters().getChangedFilesCount()));
        // END: com.azure.storage.file.datalake.DataLakePathAsyncClient.updateAccessControlRecursiveWithResponse#PathUpdateAccessControlRecursiveOptions
    }

    /**
     * Code snippets for {@link DataLakePathAsyncClient#removeAccessControlRecursive(List)}
     */
    public void removeAccessControlRecursiveCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakePathAsyncClient.removeAccessControlRecursive#List
        PathRemoveAccessControlEntry pathAccessControlEntry = new PathRemoveAccessControlEntry()
            .setEntityId("entityId");
        List<PathRemoveAccessControlEntry> pathAccessControlEntries = new ArrayList<>();
        pathAccessControlEntries.add(pathAccessControlEntry);

        client.removeAccessControlRecursive(pathAccessControlEntries).subscribe(
            response -> System.out.printf("Successful changed file operations: %d",
                response.getCounters().getChangedFilesCount()));
        // END: com.azure.storage.file.datalake.DataLakePathAsyncClient.removeAccessControlRecursive#List
    }

    /**
     * Code snippets for {@link DataLakePathAsyncClient#removeAccessControlRecursiveWithResponse(PathRemoveAccessControlRecursiveOptions)}
     */
    public void removeAccessControlRecursiveWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakePathAsyncClient.removeAccessControlRecursiveWithResponse#PathRemoveAccessControlRecursiveOptions
        PathRemoveAccessControlEntry pathAccessControlEntry = new PathRemoveAccessControlEntry()
            .setEntityId("entityId");
        List<PathRemoveAccessControlEntry> pathAccessControlEntries = new ArrayList<>();
        pathAccessControlEntries.add(pathAccessControlEntry);

        Integer batchSize = 2;
        Integer maxBatches = 10;
        boolean continueOnFailure = false;
        String continuationToken = null;
        Consumer<Response<AccessControlChanges>> progressHandler =
            response -> System.out.println("Received response");

        PathRemoveAccessControlRecursiveOptions options =
            new PathRemoveAccessControlRecursiveOptions(pathAccessControlEntries)
                .setBatchSize(batchSize)
                .setMaxBatches(maxBatches)
                .setContinueOnFailure(continueOnFailure)
                .setContinuationToken(continuationToken)
                .setProgressHandler(progressHandler);

        client.removeAccessControlRecursive(pathAccessControlEntries).subscribe(
            response -> System.out.printf("Successful changed file operations: %d",
                response.getCounters().getChangedFilesCount()));
        // END: com.azure.storage.file.datalake.DataLakePathAsyncClient.removeAccessControlRecursiveWithResponse#PathRemoveAccessControlRecursiveOptions
    }

    /**
     * Code snippets for {@link DataLakePathAsyncClient#setPermissions(PathPermissions, String, String)}
     */
    public void setPermissionsCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakePathAsyncClient.setPermissions#PathPermissions-String-String
        PathPermissions permissions = new PathPermissions()
            .setGroup(new RolePermissions().setExecutePermission(true).setReadPermission(true))
            .setOwner(new RolePermissions().setExecutePermission(true).setReadPermission(true).setWritePermission(true))
            .setOther(new RolePermissions().setReadPermission(true));
        String group = "group";
        String owner = "owner";

        client.setPermissions(permissions, group, owner).subscribe(
            response -> System.out.printf("Last Modified Time: %s", response.getLastModified()));
        // END: com.azure.storage.file.datalake.DataLakePathAsyncClient.setPermissions#PathPermissions-String-String
    }

    /**
     * Code snippets for {@link DataLakePathAsyncClient#setPermissionsWithResponse(PathPermissions, String, String, DataLakeRequestConditions)}
     */
    public void setPermissionsWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakePathAsyncClient.setPermissionsWithResponse#PathPermissions-String-String-DataLakeRequestConditions
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions().setLeaseId(leaseId);
        PathPermissions permissions = new PathPermissions()
            .setGroup(new RolePermissions().setExecutePermission(true).setReadPermission(true))
            .setOwner(new RolePermissions().setExecutePermission(true).setReadPermission(true).setWritePermission(true))
            .setOther(new RolePermissions().setReadPermission(true));
        String group = "group";
        String owner = "owner";

        client.setPermissionsWithResponse(permissions, group, owner, requestConditions).subscribe(
            response -> System.out.printf("Last Modified Time: %s", response.getValue().getLastModified()));
        // END: com.azure.storage.file.datalake.DataLakePathAsyncClient.setPermissionsWithResponse#PathPermissions-String-String-DataLakeRequestConditions
    }

    /**
     * Code snippets for {@link DataLakePathAsyncClient#getAccessControl()}
     */
    public void getAccessControlCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakePathAsyncClient.getAccessControl
        client.getAccessControl().subscribe(
            response -> System.out.printf("Access Control List: %s, Group: %s, Owner: %s, Permissions: %s",
                PathAccessControlEntry.serializeList(response.getAccessControlList()), response.getGroup(),
                response.getOwner(), response.getPermissions()));
        // END: com.azure.storage.file.datalake.DataLakePathAsyncClient.getAccessControl
    }

    /**
     * Code snippets for {@link DataLakePathAsyncClient#getAccessControlWithResponse(boolean, DataLakeRequestConditions)}
     */
    public void getAccessControlWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakePathAsyncClient.getAccessControlWithResponse#boolean-DataLakeRequestConditions
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions().setLeaseId(leaseId);
        boolean userPrincipalNameReturned = false;

        client.getAccessControlWithResponse(userPrincipalNameReturned, requestConditions).subscribe(
            response -> System.out.printf("Access Control List: %s, Group: %s, Owner: %s, Permissions: %s",
                PathAccessControlEntry.serializeList(response.getValue().getAccessControlList()),
                response.getValue().getGroup(), response.getValue().getOwner(), response.getValue().getPermissions()));
        // END: com.azure.storage.file.datalake.DataLakePathAsyncClient.getAccessControlWithResponse#boolean-DataLakeRequestConditions
    }

    /**
     * Code snippet for {@link DataLakePathAsyncClient#generateUserDelegationSas(DataLakeServiceSasSignatureValues, UserDelegationKey)}
     * and {@link DataLakePathAsyncClient#generateSas(DataLakeServiceSasSignatureValues)}
     */
    public void generateSas() {
        // BEGIN: com.azure.storage.file.datalake.DataLakePathAsyncClient.generateSas#DataLakeServiceSasSignatureValues
        OffsetDateTime expiryTime = OffsetDateTime.now().plusDays(1);
        PathSasPermission permission = new PathSasPermission().setReadPermission(true);

        DataLakeServiceSasSignatureValues values = new DataLakeServiceSasSignatureValues(expiryTime, permission)
            .setStartTime(OffsetDateTime.now());

        client.generateSas(values); // Client must be authenticated via StorageSharedKeyCredential
        // END: com.azure.storage.file.datalake.DataLakePathAsyncClient.generateSas#DataLakeServiceSasSignatureValues

        // BEGIN: com.azure.storage.file.datalake.DataLakePathAsyncClient.generateUserDelegationSas#DataLakeServiceSasSignatureValues-UserDelegationKey
        OffsetDateTime myExpiryTime = OffsetDateTime.now().plusDays(1);
        PathSasPermission myPermission = new PathSasPermission().setReadPermission(true);

        DataLakeServiceSasSignatureValues myValues = new DataLakeServiceSasSignatureValues(expiryTime, permission)
            .setStartTime(OffsetDateTime.now());

        client.generateUserDelegationSas(values, userDelegationKey);
        // END: com.azure.storage.file.datalake.DataLakePathAsyncClient.generateUserDelegationSas#DataLakeServiceSasSignatureValues-UserDelegationKey
    }

    /**
     * Code snippet for {@link DataLakePathAsyncClient#generateUserDelegationSas(DataLakeServiceSasSignatureValues, UserDelegationKey, String, Context)}
     * and {@link DataLakePathAsyncClient#generateSas(DataLakeServiceSasSignatureValues, Context)}
     */
    public void generateSasWithContext() {
        // BEGIN: com.azure.storage.file.datalake.DataLakePathAsyncClient.generateSas#DataLakeServiceSasSignatureValues-Context
        OffsetDateTime expiryTime = OffsetDateTime.now().plusDays(1);
        PathSasPermission permission = new PathSasPermission().setReadPermission(true);

        DataLakeServiceSasSignatureValues values = new DataLakeServiceSasSignatureValues(expiryTime, permission)
            .setStartTime(OffsetDateTime.now());

        // Client must be authenticated via StorageSharedKeyCredential
        client.generateSas(values, new Context("key", "value"));
        // END: com.azure.storage.file.datalake.DataLakePathAsyncClient.generateSas#DataLakeServiceSasSignatureValues-Context

        // BEGIN: com.azure.storage.file.datalake.DataLakePathAsyncClient.generateUserDelegationSas#DataLakeServiceSasSignatureValues-UserDelegationKey-String-Context
        OffsetDateTime myExpiryTime = OffsetDateTime.now().plusDays(1);
        PathSasPermission myPermission = new PathSasPermission().setReadPermission(true);

        DataLakeServiceSasSignatureValues myValues = new DataLakeServiceSasSignatureValues(expiryTime, permission)
            .setStartTime(OffsetDateTime.now());

        client.generateUserDelegationSas(values, userDelegationKey, accountName, new Context("key", "value"));
        // END: com.azure.storage.file.datalake.DataLakePathAsyncClient.generateUserDelegationSas#DataLakeServiceSasSignatureValues-UserDelegationKey-String-Context
    }
}
