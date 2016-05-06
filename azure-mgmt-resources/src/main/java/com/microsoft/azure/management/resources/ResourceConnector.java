package com.microsoft.azure.management.resources;

import com.microsoft.rest.RestClient;

public interface ResourceConnector {
    interface Builder<T extends ResourceConnector> {
        T create(RestClient restClient, String subscriptionId, ResourceGroup resourceGroup);
    }
}
