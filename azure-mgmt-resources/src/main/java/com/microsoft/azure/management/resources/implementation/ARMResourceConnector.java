package com.microsoft.azure.management.resources.implementation;

import com.microsoft.azure.management.resources.Deployments;
import com.microsoft.azure.management.resources.GenericResources;
import com.microsoft.azure.management.resources.ResourceConnector;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.rest.credentials.ServiceClientCredentials;

public class ARMResourceConnector extends ResourceConnectorBase<ARMResourceConnector> {
    private ResourceManager resourceClient;
    private GenericResources genericResources;
    private Deployments.InGroup deployments;

    private ARMResourceConnector(ServiceClientCredentials credentials, String subscriptionId, ResourceGroup resourceGroup) {
        super(credentials, subscriptionId, resourceGroup);
        this.genericResources = new GenericResourcesImpl(resourceManagementClient(), resourceGroup.name());
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

    public Deployments.InGroup deployments() {
        if (deployments == null) {
            deployments = new DeploymentsInGroupImpl(resourceClient().deployments(), resourceGroup);
        }
        return deployments;
    }

    private ResourceManager resourceClient() {
        if (resourceClient == null) {
            resourceClient = ResourceManager
                    .authenticate(credentials)
                    .useSubscription(subscriptionId);
        }
        return resourceClient;
    }
}
