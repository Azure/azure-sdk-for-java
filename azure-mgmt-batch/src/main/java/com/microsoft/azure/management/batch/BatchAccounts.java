/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.batch;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.batch.implementation.BatchAccountsInner;
import com.microsoft.azure.management.batch.implementation.BatchManager;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
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
 *  Entry point to Azure Batch account management API.
 */
@Fluent
public interface BatchAccounts extends
        SupportsCreating<BatchAccount.DefinitionStages.Blank>,
        SupportsListing<BatchAccount>,
        SupportsListingByResourceGroup<BatchAccount>,
        SupportsGettingByResourceGroup<BatchAccount>,
        SupportsGettingById<BatchAccount>,
        SupportsDeletingById,
        SupportsDeletingByResourceGroup,
        SupportsBatchCreation<BatchAccount>,
        SupportsBatchDeletion,
        HasManager<BatchManager>,
        HasInner<BatchAccountsInner> {

    /**
     * Looks up the number of Batch accounts that can be created in the specified region.
     *
     * @param region an Azure region
     * @return the number of Batch accounts that can be created in the specified region
     */
    int getBatchAccountQuotaByLocation(Region region);
}
