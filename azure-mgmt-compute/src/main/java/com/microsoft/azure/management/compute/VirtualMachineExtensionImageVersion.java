/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.compute.implementation.VirtualMachineExtensionImageInner;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasName;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;

/**
 * An immutable client-side representation of an Azure virtual machine extension image version.
 */
@Fluent
public interface VirtualMachineExtensionImageVersion extends
        Wrapper<VirtualMachineExtensionImageInner>,
        HasName {
    /**
     * @return the resource ID of the extension image version
     */
    String id();

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
     */
    VirtualMachineExtensionImage getImage();
}