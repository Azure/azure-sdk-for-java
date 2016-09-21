package com.microsoft.azure.management.compute;

import com.microsoft.azure.management.compute.implementation.VirtualMachineExtensionImageInner;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;

/**
 * An immutable client-side representation of an Azure virtual machine extension image.
 * <p>
 * Note: Azure virtual machine extension image is also referred as virtual machine extension handler.
 */
public interface VirtualMachineExtensionImage extends
        Wrapper<VirtualMachineExtensionImageInner> {
    /**
     * @return the resource ID of the extension image
     */
    String id();

    /**
     * @return the region in which virtual machine extension image is available
     */
    String regionName();

    /**
     * @return the name of the publisher of the virtual machine extension image
     */
    String publisherName();

    /**
     * @return the name of the virtual machine extension image type this image belongs to
     */
    String typeName();

    /**
     * @return the name of the virtual machine extension image version this image represents
     */
    String versionName();

    /**
     * @return the operating system this virtual machine extension image supports
     */
    OperatingSystemTypes osType();

    /**
     * @return the type of role this virtual machine extension image supports
     */
    ComputeRoles computeRole();

    /**
     * @return the schema defined by publisher, where extension consumers should provide settings in a matching schema
     * <p>
     * Note this field will be null since server provide null for them
     */
    String handlerSchema();

    /**
     * @return true if the extension can be used on xRP Virtual Machine ScaleSets.
     * <p>
     * Note by default existing extensions are usable on scale sets, but there might be cases where a publisher wants to
     * explicitly indicate the extension is only enabled for Compute Resource Provider VMs but not Virtual Machine ScaleSets.
     */
    boolean vmScaleSetEnabled();

    /**
     * @return true if the handler can support multiple extensions.
     */
    boolean supportsMultipleExtensions();

    /**
     * @return the virtual machine extension image version this image belongs to
     */
    VirtualMachineExtensionImageVersion version();
}