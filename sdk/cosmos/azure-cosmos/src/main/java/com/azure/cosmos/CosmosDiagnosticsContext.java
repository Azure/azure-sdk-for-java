// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.ClientSideRequestStatistics;
import com.azure.cosmos.implementation.FeedResponseDiagnostics;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.ResourceType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkState;

/**
 * This class provides metadata for an operation in the Cosmos DB SDK that can be used
 * by diagnostic handlers
 */
public final class CosmosDiagnosticsContext {
    private final static ImplementationBridgeHelpers.CosmosDiagnosticsHelper.CosmosDiagnosticsAccessor diagAccessor =
        ImplementationBridgeHelpers.CosmosDiagnosticsHelper.getCosmosDiagnosticsAccessor();

    private final static ObjectMapper mapper = new ObjectMapper();

    private final String spanName;
    private final String accountName;
    private final String databaseName;
    private final String collectionName;
    private final ResourceType resourceType;
    private final String resourceTypeString;
    private final OperationType operationType;
    private final String operationTypeString;
    private final ConsistencyLevel consistencyLevel;
    private final ConcurrentLinkedDeque<CosmosDiagnostics> diagnostics;
    private final Integer maxItemCount;
    private final Duration thresholdForDiagnosticsOnTracer;

    private Throwable finalError;
    private Instant startTime = null;
    private Duration duration = null;
    private int statusCode = 0;
    private int subStatusCode = 0;
    private Integer actualItemCount = 0;
    private float totalRequestCharge = 0;
    private int maxRequestSize = 0;
    private int maxResponseSize = 0;
    private String cachedRequestDiagnostics = null;

    CosmosDiagnosticsContext(
        String spanName,
        String accountName,
        String databaseName,
        String collectionName,
        ResourceType resourceType,
        OperationType operationType,
        ConsistencyLevel consistencyLevel,
        Integer maxItemCount,
        Duration thresholdForDiagnosticsOnTracer) {

        checkNotNull(spanName, "Argument 'spanName' must not be null.");
        checkNotNull(accountName, "Argument 'accountName' must not be null.");
        checkNotNull(databaseName, "Argument 'databaseName' must not be null.");
        checkNotNull(collectionName, "Argument 'collectionName' must not be null.");
        checkNotNull(resourceType, "Argument 'resourceType' must not be null.");
        checkNotNull(operationType, "Argument 'operationType' must not be null.");
        checkNotNull(consistencyLevel, "Argument 'consistencyLevel' must not be null.");
        checkNotNull(
            thresholdForDiagnosticsOnTracer,
            "Argument 'thresholdForDiagnosticsOnTracer' must not be null.");

        this.spanName = spanName;
        this.accountName = accountName;
        this.databaseName = databaseName;
        this.collectionName = collectionName;
        this.resourceType = resourceType;
        this.resourceTypeString = resourceType.toString();
        this.operationType = operationType;
        this.operationTypeString = operationType.toString();
        this.diagnostics = new ConcurrentLinkedDeque<>();
        this.consistencyLevel = consistencyLevel;
        this.maxItemCount = maxItemCount;
        this.thresholdForDiagnosticsOnTracer = thresholdForDiagnosticsOnTracer;
    }

    /**
     * The name of the account related to the operation
     * @return the name of the account related to the operation
     */
    public String getAccountName() {
        return this.accountName;
    }

    /**
     * The name of the database related to the operation
     * @return the name of the database related to the operation
     */
    public String getDatabaseName() {
        return this.databaseName;
    }

    /**
     * The name of the collection related to the operation
     * @return the name of the collection related to the operation
     */
    public String getCollectionName() {
        return this.collectionName;
    }

    /**
     * The resource type of the operation
     * @return the resource type of the operation
     */
    public String getResourceType() {
        return this.resourceTypeString;
    }

    ResourceType getResourceTypeInternal() {
        return this.resourceType;
    }

    /**
     * The operation type of the operation
     * @return the operation type of the operation
     */
    public String getOperationType() {
        return this.operationTypeString;
    }

    OperationType getOperationTypeInternal() {
        return this.operationType;
    }

    /**
     * The effective consistency level of the operation
     * @return the effective consistency level of the operation
     */
    public ConsistencyLevel getConsistencyLevel() {
        return this.consistencyLevel;
    }

    /**
     * The max. number of items requested in a feed operation
     * @return the max. number of items requested in a feed operation. Will be null for point operations.
     */
    public Integer getMaxItemCount() {
        return this.maxItemCount;
    }

    /**
     * The actual number of items returned by a feed operation
     * @return the actual number of items returned by a feed operation. Will be null for point operations.
     */
    public Integer getActualItemCount() {
        return this.actualItemCount;
    }

    /**
     * The span name as a logical identifier for an operation
     * @return the span name as a logical identifier for an operation
     */
    public String getSpanName() {
        return this.spanName;
    }

