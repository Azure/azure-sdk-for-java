/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.network;

import com.azure.core.annotation.Fluent;
import com.azure.management.network.models.ExpressRouteCircuitStatsInner;
import com.azure.management.resources.fluentcore.model.HasInner;

/**
 * Contains stats associated with the peering.
 */
@Fluent
public interface ExpressRouteCircuitStats extends HasInner<ExpressRouteCircuitStatsInner> {
    /**
     * @return inbound bytes through primary channel of the peering
     */
    long primaryBytesIn();

    /**
     * @return outbound bytes through primary channel of the peering
     */
    long primaryBytesOut();

    /**
     * @return inbound bytes through secondary channel of the peering
     */
    long secondaryBytesIn();

    /**
     * @return outbound bytes through secondary channel of the peering
     */
    long secondaryBytesOut();
}
