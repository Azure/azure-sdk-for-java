/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.implementation;

import com.microsoft.azure.management.compute.AvailabilitySets;
import com.microsoft.azure.management.compute.VirtualMachines;
import com.microsoft.azure.management.compute.implementation.AvailabilitySetsImpl;
import com.microsoft.azure.management.compute.implementation.api.ComputeManagementClientImpl;
import com.microsoft.azure.management.resources.GenericResources;
import com.microsoft.azure.management.resources.ResourceGroups;
import com.microsoft.azure.management.resources.implementation.api.ResourceManagementClientImpl;
import com.microsoft.azure.management.resources.implementation.GenericResourcesImpl;
import com.microsoft.azure.management.resources.implementation.ResourceGroupsImpl;
import com.microsoft.azure.management.storage.StorageAccounts;
import com.microsoft.azure.management.storage.Usages;
import com.microsoft.azure.management.storage.implementation.StorageAccountsImpl;
import com.microsoft.azure.management.storage.implementation.UsagesImpl;
import com.microsoft.azure.management.storage.implementation.api.StorageManagementClientImpl;
import com.microsoft.rest.RestClient;

final class AzureSubscriptionImpl
        implements Azure.Subscription {
    private final String subscriptionId;
    private final RestClient restClient;
    // SDK Clients
    private ResourceManagementClientImpl resourceManagementClient;
    private StorageManagementClientImpl storageManagementClient;
    private ComputeManagementClientImpl computeManagementClient;
    // Fluent Collections
    private Azure.ResourceGroups azureResourceGroups;
    private GenericResources genericResources;
    private StorageAccounts storageAccounts;
    private Usages storageUsages;
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
        if (genericResources == null) {
            genericResources = new GenericResourcesImpl(resourceManagementClient());
        }
        return genericResources;
    }

    @Override
    public StorageAccounts storageAccounts() {
        if (storageAccounts == null) {
            storageAccounts =  new StorageAccountsImpl(storageManagementClient().storageAccounts(), resourceGroupsCore());
        }
        return storageAccounts;
    }

    @Override
    public Usages storageUsages() {
        if (storageUsages == null) {
            storageUsages = new UsagesImpl(storageManagementClient());
        }
        return storageUsages;
    }

    @Override
    public AvailabilitySets availabilitySets() {
        if (availabilitySets == null) {
            availabilitySets = new AvailabilitySetsImpl(computeManagementClient().availabilitySets(), resourceGroupsCore(), virtualMachines());
        }
        return availabilitySets;
    }

    public VirtualMachines virtualMachines() {
        // TODO
        return null;
    }

    private ResourceManagementClientImpl resourceManagementClient() {
        if (resourceManagementClient == null) {
            resourceManagementClient = new ResourceManagementClientImpl(restClient);
            resourceManagementClient.setSubscriptionId(subscriptionId);
        }
        return resourceManagementClient;
    }

    private StorageManagementClientImpl storageManagementClient() {
        if (storageManagementClient == null) {
            storageManagementClient = new StorageManagementClientImpl(restClient);
            storageManagementClient.setSubscriptionId(subscriptionId);
        }
        return storageManagementClient;
    }

    private ComputeManagementClientImpl computeManagementClient() {
        if (computeManagementClient == null) {
            computeManagementClient = new ComputeManagementClientImpl(restClient);
            computeManagementClient.setSubscriptionId(subscriptionId);
        }
        return computeManagementClient;
    }

    private ResourceGroups resourceGroupsCore() {
        return new ResourceGroupsImpl(resourceManagementClient());
    }
}
