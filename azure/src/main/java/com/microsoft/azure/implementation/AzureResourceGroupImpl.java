package com.microsoft.azure.implementation;

import com.microsoft.azure.management.compute.AvailabilitySets;
import com.microsoft.azure.management.compute.implementation.ComputeResourceConnector;
import com.microsoft.azure.management.resources.Deployments;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.model.Provisionable;
import com.microsoft.azure.management.resources.implementation.ARMResourceConnector;
import com.microsoft.azure.management.resources.implementation.ResourceGroupImpl;
import com.microsoft.azure.management.resources.implementation.api.ResourceManagementClientImpl;
import com.microsoft.azure.management.storage.StorageAccounts;
import com.microsoft.azure.management.storage.implementation.StorageResourceConnector;

import java.util.List;

final class AzureResourceGroupImpl extends ResourceGroupImpl implements Azure.ResourceGroup {
    private final ARMResourceConnector armResourceConnector;
    private final StorageResourceConnector storageResourceConnector;
    private final ComputeResourceConnector computeResourceConnector;
    // Collections
    private Deployments.InGroup deployments;
    private StorageAccounts.InGroup storageAccounts;
    private AvailabilitySets.InGroup availabilitySets;

    AzureResourceGroupImpl(ResourceGroup resourceGroup, ResourceManagementClientImpl client) {
        super(resourceGroup.inner(), client);
        this.armResourceConnector = resourceGroup.connectToResource(new ARMResourceConnector.Builder());
        this.storageResourceConnector = resourceGroup.connectToResource(new StorageResourceConnector.Builder());
        this.computeResourceConnector = resourceGroup.connectToResource(new ComputeResourceConnector.Builder());
    }

    @Override
    public Deployments.InGroup deployments() {
        if (deployments == null) {
            deployments = armResourceConnector.deployments();
        }
        return deployments;
    }

    @Override
    public StorageAccounts.InGroup storageAccounts() {
        if (storageAccounts == null) {
            storageAccounts =  storageResourceConnector.storageAccounts();
        }
        return storageAccounts;
    }

    @Override
    public AvailabilitySets.InGroup availabilitySets() {
        if (availabilitySets == null) {
            availabilitySets = computeResourceConnector.availabilitySets();
        }
        return availabilitySets;
    }

    @Override
    public List<Provisionable<?>> prerequisites() {
        return null;
    }
}
