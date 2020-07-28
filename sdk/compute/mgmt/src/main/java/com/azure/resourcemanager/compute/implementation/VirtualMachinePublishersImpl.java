// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.compute.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.compute.models.VirtualMachinePublisher;
import com.azure.resourcemanager.compute.models.VirtualMachinePublishers;
import com.azure.resourcemanager.compute.fluent.VirtualMachineExtensionImagesClient;
import com.azure.resourcemanager.compute.fluent.inner.VirtualMachineImageResourceInner;
import com.azure.resourcemanager.compute.fluent.VirtualMachineImagesClient;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.PagedConverter;

/** The implementation for {@link VirtualMachinePublishers}. */
public class VirtualMachinePublishersImpl
    extends ReadableWrappersImpl<VirtualMachinePublisher, VirtualMachinePublisherImpl, VirtualMachineImageResourceInner>
    implements VirtualMachinePublishers {

    private final VirtualMachineImagesClient imagesClientCollection;
    private final VirtualMachineExtensionImagesClient extensionsInnerCollection;

    public VirtualMachinePublishersImpl(
        VirtualMachineImagesClient imagesClientCollection,
        VirtualMachineExtensionImagesClient extensionsInnerCollection) {
        this.imagesClientCollection = imagesClientCollection;
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
        return new VirtualMachinePublisherImpl(
            Region.fromName(inner.location()),
            inner.name(),
            this.imagesClientCollection,
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
        return PagedConverter
            .convertListToPagedFlux(imagesClientCollection.listPublishersAsync(regionName))
            .mapPage(this::wrapModel);
    }
}
