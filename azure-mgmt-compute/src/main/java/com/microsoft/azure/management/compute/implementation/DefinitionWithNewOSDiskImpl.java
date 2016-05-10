package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.management.compute.VirtualMachine;

final class DefinitionWithNewOSDiskImpl  extends DefinitionWithOSDiskBaseImpl
        implements VirtualMachine.DefinitionWithNewOSDisk
{
    public DefinitionWithNewOSDiskImpl(VirtualMachine.DefinitionWithDataDisk definitionWithDataDisk) {
        super(definitionWithDataDisk);
    }

    public VirtualMachine.NewOSDiskFromImage defineOSDisk(String name) {
        this.storageProfile.osDisk().setName(name);
        return this;
    }
}
