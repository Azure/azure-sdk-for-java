package com.microsoft.azure.management.storage;

import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsDeletingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsListingByGroup;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeleting;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;

public interface StorageAccounts extends
        SupportsListing<StorageAccount>,
        SupportsListingByGroup<StorageAccount>,
        SupportsGettingByGroup<StorageAccount>,
        SupportsCreating<StorageAccount.DefinitionBlank>,
        SupportsDeleting,
        SupportsDeletingByGroup {
}
