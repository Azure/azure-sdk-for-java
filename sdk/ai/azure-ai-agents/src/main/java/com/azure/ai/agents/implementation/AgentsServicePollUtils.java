// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.implementation;

import com.azure.ai.agents.models.JobStatus;
import com.azure.ai.agents.models.MemoryStoreUpdateStatus;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.UrlBuilder;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.PollingContext;
import com.azure.core.util.polling.PollingStrategyOptions;
import com.azure.core.util.serializer.ObjectSerializer;
import com.azure.core.util.serializer.TypeReference;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Collections;
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
    private static final String RESOURCE_LOCATION = "resourceLocation";

    private AgentsServicePollUtils() {
    }

    static <T> Mono<PollResponse<T>> poll(PollingStrategyOptions options, ObjectSerializer serializer, String endpoint,
        PollingContext<T> pollingContext, TypeReference<T> pollResponseType) {
        HttpRequest request = new HttpRequest(HttpMethod.GET, getPollUrl(options, pollingContext));
        Context context = options.getContext() == null ? Context.NONE : options.getContext();
        return FluxUtil
            .withContext(subscriberContext -> options.getHttpPipeline()
                .send(request, CoreUtils.mergeContexts(subscriberContext, context)))
            .flatMap(response -> response.getBodyAsByteArray()
                .defaultIfEmpty(new byte[0])
                .flatMap(bytes -> createPollResponse(BinaryData.fromBytes(bytes), response, serializer, endpoint,
                    pollingContext, pollResponseType)));
    }

    static <T> PollResponse<T> pollSync(PollingStrategyOptions options, ObjectSerializer serializer, String endpoint,
        PollingContext<T> pollingContext, TypeReference<T> pollResponseType) {
        HttpRequest request = new HttpRequest(HttpMethod.GET, getPollUrl(options, pollingContext));
        Context context = options.getContext() == null ? Context.NONE : options.getContext();
        try (HttpResponse response = options.getHttpPipeline().sendSync(request, context)) {
            byte[] bytes = response.getBodyAsByteArray().defaultIfEmpty(new byte[0]).block();
            return createPollResponseSync(BinaryData.fromBytes(bytes), response, serializer, endpoint, pollingContext,
                pollResponseType);
        }
    }

    private static <T> Mono<PollResponse<T>> createPollResponse(BinaryData responseBody, HttpResponse response,
        ObjectSerializer serializer, String endpoint, PollingContext<T> pollingContext,
        TypeReference<T> pollResponseType) {
        Duration retryAfter = PollingUtils.getRetryAfterFromHeaders(response.getHeaders(), OffsetDateTime::now);
        if (responseBody.getLength() == 0) {
            return Mono.just(new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS, null, retryAfter));
        }

        return PollingUtils.deserializeResponse(responseBody, serializer, PollingUtils.POST_POLL_RESULT_TYPE_REFERENCE)
            .defaultIfEmpty(Collections.emptyMap())
            .flatMap(pollResult -> {
                updatePollingContext(pollingContext, pollResult, responseBody, endpoint);
                LongRunningOperationStatus status = mapStatus(pollResult.get("status"));
                return PollingUtils.deserializeResponse(responseBody, serializer, pollResponseType)
                    .map(value -> new PollResponse<>(status, value, retryAfter))
                    .defaultIfEmpty(new PollResponse<>(status, null, retryAfter));
            });
    }

    private static <T> PollResponse<T> createPollResponseSync(BinaryData responseBody, HttpResponse response,
        ObjectSerializer serializer, String endpoint, PollingContext<T> pollingContext,
        TypeReference<T> pollResponseType) {
        Duration retryAfter = PollingUtils.getRetryAfterFromHeaders(response.getHeaders(), OffsetDateTime::now);
        if (responseBody.getLength() == 0) {
            return new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS, null, retryAfter);
        }

        Map<String, Object> pollResult = PollingUtils.deserializeResponseSync(responseBody, serializer,
            PollingUtils.POST_POLL_RESULT_TYPE_REFERENCE);
        if (pollResult == null) {
            pollResult = Collections.emptyMap();
        }
        updatePollingContext(pollingContext, pollResult, responseBody, endpoint);
        LongRunningOperationStatus status = mapStatus(pollResult.get("status"));
        T value = PollingUtils.deserializeResponseSync(responseBody, serializer, pollResponseType);
        return new PollResponse<>(status, value, retryAfter);
    }

    private static String getPollUrl(PollingStrategyOptions options, PollingContext<?> pollingContext) {
        String url = pollingContext.getData(PollingUtils.OPERATION_LOCATION_HEADER.getCaseSensitiveName());
        if (!CoreUtils.isNullOrEmpty(options.getServiceVersion())) {
            UrlBuilder urlBuilder = UrlBuilder.parse(url);
            urlBuilder.setQueryParameter("api-version", options.getServiceVersion());
            url = urlBuilder.toString();
        }
        return url;
    }

    private static void updatePollingContext(PollingContext<?> pollingContext, Map<String, Object> pollResult,
        BinaryData responseBody, String endpoint) {
        pollingContext.setData(PollingUtils.POLL_RESPONSE_BODY, responseBody.toString());
        Object resourceLocation = pollResult.get("resourceLocation");
        if (resourceLocation instanceof String) {
            pollingContext.setData(RESOURCE_LOCATION,
                PollingUtils.getAbsolutePath((String) resourceLocation, endpoint, LOGGER));
        }
    }

    /**
     * Remaps a {@link PollResponse} whose status may contain a custom service status. If no remapping is needed the
     * original response is returned as-is.
     */
    static <T> PollResponse<T> remapStatus(PollResponse<T> response) {
        LongRunningOperationStatus status = response.getStatus();
        LongRunningOperationStatus mapped = mapStatus(status);
        if (mapped == status) {
            return response;
        }
        return new PollResponse<>(mapped, response.getValue(), response.getRetryAfter());
    }

    private static LongRunningOperationStatus mapStatus(Object statusValue) {
        if (statusValue == null || CoreUtils.isNullOrEmpty(statusValue.toString().trim())) {
            return LongRunningOperationStatus.IN_PROGRESS;
        }
        if (statusValue == LongRunningOperationStatus.NOT_STARTED
            || statusValue == LongRunningOperationStatus.IN_PROGRESS
            || statusValue == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED
            || statusValue == LongRunningOperationStatus.FAILED
            || statusValue == LongRunningOperationStatus.USER_CANCELLED) {
            return (LongRunningOperationStatus) statusValue;
        }

        String status = statusValue.toString().trim();
        if (JobStatus.QUEUED.toString().equalsIgnoreCase(status)) {
            return LongRunningOperationStatus.IN_PROGRESS;
        } else if (LongRunningOperationStatus.NOT_STARTED.toString().equalsIgnoreCase(status)
            || "NotStarted".equalsIgnoreCase(status)) {
            return LongRunningOperationStatus.NOT_STARTED;
        } else if (JobStatus.IN_PROGRESS.toString().equalsIgnoreCase(status)
            || LongRunningOperationStatus.IN_PROGRESS.toString().equalsIgnoreCase(status)
            || "InProgress".equalsIgnoreCase(status)
            || "Running".equalsIgnoreCase(status)) {
            return LongRunningOperationStatus.IN_PROGRESS;
        } else if (JobStatus.SUCCEEDED.toString().equalsIgnoreCase(status)
            || MemoryStoreUpdateStatus.COMPLETED.toString().equalsIgnoreCase(status)
            || LongRunningOperationStatus.SUCCESSFULLY_COMPLETED.toString().equalsIgnoreCase(status)) {
            return LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;
        } else if (JobStatus.FAILED.toString().equalsIgnoreCase(status)
            || LongRunningOperationStatus.FAILED.toString().equalsIgnoreCase(status)) {
            return LongRunningOperationStatus.FAILED;
        } else if (JobStatus.CANCELLED.toString().equalsIgnoreCase(status)
            || MemoryStoreUpdateStatus.SUPERSEDED.toString().equalsIgnoreCase(status)
            || LongRunningOperationStatus.USER_CANCELLED.toString().equalsIgnoreCase(status)
            || "Canceled".equalsIgnoreCase(status)) {
            return LongRunningOperationStatus.USER_CANCELLED;
        }
        return LongRunningOperationStatus.fromString(status, false);
    }
}
