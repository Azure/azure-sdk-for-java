// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management.implementation.polling;

import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.management.polling.PollResult;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.PollingContext;
import com.azure.core.util.serializer.SerializerAdapter;

import java.lang.reflect.Type;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Synchronous poll operation for Azure resource manager (ARM) long-running-operation (LRO).
 */
public final class SyncPollOperation {
    /**
     * Gets a Function that starts the Azure resource manager(ARM) long-running-operation(LRO).
     *
     * @param serializerAdapter the serializer for any encoding and decoding
     * @param pollResultType the type of the poll result, if no result is expecting then this should be Void.class
     * @param lroInitialResponseSupplier Supplier of the activation operation to activate (start) the long-running operation
     * @param <T> the type of poll result
     * @return the ARM LRO activation Function
     */
    public static <T> Function<PollingContext<PollResult<T>>, PollResponse<PollResult<T>>> activationFunction(
        SerializerAdapter serializerAdapter, Type pollResultType,
        Supplier<Response<BinaryData>> lroInitialResponseSupplier) {
        return pollingContext -> {
            Response<BinaryData> response = lroInitialResponseSupplier.get();
            PollingState state = PollingState.create(serializerAdapter, response.getRequest(), response.getStatusCode(),
                response.getHeaders(), response.getValue().toString());
            state.store(pollingContext);
            T result = PollOperation.deserialize(serializerAdapter, response.getValue().toString(), pollResultType);
            return new PollResponse<>(state.getOperationStatus(), new PollResult<>(result), state.getPollDelay());
        };
    }

    /**
     * Gets a Function that polls provisioning state of ARM resource.
     *
     * @param serializerAdapter the serializer for any encoding and decoding
     * @param httpPipeline the HttpPipeline for making poll request
     * @param pollResultType the type of the poll result
     * @param context the context
     * @param <T> the type of poll result type
     * @return the ARM poll function
     */
    public static <T> Function<PollingContext<PollResult<T>>, PollResponse<PollResult<T>>> pollFunction(
        SerializerAdapter serializerAdapter, HttpPipeline httpPipeline, Type pollResultType, Context context) {
        return pollingContext -> {
            PollingState state = PollingState.from(serializerAdapter, pollingContext);
            if (state.getOperationStatus().isComplete()) {
                return PollOperation.pollResponseFromPollingState(serializerAdapter, pollResultType, state);
            } else {
                try (HttpResponse response
                    = httpPipeline.sendSync(new HttpRequest(HttpMethod.GET, state.getPollUrl()), context)) {
                    String body = response.getBodyAsBinaryData().toString();
                    state.update(response.getStatusCode(), response.getHeaders(), body);
                    state.store(pollingContext);
                    return PollOperation.pollResponseFromPollingState(serializerAdapter, pollResultType, state);
                }
            }
        };
    }

    /**
     * Currently there is no option to cancel an ARM LRO in generic way, this is NOP.
     *
     * @param context the context
     * @param <T> the type of poll result type
     * @return cancel Function
     */
    public static <T> BiFunction<PollingContext<PollResult<T>>, PollResponse<PollResult<T>>, PollResult<T>>
        cancelFunction(Context context) {
        return (pollingContext, pollResultPollResponse) -> null;
    }

    /**
     * Gets a Function that retrieves final result of a LRO.
     *
     * @param serializerAdapter the serializer for any encoding and decoding
     * @param httpPipeline the HttpPipeline for fetching final result
     * @param finalResultType the final result type
     * @param context the context
     * @param <T> the final result type
     * @param <U> the poll result type
     * @return retrieve final LRO result Function
     */
    public static <T, U> Function<PollingContext<T>, U> fetchResultFunction(SerializerAdapter serializerAdapter,
        HttpPipeline httpPipeline, Type finalResultType, Context context) {
        return pollingContext -> {
            PollingState state = PollingState.from(serializerAdapter, pollingContext);
            FinalResult finalResult = state.getFinalResult();
            if (finalResult == null) {
                return null;
            } else {
                String value = finalResult.getResult();
                U result;
                if (value != null) {
                    result = PollOperation.deserialize(serializerAdapter, value, finalResultType);
                } else {
                    try (HttpResponse response
                        = httpPipeline.sendSync(new HttpRequest(HttpMethod.GET, finalResult.getResultUri()), context)) {
                        result = PollOperation.deserialize(serializerAdapter, response.getBodyAsBinaryData().toString(),
                            finalResultType);
                    }
                }
                return result;
            }
        };
    }
}
