package com.microsoft.azure.management.resources.implementation;

import com.microsoft.azure.management.resources.Deployments.InGroup;
import com.microsoft.azure.management.resources.GenericResources;
import com.microsoft.azure.management.resources.ResourceConnector;
import com.microsoft.azure.management.resources.implementation.api.ResourceManagementClientImpl;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.rest.credentials.ServiceClientCredentials;

public class ARMResourceConnector implements ResourceConnector<ARMResourceConnector> {
    private ResourceManagementClientImpl client;
    private GenericResources genericResources;
    private InGroup deployments;

    private ARMResourceConnector(ServiceClientCredentials credentials, String subscriptionId, ResourceGroup resourceGroup) {
        this.client = new ResourceManagementClientImpl(credentials);
        this.client.setSubscriptionId(subscriptionId);
        this.genericResources = new GenericResourcesImpl(this.client, resourceGroup.name());
        this.deployments = new DeploymentsInGroupImpl(this.client, resourceGroup.name());
    }

    private static ARMResourceConnector create(ServiceClientCredentials credentials, String subscriptionId, ResourceGroup resourceGroup) {
        return new ARMResourceConnector(credentials, subscriptionId, resourceGroup);
    }

    public static class Builder implements ResourceConnector.Builder<ARMResourceConnector> {
        public ARMResourceConnector create(ServiceClientCredentials credentials, String subscriptionId, ResourceGroup resourceGroup) {
            return ARMResourceConnector.create(credentials, subscriptionId, resourceGroup);
        }
    }

    public GenericResources genericResources() {
        return genericResources;
    }

    public InGroup deployments() {
        return deployments;
    }
}
