// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.implementation;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Map;

import com.azure.ai.agents.models.JobStatus;
import com.azure.ai.agents.models.MemoryStoreUpdateCompletedResult;
import com.azure.ai.agents.models.MemoryStoreUpdateStatus;
import com.azure.core.util.BinaryData;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.PollingContext;
import com.azure.core.util.serializer.TypeReference;

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

    /**
     * Remaps a {@link PollResponse} whose status may contain a custom service terminal state
     * ({@code "completed"}, {@code "superseded"}) that the base {@code OperationResourcePollingStrategy}
     * cannot recognize.  If no remapping is needed the original response is returned as-is.
     *
     * <p>The Memory Stores service defines:</p>
     * <ul>
     *   <li>{@code "completed"}  {@link LongRunningOperationStatus#SUCCESSFULLY_COMPLETED}</li>
     *   <li>{@code "superseded"} {@link LongRunningOperationStatus#USER_CANCELLED}</li>
     * </ul>
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
        }
        return status;
    }

    private static LongRunningOperationStatus mapStatus(Object statusValue) {
        if (statusValue == null || CoreUtils.isNullOrEmpty(statusValue.toString().trim())) {
            return LongRunningOperationStatus.IN_PROGRESS;
        }
        String status = statusValue.toString().trim();
        if (JobStatus.QUEUED.toString().equalsIgnoreCase(status)
            || JobStatus.IN_PROGRESS.toString().equalsIgnoreCase(status)) {
            return LongRunningOperationStatus.IN_PROGRESS;
        } else if (JobStatus.SUCCEEDED.toString().equalsIgnoreCase(status)
            || MemoryStoreUpdateStatus.COMPLETED.toString().equalsIgnoreCase(status)) {
            return LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;
        } else if (JobStatus.FAILED.toString().equalsIgnoreCase(status)) {
            return LongRunningOperationStatus.FAILED;
        } else if (JobStatus.CANCELLED.toString().equalsIgnoreCase(status)
            || MemoryStoreUpdateStatus.SUPERSEDED.toString().equalsIgnoreCase(status)) {
            return LongRunningOperationStatus.USER_CANCELLED;
        }
        return LongRunningOperationStatus.fromString(status, false);
    }
}
