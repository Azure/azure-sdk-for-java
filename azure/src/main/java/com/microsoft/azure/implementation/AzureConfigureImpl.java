package com.microsoft.azure.implementation;

import com.microsoft.azure.management.resources.fluentcore.arm.implementation.AzureConfigurableImpl;
import com.microsoft.rest.credentials.ServiceClientCredentials;

final class AzureConfigureImpl extends AzureConfigurableImpl<Azure.Configure>
    implements Azure.Configure {
    AzureConfigureImpl() {}

    @Override
    public Azure authenticate(ServiceClientCredentials credentials) {
        this.restClient = this.restClientBuilder.withCredentials(credentials).build();
        return Azure.authenticate(this.restClient);
    }
}
