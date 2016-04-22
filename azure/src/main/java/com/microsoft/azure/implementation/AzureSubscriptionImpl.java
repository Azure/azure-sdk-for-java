/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.implementation;

import com.microsoft.azure.management.resources.GenericResources;
import com.microsoft.azure.management.resources.ResourceGroups;
import com.microsoft.azure.management.resources.implementation.api.ResourceManagementClientImpl;
import com.microsoft.azure.management.resources.fluentcore.arm.implementation.AzureBaseImpl;
import com.microsoft.azure.management.resources.implementation.GenericResourcesImpl;
import com.microsoft.azure.management.resources.implementation.ResourceGroupsImpl;
import com.microsoft.azure.management.storage.StorageAccounts;
import com.microsoft.azure.management.storage.Usages;
import com.microsoft.azure.management.storage.implementation.StorageAccountsImpl;
import com.microsoft.azure.management.storage.implementation.UsagesImpl;
import com.microsoft.azure.management.storage.implementation.api.StorageManagementClientImpl;
import com.microsoft.rest.credentials.ServiceClientCredentials;

final class AzureSubscriptionImpl extends AzureBaseImpl<Azure.Subscription>
        implements Azure.Subscription {
    private ServiceClientCredentials credentials;
    private String subscriptionId;
    // SDK Clients
    private ResourceManagementClientImpl resourceManagementClient;
    private StorageManagementClientImpl storageManagementClient;
    // Fluent Collections
    private Azure.ResourceGroups azureResourceGroups;
    private GenericResources genericResources;
    private StorageAccounts storageAccounts;
    private Usages storageUsages;

    AzureSubscriptionImpl(ServiceClientCredentials credentials, String subscriptionId) {
        this.credentials = credentials;
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
            storageAccounts =  new StorageAccountsImpl(storageManagementClient(), resourceGroupsCore());
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

    private ResourceManagementClientImpl resourceManagementClient() {
        if (resourceManagementClient == null) {
            resourceManagementClient = new ResourceManagementClientImpl(credentials);
            resourceManagementClient.setSubscriptionId(subscriptionId);
        }
        return resourceManagementClient;
    }

    private StorageManagementClientImpl storageManagementClient() {
        if (storageManagementClient == null) {
            storageManagementClient = new StorageManagementClientImpl(credentials);
            storageManagementClient.setSubscriptionId(subscriptionId);
        }
        return storageManagementClient;
    }

    private ResourceGroups resourceGroupsCore() {
        return new ResourceGroupsImpl(resourceManagementClient());
    }
}
