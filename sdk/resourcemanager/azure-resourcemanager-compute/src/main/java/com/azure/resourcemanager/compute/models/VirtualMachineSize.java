// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.compute.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasName;

/** A type representing virtual machine size available for a subscription in a region. */
@Fluent
public interface VirtualMachineSize extends HasName {
    /** @return the number of cores supported by the VM size */
    int numberOfCores();

    /** @return the OS disk size allowed by the VM size */
    int osDiskSizeInMB();

    /** @return the resource disk size allowed by the VM size */
    int resourceDiskSizeInMB();

    /** @return the memory size supported by the VM size */
    int memoryInMB();

    /** @return the maximum number of data disks allowed by a VM size */
    int maxDataDiskCount();
}
