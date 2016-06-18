/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.compute;

import java.io.IOException;
import java.util.List;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.compute.VirtualMachineImage.Sku;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;

/**
 * Represents a virtual machine image offer.
 */
public interface Offer {
    /**
     * @return the region where this virtual machine image offer is available
     */
    Region region();

    /**
     * @return the publisher of this virtual machine image offer
     */
    VirtualMachineImage.Publisher publisher();

    /**
     * @return the name of the virtual machine image offer
     */
    String name();

    /**
     * Lists the virtual machine image SKUs in this offer.
     *
     * @return the virtual machine image SKUs
     * @throws CloudException thrown for an invalid response from the service
     * @throws IOException thrown for IO exception
     */
    List<Sku> listSkus() throws CloudException, IOException;
}
