package com.microsoft.azure.management.storage;

import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsDeletingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsListingByGroup;
import com.microsoft.azure.management.resources.fluentcore.collection.ResourcesInGroup;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsGetting;

public interface StorageAccounts extends
        ResourcesInGroup<StorageAccount, StorageAccount.DefinitionBlank>,
        SupportsListingByGroup<StorageAccount>,
        SupportsGetting<StorageAccount>,
        SupportsGettingByGroup<StorageAccount>,
        SupportsDeletingByGroup {

    interface InGroup extends
            ResourcesInGroup<StorageAccount, StorageAccount.DefinitionAfterGroup> {
    }
}
