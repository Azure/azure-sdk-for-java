/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.compute;

import com.azure.core.annotation.Fluent;
import com.azure.management.resources.fluentcore.arm.Region;
import com.azure.management.resources.fluentcore.arm.models.HasName;

/**
 * Represents a virtual machine image SKU.
 */
@Fluent
public interface VirtualMachineSku extends HasName {
    /**
     * @return the region where this virtual machine image offer SKU is available
     */
    Region region();

    /**
     * @return the publisher of this virtual machine image offer SKU
     */
    VirtualMachinePublisher publisher();

    /**
     * @return the virtual machine offer name that this SKU belongs to
     */
    VirtualMachineOffer offer();

    /**
     * @return virtual machine images in the SKU
     */
    VirtualMachineImagesInSku images();
}