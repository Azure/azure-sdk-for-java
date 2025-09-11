// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.models;

import com.azure.core.annotation.Fluent;

/** A type representing a SKU available for virtual machines in a scale set. */
@Fluent
public interface VirtualMachineScaleSetSku {
    /**
     * Gets the type of resource the SKU applies to.
     *
     * @return the type of resource the SKU applies to
     */
    String resourceType();

    /**
     * Gets the SKU type.
     *
     * @return the SKU type
     */
    VirtualMachineScaleSetSkuTypes skuType();

    /**
     * Gets available scaling information.
     *
     * @return available scaling information
     */
    VirtualMachineScaleSetSkuCapacity capacity();
}
