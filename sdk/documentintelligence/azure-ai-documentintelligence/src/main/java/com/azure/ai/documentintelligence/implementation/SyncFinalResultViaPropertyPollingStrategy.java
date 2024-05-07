// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.documentintelligence.implementation;

import com.azure.core.exception.AzureException;
import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.PollingContext;
import com.azure.core.util.polling.PollingStrategyOptions;
import com.azure.core.util.polling.SyncOperationResourcePollingStrategy;
import com.azure.core.util.serializer.JsonSerializerProviders;
import com.azure.core.util.serializer.ObjectSerializer;
import com.azure.core.util.serializer.TypeReference;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Objects;

/**
 * Implements a synchronous operation location polling strategy, from Operation-Location.
 *
 * @param <T> the type of the response type from a polling call, or BinaryData if raw response body should be kept
 * @param <U> the type of the final result object to deserialize into, or BinaryData if raw response body should be
 *           kept
 */
public final class SyncFinalResultViaPropertyPollingStrategy<T, U> extends SyncOperationResourcePollingStrategy<T, U> {

    private static final ClientLogger LOGGER = new ClientLogger(SyncFinalResultViaPropertyPollingStrategy.class);

    private static final HttpHeaderName OPERATION_LOCATION_HEADER
        = HttpHeaderName.fromString("Operation-Location");

    private final ObjectSerializer serializer;
    private final String endpoint;
    private final String propertyName;

    /**
     * Creates an instance of the operation resource polling strategy.
     *
     * @param pollingStrategyOptions options to configure this polling strategy.
     * @param propertyName the name of the property to extract final result.
     * @throws NullPointerException if {@code pollingStrategyOptions} is null.
     */
    public SyncFinalResultViaPropertyPollingStrategy(PollingStrategyOptions pollingStrategyOptions, String propertyName) {
        super(OPERATION_LOCATION_HEADER, pollingStrategyOptions);
        this.propertyName = Objects.requireNonNull(propertyName);
        this.endpoint = pollingStrategyOptions.getEndpoint();
        this.serializer = pollingStrategyOptions.getSerializer() != null
            ? pollingStrategyOptions.getSerializer() : JsonSerializerProviders.createInstance(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PollResponse<T> onInitialResponse(Response<?> response, PollingContext<T> pollingContext,
                                             TypeReference<T> pollResponseType) {
        // Response<?> is Response<BinaryData>

        HttpHeader operationLocationHeader = response.getHeaders().get(OPERATION_LOCATION_HEADER);
        if (operationLocationHeader != null) {
            pollingContext.setData(OPERATION_LOCATION_HEADER.getCaseSensitiveName(),
                PollingUtils.getAbsolutePath(operationLocationHeader.getValue(), endpoint, LOGGER));
        }
        final String httpMethod = response.getRequest().getHttpMethod().name();
        pollingContext.setData(PollingUtils.HTTP_METHOD, httpMethod);

        if (response.getStatusCode() == 200
            || response.getStatusCode() == 201
            || response.getStatusCode() == 202
            || response.getStatusCode() == 204) {
            final Duration retryAfter = PollingUtils.getRetryAfterFromHeaders(response.getHeaders(), OffsetDateTime::now);
            return new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS,
                PollingUtils.deserializeResponseSync((BinaryData) response.getValue(), serializer, pollResponseType), retryAfter);
        }

        throw LOGGER.logExceptionAsError(new AzureException(String.format(
            "Operation failed or cancelled with status code %d, '%s' header: %s, and response body: %s",
            response.getStatusCode(), OPERATION_LOCATION_HEADER, operationLocationHeader, response.getValue())));
    }

    /**
     * {@inheritDoc}
     */
    public U getResult(PollingContext<T> pollingContext, TypeReference<U> resultType) {
        if (pollingContext.getLatestResponse().getStatus() == LongRunningOperationStatus.FAILED) {
            throw LOGGER.logExceptionAsError(new AzureException("Long running operation failed."));
        } else if (pollingContext.getLatestResponse().getStatus() == LongRunningOperationStatus.USER_CANCELLED) {
            throw LOGGER.logExceptionAsError(new AzureException("Long running operation cancelled."));
        }
        // take the last poll response body from PollingContext,
        // and de-serialize the <propertyName> property as final result
        BinaryData latestResponseBody =
            BinaryData.fromString(pollingContext.getData(PollingUtils.POLL_RESPONSE_BODY));
        Map<String, Object> pollResult =
            PollingUtils.deserializeResponseSync(latestResponseBody, serializer, PollingUtils.POST_POLL_RESULT_TYPE_REFERENCE);
        if (pollResult != null && pollResult.get(propertyName) != null) {
            return PollingUtils.deserializeResponseSync(BinaryData.fromObject(pollResult.get(propertyName)), serializer, resultType);
        } else {
            throw LOGGER.logExceptionAsError(new AzureException("Cannot get final result"));
        }
    }
}
