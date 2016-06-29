/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.compute;

import com.microsoft.azure.management.resources.fluentcore.arm.Region;

/**
 * Represents a virtual machine image SKU.
 */
public interface VirtualMachineSku {
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
     * @return the commercial name of the virtual machine image (SKU)
     */
    String name();

    /**
     * @return virtual machine images in the sku
     */
    VirtualMachineImagesInSku images();
}