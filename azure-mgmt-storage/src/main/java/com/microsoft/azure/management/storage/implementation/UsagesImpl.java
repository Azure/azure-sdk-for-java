package com.microsoft.azure.management.storage.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;
import com.microsoft.azure.management.storage.StorageUsage;
import com.microsoft.azure.management.storage.Usages;
import java.io.IOException;

/**
 * The implementation of {@link Usages}.
 */
class UsagesImpl extends ReadableWrappersImpl<StorageUsage, UsageImpl, UsageInner>
        implements Usages {
    private final StorageManagementClientImpl client;

    UsagesImpl(StorageManagementClientImpl client) {
        this.client = client;
    }

    @Override
    public PagedList<StorageUsage> list() throws CloudException, IOException {
        return wrapList(client.usages().list());
    }

    @Override
    protected UsageImpl wrapModel(UsageInner usageInner) {
        return new UsageImpl(usageInner);
    }
}
