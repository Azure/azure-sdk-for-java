package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.management.compute.AvailabilitySets;
import com.microsoft.azure.management.compute.VirtualMachines;
import com.microsoft.azure.management.compute.implementation.api.ComputeManagementClientImpl;
import com.microsoft.azure.management.resources.implementation.ResourceManager;
import com.microsoft.rest.RestClient;

class AzureSubscriptionImpl implements ComputeManager.Subscription {
    private final RestClient restClient;
    private final String subscriptionId;
    // The service managers
    private ResourceManager.Subscription resourceClient;
    // The sdk clients
    private ComputeManagementClientImpl computeManagementClient;
    // The collections
    private AvailabilitySets availabilitySets;
    private VirtualMachines virtualMachines;

    public AzureSubscriptionImpl(RestClient restClient, String subscriptionId) {
        this.restClient = restClient;
        this.subscriptionId = subscriptionId;
    }

    public AvailabilitySets availabilitySets() {
        if (availabilitySets == null) {
            availabilitySets = new AvailabilitySetsImpl(computeManagementClient().availabilitySets(),
                    resourceClient().resourceGroups(), virtualMachines());
        }
        return  availabilitySets;
    }

    public VirtualMachines virtualMachines() {
        if (virtualMachines == null) {

        }
        return virtualMachines;
    }

    private ComputeManagementClientImpl computeManagementClient() {
        if (computeManagementClient == null) {
            computeManagementClient = new ComputeManagementClientImpl(restClient);
            computeManagementClient.setSubscriptionId(subscriptionId);
        }
        return computeManagementClient;
    }

    private ResourceManager.Subscription resourceClient() {
        if (restClient == null) {
            resourceClient = ResourceManager
                    .authenticate(restClient)
                    .withSubscription(subscriptionId);
        }
        return resourceClient;
    }
}
