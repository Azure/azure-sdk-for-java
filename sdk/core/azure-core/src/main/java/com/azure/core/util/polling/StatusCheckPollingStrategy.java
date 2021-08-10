// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.polling;

import com.azure.core.http.rest.Response;
import com.azure.core.implementation.TypeUtil;
import com.azure.core.util.BinaryData;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.TypeReference;
import reactor.core.publisher.Mono;

/**
 * Fallback polling strategy that doesn't poll but exits successfully if no other polling are detected
 * and status code is 2xx.
 *
 * @param <T> the {@link TypeReference} of the response type from a polling call, or BinaryData if raw response body
 *            should be kept
 * @param <U> the {@link TypeReference} of the final result object to deserialize into, or BinaryData if raw response
 *            body should be kept
 */
public class StatusCheckPollingStrategy<T, U> implements PollingStrategy<T, U> {

    private final ClientLogger logger = new ClientLogger(StatusCheckPollingStrategy.class);

    @Override
    public Mono<Boolean> canPoll(Response<?> initialResponse) {
        return Mono.just(true);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Mono<LongRunningOperationStatus> onInitialResponse(Response<?> response, PollingContext<T> pollingContext,
                                                              TypeReference<T> pollResponseType) {
        return Mono.just(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED);
    }

    @Override
    public Mono<PollResponse<T>> poll(PollingContext<T> context, TypeReference<T> pollResponseType) {
        throw logger.logExceptionAsWarning(
            new IllegalStateException("StatusCheckPollingStrategy doesn't support polling"));
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
