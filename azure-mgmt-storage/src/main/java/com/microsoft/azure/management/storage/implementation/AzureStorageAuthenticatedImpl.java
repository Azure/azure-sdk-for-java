package com.microsoft.azure.management.storage.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.storage.AzureStorageAuthenticated;
import com.microsoft.azure.management.storage.StorageAccounts;
import com.microsoft.azure.management.storage.implementation.api.StorageManagementClientImpl;
import com.microsoft.rest.credentials.ServiceClientCredentials;

import java.io.IOException;

public class AzureStorageAuthenticatedImpl implements AzureStorageAuthenticated {
    private ServiceClientCredentials credentials;
    private StorageManagementClientImpl storageManagementClient;

    AzureStorageAuthenticatedImpl(ServiceClientCredentials credentials, String subscriptionId) {
        this.credentials = credentials;
        this.storageManagementClient = new StorageManagementClientImpl(credentials);
        this.storageManagementClient.setSubscriptionId(subscriptionId);
    }

    @Override
    public StorageAccounts storageAccounts() throws IOException, CloudException {
        return new StorageAccountsImpl(storageManagementClient);
    }
}
