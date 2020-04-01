/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.compute.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.management.compute.models.VirtualMachineImagesInner;
import com.azure.management.compute.VirtualMachineImage;
import com.azure.management.compute.VirtualMachineImagesInSku;
import com.azure.management.compute.VirtualMachineSku;
import com.azure.management.resources.fluentcore.utils.PagedConverter;
import reactor.core.publisher.Flux;

/**
 * The implementation for {@link VirtualMachineImagesInSku}.
 */
class VirtualMachineImagesInSkuImpl implements VirtualMachineImagesInSku {

    private final VirtualMachineImagesInner innerCollection;
    private final VirtualMachineSku sku;

    VirtualMachineImagesInSkuImpl(VirtualMachineSku sku, VirtualMachineImagesInner innerCollection) {
        this.sku = sku;
        this.innerCollection = innerCollection;
    }

    @Override
    public PagedIterable<VirtualMachineImage> list() {
        return new PagedIterable<>(listAsync());
    }

    @Override
    public PagedFlux<VirtualMachineImage> listAsync() {
        final VirtualMachineImagesInSkuImpl self = this;
        return PagedConverter.convertListToPagedFlux(innerCollection.listAsync(sku.region().toString(),
                sku.publisher().name(),
                sku.offer().name(),
                sku.name())
                .flatMapMany(Flux::fromIterable)
                .flatMap(resourceInner -> innerCollection.getAsync(self.sku.region().toString(),
                        self.sku.publisher().name(),
                        self.sku.offer().name(),
                        self.sku.name(),
                        resourceInner.name())
                        .map(imageInner -> (VirtualMachineImage)new VirtualMachineImageImpl(self.sku.region(),
                                self.sku.publisher().name(),
                                self.sku.offer().name(),
                                self.sku.name(),
                                resourceInner.name(),
                                imageInner))
                )
                .collectList());
    }
}
