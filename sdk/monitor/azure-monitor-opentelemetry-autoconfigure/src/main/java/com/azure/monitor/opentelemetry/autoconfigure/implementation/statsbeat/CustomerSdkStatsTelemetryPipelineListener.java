// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure.implementation.statsbeat;

import com.azure.core.util.logging.ClientLogger;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.pipeline.TelemetryPipelineListener;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.pipeline.TelemetryPipelineRequest;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.pipeline.TelemetryPipelineResponse;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.utils.StatusCode;
import io.opentelemetry.sdk.common.CompletableResultCode;
import reactor.util.annotation.Nullable;

import java.util.Map;

/**
 * TelemetryPipelineListener that observes HTTP responses and exceptions from the telemetry
 * pipeline, recording item-level success/drop/retry counts into the CustomerSdkStats accumulator.
 */
public class CustomerSdkStatsTelemetryPipelineListener implements TelemetryPipelineListener {

    private static final ClientLogger LOGGER = new ClientLogger(CustomerSdkStatsTelemetryPipelineListener.class);

    private final CustomerSdkStats customerSdkStats;

    public CustomerSdkStatsTelemetryPipelineListener(CustomerSdkStats customerSdkStats) {
        this.customerSdkStats = customerSdkStats;
    }

    @Override
    public void onResponse(TelemetryPipelineRequest request, TelemetryPipelineResponse response) {
        Map<String, Long> itemCountsByType = request.getItemCountsByType();
        if (itemCountsByType.isEmpty()) {
            // No item metadata (e.g. from local storage resend) — skip tracking
            LOGGER.info("CustomerSdkStats onResponse: itemCountsByType is empty, skipping.");
            return;
        }

        int statusCode = response.getStatusCode();
        LOGGER.info("CustomerSdkStats onResponse: statusCode={}, itemCountsByType={}", statusCode, itemCountsByType);

        if (statusCode == 200) {
            customerSdkStats.incrementSuccessCount(itemCountsByType);
        } else if (StatusCode.isRetryable(statusCode)) {
            // Retryable status codes: items will be retried via local storage
            String retryCode = String.valueOf(statusCode);
            String retryReason = getReasonPhraseForStatusCode(statusCode);
            customerSdkStats.incrementRetryCount(itemCountsByType, retryCode, retryReason);
        } else if (StatusCode.isRedirect(statusCode)) {
            // Redirects are handled transparently by the HTTP client; do not count
        } else {
            // Non-retryable status codes: items are dropped
            String dropCode = String.valueOf(statusCode);
            String dropReason = getReasonPhraseForStatusCode(statusCode);
            customerSdkStats.incrementDroppedCount(itemCountsByType, dropCode, dropReason,
                request.getSuccessItemCountsByType(), request.getFailureItemCountsByType());
        }
    }

    @Override
    public void onException(TelemetryPipelineRequest request, String errorMessage, Throwable throwable) {
        Map<String, Long> itemCountsByType = request.getItemCountsByType();
        if (itemCountsByType.isEmpty()) {
            return;
        }

        // Exceptions result in retry via local storage persistence
        boolean isTimeout = CustomerSdkStatsExceptionCategory.isTimeout(throwable);
        String retryCode
            = isTimeout ? CustomerSdkStats.RETRY_CODE_CLIENT_TIMEOUT : CustomerSdkStats.RETRY_CODE_CLIENT_EXCEPTION;
        String retryReason = CustomerSdkStatsExceptionCategory.categorize(throwable);
        customerSdkStats.incrementRetryCount(itemCountsByType, retryCode, retryReason);
    }

    @Override
    public CompletableResultCode shutdown() {
        return CompletableResultCode.ofSuccess();
    }

    @Nullable
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
                return "Too many requests";

            case 500:
                return "Internal server error";

            case 502:
                return "Bad gateway";

            case 503:
                return "Service unavailable";

            case 504:
                return "Gateway timeout";

            default:
                return null;
        }
    }
}
