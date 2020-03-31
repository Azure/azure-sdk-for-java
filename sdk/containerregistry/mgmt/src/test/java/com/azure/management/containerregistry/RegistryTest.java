/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.containerregistry;


import com.azure.management.RestClient;
import com.azure.management.containerregistry.implementation.ContainerRegistryManager;
import com.azure.management.resources.core.TestBase;
import com.azure.management.resources.implementation.ResourceManager;

/**
 * The base for storage manager tests.
 */
public abstract class RegistryTest extends TestBase {
    protected ResourceManager resourceManager;
    protected ContainerRegistryManager registryManager;
    protected String rgName;

    @Override
    protected void initializeClients(RestClient restClient, String defaultSubscription, String domain) {
        resourceManager = ResourceManager
                .authenticate(restClient)
                .withSdkContext(sdkContext)
                .withSubscription(defaultSubscription);

        registryManager = ContainerRegistryManager
                .authenticate(restClient, defaultSubscription, sdkContext);

        rgName = generateRandomResourceName("rgacr", 10);
    }


}
