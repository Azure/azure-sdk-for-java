// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.containerregistry;

import com.azure.core.http.HttpPipeline;
import com.azure.resourcemanager.resources.core.TestBase;
import com.azure.resourcemanager.resources.fluentcore.profile.AzureProfile;
import com.azure.resourcemanager.resources.ResourceManager;

/** The base for storage manager tests. */
public abstract class RegistryTest extends TestBase {
    protected ResourceManager resourceManager;
    protected ContainerRegistryManager registryManager;
    protected String rgName;

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        resourceManager =
            ResourceManager.authenticate(httpPipeline, profile).withSdkContext(sdkContext).withDefaultSubscription();

        registryManager = ContainerRegistryManager.authenticate(httpPipeline, profile, sdkContext);

        rgName = generateRandomResourceName("rgacr", 10);
    }
}
