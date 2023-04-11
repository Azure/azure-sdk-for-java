// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.function.BiPredicate;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * This class describes the thresholds when more details diagnostics are emitted for an operation due to high latency,
 * high RU consumption or high payload sizes.
 */
public final class CosmosDiagnosticsThresholds {
    private final static Logger LOGGER = LoggerFactory.getLogger(CosmosDiagnosticsThresholds.class);

     /**
     * The default request charge (RU) threshold to determine whether to include request diagnostics or not
     */
    public final static float DEFAULT_REQUEST_CHARGE_THRESHOLD = 1000;

    /**
     * The default latency threshold to determine whether to include request diagnostics or not for point operations
     */
    public final static Duration DEFAULT_POINT_OPERATION_LATENCY_THRESHOLD = Duration.ofSeconds(1);

    /**
     * The default latency threshold to determine whether to include request diagnostics or not for non-point operations
     */
    public final static Duration DEFAULT_NON_POINT_OPERATION_LATENCY_THRESHOLD = Duration.ofSeconds(3);

    /**
     * The default payload size (in bytes) threshold to determine whether to include request diagnostics or not
     */
    public final static int DEFAULT_PAYLOAD_SIZE_THRESHOLD_IN_BYTES = Integer.MAX_VALUE;

    private final static CosmosDiagnosticsThresholds DEFAULT = new CosmosDiagnosticsThresholds();

    private Duration pointOperationLatencyThreshold;
    private Duration nonPointOperationLatencyThreshold;
    private float requestChargeThreshold;
    private int payloadSizeInBytesThreshold;

    private BiPredicate<Integer, Integer> isFailureHandler = (statusCode, subStatusCode) -> {
        checkNotNull(statusCode, "Argument 'statusCode' must not be null." );
        checkNotNull(subStatusCode, "Argument 'subStatusCode' must not be null." );
        if (statusCode >= 500) {
            return true;
        }

        if (subStatusCode == 0 &&
            (statusCode == HttpConstants.StatusCodes.NOTFOUND ||
                statusCode == HttpConstants.StatusCodes.CONFLICT ||
                statusCode == HttpConstants.StatusCodes.PRECONDITION_FAILED)) {

            return false;
        }

        if (statusCode == 429 &&
            (subStatusCode == HttpConstants.SubStatusCodes.THROUGHPUT_CONTROL_REQUEST_RATE_TOO_LARGE ||
                subStatusCode == HttpConstants.SubStatusCodes.USER_REQUEST_RATE_TOO_LARGE)) {
            return false;
        }

        return statusCode >= 400;
    };


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
     * @return current CosmosDiagnosticsThresholds instance
     */
    public CosmosDiagnosticsThresholds setPointOperationLatencyThreshold(
        Duration pointOperationLatencyThreshold) {

        checkNotNull(
            pointOperationLatencyThreshold,
            "Argument 'pointOperationLatencyThreshold' must not be null.");

        this.pointOperationLatencyThreshold = pointOperationLatencyThreshold;

        return this;
    }

    /**
     * Can be used to define custom latency thresholds. When the latency threshold is exceeded more detailed
     * diagnostics will be emitted (including the request diagnostics). There is some overhead of emitting the
     * more detailed diagnostics - so recommendation is to choose latency thresholds that reduce the noise level
     * and only emit detailed diagnostics when there is really business impact seen.
     * The default value for the point operation latency threshold is
     * {@link CosmosDiagnosticsThresholds#DEFAULT_POINT_OPERATION_LATENCY_THRESHOLD}, for non-point operations
     * {@link CosmosDiagnosticsThresholds#DEFAULT_NON_POINT_OPERATION_LATENCY_THRESHOLD}.
     * @param nonPointOperationLatencyThreshold the latency threshold for all operations except (ReadItem, CreateItem,
     * UpsertItem, ReplaceItem, PatchItem or DeleteItem)
     * @return current CosmosDiagnosticsThresholds instance
     */
    public CosmosDiagnosticsThresholds setNonPointOperationLatencyThreshold(
        Duration nonPointOperationLatencyThreshold) {

        checkNotNull(nonPointOperationLatencyThreshold,
            "Argument 'nonPointOperationLatencyThreshold' must not be null.");

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
     * By default, all status codes >= 400 except for (404/0 - item not found,
     * 409/0 - conflict, document with same id+pk already exists, 412/0 - (etag) pre-condition failure and
     * 429/3200 - throttling due to provisioned RU exceeded) are considered failures. Those exceptions can happen
     * very frequently and are usually expected under certain circumstances by applications - so, the noise-level for
     * emitting diagnostics would be too high.
     * The first parameter will be the status code - the second parameter the subStatusCode. The returned boolean of the
     * function would indicate whether the operation should be considered as failure form a diagnostics perspective.
     * @param isFailureHandler the function that will be used to determine whether a status code/sub-status code
     * tuple should be considered a failure.
     * @return current CosmosDiagnosticsThresholds instance
     */
    public CosmosDiagnosticsThresholds setFailureHandler(BiPredicate<Integer, Integer> isFailureHandler) {
        checkNotNull(nonPointOperationLatencyThreshold,
            "Argument 'isFailureHandler' must not be null.");
        this.isFailureHandler = isFailureHandler;
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
        try {
            return this.isFailureHandler.test(statusCode, subStatusCode);
        } catch (Exception error) {
            LOGGER.error("Execution of custom isFailureHandler failed - treating operation as failure.", error);
            return false;
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

    static { initialize(); }
}
