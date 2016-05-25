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
    Integer numberOfCores();

    /**
     * @return the OS disk size allowed by a VM size
     */
    Integer osDiskSizeInMB();

    /**
     * @return Resource disk size allowed by a VM size
     */
    Integer resourceDiskSizeInMB();

    /**
     * @return the Memory size supported by a VM size
     */
    Integer memoryInMB();

    /**
     * @return the Maximum number of data disks allowed by a VM size
     */
    Integer maxDataDiskCount();
}
