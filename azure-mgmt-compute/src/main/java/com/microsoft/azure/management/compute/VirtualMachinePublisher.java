/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.compute;

import com.microsoft.azure.management.resources.fluentcore.arm.Region;

/**
 * Represents a virtual machine image publisher.
 */
public interface VirtualMachinePublisher {
    /**
     * @return the region where virtual machine images from this publisher is available
     */
    Region region();

    /**
     * @return the name of the publisher
     */
    String name();

    /**
     * @return the offers from this publisher
     */
    VirtualMachineOffers offers();
}