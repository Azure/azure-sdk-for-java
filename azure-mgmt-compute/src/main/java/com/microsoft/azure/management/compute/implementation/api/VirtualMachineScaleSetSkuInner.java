/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.implementation.api;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Describes an available virtual machine scale set sku.
 */
public class VirtualMachineScaleSetSkuInner {
    /**
     * Gets the type of resource the sku applies to.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String resourceType;

    /**
     * Gets the Sku.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Sku sku;

    /**
     * Gets available scaling information.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private VirtualMachineScaleSetSkuCapacity capacity;

    /**
     * Get the resourceType value.
     *
     * @return the resourceType value
     */
    public String resourceType() {
        return this.resourceType;
    }

    /**
     * Get the sku value.
     *
     * @return the sku value
     */
    public Sku sku() {
        return this.sku;
    }

    /**
     * Get the capacity value.
     *
     * @return the capacity value
     */
    public VirtualMachineScaleSetSkuCapacity capacity() {
        return this.capacity;
    }

}
