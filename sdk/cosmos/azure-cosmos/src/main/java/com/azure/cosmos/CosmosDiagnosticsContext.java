// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.core.util.Context;
import com.azure.cosmos.implementation.ClientSideRequestStatistics;
import com.azure.cosmos.implementation.FeedResponseDiagnostics;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.ResourceType;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkState;

public final class CosmosDiagnosticsContext {
    private static final ImplementationBridgeHelpers.CosmosDiagnosticsHelper.CosmosDiagnosticsAccessor diagAccessor =
        ImplementationBridgeHelpers.CosmosDiagnosticsHelper.getCosmosDiagnosticsAccessor();

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

    private Throwable finalError;
    private Instant startTime = null;
    private Duration duration = null;
    private int statusCode = 0;
    private int subStatusCode = 0;
    private Integer actualItemCount = 0;
    private float totalRequestCharge = 0;
    private int maxRequestSize = 0;
    private int maxResponseSize = 0;

    CosmosDiagnosticsContext(
        String spanName,
        String accountName,
        String databaseName,
        String collectionName,
        ResourceType resourceType,
        OperationType operationType,
        ConsistencyLevel consistencyLevel,
        Integer maxItemCount) {

        checkNotNull(spanName, "Argument 'spanName' must not be null.");
        checkNotNull(accountName, "Argument 'accountName' must not be null.");
        checkNotNull(databaseName, "Argument 'databaseName' must not be null.");
        checkNotNull(collectionName, "Argument 'collectionName' must not be null.");
        checkNotNull(resourceType, "Argument 'resourceType' must not be null.");
        checkNotNull(operationType, "Argument 'operationType' must not be null.");
        checkNotNull(consistencyLevel, "Argument 'consistencyLevel' must not be null.");

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
    }

    public String getAccountName() {
        return this.accountName;
    }

    public String getDatabaseName() {
        return this.databaseName;
    }

    public String getCollectionName() {
        return this.collectionName;
    }

    public String getResourceType() {
        return this.resourceTypeString;
    }

    ResourceType getResourceTypeInternal() {
        return this.resourceType;
    }

    public String getOperationType() {
        return this.operationTypeString;
    }

    OperationType getOperationTypeInternal() {
        return this.operationType;
    }

    public ConsistencyLevel getConsistencyLevel() {
        return this.consistencyLevel;
    }

    public Integer getMaxItemCount() {
        return this.maxItemCount;
    }

    public Integer getActualItemCount() {
        return this.actualItemCount;
    }

    public String getSpanName() {
        return this.spanName;
    }

    public void addDiagnostics(Collection<CosmosDiagnostics> cosmosDiagnostics) {
        checkNotNull(cosmosDiagnostics, "Argument 'cosmosDiagnostics' must not be null.");
        for (CosmosDiagnostics d: cosmosDiagnostics) {
            this.addDiagnostics(d);
        }
    }

    public void addDiagnostics(CosmosDiagnostics cosmosDiagnostics) {
        checkNotNull(cosmosDiagnostics, "Argument 'cosmosDiagnostics' must not be null.");
        this.addRequestSize(diagAccessor.getRequestPayloadSizeInBytes(cosmosDiagnostics));
        this.addResponseSize(diagAccessor.getTotalResponsePayloadSizeInBytes(cosmosDiagnostics));
        this.diagnostics.add(cosmosDiagnostics);
    }

    public int getStatusCode() {
        return this.statusCode;
    }

    public int getSubStatusCode() {
        return this.subStatusCode;
    }

    public Throwable getFinalError() {
        return this.finalError;
    }

    public int getMaxRequestPayloadSizeInBytes() {
        return this.maxRequestSize;
    }

    public int getMaxResponsePayloadSizeInBytes() {
        return this.maxResponseSize;
    }

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

    public Collection<CosmosDiagnostics> getDiagnostics() {
        return this.diagnostics;
    }

    public boolean hasCompleted() {
        return this.duration != null;
    }

    Duration getDuration() {
        return this.duration;
    }

    void startOperation() {
        checkState(
            this.startTime == null,
            "Method 'startOperation' must not be called multiple times.");
        this.startTime = Instant.now();
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
    }

    String getRequestDiagnostics() {
        // @TODO implement
        return "";
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
                                                           ConsistencyLevel consistencyLevel, Integer maxItemCount) {

                        return new CosmosDiagnosticsContext(
                            spanName,
                            account,
                            databaseId,
                            containerId,
                            resourceType,
                            operationType,
                            consistencyLevel,
                            maxItemCount);
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
