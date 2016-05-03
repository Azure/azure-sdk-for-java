/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.implementation;

import com.microsoft.azure.management.compute.AvailabilitySets;
import com.microsoft.azure.management.compute.VirtualMachines;
import com.microsoft.azure.management.compute.implementation.ComputeManager;
import com.microsoft.azure.management.resources.GenericResources;
import com.microsoft.azure.management.resources.implementation.ResourceManager;
import com.microsoft.azure.management.resources.implementation.api.ResourceManagementClientImpl;
import com.microsoft.azure.management.storage.StorageAccounts;
import com.microsoft.azure.management.storage.Usages;
import com.microsoft.azure.management.storage.implementation.StorageManager;
import com.microsoft.rest.RestClient;

final class AzureSubscriptionImpl
        implements Azure.Subscription {
    private final String subscriptionId;
    private final RestClient restClient;
    // service specific managers in subscription level.
    private ResourceManager.Subscription resourceClient;
    private StorageManager.Subscription storageClient;
    private ComputeManager.Subscription computeClient;
    // SDK Clients
    // TODO: Get rid of these
    private ResourceManagementClientImpl resourceManagementClient;
    // Fluent Collections
    private Azure.ResourceGroups azureResourceGroups;
    private AvailabilitySets availabilitySets;

    AzureSubscriptionImpl(RestClient restClient, String subscriptionId) {
        this.restClient = restClient;
        this.subscriptionId = subscriptionId;
    }

    @Override
    public Azure.ResourceGroups resourceGroups() {
        if (azureResourceGroups == null) {
            azureResourceGroups = new AzureResourceGroupsImpl(resourceManagementClient());
        }
        return azureResourceGroups;
    }

    @Override
    public GenericResources genericResources() {
        return resourceClient().genericResources();
    }

    @Override
    public StorageAccounts storageAccounts() {
        return storageClient().storageAccounts();
    }

    @Override
    public Usages storageUsages() {
        return storageClient().usages();
    }

    @Override
    public AvailabilitySets availabilitySets() {
        return computeClient().availabilitySets();
    }

    @Override
    public VirtualMachines virtualMachines() {
        return computeClient().virtualMachines();
    }

    private ResourceManager.Subscription resourceClient() {
        if (resourceClient == null) {
            resourceClient = ResourceManager
                    .authenticate(restClient)
                    .withSubscription(subscriptionId);
        }
        return resourceClient;
    }

    private StorageManager.Subscription storageClient() {
        if (storageClient == null) {
            storageClient = StorageManager
                    .authenticate(restClient)
                    .withSubscription(subscriptionId);
        }
        return storageClient;
    }

    private ComputeManager.Subscription computeClient() {
        if (computeClient == null) {
            computeClient = ComputeManager
                    .authenticate(restClient)
                    .withSubscription(subscriptionId);
        }
        return computeClient;
    }

    private ResourceManagementClientImpl resourceManagementClient() {
        if (resourceManagementClient == null) {
            resourceManagementClient = new ResourceManagementClientImpl(restClient);
            resourceManagementClient.setSubscriptionId(subscriptionId);
        }
        return resourceManagementClient;
    }
}
