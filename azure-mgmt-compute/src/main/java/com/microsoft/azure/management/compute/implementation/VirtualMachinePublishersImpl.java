/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.compute.VirtualMachinePublisher;
import com.microsoft.azure.management.compute.VirtualMachinePublishers;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;

import java.io.IOException;

/**
 * The implementation for {@link VirtualMachinePublishers}.
 */
class VirtualMachinePublishersImpl
        extends ReadableWrappersImpl<VirtualMachinePublisher, VirtualMachinePublisherImpl, VirtualMachineImageResourceInner>
        implements VirtualMachinePublishers {

    private final VirtualMachineImagesInner imagesInnerCollection;
    private final VirtualMachineExtensionImagesInner extensionsInnerCollection;

    VirtualMachinePublishersImpl(VirtualMachineImagesInner imagesInnerCollection, VirtualMachineExtensionImagesInner extensionsInnerCollection) {
        this.imagesInnerCollection = imagesInnerCollection;
        this.extensionsInnerCollection = extensionsInnerCollection;
    }

    @Override
    public PagedList<VirtualMachinePublisher> listByRegion(Region region) throws CloudException, IOException {
        return listByRegion(region.toString());
    }

    @Override
    protected VirtualMachinePublisherImpl wrapModel(VirtualMachineImageResourceInner inner) {
        return new VirtualMachinePublisherImpl(Region.fromName(inner.location()),
                inner.name(),
                this.imagesInnerCollection,
                this.extensionsInnerCollection);
    }

    @Override
    public PagedList<VirtualMachinePublisher> listByRegion(String regionName) throws CloudException, IOException {
        return wrapList(imagesInnerCollection.listPublishers(regionName));
    }
}