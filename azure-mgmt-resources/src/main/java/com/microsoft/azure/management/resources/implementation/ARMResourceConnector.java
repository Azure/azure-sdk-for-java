package com.microsoft.azure.management.resources.implementation;

import com.microsoft.azure.management.resources.Deployments.InGroup;
import com.microsoft.azure.management.resources.GenericResources;
import com.microsoft.azure.management.resources.ResourceConnector;
import com.microsoft.azure.management.resources.implementation.api.ResourceManagementClientImpl;

public class ARMResourceConnector implements ResourceConnector<ARMResourceConnector> {
    private ResourceManagementClientImpl client;
    private GenericResources genericResources;
    private InGroup deployments;

    private ARMResourceConnector(ResourceManagementClientImpl resourceManagementClient, String resourceGroupName) {
        this.client = resourceManagementClient;
        this.genericResources = new GenericResourcesImpl(this.client, resourceGroupName);
        this.deployments = new DeploymentsInGroupImpl(resourceManagementClient, resourceGroupName);
    }

    private static ARMResourceConnector create(ResourceManagementClientImpl resourceManagementClient, String resourceGroupName) {
        return new ARMResourceConnector(resourceManagementClient, resourceGroupName);
    }

    public static class Builder implements ResourceConnector.Builder<ARMResourceConnector> {
        public ARMResourceConnector create(ResourceManagementClientImpl resourceManagementClient, String resourceGroupName) {
            return ARMResourceConnector.create(resourceManagementClient, resourceGroupName);
        }
    }

    public GenericResources genericResources() {
        return genericResources;
    }

    public InGroup deployments() {
        return deployments;
    }
}
