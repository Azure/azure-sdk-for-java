// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.compute.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.exception.ManagementException;
import com.azure.resourcemanager.compute.models.VirtualMachineExtensionImage;
import com.azure.resourcemanager.compute.models.VirtualMachineExtensionImageVersion;
import com.azure.resourcemanager.compute.models.VirtualMachineExtensionImages;
import com.azure.resourcemanager.compute.models.VirtualMachinePublishers;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.utils.PagedConverter;
import reactor.core.publisher.Flux;

/** The implementation for {@link VirtualMachineExtensionImages}. */
public class VirtualMachineExtensionImagesImpl implements VirtualMachineExtensionImages {
    private final VirtualMachinePublishers publishers;

    public VirtualMachineExtensionImagesImpl(VirtualMachinePublishers publishers) {
        this.publishers = publishers;
    }

    @Override
    public PagedIterable<VirtualMachineExtensionImage> listByRegion(Region region) {
        return listByRegion(region.toString());
    }

    @Override
    public PagedIterable<VirtualMachineExtensionImage> listByRegion(String regionName) {
        return new PagedIterable<>(listByRegionAsync(regionName));
    }

    @Override
    public PagedFlux<VirtualMachineExtensionImage> listByRegionAsync(Region region) {
        return listByRegionAsync(region.name());
    }

    @Override
    public PagedFlux<VirtualMachineExtensionImage> listByRegionAsync(String regionName) {
        return PagedConverter
            .flatMapPage(
                publishers.listByRegionAsync(regionName),
                virtualMachinePublisher ->
                    virtualMachinePublisher
                        .extensionTypes()
                        .listAsync()
                        .onErrorResume(ManagementException.class,
                            e -> e.getResponse().getStatusCode() == 404 ? Flux.empty() : Flux.error(e))
                        .flatMap(virtualMachineExtensionImageType ->
                            virtualMachineExtensionImageType.versions().listAsync())
                        .flatMap(VirtualMachineExtensionImageVersion::getImageAsync));
    }

    @Override
    public VirtualMachinePublishers publishers() {
        return this.publishers;
    }
}
