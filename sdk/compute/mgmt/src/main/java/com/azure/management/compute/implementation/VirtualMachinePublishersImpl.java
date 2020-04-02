/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.compute.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.management.compute.models.VirtualMachineExtensionImagesInner;
import com.azure.management.compute.models.VirtualMachineImageResourceInner;
import com.azure.management.compute.models.VirtualMachineImagesInner;
import com.azure.management.compute.VirtualMachinePublisher;
import com.azure.management.compute.VirtualMachinePublishers;
import com.azure.management.resources.fluentcore.arm.Region;
import com.azure.management.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;
import com.azure.management.resources.fluentcore.utils.PagedConverter;

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
    public PagedIterable<VirtualMachinePublisher> listByRegion(Region region) {
        return listByRegion(region.toString());
    }

    @Override
    protected VirtualMachinePublisherImpl wrapModel(VirtualMachineImageResourceInner inner) {
        if (inner == null) {
            return null;
        }
        return new VirtualMachinePublisherImpl(Region.fromName(inner.location()),
                inner.name(),
                this.imagesInnerCollection,
                this.extensionsInnerCollection);
    }

    @Override
    public PagedIterable<VirtualMachinePublisher> listByRegion(String regionName) {
        return new PagedIterable<>(listByRegionAsync(regionName));
    }

    @Override
    public PagedFlux<VirtualMachinePublisher> listByRegionAsync(Region region) {
        return listByRegionAsync(region.name());
    }

    @Override
    public PagedFlux<VirtualMachinePublisher> listByRegionAsync(String regionName) {
        return PagedConverter.convertListToPagedFlux(imagesInnerCollection.listPublishersAsync(regionName))
                .mapPage(this::wrapModel);
    }
}