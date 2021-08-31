// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.polling;

import com.azure.core.exception.AzureException;
import com.azure.core.http.rest.Response;
import com.azure.core.implementation.TypeUtil;
import com.azure.core.util.BinaryData;
import com.azure.core.util.serializer.TypeReference;
import reactor.core.publisher.Mono;

/**
 * Fallback polling strategy that doesn't poll but exits successfully if no other polling strategies are detected
 * and status code is 2xx.
 *
 * @param <T> the type of the response type from a polling call, or BinaryData if raw response body should be kept
 * @param <U> the type of the final result object to deserialize into, or BinaryData if raw response body should be
 *           kept
 */
public class StatusCheckPollingStrategy<T, U> implements PollingStrategy<T, U> {
    @Override
    public Mono<Boolean> canPoll(Response<?> initialResponse) {
        return Mono.just(true);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Mono<PollResponse<T>> onInitialResponse(Response<?> response, PollingContext<T> pollingContext,
                                                              TypeReference<T> pollResponseType) {
        if (response.getStatusCode() == 200
            || response.getStatusCode() == 201
            || response.getStatusCode() == 202
            || response.getStatusCode() == 204) {
            return Mono.just(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED).flatMap(status -> {
                if (response.getValue() == null) {
                    return Mono.just(new PollResponse<>(status, null));
                } else if (TypeUtil.isTypeOrSubTypeOf(
                        response.getValue().getClass(), pollResponseType.getJavaType())) {
                    return Mono.just(new PollResponse<>(status, (T) response.getValue()));
                } else {
                    Mono<BinaryData> binaryDataMono;
                    if (response.getValue() instanceof BinaryData) {
                        binaryDataMono = Mono.just((BinaryData) response.getValue());
                    } else {
                        binaryDataMono = BinaryData.fromObjectAsync(response.getValue());
                    }
                    if (TypeUtil.isTypeOrSubTypeOf(BinaryData.class, pollResponseType.getJavaType())) {
                        return binaryDataMono.map(binaryData -> new PollResponse<>(status, (T) binaryData));
                    } else {
                        return binaryDataMono.flatMap(binaryData -> binaryData.toObjectAsync(pollResponseType))
                            .map(value -> new PollResponse<>(status, value));
                    }
                }
            });
        } else {
            return Mono.error(new AzureException("Operation failed or cancelled: " + response.getStatusCode()));
        }
    }

    @Override
    public Mono<PollResponse<T>> poll(PollingContext<T> context, TypeReference<T> pollResponseType) {
        return Mono.error(new IllegalStateException("StatusCheckPollingStrategy doesn't support polling"));
    }

    @SuppressWarnings("unchecked")
    @Override
    public Mono<U> getResult(PollingContext<T> pollingContext, TypeReference<U> resultType) {
        T activationResponse = pollingContext.getActivationResponse().getValue();
        if (TypeUtil.isTypeOrSubTypeOf(activationResponse.getClass(), resultType.getJavaType())) {
            return (Mono<U>) Mono.just(activationResponse);
        } else {
            Mono<BinaryData> binaryDataMono;
            if (activationResponse instanceof BinaryData) {
                binaryDataMono = Mono.just((BinaryData) activationResponse);
            } else {
                binaryDataMono = BinaryData.fromObjectAsync(activationResponse);
            }
            if (TypeUtil.isTypeOrSubTypeOf(BinaryData.class, resultType.getJavaType())) {
                return (Mono<U>) binaryDataMono;
            } else {
                return binaryDataMono.flatMap(binaryData -> binaryData.toObjectAsync(resultType));
            }
        }
    }

    @Override
    public Mono<T> cancel(PollingContext<T> pollingContext, PollResponse<T> initialResponse) {
        return Mono.error(new IllegalStateException("Cancellation is not supported."));
    }
}
