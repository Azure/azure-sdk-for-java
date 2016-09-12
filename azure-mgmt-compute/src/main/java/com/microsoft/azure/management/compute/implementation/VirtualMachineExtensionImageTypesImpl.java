package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.compute.VirtualMachineExtensionImageType;
import com.microsoft.azure.management.compute.VirtualMachineExtensionImageTypes;
import com.microsoft.azure.management.compute.VirtualMachinePublisher;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;

import java.io.IOException;

/**
 * The implementation for {@link VirtualMachineExtensionImageTypes}.
 */
class VirtualMachineExtensionImageTypesImpl
        extends ReadableWrappersImpl<VirtualMachineExtensionImageType, VirtualMachineExtensionImageTypeImpl, VirtualMachineExtensionImageInner>
        implements VirtualMachineExtensionImageTypes {
    private final VirtualMachineExtensionImagesInner client;
    private final VirtualMachinePublisher publisher;

    VirtualMachineExtensionImageTypesImpl(VirtualMachineExtensionImagesInner client, VirtualMachinePublisher publisher) {
        this.client = client;
        this.publisher = publisher;
    }

    @Override
    public PagedList<VirtualMachineExtensionImageType> list() throws CloudException, IOException {
        return wrapList(this.client.listTypes(this.publisher.region().toString(), this.publisher.name()));
    }

    @Override
    protected VirtualMachineExtensionImageTypeImpl wrapModel(VirtualMachineExtensionImageInner inner) {
        return new VirtualMachineExtensionImageTypeImpl(this.client, this.publisher, inner);
    }
}