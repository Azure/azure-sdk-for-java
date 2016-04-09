/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.Subscription;
import com.microsoft.azure.management.resources.AzureResourceAuthenticated;
import com.microsoft.azure.management.resources.ResourceGroups;
import com.microsoft.azure.management.resources.implementation.AzureResource;
import com.microsoft.azure.management.storage.AzureStorageAuthenticated;
import com.microsoft.azure.management.storage.StorageAccounts;
import com.microsoft.azure.management.storage.implementation.AzureStorage;
import com.microsoft.rest.credentials.ServiceClientCredentials;

import java.io.IOException;

public class SubscriptionImpl implements Subscription {
    private ServiceClientCredentials credentials;
    private String subscriptionId;
    private AzureResourceAuthenticated resourceClient;
    private AzureStorageAuthenticated storageClient;

    public SubscriptionImpl(ServiceClientCredentials credentials, String subscriptionId) {
        this.credentials = credentials;
        this.subscriptionId = subscriptionId;
    }

    private AzureResourceAuthenticated resourceClient() {
        if (resourceClient == null) {
            resourceClient = AzureResource.authenticate(this.credentials); // TODO: Subscriptionid
        }
        return resourceClient;
    }

    private AzureStorageAuthenticated storageClient() {
        if (storageClient == null) {
            storageClient = AzureStorage.authenticate(this.credentials, this.subscriptionId);
        }
        return storageClient;
    }

    @Override
    public ResourceGroups resourceGroups() throws IOException, CloudException {
        return resourceClient().resourceGroups();
    }

    @Override
    public StorageAccounts storageAccounts()  throws IOException, CloudException {
        return storageClient().storageAccounts();
    }
}
