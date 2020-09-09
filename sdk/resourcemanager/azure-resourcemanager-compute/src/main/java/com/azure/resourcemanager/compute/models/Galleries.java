// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.compute.fluent.GalleriesClient;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsBatchDeletion;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsDeletingByResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingByResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsListingByResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsCreating;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsListing;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;

/** Entry point to galleries management API in Azure. */
@Fluent
public interface Galleries
    extends SupportsCreating<Gallery.DefinitionStages.Blank>,
        SupportsDeletingByResourceGroup,
        SupportsBatchDeletion,
        SupportsGettingByResourceGroup<Gallery>,
        SupportsListingByResourceGroup<Gallery>,
        SupportsListing<Gallery>,
        HasInner<GalleriesClient> {
}
