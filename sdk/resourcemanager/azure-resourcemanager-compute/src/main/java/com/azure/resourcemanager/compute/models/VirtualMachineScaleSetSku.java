// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.models;

import com.azure.core.annotation.Fluent;

/** A type representing a SKU available for virtual machines in a scale set. */
@Fluent
public interface VirtualMachineScaleSetSku {
    /** @return the type of resource the SKU applies to */
    String resourceType();

    /** @return the SKU type */
    VirtualMachineScaleSetSkuTypes skuType();

    /** @return available scaling information */
    VirtualMachineScaleSetSkuCapacity capacity();
}
