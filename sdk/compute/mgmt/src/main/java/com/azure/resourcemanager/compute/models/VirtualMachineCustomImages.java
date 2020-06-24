// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.compute.ComputeManager;
import com.azure.resourcemanager.compute.fluent.ImagesClient;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsBatchDeletion;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsDeletingByResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingById;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingByResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsListingByResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsBatchCreation;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsCreating;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsDeletingById;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsListing;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;

/** Entry point to custom virtual machine image management. */
@Fluent
public interface VirtualMachineCustomImages
    extends SupportsListing<VirtualMachineCustomImage>,
        SupportsCreating<VirtualMachineCustomImage.DefinitionStages.Blank>,
        SupportsDeletingById,
        SupportsListingByResourceGroup<VirtualMachineCustomImage>,
        SupportsGettingByResourceGroup<VirtualMachineCustomImage>,
        SupportsGettingById<VirtualMachineCustomImage>,
        SupportsDeletingByResourceGroup,
        SupportsBatchCreation<VirtualMachineCustomImage>,
        SupportsBatchDeletion,
        HasManager<ComputeManager>,
        HasInner<ImagesClient> {
}
