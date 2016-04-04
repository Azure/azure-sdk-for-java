/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.implementation;

import com.microsoft.azure.Subscription;
import com.microsoft.azure.management.compute.ComputeManagementClient;
import com.microsoft.azure.management.compute.implementation.ComputeManagementClientImpl;
import com.microsoft.azure.management.compute.VirtualMachines;
import com.microsoft.azure.management.network.NetworkManagementClient;
import com.microsoft.azure.management.network.NetworkManagementClientImpl;
import com.microsoft.azure.management.network.VirtualNetworks;
import com.microsoft.azure.management.resources.collection.ResourceGroups;
import com.microsoft.azure.management.resources.client.ResourceManagementClient;
import com.microsoft.azure.management.resources.client.implementation.ResourceManagementClientImpl;
import com.microsoft.azure.management.storage.StorageAccounts;
import com.microsoft.azure.management.storage.StorageManagementClient;
import com.microsoft.azure.management.storage.StorageManagementClientImpl;
import com.microsoft.rest.credentials.ServiceClientCredentials;

public class SubscriptionImpl implements Subscription {
    private ResourceManagementClient resourceClient;
    private StorageManagementClient storageClient;
    private NetworkManagementClient networkClient;
    private ComputeManagementClient computeClient;

    public SubscriptionImpl(ServiceClientCredentials credentials, String subscriptionId) {
        this.resourceClient = new ResourceManagementClientImpl(credentials);
        this.storageClient = new StorageManagementClientImpl(credentials);
        this.networkClient = new NetworkManagementClientImpl(credentials);
        this.computeClient = new ComputeManagementClientImpl(credentials);
        this.resourceClient.setSubscriptionId(subscriptionId);
        this.storageClient.setSubscriptionId(subscriptionId);
        this.networkClient.setSubscriptionId(subscriptionId);
        this.computeClient.setSubscriptionId(subscriptionId);
    }

    @Override
    public VirtualMachines virtualMachines() {
        return computeClient.virtualMachines();
    }

    @Override
    public ResourceGroups resourceGroups() {
        return resourceClient.resourceGroups();
    }

    @Override
    public StorageAccounts storageAccounts() {
        return storageClient.storageAccounts();
    }

    @Override
    public VirtualNetworks virtualNetworks() {
        return networkClient.virtualNetworks();
    }
}
