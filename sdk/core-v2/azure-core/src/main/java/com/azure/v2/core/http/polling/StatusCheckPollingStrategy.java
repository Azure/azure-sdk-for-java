// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.core.http.polling;

import com.azure.v2.core.implementation.ImplUtils;
import com.azure.v2.core.implementation.polling.PollingUtils;
import io.clientcore.core.http.models.HttpResponseException;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.serialization.ObjectSerializer;
import io.clientcore.core.serialization.json.JsonSerializer;

import java.lang.reflect.Type;
import java.time.Duration;
import java.time.OffsetDateTime;

/**
 * Fallback polling strategy that doesn't poll but exits successfully if no other polling strategies are detected and
 * status code is 2xx.
 *
 * @param <T> the type of the response type from a polling call, or BinaryData if raw response body should be kept
 * @param <U> the type of the final result object to deserialize into, or BinaryData if raw response body should be
 * kept
 */
public class StatusCheckPollingStrategy<T, U> implements PollingStrategy<T, U> {
    private static final ClientLogger LOGGER = new ClientLogger(StatusCheckPollingStrategy.class);
    private static final ObjectSerializer DEFAULT_SERIALIZER = new JsonSerializer();

    private final ObjectSerializer serializer;

    /**
     * Creates a status check polling strategy with a JSON serializer.
     */
    public StatusCheckPollingStrategy() {
        this(DEFAULT_SERIALIZER);
    }

    /**
     * Creates a status check polling strategy with a custom object serializer.
     *
     * @param serializer a custom serializer for serializing and deserializing polling responses
     */
    public StatusCheckPollingStrategy(ObjectSerializer serializer) {
        this.serializer = (serializer == null) ? DEFAULT_SERIALIZER : serializer;
    }

    @Override
    public boolean canPoll(Response<T> initialResponse) {
        return true;
    }

    @Override
    public PollResponse<T> onInitialResponse(Response<T> response, PollingContext<T> pollingContext,
        Type pollResponseType) {
        if (response.getStatusCode() == 200
            || response.getStatusCode() == 201
            || response.getStatusCode() == 202
            || response.getStatusCode() == 204) {
            Duration retryAfter = ImplUtils.getRetryAfterFromHeaders(response.getHeaders(), OffsetDateTime::now);
            return new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                PollingUtils.convertResponse(response.getValue(), serializer, pollResponseType), retryAfter);
        } else {
            Response<BinaryData> binaryDataResponse = new Response<>(response.getRequest(), response.getStatusCode(),
                response.getHeaders(), BinaryData.fromObject(response.getValue()));
            throw LOGGER.throwableAtError()
                .log("Operation failed or cancelled",
                    message -> new HttpResponseException(message, binaryDataResponse, null));
        }
    }

    @Override
    public PollResponse<T> poll(PollingContext<T> context, Type pollResponseType) {
        throw LOGGER.throwableAtError()
            .log("StatusCheckPollingStrategy doesn't support polling", IllegalStateException::new);
    }

    @Override
    public U getResult(PollingContext<T> pollingContext, Type resultType) {
        T activationResponse = pollingContext.getActivationResponse().getValue();
        return PollingUtils.convertResponse(activationResponse, serializer, resultType);
    }
}
