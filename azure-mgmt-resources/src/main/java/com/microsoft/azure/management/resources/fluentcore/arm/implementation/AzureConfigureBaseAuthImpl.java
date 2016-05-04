package com.microsoft.azure.management.resources.fluentcore.arm.implementation;

import com.microsoft.rest.credentials.ServiceClientCredentials;

public abstract class AzureConfigureBaseAuthImpl<T> extends AzureConfigureBaseImpl<AzureConfigureBaseAuthImpl<T>> {
    protected void buildRestClient(ServiceClientCredentials credentials) {
        this.restClient = this.restClientBuilder.withCredentials(credentials).build();
    }

    public abstract T authenticate(ServiceClientCredentials credentials, String subscriptionId);
}