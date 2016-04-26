package com.microsoft.azure.management.resources.implementation;

import com.microsoft.azure.management.resources.Deployments.InGroup;
import com.microsoft.azure.management.resources.GenericResources;
import com.microsoft.azure.management.resources.ResourceConnector;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.rest.credentials.ServiceClientCredentials;

public class ARMResourceConnector extends ResourceConnectorBase<ARMResourceConnector> {
    private GenericResources genericResources;
    private InGroup deployments;

    private ARMResourceConnector(ServiceClientCredentials credentials, String subscriptionId, ResourceGroup resourceGroup) {
        super(credentials, subscriptionId, resourceGroup);
        this.genericResources = new GenericResourcesImpl(resourceManagementClient(), resourceGroup.name());
        this.deployments = new DeploymentsInGroupImpl(resourceManagementClient(), resourceGroup.name());
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
