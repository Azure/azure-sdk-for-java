// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.ImplementationBridgeHelpers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * This class describes the thresholds when more details diagnostics are emitted for an operation due to high latency,
 * high RU consumption or high payload sizes.
 */
public final class CosmosDiagnosticsThresholds {
    private final static CosmosDiagnosticsThresholds DEFAULT;

    /**
     * The default request charge (RU) threshold to determine whether to include request diagnostics or not
     */
    public final static float DEFAULT_REQUEST_CHARGE_THRESHOLD;

    /**
     * The default latency threshold to determine whether to include request diagnostics or not for point operations
     */
    public final static Duration DEFAULT_POINT_OPERATION_LATENCY_THRESHOLD;

    /**
     * The default latency threshold to determine whether to include request diagnostics or not for non-point operations
     */
    public final static Duration DEFAULT_NON_POINT_OPERATION_LATENCY_THRESHOLD;

    /**
     * The default payload size (in bytes) threshold to determine whether to include request diagnostics or not
     */
    public final static int DEFAULT_PAYLOAD_SIZE_THRESHOLD_IN_BYTES;

    private Duration pointOperationLatencyThreshold;
    private Duration nonPointOperationLatencyThreshold;
    private float requestChargeThreshold;
    private int payloadSizeInBytesThreshold;
    private final ConcurrentLinkedQueue<StatusCodeHandling> statusCodeHandling = new ConcurrentLinkedQueue<>(
        getDefaultStatusCodeHandling()
    );

    /**
     * Creates an instance of the CosmosDiagnosticsThresholds class with default values
     */
    public CosmosDiagnosticsThresholds() {
        this.pointOperationLatencyThreshold = DEFAULT_POINT_OPERATION_LATENCY_THRESHOLD;
        this.nonPointOperationLatencyThreshold = DEFAULT_NON_POINT_OPERATION_LATENCY_THRESHOLD;
        this.requestChargeThreshold = DEFAULT_REQUEST_CHARGE_THRESHOLD;
        this.payloadSizeInBytesThreshold = DEFAULT_PAYLOAD_SIZE_THRESHOLD_IN_BYTES;
    }

    /**
     * Can be used to define custom latency thresholds. When the latency threshold is exceeded more detailed
     * diagnostics will be emitted (including the request diagnostics). There is some overhead of emitting the
     * more detailed diagnostics - so recommendation is to choose latency thresholds that reduce the noise level
     * and only emit detailed diagnostics when there is really business impact seen.
     * The default value for the point operation latency threshold is
     * {@link CosmosDiagnosticsThresholds#DEFAULT_POINT_OPERATION_LATENCY_THRESHOLD}, for non-point operations
     * {@link CosmosDiagnosticsThresholds#DEFAULT_NON_POINT_OPERATION_LATENCY_THRESHOLD}.
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
     * The default value for the request charge threshold
     * are {@link CosmosDiagnosticsThresholds#DEFAULT_REQUEST_CHARGE_THRESHOLD} RUs.
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
     * The default value for the payload size threshold are
     * {@link CosmosDiagnosticsThresholds#DEFAULT_PAYLOAD_SIZE_THRESHOLD_IN_BYTES} bytes.
     * @param bytes the threshold for the payload size in bytes
     * @return current CosmosDiagnosticsThresholds instance
     */
    public CosmosDiagnosticsThresholds setPayloadSizeThreshold(int bytes) {
        this.payloadSizeInBytesThreshold = bytes;

        return this;
    }

    /**
     * Can be used to customize the logic determining whether the outcome of an operation (based on statusCode +
     * subStatusCode) is considered a failure (and diagnostics will be emitted) or not.
     * By default all status codes >= 400 except for (404/0 - item not found,
     * 409/0 - conflict, document with same id+pk already exists, 412/0 - (etag) pre-condition failure and
     * 429/3200 - throttling due to provisioned RU exceeded) are considered failures. Those exception can happen
     * very frequently are are usually expected under certain circumstances by applications - so the noise-level for
     * emitting diagnostics would be too high.
     * Any custom rule applied via this method
     * or {@link CosmosDiagnosticsThresholds#configureStatusCodeHandling(int, Integer, boolean)} will override previous
     * rules - so, the last applicable rule will override any previous one.
     * @param minStatusCode min status code for a status code range
     * @param maxStatusCode max status code for a status code range
     * @param isFailureCondition a flag indicating whether this status code range should be considered as a failure
     * @return current CosmosDiagnosticsThresholds instance
     */
    public CosmosDiagnosticsThresholds configureStatusCodeHandling(
        int minStatusCode, int maxStatusCode, boolean isFailureCondition) {

        this.statusCodeHandling.add(
            new StatusCodeHandling(minStatusCode, maxStatusCode, null, isFailureCondition));

        return this;
    }

