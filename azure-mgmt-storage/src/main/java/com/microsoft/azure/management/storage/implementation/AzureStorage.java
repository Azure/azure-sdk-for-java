package com.microsoft.azure.management.storage.implementation;

import com.microsoft.azure.management.storage.AzureStorageAuthenticated;
import com.microsoft.rest.credentials.ServiceClientCredentials;

public final class AzureStorage {
    public static AzureStorageAuthenticated authenticate(ServiceClientCredentials credentials, String subscriptionId) {
        return new AzureStorageAuthenticatedImpl(credentials, subscriptionId);
    }
}
