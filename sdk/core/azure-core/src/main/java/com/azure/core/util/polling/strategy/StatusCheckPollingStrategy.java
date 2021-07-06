// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.polling.strategy;

import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.implementation.TypeUtil;
import com.azure.core.util.BinaryData;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollingContext;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import reactor.core.publisher.Mono;

import java.lang.reflect.Type;

/**
 * Fallback polling strategy that doesn't poll but exits successfully if no other polling are detected
 * and status code is 2xx.
 */
public class StatusCheckPollingStrategy implements PollingStrategy {

    private final ClientLogger logger = new ClientLogger(StatusCheckPollingStrategy.class);

    private final JacksonAdapter serializer = new JacksonAdapter();

    @Override
    public boolean canPoll(Response<?> activationResponse) {
        return true;
    }

    @Override
    public String getPollingUrl(PollingContext<BinaryData> context) {
        throw logger.logExceptionAsWarning(
            new IllegalStateException("StatusCheckPollingStrategy doesn't support polling"));
    }

    @Override
    public String getFinalGetUrl(PollingContext<BinaryData> context) {
        return null;
    }

    @Override
    public Mono<LongRunningOperationStatus> onActivationResponse(Response<?> response, PollingContext<BinaryData> context) {
        return Mono.just(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED);
    }

    @Override
    public Mono<LongRunningOperationStatus> onPollingResponse(HttpResponse response, PollingContext<BinaryData> context) {
        return Mono.just(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <U> Mono<U> getFinalResult(HttpResponse response, PollingContext<BinaryData> context, Type resultType) {
        if (TypeUtil.isTypeOrSubTypeOf(BinaryData.class, resultType)) {
            return (Mono<U>) BinaryData.fromFlux(response.getBody());
        } else {
            return response.getBodyAsString().flatMap(body -> Mono.fromCallable(() ->
                    serializer.deserialize(body, resultType, SerializerEncoding.JSON)));
        }
    }
}
