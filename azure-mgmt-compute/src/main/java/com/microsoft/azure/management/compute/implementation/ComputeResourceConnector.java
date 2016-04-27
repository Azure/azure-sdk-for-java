package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.management.compute.AvailabilitySets;
import com.microsoft.azure.management.compute.VirtualMachines;
import com.microsoft.azure.management.compute.implementation.api.ComputeManagementClientImpl;
import com.microsoft.azure.management.resources.ResourceConnector;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.implementation.ResourceConnectorBase;
import com.microsoft.rest.credentials.ServiceClientCredentials;

public class ComputeResourceConnector extends ResourceConnectorBase<ComputeResourceConnector> {
    private ComputeManagementClientImpl client;
    private AvailabilitySets.InGroup availabilitySets;

    private ComputeResourceConnector(ServiceClientCredentials credentials, String subscriptionId, ResourceGroup resourceGroup) {
        super(credentials, subscriptionId, resourceGroup);
        constructClient();
    }

    private static ComputeResourceConnector create(ServiceClientCredentials credentials, String subscriptionId, ResourceGroup resourceGroup) {
        return new ComputeResourceConnector(credentials, subscriptionId, resourceGroup);
    }

    private void constructClient() {
        client = new ComputeManagementClientImpl(credentials);
        client.setSubscriptionId(subscriptionId);
    }

    public static class Builder implements ResourceConnector.Builder<ComputeResourceConnector> {
        public ComputeResourceConnector create(ServiceClientCredentials credentials, String subscriptionId, ResourceGroup resourceGroup) {
            return ComputeResourceConnector.create(credentials, subscriptionId, resourceGroup);
        }
    }

    public AvailabilitySets.InGroup availabilitySets() {
        if (availabilitySets == null) {
            availabilitySets = new AvailabilitySetsInGroup(availabilitySetsCore(), resourceGroup);
        }
        return availabilitySets;
    }

    public VirtualMachines VirtualMachines() {
        // TODO
        return null;
    }

    private AvailabilitySets availabilitySetsCore() {
        AvailabilitySets availabilitySetsCore = new AvailabilitySetsImpl(client.availabilitySets(), resourceGroups(), VirtualMachines());
        return  availabilitySetsCore;
    }
}
