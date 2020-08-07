// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.compute.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.compute.models.VirtualMachineImage;
import com.azure.resourcemanager.compute.models.VirtualMachineImagesInSku;
import com.azure.resourcemanager.compute.models.VirtualMachineSku;
import com.azure.resourcemanager.compute.fluent.VirtualMachineImagesClient;
import com.azure.resourcemanager.resources.fluentcore.utils.PagedConverter;
import reactor.core.publisher.Flux;

/** The implementation for {@link VirtualMachineImagesInSku}. */
class VirtualMachineImagesInSkuImpl implements VirtualMachineImagesInSku {

    private final VirtualMachineImagesClient innerCollection;
    private final VirtualMachineSku sku;

    VirtualMachineImagesInSkuImpl(VirtualMachineSku sku, VirtualMachineImagesClient innerCollection) {
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
        return PagedConverter
            .convertListToPagedFlux(
                innerCollection
                    .listAsync(sku.region().toString(), sku.publisher().name(), sku.offer().name(), sku.name())
                    .flatMapMany(Flux::fromIterable)
                    .flatMap(
                        resourceInner ->
                            innerCollection
                                .getAsync(
                                    self.sku.region().toString(),
                                    self.sku.publisher().name(),
                                    self.sku.offer().name(),
                                    self.sku.name(),
                                    resourceInner.name())
                                .map(
                                    imageInner ->
                                        (VirtualMachineImage)
                                            new VirtualMachineImageImpl(
                                                self.sku.region(),
                                                self.sku.publisher().name(),
                                                self.sku.offer().name(),
                                                self.sku.name(),
                                                resourceInner.name(),
                                                imageInner)))
                    .collectList());
    }
}
