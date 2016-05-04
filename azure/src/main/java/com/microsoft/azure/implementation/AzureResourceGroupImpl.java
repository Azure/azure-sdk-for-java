package com.microsoft.azure.implementation;

import com.microsoft.azure.management.compute.AvailabilitySets;
import com.microsoft.azure.management.compute.implementation.ComputeResourceConnector;
import com.microsoft.azure.management.resources.implementation.api.ResourceManagementClientImpl;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.implementation.ResourceGroupImpl;
import com.microsoft.azure.management.storage.StorageAccounts;
import com.microsoft.azure.management.storage.implementation.StorageResourceConnector;

public class AzureResourceGroupImpl extends ResourceGroupImpl implements Azure.ResourceGroup {
    private ResourceGroup resourceGroupCore;
    private StorageAccounts.InGroup storageAccounts;
    private AvailabilitySets.InGroup availabilitySets;

    public AzureResourceGroupImpl(ResourceGroup resourceGroupCore, ResourceManagementClientImpl client) {
        super(resourceGroupCore.inner(), client);
        this.resourceGroupCore = resourceGroupCore;
    }

    public StorageAccounts.InGroup storageAccounts() {
        if (storageAccounts == null) {
            storageAccounts =  resourceGroupCore.connectToResource(new StorageResourceConnector.Builder()).storageAccounts();
        }
        return storageAccounts;
    }

    public AvailabilitySets.InGroup availabilitySets() {
        if (availabilitySets == null) {
            availabilitySets = resourceGroupCore.connectToResource(new ComputeResourceConnector.Builder()).availabilitySets();
        }
        return availabilitySets;
    }
}
