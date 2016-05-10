package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.SubResource;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.implementation.api.*;
import com.microsoft.azure.management.resources.ResourceGroups;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;

import java.util.List;

class VirtualMachineImpl
        extends GroupableResourceImpl<VirtualMachine, VirtualMachineInner, VirtualMachineImpl>
        implements
            VirtualMachine,
            VirtualMachine.DefinitionBlank,
            VirtualMachine.DefinitionWithRegion,
            VirtualMachine.DefinitionWithGroup,
            VirtualMachine.DefinitionWithImage {
    private final VirtualMachinesInner client;

    VirtualMachineImpl(String name, VirtualMachineInner innerModel, VirtualMachinesInner client, ResourceGroups resourceGroups) {
        super(name, innerModel, resourceGroups);
        this.client = client;
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
    public DefinitionWithImage withImage(ImageReference imageReference) {
        if (inner().storageProfile() == null) {
            inner().setStorageProfile(new StorageProfile());
        }
        inner().storageProfile().setImageReference(imageReference);
        return this;
    }

    @Override
    public DefinitionWithImage withLatestImage(String publisher, String offer, String sku) {
        return this.withImage(new ImageReference()
                .setPublisher(publisher).setOffer(offer).setSku(sku).setVersion("latest"));
    }

    @Override
    public DefinitionWithImage withKnownImage(KnownVirtualMachineImage knownImage) {
        return this.withImage(knownImage.imageReference());
    }

    @Override
    public VirtualMachine refresh() throws Exception {
        return this;
    }
}
