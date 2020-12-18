// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.storage.file.datalake.models.AccessControlChangeResult;
import com.azure.storage.file.datalake.models.AccessControlChanges;
import com.azure.storage.file.datalake.models.DataLakeRequestConditions;
import com.azure.storage.file.datalake.models.PathAccessControl;
import com.azure.storage.file.datalake.models.PathAccessControlEntry;
import com.azure.storage.file.datalake.models.PathHttpHeaders;
import com.azure.storage.file.datalake.models.PathInfo;
import com.azure.storage.file.datalake.models.PathPermissions;
import com.azure.storage.file.datalake.models.PathProperties;
import com.azure.storage.file.datalake.models.PathRemoveAccessControlEntry;
import com.azure.storage.file.datalake.models.RolePermissions;
import com.azure.storage.file.datalake.models.UserDelegationKey;
import com.azure.storage.file.datalake.options.PathRemoveAccessControlRecursiveOptions;
import com.azure.storage.file.datalake.options.PathSetAccessControlRecursiveOptions;
import com.azure.storage.file.datalake.options.PathUpdateAccessControlRecursiveOptions;
import com.azure.storage.file.datalake.sas.DataLakeServiceSasSignatureValues;
import com.azure.storage.file.datalake.sas.PathSasPermission;

import java.time.Duration;
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
public class PathClientJavaDocCodeSamples {
    private String fileName = "fileName";
    private DataLakeFileClient client = JavaDocCodeSnippetsHelpers.getFileClient(fileName);
    private String leaseId = "leaseId";
    private Duration timeout = Duration.ofSeconds(30);

    private String key1 = "key1";
    private String key2 = "key2";
    private String value1 = "val1";
    private String value2 = "val2";
    private String accountName = "accountName";
    private UserDelegationKey userDelegationKey = JavaDocCodeSnippetsHelpers.getUserDelegationKey();

    /**
     * Code snippets for {@link DataLakePathClient#create()} and
     * {@link DataLakePathClient#createWithResponse(String, String, PathHttpHeaders, Map, DataLakeRequestConditions, Duration, Context)}
     */
    public void createCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakePathClient.create
        System.out.printf("Last Modified Time:%s", client.create().getLastModified());
        // END: com.azure.storage.file.datalake.DataLakePathClient.create

        // BEGIN: com.azure.storage.file.datalake.DataLakePathClient.create#boolean
        boolean overwrite = true;
        System.out.printf("Last Modified Time:%s", client.create(true).getLastModified());
        // END: com.azure.storage.file.datalake.DataLakePathClient.create#boolean

        // BEGIN: com.azure.storage.file.datalake.DataLakePathClient.createWithResponse#String-String-PathHttpHeaders-Map-DataLakeRequestConditions-Duration-Context
        PathHttpHeaders httpHeaders = new PathHttpHeaders()
            .setContentLanguage("en-US")
            .setContentType("binary");
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions()
            .setLeaseId(leaseId);
        String permissions = "permissions";
        String umask = "umask";

