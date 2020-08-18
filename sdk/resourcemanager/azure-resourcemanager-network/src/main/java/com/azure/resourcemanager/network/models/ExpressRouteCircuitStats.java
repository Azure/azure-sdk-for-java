// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.network.fluent.inner.ExpressRouteCircuitStatsInner;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;

/** Contains stats associated with the peering. */
@Fluent
public interface ExpressRouteCircuitStats extends HasInner<ExpressRouteCircuitStatsInner> {
    /** @return inbound bytes through primary channel of the peering */
    long primaryBytesIn();

    /** @return outbound bytes through primary channel of the peering */
    long primaryBytesOut();

    /** @return inbound bytes through secondary channel of the peering */
    long secondaryBytesIn();

    /** @return outbound bytes through secondary channel of the peering */
    long secondaryBytesOut();
}
