// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.management.network;

import com.azure.core.http.HttpPipeline;
import com.azure.management.keyvault.implementation.KeyVaultManager;
import com.azure.management.msi.implementation.MSIManager;
import com.azure.management.network.implementation.NetworkManager;
import com.azure.management.resources.core.TestBase;
import com.azure.management.resources.fluentcore.profile.AzureProfile;
import com.azure.management.resources.implementation.ResourceManager;

public class NetworkManagementTest extends TestBase {
    protected ResourceManager resourceManager;
    protected NetworkManager networkManager;
    protected KeyVaultManager keyVaultManager;
    protected MSIManager msiManager;
    protected String rgName = "";

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        rgName = generateRandomResourceName("javanwmrg", 15);

        resourceManager =
            ResourceManager.authenticate(httpPipeline, profile).withSdkContext(sdkContext).withDefaultSubscription();

        networkManager = NetworkManager.authenticate(httpPipeline, profile, sdkContext);

        keyVaultManager = KeyVaultManager.authenticate(httpPipeline, profile, sdkContext);

        msiManager = MSIManager.authenticate(httpPipeline, profile, sdkContext);
    }

    @Override
    protected void cleanUpResources() {
        resourceManager.resourceGroups().beginDeleteByName(rgName);
    }
}