        Response<PathInfo> response = client.createWithResponse(permissions, umask, httpHeaders,
            Collections.singletonMap("metadata", "value"), requestConditions, timeout,
            new Context(key1, value1));
        System.out.printf("Last Modified Time:%s", response.getValue().getLastModified());
        // END: com.azure.storage.file.datalake.DataLakePathClient.createWithResponse#String-String-PathHttpHeaders-Map-DataLakeRequestConditions-Duration-Context
    }

    /**
     * Code snippets for {@link DataLakePathClient#setMetadata(Map)}
     */
    public void setMetadataCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakePathClient.setMetadata#Map
        client.setMetadata(Collections.singletonMap("metadata", "value"));
        System.out.println("Set metadata completed");
        // END: com.azure.storage.file.datalake.DataLakePathClient.setMetadata#Map
    }

    /**
     * Code snippets for {@link DataLakePathClient#setMetadataWithResponse(Map, DataLakeRequestConditions, Duration, Context)}
     */
    public void setMetadataWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakePathClient.setMetadata#Map-DataLakeRequestConditions-Duration-Context
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions().setLeaseId(leaseId);

        client.setMetadataWithResponse(Collections.singletonMap("metadata", "value"), requestConditions, timeout,
            new Context(key2, value2));
        System.out.println("Set metadata completed");
        // END: com.azure.storage.file.datalake.DataLakePathClient.setMetadata#Map-DataLakeRequestConditions-Duration-Context
    }

    /**
     * Code snippets for {@link DataLakePathClient#setHttpHeaders(PathHttpHeaders)}
     */
    public void setHTTPHeadersCodeSnippet() {
        // BEGIN: com.azure.storage.file.datalake.DataLakePathClient.setHttpHeaders#PathHttpHeaders
        client.setHttpHeaders(new PathHttpHeaders()
            .setContentLanguage("en-US")
            .setContentType("binary"));
        // END: com.azure.storage.file.datalake.DataLakePathClient.setHttpHeaders#PathHttpHeaders
    }

    /**
     * Code snippets for {@link DataLakePathClient#setHttpHeadersWithResponse(PathHttpHeaders, DataLakeRequestConditions, Duration, Context)}
     */
    public void setHTTPHeadersWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakePathClient.setHttpHeadersWithResponse#PathHttpHeaders-DataLakeRequestConditions-Duration-Context
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions().setLeaseId(leaseId);

        Response<Void> response = client.setHttpHeadersWithResponse(new PathHttpHeaders()
            .setContentLanguage("en-US")
            .setContentType("binary"), requestConditions, timeout, new Context(key2, value2));
        System.out.printf("Set HTTP headers completed with status %d%n",
                    response.getStatusCode());
        // END: com.azure.storage.file.datalake.DataLakePathClient.setHttpHeadersWithResponse#PathHttpHeaders-DataLakeRequestConditions-Duration-Context
    }

    /**
     * Code snippets for {@link DataLakePathClient#getProperties()}
     */
    public void getPropertiesCodeSnippet() {
        // BEGIN: com.azure.storage.file.datalake.DataLakePathClient.getProperties
        System.out.printf("Creation Time: %s, Size: %d%n", client.getProperties().getCreationTime(),
            client.getProperties().getFileSize());
        // END: com.azure.storage.file.datalake.DataLakePathClient.getProperties
    }

    /**
     * Code snippets for {@link DataLakePathClient#getPropertiesWithResponse(DataLakeRequestConditions, Duration, Context)}
     */
    public void getPropertiesWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakePathClient.getPropertiesWithResponse#DataLakeRequestConditions-Duration-Context
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions().setLeaseId(leaseId);

        Response<PathProperties> response = client.getPropertiesWithResponse(requestConditions, timeout,
            new Context(key2, value2));

        System.out.printf("Creation Time: %s, Size: %d%n", response.getValue().getCreationTime(),
            response.getValue().getFileSize());
        // END: com.azure.storage.file.datalake.DataLakePathClient.getPropertiesWithResponse#DataLakeRequestConditions-Duration-Context
    }

    /**
     * Code snippets for {@link DataLakePathClient#exists()}
     */
    public void existsCodeSnippet() {
        // BEGIN: com.azure.storage.file.datalake.DataLakePathClient.exists
        System.out.printf("Exists? %b%n", client.exists());
        // END: com.azure.storage.file.datalake.DataLakePathClient.exists
    }

    /**
     * Code snippet for {@link DataLakePathClient#existsWithResponse(Duration, Context)}
     */
    public void existsWithResponseCodeSnippet() {
        // BEGIN: com.azure.storage.file.datalake.DataLakePathClient.existsWithResponse#Duration-Context
        System.out.printf("Exists? %b%n", client.existsWithResponse(timeout, new Context(key2, value2)).getValue());
        // END: com.azure.storage.file.datalake.DataLakePathClient.existsWithResponse#Duration-Context
    }

    /**
     * Code snippets for {@link DataLakePathClient#setAccessControlList(List, String, String)}
     */
    public void setAccessControlListCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakePathClient.setAccessControlList#List-String-String
        PathAccessControlEntry pathAccessControlEntry = new PathAccessControlEntry()
            .setEntityId("entityId")
            .setPermissions(new RolePermissions().setReadPermission(true));
        List<PathAccessControlEntry> pathAccessControlEntries = new ArrayList<>();
        pathAccessControlEntries.add(pathAccessControlEntry);
        String group = "group";
        String owner = "owner";

        System.out.printf("Last Modified Time: %s", client.setAccessControlList(pathAccessControlEntries, group, owner)
            .getLastModified());
        // END: com.azure.storage.file.datalake.DataLakePathClient.setAccessControlList#List-String-String
    }

    /**
     * Code snippets for {@link DataLakePathClient#setAccessControlListWithResponse(List, String, String, DataLakeRequestConditions, Duration, Context)}
     */
    public void setAccessControlListWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakePathClient.setAccessControlListWithResponse#List-String-String-DataLakeRequestConditions-Duration-Context
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions().setLeaseId(leaseId);
        PathAccessControlEntry pathAccessControlEntry = new PathAccessControlEntry()
            .setEntityId("entityId")
            .setPermissions(new RolePermissions().setReadPermission(true));
        List<PathAccessControlEntry> pathAccessControlEntries = new ArrayList<>();
        pathAccessControlEntries.add(pathAccessControlEntry);
        String group = "group";
        String owner = "owner";

        Response<PathInfo> response = client.setAccessControlListWithResponse(pathAccessControlEntries, group, owner,
            requestConditions, timeout, new Context(key2, value2));
        System.out.printf("Last Modified Time: %s", response.getValue().getLastModified());
        // END: com.azure.storage.file.datalake.DataLakePathClient.setAccessControlListWithResponse#List-String-String-DataLakeRequestConditions-Duration-Context
    }

    /**
     * Code snippets for {@link DataLakePathClient#setAccessControlRecursive(List)}
     */
    public void setAccessControlListRecursiveCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakePathClient.setAccessControlRecursive#List
        PathAccessControlEntry pathAccessControlEntry = new PathAccessControlEntry()
            .setEntityId("entityId")
            .setPermissions(new RolePermissions().setReadPermission(true));
        List<PathAccessControlEntry> pathAccessControlEntries = new ArrayList<>();
        pathAccessControlEntries.add(pathAccessControlEntry);

        AccessControlChangeResult response = client.setAccessControlRecursive(pathAccessControlEntries);

        System.out.printf("Successful changed file operations: %d",
            response.getCounters().getChangedFilesCount());
        // END: com.azure.storage.file.datalake.DataLakePathClient.setAccessControlRecursive#List
    }

    /**
     * Code snippets for {@link DataLakePathClient#setAccessControlRecursiveWithResponse(com.azure.storage.file.datalake.options.PathSetAccessControlRecursiveOptions, Duration, Context)}
     */
    public void setAccessControlRecursiveWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakePathClient.setAccessControlRecursiveWithResponse#PathSetAccessControlRecursiveOptions-Duration-Context
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions().setLeaseId(leaseId);
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

        Response<AccessControlChangeResult> response = client.setAccessControlRecursiveWithResponse(options, timeout,
            new Context(key2, value2));
        System.out.printf("Successful changed file operations: %d",
            response.getValue().getCounters().getChangedFilesCount());
        // END: com.azure.storage.file.datalake.DataLakePathClient.setAccessControlRecursiveWithResponse#PathSetAccessControlRecursiveOptions-Duration-Context
    }

    /**
     * Code snippets for {@link DataLakePathClient#updateAccessControlRecursive(List)}
     */
    public void updateAccessControlListRecursiveCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakePathClient.updateAccessControlRecursive#List
        PathAccessControlEntry pathAccessControlEntry = new PathAccessControlEntry()
            .setEntityId("entityId")
            .setPermissions(new RolePermissions().setReadPermission(true));
        List<PathAccessControlEntry> pathAccessControlEntries = new ArrayList<>();
        pathAccessControlEntries.add(pathAccessControlEntry);

        AccessControlChangeResult response = client.updateAccessControlRecursive(pathAccessControlEntries);

        System.out.printf("Successful changed file operations: %d",
            response.getCounters().getChangedFilesCount());
        // END: com.azure.storage.file.datalake.DataLakePathClient.updateAccessControlRecursive#List
    }

    /**
     * Code snippets for {@link DataLakePathClient#updateAccessControlRecursiveWithResponse(com.azure.storage.file.datalake.options.PathUpdateAccessControlRecursiveOptions, Duration, Context)}
     */
    public void updateAccessControlRecursiveWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakePathClient.updateAccessControlRecursiveWithResponse#PathUpdateAccessControlRecursiveOptions-Duration-Context
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions().setLeaseId(leaseId);
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

        Response<AccessControlChangeResult> response = client.updateAccessControlRecursiveWithResponse(options, timeout,
            new Context(key2, value2));
        System.out.printf("Successful changed file operations: %d",
            response.getValue().getCounters().getChangedFilesCount());
        // END: com.azure.storage.file.datalake.DataLakePathClient.updateAccessControlRecursiveWithResponse#PathUpdateAccessControlRecursiveOptions-Duration-Context
    }

    /**
     * Code snippets for {@link DataLakePathClient#removeAccessControlRecursive(List)}
     */
    public void removeAccessControlListRecursiveCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakePathClient.removeAccessControlRecursive#List
        PathRemoveAccessControlEntry pathAccessControlEntry = new PathRemoveAccessControlEntry()
            .setEntityId("entityId");
        List<PathRemoveAccessControlEntry> pathAccessControlEntries = new ArrayList<>();
        pathAccessControlEntries.add(pathAccessControlEntry);

        AccessControlChangeResult response = client.removeAccessControlRecursive(pathAccessControlEntries);

        System.out.printf("Successful changed file operations: %d",
            response.getCounters().getChangedFilesCount());
        // END: com.azure.storage.file.datalake.DataLakePathClient.removeAccessControlRecursive#List
    }

    /**
     * Code snippets for {@link DataLakePathClient#removeAccessControlRecursiveWithResponse(com.azure.storage.file.datalake.options.PathRemoveAccessControlRecursiveOptions, Duration, Context)}
     */
    public void removeAccessControlRecursiveWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakePathClient.removeAccessControlRecursiveWithResponse#PathRemoveAccessControlRecursiveOptions-Duration-Context
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions().setLeaseId(leaseId);
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

        Response<AccessControlChangeResult> response = client.removeAccessControlRecursiveWithResponse(options, timeout,
            new Context(key2, value2));
        System.out.printf("Successful changed file operations: %d",
            response.getValue().getCounters().getChangedFilesCount());
        // END: com.azure.storage.file.datalake.DataLakePathClient.removeAccessControlRecursiveWithResponse#PathRemoveAccessControlRecursiveOptions-Duration-Context
    }

    /**
     * Code snippets for {@link DataLakePathClient#setPermissions(PathPermissions, String, String)}
     */
    public void setPermissionsCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakePathClient.setPermissions#PathPermissions-String-String
        PathPermissions permissions = new PathPermissions()
            .setGroup(new RolePermissions().setExecutePermission(true).setReadPermission(true))
            .setOwner(new RolePermissions().setExecutePermission(true).setReadPermission(true).setWritePermission(true))
            .setOther(new RolePermissions().setReadPermission(true));
        String group = "group";
        String owner = "owner";

        System.out.printf("Last Modified Time: %s", client.setPermissions(permissions, group, owner)
            .getLastModified());
        // END: com.azure.storage.file.datalake.DataLakePathClient.setPermissions#PathPermissions-String-String
    }

    /**
     * Code snippets for {@link DataLakePathClient#setPermissionsWithResponse(PathPermissions, String, String, DataLakeRequestConditions, Duration, Context)}
     */
    public void setPermissonsWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakePathClient.setPermissionsWithResponse#PathPermissions-String-String-DataLakeRequestConditions-Duration-Context
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions().setLeaseId(leaseId);
        PathPermissions permissions = new PathPermissions()
            .setGroup(new RolePermissions().setExecutePermission(true).setReadPermission(true))
            .setOwner(new RolePermissions().setExecutePermission(true).setReadPermission(true).setWritePermission(true))
            .setOther(new RolePermissions().setReadPermission(true));
        String group = "group";
        String owner = "owner";

        Response<PathInfo> response = client.setPermissionsWithResponse(permissions, group, owner, requestConditions,
            timeout, new Context(key2, value2));
        System.out.printf("Last Modified Time: %s", response.getValue().getLastModified());
        // END: com.azure.storage.file.datalake.DataLakePathClient.setPermissionsWithResponse#PathPermissions-String-String-DataLakeRequestConditions-Duration-Context
    }

    /**
     * Code snippets for {@link DataLakePathClient#getAccessControl()}
     */
    public void getAccessControlCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakePathClient.getAccessControl
        PathAccessControl response = client.getAccessControl();
        System.out.printf("Access Control List: %s, Group: %s, Owner: %s, Permissions: %s",
            PathAccessControlEntry.serializeList(response.getAccessControlList()), response.getGroup(),
            response.getOwner(), response.getPermissions());
        // END: com.azure.storage.file.datalake.DataLakePathClient.getAccessControl
    }

    /**
     * Code snippets for {@link DataLakePathClient#getAccessControlWithResponse(boolean, DataLakeRequestConditions, Duration, Context)}
     */
    public void getAccessControlWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakePathClient.getAccessControlWithResponse#boolean-DataLakeRequestConditions-Duration-Context
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions().setLeaseId(leaseId);
        boolean userPrincipalNameReturned = false;

        Response<PathAccessControl> response = client.getAccessControlWithResponse(userPrincipalNameReturned,
            requestConditions, timeout, new Context(key1, value1));

        PathAccessControl pac = response.getValue();

        System.out.printf("Access Control List: %s, Group: %s, Owner: %s, Permissions: %s",
            PathAccessControlEntry.serializeList(pac.getAccessControlList()), pac.getGroup(), pac.getOwner(),
            pac.getPermissions());
        // END: com.azure.storage.file.datalake.DataLakePathClient.getAccessControlWithResponse#boolean-DataLakeRequestConditions-Duration-Context
    }

    /**
     * Code snippet for {@link DataLakePathClient#generateUserDelegationSas(DataLakeServiceSasSignatureValues, UserDelegationKey)}
     * and {@link DataLakePathClient#generateSas(DataLakeServiceSasSignatureValues)}
     */
    public void generateSas() {
        // BEGIN: com.azure.storage.file.datalake.DataLakePathClient.generateSas#DataLakeServiceSasSignatureValues
        OffsetDateTime expiryTime = OffsetDateTime.now().plusDays(1);
        PathSasPermission permission = new PathSasPermission().setReadPermission(true);

        DataLakeServiceSasSignatureValues values = new DataLakeServiceSasSignatureValues(expiryTime, permission)
            .setStartTime(OffsetDateTime.now());

        client.generateSas(values); // Client must be authenticated via StorageSharedKeyCredential
        // END: com.azure.storage.file.datalake.DataLakePathClient.generateSas#DataLakeServiceSasSignatureValues

        // BEGIN: com.azure.storage.file.datalake.DataLakePathClient.generateUserDelegationSas#DataLakeServiceSasSignatureValues-UserDelegationKey
        OffsetDateTime myExpiryTime = OffsetDateTime.now().plusDays(1);
        PathSasPermission myPermission = new PathSasPermission().setReadPermission(true);

        DataLakeServiceSasSignatureValues myValues = new DataLakeServiceSasSignatureValues(expiryTime, permission)
            .setStartTime(OffsetDateTime.now());

        client.generateUserDelegationSas(values, userDelegationKey);
        // END: com.azure.storage.file.datalake.DataLakePathClient.generateUserDelegationSas#DataLakeServiceSasSignatureValues-UserDelegationKey
    }

    /**
     * Code snippet for {@link DataLakePathClient#generateUserDelegationSas(DataLakeServiceSasSignatureValues, UserDelegationKey, String, Context)}
     * and {@link DataLakePathClient#generateSas(DataLakeServiceSasSignatureValues, Context)}
     */
    public void generateSasWithContext() {
        // BEGIN: com.azure.storage.file.datalake.DataLakePathClient.generateSas#DataLakeServiceSasSignatureValues-Context
        OffsetDateTime expiryTime = OffsetDateTime.now().plusDays(1);
        PathSasPermission permission = new PathSasPermission().setReadPermission(true);

        DataLakeServiceSasSignatureValues values = new DataLakeServiceSasSignatureValues(expiryTime, permission)
            .setStartTime(OffsetDateTime.now());

        // Client must be authenticated via StorageSharedKeyCredential
        client.generateSas(values, new Context("key", "value"));
        // END: com.azure.storage.file.datalake.DataLakePathClient.generateSas#DataLakeServiceSasSignatureValues-Context

        // BEGIN: com.azure.storage.file.datalake.DataLakePathClient.generateUserDelegationSas#DataLakeServiceSasSignatureValues-UserDelegationKey-String-Context
        OffsetDateTime myExpiryTime = OffsetDateTime.now().plusDays(1);
        PathSasPermission myPermission = new PathSasPermission().setReadPermission(true);

        DataLakeServiceSasSignatureValues myValues = new DataLakeServiceSasSignatureValues(expiryTime, permission)
            .setStartTime(OffsetDateTime.now());

        client.generateUserDelegationSas(values, userDelegationKey, accountName, new Context("key", "value"));
        // END: com.azure.storage.file.datalake.DataLakePathClient.generateUserDelegationSas#DataLakeServiceSasSignatureValues-UserDelegationKey-String-Context
    }

}
