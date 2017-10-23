/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.network.ExpressRouteCircuitStats;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.WrapperImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;

/**
 * Implementation for {@link com.microsoft.azure.management.network.ExpressRouteCircuitStats}.
 */
@LangDefinition
public class ExpressRouteCircuitStatsImpl extends WrapperImpl<ExpressRouteCircuitStatsInner>
        implements ExpressRouteCircuitStats {
    ExpressRouteCircuitStatsImpl(ExpressRouteCircuitStatsInner innerObject) {
        super(innerObject);
    }

    @Override
    public long primarybytesIn() {
        return Utils.toPrimitiveLong(inner().primarybytesIn());
    }

    @Override
    public long primarybytesOut() {
        return Utils.toPrimitiveLong(inner().primarybytesOut());
    }

    @Override
    public long secondarybytesIn() {
        return Utils.toPrimitiveLong(inner().secondarybytesIn());
    }

    @Override
    public long secondarybytesOut() {
        return Utils.toPrimitiveLong(inner().secondarybytesOut());
    }
}

