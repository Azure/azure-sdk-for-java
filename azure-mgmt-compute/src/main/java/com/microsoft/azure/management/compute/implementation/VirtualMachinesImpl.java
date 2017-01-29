/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.compute.implementation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.compute.DataDisk;
import com.microsoft.azure.management.compute.HardwareProfile;
import com.microsoft.azure.management.compute.NetworkProfile;
import com.microsoft.azure.management.compute.OSDisk;
import com.microsoft.azure.management.compute.OSProfile;
import com.microsoft.azure.management.compute.StorageProfile;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachineSizes;
import com.microsoft.azure.management.compute.VirtualMachines;
import com.microsoft.azure.management.network.implementation.NetworkManager;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;
import com.microsoft.azure.management.storage.implementation.StorageManager;
import rx.Completable;
import rx.exceptions.Exceptions;
import rx.functions.Func1;

import java.util.ArrayList;

/**
 * The implementation for {@link VirtualMachines}.
 */
@LangDefinition
class VirtualMachinesImpl
        extends GroupableResourcesImpl<
        VirtualMachine,
        VirtualMachineImpl,
        VirtualMachineInner,
        VirtualMachinesInner,
        ComputeManager>
        implements VirtualMachines {
    private final StorageManager storageManager;
    private final NetworkManager networkManager;
    private final VirtualMachineSizesImpl vmSizes;
    private final VirtualMachineExtensionsInner virtualMachineExtensionsClient;

    VirtualMachinesImpl(VirtualMachinesInner client,
                        VirtualMachineExtensionsInner virtualMachineExtensionsClient,
                        VirtualMachineSizesInner virtualMachineSizesClient,
                        ComputeManager computeManager,
                        StorageManager storageManager,
                        NetworkManager networkManager) {
        super(client, computeManager);
        this.virtualMachineExtensionsClient = virtualMachineExtensionsClient;
        this.storageManager = storageManager;
        this.networkManager = networkManager;
        this.vmSizes = new VirtualMachineSizesImpl(virtualMachineSizesClient);
    }

    // Actions

    @Override
    public PagedList<VirtualMachine> list() {
        return wrapList(this.innerCollection.listAll());
    }

    @Override
    public PagedList<VirtualMachine> listByGroup(String groupName) {
        return wrapList(this.innerCollection.list(groupName));
    }

    @Override
    public VirtualMachine getByGroup(String groupName, String name) {
        return wrapModel(this.innerCollection.get(groupName, name));
    }

    @Override
    public Completable deleteByGroupAsync(String groupName, String name) {
        return this.innerCollection.deleteAsync(groupName, name).toCompletable();
    }

    @Override
    public VirtualMachine.DefinitionStages.Blank define(String name) {
        return wrapModel(name);
    }

    @Override
    public void deallocate(String groupName, String name) {
        this.innerCollection.deallocate(groupName, name);
    }

    @Override
    public void generalize(String groupName, String name) {
        this.innerCollection.generalize(groupName, name);
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
    public void redeploy(String groupName, String name) {
        this.innerCollection.redeploy(groupName, name);
    }

    @Override
    public String capture(String groupName, String name,
                          String containerName,
                          String vhdPrefix,
                          boolean overwriteVhd) {
        VirtualMachineCaptureParametersInner parameters = new VirtualMachineCaptureParametersInner();
        parameters.withDestinationContainerName(containerName);
        parameters.withOverwriteVhds(overwriteVhd);
        parameters.withVhdPrefix(vhdPrefix);
        VirtualMachineCaptureResultInner captureResult = this.innerCollection.capture(groupName, name, parameters);
        ObjectMapper mapper = new ObjectMapper();
        //Object to JSON string
        try {
            return mapper.writeValueAsString(captureResult.output());
        } catch (JsonProcessingException e) {
            throw Exceptions.propagate(e);
        }
    }

    @Override
    public void migrateToManaged(String groupName, String name) {
        this.innerCollection.convertToManagedDisks(groupName, name);
    }

    // Getters
    @Override
    public VirtualMachineSizes sizes() {
        return this.vmSizes;
    }


    // Helper methods

    @Override
    protected VirtualMachineImpl wrapModel(String name) {
        VirtualMachineInner inner = new VirtualMachineInner();
        inner.withStorageProfile(new StorageProfile()
                .withOsDisk(new OSDisk())
                .withDataDisks(new ArrayList<DataDisk>()));
        inner.withOsProfile(new OSProfile());
        inner.withHardwareProfile(new HardwareProfile());
        inner.withNetworkProfile(new NetworkProfile()
                .withNetworkInterfaces(new ArrayList<NetworkInterfaceReferenceInner>()));
        return new VirtualMachineImpl(name,
                inner,
                this.innerCollection,
                this.virtualMachineExtensionsClient,
                super.myManager,
                this.storageManager,
                this.networkManager);
    }

    @Override
    protected VirtualMachineImpl wrapModel(VirtualMachineInner virtualMachineInner) {
        if (virtualMachineInner == null) {
            return null;
        }
        return new VirtualMachineImpl(virtualMachineInner.name(),
                virtualMachineInner,
                this.innerCollection,
                this.virtualMachineExtensionsClient,
                super.myManager,
                this.storageManager,
                this.networkManager);
    }
}