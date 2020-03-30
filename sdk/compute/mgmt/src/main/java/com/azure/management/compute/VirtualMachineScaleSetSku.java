/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.compute;

import com.azure.core.annotation.Fluent;

/**
 * A type representing a SKU available for virtual machines in a scale set.
 */
@Fluent
public interface VirtualMachineScaleSetSku {
    /**
     * @return the type of resource the SKU applies to
     */
     String resourceType();

    /**
     * @return the SKU type
     */
    VirtualMachineScaleSetSkuTypes skuType();

    /**
     * @return available scaling information
     */
    VirtualMachineScaleSetSkuCapacity capacity();
}
