/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.compute.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.management.compute.models.VirtualMachineSizeInner;
import com.azure.management.compute.models.VirtualMachineSizesInner;
import com.azure.management.compute.VirtualMachineSize;
import com.azure.management.compute.VirtualMachineSizes;
import com.azure.management.resources.fluentcore.arm.Region;
import com.azure.management.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;

/**
 * The implementation for {@link VirtualMachineSizes}.
 */
class VirtualMachineSizesImpl
        extends ReadableWrappersImpl<VirtualMachineSize, VirtualMachineSizeImpl, VirtualMachineSizeInner>
        implements VirtualMachineSizes {
    private final VirtualMachineSizesInner innerCollection;

    VirtualMachineSizesImpl(VirtualMachineSizesInner innerCollection) {
        this.innerCollection = innerCollection;
    }

    @Override
    public PagedIterable<VirtualMachineSize> listByRegion(Region region) {
        return listByRegion(region.toString());
    }

    @Override
    protected VirtualMachineSizeImpl wrapModel(VirtualMachineSizeInner inner) {
        if (inner == null) {
            return null;
        }
        return new VirtualMachineSizeImpl(inner);
    }

    @Override
    public PagedIterable<VirtualMachineSize> listByRegion(String regionName) {
        return wrapList(innerCollection.list(regionName));
    }

    @Override
    public PagedFlux<VirtualMachineSize> listByRegionAsync(Region region) {
        return listByRegionAsync(region.name());
    }

    @Override
    public PagedFlux<VirtualMachineSize> listByRegionAsync(String regionName) {
        return innerCollection.listAsync(regionName)
                .mapPage(VirtualMachineSizeImpl::new);
    }
}
