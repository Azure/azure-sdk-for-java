// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.perPartitionAutomaticFailover;

import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.PartitionKeyRangeWrapper;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.routing.RegionalRoutingContext;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@JsonSerialize(using = PartitionLevelFailoverInfo.PartitionLevelFailoverInfoSerializer.class)
public class PartitionLevelFailoverInfo implements Serializable {

    // Set of URIs which have seen 503s (specific to document writes) or 403/3s
    private final Set<RegionalRoutingContext> failedRegionalRoutingContexts = ConcurrentHashMap.newKeySet();

    // The current URI corresponds to the regional endpoint to use as an override
    private RegionalRoutingContext current;
    private final GlobalEndpointManager globalEndpointManager;

    PartitionLevelFailoverInfo(RegionalRoutingContext current, GlobalEndpointManager globalEndpointManager) {
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

            return true;
        }

        return false;
    }

    public RegionalRoutingContext getCurrent() {
        return this.current;
    }

    static class PartitionLevelFailoverInfoSerializer extends com.fasterxml.jackson.databind.JsonSerializer<PartitionLevelFailoverInfo> {

        @Override
        public void serialize(PartitionLevelFailoverInfo value, JsonGenerator gen, SerializerProvider serializers) throws IOException {

            gen.writeStartObject();

            if (!value.failedRegionalRoutingContexts.isEmpty()) {

                StringBuilder sb = new StringBuilder("[");

                for (RegionalRoutingContext location : value.failedRegionalRoutingContexts) {

                    URI gatewayRegionalEndpoint = location.getGatewayRegionalEndpoint();

                    sb.append(value.globalEndpointManager.getRegionName(gatewayRegionalEndpoint, OperationType.Read)).append(",");
                }

                sb.deleteCharAt(sb.length() - 1);
                sb.append("]");

                gen.writePOJOField("failedRegions", sb.toString());
            } else {
                gen.writePOJOField("failedRegions", "[]");
            }

            if (value.current != null) {
                URI gatewayRegionalEndpoint = value.current.getGatewayRegionalEndpoint();
                gen.writePOJOField("overrideRegion", value.globalEndpointManager.getRegionName(gatewayRegionalEndpoint, OperationType.Read));
            }

            gen.writeEndObject();
        }
    }
}
