/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.compute.implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachineSizes;
import com.microsoft.azure.management.compute.VirtualMachines;
import com.microsoft.azure.management.compute.NetworkInterfaceReference;
import com.microsoft.azure.management.compute.StorageProfile;
import com.microsoft.azure.management.compute.OSDisk;
import com.microsoft.azure.management.compute.DataDisk;
import com.microsoft.azure.management.compute.OSProfile;
import com.microsoft.azure.management.compute.HardwareProfile;
import com.microsoft.azure.management.compute.NetworkProfile;
import com.microsoft.azure.management.network.implementation.NetworkManager;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;
import com.microsoft.azure.management.storage.implementation.StorageManager;
import com.microsoft.rest.ServiceResponse;

import java.io.IOException;
import java.util.ArrayList;

/**
 * The implementation for {@link VirtualMachines}.
 */
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

    VirtualMachinesImpl(VirtualMachinesInner client,
                        VirtualMachineSizesInner virtualMachineSizesClient,
                        ComputeManager computeManager,
                        StorageManager storageManager,
                        NetworkManager networkManager) {
        super(client, computeManager);
        this.storageManager = storageManager;
        this.networkManager = networkManager;
        this.vmSizes = new VirtualMachineSizesImpl(virtualMachineSizesClient);
    }

    // Actions

    @Override
    public PagedList<VirtualMachine> list() throws CloudException, IOException {
        return wrapList(this.innerCollection.listAll().getBody());
    }

    @Override
    public PagedList<VirtualMachine> listByGroup(String groupName) throws CloudException, IOException {
        return wrapList(this.innerCollection.list(groupName).getBody());
    }

    @Override
    public VirtualMachine getByGroup(String groupName, String name) throws CloudException, IOException {
        return wrapModel(this.innerCollection.get(groupName, name).getBody());
    }

    @Override
    public void delete(String id) throws Exception {
        delete(ResourceUtils.groupFromResourceId(id), ResourceUtils.nameFromResourceId(id));
    }

    @Override
    public void delete(String groupName, String name) throws Exception {
        this.innerCollection.delete(groupName, name);
    }

    @Override
    public VirtualMachine.DefinitionStages.Blank define(String name) {
        return wrapModel(name);
    }

    @Override
    public void deallocate(String groupName, String name) throws CloudException, IOException, InterruptedException {
        this.innerCollection.deallocate(groupName, name);
    }

    @Override
    public void generalize(String groupName, String name) throws CloudException, IOException {
        this.innerCollection.generalize(groupName, name);
    }

    @Override
    public void powerOff(String groupName, String name) throws CloudException, IOException, InterruptedException {
        this.innerCollection.powerOff(groupName, name);
    }

    @Override
    public void restart(String groupName, String name) throws CloudException, IOException, InterruptedException {
        this.innerCollection.restart(groupName, name);
    }

    @Override
    public void start(String groupName, String name) throws CloudException, IOException, InterruptedException {
        this.innerCollection.start(groupName, name);
    }

    @Override
    public void redeploy(String groupName, String name) throws CloudException, IOException, InterruptedException {
        this.innerCollection.redeploy(groupName, name);
    }

    @Override
    public String capture(String groupName, String name,
                          String containerName,
                          boolean overwriteVhd) throws CloudException, IOException, InterruptedException {
        VirtualMachineCaptureParametersInner parameters = new VirtualMachineCaptureParametersInner();
        parameters.withDestinationContainerName(containerName);
        parameters.withOverwriteVhds(overwriteVhd);
        ServiceResponse<VirtualMachineCaptureResultInner> captureResult = this.innerCollection.capture(groupName, name, parameters);
        ObjectMapper mapper = new ObjectMapper();
        //Object to JSON string
        return mapper.writeValueAsString(captureResult.getBody().output());
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
                .withNetworkInterfaces(new ArrayList<NetworkInterfaceReference>()));
        return new VirtualMachineImpl(name,
            inner,
            this.innerCollection,
            super.myManager,
            this.storageManager,
            this.networkManager);
    }

    @Override
    protected VirtualMachineImpl wrapModel(VirtualMachineInner virtualMachineInner) {
        return new VirtualMachineImpl(virtualMachineInner.name(),
                virtualMachineInner,
                this.innerCollection,
                super.myManager,
                this.storageManager,
                this.networkManager);
    }
}
