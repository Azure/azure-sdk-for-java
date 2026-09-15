// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.implementation;

import com.azure.ai.agents.models.JobStatus;
import com.azure.ai.agents.models.MemoryStoreUpdateCompletedResult;
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
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Map;
import reactor.core.publisher.Mono;

/**
 * Shared polling helpers for the Agents SDK.
 *
 * <p>The generated {@code OperationLocationPollingStrategy} / {@code SyncOperationLocationPollingStrategy}
 * delegate here so that the two strategies stay in sync and only minimal edits are needed in the
 * generated files.</p>
 *
 * <p>This implementation class is not part of the public API.</p>
 */
public final class AgentsServicePollUtils {
    private static final ClientLogger LOGGER = new ClientLogger(AgentsServicePollUtils.class);
    private static final String RESOURCE_LOCATION = "resourceLocation";

    private AgentsServicePollUtils() {
    }

    /**
     * Resumes a job using its existing GET operation.
     * @param getResponse status retrieval.
     * @param pollType status model type.
     * @param resultType final result type.
     * @param <T> status type.
     * @param <U> result type.
     * @return a synchronous poller that does not create a new job.
     */
    public static <T, U> com.azure.core.util.polling.SyncPoller<T, U> resume(
        java.util.function.Supplier<com.azure.core.http.rest.Response<BinaryData>> getResponse, Class<T> pollType,
        Class<U> resultType) {
        java.util.function.Function<PollingContext<T>, PollResponse<T>> poll = context -> {
            com.azure.core.http.rest.Response<BinaryData> response = getResponse.get();
            BinaryData body = response.getValue();
            context.setData(PollingUtils.POLL_RESPONSE_BODY, body.toString());
            return new PollResponse<>(mapStatus(body.toObject(Map.class).get("status")), body.toObject(pollType),
                PollingUtils.getRetryAfterFromHeaders(response.getHeaders(), OffsetDateTime::now));
        };
        return com.azure.core.util.polling.SyncPoller.createPoller(Duration.ofSeconds(1), poll, poll,
            (context, response) -> {
                throw new UnsupportedOperationException("Use the job cancellation API.");
            }, context -> resumedResult(context, resultType));
    }

    /**
     * Resumes a job using its existing asynchronous GET operation.
     * @param getResponse status retrieval.
     * @param pollType status model type.
     * @param resultType final result type.
     * @param <T> status type.
     * @param <U> result type.
     * @return an asynchronous poller that does not create a new job.
     */
    public static <T, U> com.azure.core.util.polling.PollerFlux<T, U> resumeAsync(
        java.util.function.Supplier<Mono<com.azure.core.http.rest.Response<BinaryData>>> getResponse, Class<T> pollType,
        Class<U> resultType) {
        java.util.function.Function<PollingContext<T>, Mono<PollResponse<T>>> poll
            = context -> Mono.defer(getResponse).map(response -> {
                BinaryData body = response.getValue();
                context.setData(PollingUtils.POLL_RESPONSE_BODY, body.toString());
                return new PollResponse<>(mapStatus(body.toObject(Map.class).get("status")), body.toObject(pollType),
                    PollingUtils.getRetryAfterFromHeaders(response.getHeaders(), OffsetDateTime::now));
            });
        return new com.azure.core.util.polling.PollerFlux<>(Duration.ofSeconds(1),
            context -> poll.apply(context).map(PollResponse::getValue), poll,
            (context, response) -> Mono.error(new UnsupportedOperationException("Use the job cancellation API.")),
            context -> Mono.fromCallable(() -> resumedResult(context, resultType)));
    }

    private static <U> U resumedResult(PollingContext<?> context, Class<U> resultType) {
        if (context.getLatestResponse().getStatus() != LongRunningOperationStatus.SUCCESSFULLY_COMPLETED) {
            throw new com.azure.core.exception.AzureException("Long running operation failed or was cancelled.");
        }
        Map<String, Object> body = BinaryData.fromString(context.getData(PollingUtils.POLL_RESPONSE_BODY))
            .toObject(PollingUtils.POST_POLL_RESULT_TYPE_REFERENCE);
        return getFinalResultBody(body, "result", TypeReference.createInstance(resultType)).toObject(resultType);
    }

    /**
     * Extracts the final result without replacing a non-null service result.
     *
     * <p>For a missing or null memory-update result, this matches the Python memory-update poller's fallback:
     * empty operations and zero-valued usage. These are synthetic defaults, not service-reported usage.
     * The fallback uses JSON because the generated output model has a private constructor. Other result types
     * still fail when their result is absent.</p>
     *
     * @param response the final polling response body.
     * @param propertyName the final result property.
     * @param resultType the expected result type.
     * @param <U> the result type.
     * @return the service result, or the memory-update fallback when absent.
     */
    static <U> BinaryData getFinalResultBody(Map<String, Object> response, String propertyName,
        TypeReference<U> resultType) {
        Object result = response == null ? null : response.get(propertyName);
        if (result != null) {
            return BinaryData.fromObject(result);
        }
        if ("result".equals(propertyName) && MemoryStoreUpdateCompletedResult.class.equals(resultType.getJavaType())) {
            return BinaryData.fromString("{\"memory_operations\":[],\"usage\":{\"embedding_tokens\":0,"
                + "\"input_tokens\":0,\"input_tokens_details\":{\"cached_tokens\":0,\"cache_write_tokens\":0},"
                + "\"output_tokens\":0,\"output_tokens_details\":{\"reasoning_tokens\":0},\"total_tokens\":0}}");
        }
        throw LOGGER.logExceptionAsError(new com.azure.core.exception.AzureException("Cannot get final result"));
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
        if (LongRunningOperationStatus.NOT_STARTED.toString().equalsIgnoreCase(status)
            || "NotStarted".equalsIgnoreCase(status)) {
            return LongRunningOperationStatus.NOT_STARTED;
        } else if (JobStatus.QUEUED.toString().equalsIgnoreCase(status)
            || JobStatus.IN_PROGRESS.toString().equalsIgnoreCase(status)
            || LongRunningOperationStatus.IN_PROGRESS.toString().equalsIgnoreCase(status)
            || "InProgress".equalsIgnoreCase(status)
            || "Running".equalsIgnoreCase(status)) {
            return LongRunningOperationStatus.IN_PROGRESS;
        } else if (JobStatus.SUCCEEDED.toString().equalsIgnoreCase(status)
            || MemoryStoreUpdateStatus.COMPLETED.toString().equalsIgnoreCase(status)
            || MemoryStoreUpdateStatus.SUPERSEDED.toString().equalsIgnoreCase(status)
            || LongRunningOperationStatus.SUCCESSFULLY_COMPLETED.toString().equalsIgnoreCase(status)) {
            return LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;
        } else if (JobStatus.FAILED.toString().equalsIgnoreCase(status)
            || LongRunningOperationStatus.FAILED.toString().equalsIgnoreCase(status)) {
            return LongRunningOperationStatus.FAILED;
        } else if (JobStatus.CANCELLED.toString().equalsIgnoreCase(status)
            || LongRunningOperationStatus.USER_CANCELLED.toString().equalsIgnoreCase(status)
            || "Canceled".equalsIgnoreCase(status)) {
            return LongRunningOperationStatus.USER_CANCELLED;
        }
        return LongRunningOperationStatus.fromString(status, false);
    }
}
