/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.compute.ComputeUsage;
import com.microsoft.azure.management.compute.ComputeUsages;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;
import rx.Observable;

/**
 * The implementation of {@link ComputeUsages}.
 */
@LangDefinition
class ComputeUsagesImpl extends ReadableWrappersImpl<ComputeUsage, ComputeUsageImpl, UsageInner>
        implements ComputeUsages {
    private final ComputeManagementClientImpl client;

    ComputeUsagesImpl(ComputeManagementClientImpl client) {
        this.client = client;
    }

    @Override
    public PagedList<ComputeUsage> listByRegion(Region region) {
        return listByRegion(region.name());
    }

    @Override
    public PagedList<ComputeUsage> listByRegion(String regionName) {
        return wrapList(client.usages().list(regionName));
    }

    @Override
    public Observable<ComputeUsage> listByRegionAsync(Region region) {
        return listByRegionAsync(region.name());
    }

    @Override
    public Observable<ComputeUsage> listByRegionAsync(String regionName) {
        return wrapPageAsync(client.usages().listAsync(regionName));
    }

    @Override
    protected ComputeUsageImpl wrapModel(UsageInner usageInner) {
        if (usageInner == null) {
            return null;
        }
        return new ComputeUsageImpl(usageInner);
    }
}