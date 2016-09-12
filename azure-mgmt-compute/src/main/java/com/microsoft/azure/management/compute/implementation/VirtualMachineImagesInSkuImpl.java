package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.Page;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.compute.VirtualMachineImage;
import com.microsoft.azure.management.compute.VirtualMachineImagesInSku;
import com.microsoft.azure.management.compute.VirtualMachineSku;
import com.microsoft.rest.RestException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

    public PagedList<VirtualMachineImage> list() throws CloudException, IOException {
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
             public String getNextPageLink() {
                 return null;
             }

            @Override
            public List<VirtualMachineImage> getItems() {
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
}
