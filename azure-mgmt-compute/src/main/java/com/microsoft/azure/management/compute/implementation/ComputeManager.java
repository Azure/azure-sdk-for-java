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

/**
 * A type that exposes Azure Compute service resource collections.
 */
public final class ComputeManager {
    // The service managers
    private ResourceManager resourceManager;
    // The sdk clients
    private ComputeManagementClientImpl computeManagementClient;
    // The collections
    private AvailabilitySets availabilitySets;
    private VirtualMachines virtualMachines;

    /**
     * Get a Configurable instance that can be used to create ComputeManager with optional configuration.
     *
     * @return Configurable
     */
    public static Configurable configure() {
        return new ComputeManager.ConfigurableImpl();
    }

    /**
     * Creates an instance of ComputeManager that exposes Azure Compute service resource collections.
     *
     * @param credentials The credentials to use
     * @param subscriptionId The subscription
     * @return The ComputeManager
     */
    public static ComputeManager authenticate(ServiceClientCredentials credentials, String subscriptionId) {
        return new ComputeManager(AzureEnvironment.AZURE.newRestClientBuilder()
                .withCredentials(credentials)
                .build(), subscriptionId);
    }

    /**
     * Creates an instance of ComputeManager that exposes Azure Compute service resource collections.
     *
     * @param restClient The RestClient to be used for API calls.
     * @param subscriptionId The subscription
     * @return The ComputeManager
     */
    public static ComputeManager authenticate(RestClient restClient, String subscriptionId) {
        return new ComputeManager(restClient, subscriptionId);
    }

    public interface Configurable extends AzureConfigurable<Configurable> {
        /**
         * Creates an instance of ComputeManager that exposes Azure Compute service resource collections.
         *
         * @param credentials The credentials to use
         * @param subscriptionId The subscription
         * @return The ComputeManager
         */
        ComputeManager authenticate(ServiceClientCredentials credentials, String subscriptionId);
    }

    private static final class ConfigurableImpl extends AzureConfigurableImpl<Configurable> implements  Configurable {
        @Override
        public ComputeManager authenticate(ServiceClientCredentials credentials, String subscriptionId) {
            return ComputeManager.authenticate(buildRestClient(credentials), subscriptionId);
        }
    }

    private ComputeManager(RestClient restClient, String subscriptionId) {
        computeManagementClient = new ComputeManagementClientImpl(restClient);
        computeManagementClient.setSubscriptionId(subscriptionId);
        resourceManager = ResourceManager.authenticate(restClient).withSubscription(subscriptionId);
    }

    private ComputeManager() {
    }

    /**
     * Get Azure availability set collection.
     * <p/>
     * The collection supports performing CRUD operations on Azure availability sets
     *
     * @return The availability set collection
     */
    public AvailabilitySets availabilitySets() {
        if (availabilitySets == null) {
            availabilitySets = new AvailabilitySetsImpl(computeManagementClient.availabilitySets(),
                    resourceManager.resourceGroups(), virtualMachines());
        }
        return availabilitySets;
    }

    /**
     * Get Azure virtual machine collection.
     * <p/>
     * The collection supports performing CRUD operations on Azure virtual machines
     *
     * @return The virtual machine collection
     */
    public VirtualMachines virtualMachines() {
        if (virtualMachines == null) {
            virtualMachines = new VirtualMachinesImpl(computeManagementClient.virtualMachines(), computeManagementClient.virtualMachineSizes());
        }
        return virtualMachines;
    }
}
