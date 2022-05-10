// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.phonenumbersdemo.implementation;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.OperationResourcePollingStrategy;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.PollingContext;
import com.azure.core.util.serializer.ObjectSerializer;
import com.azure.core.util.serializer.TypeReference;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

public class PhoneNumbersSearchPollingStrategy<T, U> extends OperationResourcePollingStrategy<T, U>  {

    private final PhoneNumbersImpl client;

    private String operationId;
    private String searchId;

    public PhoneNumbersSearchPollingStrategy(PhoneNumbersImpl client, HttpPipeline httpPipeline,
                                             ObjectSerializer serializer,
                                             Context context) {
        super(httpPipeline, serializer, "Operation-Location", context);
        this.client = client;
    }

    @Override
    public Mono<Boolean> canPoll(Response<?> initialResponse) {
        boolean canPoll = false;
        init(initialResponse);
        if (operationId != null && searchId != null) {
            canPoll = true;
        }
        return Mono.just(canPoll);
    }

    @Override
    public Mono<PollResponse<T>> onInitialResponse(Response<?> response,
                                                   PollingContext<T> pollingContext,
                                                   TypeReference<T> pollResponseType) {
        init(response);
        return super.onInitialResponse(response, pollingContext, pollResponseType);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Mono<PollResponse<T>> poll(PollingContext<T> pollingContext,
                                      TypeReference<T> pollResponseType) {
        return client.getOperationWithResponseAsync(operationId, null)
            .map(response -> {
                LongRunningOperationStatus status = LongRunningOperationStatus.IN_PROGRESS;
                BinaryData value = response.getValue();
                Map<String, Object> valueMap = (Map<String, Object>) value.toObject(Object.class);
                if (valueMap.containsKey("status")) {
                    String statusStr = (String) valueMap.get("status");
                    if ("succeeded".equalsIgnoreCase(statusStr)) {
                        status = LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;
                    } else if ("failed".equalsIgnoreCase(statusStr)) {
                        status = LongRunningOperationStatus.FAILED;
                    } else if ("notStarted".equalsIgnoreCase(statusStr)) {
                        status = LongRunningOperationStatus.NOT_STARTED;
                    } else if ("running".equalsIgnoreCase(statusStr)) {
                        status = LongRunningOperationStatus.IN_PROGRESS;
                    }
                }
                String retryAfterValue = response.getHeaders().getValue("Retry-After");
                Duration retryAfter = retryAfterValue == null
                    ? null : Duration.ofSeconds(Long.parseLong(retryAfterValue));
                T typedValue;
                if (pollResponseType.getJavaClass().isAssignableFrom(BinaryData.class)) {
                    typedValue = (T) response.getValue();
                } else {
                    typedValue = response.getValue().toObject(pollResponseType);
                }
                return new PollResponse<>(status, typedValue, retryAfter);
            });
    }

    @SuppressWarnings("unchecked")
    @Override
    public Mono<U> getResult(PollingContext<T> pollingContext, TypeReference<U> resultType) {
        return client.getSearchResultWithResponseAsync(searchId, null)
            .map(response -> {
                if (resultType.getJavaClass().isAssignableFrom(BinaryData.class)) {
                    return (U) response.getValue();
                } else {
                    return response.getValue().toObject(resultType);
                }
            });
    }

    private void init(Response<?> response) {
        operationId = response.getHeaders().getValue("operation-id");
        searchId = response.getHeaders().getValue("search-id");
    }
}
