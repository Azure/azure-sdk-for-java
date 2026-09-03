// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.perPartitionAutomaticFailover;

import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.routing.RegionalRoutingContext;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@JsonSerialize(using = PartitionLevelAutomaticFailoverInfo.PartitionLevelFailoverInfoSerializer.class)
public class PartitionLevelAutomaticFailoverInfo implements Serializable {

    // Set of URIs which have seen 503s (specific to document writes) or 403/3s
    private final Set<RegionalRoutingContext> failedRegionalRoutingContexts = ConcurrentHashMap.newKeySet();

    // The current URI corresponds to the regional endpoint to use as an override
    private RegionalRoutingContext current;
    private Instant currentWriteRegionSince;
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
            this.currentWriteRegionSince = Instant.now();

            return true;
        }

        return false;
    }

    public synchronized RegionalRoutingContext getCurrent() {
        return this.current;
    }

    synchronized PerPartitionAutomaticFailoverDiagnostics snapshot() {
        if (this.current == null || this.currentWriteRegionSince == null) {
            return PerPartitionAutomaticFailoverDiagnostics.EMPTY;
        }

        URI gatewayRegionalEndpoint = this.current.getGatewayRegionalEndpoint();
        String currentWriteRegion = this.globalEndpointManager.getRegionName(
            gatewayRegionalEndpoint,
            OperationType.Read);

        return new PerPartitionAutomaticFailoverDiagnostics(
            currentWriteRegion,
            this.currentWriteRegionSince);
    }

    static class PartitionLevelFailoverInfoSerializer extends com.fasterxml.jackson.databind.JsonSerializer<PartitionLevelAutomaticFailoverInfo> {

        @Override
        public void serialize(PartitionLevelAutomaticFailoverInfo value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            PerPartitionAutomaticFailoverDiagnostics snapshot = value.snapshot();
            gen.writeStartObject();
            if (snapshot != PerPartitionAutomaticFailoverDiagnostics.EMPTY) {
                gen.writeStringField("currWriteRegion", snapshot.getCurrentWriteRegion());
                gen.writeStringField("since", snapshot.getSince().toString());
            }
            gen.writeEndObject();
        }
    }
}
