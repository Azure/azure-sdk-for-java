package com.microsoft.azure.management.resources;

import com.microsoft.rest.RestClient;

public interface ResourceConnector<T extends ResourceConnector> {
    interface Builder<T> {
        T create(RestClient restClient, String subscriptionId, ResourceGroup resourceGroup);
    }
}
