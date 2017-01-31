package com.microsoft.azure.management.resources;

import com.microsoft.azure.management.resources.core.TestBase;
import com.microsoft.azure.management.resources.implementation.ResourceManager;
import com.microsoft.rest.RestClient;

/**
 * The base for resource manager tests.
 */
class ResourceManagerTestBase extends TestBase {
    protected static ResourceManager resourceClient;

    @Override
    protected void initializeClients(RestClient restClient, String defaultSubscription, String domain) {
        resourceClient = ResourceManager
                .authenticate(restClient)
                .withSubscription(defaultSubscription);
    }

    @Override
    protected void cleanUpResources() {

    }
}
