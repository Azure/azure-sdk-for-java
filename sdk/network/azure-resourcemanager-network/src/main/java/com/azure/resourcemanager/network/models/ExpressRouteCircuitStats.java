// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.network.fluent.models.ExpressRouteCircuitStatsInner;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;

/** Contains stats associated with the peering. */
@Fluent
public interface ExpressRouteCircuitStats extends HasInnerModel<ExpressRouteCircuitStatsInner> {
    /**
     * Gets inbound bytes through primary channel of the peering.
     *
     * @return inbound bytes through primary channel of the peering
     */
    long primaryBytesIn();

    /**
     * Gets outbound bytes through primary channel of the peering.
     *
     * @return outbound bytes through primary channel of the peering
     */
    long primaryBytesOut();

    /**
     * Gets inbound bytes through secondary channel of the peering.
     *
     * @return inbound bytes through secondary channel of the peering
     */
    long secondaryBytesIn();

    /**
     * Gets outbound bytes through secondary channel of the peering.
     *
     * @return outbound bytes through secondary channel of the peering
     */
    long secondaryBytesOut();
}
