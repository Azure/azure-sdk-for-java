// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.network.models.NetworkUsage;
import com.azure.resourcemanager.network.models.NetworkUsages;
import com.azure.resourcemanager.network.NetworkManagementClient;
import com.azure.resourcemanager.network.fluent.inner.UsageInner;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;

/** The implementation of NetworkUsages. */
public class NetworkUsagesImpl extends ReadableWrappersImpl<NetworkUsage, NetworkUsageImpl, UsageInner>
    implements NetworkUsages {
    private final NetworkManagementClient client;

    public NetworkUsagesImpl(NetworkManagementClient client) {
        this.client = client;
    }

    @Override
    public PagedIterable<NetworkUsage> listByRegion(Region region) {
        return listByRegion(region.name());
    }

    @Override
    public PagedIterable<NetworkUsage> listByRegion(String regionName) {
        return wrapList(client.getUsages().list(regionName));
    }

    @Override
    public PagedFlux<NetworkUsage> listByRegionAsync(Region region) {
        return listByRegionAsync(region.name());
    }

    @Override
    public PagedFlux<NetworkUsage> listByRegionAsync(String regionName) {
        return wrapPageAsync(client.getUsages().listAsync(regionName));
    }

    @Override
    protected NetworkUsageImpl wrapModel(UsageInner usageInner) {
        if (usageInner == null) {
            return null;
        }
        return new NetworkUsageImpl(usageInner);
    }
}
