/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.batch;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.batch.implementation.BatchManager;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsDeletingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsListingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasManager;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsBatchCreation;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeletingById;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;

/**
 *  Entry point to batch account management API.
 */
@Fluent
public interface BatchAccounts extends
        SupportsCreating<BatchAccount.DefinitionStages.Blank>,
        SupportsListing<BatchAccount>,
        SupportsListingByGroup<BatchAccount>,
        SupportsGettingByGroup<BatchAccount>,
        SupportsGettingById<BatchAccount>,
        SupportsDeletingById,
        SupportsDeletingByGroup,
        SupportsBatchCreation<BatchAccount>,
        HasManager<BatchManager> {
    /**
     * Queries the number of the batch account can be created in specified region`.
     *
     * @param region the region in for which to check quota
     * @return whether the number of batch accounts can be created in specified region.
     */
    int getBatchAccountQuotaByLocation(Region region);
}
