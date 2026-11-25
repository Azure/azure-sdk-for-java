// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.implementation;

import com.azure.ai.agents.models.MemoryStoreUpdateStatus;
import com.azure.core.exception.AzureException;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.PollingContext;
import com.azure.core.util.serializer.TypeReference;
import java.util.Map;

/**
 * Shared polling helpers for the Agents SDK.
 *
 * <p>The generated {@code OperationLocationPollingStrategy} / {@code SyncOperationLocationPollingStrategy}
 * delegate here so that the two strategies stay in sync and only minimal edits are needed in the
 * generated files.</p>
 *
 * <p>This class is package-private; it is <b>not</b> part of the public API.</p>
 */
final class AgentsServicePollUtils {
    private static final ClientLogger LOGGER = new ClientLogger(AgentsServicePollUtils.class);
    private static final HttpHeaderName OPERATION_ID = HttpHeaderName.fromString("Operation-Id");
    private static final String CAMPAIGN_OPERATION_ID = "campaignOperationId";

    private static final TypeReference<Map<String, Object>> MAP_TYPE_REFERENCE
        = new TypeReference<Map<String, Object>>() {
        };

    private AgentsServicePollUtils() {
    }

    static String getOptimizationPollingUrl(Object initialResponse, String endpoint) {
        if (!(initialResponse instanceof BinaryData)) {
            throw new IllegalStateException("The optimization create response did not contain a response body.");
        }
        Map<String, Object> job = ((BinaryData) initialResponse).toObject(MAP_TYPE_REFERENCE);
        Object jobId = job.get("id");
        if (jobId == null || jobId.toString().isEmpty()) {
            throw new IllegalStateException("The optimization create response did not contain a job ID.");
        }
        return endpoint + (endpoint.endsWith("/") ? "" : "/") + "agent_optimization_jobs/" + jobId;
    }

    static boolean canPollCampaign(Response<?> response) {
        int statusCode = response.getStatusCode();
        return statusCode == 200
            || statusCode == 201
            || statusCode == 202
            || (statusCode == 204 && response.getRequest().getHttpMethod() == HttpMethod.DELETE);
    }

    static boolean initializeCampaignPollingContext(Response<?> response, PollingContext<?> pollingContext) {
        HttpMethod method = response.getRequest().getHttpMethod();
        pollingContext.setData(PollingUtils.HTTP_METHOD, method.name());
        if (method == HttpMethod.DELETE && response.getStatusCode() == 204) {
            return true;
        }
        int statusCode = response.getStatusCode();
        if (statusCode != 200 && statusCode != 201 && statusCode != 202) {
            throw LOGGER.logExceptionAsError(
                new AzureException("Campaign operation failed with HTTP status " + statusCode + "."));
        }
        String pollingUrl = response.getHeaders().getValue(PollingUtils.OPERATION_LOCATION_HEADER);
        if (pollingUrl == null || pollingUrl.isEmpty()) {
            throw LOGGER.logExceptionAsError(
                new IllegalStateException("Campaign operation response is missing Operation-Location."));
        }
        if (method != HttpMethod.DELETE) {
            String operationId = response.getHeaders().getValue(OPERATION_ID);
            if (operationId == null || operationId.isEmpty()) {
                throw LOGGER.logExceptionAsError(
                    new IllegalStateException("Campaign operation response is missing Operation-Id."));
            }
            pollingContext.setData(CAMPAIGN_OPERATION_ID, operationId);
        }
        return false;
    }

    static boolean isCompletedCampaignDeletion(PollingContext<?> pollingContext) {
        LongRunningOperationStatus status = pollingContext.getLatestResponse().getStatus();
        if (status == LongRunningOperationStatus.FAILED) {
            throw LOGGER.logExceptionAsError(new AzureException("Long running operation failed."));
        } else if (status == LongRunningOperationStatus.USER_CANCELLED) {
            throw LOGGER.logExceptionAsError(new AzureException("Long running operation cancelled."));
        } else if (status != LongRunningOperationStatus.SUCCESSFULLY_COMPLETED) {
            throw LOGGER
                .logExceptionAsError(new IllegalStateException("Campaign operation has not completed successfully."));
        }
        return HttpMethod.DELETE.name().equals(pollingContext.getData(PollingUtils.HTTP_METHOD));
    }

    static RequestOptions getCampaignFinalRequestOptions(PollingContext<?> pollingContext, Context context) {
        String operationId = pollingContext.getData(CAMPAIGN_OPERATION_ID);
        if (operationId == null || operationId.isEmpty()) {
            throw LOGGER.logExceptionAsError(
                new IllegalStateException("Campaign polling context is missing the original Operation-Id."));
        }
        return new RequestOptions().setContext(context).addQueryParam("operation_id", operationId);
    }

    static BinaryData getCampaignResponseBody(Response<BinaryData> response) {
        BinaryData body = response.getValue();
        if (body == null || (body.getLength() != null && body.getLength() == 0)) {
            throw LOGGER
                .logExceptionAsError(new IllegalStateException("Campaign final GET did not contain a response body."));
        }
        return body;
    }

    /**
     * Remaps a {@link PollResponse} whose status may contain a custom service terminal state
     * ({@code "completed"}, {@code "superseded"}, {@code "cancelled"}, {@code "skipped"})
     * that the base {@code OperationResourcePollingStrategy}
     * cannot recognize.  If no remapping is needed the original response is returned as-is.
     *
     * <p>The Memory Stores service defines:</p>
     * <ul>
     *   <li>{@code "completed"}  {@link LongRunningOperationStatus#SUCCESSFULLY_COMPLETED}</li>
     *   <li>{@code "superseded"} {@link LongRunningOperationStatus#USER_CANCELLED}</li>
     * </ul>
     * <p>Optimization jobs that are {@code "skipped"} map to {@link LongRunningOperationStatus#FAILED},
     * because they terminate without completing the requested optimization.</p>
     */
    static <T> PollResponse<T> remapStatus(PollResponse<T> response) {
        LongRunningOperationStatus status = response.getStatus();
        LongRunningOperationStatus mapped = mapCustomStatus(status);
        if (mapped == status) {
            return response;
        }
        return new PollResponse<>(mapped, response.getValue(), response.getRetryAfter());
    }

    private static LongRunningOperationStatus mapCustomStatus(LongRunningOperationStatus status) {
        // Standard statuses (Succeeded, Failed, Canceled, InProgress, NotStarted) are already
        // mapped correctly by the parent's PollResult; only remap the custom ones.
        String name = status.toString();
        if (MemoryStoreUpdateStatus.COMPLETED.toString().equalsIgnoreCase(name)) {
            return LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;
        } else if (MemoryStoreUpdateStatus.SUPERSEDED.toString().equalsIgnoreCase(name)
            // Optimization jobs and telephony use "cancelled"; MemoryStoreUpdateStatus intentionally has no CANCELLED.
            || "cancelled".equalsIgnoreCase(name)) {
            return LongRunningOperationStatus.USER_CANCELLED;
        } else if ("skipped".equalsIgnoreCase(name)) {
            return LongRunningOperationStatus.FAILED;
        }
        return status;
    }
}
