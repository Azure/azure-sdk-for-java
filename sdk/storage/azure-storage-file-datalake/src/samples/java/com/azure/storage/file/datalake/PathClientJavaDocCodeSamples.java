// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.storage.file.datalake.models.DataLakeRequestConditions;
import com.azure.storage.file.datalake.models.PathAccessControl;
import com.azure.storage.file.datalake.models.PathAccessControlEntry;
import com.azure.storage.file.datalake.models.PathHttpHeaders;
import com.azure.storage.file.datalake.models.PathInfo;
import com.azure.storage.file.datalake.models.PathPermissions;
import com.azure.storage.file.datalake.models.PathProperties;
import com.azure.storage.file.datalake.models.RolePermissions;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
        DataLakeRequestConditions accessConditions = new DataLakeRequestConditions().setLeaseId(leaseId);

        client.setMetadataWithResponse(Collections.singletonMap("metadata", "value"), accessConditions, timeout,
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
        DataLakeRequestConditions accessConditions = new DataLakeRequestConditions().setLeaseId(leaseId);

        Response<Void> response = client.setHttpHeadersWithResponse(new PathHttpHeaders()
            .setContentLanguage("en-US")
            .setContentType("binary"), accessConditions, timeout, new Context(key2, value2));
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
        DataLakeRequestConditions accessConditions = new DataLakeRequestConditions().setLeaseId(leaseId);

        Response<PathProperties> response = client.getPropertiesWithResponse(accessConditions, timeout,
            new Context(key2, value2));

        System.out.printf("Creation Time: %s, Size: %d%n", response.getValue().getCreationTime(),
            response.getValue().getFileSize());
        // END: com.azure.storage.file.datalake.DataLakePathClient.getPropertiesWithResponse#DataLakeRequestConditions-Duration-Context
    }

    /**
     * Code snippets for {@link DataLakePathClient#setAccessControlList(List, String, String)}
     */
    public void setAccessControlListCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakePathClient.setAccessControlList#List-String-String
        PathAccessControlEntry pathAccessControlEntry = new PathAccessControlEntry()
            .entityID("entityId")
            .permissions(new RolePermissions().read(true));
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
        DataLakeRequestConditions accessConditions = new DataLakeRequestConditions().setLeaseId(leaseId);
        PathAccessControlEntry pathAccessControlEntry = new PathAccessControlEntry()
            .entityID("entityId")
            .permissions(new RolePermissions().read(true));
        List<PathAccessControlEntry> pathAccessControlEntries = new ArrayList<>();
        pathAccessControlEntries.add(pathAccessControlEntry);
        String group = "group";
        String owner = "owner";

        Response<PathInfo> response = client.setAccessControlListWithResponse(pathAccessControlEntries, group, owner,
            accessConditions, timeout, new Context(key2, value2));
        System.out.printf("Last Modified Time: %s", response.getValue().getLastModified());
        // END: com.azure.storage.file.datalake.DataLakePathClient.setAccessControlListWithResponse#List-String-String-DataLakeRequestConditions-Duration-Context
    }

    /**
     * Code snippets for {@link DataLakePathClient#setPermissions(PathPermissions, String, String)}
     */
    public void setPermissionsCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakePathClient.setPermissions#PathPermissions-String-String
        PathPermissions permissions = new PathPermissions()
            .group(new RolePermissions().execute(true).read(true))
            .owner(new RolePermissions().execute(true).read(true).write(true))
            .other(new RolePermissions().read(true));
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
        DataLakeRequestConditions accessConditions = new DataLakeRequestConditions().setLeaseId(leaseId);
        PathPermissions permissions = new PathPermissions()
            .group(new RolePermissions().execute(true).read(true))
            .owner(new RolePermissions().execute(true).read(true).write(true))
            .other(new RolePermissions().read(true));
        String group = "group";
        String owner = "owner";

        Response<PathInfo> response = client.setPermissionsWithResponse(permissions, group, owner, accessConditions,
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
            PathAccessControlEntry.serializeList(response.accessControlList()), response.group(), response.owner(),
            response.permissions());
        // END: com.azure.storage.file.datalake.DataLakePathClient.getAccessControl
    }

    /**
     * Code snippets for {@link DataLakePathClient#getAccessControlWithResponse(boolean, DataLakeRequestConditions, Duration, Context)}
     */
    public void getAccessControlWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.DataLakePathClient.getAccessControlWithResponse#boolean-DataLakeRequestConditions-Duration-Context
        DataLakeRequestConditions accessConditions = new DataLakeRequestConditions().setLeaseId(leaseId);
        boolean returnUpn = false;

        Response<PathAccessControl> response = client.getAccessControlWithResponse(returnUpn, accessConditions, timeout,
            new Context(key1, value1));

        PathAccessControl pac = response.getValue();

        System.out.printf("Access Control List: %s, Group: %s, Owner: %s, Permissions: %s",
            PathAccessControlEntry.serializeList(pac.accessControlList()), pac.group(), pac.owner(),
            pac.permissions());
        // END: com.azure.storage.file.datalake.DataLakePathClient.getAccessControlWithResponse#boolean-DataLakeRequestConditions-Duration-Context
    }

}
