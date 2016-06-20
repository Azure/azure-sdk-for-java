/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.compute;

/**
 * A type representing virtual machine size available for a subscription in a region.
 */
public interface VirtualMachineSize {
    /**
     * @return the VM size name
     */
    String name();

    /**
     * @return the Number of cores supported by a VM size
     */
    int numberOfCores();

    /**
     * @return the OS disk size allowed by a VM size
     */
    int osDiskSizeInMB();

    /**
     * @return resource disk size allowed by a VM size
     */
    int resourceDiskSizeInMB();

    /**
     * @return the memory size supported by a VM size
     */
    int memoryInMB();

    /**
     * @return the maximum number of data disks allowed by a VM size
     */
    int maxDataDiskCount();
}
