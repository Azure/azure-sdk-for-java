// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import com.azure.storage.file.datalake.models.DataLakeRequestConditions;
import com.azure.storage.file.datalake.models.PathAccessControlEntry;
import com.azure.storage.file.datalake.models.PathHttpHeaders;
import com.azure.storage.file.datalake.models.PathPermissions;
import com.azure.storage.file.datalake.models.RolePermissions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Code snippets for {@link DataLakeFileSystemClient}
 */
@SuppressWarnings({"unused"})
public class PathAsyncClientJavaDocCodeSamples {
    private String fileName = "fileName";
    private DataLakeFileAsyncClient client = JavaDocCodeSnippetsHelpers.getFileAsyncClient(fileName);
    private String leaseId = "leaseId";

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
        DataLakeRequestConditions accessConditions = new DataLakeRequestConditions().setLeaseId(leaseId);

        client.setMetadataWithResponse(Collections.singletonMap("metadata", "value"), accessConditions)
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
        DataLakeRequestConditions accessConditions = new DataLakeRequestConditions().setLeaseId(leaseId);

        client.setHttpHeadersWithResponse(new PathHttpHeaders()
            .setContentLanguage("en-US")
            .setContentType("binary"), accessConditions).subscribe(response ->
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
        DataLakeRequestConditions accessConditions = new DataLakeRequestConditions().setLeaseId(leaseId);

        client.getPropertiesWithResponse(accessConditions).subscribe(
            response -> System.out.printf("Creation Time: %s, Size: %d%n", response.getValue().getCreationTime(),
                response.getValue().getFileSize()));
        // END: com.azure.storage.file.datalake.DataLakePathAsyncClient.getPropertiesWithResponse#DataLakeRequestConditions
    }

    /**
     * Code snippets for {@link DataLakePathAsyncClient#setAccessControlList(List, String, String)}
     */
    public void setAccessControlCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakePathAsyncClient.setAccessControlList#List-String-String
        PathAccessControlEntry pathAccessControlEntry = new PathAccessControlEntry()
            .entityID("entityId")
            .permissions(new RolePermissions().read(true));
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
        DataLakeRequestConditions accessConditions = new DataLakeRequestConditions().setLeaseId(leaseId);
        PathAccessControlEntry pathAccessControlEntry = new PathAccessControlEntry()
            .entityID("entityId")
            .permissions(new RolePermissions().read(true));
        List<PathAccessControlEntry> pathAccessControlEntries = new ArrayList<>();
        pathAccessControlEntries.add(pathAccessControlEntry);
        String group = "group";
        String owner = "owner";

        client.setAccessControlListWithResponse(pathAccessControlEntries, group, owner, accessConditions).subscribe(
            response -> System.out.printf("Last Modified Time: %s", response.getValue().getLastModified()));
        // END: com.azure.storage.file.datalake.DataLakePathAsyncClient.setAccessControlListWithResponse#List-String-String-DataLakeRequestConditions
    }

    /**
     * Code snippets for {@link DataLakePathAsyncClient#setPermissions(PathPermissions, String, String)}
     */
    public void setPermissionsCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakePathAsyncClient.setPermissions#PathPermissions-String-String
        PathPermissions permissions = new PathPermissions()
            .group(new RolePermissions().execute(true).read(true))
            .owner(new RolePermissions().execute(true).read(true).write(true))
            .other(new RolePermissions().read(true));
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
        DataLakeRequestConditions accessConditions = new DataLakeRequestConditions().setLeaseId(leaseId);
        PathPermissions permissions = new PathPermissions()
            .group(new RolePermissions().execute(true).read(true))
            .owner(new RolePermissions().execute(true).read(true).write(true))
            .other(new RolePermissions().read(true));
        String group = "group";
        String owner = "owner";

        client.setPermissionsWithResponse(permissions, group, owner, accessConditions).subscribe(
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
        DataLakeRequestConditions accessConditions = new DataLakeRequestConditions().setLeaseId(leaseId);
        boolean returnUpn = false;

        client.getAccessControlWithResponse(returnUpn, accessConditions).subscribe(
            response -> System.out.printf("Access Control List: %s, Group: %s, Owner: %s, Permissions: %s",
                PathAccessControlEntry.serializeList(response.getValue().getAccessControlList()),
                response.getValue().getGroup(), response.getValue().getOwner(), response.getValue().getPermissions()));
        // END: com.azure.storage.file.datalake.DataLakePathAsyncClient.getAccessControlWithResponse#boolean-DataLakeRequestConditions
    }

}
