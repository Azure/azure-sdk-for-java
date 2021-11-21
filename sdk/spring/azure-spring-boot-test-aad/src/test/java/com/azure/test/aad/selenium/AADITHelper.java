// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.test.aad.selenium;

import com.azure.spring.utils.AzureCloudUrls;

import java.util.HashMap;
import java.util.Map;

import static com.azure.spring.test.EnvironmentVariable.AAD_SINGLE_TENANT_CLIENT_ID;
import static com.azure.spring.test.EnvironmentVariable.AAD_SINGLE_TENANT_CLIENT_SECRET;
import static com.azure.spring.test.EnvironmentVariable.AAD_TENANT_ID_1;
import static com.azure.spring.test.EnvironmentVariable.AZURE_CLOUD_TYPE;

public class AADITHelper {

    public static Map<String, String> createDefaultProperties() {
        Map<String, String> defaultProperties = new HashMap<>();
        defaultProperties.put("azure.activedirectory.tenant-id", AAD_TENANT_ID_1);
        defaultProperties.put("azure.activedirectory.client-id", AAD_SINGLE_TENANT_CLIENT_ID);
        defaultProperties.put("azure.activedirectory.client-secret", AAD_SINGLE_TENANT_CLIENT_SECRET);
        defaultProperties.put("azure.activedirectory.user-group.allowed-groups", "group1");
        defaultProperties.put("azure.activedirectory.post-logout-redirect-uri", "http://localhost:${server.port}");
        defaultProperties.put("azure.activedirectory.base-uri", AzureCloudUrls.getBaseUrl(AZURE_CLOUD_TYPE));
        defaultProperties.put("azure.activedirectory.graph-base-uri", AzureCloudUrls.getGraphBaseUrl(AZURE_CLOUD_TYPE));
        return defaultProperties;
    }
}
