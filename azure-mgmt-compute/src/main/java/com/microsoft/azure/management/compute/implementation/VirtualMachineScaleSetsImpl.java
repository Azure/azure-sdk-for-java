package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.compute.VirtualMachineScaleSet;
import com.microsoft.azure.management.compute.VirtualMachineScaleSetNetworkProfile;
import com.microsoft.azure.management.compute.VirtualMachineScaleSetOSDisk;
import com.microsoft.azure.management.compute.VirtualMachineScaleSetOSProfile;
import com.microsoft.azure.management.compute.VirtualMachineScaleSetStorageProfile;
import com.microsoft.azure.management.compute.VirtualMachineScaleSetVMProfile;
import com.microsoft.azure.management.compute.VirtualMachineScaleSets;
import com.microsoft.azure.management.network.implementation.NetworkManager;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;
import com.microsoft.azure.management.storage.implementation.StorageManager;

import java.io.IOException;
import java.util.ArrayList;

/**
 * The implementation for {@link VirtualMachineScaleSets}.
 */
public class VirtualMachineScaleSetsImpl
        extends GroupableResourcesImpl<
                        VirtualMachineScaleSet,
                        VirtualMachineScaleSetImpl,
                        VirtualMachineScaleSetInner,
                        VirtualMachineScaleSetsInner,
                        ComputeManager>
        implements VirtualMachineScaleSets {
    private final StorageManager storageManager;
    private final NetworkManager networkManager;

    VirtualMachineScaleSetsImpl(VirtualMachineScaleSetsInner client,
                        ComputeManager computeManager,
                        StorageManager storageManager,
                        NetworkManager networkManager) {
        super(client, computeManager);
        this.storageManager = storageManager;
        this.networkManager = networkManager;
    }

    @Override
    public VirtualMachineScaleSetImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    public VirtualMachineScaleSet getByGroup(String groupName, String name) throws CloudException, IOException {
        return null;
    }

    @Override
    protected VirtualMachineScaleSetImpl wrapModel(String name) {
        VirtualMachineScaleSetInner inner = new VirtualMachineScaleSetInner();

        inner.withVirtualMachineProfile(new VirtualMachineScaleSetVMProfile());
        inner.virtualMachineProfile()
                .withStorageProfile(new VirtualMachineScaleSetStorageProfile()
                        .withOsDisk(new VirtualMachineScaleSetOSDisk().withVhdContainers(new ArrayList<String>())));
        inner.virtualMachineProfile()
                .withOsProfile(new VirtualMachineScaleSetOSProfile());

        inner.virtualMachineProfile()
                .withNetworkProfile(new VirtualMachineScaleSetNetworkProfile());

        inner.virtualMachineProfile()
                .networkProfile()
                .withNetworkInterfaceConfigurations(new ArrayList<VirtualMachineScaleSetNetworkConfigurationInner>());

        VirtualMachineScaleSetNetworkConfigurationInner primaryNetworkInterfaceConfiguration =
                new VirtualMachineScaleSetNetworkConfigurationInner()
                        .withPrimary(true)
                        .withName("default")
                        .withIpConfigurations(new ArrayList<VirtualMachineScaleSetIPConfigurationInner>());
        primaryNetworkInterfaceConfiguration
                .ipConfigurations()
                .add(new VirtualMachineScaleSetIPConfigurationInner());

        inner.virtualMachineProfile()
                .networkProfile()
                .networkInterfaceConfigurations()
                .add(primaryNetworkInterfaceConfiguration);

        return new VirtualMachineScaleSetImpl(name,
                inner,
                this.innerCollection,
                super.myManager,
                this.storageManager,
                this.networkManager);
    }

    @Override
    protected VirtualMachineScaleSetImpl wrapModel(VirtualMachineScaleSetInner inner) {
        return new VirtualMachineScaleSetImpl(inner.name(),
                inner,
                this.innerCollection,
                super.myManager,
                this.storageManager,
                this.networkManager);
    }
}
