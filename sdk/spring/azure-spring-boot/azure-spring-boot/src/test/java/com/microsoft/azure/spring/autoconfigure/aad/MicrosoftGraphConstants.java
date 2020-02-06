/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.autoconfigure.aad;

import java.util.Arrays;
import java.util.List;

public class MicrosoftGraphConstants {
    public static final String SERVICE_ENVIRONMENT_PROPERTY = "azure.activedirectory.environment";
    public static final String CLIENT_ID_PROPERTY = "azure.activedirectory.client-id";
    public static final String CLIENT_SECRET_PROPERTY = "azure.activedirectory.client-secret";
    public static final String TARGETED_GROUPS_PROPERTY = "azure.activedirectory.active-directory-groups";
    public static final String TENANT_ID_PROPERTY = "azure.activedirectory.tenant-id";

    public static final String DEFAULT_ENVIRONMENT = "global";
    public static final String CLIENT_ID = "real_client_id";
    public static final String CLIENT_SECRET = "real_client_secret";
    public static final List<String> TARGETED_GROUPS = Arrays.asList("group1", "group2", "group3");

    public static final String TOKEN_HEADER = "Authorization";
    public static final String BEARER_TOKEN = "Bearer real_jtw_bearer_token";

    public static final String USERGROUPS_JSON = "{\n" +
    "    \"odata.metadata\": \"https://graph.windows.net/myorganization/$metadata#directoryObjects\",\n" +
    "    \"value\": [\n" +
    "        {\n" +
    "            \"@odata.type\": \"#microsoft.graph.group\",\n" +
    "            \"id\": \"12345678-7baf-48ce-96f4-a2d60c26391e\",\n" +
    "            \"deletedDateTime\": null,\n" +
    "            \"classification\": null,\n" +
    "            \"createdDateTime\": \"2017-08-02T12:54:37Z\",\n" +
    "            \"creationOptions\": [],\n" +
    "            \"description\": \"this is group1\",\n" +
    "            \"displayName\": \"group1\",\n" +
    "            \"groupTypes\": [],\n" +
    "            \"mail\": null,\n" +
    "            \"mailEnabled\": false,\n" +
    "            \"mailNickname\": \"something\",\n" +
    "            \"onPremisesLastSyncDateTime\": null,\n" +
    "            \"onPremisesSecurityIdentifier\": null,\n" +
    "            \"onPremisesSyncEnabled\": null,\n" +
    "            \"preferredDataLocation\": null,\n" +
    "            \"proxyAddresses\": [],\n" +
    "            \"renewedDateTime\": \"2017-08-02T12:54:37Z\",\n" +
    "            \"resourceBehaviorOptions\": [],\n" +
    "            \"resourceProvisioningOptions\": [],\n" +
    "            \"securityEnabled\": true,\n" +
    "            \"visibility\": null,\n" +
    "            \"onPremisesProvisioningErrors\": []\n" +
    "        },\n" +
    "        {\n" +
    "            \"@odata.type\": \"#microsoft.graph.group\",\n" +
    "            \"id\": \"12345678-e757-4474-b9c4-3f00a9ac17a0\",\n" +
    "            \"deletedDateTime\": null,\n" +
    "            \"classification\": null,\n" +
    "            \"createdDateTime\": \"2017-08-09T13:45:03Z\",\n" +
    "            \"creationOptions\": [],\n" +
    "            \"description\": \"this is group2\",\n" +
    "            \"displayName\": \"group2\",\n" +
    "            \"groupTypes\": [],\n" +
    "            \"mail\": null,\n" +
    "            \"mailEnabled\": false,\n" +
    "            \"mailNickname\": \"somethingelse\",\n" +
    "            \"onPremisesLastSyncDateTime\": null,\n" +
    "            \"onPremisesSecurityIdentifier\": null,\n" +
    "            \"onPremisesSyncEnabled\": null,\n" +
    "            \"preferredDataLocation\": null,\n" +
    "            \"proxyAddresses\": [],\n" +
    "            \"renewedDateTime\": \"2017-08-09T13:45:03Z\",\n" +
    "            \"resourceBehaviorOptions\": [],\n" +
    "            \"resourceProvisioningOptions\": [],\n" +
    "            \"securityEnabled\": true,\n" +
    "            \"visibility\": null,\n" +
    "            \"onPremisesProvisioningErrors\": []\n" +
    "        },\n" +
    "        {\n" +
    "            \"@odata.type\": \"#microsoft.graph.group\",\n" +
    "            \"id\": \"12345678-86a4-4237-aeb0-60bad29c1de0\",\n" +
    "            \"deletedDateTime\": null,\n" +
    "            \"classification\": null,\n" +
    "            \"createdDateTime\": \"2017-08-09T05:41:43Z\",\n" +
    "            \"creationOptions\": [],\n" +
    "            \"description\": \"this is group3\",\n" +
    "            \"displayName\": \"group3\",\n" +
    "            \"groupTypes\": [],\n" +
    "            \"mail\": null,\n" +
    "            \"mailEnabled\": false,\n" +
    "            \"mailNickname\": \"somethingelse\",\n" +
    "            \"onPremisesLastSyncDateTime\": null,\n" +
    "            \"onPremisesSecurityIdentifier\": null,\n" +
    "            \"onPremisesSyncEnabled\": null,\n" +
    "            \"preferredDataLocation\": null,\n" +
    "            \"proxyAddresses\": [],\n" +
    "            \"renewedDateTime\": \"2017-08-09T05:41:43Z\",\n" +
    "            \"resourceBehaviorOptions\": [],\n" +
    "            \"resourceProvisioningOptions\": [],\n" +
    "            \"securityEnabled\": true,\n" +
    "            \"visibility\": null,\n" +
    "            \"onPremisesProvisioningErrors\": []\n" +
    "        }" +
    "],\n" +
    "    \"odata.nextLink\": \"directoryObjects/$/Microsoft.DirectoryServices.User/" +
    "12345678-2898-434a-a370-8ec974c2fb57/memberOf?$skiptoken=X'4453707407000100000000" +
    "00000000100000009D29CBA7B45D854A84FF7F9B636BD9DC000000000000000000000017312E322E3" +
    "834302E3131333535362E312E342E3233333100000000'\"\n" +
    "}";

