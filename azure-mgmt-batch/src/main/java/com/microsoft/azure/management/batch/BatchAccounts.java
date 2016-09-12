package com.microsoft.azure.management.batch;

import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsDeletingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsListingByGroup;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsBatchCreation;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeleting;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;

/**
 *  Entry point to batch account management API.
 */
public interface BatchAccounts extends
        SupportsCreating<BatchAccount.DefinitionStages.Blank>,
        SupportsListing<BatchAccount>,
        SupportsListingByGroup<BatchAccount>,
        SupportsGettingByGroup<BatchAccount>,
        SupportsGettingById<BatchAccount>,
        SupportsDeleting,
        SupportsDeletingByGroup,
        SupportsBatchCreation<BatchAccount> {
}
