package com.microsoft.azure.management.storage.models;

import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;
import com.microsoft.azure.management.storage.models.implementation.api.StorageAccountInner;

public interface StorageAccount extends
        GroupableResource,
        Refreshable<StorageAccount>,
        Wrapper<StorageAccountInner> {
    interface DefinitionBlank {}
}

