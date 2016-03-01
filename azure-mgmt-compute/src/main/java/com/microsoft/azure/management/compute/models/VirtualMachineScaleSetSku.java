/**
 * Object]
 */

package com.microsoft.azure.management.compute.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Describes an available virtual machine scale set sku.
 */
public class VirtualMachineScaleSetSku {
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
    public String getResourceType() {
        return this.resourceType;
    }

    /**
     * Get the sku value.
     *
     * @return the sku value
     */
    public Sku getSku() {
        return this.sku;
    }

    /**
     * Get the capacity value.
     *
     * @return the capacity value
     */
    public VirtualMachineScaleSetSkuCapacity getCapacity() {
        return this.capacity;
    }

}