    public boolean isLatencyThresholdViolated() {

        return this.duration != null &&
            this.thresholdForDiagnosticsOnTracer.compareTo(this.duration) < 0;
    }

    void addDiagnostics(Collection<CosmosDiagnostics> cosmosDiagnostics) {
        checkNotNull(cosmosDiagnostics, "Argument 'cosmosDiagnostics' must not be null.");
        for (CosmosDiagnostics d: cosmosDiagnostics) {
            this.addDiagnostics(d);
        }
    }

    void addDiagnostics(CosmosDiagnostics cosmosDiagnostics) {
        checkNotNull(cosmosDiagnostics, "Argument 'cosmosDiagnostics' must not be null.");
        this.addRequestSize(diagAccessor.getRequestPayloadSizeInBytes(cosmosDiagnostics));
        this.addResponseSize(diagAccessor.getTotalResponsePayloadSizeInBytes(cosmosDiagnostics));
        this.diagnostics.add(cosmosDiagnostics);
        this.cachedRequestDiagnostics = null;
    }

    /**
     * The final status code of the operation (possibly after retries)
     * @return the final status code of the operation (possibly after retries)
     */
    public int getStatusCode() {
        return this.statusCode;
    }

    /**
     * The final sub-status code of the operation (possibly after retries)
     * @return the final sub-status code of the operation (possibly after retries)
     */
    public int getSubStatusCode() {
        return this.subStatusCode;
    }

    /**
     * The final error when the operation failed
     * @return the final error when the operation failed
     */
    public Throwable getFinalError() {
        return this.finalError;
    }

    /**
     * The max. request payload size in bytes
     * @return the max. request payload size in bytes
     */
    public int getMaxRequestPayloadSizeInBytes() {
        return this.maxRequestSize;
    }

    /**
     * The max. response payload size in bytes.
     * @return the max. response payload size in bytes
     */
    public int getMaxResponsePayloadSizeInBytes() {
        return this.maxResponseSize;
    }

    /**
     * The total request charge across all retries.
     * @return the total request charge across all retries.
     */
    public float getTotalRequestCharge() {
        return this.totalRequestCharge;
    }

    synchronized void addRequestCharge(float requestCharge) {
        this.totalRequestCharge += requestCharge;
    }

    synchronized void addRequestSize(int bytes) {
        this.maxRequestSize = Math.max(this.maxRequestSize, bytes);
    }

    synchronized void addResponseSize(int bytes) {
        this.maxResponseSize = Math.max(this.maxResponseSize, bytes);
    }

    /**
     * The diagnostic records for service interactions within the scope of this SDK operation
     * @return the diagnostic records for service interactions within the scope of this SDK operation
     */
    public Collection<CosmosDiagnostics> getDiagnostics() {
        return this.diagnostics;
    }

    /**
     * Returns a flag indicating whether the operation has been completed yet.
     * @return a flag indicating whether the operation has been completed yet.
     */
    public boolean hasCompleted() {
        return this.duration != null;
    }

    /**
     * The total end-to-end duration of the operation.
     * @return the total end-to-end duration of the operation.
     */
    public Duration getDuration() {
        return this.duration;
    }

    void startOperation() {
        checkState(
            this.startTime == null,
            "Method 'startOperation' must not be called multiple times.");
        this.startTime = Instant.now();
        this.cachedRequestDiagnostics = null;
    }

    synchronized void endOperation(int statusCode, int subStatusCode, Integer actualItemCount, Throwable finalError) {
        if (this.duration != null) {
            return;
        }

        this.statusCode = statusCode;
        this.subStatusCode = subStatusCode;
        this.finalError = finalError;
        this.actualItemCount = actualItemCount;
        this.duration = Duration.between(this.startTime, Instant.now());
        this.cachedRequestDiagnostics = null;
    }

    String getRequestDiagnostics() {
        ObjectNode ctxNode = mapper.createObjectNode();

        ctxNode.put("spanName", this.spanName);
        ctxNode.put("spanName", this.accountName);
        ctxNode.put("spanName", this.databaseName);
        ctxNode.put("spanName", this.collectionName);
        ctxNode.put("spanName", this.resourceType.toString());
        ctxNode.put("spanName", this.operationType.toString());
        ctxNode.put("spanName", this.consistencyLevel.toString());
        ctxNode.put("spanName", this.statusCode);
        ctxNode.put("spanName", this.subStatusCode);
        ctxNode.put("spanName", this.totalRequestCharge);
        ctxNode.put("spanName", this.maxRequestSize);
        ctxNode.put("spanName", this.maxResponseSize);

        if (this.maxItemCount != null) {
            ctxNode.put("spanName", this.maxItemCount);
        }

        if (this.actualItemCount != null) {
            ctxNode.put("spanName", this.actualItemCount);
        }

        if (this.finalError != null) {
            ctxNode.put("exception", this.finalError.toString());
        }

        if (this.diagnostics != null && this.diagnostics.size() > 0) {
            List<String> diagnosticStrings = new ArrayList<>();
            for (CosmosDiagnostics d: this.diagnostics) {
                FeedResponseDiagnostics feedDiagnostics = d.getFeedResponseDiagnostics();
                if (feedDiagnostics != null) {
                    diagnosticStrings.add(feedDiagnostics.toString());
                }

                ClientSideRequestStatistics clientSideDiagnostics =
                    d.getClientSideRequestStatisticsRaw();
                if (clientSideDiagnostics != null) {
                    diagnosticStrings.add(clientSideDiagnostics.toString());
                }
            }
            ctxNode.putPOJO("diagnostics", diagnosticStrings);
        }

        try {
            return mapper.writeValueAsString(ctxNode);
        } catch (JsonProcessingException e) {
            return "{ \"exception\": \"" + e + "\" }";
        }
    }

