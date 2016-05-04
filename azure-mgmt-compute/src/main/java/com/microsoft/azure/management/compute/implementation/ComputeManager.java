package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.management.compute.AvailabilitySets;
import com.microsoft.azure.management.compute.VirtualMachines;
import com.microsoft.azure.management.compute.implementation.api.ComputeManagementClientImpl;
import com.microsoft.azure.management.resources.fluentcore.arm.implementation.AzureConfigureBaseAuthImpl;
import com.microsoft.azure.management.resources.implementation.ResourceManager;
import com.microsoft.rest.RestClient;
import com.microsoft.rest.credentials.ServiceClientCredentials;

public final class ComputeManager {
    private final RestClient restClient;
    private final String subscriptionId;
    // The service managers
    private ResourceManager resourceClient;
    // The sdk clients
    private ComputeManagementClientImpl computeManagementClient;
    // The collections
    private AvailabilitySets availabilitySets;
    private VirtualMachines virtualMachines;

    public static AzureConfigureBaseAuthImpl<ComputeManager> configure() {
        return new AzureConfigureBaseAuthImpl<ComputeManager>() {
            public ComputeManager authenticate(ServiceClientCredentials credentials, String subscriptionId) {
                buildRestClient(credentials);
                return ComputeManager.authenticate(this.restClient, subscriptionId);
            }
        };
    }

    public static ComputeManager authenticate(ServiceClientCredentials credentials, String subscriptionId) {
        return new ComputeManager(credentials, subscriptionId);
    }

    public static ComputeManager authenticate(RestClient restClient, String subscriptionId) {
        return new ComputeManager(restClient, subscriptionId);
    }

    private ComputeManager(ServiceClientCredentials credentials, String subscriptionId) {
        this.restClient = new RestClient
                .Builder("https://management.azure.com")
                .withCredentials(credentials)
                .build();
        this.subscriptionId = subscriptionId;
    }

    private ComputeManager(RestClient restClient, String subscriptionId) {
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
        virtualMachines = null;
        return virtualMachines;
    }

    private ComputeManagementClientImpl computeManagementClient() {
        if (computeManagementClient == null) {
            computeManagementClient = new ComputeManagementClientImpl(restClient);
            computeManagementClient.setSubscriptionId(subscriptionId);
        }
        return computeManagementClient;
    }

    private ResourceManager resourceClient() {
        if (restClient == null) {
            resourceClient = ResourceManager
                    .authenticate(restClient)
                    .useSubscription(subscriptionId);
        }
        return resourceClient;
    }
}
