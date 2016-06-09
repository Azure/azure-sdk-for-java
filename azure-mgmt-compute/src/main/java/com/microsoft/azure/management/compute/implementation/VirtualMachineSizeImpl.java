package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.management.compute.VirtualMachineSize;
import com.microsoft.azure.management.compute.implementation.api.VirtualMachineSizeInner;

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
    public Integer numberOfCores() {
        return innerModel.numberOfCores();
    }

    @Override
    public Integer osDiskSizeInMB() {
        return innerModel.osDiskSizeInMB();
    }

    @Override
    public Integer resourceDiskSizeInMB() {
        return innerModel.resourceDiskSizeInMB();
    }

    @Override
    public Integer memoryInMB() {
        return innerModel.memoryInMB();
    }

    @Override
    public Integer maxDataDiskCount() {
        return innerModel.maxDataDiskCount();
    }
}