    @Override
    public String toString() {
        String snapshot = this.cachedRequestDiagnostics;
        if (snapshot != null) {
            return snapshot;
        }

        synchronized (this.spanName) {
            snapshot = this.cachedRequestDiagnostics;
            if (snapshot != null) {
                return snapshot;
            }

            return this.cachedRequestDiagnostics = getRequestDiagnostics();
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    // the following helper/accessor only helps to access this class outside of this package.//
    ///////////////////////////////////////////////////////////////////////////////////////////
    static void initialize() {
        ImplementationBridgeHelpers
            .CosmosDiagnosticsContextHelper
            .setCosmosDiagnosticsContextAccessor(
                new ImplementationBridgeHelpers
                    .CosmosDiagnosticsContextHelper
                    .CosmosDiagnosticsContextAccessor() {

                    @Override
                    public CosmosDiagnosticsContext create(String spanName, String account, String databaseId,
                                                           String containerId, ResourceType resourceType,
                                                           OperationType operationType,
                                                           ConsistencyLevel consistencyLevel, Integer maxItemCount,
                                                           Duration thresholdForDiagnosticsOnTracer) {

                        return new CosmosDiagnosticsContext(
                            spanName,
                            account,
                            databaseId,
                            containerId,
                            resourceType,
                            operationType,
                            consistencyLevel,
                            maxItemCount,
                            thresholdForDiagnosticsOnTracer);
                    }

                    @Override
                    public void startOperation(CosmosDiagnosticsContext ctx) {
                        checkNotNull(ctx, "Argument 'ctx' must not be null.");
                        ctx.startOperation();
                    }

                    @Override
                    public void endOperation(CosmosDiagnosticsContext ctx, int statusCode, int subStatusCode,
                                             Integer actualItemCount, Double requestCharge,
                                             CosmosDiagnostics diagnostics, Throwable finalError) {

                        checkNotNull(ctx, "Argument 'ctx' must not be null.");
                        if (diagnostics != null) {
                            ctx.addDiagnostics(diagnostics);
                        }

                        if (requestCharge != null) {
                            ctx.addRequestCharge(requestCharge.floatValue());
                        }
                        ctx.endOperation(statusCode, subStatusCode, actualItemCount, finalError);
                    }

                    @Override
                    public void addRequestCharge(CosmosDiagnosticsContext ctx, float requestCharge) {
                        checkNotNull(ctx, "Argument 'ctx' must not be null.");
                        ctx.addRequestCharge(requestCharge);
                    }

                    @Override
                    public void addRequestSize(CosmosDiagnosticsContext ctx, int bytes) {
                        checkNotNull(ctx, "Argument 'ctx' must not be null.");
                        ctx.addRequestSize(bytes);
                    }

                    @Override
                    public void addResponseSize(CosmosDiagnosticsContext ctx, int bytes) {
                        checkNotNull(ctx, "Argument 'ctx' must not be null.");
                        ctx.addResponseSize(bytes);
                    }

                    @Override
                    public void addDiagnostics(CosmosDiagnosticsContext ctx, CosmosDiagnostics diagnostics) {
                        checkNotNull(ctx, "Argument 'ctx' must not be null.");
                        checkNotNull(ctx, "Argument 'diagnostics' must not be null.");
                        ctx.addDiagnostics(diagnostics);
                    }

                    @Override
                    public Collection<CosmosDiagnostics> getDiagnostics(CosmosDiagnosticsContext ctx) {
                        checkNotNull(ctx, "Argument 'ctx' must not be null.");
                        return ctx.getDiagnostics();
                    }

                    @Override
                    public ResourceType getResourceType(CosmosDiagnosticsContext ctx) {
                        checkNotNull(ctx, "Argument 'ctx' must not be null.");
                        return ctx.getResourceTypeInternal();
                    }

                    @Override
                    public OperationType getOperationType(CosmosDiagnosticsContext ctx) {
                        checkNotNull(ctx, "Argument 'ctx' must not be null.");
                        return ctx.getOperationTypeInternal();
                    }
                });
    }
}
