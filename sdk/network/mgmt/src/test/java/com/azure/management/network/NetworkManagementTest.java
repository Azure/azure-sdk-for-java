/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.network;

import com.azure.management.RestClient;
import com.azure.management.keyvault.implementation.KeyVaultManager;
import com.azure.management.msi.implementation.MSIManager;
import com.azure.management.network.implementation.NetworkManager;
import com.azure.management.resources.core.TestBase;
import com.azure.management.resources.implementation.ResourceManager;

public class NetworkManagementTest extends TestBase {
    protected ResourceManager resourceManager;
    protected NetworkManager networkManager;
    protected KeyVaultManager keyVaultManager;
    protected MSIManager msiManager;
    protected String rgName = "";

    @Override
    protected void initializeClients(RestClient restClient, String defaultSubscription, String domain) {
        rgName = generateRandomResourceName("javanwmrg", 15);

        resourceManager = ResourceManager
                .authenticate(restClient)
                .withSdkContext(sdkContext)
                .withSubscription(defaultSubscription);

        networkManager = NetworkManager
                .authenticate(restClient, defaultSubscription, sdkContext);

        keyVaultManager = KeyVaultManager
                .authenticate(restClient, domain, defaultSubscription, sdkContext);

        msiManager = MSIManager
                .authenticate(restClient, defaultSubscription, sdkContext);
    }

    @Override
    protected void cleanUpResources() {
        resourceManager.resourceGroups().beginDeleteByName(rgName);
    }
}