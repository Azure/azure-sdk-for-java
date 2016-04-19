package com.microsoft.azure.management.storage;

import com.microsoft.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeleting;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;
import com.microsoft.azure.management.storage.models.StorageAccount;

public interface ResourceGroupContext {
    // StorageAccount collection having a resource group context.
    interface StorageAccounts extends
            SupportsListing<StorageAccount>,
            SupportsCreating<StorageAccount.DefinitionWithGroupContextBlank>,
            SupportsDeleting {
    }
}
