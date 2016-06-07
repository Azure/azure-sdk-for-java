/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;


/**
 * Class containing deployment slot parameters.
 */
public class CsmSlotEntityInner {
    /**
     * Set the destination deployment slot during swap operation.
     */
    private String targetSlot;

    /**
     * Get or set the flag indicating it should preserve VNet to the slot
     * during swap.
     */
    private Boolean preserveVnet;

    /**
     * Get the targetSlot value.
     *
     * @return the targetSlot value
     */
    public String targetSlot() {
        return this.targetSlot;
    }

    /**
     * Set the targetSlot value.
     *
     * @param targetSlot the targetSlot value to set
     * @return the CsmSlotEntityInner object itself.
     */
    public CsmSlotEntityInner withTargetSlot(String targetSlot) {
        this.targetSlot = targetSlot;
        return this;
    }

    /**
     * Get the preserveVnet value.
     *
     * @return the preserveVnet value
     */
    public Boolean preserveVnet() {
        return this.preserveVnet;
    }

    /**
     * Set the preserveVnet value.
     *
     * @param preserveVnet the preserveVnet value to set
     * @return the CsmSlotEntityInner object itself.
     */
    public CsmSlotEntityInner withPreserveVnet(Boolean preserveVnet) {
        this.preserveVnet = preserveVnet;
        return this;
    }

}
