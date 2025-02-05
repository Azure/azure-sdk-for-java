// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management.implementation.polling;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.management.polling.PollResult;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.PollingContext;
import com.azure.core.util.serializer.SerializerAdapter;

import java.lang.reflect.Type;
import java.time.Duration;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class SyncPollOperation {
    private static final LongRunningOperationStatus LRO_CANCELLED
        = LongRunningOperationStatus.fromString("Cancelled", true);

    /**
     * Gets a Function that starts the Azure resource manager(ARM) long-running-operation(LRO).
     *
     * @param serializerAdapter the serializer for any encoding and decoding
     * @param pollResultType the type of the poll result, if no result is expecting then this should be Void.class
     * @param lroInitialResponse the activation operation to activate (start) the long-running operation
     * @param <T> the type of poll result
     * @return the ARM LRO activation Function
     */
    public static <T> Function<PollingContext<PollResult<T>>, PollResponse<PollResult<T>>> syncActivationFunction(
        SerializerAdapter serializerAdapter, Class<T> pollResultType,
        Supplier<Response<BinaryData>> lroInitialResponse) {
        return pollingContext -> {
            Response<BinaryData> response = lroInitialResponse.get();
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
        SerializerAdapter serializerAdapter, HttpPipeline httpPipeline, Class<T> pollResultType, Context context) {
        return pollingContext -> {
            PollingState state = PollingState.from(serializerAdapter, pollingContext);
            if (state.getOperationStatus().isComplete()) {
                return pollResponseFromPollingState(serializerAdapter, pollResultType, state);
            } else {
                try (HttpResponse response
                    = httpPipeline.sendSync(new HttpRequest(HttpMethod.GET, state.getPollUrl()), context)) {
                    String body = response.getBodyAsBinaryData().toString();
                    state.update(response.getStatusCode(), response.getHeaders(), body);
                    state.store(pollingContext);
                    return pollResponseFromPollingState(serializerAdapter, pollResultType, state);
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

    private static <T> PollResponse<PollResult<T>> pollResponseFromPollingState(SerializerAdapter serializer,
        Class<T> pollResultType, PollingState state) {
        if (state.getOperationStatus().isComplete()) {
            if (state.getOperationStatus() == LongRunningOperationStatus.FAILED
                || state.getOperationStatus() == LRO_CANCELLED) {
                // Failed|Cancelled
                Error lroInitError = state.getSynchronouslyFailedLroError();
                if (lroInitError != null) {
                    return errorPollResponse(state.getOperationStatus(), lroInitError);
                }
                Error pollError = state.getPollError();
                if (pollError != null) {
                    return errorPollResponse(state.getOperationStatus(), pollError);
                }
                throw new IllegalStateException(
                    "Either LroError or PollError must" + "be set when OperationStatus is in Failed|Cancelled State.");
            } else {
                // Succeeded
                return pollResponse(serializer, state.getOperationStatus(), state.getLastResponseBody(), pollResultType,
                    state.getPollDelay());
            }
        } else {
            // InProgress|NonTerminal-Status
            return pollResponse(serializer, state.getOperationStatus(), state.getLastResponseBody(), pollResultType,
                state.getPollDelay());
        }
    }

    private static <T> PollResponse<PollResult<T>> pollResponse(SerializerAdapter serializer,
        LongRunningOperationStatus operationStatus, String pollResponseBody, Class<T> pollResultType,
        Duration pollDelay) {
        T result = PollOperation.deserialize(serializer, pollResponseBody, pollResultType);
        return new PollResponse<>(operationStatus, new PollResult<>(result), pollDelay);
    }

    private static <T> PollResponse<PollResult<T>> errorPollResponse(LongRunningOperationStatus operationStatus,
        Error error) {
        return new PollResponse<>(operationStatus, new PollResult<>(new PollResult.Error(error.getMessage(),
            error.getResponseStatusCode(), new HttpHeaders(error.getResponseHeaders()), error.getResponseBody())));
    }
}
