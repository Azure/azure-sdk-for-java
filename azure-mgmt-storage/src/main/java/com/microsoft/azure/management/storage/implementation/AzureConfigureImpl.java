package com.microsoft.azure.management.storage.implementation;

import com.microsoft.azure.management.resources.fluentcore.arm.implementation.AzureConfigureBaseImpl;
import com.microsoft.rest.credentials.ServiceClientCredentials;

final class AzureConfigureImpl extends AzureConfigureBaseImpl<AzureStorageManager.Configure>
    implements AzureStorageManager.Configure {
    @Override
    public AzureStorageManager.Authenticated authenticate(ServiceClientCredentials credentials, String subscriptionId) {
        this.restClient = this.restClientBuilder.withCredentials(credentials).build();
        return new AzureAuthenticatedImpl(this.restClient, subscriptionId);
    }
}
