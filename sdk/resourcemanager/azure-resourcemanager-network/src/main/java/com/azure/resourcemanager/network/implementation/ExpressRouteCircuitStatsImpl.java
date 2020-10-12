// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.resourcemanager.network.models.ExpressRouteCircuitStats;
import com.azure.resourcemanager.network.fluent.models.ExpressRouteCircuitStatsInner;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;

/** Implementation for {@link ExpressRouteCircuitStats}. */
public class ExpressRouteCircuitStatsImpl extends WrapperImpl<ExpressRouteCircuitStatsInner>
    implements ExpressRouteCircuitStats {
    ExpressRouteCircuitStatsImpl(ExpressRouteCircuitStatsInner innerObject) {
        super(innerObject);
    }

    @Override
    public long primaryBytesIn() {
        return ResourceManagerUtils.toPrimitiveLong(innerModel().primarybytesIn());
    }

    @Override
    public long primaryBytesOut() {
        return ResourceManagerUtils.toPrimitiveLong(innerModel().primarybytesOut());
    }

    @Override
    public long secondaryBytesIn() {
        return ResourceManagerUtils.toPrimitiveLong(innerModel().secondarybytesIn());
    }

    @Override
    public long secondaryBytesOut() {
        return ResourceManagerUtils.toPrimitiveLong(innerModel().secondarybytesOut());
    }
}
