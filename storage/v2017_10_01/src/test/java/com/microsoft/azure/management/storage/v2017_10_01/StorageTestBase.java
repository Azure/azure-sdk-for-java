package com.microsoft.azure.management.storage.v2017_10_01;

import com.microsoft.azure.arm.core.TestBase;
import com.microsoft.azure.management.resources.implementation.ResourceManager;
import com.microsoft.azure.management.storage.v2017_10_01.implementation.StorageManager;
import com.microsoft.rest.RestClient;

public abstract class StorageTestBase extends TestBase {
    protected static ResourceManager resourceManager;
    protected static StorageManager storageManager;

    @Override
    protected void initializeClients(RestClient restClient, String defaultSubscription, String domain) {
        resourceManager = ResourceManager
                .authenticate(restClient)
                .withSubscription(defaultSubscription);

        storageManager = StorageManager
                .authenticate(restClient, defaultSubscription);
    }
}
