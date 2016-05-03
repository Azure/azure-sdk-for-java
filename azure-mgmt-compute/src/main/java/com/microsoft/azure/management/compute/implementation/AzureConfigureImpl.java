package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.management.resources.fluentcore.arm.implementation.AzureConfigureBaseImpl;
import com.microsoft.rest.credentials.ServiceClientCredentials;

final class AzureConfigureImpl extends AzureConfigureBaseImpl<ComputeManager.Configure>
        implements ComputeManager.Configure {
    @Override
    public ComputeManager authenticate(ServiceClientCredentials credentials) {
        this.restClient = this.restClientBuilder.withCredentials(credentials).build();
        return ComputeManager.authenticate(this.restClient);
    }
}