    /** Token from https://docs.microsoft.com/en-us/azure/active-directory/develop/v2-id-and-access-tokens */
    public static final String JWT_TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6Ik1uQ19WWmNBVGZNNXBPWWlKSE1" +
            "iYTlnb0VLWSJ9.eyJhdWQiOiI2NzMxZGU3Ni0xNGE2LTQ5YWUtOTdiYy02ZWJhNjkxNDM5MWUiLCJpc3MiOiJodHRwczovL2xvZ2lu" +
            "Lm1pY3Jvc29mdG9ubGluZS5jb20vYjk0MTk4MTgtMDlhZi00OWMyLWIwYzMtNjUzYWRjMWYzNzZlL3YyLjAiLCJpYXQiOjE0NTIyOD" +
            "UzMzEsIm5iZiI6MTQ1MjI4NTMzMSwiZXhwIjoxNDUyMjg5MjMxLCJuYW1lIjoiQmFiZSBSdXRoIiwibm9uY2UiOiIxMjM0NSIsIm9p" +
            "ZCI6ImExZGJkZGU4LWU0ZjktNDU3MS1hZDkzLTMwNTllMzc1MGQyMyIsInByZWZlcnJlZF91c2VybmFtZSI6InRoZWdyZWF0YmFtYm" +
            "lub0BueXkub25taWNyb3NvZnQuY29tIiwic3ViIjoiTUY0Zi1nZ1dNRWppMTJLeW5KVU5RWnBoYVVUdkxjUXVnNWpkRjJubDAxUSIs" +
            "InRpZCI6ImI5NDE5ODE4LTA5YWYtNDljMi1iMGMzLTY1M2FkYzFmMzc2ZSIsInZlciI6IjIuMCJ9.p_rYdrtJ1oCmgDBggNHB9O38K" +
            "TnLCMGbMDODdirdmZbmJcTHiZDdtTc-hguu3krhbtOsoYM2HJeZM3Wsbp_YcfSKDY--X_NobMNsxbT7bqZHxDnA2jTMyrmt5v2EKUn" +
            "EeVtSiJXyO3JWUq9R0dO-m4o9_8jGP6zHtR62zLaotTBYHmgeKpZgTFB9WtUq8DVdyMn_HSvQEfz-LWqckbcTwM_9RNKoGRVk38KCh" +
            "VJo4z5LkksYRarDo8QgQ7xEKmYmPvRr_I7gvM2bmlZQds2OeqWLB1NSNbFZqyFOCgYn3bAQ-nEQSKwBaA36jYGPOVG2r2Qv1uKcpSO" +
            "xzxaQybzYpQ";
}
