// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

public class AzureEnvironmentTests {

    @Test
    public void testPredefinedEnv() {
        // Azure
        AzureEnvironment env = AzureEnvironment.AZURE;

        Assertions.assertEquals("https://management.azure.com/", env.getResourceManagerEndpoint());
        Assertions.assertEquals("https://graph.microsoft.com/", env.getMicrosoftGraphEndpoint());
        Assertions.assertEquals(".vault.azure.net", env.getKeyVaultDnsSuffix());
        Assertions.assertEquals("https://management.core.windows.net/", env.getManagementEndpoint());

        Assertions.assertEquals("https://management.azure.com/",
            env.getUrlByEndpoint(AzureEnvironment.Endpoint.RESOURCE_MANAGER));
        Assertions.assertEquals("https://graph.microsoft.com/",
            env.getUrlByEndpoint(AzureEnvironment.Endpoint.MICROSOFT_GRAPH));
        Assertions.assertEquals(".core.windows.net", env.getUrlByEndpoint(AzureEnvironment.Endpoint.STORAGE));
        Assertions.assertEquals(".vault.azure.net", env.getUrlByEndpoint(AzureEnvironment.Endpoint.KEYVAULT));
        Assertions.assertEquals("https://management.core.windows.net/",
            env.getUrlByEndpoint(AzureEnvironment.Endpoint.MANAGEMENT));

        Assertions.assertEquals("https://management.azure.com/", env.getEndpoints().get("resourceManagerEndpointUrl"));
        Assertions.assertEquals(".managedhsm.azure.net", env.getUrlByEndpoint(AzureEnvironment.Endpoint.MANAGED_HSM));

        // Azure China
        AzureEnvironment envChina = AzureEnvironment.AZURE_CHINA;
        Assertions.assertEquals(".managedhsm.azure.cn",
            envChina.getUrlByEndpoint(AzureEnvironment.Endpoint.MANAGED_HSM));

        // Azure US Government
        AzureEnvironment envUsGov = AzureEnvironment.AZURE_US_GOVERNMENT;
        Assertions.assertEquals(".managedhsm.usgovcloudapi.net",
            envUsGov.getUrlByEndpoint(AzureEnvironment.Endpoint.MANAGED_HSM));
    }

    @Test
    public void testLoadedEnv() {
        String resourceManagerEndpoint = "https://management.redmond.azurestack.corp.microsoft.com/";
        String audienceEndpoint = "https://management.adfs.redmond.selfhost.local/d48c9ef1-e46d-4148-b41e-ca95b463f5a8";
        Map<String, String> envMap = new LinkedHashMap<>();
        envMap.put("resourceManagerEndpointUrl", resourceManagerEndpoint);
        envMap.put("microsoftGraphResourceId", "N/A");
        envMap.put("managementEndpointUrl", audienceEndpoint);

        AzureEnvironment env = new AzureEnvironment(envMap);

        Assertions.assertEquals(resourceManagerEndpoint, env.getResourceManagerEndpoint());
        Assertions.assertEquals("N/A", env.getMicrosoftGraphEndpoint());
        Assertions.assertEquals(audienceEndpoint, env.getManagementEndpoint());
    }
}
