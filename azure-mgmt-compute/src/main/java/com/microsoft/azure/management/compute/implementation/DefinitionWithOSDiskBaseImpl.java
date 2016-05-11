package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.implementation.api.*;

class DefinitionWithOSDiskBaseImpl implements
        VirtualMachine.DefinitionWithVMImage,
        VirtualMachine.NewOSDiskFromImage,
        VirtualMachine.DefinitionWithOSDiskConfiguration {
    protected final VirtualMachine.DefinitionWithDataDisk definitionWithDataDisk;
    protected StorageProfile storageProfile;
    private String storageAccountName;
    private String resourceNamePrefix;

    public DefinitionWithOSDiskBaseImpl(VirtualMachine.DefinitionWithDataDisk definitionWithDataDisk, String resourceNamePrefix) {
        this.definitionWithDataDisk = definitionWithDataDisk;
        this.resourceNamePrefix = resourceNamePrefix;
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

    @Override
    public VirtualMachine.DefinitionWithDataDisk withImage(ImageReference imageReference) {
        this.storageProfile.setImageReference(imageReference);
        this.storageProfile.osDisk().setCreateOption(DiskCreateOptionTypes.FROMIMAGE);
        return definitionWithDataDisk;
    }

    @Override
    public VirtualMachine.DefinitionWithDataDisk withLatestImage(String publisher, String offer, String sku) {
        return withImage(new ImageReference()
                .setPublisher(publisher).setOffer(offer).setSku(sku).setVersion("latest"));
    }

    @Override
    public VirtualMachine.DefinitionWithDataDisk withKnownImage(KnownVirtualMachineImage knownImage) {
        return withImage(knownImage.imageReference());
    }

    @Override
    public VirtualMachine.DefinitionWithOSDiskConfiguration fromImage(ImageReference imageReference) {
        this.storageProfile.setImageReference(imageReference);
        this.storageProfile.osDisk().setCreateOption(DiskCreateOptionTypes.FROMIMAGE);
        return this;
    }

    @Override
    public VirtualMachine.DefinitionWithOSDiskConfiguration fromLatestImage(String publisher, String offer, String sku) {
        return fromImage(new ImageReference()
                .setPublisher(publisher).setOffer(offer).setSku(sku).setVersion("latest"));
    }

    @Override
    public VirtualMachine.DefinitionWithOSDiskConfiguration fromKnownImage(KnownVirtualMachineImage knownImage) {
        return fromImage(knownImage.imageReference());
    }

    @Override
    public VirtualMachine.DefinitionWithOSDiskConfiguration withReadOnlyCaching() {
        this.storageProfile.osDisk().setCaching(CachingTypes.READONLY);
        return this;
    }

    @Override
    public VirtualMachine.DefinitionWithOSDiskConfiguration withReadWriteCaching() {
        this.storageProfile.osDisk().setCaching(CachingTypes.READWRITE);
        return this;
    }

    @Override
    public VirtualMachine.DefinitionWithOSDiskConfiguration withNoCaching() {
        this.storageProfile.osDisk().setCaching(CachingTypes.NONE);
        return this;
    }

    @Override
    public VirtualMachine.DefinitionWithOSDiskConfiguration withSize(Integer sizeInGB) {
        this.storageProfile.osDisk().setDiskSizeGB(sizeInGB);
        return this;
    }

    @Override
    public VirtualMachine.DefinitionWithOSDiskConfiguration storeVHDAt(String containerName, String vhdName) {
        this.storageProfile.osDisk().setVhd(toVhd(containerName, vhdName));
        return this;
    }

    @Override
    public VirtualMachine.DefinitionWithOSDiskConfiguration encryptionSettings(DiskEncryptionSettings settings) {
        this.storageProfile.osDisk().setEncryptionSettings(settings);
        return this;
    }

    @Override
    public VirtualMachine.DefinitionWithDataDisk attach() {
        return definitionWithDataDisk;
    }

    public StorageProfile getStorageProfile() {
        OSDisk osDisk = this.storageProfile.osDisk();
        if (osDisk.vhd() == null) {
            osDisk.setVhd(toVhd("vhds", generateRandomName() + ".vhd"));
        }

        if (osDisk.caching() == null) {
            osDisk.setCaching(CachingTypes.READWRITE);
        }

        if (osDisk.name() == null) {
            osDisk.setName(generateRandomName());
        }

        return storageProfile;
    }

    private String generateRandomName() {
        // TODO: Generate a Random name  this.resourceNamePrefix + "-" + {rand}
        return null;
    }
}
