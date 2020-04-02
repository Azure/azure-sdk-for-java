/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.compute.implementation;

import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.serializer.AzureJacksonAdapter;
import com.azure.management.AzureTokenCredential;
import com.azure.management.RestClient;
import com.azure.management.RestClientBuilder;
import com.azure.management.Utils;
import com.azure.management.compute.AvailabilitySets;
import com.azure.management.compute.ComputeSkus;
import com.azure.management.compute.ComputeUsages;
import com.azure.management.compute.Disks;
import com.azure.management.compute.Galleries;
import com.azure.management.compute.GalleryImageVersions;
import com.azure.management.compute.GalleryImages;
import com.azure.management.compute.Snapshots;
import com.azure.management.compute.VirtualMachineCustomImages;
import com.azure.management.compute.VirtualMachineExtensionImages;
import com.azure.management.compute.VirtualMachineImages;
import com.azure.management.compute.VirtualMachineScaleSets;
import com.azure.management.compute.VirtualMachines;
import com.azure.management.compute.models.ComputeManagementClientBuilder;
import com.azure.management.compute.models.ComputeManagementClientImpl;
import com.azure.management.graphrbac.implementation.GraphRbacManager;
import com.azure.management.network.implementation.NetworkManager;
import com.azure.management.resources.fluentcore.arm.AzureConfigurable;
import com.azure.management.resources.fluentcore.arm.implementation.AzureConfigurableImpl;
import com.azure.management.resources.fluentcore.arm.implementation.Manager;
import com.azure.management.resources.fluentcore.utils.SdkContext;
import com.azure.management.storage.implementation.StorageManager;

/**
 * Entry point to Azure compute resource management.
 */
public final class ComputeManager extends Manager<ComputeManager, ComputeManagementClientImpl> {
    // The service managers
    private StorageManager storageManager;
    private NetworkManager networkManager;
    private GraphRbacManager rbacManager;

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
    private ComputeSkus computeSkus;
    private Galleries galleries;
    private GalleryImages galleryImages;
    private GalleryImageVersions galleryImageVersions;

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
     * @param credential the credentials to use
     * @param subscriptionId the subscription
     * @return the ComputeManager
     */
    public static ComputeManager authenticate(AzureTokenCredential credential, String subscriptionId) {
        return authenticate(new RestClientBuilder()
                .withBaseUrl(credential.getEnvironment(), AzureEnvironment.Endpoint.RESOURCE_MANAGER)
                .withCredential(credential)
                .withSerializerAdapter(new AzureJacksonAdapter())
                .buildClient(), subscriptionId);
    }

    /**
     * Creates an instance of ComputeManager that exposes Compute resource management API entry points.
     *
     * @param restClient the RestClient to be used for API calls.
     * @param subscriptionId the subscription
     * @return the ComputeManager
     */
    public static ComputeManager authenticate(RestClient restClient, String subscriptionId) {
        return authenticate(restClient, subscriptionId, new SdkContext());
    }

    /**
     * Creates an instance of ComputeManager that exposes Compute resource management API entry points.
     *
     * @param restClient the RestClient to be used for API calls.
     * @param subscriptionId the subscription
     * @param sdkContext the sdk context
     * @return the ComputeManager
     */
    public static ComputeManager authenticate(RestClient restClient, String subscriptionId, SdkContext sdkContext) {
        return new ComputeManager(restClient, subscriptionId, sdkContext);
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
        ComputeManager authenticate(AzureTokenCredential credentials, String subscriptionId);
    }

    /**
     * The implementation for Configurable interface.
     */
    private static final class ConfigurableImpl extends AzureConfigurableImpl<Configurable> implements  Configurable {
        @Override
        public ComputeManager authenticate(AzureTokenCredential credential, String subscriptionId) {
            return ComputeManager.authenticate(buildRestClient(credential), subscriptionId);
        }
    }

    private ComputeManager(RestClient restClient, String subscriptionId, SdkContext sdkContext) {
        super(
                restClient,
                subscriptionId,
                new ComputeManagementClientBuilder()
                        .pipeline(restClient.getHttpPipeline())
                        .subscriptionId(subscriptionId)
                        .build(),
                sdkContext
        );
        storageManager = StorageManager.authenticate(restClient, subscriptionId, sdkContext);
        networkManager = NetworkManager.authenticate(restClient, subscriptionId, sdkContext);
        rbacManager = GraphRbacManager.authenticate(restClient, Utils.getTenantIdFromRestClient(restClient), sdkContext);
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
     * @return the virtual machine resource management API entry point
     */
    public VirtualMachines virtualMachines() {
        if (virtualMachines == null) {
            virtualMachines = new VirtualMachinesImpl(
                    this,
                    storageManager,
                    networkManager,
                    rbacManager);
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
                    networkManager,
                    this.rbacManager);
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

    /**
     * @return the compute service SKU management API entry point
     */
    public ComputeSkus computeSkus() {
        if (computeSkus == null) {
            computeSkus = new ComputeSkusImpl(this);
        }
        return computeSkus;
    }

    /**
     * @return the compute service gallery management entry point
     */
    public Galleries galleries() {
        if (galleries == null) {
            galleries = new GalleriesImpl(this);
        }
        return galleries;
    }

    /**
     * @return the compute service gallery image management entry point
     */
    public GalleryImages galleryImages() {
        if (galleryImages == null) {
            galleryImages = new GalleryImagesImpl(this);
        }
        return galleryImages;
    }

    /**
     * @return the compute service gallery image version management entry point
     */
    public GalleryImageVersions galleryImageVersions() {
        if (galleryImageVersions == null) {
            galleryImageVersions = new GalleryImageVersionsImpl(this);
        }
        return galleryImageVersions;
    }
}