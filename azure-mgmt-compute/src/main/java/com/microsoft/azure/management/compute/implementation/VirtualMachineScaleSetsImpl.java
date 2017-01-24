package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
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
import rx.Completable;

import java.util.ArrayList;

/**
 * The implementation for {@link VirtualMachineScaleSets}.
 */
@LangDefinition
public class VirtualMachineScaleSetsImpl
        extends GroupableResourcesImpl<
                        VirtualMachineScaleSet,
                        VirtualMachineScaleSetImpl,
                        VirtualMachineScaleSetInner,
                        VirtualMachineScaleSetsInner,
                        ComputeManager>
        implements VirtualMachineScaleSets {
    private final VirtualMachineScaleSetVMsInner vmInstancesClient;
    private final StorageManager storageManager;
    private final NetworkManager networkManager;

    VirtualMachineScaleSetsImpl(VirtualMachineScaleSetsInner client,
                        VirtualMachineScaleSetVMsInner vmInstancesClient,
                        ComputeManager computeManager,
                        StorageManager storageManager,
                        NetworkManager networkManager) {
        super(client, computeManager);
        this.vmInstancesClient = vmInstancesClient;
        this.storageManager = storageManager;
        this.networkManager = networkManager;
    }

    @Override
    public VirtualMachineScaleSet getByGroup(String groupName, String name) {
        return wrapModel(this.innerCollection.get(groupName, name));
    }

    @Override
    public PagedList<VirtualMachineScaleSet> listByGroup(String groupName) {
        return wrapList(this.innerCollection.list(groupName));
    }

    @Override
    public PagedList<VirtualMachineScaleSet> list() {
        return wrapList(this.innerCollection.listAll());
    }

    @Override
    public Completable deleteByGroupAsync(String groupName, String name) {
        return this.innerCollection.deleteAsync(groupName, name).toCompletable();
    }

    @Override
    public void deallocate(String groupName, String name) {
        this.innerCollection.deallocate(groupName, name);
    }

    @Override
    public void powerOff(String groupName, String name) {
        this.innerCollection.powerOff(groupName, name);
    }

    @Override
    public void restart(String groupName, String name) {
        this.innerCollection.restart(groupName, name);
    }

    @Override
    public void start(String groupName, String name) {
        this.innerCollection.start(groupName, name);
    }

    @Override
    public void reimage(String groupName, String name) {
        this.innerCollection.reimage(groupName, name);
    }

    @Override
    public VirtualMachineScaleSetImpl define(String name) {
        return wrapModel(name);
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
                        .withName("primary-nic-cfg")
                        .withIpConfigurations(new ArrayList<VirtualMachineScaleSetIPConfigurationInner>());
        primaryNetworkInterfaceConfiguration
                .ipConfigurations()
                .add(new VirtualMachineScaleSetIPConfigurationInner()
                        .withName("primary-nic-ip-cfg"));

        inner.virtualMachineProfile()
                .networkProfile()
                .networkInterfaceConfigurations()
                .add(primaryNetworkInterfaceConfiguration);

        return new VirtualMachineScaleSetImpl(name,
                inner,
                this.innerCollection,
                this.vmInstancesClient,
                super.myManager,
                this.storageManager,
                this.networkManager);
    }

    @Override
    protected VirtualMachineScaleSetImpl wrapModel(VirtualMachineScaleSetInner inner) {
        if (inner == null) {
            return null;
        }
        return new VirtualMachineScaleSetImpl(inner.name(),
                inner,
                this.innerCollection,
                this.vmInstancesClient,
                super.myManager,
                this.storageManager,
                this.networkManager);
    }
}
