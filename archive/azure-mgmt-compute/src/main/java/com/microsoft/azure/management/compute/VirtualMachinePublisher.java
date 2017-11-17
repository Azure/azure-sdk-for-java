/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.compute;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasName;

/**
 * Represents a virtual machine image publisher.
 */
@Fluent
public interface VirtualMachinePublisher extends HasName {
    /**
     * @return the region where virtual machine images from this publisher are available
     */
    Region region();

    /**
     * @return the offers from this publisher
     */
    VirtualMachineOffers offers();

    /**
     * @return the virtual machine image extensions from this publisher
     */
    VirtualMachineExtensionImageTypes extensionTypes();
}