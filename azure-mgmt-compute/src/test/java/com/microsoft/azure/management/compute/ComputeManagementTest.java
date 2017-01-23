package com.microsoft.azure.management.compute;

import com.microsoft.azure.management.compute.implementation.ComputeManager;
import com.microsoft.azure.management.network.implementation.NetworkManager;
import com.microsoft.azure.management.resources.core.TestBase;
import com.microsoft.azure.management.resources.implementation.ResourceManager;
import com.microsoft.azure.management.storage.implementation.StorageManager;
import com.microsoft.rest.RestClient;

public abstract class ComputeManagementTest extends TestBase {
    protected static ResourceManager resourceManager;
    protected static ComputeManager computeManager;
    protected static NetworkManager networkManager;
    protected static StorageManager storageManager;

    @Override
    protected void initializeClients(RestClient restClient, String defaultSubscription, String domain) {
        resourceManager = ResourceManager
                .authenticate(restClient)
                .withSubscription(defaultSubscription);

        computeManager = ComputeManager
                .authenticate(restClient, defaultSubscription);

        networkManager = NetworkManager
                .authenticate(restClient, defaultSubscription);

        storageManager = StorageManager
                .authenticate(restClient, defaultSubscription);
    }

    @Override
    protected void cleanUpResources() {

    }
}
