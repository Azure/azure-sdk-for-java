/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.Page;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.compute.VirtualMachineImage;
import com.microsoft.azure.management.compute.VirtualMachineImagesInSku;
import com.microsoft.azure.management.compute.VirtualMachineSku;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;
import com.microsoft.rest.RestException;
import rx.Observable;
import rx.functions.Func1;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The implementation for {@link VirtualMachineImagesInSku}.
 */
@LangDefinition
class VirtualMachineImagesInSkuImpl implements VirtualMachineImagesInSku {

    private final VirtualMachineImagesInner innerCollection;
    private final VirtualMachineSku sku;

    VirtualMachineImagesInSkuImpl(VirtualMachineSku sku, VirtualMachineImagesInner innerCollection) {
        this.sku = sku;
        this.innerCollection = innerCollection;
    }

    public PagedList<VirtualMachineImage> list() {
        final List<VirtualMachineImage> images = new ArrayList<>();
        for (VirtualMachineImageResourceInner inner
                : innerCollection.list(
                this.sku.region().toString(),
                this.sku.publisher().name(),
                this.sku.offer().name(),
                this.sku.name())) {
            String version = inner.name();
            images.add(new VirtualMachineImageImpl(
                    this.sku.region(),
                    this.sku.publisher().name(),
                    this.sku.offer().name(),
                    this.sku.name(),
                    version,
                    innerCollection.get(this.sku.region().toString(),
                            this.sku.publisher().name(),
                            this.sku.offer().name(),
                            this.sku.name(),
                            version)));
        }
        Page<VirtualMachineImage> page = new Page<VirtualMachineImage>() {
            @Override
            public String nextPageLink() {
                return null;
            }

            @Override
            public List<VirtualMachineImage> items() {
                return images;
            }
        };
        return new PagedList<VirtualMachineImage>(page) {
            @Override
            public Page<VirtualMachineImage> nextPage(String nextPageLink) throws RestException, IOException {
                return null;
            }
        };
    }

    @Override
    public Observable<VirtualMachineImage> listAsync() {
        final VirtualMachineImagesInSkuImpl self = this;
        return ReadableWrappersImpl.convertListToInnerAsync(innerCollection.listAsync(this.sku.region().toString(),
                this.sku.publisher().name(),
                this.sku.offer().name(),
                this.sku.name())).flatMap(new Func1<VirtualMachineImageResourceInner, Observable<VirtualMachineImage>>() {
            @Override
            public Observable<VirtualMachineImage> call(final VirtualMachineImageResourceInner virtualMachineImageResourceInner) {
                return innerCollection.getAsync(self.sku.region().toString(),
                        self.sku.publisher().name(),
                        self.sku.offer().name(),
                        self.sku.name(),
                        virtualMachineImageResourceInner.name()).map(new Func1<VirtualMachineImageInner, VirtualMachineImage>() {
                    @Override
                    public VirtualMachineImage call(VirtualMachineImageInner virtualMachineImageInner) {
                        return new VirtualMachineImageImpl(
                                self.sku.region(),
                                self.sku.publisher().name(),
                                self.sku.offer().name(),
                                self.sku.name(),
                                virtualMachineImageResourceInner.name(),
                                virtualMachineImageInner);
                    }
                });
            }
        });
    }
}
