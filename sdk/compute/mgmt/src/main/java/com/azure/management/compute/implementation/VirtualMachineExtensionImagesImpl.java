/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.compute.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.management.compute.VirtualMachineExtensionImage;
import com.azure.management.compute.VirtualMachineExtensionImageVersion;
import com.azure.management.compute.VirtualMachineExtensionImages;
import com.azure.management.compute.VirtualMachinePublishers;
import com.azure.management.resources.fluentcore.arm.Region;
import com.azure.management.resources.fluentcore.utils.PagedConverter;
import reactor.core.publisher.Mono;

/**
 * The implementation for {@link VirtualMachineExtensionImages}.
 */
class VirtualMachineExtensionImagesImpl
        implements VirtualMachineExtensionImages {
    private final VirtualMachinePublishers publishers;

    VirtualMachineExtensionImagesImpl(VirtualMachinePublishers publishers) {
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
        return PagedConverter.flatMapPage(publishers.listByRegionAsync(regionName), virtualMachinePublisher -> virtualMachinePublisher.extensionTypes().listAsync()
                .onErrorResume(e -> Mono.empty())
                .flatMap(virtualMachineExtensionImageType -> virtualMachineExtensionImageType.versions().listAsync())
                .flatMap(VirtualMachineExtensionImageVersion::getImageAsync));
    }

    @Override
    public VirtualMachinePublishers publishers() {
        return this.publishers;
    }
}