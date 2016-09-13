package com.microsoft.azure.management.compute;

import com.microsoft.azure.management.compute.implementation.VirtualMachineExtensionImageInner;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;

/**
 * An immutable client-side representation of an Azure virtual machine extension image type.
 */
public interface VirtualMachineExtensionImageType extends
        Wrapper<VirtualMachineExtensionImageInner> {
    /**
     * @return the resource ID of the virtual machine extension image type
     */
    String id();

    /**
     * @return the name of the virtual machine extension image type
     */
    String name();

    /**
     * @return the region in which virtual machine extension image type is available
     */
    String regionName();

    /**
     * @return the publisher of this virtual machine extension image type
     */
    VirtualMachinePublisher publisher();

    /**
     * @return Virtual machine image extension versions available in this type
     */
    VirtualMachineExtensionImageVersions versions();
}

