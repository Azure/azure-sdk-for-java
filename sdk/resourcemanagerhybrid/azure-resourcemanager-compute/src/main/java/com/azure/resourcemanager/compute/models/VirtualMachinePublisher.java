// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.compute.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.management.Region;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasName;

/** Represents a virtual machine image publisher. */
@Fluent
public interface VirtualMachinePublisher extends HasName {
    /** @return the region where virtual machine images from this publisher are available */
    Region region();

    /** @return the offers from this publisher */
    VirtualMachineOffers offers();

    /** @return the virtual machine image extensions from this publisher */
    VirtualMachineExtensionImageTypes extensionTypes();
}
