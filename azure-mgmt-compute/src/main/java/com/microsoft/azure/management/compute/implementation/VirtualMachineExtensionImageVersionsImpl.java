package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.compute.VirtualMachineExtensionImageType;
import com.microsoft.azure.management.compute.VirtualMachineExtensionImageVersion;
import com.microsoft.azure.management.compute.VirtualMachineExtensionImageVersions;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;

/**
 * The implementation for {@link VirtualMachineExtensionImageVersions}.
 */
public class VirtualMachineExtensionImageVersionsImpl
        extends ReadableWrappersImpl<VirtualMachineExtensionImageVersion, VirtualMachineExtensionImageVersionImpl, VirtualMachineExtensionImageInner>
        implements VirtualMachineExtensionImageVersions {
    private final VirtualMachineExtensionImagesInner client;
    private final VirtualMachineExtensionImageType type;

    VirtualMachineExtensionImageVersionsImpl(VirtualMachineExtensionImagesInner client, VirtualMachineExtensionImageType type) {
        this.client = client;
        this.type = type;
    }

    @Override
    public PagedList<VirtualMachineExtensionImageVersion> list() {
        return wrapList(this.client.listVersions(this.type.regionName(),
                this.type.publisher().name(),
                this.type.name()));
    }

    @Override
    protected VirtualMachineExtensionImageVersionImpl wrapModel(VirtualMachineExtensionImageInner inner) {
        return new VirtualMachineExtensionImageVersionImpl(this.client, this.type, inner);
    }
}