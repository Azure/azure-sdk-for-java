// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.polling;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.implementation.TypeUtil;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.TypeReference;
import reactor.core.publisher.Mono;

import java.lang.reflect.Type;
import java.time.Duration;

/**
 * Fallback polling strategy that doesn't poll but exits successfully if no other polling are detected
 * and status code is 2xx.
 */
public class StatusCheckPollingStrategy implements PollingStrategy {

    private final ClientLogger logger = new ClientLogger(StatusCheckPollingStrategy.class);

    @Override
    public Mono<Boolean> canPoll(Response<?> initialResponse) {
        return Mono.just(true);
    }

    @Override
    public Mono<LongRunningOperationStatus> onInitialResponse(Response<?> response, PollingContext<BinaryData> pollingContext) {
        return Mono.just(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED);
    }

    @Override
    public Mono<PollResponse<BinaryData>> poll(PollingContext<BinaryData> context) {
        throw logger.logExceptionAsWarning(
            new IllegalStateException("StatusCheckPollingStrategy doesn't support polling"));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <U> Mono<U> getResult(PollingContext<BinaryData> pollingContext, TypeReference<U> resultType) {
        BinaryData activationResponse = pollingContext.getActivationResponse().getValue();
        if (TypeUtil.isTypeOrSubTypeOf(BinaryData.class, resultType.getJavaType())) {
            return (Mono<U>) Mono.just(activationResponse);
        } else {
            return activationResponse.toObjectAsync(resultType);
        }
    }

    @Override
    public Mono<BinaryData> cancel(PollingContext<BinaryData> pollingContext, PollResponse<BinaryData> initialResponse) {
        return Mono.error(new IllegalStateException("Cancellation is not supported."));
    }
}
