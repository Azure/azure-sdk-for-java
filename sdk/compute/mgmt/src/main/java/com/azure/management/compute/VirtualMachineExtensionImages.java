// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.management.compute;

import com.azure.core.annotation.Fluent;
import com.azure.management.resources.fluentcore.collection.SupportsListingByRegion;

/**
 *  Entry point to virtual machine extension image management.
 */
@Fluent
public interface VirtualMachineExtensionImages extends SupportsListingByRegion<VirtualMachineExtensionImage> {
    /**
     * @return entry point to virtual machine extension image publishers
     */
    VirtualMachinePublishers publishers();
}
