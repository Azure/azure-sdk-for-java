package com.microsoft.azure.management.resources.implementation;

import com.microsoft.azure.management.resources.ResourceConnector;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.rest.RestClient;

public class ResourceConnectorBase implements ResourceConnector {
    protected final RestClient restClient;
    protected final String subscriptionId;
    protected final ResourceGroup resourceGroup;

    protected ResourceConnectorBase(RestClient restClient, String subscriptionId, ResourceGroup resourceGroup) {
        this.restClient = restClient;
        this.subscriptionId = subscriptionId;
        this.resourceGroup = resourceGroup;
    }
}
