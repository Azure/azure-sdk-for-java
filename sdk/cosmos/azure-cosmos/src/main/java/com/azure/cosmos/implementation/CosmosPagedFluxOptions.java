// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosDiagnosticsThresholds;
import com.azure.cosmos.util.CosmosPagedFlux;

import java.time.Duration;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Specifies paging options for Cosmos Paged Flux implementation.
 * @see CosmosPagedFlux
 */
public class CosmosPagedFluxOptions {
    private static final ImplementationBridgeHelpers.CosmosAsyncClientHelper.CosmosAsyncClientAccessor clientAccessor =
        ImplementationBridgeHelpers.CosmosAsyncClientHelper.getCosmosAsyncClientAccessor();

    private String requestContinuation;
    private Integer maxItemCount;
    private DiagnosticsProvider tracerProvider;
    private String tracerSpanName;
    private String databaseId;
    private String containerId;
    private OperationType operationType;
    private ResourceType resourceType;
    private String serviceEndpoint;
    private CosmosAsyncClient cosmosAsyncClient;
    private CosmosDiagnosticsThresholds thresholds;
    private String operationId;
    public ConsistencyLevel effectiveConsistencyLevel;

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

    public ConsistencyLevel getEffectiveConsistencyLevel() { return this.effectiveConsistencyLevel; }

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
    public DiagnosticsProvider getTracerProvider() {
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
    public String getAccountTag() {
        return serviceEndpoint;
    }

    public CosmosDiagnosticsThresholds getDiagnosticsThresholds() {
        return  this.thresholds;
    }

    public void setDiagnosticsThresholds(CosmosDiagnosticsThresholds thresholds) {
        this.thresholds = thresholds;
    }

    public String getOperationId() {
        return this.operationId;
    }


    public void setTracerInformation(
        String tracerSpanName,
        String databaseId,
        String containerId,
        String operationId,
        OperationType operationType,
        ResourceType resourceType,
        CosmosAsyncClient cosmosAsyncClient,
        ConsistencyLevel consistencyLevel,
        CosmosDiagnosticsThresholds thresholds) {

        checkNotNull(tracerSpanName, "Argument 'tracerSpanName' must not be NULL.");
        checkNotNull(operationType, "Argument 'operationType' must not be NULL.");
        checkNotNull(resourceType, "Argument 'resourceType' must not be NULL.");
        checkNotNull(cosmosAsyncClient, "Argument 'cosmosAsyncClient' must not be NULL.");
        checkNotNull(thresholds, "Argument 'thresholds' must not be NULL.");

        this.databaseId = databaseId;
        this.containerId = containerId;
        this.tracerSpanName = tracerSpanName;
        this.tracerProvider  =  BridgeInternal.getTracerProvider(cosmosAsyncClient);
        this.serviceEndpoint = clientAccessor.getAccountTagValue(cosmosAsyncClient);
        this.operationId = operationId;
        this.operationType = operationType;
        this.resourceType = resourceType;
        this.cosmosAsyncClient = cosmosAsyncClient;
        this.effectiveConsistencyLevel = clientAccessor
            .getEffectiveConsistencyLevel(cosmosAsyncClient, operationType, consistencyLevel);
        this.thresholds = thresholds;
    }

    public void setTracerAndTelemetryInformation(String tracerSpanName,
                                                 String databaseId,
                                                 String containerId,
                                                 OperationType operationType,
                                                 ResourceType resourceType,
                                                 CosmosAsyncClient cosmosAsyncClient,
                                                 String operationId,
                                                 ConsistencyLevel consistencyLevel,
                                                 CosmosDiagnosticsThresholds thresholds
    ) {
        checkNotNull(tracerSpanName, "Argument 'tracerSpanName' must not be NULL.");
        checkNotNull(databaseId, "Argument 'databaseId' must not be NULL.");
        checkNotNull(operationType, "Argument 'operationType' must not be NULL.");
        checkNotNull(resourceType, "Argument 'resourceType' must not be NULL.");
        checkNotNull(cosmosAsyncClient, "Argument 'cosmosAsyncClient' must not be NULL.");
        checkNotNull(thresholds, "Argument 'thresholds' must not be NULL.");
        this.tracerProvider  =  BridgeInternal.getTracerProvider(cosmosAsyncClient);
        this.serviceEndpoint = clientAccessor.getAccountTagValue(cosmosAsyncClient);
        this.tracerSpanName = tracerSpanName;
        this.databaseId = databaseId;
        this.containerId = containerId;
        this.operationType = operationType;
        this.resourceType = resourceType;
        this.cosmosAsyncClient = cosmosAsyncClient;
        this.operationId = operationId;
        this.effectiveConsistencyLevel = clientAccessor
            .getEffectiveConsistencyLevel(cosmosAsyncClient, operationType, consistencyLevel);
        this.thresholds = thresholds;
    }
}
