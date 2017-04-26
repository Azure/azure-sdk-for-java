/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.compute.implementation.ComputeManager;
import com.microsoft.azure.management.compute.implementation.ImagesInner;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsBatchDeletion;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsDeletingByResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsListingByResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasManager;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsBatchCreation;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeletingById;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;

/**
 * Entry point to custom virtual machine image management.
 */
@Fluent
public interface VirtualMachineCustomImages extends
        SupportsListing<VirtualMachineCustomImage>,
        SupportsCreating<VirtualMachineCustomImage.DefinitionStages.Blank>,
        SupportsDeletingById,
        SupportsListingByResourceGroup<VirtualMachineCustomImage>,
        SupportsGettingByResourceGroup<VirtualMachineCustomImage>,
        SupportsGettingById<VirtualMachineCustomImage>,
        SupportsDeletingByResourceGroup,
        SupportsBatchCreation<VirtualMachineCustomImage>,
        SupportsBatchDeletion,
        HasManager<ComputeManager>,
        HasInner<ImagesInner> {
}
