package com.microsoft.azure.management.resources.implementation;

import com.microsoft.azure.management.resources.Deployments;
import com.microsoft.azure.management.resources.GenericResources;
import com.microsoft.azure.management.resources.ResourceConnector;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.rest.RestClient;

/**
 * An instance of this class connects ARM resource operations to a resource group.
 */
public final class ARMResourceConnector extends ResourceConnectorBase {
    private ResourceManager resourceClient;
    private GenericResources.InGroup genericResources;
    private Deployments.InGroup deployments;

    private ARMResourceConnector(RestClient restClient, String subscriptionId, ResourceGroup resourceGroup) {
        super(restClient, subscriptionId, resourceGroup);
    }

    private static ARMResourceConnector create(RestClient restClient, String subscriptionId, ResourceGroup resourceGroup) {
        return new ARMResourceConnector(restClient, subscriptionId, resourceGroup);
    }

    /**
     * An instance of this builder class can create an ARMResourceConnector.
     */
    public static class Builder implements ResourceConnector.Builder<ARMResourceConnector> {
        /**
         * Create an ARMResourceConnector.
         *
         * @param restClient an instance of {@link RestClient}.
         * @param subscriptionId the subscription ID the client is authenticated to.
         * @param resourceGroup the resource group to connect to.
         * @return a constructed connector to ARM resources.
         */
        public ARMResourceConnector create(RestClient restClient, String subscriptionId, ResourceGroup resourceGroup) {
            return ARMResourceConnector.create(restClient, subscriptionId, resourceGroup);
        }
    }

    /**
     * Get the operation class for generic resources in Azure.
     *
     * @return the the operation class instance for generic resources.
     */
    public GenericResources.InGroup genericResources() {
        if (genericResources == null) {
            genericResources = new GenericResourcesInGroupImpl(resourceClient().genericResources(), resourceGroup);
        }
        return genericResources;
    }

    /**
     * Get the operation class for deployments in Azure.
     *
     * @return the the operation class instance for deployments.
     */
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
