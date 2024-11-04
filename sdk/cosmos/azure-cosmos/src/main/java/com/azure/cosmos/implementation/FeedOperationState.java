// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosDiagnosticsContext;
import com.azure.cosmos.CosmosDiagnosticsThresholds;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public abstract class FeedOperationState {

    protected static final ImplementationBridgeHelpers
        .CosmosAsyncClientHelper
        .CosmosAsyncClientAccessor clientAccessor = ImplementationBridgeHelpers
            .CosmosAsyncClientHelper
            .getCosmosAsyncClientAccessor();

    protected static final ImplementationBridgeHelpers
        .CosmosDiagnosticsContextHelper
        .CosmosDiagnosticsContextAccessor ctxAccessor = ImplementationBridgeHelpers
            .CosmosDiagnosticsContextHelper
            .getCosmosDiagnosticsContextAccessor();

    private final CosmosAsyncClient cosmosAsyncClient;
    private final CosmosDiagnosticsThresholds thresholds;
    private final AtomicReference<CosmosDiagnosticsContext> ctxHolder;
    private final AtomicReference<Runnable> diagnosticsFactoryResetCallback;
    private final AtomicReference<Consumer<CosmosDiagnosticsContext>> diagnosticsFactoryMergeCallback;
    private final AtomicReference<String> requestContinuation;
    private final AtomicReference<Integer> maxItemCount;
    private final AtomicInteger sequenceNumberGenerator;
    private final AtomicReference<Double> samplingRate;
    private final AtomicBoolean isSampledOut;
    private final CosmosPagedFluxOptions fluxOptions;

    public FeedOperationState(
        CosmosAsyncClient cosmosAsyncClient,
        String spanName,
        String dbName,
        String containerName,
        ResourceType resourceType,
        OperationType operationType,
        String operationId,
        ConsistencyLevel effectiveConsistencyLevel,
        CosmosDiagnosticsThresholds thresholds,
        CosmosPagedFluxOptions fluxOptions,
        Integer initialMaxItemCount,
        OverridableRequestOptions requestOptions
    ) {
        checkNotNull(cosmosAsyncClient, "Argument 'cosmosAsyncClient' must not be null." );
        checkNotNull(thresholds, "Argument 'thresholds' must not be null." );
        checkNotNull(effectiveConsistencyLevel, "Argument 'effectiveConsistencyLevel' must not be null." );

        this.cosmosAsyncClient = cosmosAsyncClient;
        this.thresholds = thresholds;
        this.diagnosticsFactoryResetCallback = new AtomicReference<>(null);
        this.diagnosticsFactoryMergeCallback = new AtomicReference<>(null);
        if (fluxOptions != null) {
            this.requestContinuation = new AtomicReference<>(fluxOptions.getRequestContinuation());
        } else {
            this.requestContinuation = new AtomicReference<>(null);
        }

        this.maxItemCount = new AtomicReference<>(initialMaxItemCount);
        this.sequenceNumberGenerator = new AtomicInteger(0);
        this.fluxOptions = fluxOptions;
        this.samplingRate = new AtomicReference<>(null);
        this.isSampledOut = new AtomicBoolean(false);

        CosmosDiagnosticsContext cosmosCtx = ctxAccessor.create(
            checkNotNull(spanName, "Argument 'spanName' must not be null." ),
            clientAccessor.getAccountTagValue(cosmosAsyncClient),
            BridgeInternal.getServiceEndpoint(this.cosmosAsyncClient),
            dbName,
            containerName,
            checkNotNull(resourceType, "Argument 'resourceType' must not be null." ),
            checkNotNull(operationType, "Argument 'operationType' must not be null." ),
            operationId,
            checkNotNull(effectiveConsistencyLevel, "Argument 'effectiveConsistencyLevel' must not be null." ),
            initialMaxItemCount != null ? initialMaxItemCount : Constants.Properties.DEFAULT_MAX_PAGE_SIZE,
            this.thresholds,
            null,
            clientAccessor.getConnectionMode(cosmosAsyncClient),
            clientAccessor.getUserAgent(cosmosAsyncClient),
            this.sequenceNumberGenerator.incrementAndGet(),
            fluxOptions != null ? fluxOptions.getQueryText(): null,
            requestOptions);
        this.ctxHolder = new AtomicReference<>(cosmosCtx);
    }

    public void registerDiagnosticsFactory(Runnable resetCallback, Consumer<CosmosDiagnosticsContext> mergeCallback) {
        this.diagnosticsFactoryResetCallback.set(resetCallback);
        this.diagnosticsFactoryMergeCallback.set(mergeCallback);
    }

    public Double getSamplingRateSnapshot() {
        return this.samplingRate.get();
    }

    public void setSamplingRateSnapshot(double samplingRateSnapshot, boolean isSampledOut) {
        this.samplingRate.set(samplingRateSnapshot);
        this.isSampledOut.set(isSampledOut);
        CosmosDiagnosticsContext ctxSnapshot = this.ctxHolder.get();
        ctxAccessor.setSamplingRateSnapshot(ctxSnapshot, samplingRateSnapshot, isSampledOut);
    }

    // Can return null
    public CosmosPagedFluxOptions getPagedFluxOptions() {
        return this.fluxOptions;
    }

    public void setMaxItemCount(Integer maxItemCount) {
        this.maxItemCount.set(maxItemCount);
    }

    public Integer getMaxItemCount() {
        return this.maxItemCount.get();
    }

    public String getRequestContinuation() {
        return this.requestContinuation.get();
    }

    public void setRequestContinuation(String requestContinuation) {
        this.requestContinuation.set(requestContinuation);
        if (this.fluxOptions != null) {
            this.fluxOptions.setRequestContinuation(requestContinuation);
        }
    }

    public DiagnosticsProvider getDiagnosticsProvider() {
        return clientAccessor.getDiagnosticsProvider(this.cosmosAsyncClient);
    }

    public String getSpanName() {
        return ctxAccessor.getSpanName(this.ctxHolder.get());
    }

    public CosmosDiagnosticsContext getDiagnosticsContextSnapshot() {
        return this.ctxHolder.get();
    }

    public void resetDiagnosticsContext() {
        CosmosDiagnosticsContext snapshot = this.ctxHolder.get();
        if (snapshot == null) {
            throw new IllegalStateException("CosmosDiagnosticsContext must never be null");
        }

        final CosmosDiagnosticsContext cosmosCtx = ctxAccessor.create(
            ctxAccessor.getSpanName(snapshot),
            ctxAccessor.getEndpoint(snapshot),
            BridgeInternal.getServiceEndpoint(this.cosmosAsyncClient),
            snapshot.getDatabaseName(),
            snapshot.getContainerName(),
            ctxAccessor.getResourceType(snapshot),
            ctxAccessor.getOperationType(snapshot),
            snapshot.getOperationId(),
            snapshot.getEffectiveConsistencyLevel(),
            this.maxItemCount.get(),
            this.thresholds,
            snapshot.getTrackingId(),
            snapshot.getConnectionMode(),
            snapshot.getUserAgent(),
            this.sequenceNumberGenerator.incrementAndGet(),
            fluxOptions.getQueryText(),
            ctxAccessor.getRequestOptions(snapshot)
    );
        Double samplingRateSnapshot = this.samplingRate.get();
        if (samplingRateSnapshot != null) {
            ctxAccessor.setSamplingRateSnapshot(cosmosCtx, samplingRateSnapshot, this.isSampledOut.get());
        }

        this.ctxHolder.set(cosmosCtx);

        if (this.diagnosticsFactoryResetCallback != null) {
            Runnable resetCallbackSnapshot = this.diagnosticsFactoryResetCallback.get();
            if (resetCallbackSnapshot != null) {
                resetCallbackSnapshot.run();
            }
        }
    }

    public void mergeDiagnosticsContext() {
        final CosmosDiagnosticsContext cosmosCtx = this.ctxHolder.get();

        if (this.diagnosticsFactoryMergeCallback != null) {
            Consumer<CosmosDiagnosticsContext> mergeCallbackSnapshot = this.diagnosticsFactoryMergeCallback.get();
            if (mergeCallbackSnapshot != null) {
                mergeCallbackSnapshot.accept(cosmosCtx);
            }
        }
    }
}
