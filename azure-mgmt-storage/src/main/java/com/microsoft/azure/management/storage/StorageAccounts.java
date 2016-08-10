/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.storage;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsDeletingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsListingByGroup;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsBatchCreation;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeleting;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;

import java.io.IOException;

/**
 * Entry point for storage accounts management API.
 */
public interface StorageAccounts extends
        SupportsListing<StorageAccount>,
        SupportsCreating<StorageAccount.DefinitionStages.Blank>,
        SupportsDeleting,
        SupportsListingByGroup<StorageAccount>,
        SupportsGettingByGroup<StorageAccount>,
        SupportsGettingById<StorageAccount>,
        SupportsDeletingByGroup,
        SupportsBatchCreation<StorageAccount> {
    /**
     * Checks that account name is valid and is not in use.
     *
     * @param name the account name to check
     * @return whether the name is available and other info if not
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     */
    CheckNameAvailabilityResult checkNameAvailability(String name) throws CloudException, IOException;
}
