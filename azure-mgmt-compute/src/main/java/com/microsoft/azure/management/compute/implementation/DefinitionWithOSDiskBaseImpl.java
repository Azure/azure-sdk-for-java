package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.implementation.api.*;

class DefinitionWithOSDiskBaseImpl implements
        VirtualMachine.DefinitionWithVMImage,
        VirtualMachine.NewOSDiskFromImage,
        VirtualMachine.DefinitionWithOSDiskAttached {
    protected final VirtualMachine.DefinitionWithDataDisk definitionWithDataDisk;
    protected StorageProfile storageProfile;
    private String storageAccountName;

    public DefinitionWithOSDiskBaseImpl(VirtualMachine.DefinitionWithDataDisk definitionWithDataDisk) {
        this.definitionWithDataDisk = definitionWithDataDisk;
        this.storageProfile = new StorageProfile();
        this.storageProfile.setOsDisk(new OSDisk());
        this.storageProfile.osDisk().setImage(null);
        this.storageProfile.setImageReference(null);
    }

    protected VirtualHardDisk toVhd(String containerName, String vhdName) {
        VirtualHardDisk vhd = new VirtualHardDisk();
        // TODO: Don't hardcode the domain
        vhd.setUri("https://" + storageAccountName + "blob.core.windows.net/" + containerName + "/" + vhdName);
        return vhd;
    }

    public VirtualMachine.DefinitionWithDataDisk withImage(ImageReference imageReference) {
        this.storageProfile.setImageReference(imageReference);
        this.storageProfile.osDisk().setCreateOption(DiskCreateOptionTypes.FROMIMAGE);
        return definitionWithDataDisk;
    }

    public VirtualMachine.DefinitionWithDataDisk withLatestImage(String publisher, String offer, String sku) {
        return withImage(new ImageReference()
                .setPublisher(publisher).setOffer(offer).setSku(sku).setVersion("latest"));
    }

    public VirtualMachine.DefinitionWithDataDisk withKnownImage(KnownVirtualMachineImage knownImage) {
        return withImage(knownImage.imageReference());
    }

    public VirtualMachine.DefinitionWithOSDiskAttached fromImage(ImageReference imageReference) {
        this.storageProfile.setImageReference(imageReference);
        this.storageProfile.osDisk().setCreateOption(DiskCreateOptionTypes.FROMIMAGE);
        return this;
    }

    public VirtualMachine.DefinitionWithOSDiskAttached fromLatestImage(String publisher, String offer, String sku) {
        return fromImage(new ImageReference()
                .setPublisher(publisher).setOffer(offer).setSku(sku).setVersion("latest"));
    }

    public VirtualMachine.DefinitionWithOSDiskAttached fromKnownImage(KnownVirtualMachineImage knownImage) {
        return fromImage(knownImage.imageReference());
    }

    public VirtualMachine.DefinitionWithOSDiskAttached storeVhdAt(String containerName, String vhdName) {
        this.storageProfile.osDisk().setVhd(toVhd(containerName, vhdName));
        return this;
    }

    public VirtualMachine.DefinitionWithDataDisk attach() {
        return definitionWithDataDisk;
    }

    public StorageProfile getStorageProfile() {
        // TODO: Setup default values in the storage profile.
        return storageProfile;
    }
}
