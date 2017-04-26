/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.storage.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;
import com.microsoft.azure.management.storage.StorageUsage;
import com.microsoft.azure.management.storage.Usages;
import rx.Observable;

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
    public PagedList<StorageUsage> list() {
        return wrapList(client.usages().list());
    }

    @Override
    public Observable<StorageUsage> listAsync() {
        return wrapPageAsync(client.usages().listAsync());
    }

    @Override
    protected UsageImpl wrapModel(UsageInner usageInner) {
        if (usageInner == null) {
            return null;
        }
        return new UsageImpl(usageInner);
    }
}
