package com.microsoft.azure.management.resources.implementation;

import com.microsoft.azure.management.resources.Deployments;
import com.microsoft.azure.management.resources.GenericResources;
import com.microsoft.azure.management.resources.ResourceConnector;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.rest.RestClient;

public class ARMResourceConnector extends ResourceConnectorBase {
    private ResourceManager resourceClient;
    private GenericResources.InGroup genericResources;
    private Deployments.InGroup deployments;

    private ARMResourceConnector(RestClient restClient, String subscriptionId, ResourceGroup resourceGroup) {
        super(restClient, subscriptionId, resourceGroup);
    }

    private static ARMResourceConnector create(RestClient restClient, String subscriptionId, ResourceGroup resourceGroup) {
        return new ARMResourceConnector(restClient, subscriptionId, resourceGroup);
    }

    public static class Builder implements ResourceConnector.Builder<ARMResourceConnector> {
        public ARMResourceConnector create(RestClient restClient, String subscriptionId, ResourceGroup resourceGroup) {
            return ARMResourceConnector.create(restClient, subscriptionId, resourceGroup);
        }
    }

    public GenericResources.InGroup genericResources() {
        if (genericResources == null) {
            genericResources = new GenericResourcesInGroupImpl(resourceClient().genericResources(), resourceGroup);
        }
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
                    .authenticate(restClient)
                    .withSubscription(subscriptionId);
        }
        return resourceClient;
    }
}
