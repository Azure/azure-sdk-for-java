/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.compute;

import java.io.IOException;
import java.util.List;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;

/**
 * Represents a virtual machine image SKU.
 */
public interface Sku {
    /**
     * @return the region where this virtual machine image offer SKU is available
     */
    Region region();

    /**
     * @return the publisher of this virtual machine image offer SKU
     */
    Publisher publisher();

    /**
     * @return the virtual machine offer name that this SKU belongs to
     */
    Offer offer();

    /**
     * @return the commercial name of the virtual machine image (SKU)
     */
    String name();

    /**
     * @return virtual machine images in the sku
     */
    VirtualMachineImagesInSku images();
}
