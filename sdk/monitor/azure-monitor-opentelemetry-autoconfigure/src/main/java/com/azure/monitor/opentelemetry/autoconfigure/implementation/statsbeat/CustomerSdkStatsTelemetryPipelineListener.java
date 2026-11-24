// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure.implementation.statsbeat;

import com.azure.monitor.opentelemetry.autoconfigure.implementation.pipeline.TelemetryPipelineListener;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.pipeline.TelemetryPipelineRequest;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.pipeline.TelemetryPipelineResponse;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.utils.StatusCode;
import io.opentelemetry.sdk.common.CompletableResultCode;

import java.util.concurrent.ScheduledExecutorService;

/**
 * TelemetryPipelineListener that observes HTTP responses and exceptions from the telemetry
 * pipeline, recording item-level success/drop/retry counts into the CustomerSdkStats accumulator.
 */
public class CustomerSdkStatsTelemetryPipelineListener implements TelemetryPipelineListener {

    private final CustomerSdkStats customerSdkStats;
    private volatile ScheduledExecutorService scheduler;

    public CustomerSdkStatsTelemetryPipelineListener(CustomerSdkStats customerSdkStats) {
        this.customerSdkStats = customerSdkStats;
    }

    /**
     * Sets the scheduler used for periodic SDKStats export, so it can be shut down
     * when the pipeline is shut down.
     */
    public void setScheduler(ScheduledExecutorService scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public void onResponse(TelemetryPipelineRequest request, TelemetryPipelineResponse response) {
        TelemetryBatchMetadata batchMetadata = request.getTelemetryBatchMetadata();
        if (batchMetadata.isEmpty()) {
            // No item metadata (e.g. from local storage resend) — skip tracking.
            // NOTE: This means telemetry retried from disk will not be counted in SDKStats.
            // To track those, item-count metadata would need to be persisted alongside the payload.
            return;
        }

        int statusCode = response.getStatusCode();

        if (statusCode == 200 || statusCode == 206) {
            // 200 = full success; 206 = partial success (some items accepted, some rejected).
            // For 206, we count the entire batch as success because:
            //  - The majority of items were accepted by the ingestion service.
            //  - The failed items are retried from disk by LocalStorageTelemetryPipelineListener.
            //  - Disk retries carry empty metadata, so there is no double-counting risk.
            //  - Splitting counts proportionally per-type would require complex response parsing
            //    for a rare edge case.
            customerSdkStats.incrementSuccessCount(batchMetadata.getItemCountsByType());
        } else if (StatusCode.isRetryable(statusCode)) {
            // Retryable status codes: items will be retried via local storage
            String retryCode = String.valueOf(statusCode);
            String retryReason = getReasonPhraseForStatusCode(statusCode);
            customerSdkStats.incrementRetryCount(batchMetadata.getItemCountsByType(), retryCode, retryReason);
        } else if (!StatusCode.isRedirect(statusCode)) {
            // Non-redirect, non-retryable status codes: items are dropped.
            // Redirects are handled transparently by the HTTP client and are not counted.
            String dropCode = String.valueOf(statusCode);
            String dropReason = getReasonPhraseForStatusCode(statusCode);
            customerSdkStats.incrementDroppedCount(batchMetadata.getItemCountsByType(), dropCode, dropReason,
                batchMetadata.getSuccessItemCountsByType(), batchMetadata.getFailureItemCountsByType());
        }
    }

    @Override
    public void onException(TelemetryPipelineRequest request, String errorMessage, Throwable throwable) {
        TelemetryBatchMetadata batchMetadata = request.getTelemetryBatchMetadata();
        if (batchMetadata.isEmpty()) {
            return;
        }

        // Exceptions result in retry via local storage persistence
        boolean isTimeout = CustomerSdkStatsExceptionCategory.containsTimeout(throwable);
        String retryCode
            = isTimeout ? CustomerSdkStats.RETRY_CODE_CLIENT_TIMEOUT : CustomerSdkStats.RETRY_CODE_CLIENT_EXCEPTION;
        String retryReason = CustomerSdkStatsExceptionCategory.categorize(throwable);
        customerSdkStats.incrementRetryCount(batchMetadata.getItemCountsByType(), retryCode, retryReason);
    }

    @Override
    public CompletableResultCode shutdown() {
        ScheduledExecutorService exec = this.scheduler;
        if (exec != null) {
            exec.shutdown();
        }
        return CompletableResultCode.ofSuccess();
    }

    static String getReasonPhraseForStatusCode(int statusCode) {
        switch (statusCode) {
            case 400:
                return "Bad request";

            case 401:
                return "Unauthorized";

            case 402:
                return "Exceeded daily quota";

            case 403:
                return "Forbidden";

            case 404:
                return "Not found";

            case 408:
                return "Request timeout";

            case 429:
                return "Too many requests";

            case 439:
                return "Exceeded daily quota";

            case 500:
                return "Internal server error";

            case 502:
                return "Bad gateway";

            case 503:
                return "Service unavailable";

            case 504:
                return "Gateway timeout";

            default:
                return "Unknown";
        }
    }
}
