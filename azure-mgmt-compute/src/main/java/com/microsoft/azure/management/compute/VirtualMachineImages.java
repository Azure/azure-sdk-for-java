/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.compute;

import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListingByRegion;

/**
 *  Entry point to virtual machine image management API.
 */
public interface VirtualMachineImages extends
        SupportsListingByRegion<VirtualMachineImage> {
    /**
     * @return entry point to virtual machine image publishers
     */
    VirtualMachinePublishers publishers();
}
