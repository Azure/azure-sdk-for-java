package com.microsoft.azure.management.compute;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.compute.implementation.VirtualMachineExtensionImageInner;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;

import java.io.IOException;

/**
 * An immutable client-side representation of an Azure virtual machine extension image version.
 */
public interface VirtualMachineExtensionImageVersion extends
        Wrapper<VirtualMachineExtensionImageInner> {
    /**
     * @return the resource ID of the extension image version
     */
    String id();

    /**
     * @return the name of the virtual machine extension image version
     */
    String name();

    /**
     * @return the region in which virtual machine extension image version is available
     */
    String regionName();

    /**
     * @return the virtual machine extension image type this version belongs to
     */
    VirtualMachineExtensionImageType type();

    /**
     * @return virtual machine extension image this version represents
     *
     * @throws CloudException thrown for an invalid response from the service
     * @throws IOException exception thrown from serialization/deserialization
     */
    VirtualMachineExtensionImage image() throws CloudException, IOException;
}