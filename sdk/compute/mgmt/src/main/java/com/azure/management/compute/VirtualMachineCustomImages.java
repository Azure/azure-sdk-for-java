/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.compute;

import com.azure.core.annotation.Fluent;
import com.azure.management.compute.implementation.ComputeManager;
import com.azure.management.compute.models.ImagesInner;
import com.azure.management.resources.fluentcore.arm.collection.SupportsBatchDeletion;
import com.azure.management.resources.fluentcore.arm.collection.SupportsDeletingByResourceGroup;
import com.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.azure.management.resources.fluentcore.arm.collection.SupportsGettingByResourceGroup;
import com.azure.management.resources.fluentcore.arm.collection.SupportsListingByResourceGroup;
import com.azure.management.resources.fluentcore.arm.models.HasManager;
import com.azure.management.resources.fluentcore.collection.SupportsBatchCreation;
import com.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.azure.management.resources.fluentcore.collection.SupportsDeletingById;
import com.azure.management.resources.fluentcore.collection.SupportsListing;
import com.azure.management.resources.fluentcore.model.HasInner;

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
