// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.test.aad.selenium;

import com.azure.spring.utils.AzureCloudUrls;
import com.azure.test.aad.common.AbstractAADSeleniumITHelper;

import java.util.HashMap;
import java.util.Map;

import static com.azure.spring.test.EnvironmentVariable.AAD_MULTI_TENANT_CLIENT_ID;
import static com.azure.spring.test.EnvironmentVariable.AAD_MULTI_TENANT_CLIENT_SECRET;
import static com.azure.spring.test.EnvironmentVariable.AAD_TENANT_ID_1;
import static com.azure.spring.test.EnvironmentVariable.AZURE_CLOUD_TYPE;

public class AADSeleniumITHelper extends AbstractAADSeleniumITHelper {

    public static Map<String, String> createDefaultProperties() {
        Map<String, String> properties = new HashMap<>();
        properties.put("azure.activedirectory.tenant-id", AAD_TENANT_ID_1);
        properties.put("azure.activedirectory.client-id", AAD_MULTI_TENANT_CLIENT_ID);
        properties.put("azure.activedirectory.client-secret", AAD_MULTI_TENANT_CLIENT_SECRET);
        properties.put("azure.activedirectory.user-group.allowed-groups", "group1");
        properties.put("azure.activedirectory.post-logout-redirect-uri", "http://localhost:${server.port}");
        properties.put("azure.activedirectory.base-uri", AzureCloudUrls.getBaseUrl(AZURE_CLOUD_TYPE));
        properties.put("azure.activedirectory.graph-base-uri", AzureCloudUrls.getGraphBaseUrl(AZURE_CLOUD_TYPE));
        properties.put("azure.activedirectory.application-type", "web_application_and_resource_server");

        properties.put("azure.activedirectory.app-id-uri", "api://" + AAD_MULTI_TENANT_CLIENT_ID);
        properties.put("azure.activedirectory.authorization-clients.graph.scopes",
            "https://graph.microsoft.com/User.Read");
        properties.put("azure.activedirectory.authorization-clients.graph.authorization-grant-type", "on_behalf_of");
        return properties;
    }

    public AADSeleniumITHelper(Class<?> appClass, Map<String, String> properties, String username, String password) {
        super(appClass, properties, AZURE_CLOUD_TYPE,
            AAD_TENANT_ID_1, username, password);
    }
}
