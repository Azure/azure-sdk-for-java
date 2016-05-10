package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.implementation.api.DiskCreateOptionTypes;
import com.microsoft.azure.management.compute.implementation.api.OperatingSystemTypes;

final class DefinitionWithOSDiskImpl extends DefinitionWithOSDiskBaseImpl
        implements
        VirtualMachine.DefinitionWithOSDisk,
        VirtualMachine.OSDiskFromImage {
    public DefinitionWithOSDiskImpl(VirtualMachine.DefinitionWithDataDisk definitionWithDataDisk) {
        super(definitionWithDataDisk);
    }

    public VirtualMachine.OSDiskFromImage defineOSDisk(String name) {
        this.storageProfile.osDisk().setName(name);
        return this;
    }

    public VirtualMachine.DefinitionWithDataDisk withUserImage(String containerName, String vhdName) {
        this.storageProfile.osDisk().setCreateOption(DiskCreateOptionTypes.FROMIMAGE);
        this.storageProfile.osDisk().setImage(toVhd(containerName, vhdName));
        return definitionWithDataDisk;
    }

    public VirtualMachine.DefinitionWithDataDisk withExistingOSDisk(String containerName, String vhdName, OperatingSystemTypes osType) {
        this.storageProfile.osDisk().setCreateOption(DiskCreateOptionTypes.ATTACH);
        this.storageProfile.osDisk().setVhd(toVhd(containerName, vhdName));
        this.storageProfile.osDisk().setOsType(osType);
        return definitionWithDataDisk;
    }

    public VirtualMachine.DefinitionWithOSDiskAttached fromUserImage(String containerName, String vhdName) {
        this.storageProfile.osDisk().setCreateOption(DiskCreateOptionTypes.FROMIMAGE);
        this.storageProfile.osDisk().setImage(toVhd(containerName, vhdName));
        return this;
    }
}
