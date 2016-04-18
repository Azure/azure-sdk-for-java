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

    private ResourceManagementClientImpl resourceManagementClient;
    private StorageManagementClientImpl storageManagementClient;

    AzureSubscriptionImpl(ServiceClientCredentials credentials, String subscriptionId) {
        this.credentials = credentials;
        this.subscriptionId = subscriptionId;
    }

    @Override
    public ResourceGroups resourceGroups() {
        return new ResourceGroupsImpl(resourceManagementClient());
    }

    @Override
    public GenericResources genericResources() {
        return new GenericResourcesImpl(resourceManagementClient());
    }

    @Override
    public StorageAccounts storageAccounts() {
        return new StorageAccountsImpl(storageManagementClient(), resourceGroups());
    }

    @Override
    public Usages usages() {
        return new UsagesImpl(storageManagementClient());
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
}
