/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.network.implementation;

import com.azure.management.network.ExpressRouteCircuitStats;
import com.azure.management.network.models.ExpressRouteCircuitStatsInner;
import com.azure.management.resources.fluentcore.model.implementation.WrapperImpl;
import com.azure.management.resources.fluentcore.utils.Utils;

/**
 * Implementation for {@link ExpressRouteCircuitStats}.
 */
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

