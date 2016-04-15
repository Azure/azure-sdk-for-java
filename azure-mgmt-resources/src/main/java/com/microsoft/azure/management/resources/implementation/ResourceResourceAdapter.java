package com.microsoft.azure.management.resources.implementation;

import com.microsoft.azure.management.resources.Deployments;
import com.microsoft.azure.management.resources.GenericResources;
import com.microsoft.azure.management.resources.ResourceAdapter;
import com.microsoft.azure.management.resources.implementation.api.ResourceManagementClientImpl;

public class ResourceResourceAdapter implements ResourceAdapter<ResourceResourceAdapter> {
    private ResourceManagementClientImpl client;
    private GenericResources genericResources;
    private Deployments deployments;

    private ResourceResourceAdapter(ResourceManagementClientImpl resourceManagementClient, String resourceGroupName) {
        this.client = resourceManagementClient;
        this.genericResources = new GenericResourcesImpl(this.client, resourceGroupName);
        this.deployments = new DeploymentsImpl(this.client, resourceGroupName);
    }

    private static ResourceResourceAdapter create(ResourceManagementClientImpl resourceManagementClient, String resourceGroupName) {
        return new ResourceResourceAdapter(resourceManagementClient, resourceGroupName);
    }

    public static class Builder implements ResourceAdapter.Builder<ResourceResourceAdapter> {
        public ResourceResourceAdapter create(ResourceManagementClientImpl resourceManagementClient, String resourceGroupName) {
            return ResourceResourceAdapter.create(resourceManagementClient, resourceGroupName);
        }
    }

    public GenericResources genericResources() {
        return genericResources;
    }

    public Deployments deployments() {
        return deployments;
    }
}
