/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.network.NetworkUsage;
import com.microsoft.azure.management.network.NetworkUsages;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;

/**
 * The implementation of {@link NetworkUsages}.
 */
class NetworkUsagesImpl extends ReadableWrappersImpl<NetworkUsage, NetworkUsageImpl, UsageInner>
        implements NetworkUsages {
    private final NetworkManagementClientImpl client;

    NetworkUsagesImpl(NetworkManagementClientImpl client) {
        this.client = client;
    }

    @Override
    public PagedList<NetworkUsage> listByRegion(Region region) {
        return listByRegion(region.name());
    }

    @Override
    public PagedList<NetworkUsage> listByRegion(String regionName) {
        return wrapList(client.usages().list(regionName));
    }

    @Override
    protected NetworkUsageImpl wrapModel(UsageInner usageInner) {
        if (usageInner == null) {
            return null;
        }
        return new NetworkUsageImpl(usageInner);
    }
}