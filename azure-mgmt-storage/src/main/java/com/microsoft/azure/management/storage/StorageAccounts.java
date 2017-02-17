/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.storage;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.*;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasManager;
import com.microsoft.azure.management.resources.fluentcore.collection.*;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
import com.microsoft.azure.management.storage.implementation.StorageAccountsInner;
import com.microsoft.azure.management.storage.implementation.StorageManager;

/**
 * Entry point for storage accounts management API.
 */
@Fluent
public interface StorageAccounts extends
        SupportsListing<StorageAccount>,
        SupportsCreating<StorageAccount.DefinitionStages.Blank>,
        SupportsDeletingById,
        SupportsListingByGroup<StorageAccount>,
        SupportsGettingByGroup<StorageAccount>,
        SupportsGettingById<StorageAccount>,
        SupportsDeletingByGroup,
        SupportsBatchCreation<StorageAccount>,
        HasManager<StorageManager>,
        HasInner<StorageAccountsInner>,
        SupportsAsyncListing <StorageAccount> {
    /**
     * Checks that account name is valid and is not in use.
     *
     * @param name the account name to check
     * @return whether the name is available and other info if not
     */
    CheckNameAvailabilityResult checkNameAvailability(String name);
}
