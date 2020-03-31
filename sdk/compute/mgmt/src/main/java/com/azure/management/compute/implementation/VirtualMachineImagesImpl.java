/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.compute.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.management.compute.models.VirtualMachineImageInner;
import com.azure.management.compute.models.VirtualMachineImageResourceInner;
import com.azure.management.compute.models.VirtualMachineImagesInner;
import com.azure.management.compute.VirtualMachineImage;
import com.azure.management.compute.VirtualMachineImages;
import com.azure.management.compute.VirtualMachinePublishers;
import com.azure.management.resources.fluentcore.arm.Region;
import com.azure.management.resources.fluentcore.utils.PagedConverter;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * The implementation for {@link VirtualMachineImages}.
 */
class VirtualMachineImagesImpl
        implements VirtualMachineImages {
    private final VirtualMachinePublishers publishers;
    private final VirtualMachineImagesInner client;

    VirtualMachineImagesImpl(VirtualMachinePublishers publishers, VirtualMachineImagesInner client) {
        this.publishers = publishers;
        this.client = client;
    }

    @Override
    public VirtualMachineImage getImage(Region region, String publisherName, String offerName, String skuName, String version) {
        if (version.equalsIgnoreCase("latest")) {
            List<VirtualMachineImageResourceInner> innerImages = this.client.list(region.name(), publisherName, offerName, skuName, null, 1, "name desc");
            if (innerImages != null && !innerImages.isEmpty()) {
                VirtualMachineImageResourceInner innerImageResource = innerImages.get(0);
                version = innerImageResource.name();
            }
        }
        VirtualMachineImageInner innerImage = this.client.get(region.name(),
                publisherName,
                offerName,
                skuName,
                version);
        return (innerImage != null) ? new VirtualMachineImageImpl(region, publisherName, offerName, skuName, version, innerImage) : null;
    }

  @Override
  public VirtualMachineImage getImage(String region, String publisherName, String offerName, String skuName, String version) {
      if (version.equalsIgnoreCase("latest")) {
          List<VirtualMachineImageResourceInner> innerImages = this.client.list(region, publisherName, offerName, skuName, null, 1, "name desc");
          if (innerImages != null && !innerImages.isEmpty()) {
              VirtualMachineImageResourceInner innerImageResource = innerImages.get(0);
              version = innerImageResource.name();
          }
      }
      VirtualMachineImageInner innerImage = this.client.get(region,
              publisherName,
              offerName,
              skuName,
              version);
      return (innerImage != null) ? new VirtualMachineImageImpl(Region.fromName(region), publisherName, offerName, skuName, version, innerImage) : null;
  }

  @Override
    public PagedIterable<VirtualMachineImage> listByRegion(Region location) {
        return listByRegion(location.toString());
    }

    @Override
    public PagedIterable<VirtualMachineImage> listByRegion(String regionName) {
        return new PagedIterable<>(listByRegionAsync(regionName));
    }

    @Override
    public PagedFlux<VirtualMachineImage> listByRegionAsync(Region region) {
        return listByRegionAsync(region.name());
    }

    @Override
    public PagedFlux<VirtualMachineImage> listByRegionAsync(String regionName) {
        return PagedConverter.flatMapPage(publishers().listByRegionAsync(regionName), virtualMachinePublisher -> virtualMachinePublisher.offers().listAsync()
                .onErrorResume(e -> Mono.empty())
                .flatMap(virtualMachineOffer -> virtualMachineOffer.skus().listAsync())
                .flatMap(virtualMachineSku -> virtualMachineSku.images().listAsync()));
    }

    @Override
    public VirtualMachinePublishers publishers() {
        return this.publishers;
    }

}