    /**
     * Can be used to customize the logic determining whether the outcome of an operation (based on statusCode +
     * subStatusCode) is considered a failure (and diagnostics will be emitted) or not.
     * By default all status codes >= 400 except for (404/0 - item not found,
     * 409/0 - conflict, document with same id+pk already exists, 412/0 - (etag) pre-condition failure and
     * 429/3200 - throttling due to provisioned RU exceeded) are considered failures. Those exception can happen
     * very frequently are are usually expected under certain circumstances by applications - so the noise-level for
     * emitting diagnostics would be too high.
     * Any custom rule applied via {@link CosmosDiagnosticsThresholds#configureStatusCodeHandling(int, int, boolean)}
     * or this method will override previous
     * rules - so, the last applicable rule will override any previous one.
     * @param statusCode the status code
     * @param subStatusCode the sub status code - null means all sub status codes, a specific value means this rule
     * is only applicable when status code and sub status Code match.
     * @param isFailureCondition a flag indicating whether this status code range should be considered as a failure
     * @return current CosmosDiagnosticsThresholds instance
     */
    public CosmosDiagnosticsThresholds configureStatusCodeHandling(
        int statusCode, Integer subStatusCode, boolean isFailureCondition) {

        this.statusCodeHandling.add(
            new StatusCodeHandling(statusCode, statusCode, subStatusCode, isFailureCondition));

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

    boolean isFailureCondition(int statusCode, int subStatusCode) {
        boolean isFailure = false;
        for (StatusCodeHandling s: this.statusCodeHandling) {
            if (statusCode >= s.minStatusCode &&
                statusCode <= s.maxStatusCode &&
                (s.subStatusCode == null || s.subStatusCode == subStatusCode)) {

                isFailure = s.isFailureCondition;
            }
        }

        return isFailure;
    }

    private static List<StatusCodeHandling> getDefaultStatusCodeHandling() {
        ArrayList<StatusCodeHandling> result = new ArrayList<>();
        result.add(new StatusCodeHandling(500, Integer.MAX_VALUE, null, true));
        result.add(new StatusCodeHandling(400, 499, null,  true));
        result.add(new StatusCodeHandling(404, 404, 0,  false));
        result.add(new StatusCodeHandling(409, 409, 0,  false));
        result.add(new StatusCodeHandling(412, 412, 0,  false));
        result.add(new StatusCodeHandling(429, 429, 3200,  false));

        return result;
    }

    private final static class StatusCodeHandling {
        private final int minStatusCode;
        private final int maxStatusCode;
        private final Integer subStatusCode;
        private final boolean isFailureCondition;

        public StatusCodeHandling(int minStatusCode, int maxStatusCode, Integer subStatusCode, boolean isFailureCondition) {
            this.minStatusCode = minStatusCode;
            this.maxStatusCode = maxStatusCode;
            this.isFailureCondition = isFailureCondition;
            this.subStatusCode = subStatusCode;
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    // the following helper/accessor only helps to access this class outside of this package.//
    ///////////////////////////////////////////////////////////////////////////////////////////
    static void initialize() {
        ImplementationBridgeHelpers.CosmosDiagnosticsThresholdsHelper.setCosmosDiagnosticsThresholdsAccessor(
            new ImplementationBridgeHelpers.CosmosDiagnosticsThresholdsHelper.CosmosDiagnosticsThresholdsAccessor() {

                @Override
                public Duration getPointReadLatencyThreshold(CosmosDiagnosticsThresholds thresholds) {
                    if (thresholds == null) {
                        return DEFAULT.getPointOperationLatencyThreshold();
                    }

                    checkNotNull(thresholds,"Argument 'thresholds' must not be null.");
                    return thresholds.getPointOperationLatencyThreshold();
                }

                @Override
                public Duration getNonPointReadLatencyThreshold(CosmosDiagnosticsThresholds thresholds) {
                    if (thresholds == null) {
                        return DEFAULT.getNonPointOperationLatencyThreshold();
                    }

                    checkNotNull(thresholds,"Argument 'thresholds' must not be null.");
                    return thresholds.getNonPointOperationLatencyThreshold();
                }

                @Override
                public float getRequestChargeThreshold(CosmosDiagnosticsThresholds thresholds) {
                    if (thresholds == null) {
                        return DEFAULT.getRequestChargeThreshold();
                    }

                    checkNotNull(thresholds,"Argument 'thresholds' must not be null.");
                    return thresholds.getRequestChargeThreshold();
                }

                @Override
                public int getPayloadSizeThreshold(CosmosDiagnosticsThresholds thresholds) {
                    if (thresholds == null) {
                        return DEFAULT.getPayloadSizeThreshold();
                    }

                    checkNotNull(thresholds,"Argument 'thresholds' must not be null.");
                    return thresholds.getPayloadSizeThreshold();
                }

                @Override
                public boolean isFailureCondition(
                    CosmosDiagnosticsThresholds thresholds, int statusCode, int subStatusCode) {

                    if (thresholds == null) {
                        return DEFAULT.isFailureCondition(statusCode, subStatusCode);
                    }

                    checkNotNull(thresholds,"Argument 'thresholds' must not be null.");
                    return thresholds.isFailureCondition(statusCode, subStatusCode);
                }
            }
        );
    }

    static {

        DEFAULT = new CosmosDiagnosticsThresholds();
        DEFAULT_REQUEST_CHARGE_THRESHOLD = 1000;
        DEFAULT_POINT_OPERATION_LATENCY_THRESHOLD = Duration.ofSeconds(1);
        DEFAULT_NON_POINT_OPERATION_LATENCY_THRESHOLD = Duration.ofSeconds(3);
        DEFAULT_PAYLOAD_SIZE_THRESHOLD_IN_BYTES = Integer.MAX_VALUE;

        initialize(); }
}
