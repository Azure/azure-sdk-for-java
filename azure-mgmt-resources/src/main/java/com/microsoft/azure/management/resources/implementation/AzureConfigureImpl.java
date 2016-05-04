package com.microsoft.azure.management.resources.implementation;

import com.microsoft.azure.management.resources.fluentcore.arm.implementation.AzureConfigureBaseImpl;
import com.microsoft.rest.credentials.ServiceClientCredentials;

final class AzureConfigureImpl extends AzureConfigureBaseImpl<ResourceManager.Configure>
        implements ResourceManager.Configure {
    @Override
    public ResourceManager.Authenticated authenticate(ServiceClientCredentials credentials) {
        this.restClient = this.restClientBuilder.withCredentials(credentials).build();
        return ResourceManager.authenticate(this.restClient);
    }
}
