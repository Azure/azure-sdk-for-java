package com.microsoft.azure.implementation;

import com.microsoft.azure.management.resources.implementation.api.ResourceManagementClientImpl;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.implementation.ResourceGroupImpl;
import com.microsoft.azure.management.storage.StorageAccounts;
import com.microsoft.azure.management.storage.implementation.StorageResourceConnector;

public class AzureResourceGroupImpl extends ResourceGroupImpl implements Azure.ResourceGroup {
    private ResourceGroup resourceGroupCore;

    public AzureResourceGroupImpl(ResourceGroup resourceGroupCore, ResourceManagementClientImpl client) {
        super(resourceGroupCore.inner(), client);
        this.resourceGroupCore = resourceGroupCore;
    }

    // StorageAccount collection having a resource resourceGroupCore context.
    //
    public StorageAccounts.InGroup storageAccounts() {
        return resourceGroupCore.connectToResource(new StorageResourceConnector.Builder()).storageAccounts();
    }

    public ResourceGroup resourceGroupCore() {
        return resourceGroupCore;
    }
}
