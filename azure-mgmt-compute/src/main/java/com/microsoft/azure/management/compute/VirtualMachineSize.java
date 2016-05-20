package com.microsoft.azure.management.compute;

/**
 * A type representing virtual machine size available for a subscription in a region.
 */
public interface VirtualMachineSize {
    /**
     * Gets the VM size name.
     */
    String name();

    /**
     * Gets the Number of cores supported by a VM size.
     */
    Integer numberOfCores();

    /**
     * Gets the OS disk size allowed by a VM size.
     */
    Integer osDiskSizeInMB();

    /**
     * Gets Resource disk size allowed by a VM size.
     */
    Integer resourceDiskSizeInMB();

    /**
     * Gets the Memory size supported by a VM size.
     */
    Integer memoryInMB();

    /**
     * Gets the Maximum number of data disks allowed by a VM size.
     */
    Integer maxDataDiskCount();
}
