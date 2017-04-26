/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.compute.implementation.VirtualMachineExtensionImageInner;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasName;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;

/**
 * An immutable client-side representation of an Azure virtual machine extension image type.
 */
@Fluent
public interface VirtualMachineExtensionImageType extends
        HasInner<VirtualMachineExtensionImageInner>,
        HasName {
    /**
     * @return the resource ID of the virtual machine extension image type
     */
    String id();

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

