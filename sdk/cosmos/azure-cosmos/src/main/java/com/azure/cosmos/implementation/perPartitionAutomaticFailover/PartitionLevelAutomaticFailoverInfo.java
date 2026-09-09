// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.perPartitionAutomaticFailover;

import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.routing.RegionalRoutingContext;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class PartitionLevelAutomaticFailoverInfo implements Serializable {

    // Set of URIs which have seen 503s (specific to document writes) or 403/3s
    private final Set<RegionalRoutingContext> failedRegionalRoutingContexts = ConcurrentHashMap.newKeySet();

    // The current URI corresponds to the regional endpoint to use as an override
    private RegionalRoutingContext current;
    private volatile PerPartitionAutomaticFailoverDiagnostics diagnosticsSnapshot
        = PerPartitionAutomaticFailoverDiagnostics.EMPTY;
    private final GlobalEndpointManager globalEndpointManager;

    PartitionLevelAutomaticFailoverInfo(RegionalRoutingContext current, GlobalEndpointManager globalEndpointManager) {
        this.current = current;
        this.globalEndpointManager = globalEndpointManager;
    }

    synchronized boolean tryMoveToNextLocation(
        List<RegionalRoutingContext> readRegionalRoutingContexts,
        RegionalRoutingContext failedRegionalRoutingContext) {

        if (!failedRegionalRoutingContext.equals(this.current)) {
            return true;
        }

        for (RegionalRoutingContext regionalRoutingContext : readRegionalRoutingContexts) {

            if (regionalRoutingContext.equals(this.current)) {
                continue;
            }

            if (this.failedRegionalRoutingContexts.contains(regionalRoutingContext)) {
                continue;
            }

            this.failedRegionalRoutingContexts.add(failedRegionalRoutingContext);
            this.current = regionalRoutingContext;
            Instant currentWriteRegionSince = Instant.now();
            String currentWriteRegion = this.globalEndpointManager.getRegionName(
                regionalRoutingContext.getGatewayRegionalEndpoint(),
                OperationType.Read);
            this.diagnosticsSnapshot = new PerPartitionAutomaticFailoverDiagnostics(
                currentWriteRegion,
                currentWriteRegionSince);

            return true;
        }

        return false;
    }

    public synchronized RegionalRoutingContext getCurrent() {
        return this.current;
    }

    PerPartitionAutomaticFailoverDiagnostics snapshot() {
        return this.diagnosticsSnapshot;
    }
}
