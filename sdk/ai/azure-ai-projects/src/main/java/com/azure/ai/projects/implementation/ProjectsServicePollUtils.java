// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.projects.implementation;

import com.azure.core.exception.AzureException;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.PollerFlux;
import com.azure.core.util.polling.PollingContext;
import com.azure.core.util.polling.SyncPoller;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import reactor.core.publisher.Mono;

/** Internal helpers for resuming existing Projects jobs with Azure Core pollers. */
public final class ProjectsServicePollUtils {
    private ProjectsServicePollUtils() {
    }

    /**
     * Resumes a job through its existing GET endpoint.
     * @param getResponse status retrieval.
     * @param pollType status model type.
     * @param resultType final result type.
     * @param <T> status type.
     * @param <U> result type.
     * @return the resumed sync poller.
     */
    public static <T, U> SyncPoller<T, U> resume(Supplier<Response<BinaryData>> getResponse, Class<T> pollType,
        Class<U> resultType) {
        Function<PollingContext<T>, PollResponse<T>> poll = context -> response(getResponse.get(), context, pollType);
        return SyncPoller.createPoller(Duration.ofSeconds(1), poll, poll, (context, current) -> {
            throw new UnsupportedOperationException("Use the job cancellation API.");
        }, context -> result(context, resultType));
    }

    /**
     * Resumes a job through its existing asynchronous GET endpoint.
     * @param getResponse status retrieval.
     * @param pollType status model type.
     * @param resultType final result type.
     * @param <T> status type.
     * @param <U> result type.
     * @return the resumed async poller.
     */
    public static <T, U> PollerFlux<T, U> resumeAsync(Supplier<Mono<Response<BinaryData>>> getResponse,
        Class<T> pollType, Class<U> resultType) {
        Function<PollingContext<T>, Mono<PollResponse<T>>> poll
            = context -> Mono.defer(getResponse).map(value -> response(value, context, pollType));
        return new PollerFlux<>(Duration.ofSeconds(1), context -> poll.apply(context).map(PollResponse::getValue), poll,
            (context, current) -> Mono.error(new UnsupportedOperationException("Use the job cancellation API.")),
            context -> Mono.fromCallable(() -> result(context, resultType)));
    }

    private static <T> PollResponse<T> response(Response<BinaryData> response, PollingContext<T> context,
        Class<T> type) {
        BinaryData body = response.getValue();
        context.setData(PollingUtils.POLL_RESPONSE_BODY, body.toString());
        Object rawStatus = body.toObject(Map.class).get("status");
        String status = rawStatus == null ? "in_progress" : rawStatus.toString().toLowerCase(Locale.ROOT);
        LongRunningOperationStatus mapped;
        switch (status) {
            case "succeeded":
            case "completed":
                mapped = LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;
                break;

            case "failed":
                mapped = LongRunningOperationStatus.FAILED;
                break;

            case "cancelled":
            case "canceled":
                mapped = LongRunningOperationStatus.USER_CANCELLED;
                break;

            default:
                mapped = LongRunningOperationStatus.IN_PROGRESS;
        }
        return new PollResponse<>(mapped, body.toObject(type),
            PollingUtils.getRetryAfterFromHeaders(response.getHeaders(), OffsetDateTime::now));
    }

    private static <U> U result(PollingContext<?> context, Class<U> type) {
        if (context.getLatestResponse().getStatus() != LongRunningOperationStatus.SUCCESSFULLY_COMPLETED) {
            throw new AzureException("Long running operation failed or was cancelled.");
        }
        Object result
            = BinaryData.fromString(context.getData(PollingUtils.POLL_RESPONSE_BODY)).toObject(Map.class).get("result");
        if (result == null) {
            throw new AzureException("Cannot get final result.");
        }
        return BinaryData.fromObject(result).toObject(type);
    }
}
