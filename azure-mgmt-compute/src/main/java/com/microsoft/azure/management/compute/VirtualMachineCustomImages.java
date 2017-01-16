/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsDeletingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsListingByGroup;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsBatchCreation;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeletingById;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;

/**
 * Entry point for image management API.
 */
@Fluent
public interface VirtualMachineCustomImages extends
        SupportsListing<VirtualMachineCustomImage>,
        SupportsCreating<VirtualMachineCustomImage.DefinitionStages.Blank>,
        SupportsDeletingById,
        SupportsListingByGroup<VirtualMachineCustomImage>,
        SupportsGettingByGroup<VirtualMachineCustomImage>,
        SupportsGettingById<VirtualMachineCustomImage>,
        SupportsDeletingByGroup,
        SupportsBatchCreation<VirtualMachineCustomImage> {
}
