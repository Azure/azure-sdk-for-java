// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.compute.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.management.Region;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasName;

/** Represents a virtual machine image offer. */
@Fluent
public interface VirtualMachineOffer extends HasName {
    /**
     * Gets the region where this virtual machine image offer is available.
     *
     * @return the region where this virtual machine image offer is available
     */
    Region region();

    /**
     * Gets the publisher of this virtual machine image offer.
     *
     * @return the publisher of this virtual machine image offer
     */
    VirtualMachinePublisher publisher();

    /**
     * Gets virtual machine image SKUs available in this offer.
     *
     * @return virtual machine image SKUs available in this offer
     */
    VirtualMachineSkus skus();
}
