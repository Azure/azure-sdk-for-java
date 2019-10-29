// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import com.azure.core.http.rest.Response;
import com.azure.storage.file.datalake.models.DataLakeRequestConditions;
import com.azure.storage.file.datalake.models.PathAccessControl;

import java.util.Collections;
import java.util.Map;
import java.time.Duration;
import com.azure.core.util.Context;
import com.azure.storage.file.datalake.models.PathHttpHeaders;
import com.azure.storage.file.datalake.models.PathInfo;
import com.azure.storage.file.datalake.models.PathProperties;

/**
 * Code snippets for {@link FileSystemClient}
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
     * Code snippets for {@link PathClient#setMetadata(Map)}
     */
    public void setMetadataCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.PathClient.setMetadata#Map
        client.setMetadata(Collections.singletonMap("metadata", "value"));
        System.out.println("Set metadata completed");
        // END: com.azure.storage.file.datalake.PathClient.setMetadata#Map
    }

    /**
     * Code snippets for {@link PathClient#setMetadataWithResponse(Map, DataLakeRequestConditions, Duration, Context)}
     */
    public void setMetadataWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.PathClient.setMetadata#Map-DataLakeRequestConditions-Duration-Context
        DataLakeRequestConditions accessConditions = new DataLakeRequestConditions().setLeaseId(leaseId);

        client.setMetadataWithResponse(Collections.singletonMap("metadata", "value"), accessConditions, timeout,
            new Context(key2, value2));
        System.out.println("Set metadata completed");
        // END: com.azure.storage.file.datalake.PathClient.setMetadata#Map-DataLakeRequestConditions-Duration-Context
    }

    /**
     * Code snippets for {@link PathClient#setHttpHeaders(PathHttpHeaders)}
     */
    public void setHTTPHeadersCodeSnippet() {
        // BEGIN: com.azure.storage.file.datalake.PathClient.setHttpHeaders#PathHttpHeaders
        client.setHttpHeaders(new PathHttpHeaders()
            .setContentLanguage("en-US")
            .setContentType("binary"));
        // END: com.azure.storage.file.datalake.PathClient.setHttpHeaders#PathHttpHeaders
    }

    /**
     * Code snippets for {@link PathClient#setHttpHeadersWithResponse(PathHttpHeaders, DataLakeRequestConditions, Duration, Context)}
     */
    public void setHTTPHeadersWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.PathClient.setHttpHeadersWithResponse#PathHttpHeaders-DataLakeRequestConditions-Duration-Context
        DataLakeRequestConditions accessConditions = new DataLakeRequestConditions().setLeaseId(leaseId);

        Response<Void> response = client.setHttpHeadersWithResponse(new PathHttpHeaders()
            .setContentLanguage("en-US")
            .setContentType("binary"), accessConditions, timeout, new Context(key2, value2));
        System.out.printf("Set HTTP headers completed with status %d%n",
                    response.getStatusCode());
        // END: com.azure.storage.file.datalake.PathClient.setHttpHeadersWithResponse#PathHttpHeaders-DataLakeRequestConditions-Duration-Context
    }

    /**
     * Code snippets for {@link PathClient#getProperties()}
     */
    public void getPropertiesCodeSnippet() {
        // BEGIN: com.azure.storage.file.datalake.PathClient.getProperties
        System.out.printf("Creation Time: %s, Size: %d%n", client.getProperties().getCreationTime(),
            client.getProperties().getFileSize());
        // END: com.azure.storage.file.datalake.PathClient.getProperties
    }

    /**
     * Code snippets for {@link PathClient#getPropertiesWithResponse(DataLakeRequestConditions, Duration, Context)}
     */
    public void getPropertiesWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.PathClient.getPropertiesWithResponse#DataLakeRequestConditions-Duration-Context
        DataLakeRequestConditions accessConditions = new DataLakeRequestConditions().setLeaseId(leaseId);

        Response<PathProperties> response = client.getPropertiesWithResponse(accessConditions, timeout,
            new Context(key2, value2));

        System.out.printf("Creation Time: %s, Size: %d%n", response.getValue().getCreationTime(),
            response.getValue().getFileSize());
        // END: com.azure.storage.file.datalake.PathClient.getPropertiesWithResponse#DataLakeRequestConditions-Duration-Context
    }

    /**
     * Code snippets for {@link PathClient#setAccessControl(PathAccessControl)}
     */
    public void setAccessControlCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.PathClient.setAccessControl#PathAccessControl
        PathAccessControl control = new PathAccessControl()
            .setPermissions("0766");

        System.out.printf("Last Modified Time: %s", client.setAccessControl(control).getLastModified());
        // END: com.azure.storage.file.datalake.PathClient.setAccessControl#PathAccessControl
    }

    /**
     * Code snippets for {@link PathClient#setAccessControlWithResponse(PathAccessControl, DataLakeRequestConditions, Duration, Context)}
     */
    public void setAccessControlWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.PathClient.setAccessControlWithResponse#PathAccessControl-DataLakeRequestConditions-Duration-Context
        DataLakeRequestConditions accessConditions = new DataLakeRequestConditions().setLeaseId(leaseId);
        PathAccessControl control = new PathAccessControl().setPermissions("0766");

        Response<PathInfo> response = client.setAccessControlWithResponse(control, accessConditions, timeout,
            new Context(key2, value2));
        System.out.printf("Last Modified Time: %s", response.getValue().getLastModified());
        // END: com.azure.storage.file.datalake.PathClient.setAccessControlWithResponse#PathAccessControl-DataLakeRequestConditions-Duration-Context
    }

    /**
     * Code snippets for {@link PathClient#getAccessControl()}
     */
    public void getAccessControlCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.PathClient.getAccessControl
        PathAccessControl response = client.getAccessControl();
        System.out.printf("Access Control List: %s, Group: %s, Owner: %s, Permissions: %s",
                response.getAcl(), response.getGroup(), response.getOwner(), response.getPermissions());
        // END: com.azure.storage.file.datalake.PathClient.getAccessControl
    }

    /**
     * Code snippets for {@link PathClient#getAccessControlWithResponse(boolean, DataLakeRequestConditions, Duration, Context)}
     */
    public void getAccessControlWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.file.datalake.PathClient.getAccessControlWithResponse#boolean-DataLakeRequestConditions-Duration-Context
        DataLakeRequestConditions accessConditions = new DataLakeRequestConditions().setLeaseId(leaseId);
        boolean returnUpn = false;

        Response<PathAccessControl> response = client.getAccessControlWithResponse(returnUpn, accessConditions, timeout,
            new Context(key1, value1));

       System.out.printf("Access Control List: %s, Group: %s, Owner: %s, Permissions: %s",
                response.getValue().getAcl(), response.getValue().getGroup(), response.getValue().getOwner(),
                response.getValue().getPermissions());
        // END: com.azure.storage.file.datalake.PathClient.getAccessControlWithResponse#boolean-DataLakeRequestConditions-Duration-Context}
    }

}
