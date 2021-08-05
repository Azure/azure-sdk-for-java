// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.util.CosmosPagedFlux;

import java.time.Duration;

/**
 * Specifies paging options for Cosmos Paged Flux implementation.
 * @see CosmosPagedFlux
 */
public class CosmosPagedFluxOptions {

    private String requestContinuation;
    private Integer maxItemCount;
    private TracerProvider tracerProvider;
    private String tracerSpanName;
    private String databaseId;
    private String containerId;
    private OperationType operationType;
    private ResourceType resourceType;
    private String serviceEndpoint;
    private CosmosAsyncClient cosmosAsyncClient;
    private Duration thresholdForDiagnosticsOnTracer;

    public CosmosPagedFluxOptions() {}

    public String getContainerId() {
        return containerId;
    }

    public OperationType getOperationType() {
        return operationType;
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public CosmosAsyncClient getCosmosAsyncClient() {
        return cosmosAsyncClient;
    }

    /**
     * Gets the request continuation token.
     *
     * @return the request continuation.
     */
    public String getRequestContinuation() {
        return requestContinuation;
    }

    /**
     * Sets the request continuation token.
     *
     * @param requestContinuation the request continuation.
     * @return the {@link CosmosPagedFluxOptions}.
     */
    public CosmosPagedFluxOptions setRequestContinuation(String requestContinuation) {
        this.requestContinuation = requestContinuation;
        return this;
    }

    /**
     * Gets the targeted number of items to be returned in the enumeration
     * operation per page.
     * <p>
     * For query operations this is a hard upper limit.
     * For ChangeFeed operations the number of items returned in a single
     * page can exceed the targeted number if the targeted number is smaller
     * than the number of change feed events within an atomic transaction. In this case
     * all items within that atomic transaction are returned even when this results in
     * page size > targeted maxItemSize.
     * </p>
     *
     * @return the targeted number of items.
     */
    public Integer getMaxItemCount() {
        return this.maxItemCount;
    }

    /**
     * Sets the targeted number of items to be returned in the enumeration
     * operation per page.
     * <p>
     * For query operations this is a hard upper limit.
     * For ChangeFeed operations the number of items returned in a single
     * page can exceed the targeted number if the targeted number is smaller
     * than the number of change feed events within an atomic transaction. In this case
     * all items within that atomic transaction are returned even when this results in
     * page size > targeted maxItemSize.
     * </p>
     *
     * @param maxItemCount the max number of items.
     * @return the {@link CosmosPagedFluxOptions}.
     */
    public CosmosPagedFluxOptions setMaxItemCount(Integer maxItemCount) {
        this.maxItemCount = maxItemCount;
        return this;
    }

    /**
     * Gets the tracer provider
     * @return tracerProvider
     */
    public TracerProvider getTracerProvider() {
        return this.tracerProvider;
    }

    /**
     * Gets the tracer span name
     * @return tracerSpanName
     */
    public String getTracerSpanName() {
        return tracerSpanName;
    }

    /**
     * Gets the databaseId
     * @return databaseId
     */
    public String getDatabaseId() {
        return databaseId;
    }

    /**
     * Gets the service end point
     * @return serviceEndpoint
     */
    public String getServiceEndpoint() {
        return serviceEndpoint;
    }

    /**
     * Gets the thresholdForDiagnosticsOnTracer, if latency on query operation is greater than this
     * diagnostics will be send to open telemetry exporter as events in tracer span of end to end CRUD api.
     *
     * Default is 500 ms.
     *
     * @return  thresholdForDiagnosticsOnTracer the latency threshold for diagnostics on tracer.
     */
    public Duration getThresholdForDiagnosticsOnTracer() {
        return thresholdForDiagnosticsOnTracer;
    }

    /**
     * Sets the thresholdForDiagnosticsOnTracer, if latency on query operation is greater than this
     * diagnostics will be send to open telemetry exporter as events in tracer span of end to end CRUD api.
     *
     * Default is 500 ms.
     *
     * @param thresholdForDiagnosticsOnTracer the latency threshold for diagnostics on tracer.
     */
    public void setThresholdForDiagnosticsOnTracer(Duration thresholdForDiagnosticsOnTracer) {
        this.thresholdForDiagnosticsOnTracer = thresholdForDiagnosticsOnTracer;
    }

    public void setTracerInformation(TracerProvider tracerProvider, String tracerSpanName, String serviceEndpoint, String databaseId) {
        this.databaseId = databaseId;
        this.serviceEndpoint = serviceEndpoint;
        this.tracerSpanName = tracerSpanName;
        this.tracerProvider = tracerProvider;
    }

    public void setTracerAndTelemetryInformation(String tracerSpanName,
                                                 String databaseId,
                                                 String containerId,
                                                 OperationType operationType,
                                                 ResourceType resourceType,
                                                 CosmosAsyncClient cosmosAsyncClient
    ) {
        this.tracerProvider = BridgeInternal.getTracerProvider(cosmosAsyncClient);
        this.serviceEndpoint = BridgeInternal.getServiceEndpoint(cosmosAsyncClient);
        this.tracerSpanName = tracerSpanName;
        this.databaseId = databaseId;
        this.containerId = containerId;
        this.operationType = operationType;
        this.resourceType = resourceType;
        this.cosmosAsyncClient = cosmosAsyncClient;
    }
}
