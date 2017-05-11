/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.AzureResponseBuilder;
import com.microsoft.azure.credentials.AzureTokenCredentials;
import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.compute.AvailabilitySets;
import com.microsoft.azure.management.compute.ComputeUsages;
import com.microsoft.azure.management.compute.Disks;
import com.microsoft.azure.management.compute.Snapshots;
import com.microsoft.azure.management.compute.VirtualMachineCustomImages;
import com.microsoft.azure.management.compute.VirtualMachineExtensionImages;
import com.microsoft.azure.management.compute.VirtualMachineImages;
import com.microsoft.azure.management.compute.VirtualMachineScaleSets;
import com.microsoft.azure.management.compute.VirtualMachines;
import com.microsoft.azure.management.compute.ContainerServices;
import com.microsoft.azure.management.network.implementation.NetworkManager;
import com.microsoft.azure.management.resources.fluentcore.arm.AzureConfigurable;
import com.microsoft.azure.management.resources.fluentcore.arm.implementation.AzureConfigurableImpl;
import com.microsoft.azure.management.resources.fluentcore.arm.implementation.Manager;
import com.microsoft.azure.management.resources.fluentcore.utils.ProviderRegistrationInterceptor;
import com.microsoft.azure.management.storage.implementation.StorageManager;
import com.microsoft.azure.serializer.AzureJacksonAdapter;
import com.microsoft.rest.RestClient;

/**
 * Entry point to Azure compute resource management.
 */
public final class ComputeManager extends Manager<ComputeManager, ComputeManagementClientImpl> {
    // The service managers
    private StorageManager storageManager;
    private NetworkManager networkManager;
    // The collections
    private AvailabilitySets availabilitySets;
    private VirtualMachines virtualMachines;
    private VirtualMachineImages virtualMachineImages;
    private VirtualMachineExtensionImages virtualMachineExtensionImages;
    private VirtualMachineScaleSets virtualMachineScaleSets;
    private ComputeUsages computeUsages;
    private VirtualMachineCustomImages virtualMachineCustomImages;
    private Disks disks;
    private Snapshots snapshots;
    private ContainerServices containerServices;

    /**
     * Get a Configurable instance that can be used to create ComputeManager with optional configuration.
     *
     * @return Configurable
     */
    public static Configurable configure() {
        return new ComputeManager.ConfigurableImpl();
    }

    /**
     * Creates an instance of ComputeManager that exposes Compute resource management API entry points.
     *
     * @param credentials the credentials to use
     * @param subscriptionId the subscription
     * @return the ComputeManager
     */
    public static ComputeManager authenticate(AzureTokenCredentials credentials, String subscriptionId) {
        return new ComputeManager(new RestClient.Builder()
                .withBaseUrl(credentials.environment(), AzureEnvironment.Endpoint.RESOURCE_MANAGER)
                .withCredentials(credentials)
                .withSerializerAdapter(new AzureJacksonAdapter())
                .withResponseBuilderFactory(new AzureResponseBuilder.Factory())
                .withInterceptor(new ProviderRegistrationInterceptor(credentials))
                .build(), subscriptionId);
    }

    /**
     * Creates an instance of ComputeManager that exposes Compute resource management API entry points.
     *
     * @param restClient the RestClient to be used for API calls.
     * @param subscriptionId the subscription
     * @return the ComputeManager
     */
    public static ComputeManager authenticate(RestClient restClient, String subscriptionId) {
        return new ComputeManager(restClient, subscriptionId);
    }

    /**
     * The interface allowing configurations to be set.
     */
    public interface Configurable extends AzureConfigurable<Configurable> {
        /**
         * Creates an instance of ComputeManager that exposes Compute resource management API entry points.
         *
         * @param credentials the credentials to use
         * @param subscriptionId the subscription
         * @return the ComputeManager
         */
        ComputeManager authenticate(AzureTokenCredentials credentials, String subscriptionId);
    }

    /**
     * The implementation for Configurable interface.
     */
    private static final class ConfigurableImpl extends AzureConfigurableImpl<Configurable> implements  Configurable {
        @Override
        public ComputeManager authenticate(AzureTokenCredentials credentials, String subscriptionId) {
            return ComputeManager.authenticate(buildRestClient(credentials), subscriptionId);
        }
    }

    private ComputeManager(RestClient restClient, String subscriptionId) {
        super(
                restClient,
                subscriptionId,
                new ComputeManagementClientImpl(restClient).withSubscriptionId(subscriptionId));
        storageManager = StorageManager.authenticate(restClient, subscriptionId);
        networkManager = NetworkManager.authenticate(restClient, subscriptionId);
    }

    /**
     * @return the availability set resource management API entry point
     */
    public AvailabilitySets availabilitySets() {
        if (availabilitySets == null) {
            availabilitySets = new AvailabilitySetsImpl(this);
        }
        return availabilitySets;
    }

    /**
     * @return the availability set resource management API entry point
     */
    @Beta
    public ContainerServices containerServices() {
        if (containerServices == null) {
            containerServices = new ContainerServicesImpl(this);
        }
        return containerServices;
    }

    /**
     * @return the virtual machine resource management API entry point
     */
    public VirtualMachines virtualMachines() {
        if (virtualMachines == null) {
            virtualMachines = new VirtualMachinesImpl(
                    this,
                    storageManager,
                    networkManager);
        }
        return virtualMachines;
    }

    /**
     * @return the virtual machine image resource management API entry point
     */
    public VirtualMachineImages virtualMachineImages() {
        if (virtualMachineImages == null) {
            virtualMachineImages = new VirtualMachineImagesImpl(new VirtualMachinePublishersImpl(super.innerManagementClient.virtualMachineImages(),
                    super.innerManagementClient.virtualMachineExtensionImages()),
                    super.innerManagementClient.virtualMachineImages());
        }
        return virtualMachineImages;
    }

    /**
     * @return the virtual machine extension image resource management API entry point
     */
    public VirtualMachineExtensionImages virtualMachineExtensionImages() {
        if (virtualMachineExtensionImages == null) {
            virtualMachineExtensionImages = new VirtualMachineExtensionImagesImpl(new VirtualMachinePublishersImpl(super.innerManagementClient.virtualMachineImages(),
                    super.innerManagementClient.virtualMachineExtensionImages()));
        }
        return virtualMachineExtensionImages;
    }

    /**
     * @return the virtual machine scale set resource management API entry point
     */
    public VirtualMachineScaleSets virtualMachineScaleSets() {
        if (virtualMachineScaleSets == null) {
            virtualMachineScaleSets = new VirtualMachineScaleSetsImpl(
                    this,
                    storageManager,
                    networkManager);
        }
        return virtualMachineScaleSets;
    }

    /**
     * @return the compute resource usage management API entry point
     */
    public ComputeUsages usages() {
        if (computeUsages == null) {
            computeUsages = new ComputeUsagesImpl(super.innerManagementClient);
        }
        return computeUsages;
    }

    /**
     * @return the virtual machine custom image management API entry point
     */
    public VirtualMachineCustomImages virtualMachineCustomImages() {
        if (virtualMachineCustomImages == null) {
            virtualMachineCustomImages = new VirtualMachineCustomImagesImpl(this);
        }
        return virtualMachineCustomImages;
    }

    /**
     * @return the managed disk management API entry point
     */
    public Disks disks() {
        if (disks == null) {
            disks = new DisksImpl(this);
        }
        return disks;
    }

    /**
     * @return the managed snapshot management API entry point
     */
    public Snapshots snapshots() {
        if (snapshots == null) {
            snapshots = new SnapshotsImpl(this);
        }
        return snapshots;
    }
}