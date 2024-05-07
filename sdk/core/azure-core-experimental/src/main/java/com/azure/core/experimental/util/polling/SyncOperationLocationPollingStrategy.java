// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.util.polling;

import com.azure.core.exception.AzureException;
import com.azure.core.experimental.util.polling.implementation.PostPollResult;
import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.implementation.ImplUtils;
import com.azure.core.util.BinaryData;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.PollingContext;
import com.azure.core.util.polling.PollingStrategyOptions;
import com.azure.core.util.polling.SyncOperationResourcePollingStrategy;
import com.azure.core.util.polling.implementation.PollingConstants;
import com.azure.core.util.polling.implementation.PollingUtils;
import com.azure.core.util.serializer.JsonSerializerProviders;
import com.azure.core.util.serializer.ObjectSerializer;
import com.azure.core.util.serializer.TypeReference;

import java.time.Duration;
import java.time.OffsetDateTime;

/**
 * Implements a synchronous operation location polling strategy, from Operation-Location.
 *
 * @param <T> the type of the response type from a polling call, or BinaryData if raw response body should be kept
 * @param <U> the type of the final result object to deserialize into, or BinaryData if raw response body should be
 * kept
 */
public class SyncOperationLocationPollingStrategy<T, U> extends SyncOperationResourcePollingStrategy<T, U> {

    private static final ClientLogger LOGGER = new ClientLogger(SyncOperationLocationPollingStrategy.class);

    private static final HttpHeaderName OPERATION_LOCATION_HEADER = HttpHeaderName.fromString("Operation-Location");

    private static final TypeReference<PostPollResult> POST_POLL_RESULT_TYPE_REFERENCE
        = TypeReference.createInstance(PostPollResult.class);

    private final ObjectSerializer serializer;
    private final String endpoint;

    /**
     * Creates an instance of the operation resource polling strategy.
     *
     * @param pollingStrategyOptions options to configure this polling strategy.
     * @throws NullPointerException if {@code pollingStrategyOptions} is null.
     */
    public SyncOperationLocationPollingStrategy(PollingStrategyOptions pollingStrategyOptions) {
        super(OPERATION_LOCATION_HEADER, pollingStrategyOptions);
        this.endpoint = pollingStrategyOptions.getEndpoint();
        this.serializer = pollingStrategyOptions.getSerializer() != null
            ? pollingStrategyOptions.getSerializer()
            : JsonSerializerProviders.createInstance(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PollResponse<T> onInitialResponse(Response<?> response, PollingContext<T> pollingContext,
        TypeReference<T> pollResponseType) {
        HttpHeader operationLocationHeader = response.getHeaders().get(OPERATION_LOCATION_HEADER);
        if (operationLocationHeader != null) {
            pollingContext.setData(OPERATION_LOCATION_HEADER.getCaseSensitiveName(),
                PollingUtils.getAbsolutePath(operationLocationHeader.getValue(), endpoint, LOGGER));
        }
        final String httpMethod = response.getRequest().getHttpMethod().name();
        pollingContext.setData(PollingConstants.HTTP_METHOD, httpMethod);

        if (response.getStatusCode() == 200
            || response.getStatusCode() == 201
            || response.getStatusCode() == 202
            || response.getStatusCode() == 204) {
            final Duration retryAfter = ImplUtils.getRetryAfterFromHeaders(response.getHeaders(), OffsetDateTime::now);
            if (HttpMethod.PUT.name().equalsIgnoreCase(httpMethod)
                || HttpMethod.PATCH.name().equalsIgnoreCase(httpMethod)) {
                // PUT has initial response body as resultType
                // we expect Response<?> be either Response<BinaryData> or Response<U>
                // if it is not Response<BinaryData>, PollingUtils.serializeResponse would miss read-only properties
                BinaryData initialResponseBody = PollingUtils.serializeResponseSync(response.getValue(), serializer);
                pollingContext.setData(PollingConstants.INITIAL_RESOURCE_RESPONSE_BODY, initialResponseBody.toString());
                return new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS, null, retryAfter);
            } else {
                // same as OperationResourcePollingStrategy
                return new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS,
                    PollingUtils.convertResponseSync(response.getValue(), serializer, pollResponseType), retryAfter);
            }
        }

        throw LOGGER.logExceptionAsError(new AzureException(
            String.format("Operation failed or cancelled with status code %d, '%s' header: %s, and response body: %s",
                response.getStatusCode(), OPERATION_LOCATION_HEADER, operationLocationHeader,
                PollingUtils.serializeResponseSync(response.getValue(), serializer))));
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
        String httpMethod = pollingContext.getData(PollingConstants.HTTP_METHOD);
        if (HttpMethod.PUT.name().equalsIgnoreCase(httpMethod)
            || HttpMethod.PATCH.name().equalsIgnoreCase(httpMethod)) {
            // take the initial response body from PollingContext, and de-serialize as final result
            BinaryData initialResponseBody
                = BinaryData.fromString(pollingContext.getData(PollingConstants.INITIAL_RESOURCE_RESPONSE_BODY));
            return PollingUtils.deserializeResponseSync(initialResponseBody, serializer, resultType);
        } else if (HttpMethod.POST.name().equalsIgnoreCase(httpMethod)) {
            // take the last poll response body from PollingContext,
            // and de-serialize the "result" property as final result
            BinaryData latestResponseBody
                = BinaryData.fromString(pollingContext.getData(PollingConstants.POLL_RESPONSE_BODY));
            PostPollResult pollResult
                = PollingUtils.deserializeResponseSync(latestResponseBody, serializer, POST_POLL_RESULT_TYPE_REFERENCE);
            if (pollResult != null && pollResult.getResult() != null) {
                return PollingUtils.deserializeResponseSync(pollResult.getResult(), serializer, resultType);
            } else {
                throw LOGGER.logExceptionAsError(new AzureException("Cannot get final result"));
            }
        } else {
            throw LOGGER.logExceptionAsError(new AzureException("Cannot get final result"));
        }
    }
}
