package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.Page;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachineSize;
import com.microsoft.azure.management.compute.VirtualMachines;
import com.microsoft.azure.management.compute.implementation.api.VirtualMachineSizeInner;
import com.microsoft.azure.management.compute.implementation.api.VirtualMachineSizesInner;
import com.microsoft.azure.management.compute.implementation.api.VirtualMachinesInner;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import com.microsoft.azure.management.resources.implementation.api.PageImpl;
import com.microsoft.rest.RestException;

import java.io.IOException;

class VirtualMachinesImpl
        implements VirtualMachines {
    private final VirtualMachinesInner client;
    private final VirtualMachineSizesInner virtualMachineSizesClient;

    VirtualMachinesImpl(VirtualMachinesInner client, VirtualMachineSizesInner virtualMachineSizesClient) {
        this.client = client;
        this.virtualMachineSizesClient = virtualMachineSizesClient;
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
        return null;
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
        page.setItems(virtualMachineSizesClient.list(region).getBody());
        page.setNextPageLink(null);
        return converter.convert(new PagedList<VirtualMachineSizeInner>(page) {
            @Override
            public Page<VirtualMachineSizeInner> nextPage(String nextPageLink) throws RestException, IOException {
                return null;
            }
        });
    }
}
