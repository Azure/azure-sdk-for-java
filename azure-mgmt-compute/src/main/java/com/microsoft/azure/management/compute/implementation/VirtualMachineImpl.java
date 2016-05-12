package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.SubResource;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.implementation.api.*;
import com.microsoft.azure.management.resources.ResourceGroups;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.model.Provisionable;
import com.microsoft.azure.management.storage.StorageAccount;

import java.util.List;

class VirtualMachineImpl
        extends GroupableResourceImpl<VirtualMachine, VirtualMachineInner, VirtualMachineImpl>
        implements
            VirtualMachine,
            VirtualMachine.DefinitionBlank,
            VirtualMachine.DefinitionWithGroup,
            VirtualMachine.DefinitionWithDataDisk,
            VirtualMachine.DefinitionWithStorageAccount {
    private final VirtualMachinesInner client;
    private DefinitionWithOSDiskBaseImpl definitionWithOSDiskBaseImpl;

    VirtualMachineImpl(String name, VirtualMachineInner innerModel, VirtualMachinesInner client, ResourceGroups resourceGroups) {
        super(name, innerModel, resourceGroups);
        this.client = client;
    }

    @Override
    public List<Provisionable<?>> prerequisites() {
        return super.prerequisites();
    }

    @Override
    public Plan plan() {
        return inner().plan();
    }

    @Override
    public HardwareProfile hardwareProfile() {
        return inner().hardwareProfile();
    }

    @Override
    public StorageProfile storageProfile() {
        return inner().storageProfile();
    }

    @Override
    public OSProfile osProfile() {
        return inner().osProfile();
    }

    @Override
    public NetworkProfile networkProfile() {
        return inner().networkProfile();
    }

    @Override
    public DiagnosticsProfile diagnosticsProfile() {
        return inner().diagnosticsProfile();
    }

    @Override
    public SubResource availabilitySet() {
        return inner().availabilitySet();
    }

    @Override
    public String provisioningState() {
        return inner().provisioningState();
    }

    @Override
    public VirtualMachineInstanceView instanceView() {
        return inner().instanceView();
    }

    @Override
    public String licenseType() {
        return inner().licenseType();
    }

    @Override
    public List<VirtualMachineExtensionInner> resources() {
        return inner().resources();
    }

    @Override
    public DefinitionWithNewOSDisk withNewStorageAccount(String name) {
        // TODO Setup storage account details
        DefinitionWithNewOSDiskImpl definitionWithNewOSDiskImpl = new DefinitionWithNewOSDiskImpl(this, this.name());
        this.definitionWithOSDiskBaseImpl = definitionWithNewOSDiskImpl;
        return definitionWithNewOSDiskImpl;
    }

    @Override
    public DefinitionWithOSDisk withExistingStorageAccount(String name) {
        // TODO Setup storage account details
        DefinitionWithOSDiskImpl  definitionWithOSDiskImpl = new DefinitionWithOSDiskImpl(this, this.name());
        this.definitionWithOSDiskBaseImpl = definitionWithOSDiskImpl;
        return definitionWithOSDiskImpl;
    }

    @Override
    public  DefinitionWithOSDisk withExistingStorageAccount(StorageAccount.DefinitionProvisionable provisionable) {
        // TODO Setup storage account details
        DefinitionWithOSDiskImpl  definitionWithOSDiskImpl = new DefinitionWithOSDiskImpl(this, this.name());
        this.definitionWithOSDiskBaseImpl = definitionWithOSDiskImpl;
        return definitionWithOSDiskImpl;
    }

    @Override
    public VirtualMachine refresh() throws Exception {
        return this;
    }
}
