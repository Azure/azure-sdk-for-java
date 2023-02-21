// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.clienttelemetry.CosmosMeterOptions;
import com.azure.cosmos.implementation.clienttelemetry.MetricCategory;
import com.azure.cosmos.implementation.clienttelemetry.TagName;
import com.azure.cosmos.models.CosmosMetricName;
import io.micrometer.core.instrument.Tag;

import java.time.Duration;
import java.util.EnumSet;
import java.util.List;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * This class describes the thresholds when more details diagnostics are emitted for an operation due to high latency,
 * high RU consumption or high payload sizes.
 */
public final class CosmosDiagnosticsThresholds {
    private Duration pointOperationLatencyThreshold;
    private Duration nonPointOperationLatencyThreshold;
    private float requestChargeThreshold;
    private int payloadSizeInBytesThreshold;

    /**
     * Creates an instance of the CosmosDiagnosticsThresholds class with default values
     */
    public CosmosDiagnosticsThresholds() {
        this.pointOperationLatencyThreshold = Duration.ofSeconds(1);
        this.nonPointOperationLatencyThreshold = Duration.ofSeconds(3);
        this.requestChargeThreshold = 1000;
        this.payloadSizeInBytesThreshold = Integer.MAX_VALUE;
    }

    /**
     * Can be used to define custom latency thresholds. When the latency threshold is exceeded more detailed
     * diagnostics will be emitted (including the request diagnostics). There is some overhead of emitting the
     * more detailed diagnostics - so recommendation is to choose latency thresholds that reduce the noise level
     * and only emit detailed diagnostics when there is really business impact seen.
     * @param pointOperationLatencyThreshold the latency threshold for point operations (ReadItem, CreateItem,
     * UpsertItem, ReplaceItem, PatchItem or DeleteItem)
     * @param nonPointOperationLatencyThreshold the latency threshold for all other operations. The latency threshold
     * for these will usually be significantly higher because responses to bulk, change feed or query can contain large
     * payloads.
     * @return current CosmosDiagnosticsThresholds instance
     */
    public CosmosDiagnosticsThresholds configureLatencyThresholds(
        Duration pointOperationLatencyThreshold,
        Duration nonPointOperationLatencyThreshold) {

        checkNotNull(
            pointOperationLatencyThreshold,
            "Argument 'pointOperationLatencyThreshold' must not be null.");
        checkNotNull(nonPointOperationLatencyThreshold,
            "Argument 'nonPointOperationLatencyThreshold' must not be null.");

        this.pointOperationLatencyThreshold = pointOperationLatencyThreshold;
        this.nonPointOperationLatencyThreshold = nonPointOperationLatencyThreshold;

        return this;
    }

    /**
     * Can be used to define a custom RU (request charge) threshold. When the threshold is exceeded more detailed
     * diagnostics will be emitted (including the request diagnostics). There is some overhead of emitting the
     * more detailed diagnostics - so recommendation is to choose a request charge threshold that reduces the noise
     * level and only emits detailed diagnostics when the request charge is significantly  higher thane expected.
     * @param requestChargeThreshold The total request charge threshold for an operation. When this threshold is
     * exceeded for an operation the corresponding detailed diagnostics will be emitted.
     * @return current CosmosDiagnosticsThresholds instance
     */
    public CosmosDiagnosticsThresholds setRequestChargeThreshold(float requestChargeThreshold) {
        this.requestChargeThreshold = requestChargeThreshold;

        return this;
    }

    /**
     * Can be used to define a payload size threshold. When the threshold is exceeded for either request or
     * response payloads more detailed diagnostics will be emitted (including the request diagnostics).
     * There is some overhead of emitting the more detailed diagnostics - so recommendation is to choose a
     * payload size threshold that reduces the noise level and only emits detailed diagnostics when the payload size
     * is significantly higher than expected.
     * @param bytes the threshold for the payload size in bytes
     * @return current CosmosDiagnosticsThresholds instance
     */
    public CosmosDiagnosticsThresholds setPayloadSizeThreshold(int bytes) {
        this.payloadSizeInBytesThreshold = bytes;

        return this;
    }

    Duration getPointOperationLatencyThreshold() {
        return this.pointOperationLatencyThreshold;
    }

    Duration getNonPointOperationLatencyThreshold() {
        return this.nonPointOperationLatencyThreshold;
    }

    float getRequestChargeThreshold() {
        return this.requestChargeThreshold;
    }

    int getPayloadSizeThreshold() {
        return this.payloadSizeInBytesThreshold;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    // the following helper/accessor only helps to access this class outside of this package.//
    ///////////////////////////////////////////////////////////////////////////////////////////
    static void initialize() {
        ImplementationBridgeHelpers.CosmosDiagnosticsThresholdsHelper.setCosmosDiagnosticsThresholdsAccessor(
            new ImplementationBridgeHelpers.CosmosDiagnosticsThresholdsHelper.CosmosDiagnosticsThresholdsAccessor() {

                @Override
                public Duration getPointReadLatencyThreshold(CosmosDiagnosticsThresholds thresholds) {
                    checkNotNull(thresholds,"Argument 'thresholds' must not be null.");
                    return thresholds.getPointOperationLatencyThreshold();
                }

                @Override
                public Duration getNonPointReadLatencyThreshold(CosmosDiagnosticsThresholds thresholds) {
                    checkNotNull(thresholds,"Argument 'thresholds' must not be null.");
                    return thresholds.getNonPointOperationLatencyThreshold();
                }

                @Override
                public float getRequestChargeThreshold(CosmosDiagnosticsThresholds thresholds) {
                    checkNotNull(thresholds,"Argument 'thresholds' must not be null.");
                    return thresholds.getRequestChargeThreshold();
                }

                @Override
                public int getPayloadSizeThreshold(CosmosDiagnosticsThresholds thresholds) {
                    checkNotNull(thresholds,"Argument 'thresholds' must not be null.");
                    return thresholds.getPayloadSizeThreshold();
                }
            }
        );
    }

    static { initialize(); }
}
