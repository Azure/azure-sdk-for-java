package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.management.compute.AvailabilitySets;
import com.microsoft.azure.management.compute.VirtualMachines;
import com.microsoft.azure.management.resources.ResourceConnector;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.implementation.ResourceConnectorBase;
import com.microsoft.rest.RestClient;

public class ComputeResourceConnector extends ResourceConnectorBase<ComputeResourceConnector> {
    private ComputeManager computeClient;
    private AvailabilitySets.InGroup availabilitySets;
    private VirtualMachines.InGroup virtualMachines;

    private ComputeResourceConnector(RestClient restClient, String subscriptionId, ResourceGroup resourceGroup) {
        super(restClient, subscriptionId, resourceGroup);
    }

    private static ComputeResourceConnector create(RestClient restClient, String subscriptionId, ResourceGroup resourceGroup) {
        return new ComputeResourceConnector(restClient, subscriptionId, resourceGroup);
    }

    public static class Builder implements ResourceConnector.Builder<ComputeResourceConnector> {
        public ComputeResourceConnector create(RestClient restClient, String subscriptionId, ResourceGroup resourceGroup) {
            return ComputeResourceConnector.create(restClient, subscriptionId, resourceGroup);
        }
    }

    public AvailabilitySets.InGroup availabilitySets() {
        if (availabilitySets == null) {
            availabilitySets = new AvailabilitySetsInGroup(computeClient().availabilitySets(), resourceGroup);
        }
        return availabilitySets;
    }

    public VirtualMachines.InGroup VirtualMachines() {
        // TODO
        return null;
    }

    private ComputeManager computeClient() {
        if (computeClient == null) {
            computeClient = ComputeManager
                    .authenticate(restClient, subscriptionId);
        }
        return computeClient;
    }
}
