// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.network.NetworkUsage;
import com.azure.resourcemanager.network.NetworkUsages;
import com.azure.resourcemanager.network.models.NetworkManagementClientImpl;
import com.azure.resourcemanager.network.models.UsageInner;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;

/** The implementation of NetworkUsages. */
class NetworkUsagesImpl extends ReadableWrappersImpl<NetworkUsage, NetworkUsageImpl, UsageInner>
    implements NetworkUsages {
    private final NetworkManagementClientImpl client;

    NetworkUsagesImpl(NetworkManagementClientImpl client) {
        this.client = client;
    }

    @Override
    public PagedIterable<NetworkUsage> listByRegion(Region region) {
        return listByRegion(region.name());
    }

    @Override
    public PagedIterable<NetworkUsage> listByRegion(String regionName) {
        return wrapList(client.usages().list(regionName));
    }

    @Override
    public PagedFlux<NetworkUsage> listByRegionAsync(Region region) {
        return listByRegionAsync(region.name());
    }

    @Override
    public PagedFlux<NetworkUsage> listByRegionAsync(String regionName) {
        return wrapPageAsync(client.usages().listAsync(regionName));
    }

    @Override
    protected NetworkUsageImpl wrapModel(UsageInner usageInner) {
        if (usageInner == null) {
            return null;
        }
        return new NetworkUsageImpl(usageInner);
    }
}
