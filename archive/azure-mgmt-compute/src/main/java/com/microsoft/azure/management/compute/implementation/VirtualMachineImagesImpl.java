/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.compute.VirtualMachineImage;
import com.microsoft.azure.management.compute.VirtualMachineImages;
import com.microsoft.azure.management.compute.VirtualMachineOffer;
import com.microsoft.azure.management.compute.VirtualMachinePublisher;
import com.microsoft.azure.management.compute.VirtualMachinePublishers;
import com.microsoft.azure.management.compute.VirtualMachineSku;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import rx.Observable;
import rx.functions.Func1;

import java.util.List;

/**
 * The implementation for {@link VirtualMachineImages}.
 */
@LangDefinition
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
    public PagedList<VirtualMachineImage> listByRegion(Region location) {
        return listByRegion(location.toString());
    }

    @Override
    public PagedList<VirtualMachineImage> listByRegion(String regionName) {
        PagedList<VirtualMachinePublisher> publishers = this.publishers().listByRegion(regionName);

        PagedList<VirtualMachineOffer> offers =
                new ChildListFlattener<>(publishers, new ChildListFlattener.ChildListLoader<VirtualMachinePublisher, VirtualMachineOffer>() {
                    @Override
                    public PagedList<VirtualMachineOffer> loadList(VirtualMachinePublisher publisher)  {
                        return publisher.offers().list();
                    }
                }).flatten();

        PagedList<VirtualMachineSku> skus =
                new ChildListFlattener<>(offers, new ChildListFlattener.ChildListLoader<VirtualMachineOffer, VirtualMachineSku>() {
                    @Override
                    public PagedList<VirtualMachineSku> loadList(VirtualMachineOffer offer)  {
                        return offer.skus().list();
                    }
                }).flatten();

        PagedList<VirtualMachineImage> images =
                new ChildListFlattener<>(skus, new ChildListFlattener.ChildListLoader<VirtualMachineSku, VirtualMachineImage>() {
                    @Override
                    public PagedList<VirtualMachineImage> loadList(VirtualMachineSku sku)  {
                        return sku.images().list();
                    }
                }).flatten();

        return images;
    }

    @Override
    public Observable<VirtualMachineImage> listByRegionAsync(Region region) {
        return listByRegionAsync(region.name());
    }

    @Override
    public Observable<VirtualMachineImage> listByRegionAsync(String regionName) {
        return this.publishers().listByRegionAsync(regionName)
                .flatMap(new Func1<VirtualMachinePublisher, Observable<VirtualMachineOffer>>() {
                    @Override
                    public Observable<VirtualMachineOffer> call(VirtualMachinePublisher virtualMachinePublisher) {
                        return virtualMachinePublisher.offers().listAsync();
                    }
                }).flatMap(new Func1<VirtualMachineOffer, Observable<VirtualMachineSku>>() {
                    @Override
                    public Observable<VirtualMachineSku> call(VirtualMachineOffer virtualMachineExtensionImageType) {
                        return virtualMachineExtensionImageType.skus().listAsync();
                    }
                }).flatMap(new Func1<VirtualMachineSku, Observable<VirtualMachineImage>>() {
                    @Override
                    public Observable<VirtualMachineImage> call(VirtualMachineSku virtualMachineSku) {
                        return virtualMachineSku.images().listAsync();
                    }
                });
    }

    @Override
    public VirtualMachinePublishers publishers() {
        return this.publishers;
    }

}