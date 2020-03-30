/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.compute;

import com.azure.core.annotation.Fluent;
import com.azure.management.compute.models.GalleriesInner;
import com.azure.management.resources.fluentcore.arm.collection.SupportsBatchDeletion;
import com.azure.management.resources.fluentcore.arm.collection.SupportsDeletingByResourceGroup;
import com.azure.management.resources.fluentcore.arm.collection.SupportsGettingByResourceGroup;
import com.azure.management.resources.fluentcore.arm.collection.SupportsListingByResourceGroup;
import com.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.azure.management.resources.fluentcore.collection.SupportsListing;
import com.azure.management.resources.fluentcore.model.HasInner;

/**
 * Entry point to galleries management API in Azure.
 */
@Fluent
public interface Galleries extends SupportsCreating<Gallery.DefinitionStages.Blank>,
        SupportsDeletingByResourceGroup,
        SupportsBatchDeletion,
        SupportsGettingByResourceGroup<Gallery>,
        SupportsListingByResourceGroup<Gallery>,
        SupportsListing<Gallery>,
        HasInner<GalleriesInner> {
}
