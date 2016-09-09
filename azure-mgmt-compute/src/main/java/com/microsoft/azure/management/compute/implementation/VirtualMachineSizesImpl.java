/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.compute.VirtualMachineSize;
import com.microsoft.azure.management.compute.VirtualMachineSizes;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;

import java.io.IOException;

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
    public PagedList<VirtualMachineSize> listByRegion(Region region) throws CloudException, IOException {
        return listByRegion(region.toString());
    }

    @Override
    protected VirtualMachineSizeImpl wrapModel(VirtualMachineSizeInner inner) {
        return new VirtualMachineSizeImpl(inner);
    }

    @Override
    public PagedList<VirtualMachineSize> listByRegion(String regionName) throws CloudException, IOException {
        return wrapList(innerCollection.list(regionName));
    }
}
