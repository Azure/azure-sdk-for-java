// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * This class represents diagnostic information for transport requests (calls to a replica in direct mode, calls to
 * the Gateway for example to get metadata like physical addresses of replica for a partition.
 */
public final class CosmosDiagnosticsRequestInfo {

    private final String activityId;
    private final String partitionId;
    private final String partitionKeyRangeId;
    private final String requestType;
    private final Instant startTime;
    private final Duration duration;
    private final Duration backendLatency;
    private final Collection<CosmosDiagnosticsRequestEvent> events;
    private final String endpoint;

    CosmosDiagnosticsRequestInfo(
        String activityId,
        String partitionId, // nullable
        String partitionKeyRangeId, // nullable
        String requestType,
        Instant startTime,
        Duration duration, // nullable
        Duration backendLatency, // nullable
        double requestCharge,
        int responsePayloadSizeInBytes,
        int statusCode,
        int subStatusCode,
        Collection<CosmosDiagnosticsRequestEvent> events,
        String endpoint) {

        checkNotNull(activityId, "Argument 'activityId' must not be null.");
        checkNotNull(requestType, "Argument 'requestType' must not be null.");
        checkNotNull(startTime, "Argument 'startTime' must not be null.");
        checkNotNull(events, "Argument 'events' must not be null.");

        this.activityId = activityId;
        this.partitionId = partitionId;
        this.partitionKeyRangeId = partitionKeyRangeId;
        this.requestType = requestType;
        this.startTime = startTime;
        this.duration = duration;
        this.events = events;
        this.backendLatency = backendLatency;
        this.endpoint = endpoint != null ? endpoint : "";
    }

    /**
     * Gets the Activity used to track this request in the Cosmos DB service.
     * @return the Activity used to track this request in the Cosmos DB service.
     */
    public String getActivityId() {
        return this.activityId;
    }

    /**
     * Gets the identifier for the physical partition/shard this request was processed in or null when the request
     * was not targeting a replica/partition directly.
     * @return the identifier for the physical partition/shard this request was processed in or null when the request
     * was not targeting a replica/partition directly.
     */
    public String getPartitionId() {
        return this.partitionId;
    }

    /**
     * Gets the PartitionKeyRangeId for the physical partition/shard this request was processed in or null when the request
     * was not targeting a replica/partition directly.
     * @return the PartitionKeyRangeId for the physical partition/shard this request was processed in or null when the request
     * was not targeting a replica/partition directly.
     */
    public String getPartitionKeyRangeId() {
        return this.partitionKeyRangeId;
    }

    /**
     * Gets the type of request
     * @return the type of the request
     */
    public String getRequestType() {
        return this.requestType;
    }

    /**
     * Gets the start time of the request.
     * @return the start time of the request.
     */
    public Instant getStartTime() {
        return this.startTime;
    }

    /**
     * Gets the total duration for processing this request.
     * @return the total duration for processing this request.
     */
    public Duration getDuration() {
        return this.duration;
    }

    /**
     * Gets the backend latency if available or null when backend latency isn't available for a request
     * @return the backend latency if available or null when backend latency isn't available for a request
     */
    public Duration getBackendLatency() {
        return this.backendLatency;
    }

    /**
     * Gets more granular information about different stages of the request pipeline.
     * @return more granular information about different stages of the request pipeline.
     */
    public Collection<CosmosDiagnosticsRequestEvent> getRequestPipelineEvents() {
        return this.events;
    }

    /**
     * Gets the endpoint targeted by this request
     * @return the endpoint targeted by this request
     */
    public String getEndpoint() {
        return this.endpoint;
    }
}
