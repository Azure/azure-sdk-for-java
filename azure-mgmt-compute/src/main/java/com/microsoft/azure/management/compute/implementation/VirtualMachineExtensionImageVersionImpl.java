package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.apigeneration.LangMethodDefinition;
import com.microsoft.azure.management.apigeneration.Method;
import com.microsoft.azure.management.compute.VirtualMachineExtensionImage;
import com.microsoft.azure.management.compute.VirtualMachineExtensionImageType;
import com.microsoft.azure.management.compute.VirtualMachineExtensionImageVersion;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.WrapperImpl;

/**
 * The implementation for {@link VirtualMachineExtensionImageVersion}.
 */
@LangDefinition
class VirtualMachineExtensionImageVersionImpl
        extends WrapperImpl<VirtualMachineExtensionImageInner>
        implements VirtualMachineExtensionImageVersion {
    private final VirtualMachineExtensionImagesInner client;
    private final VirtualMachineExtensionImageType type;

    VirtualMachineExtensionImageVersionImpl(VirtualMachineExtensionImagesInner client,
                                            VirtualMachineExtensionImageType extensionImageType,
                                            VirtualMachineExtensionImageInner inner) {
        super(inner);
        this.client = client;
        this.type = extensionImageType;
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
    @LangMethodDefinition(AsType =  LangMethodDefinition.LangMethodType.Property) // By default emitted as method adding this to emit Property
    public VirtualMachineExtensionImageType type() {
        return this.type;
    }

    @Override
    @Method
    public VirtualMachineExtensionImage image() {
        VirtualMachineExtensionImageInner inner = this.client.get(this.regionName(),
                this.type().publisher().name(),
                this.type().name(),
                this.name());
        return new VirtualMachineExtensionImageImpl(this, inner);
    }
}