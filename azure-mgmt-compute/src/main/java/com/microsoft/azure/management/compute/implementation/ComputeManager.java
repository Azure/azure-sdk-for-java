package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.management.compute.AvailabilitySets;
import com.microsoft.azure.management.compute.VirtualMachines;
import com.microsoft.azure.management.compute.implementation.api.ComputeManagementClientImpl;
import com.microsoft.azure.management.resources.fluentcore.arm.AzureConfigurable;
import com.microsoft.azure.management.resources.fluentcore.arm.implementation.AzureConfigurableImpl;
import com.microsoft.azure.management.resources.implementation.ResourceManager;
import com.microsoft.rest.RestClient;
import com.microsoft.rest.credentials.ServiceClientCredentials;

public final class ComputeManager {
    // The service managers
    private ResourceManager resourceManager;
    // The sdk clients
    private ComputeManagementClientImpl computeManagementClient;
    // The collections
    private AvailabilitySets availabilitySets;
    private VirtualMachines virtualMachines;

    public static Configurable configure() {
        return new ComputeManager.ConfigurableImpl();
    }

    public static ComputeManager authenticate(ServiceClientCredentials credentials, String subscriptionId) {
        return new ComputeManager(AzureEnvironment.AZURE.newRestClientBuilder()
                .withCredentials(credentials)
                .build(), subscriptionId);
    }

    public static ComputeManager authenticate(RestClient restClient, String subscriptionId) {
        return new ComputeManager(restClient, subscriptionId);
    }

    public interface Configurable extends AzureConfigurable<Configurable> {
        ComputeManager authenticate(ServiceClientCredentials credentials, String subscriptionId);
    }

    private static final class ConfigurableImpl extends AzureConfigurableImpl<Configurable> implements  Configurable {
        public ComputeManager authenticate(ServiceClientCredentials credentials, String subscriptionId) {
            return ComputeManager.authenticate(buildRestClient(credentials), subscriptionId);
        }
    }

    private ComputeManager(RestClient restClient, String subscriptionId) {
        computeManagementClient = new ComputeManagementClientImpl(restClient);
        computeManagementClient.setSubscriptionId(subscriptionId);
        resourceManager = ResourceManager.authenticate(restClient).withSubscription(subscriptionId);
    }

    private ComputeManager() {}

    public AvailabilitySets availabilitySets() {
        if (availabilitySets == null) {
            availabilitySets = new AvailabilitySetsImpl(computeManagementClient.availabilitySets(),
                    resourceManager.resourceGroups(), virtualMachines());
        }
        return availabilitySets;
    }

    public VirtualMachines virtualMachines() {
        if (virtualMachines == null) {
            virtualMachines = new VirtualMachinesImpl(computeManagementClient.virtualMachines());
        }
        return virtualMachines;
    }
}
