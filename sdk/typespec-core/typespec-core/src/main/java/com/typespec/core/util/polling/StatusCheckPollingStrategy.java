// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.util.polling;

import com.typespec.core.exception.AzureException;
import com.typespec.core.http.rest.Response;
import com.typespec.core.implementation.ImplUtils;
import com.typespec.core.implementation.serializer.DefaultJsonSerializer;
import com.typespec.core.util.polling.implementation.PollingUtils;
import com.typespec.core.util.serializer.ObjectSerializer;
import com.typespec.core.util.serializer.TypeReference;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.OffsetDateTime;

/**
 * Fallback polling strategy that doesn't poll but exits successfully if no other polling strategies are detected
 * and status code is 2xx.
 *
 * @param <T> the type of the response type from a polling call, or BinaryData if raw response body should be kept
 * @param <U> the type of the final result object to deserialize into, or BinaryData if raw response body should be
 *           kept
 */
public class StatusCheckPollingStrategy<T, U> implements PollingStrategy<T, U> {
    private static final ObjectSerializer DEFAULT_SERIALIZER = new DefaultJsonSerializer();

    private final ObjectSerializer serializer;

    /**
     * Creates a status check polling strategy with a JSON serializer.
     */
    public StatusCheckPollingStrategy() {
        this(DEFAULT_SERIALIZER);
    }

    /**
     * Creates a status check polling strategy with a custom object serializer.
     * @param serializer a custom serializer for serializing and deserializing polling responses
     */
    public StatusCheckPollingStrategy(ObjectSerializer serializer) {
        this.serializer = (serializer == null) ? DEFAULT_SERIALIZER : serializer;
    }

    @Override
    public Mono<Boolean> canPoll(Response<?> initialResponse) {
        return Mono.just(true);
    }

    @Override
    public Mono<PollResponse<T>> onInitialResponse(Response<?> response, PollingContext<T> pollingContext,
                                                              TypeReference<T> pollResponseType) {
        if (response.getStatusCode() == 200
                || response.getStatusCode() == 201
                || response.getStatusCode() == 202
                || response.getStatusCode() == 204) {
            Duration retryAfter = ImplUtils.getRetryAfterFromHeaders(response.getHeaders(), OffsetDateTime::now);
            return PollingUtils.convertResponse(response.getValue(), serializer, pollResponseType)
                .map(value -> new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, value, retryAfter))
                .switchIfEmpty(Mono.fromSupplier(() -> new PollResponse<>(
                    LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, null, retryAfter)));
        } else {
            return Mono.error(new AzureException("Operation failed or cancelled: " + response.getStatusCode()));
        }
    }

    @Override
    public Mono<PollResponse<T>> poll(PollingContext<T> context, TypeReference<T> pollResponseType) {
        return Mono.error(new IllegalStateException("StatusCheckPollingStrategy doesn't support polling"));
    }

    @Override
    public Mono<U> getResult(PollingContext<T> pollingContext, TypeReference<U> resultType) {
        T activationResponse = pollingContext.getActivationResponse().getValue();
        return PollingUtils.convertResponse(activationResponse, serializer, resultType);
    }
}
