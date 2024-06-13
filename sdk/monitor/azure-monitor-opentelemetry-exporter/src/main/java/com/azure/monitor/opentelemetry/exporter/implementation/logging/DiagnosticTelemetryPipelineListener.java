// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.logging;

import com.azure.core.util.logging.ClientLogger;
import com.azure.monitor.opentelemetry.exporter.implementation.pipeline.TelemetryPipeline;
import com.azure.monitor.opentelemetry.exporter.implementation.pipeline.TelemetryPipelineListener;
import com.azure.monitor.opentelemetry.exporter.implementation.pipeline.TelemetryPipelineRequest;
import com.azure.monitor.opentelemetry.exporter.implementation.pipeline.TelemetryPipelineResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.sdk.common.CompletableResultCode;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static com.azure.monitor.opentelemetry.exporter.implementation.utils.AzureMonitorMsgId.INGESTION_ERROR;
import static java.util.Collections.singleton;

public class DiagnosticTelemetryPipelineListener implements TelemetryPipelineListener {

    private static final Class<?> FOR_CLASS = TelemetryPipeline.class;
    private static final ClientLogger logger = new ClientLogger(FOR_CLASS);

    // share this across multiple pipelines
    private static final AtomicBoolean friendlyExceptionThrown = new AtomicBoolean();

    private final OperationLogger operationLogger;
    private final boolean logRetryableFailures;
    private final String retryableFailureSuffix;

    // e.g. "Sending telemetry to the ingestion service"
    public DiagnosticTelemetryPipelineListener(
        String operation, boolean logRetryableFailures, String retryableFailureSuffix) {
        operationLogger = new OperationLogger(FOR_CLASS, operation);
        this.logRetryableFailures = logRetryableFailures;
        this.retryableFailureSuffix = retryableFailureSuffix;
    }

    @Override
    public void onResponse(TelemetryPipelineRequest request, TelemetryPipelineResponse response) {
        int responseCode = response.getStatusCode();
        switch (responseCode) {
            case 200: // SUCCESS
                operationLogger.recordSuccess();
                break;
            case 206: // PARTIAL CONTENT, Breeze-specific: PARTIAL SUCCESS
            case 400: // breeze returns if json content is bad (e.g. missing required field)
                Set<String> errors = response.getErrorMessages();
                if (!errors.isEmpty()) {
                    operationLogger.recordFailure(
                        "Received response code " + responseCode + " (" + String.join(", ", errors) + ")",
                        INGESTION_ERROR);
                }
                break;
            case 307:
            case 308:
                operationLogger.recordFailure("Too many redirects", INGESTION_ERROR);
                break;
            case 401: // breeze returns if aad enabled and no authentication token provided
            case 403: // breeze returns if aad enabled or disabled (both cases) and
                if (logRetryableFailures) {
                    operationLogger.recordFailure(
                        getErrorMessageFromCredentialRelatedResponse(responseCode, response.getBody()),
                        INGESTION_ERROR);
                }
                break;
            case 408: // REQUEST TIMEOUT
            case 429: // TOO MANY REQUESTS
            case 500: // INTERNAL SERVER ERROR
            case 502: // BAD GATEWAY
            case 503: // SERVICE UNAVAILABLE
            case 504: // GATEWAY TIMEOUT
                if (logRetryableFailures) {
                    operationLogger.recordFailure(
                        "Received response code " + responseCode + retryableFailureSuffix,
                        INGESTION_ERROR);
                }
                break;
            case 402: // Breeze-specific: New Daily Quota Exceeded
                operationLogger.recordFailure(
                    "Received response code 402 (daily quota exceeded and throttled over extended time)",
                    INGESTION_ERROR);
                break;
            case 439: // Breeze-specific: Deprecated Daily Quota Exceeded
                operationLogger.recordFailure(
                    "Received response code 439 (daily quota exceeded and throttled over extended time)",
                    INGESTION_ERROR);
                break;
            default:
                operationLogger.recordFailure("received response code: " + responseCode, INGESTION_ERROR);
        }
    }

    @Override
    public void onException(TelemetryPipelineRequest request, String reason, Throwable throwable) {
        if (!NetworkFriendlyExceptions.logSpecialOneTimeFriendlyException(
            throwable, request.getUrl().toString(), friendlyExceptionThrown, logger)) {
            if (logRetryableFailures) {
                operationLogger.recordFailure(
                    reason + retryableFailureSuffix,
                    throwable,
                    INGESTION_ERROR);
            }
        }
    }

    @Override
    public CompletableResultCode shutdown() {
        return CompletableResultCode.ofSuccess();
    }

    public static String getErrorMessageFromCredentialRelatedResponse(
        int responseCode, String responseBody) {
        JsonNode jsonNode;
        try {
            jsonNode = new ObjectMapper().readTree(responseBody);
        } catch (JsonProcessingException e) {
            return "Ingestion service returned "
                + responseCode
                + ", but could not parse response as json: "
                + responseBody;
        }
        String action =
            responseCode == 401
                ? ". Please provide Azure Active Directory credentials"
                : ". Please check your Azure Active Directory credentials, they might be incorrect or expired";
        List<JsonNode> errors = new ArrayList<>();
        jsonNode.get("errors").forEach(errors::add);
        return errors.get(0).get("message").asText()
            + action
            + " (telemetry will be stored to disk and retried)";
    }
}
