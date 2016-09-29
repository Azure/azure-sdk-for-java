/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.management.compute.VirtualMachineSize;

/**
 * The implementation for {@link VirtualMachineSize}.
 */
class VirtualMachineSizeImpl implements VirtualMachineSize {
    private VirtualMachineSizeInner innerModel;

    VirtualMachineSizeImpl(VirtualMachineSizeInner innerModel) {
        this.innerModel = innerModel;
    }

    @Override
    public String name() {
        return innerModel.name();
    }

    @Override
    public int numberOfCores() {
        return innerModel.numberOfCores();
    }

    @Override
    public int osDiskSizeInMB() {
        return innerModel.osDiskSizeInMB();
    }

    @Override
    public int resourceDiskSizeInMB() {
        return innerModel.resourceDiskSizeInMB();
    }

    @Override
    public int memoryInMB() {
        return innerModel.memoryInMB();
    }

    @Override
    public int maxDataDiskCount() {
        return innerModel.maxDataDiskCount();
    }
}
