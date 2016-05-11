package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachines;
import com.microsoft.azure.management.compute.implementation.api.VirtualMachinesInner;

import java.io.IOException;

class VirtualMachinesImpl
        implements VirtualMachines {
    private final VirtualMachinesInner client;

    VirtualMachinesImpl(VirtualMachinesInner client) {
        this.client = client;
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
}
