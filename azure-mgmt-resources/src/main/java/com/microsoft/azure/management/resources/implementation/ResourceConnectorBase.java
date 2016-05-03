package com.microsoft.azure.management.resources.implementation;

import com.microsoft.azure.management.resources.ResourceConnector;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.ResourceGroups;
import com.microsoft.azure.management.resources.implementation.api.ResourceManagementClientImpl;
import com.microsoft.rest.credentials.ServiceClientCredentials;

public class ResourceConnectorBase<T extends ResourceConnector> implements ResourceConnector<T> {
    protected final ServiceClientCredentials credentials;
    protected final String subscriptionId;
    protected final ResourceGroup resourceGroup;
    private ResourceGroups resourceGroups;
    private ResourceManagementClientImpl client;

    protected ResourceConnectorBase(ServiceClientCredentials credentials, String subscriptionId, ResourceGroup resourceGroup) {
        this.credentials = credentials;
        this.subscriptionId = subscriptionId;
        this.resourceGroup = resourceGroup;
    }

    protected ServiceClientCredentials credentials() {
        return this.credentials;
    }

    protected String subscriptionId() {
        return this.subscriptionId;
    }

    protected ResourceGroups resourceGroups() {
        if (resourceGroups == null) {
            resourceGroups = new ResourceGroupsImpl(resourceManagementClient());
        }
        return resourceGroups;
    }

    protected ResourceManagementClientImpl resourceManagementClient() {
        if (client == null) {
            client = new ResourceManagementClientImpl(credentials);
            client.setSubscriptionId(subscriptionId);
        }
        return client;
    }
}
