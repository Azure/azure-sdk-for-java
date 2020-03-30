/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.compute.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.management.compute.models.ComputeManagementClientImpl;
import com.azure.management.compute.models.UsageInner;
import com.azure.management.compute.ComputeUsage;
import com.azure.management.compute.ComputeUsages;
import com.azure.management.resources.fluentcore.arm.Region;
import com.azure.management.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;

/**
 * The implementation of {@link ComputeUsages}.
 */
class ComputeUsagesImpl extends ReadableWrappersImpl<ComputeUsage, ComputeUsageImpl, UsageInner>
        implements ComputeUsages {
    private final ComputeManagementClientImpl client;

    ComputeUsagesImpl(ComputeManagementClientImpl client) {
        this.client = client;
    }

    @Override
    public PagedIterable<ComputeUsage> listByRegion(Region region) {
        return listByRegion(region.name());
    }

    @Override
    public PagedIterable<ComputeUsage> listByRegion(String regionName) {
        return wrapList(client.usages().list(regionName));
    }

    @Override
    public PagedFlux<ComputeUsage> listByRegionAsync(Region region) {
        return listByRegionAsync(region.name());
    }

    @Override
    public PagedFlux<ComputeUsage> listByRegionAsync(String regionName) {
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