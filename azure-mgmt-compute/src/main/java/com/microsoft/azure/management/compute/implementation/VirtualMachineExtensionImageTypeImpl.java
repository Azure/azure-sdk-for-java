package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.apigeneration.LangMethodDefinition;
import com.microsoft.azure.management.compute.VirtualMachineExtensionImageType;
import com.microsoft.azure.management.compute.VirtualMachineExtensionImageVersions;
import com.microsoft.azure.management.compute.VirtualMachinePublisher;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.WrapperImpl;

/**
 * The implementation for {@link VirtualMachineExtensionImageType}.
 */
@LangDefinition
class VirtualMachineExtensionImageTypeImpl
        extends WrapperImpl<VirtualMachineExtensionImageInner>
        implements VirtualMachineExtensionImageType {
    private final VirtualMachineExtensionImagesInner client;
    private final VirtualMachinePublisher publisher;

    VirtualMachineExtensionImageTypeImpl(VirtualMachineExtensionImagesInner client,
                                         VirtualMachinePublisher publisher,
                                         VirtualMachineExtensionImageInner inner) {
        super(inner);
        this.client = client;
        this.publisher = publisher;
    }

    @Override
    public String id() {
        return this.inner().id();
    }

    @Override
    public String name() {
        return this.inner().name();
    }

    @Override
    public String regionName() {
        return this.inner().location();
    }

    @Override
    @LangMethodDefinition(AsType =  LangMethodDefinition.LangMethodType.Property) // By default emitted as method adding this to emit as Property
    public VirtualMachinePublisher publisher() {
        return this.publisher;
    }

    @Override
    @LangMethodDefinition(AsType =  LangMethodDefinition.LangMethodType.Property) // By default emitted as method adding this to emit Property
    public VirtualMachineExtensionImageVersions versions() {
        return new VirtualMachineExtensionImageVersionsImpl(this.client, this);
    }
}