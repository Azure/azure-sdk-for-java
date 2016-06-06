package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.Page;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.compute.AvailabilitySets;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachineSize;
import com.microsoft.azure.management.compute.VirtualMachines;
import com.microsoft.azure.management.compute.implementation.api.VirtualMachineInner;
import com.microsoft.azure.management.compute.implementation.api.VirtualMachineSizeInner;
import com.microsoft.azure.management.compute.implementation.api.VirtualMachineSizesInner;
import com.microsoft.azure.management.compute.implementation.api.VirtualMachinesInner;
import com.microsoft.azure.management.network.implementation.NetworkManager;
import com.microsoft.azure.management.network.implementation.api.NetworkInterfacesInner;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import com.microsoft.azure.management.resources.implementation.ResourceManager;
import com.microsoft.azure.management.resources.implementation.api.PageImpl;
import com.microsoft.azure.management.storage.implementation.StorageManager;
import com.microsoft.rest.RestException;

import java.io.IOException;

class VirtualMachinesImpl
        implements VirtualMachines {
    private final VirtualMachinesInner client;
    private final VirtualMachineSizesInner virtualMachineSizesClient;
    private final AvailabilitySets availabilitySets;
    private final NetworkInterfacesInner networkInterfaces;
    private final ResourceManager resourceManager;
    private final StorageManager storageManager;
    private final NetworkManager networkManager;


    VirtualMachinesImpl(VirtualMachinesInner client,
                        VirtualMachineSizesInner virtualMachineSizesClient,
                        NetworkInterfacesInner networkInterfaces, // TODO this will be removed once we have NetworkInterfaces entry point available in NetworkManager
                        AvailabilitySets availabilitySets,
                        ResourceManager resourceManager,
                        StorageManager storageManager,
                        NetworkManager networkManager) {
        this.client = client;
        this.virtualMachineSizesClient = virtualMachineSizesClient;
        this.networkInterfaces = networkInterfaces;
        this.availabilitySets = availabilitySets;
        this.resourceManager = resourceManager;
        this.storageManager = storageManager;
        this.networkManager = networkManager;
    }

    @Override
    public void delete(String groupName, String name) throws Exception {

    }

    @Override
    public VirtualMachine get(String groupName, String name) throws CloudException, IOException {
        return null;
    }

    @Override
    public PagedList<VirtualMachine> list(String groupName) throws CloudException, IOException {
        return null;
    }

    @Override
    public VirtualMachine.DefinitionBlank define(String name) {
        return createFluentModel(name);
    }

    @Override
    public void delete(String id) throws Exception {

    }

    @Override
    public PagedList<VirtualMachine> list() throws CloudException, IOException {
        return null;
    }

    @Override
    public PagedList<VirtualMachineSize> listSizes(String region) throws CloudException, IOException {
        PagedListConverter<VirtualMachineSizeInner, VirtualMachineSize> converter =
                new PagedListConverter<VirtualMachineSizeInner, VirtualMachineSize>() {
            @Override
            public VirtualMachineSize typeConvert(VirtualMachineSizeInner virtualMachineSizeInner) {
                return new VirtualMachineSizeImpl(virtualMachineSizeInner);
            }
        };

        PageImpl<VirtualMachineSizeInner> page = new PageImpl<>();
        page.withItems(virtualMachineSizesClient.list(region).getBody());
        page.withNextPageLink(null);
        return converter.convert(new PagedList<VirtualMachineSizeInner>(page) {
            @Override
            public Page<VirtualMachineSizeInner> nextPage(String nextPageLink) throws RestException, IOException {
                return null;
            }
        });
    }

    private VirtualMachineImpl createFluentModel(String name) {
        return new VirtualMachineImpl(name,
            new VirtualMachineInner(),
            this.client,
            this.networkInterfaces, // TODO this will be removed once we have NetworkInterfaces entry point available in NetworkManager
            this.availabilitySets,
            this.resourceManager,
            this.storageManager,
            this.networkManager);
    }

    private VirtualMachineImpl createFluentModel(VirtualMachineInner virtualMachineInner) {
        return new VirtualMachineImpl(virtualMachineInner.name(),
                virtualMachineInner,
                this.client,
                this.networkInterfaces,
                this.availabilitySets,
                this.resourceManager,
                this.storageManager,
                this.networkManager);
    }
}
