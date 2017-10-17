/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.compute.VirtualMachineScaleSet;
import com.microsoft.azure.management.compute.VirtualMachineScaleSetNetworkProfile;
import com.microsoft.azure.management.compute.VirtualMachineScaleSetOSDisk;
import com.microsoft.azure.management.compute.VirtualMachineScaleSetOSProfile;
import com.microsoft.azure.management.compute.VirtualMachineScaleSetStorageProfile;
import com.microsoft.azure.management.compute.VirtualMachineScaleSetVMProfile;
import com.microsoft.azure.management.compute.VirtualMachineScaleSets;
import com.microsoft.azure.management.graphrbac.implementation.GraphRbacManager;
import com.microsoft.azure.management.network.implementation.NetworkManager;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;
import com.microsoft.azure.management.storage.implementation.StorageManager;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import rx.Completable;

import java.util.ArrayList;

/**
 * The implementation for VirtualMachineScaleSets.
 */
@LangDefinition
public class VirtualMachineScaleSetsImpl
    extends TopLevelModifiableResourcesImpl<
        VirtualMachineScaleSet,
        VirtualMachineScaleSetImpl,
        VirtualMachineScaleSetInner,
        VirtualMachineScaleSetsInner,
        ComputeManager>
    implements VirtualMachineScaleSets {
    private final StorageManager storageManager;
    private final NetworkManager networkManager;
    private final GraphRbacManager rbacManager;

    VirtualMachineScaleSetsImpl(
            ComputeManager computeManager,
            StorageManager storageManager,
            NetworkManager networkManager,
            GraphRbacManager rbacManager) {
        super(computeManager.inner().virtualMachineScaleSets(), computeManager);
        this.storageManager = storageManager;
        this.networkManager = networkManager;
        this.rbacManager = rbacManager;
    }

    @Override
    public void deallocate(String groupName, String name) {
        this.inner().deallocate(groupName, name);
    }

    @Override
    public Completable deallocateAsync(String groupName, String name) {
        return this.inner().deallocateAsync(groupName, name).toCompletable();
    }

    @Override
    public ServiceFuture<Void> deallocateAsync(String groupName, String name, ServiceCallback<Void> callback) {
        return ServiceFuture.fromBody(deallocateAsync(groupName, name), callback);
    }

    @Override
    public void powerOff(String groupName, String name) {
        this.inner().powerOff(groupName, name);
    }

    @Override
    public Completable powerOffAsync(String groupName, String name) {
        return this.inner().powerOffAsync(groupName, name).toCompletable();
    }

    @Override
    public ServiceFuture<Void> powerOffAsync(String groupName, String name, ServiceCallback<Void> callback) {
        return ServiceFuture.fromBody(powerOffAsync(groupName, name), callback);
    }

    @Override
    public void restart(String groupName, String name) {
        this.inner().restart(groupName, name);
    }

    @Override
    public Completable restartAsync(String groupName, String name) {
        return this.inner().restartAsync(groupName, name).toCompletable();
    }

    @Override
    public ServiceFuture<Void> restartAsync(String groupName, String name, ServiceCallback<Void> callback) {
        return ServiceFuture.fromBody(restartAsync(groupName, name), callback);
    }

    @Override
    public void start(String groupName, String name) {
        this.inner().start(groupName, name);
    }

    @Override
    public Completable startAsync(String groupName, String name) {
        return this.inner().startAsync(groupName, name).toCompletable();
    }

    @Override
    public ServiceFuture<Void> startAsync(String groupName, String name, ServiceCallback<Void> callback) {
        return ServiceFuture.fromBody(startAsync(groupName, name), callback);
    }

    @Override
    public void reimage(String groupName, String name) {
        this.inner().reimage(groupName, name);
    }

    @Override
    public Completable reimageAsync(String groupName, String name) {
       return this.inner().reimageAsync(groupName, name).toCompletable();
    }

    @Override
    public ServiceFuture<Void> reimageAsync(String groupName, String name, ServiceCallback<Void> callback) {
        return ServiceFuture.fromBody(reimageAsync(groupName, name), callback);
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
                this.manager(),
                this.storageManager,
                this.networkManager,
                this.rbacManager);
    }

    @Override
    protected VirtualMachineScaleSetImpl wrapModel(VirtualMachineScaleSetInner inner) {
        if (inner == null) {
            return null;
        }
        return new VirtualMachineScaleSetImpl(inner.name(),
                inner,
                this.manager(),
                this.storageManager,
                this.networkManager,
                this.rbacManager);
    }
}
