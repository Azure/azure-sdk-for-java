package com.microsoft.azure.implementation;

import com.microsoft.azure.management.resources.ResourceGroups;
import com.microsoft.azure.management.resources.implementation.ResourceGroupsImpl;
import com.microsoft.azure.management.resources.implementation.api.ResourceManagementClientImpl;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.implementation.ResourceGroupImpl;
import com.microsoft.azure.management.storage.StorageAccounts;
import com.microsoft.azure.management.storage.StorageAccountsInGroup;
import com.microsoft.azure.management.storage.implementation.StorageAccountsImpl;
import com.microsoft.azure.management.storage.implementation.StorageAccountsInGroupImpl;
import com.microsoft.azure.management.storage.implementation.api.StorageManagementClientImpl;
import com.microsoft.rest.credentials.ServiceClientCredentials;

public class AzureResourceGroupImpl extends ResourceGroupImpl implements Azure.ResourceGroup {
    private ResourceManagementClientImpl client;
    private ResourceGroup resourceGroupCore;
    private ResourceGroups resourceGroupsCore;
    private StorageAccounts storageAccountsCore;

    public AzureResourceGroupImpl(ResourceGroup resourceGroupCore, ResourceManagementClientImpl client) {
        super(resourceGroupCore.inner(), client);
        this.resourceGroupCore = resourceGroupCore;
        this.client = client;
    }

    // StorageAccount collection having a resource resourceGroupCore context.
    //
    public StorageAccountsInGroup storageAccounts() {
        return new StorageAccountsInGroupImpl(storageAccountsCore(), resourceGroupCore);
    }

    public ResourceGroup resourceGroupCore() {
        return resourceGroupCore;
    }

    private ResourceGroups resourceGroupsCore() {
        if (resourceGroupsCore == null) {
            resourceGroupsCore = new ResourceGroupsImpl(this.client);
        }
        return resourceGroupsCore;
    }

    private StorageAccounts storageAccountsCore() {
        if (storageAccountsCore == null) {
            storageAccountsCore = new StorageAccountsImpl(StorageManagementClient(), resourceGroupsCore());
        }
        return storageAccountsCore;
    }

    private ServiceClientCredentials credentials() {
        return this.client.getCredentials();
    }

    private String subscriptionId() {
        return this.client.getSubscriptionId();
    }

    private StorageManagementClientImpl StorageManagementClient() {
        StorageManagementClientImpl client = new StorageManagementClientImpl(credentials());
        client.setSubscriptionId(subscriptionId());
        return client;
    }
}
