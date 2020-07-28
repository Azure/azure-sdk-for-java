// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.resourcemanager.network.models.ExpressRouteCircuitStats;
import com.azure.resourcemanager.network.fluent.inner.ExpressRouteCircuitStatsInner;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.Utils;

/** Implementation for {@link ExpressRouteCircuitStats}. */
public class ExpressRouteCircuitStatsImpl extends WrapperImpl<ExpressRouteCircuitStatsInner>
    implements ExpressRouteCircuitStats {
    ExpressRouteCircuitStatsImpl(ExpressRouteCircuitStatsInner innerObject) {
        super(innerObject);
    }

    @Override
    public long primaryBytesIn() {
        return Utils.toPrimitiveLong(inner().primarybytesIn());
    }

    @Override
    public long primaryBytesOut() {
        return Utils.toPrimitiveLong(inner().primarybytesOut());
    }

    @Override
    public long secondaryBytesIn() {
        return Utils.toPrimitiveLong(inner().secondarybytesIn());
    }

    @Override
    public long secondaryBytesOut() {
        return Utils.toPrimitiveLong(inner().secondarybytesOut());
    }
}
