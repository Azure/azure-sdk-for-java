package com.microsoft.azure.management.storage.implementation;

import com.microsoft.azure.management.resources.fluentcore.arm.implementation.AzureConfigureBaseImpl;
import com.microsoft.rest.credentials.ServiceClientCredentials;

final class AzureConfigureImpl extends AzureConfigureBaseImpl<StorageManager.Configure>
    implements StorageManager.Configure {
    @Override
    public StorageManager authenticate(ServiceClientCredentials credentials) {
        this.restClient = this.restClientBuilder.withCredentials(credentials).build();
        return StorageManager.authenticate(this.restClient);
    }
}
