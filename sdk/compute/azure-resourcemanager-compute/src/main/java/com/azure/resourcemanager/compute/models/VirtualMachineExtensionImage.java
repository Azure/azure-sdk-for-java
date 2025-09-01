// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.compute.fluent.models.VirtualMachineExtensionImageInner;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;

/**
 * An immutable client-side representation of an Azure virtual machine extension image.
 *
 * <p>Note: Azure virtual machine extension image is also referred as virtual machine extension handler.
 */
@Fluent
public interface VirtualMachineExtensionImage extends HasInnerModel<VirtualMachineExtensionImageInner> {
    /**
     * Gets the resource ID of the extension image.
     *
     * @return the resource ID of the extension image
     */
    String id();

    /**
     * Gets the region in which virtual machine extension image is available.
     *
     * @return the region in which virtual machine extension image is available
     */
    String regionName();

    /**
     * Gets the name of the publisher of the virtual machine extension image.
     *
     * @return the name of the publisher of the virtual machine extension image
     */
    String publisherName();

    /**
     * Gets the name of the virtual machine extension image type this image belongs to.
     *
     * @return the name of the virtual machine extension image type this image belongs to
     */
    String typeName();

    /**
     * Gets the name of the virtual machine extension image version this image represents.
     *
     * @return the name of the virtual machine extension image version this image represents
     */
    String versionName();

    /**
     * Gets the operating system this virtual machine extension image supports.
     *
     * @return the operating system this virtual machine extension image supports
     */
    OperatingSystemTypes osType();

    /**
     * Gets the type of role this virtual machine extension image supports.
     *
     * @return the type of role this virtual machine extension image supports
     */
    ComputeRoles computeRole();

    /**
     * Gets the schema defined by publisher.
     *
     * @return the schema defined by publisher, where extension consumers should provide settings in a matching schema
     */
    String handlerSchema();

    /**
     * Checks whether the extension can be used with virtual machine scale sets.
     *
     * @return true if the extension can be used with virtual machine scale sets, false otherwise
     */
    boolean supportsVirtualMachineScaleSets();

    /**
     * Checks whether the handler can support multiple extensions.
     *
     * @return true if the handler can support multiple extensions
     */
    boolean supportsMultipleExtensions();

    /**
     * Gets the virtual machine extension image version this image belongs to.
     *
     * @return the virtual machine extension image version this image belongs to
     */
    VirtualMachineExtensionImageVersion version();
}
