// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.core.http.polling;

import com.azure.v2.core.implementation.ImplUtils;
import com.azure.v2.core.implementation.polling.PollResult;
import com.azure.v2.core.implementation.polling.PollingConstants;
import com.azure.v2.core.implementation.polling.PollingUtils;
import io.clientcore.core.http.models.HttpHeader;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.HttpResponseException;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.models.RequestContext;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.models.CoreException;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.serialization.ObjectSerializer;
import io.clientcore.core.serialization.json.JsonSerializer;
import io.clientcore.core.utils.CoreUtils;
import io.clientcore.core.utils.UriBuilder;

import java.lang.reflect.Type;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Objects;

import static com.azure.v2.core.implementation.polling.PollingUtils.getAbsolutePath;
import static com.azure.v2.core.implementation.polling.PollingUtils.operationResourceCanPoll;
import static com.azure.v2.core.implementation.polling.PollingUtils.serializeResponse;

/**
 * Implements a operation resource polling strategy, typically from Operation-Location.
 *
 * @param <T> the type of the response type from a polling call, or BinaryData if raw response body should be kept
 * @param <U> the type of the final result object to deserialize into, or BinaryData if raw response body should be
 * kept
 */
public class OperationResourcePollingStrategy<T, U> implements PollingStrategy<T, U> {
    private static final ClientLogger LOGGER = new ClientLogger(OperationResourcePollingStrategy.class);
    private static final HttpHeaderName DEFAULT_OPERATION_LOCATION_HEADER
        = HttpHeaderName.fromString("Operation-Location");
    private static final Type POLL_RESULT_TYPE_REFERENCE = PollResult.class;

    private final HttpPipeline httpPipeline;
    private final ObjectSerializer serializer;
    private final String endpoint;
    private final HttpHeaderName operationLocationHeaderName;
    private final RequestContext requestContext;
    private final String serviceVersion;

    /**
     * Creates an instance of the operation resource polling strategy using a JSON serializer and "Operation-Location"
     * as the header for polling.
     *
     * @param httpPipeline an instance of {@link HttpPipeline} to send requests with
     */
    public OperationResourcePollingStrategy(HttpPipeline httpPipeline) {
        this(DEFAULT_OPERATION_LOCATION_HEADER, new PollingStrategyOptions(httpPipeline));
    }

    /**
     * Creates an instance of the operation resource polling strategy.
     *
     * @param httpPipeline an instance of {@link HttpPipeline} to send requests with
     * @param serializer a custom serializer for serializing and deserializing polling responses
     * @param operationLocationHeaderName a custom header for polling the long-running operation
     */
    public OperationResourcePollingStrategy(HttpPipeline httpPipeline, ObjectSerializer serializer,
        String operationLocationHeaderName) {
        this(httpPipeline, serializer, operationLocationHeaderName, RequestContext.none());
    }

    /**
     * Creates an instance of the operation resource polling strategy.
     *
     * @param httpPipeline an instance of {@link HttpPipeline} to send requests with
     * @param serializer a custom serializer for serializing and deserializing polling responses
     * @param operationLocationHeaderName a custom header for polling the long-running operation
     * @param requestContext an instance of {@link RequestContext}
     */
    public OperationResourcePollingStrategy(HttpPipeline httpPipeline, ObjectSerializer serializer,
        String operationLocationHeaderName, RequestContext requestContext) {
        this(httpPipeline, null, serializer, operationLocationHeaderName, requestContext);
    }

    /**
     * Creates an instance of the operation resource polling strategy.
     *
     * @param httpPipeline an instance of {@link HttpPipeline} to send requests with.
     * @param endpoint an endpoint for creating an absolute path when the path itself is relative.
     * @param serializer a custom serializer for serializing and deserializing polling responses.
     * @param operationLocationHeaderName a custom header for polling the long-running operation.
     * @param requestContext an instance of {@link RequestContext}.
     */
    public OperationResourcePollingStrategy(HttpPipeline httpPipeline, String endpoint, ObjectSerializer serializer,
        String operationLocationHeaderName, RequestContext requestContext) {
        this(operationLocationHeaderName == null ? null : HttpHeaderName.fromString(operationLocationHeaderName),
            new PollingStrategyOptions(httpPipeline).setEndpoint(endpoint)
                .setSerializer(serializer)
                .setRequestContext(requestContext));
    }

    /**
     * Creates an instance of the operation resource polling strategy.
     *
     * @param operationLocationHeaderName a custom header for polling the long-running operation.
     * @param pollingStrategyOptions options to configure this polling strategy.
     * @throws NullPointerException if {@code pollingStrategyOptions} is null.
     */
    public OperationResourcePollingStrategy(HttpHeaderName operationLocationHeaderName,
        PollingStrategyOptions pollingStrategyOptions) {
        Objects.requireNonNull(pollingStrategyOptions, "'pollingStrategyOptions' cannot be null");
        this.httpPipeline = pollingStrategyOptions.getHttpPipeline();
        this.endpoint = pollingStrategyOptions.getEndpoint();
        this.serializer = pollingStrategyOptions.getSerializer() != null
            ? pollingStrategyOptions.getSerializer()
            : new JsonSerializer();
        this.operationLocationHeaderName
            = (operationLocationHeaderName == null) ? DEFAULT_OPERATION_LOCATION_HEADER : operationLocationHeaderName;

        this.serviceVersion = pollingStrategyOptions.getServiceVersion();
        this.requestContext = pollingStrategyOptions.getRequestContext() == null
            ? RequestContext.none()
            : pollingStrategyOptions.getRequestContext();
    }

    @Override
    public boolean canPoll(Response<T> initialResponse) {
        return operationResourceCanPoll(initialResponse, operationLocationHeaderName, endpoint, LOGGER);
    }

    @Override
    public PollResponse<T> onInitialResponse(Response<T> response, PollingContext<T> pollingContext,
        Type pollResponseType) {
        HttpHeader operationLocationHeader = response.getHeaders().get(operationLocationHeaderName);
        HttpHeader locationHeader = response.getHeaders().get(HttpHeaderName.LOCATION);
        if (operationLocationHeader != null) {
            pollingContext.setData(operationLocationHeaderName.getCaseSensitiveName(),
                getAbsolutePath(operationLocationHeader.getValue(), endpoint, LOGGER));
        }

        if (locationHeader != null) {
            pollingContext.setData(PollingConstants.LOCATION,
                getAbsolutePath(locationHeader.getValue(), endpoint, LOGGER));
        }

        pollingContext.setData(PollingConstants.HTTP_METHOD, response.getRequest().getHttpMethod().name());
        pollingContext.setData(PollingConstants.REQUEST_URL, response.getRequest().getUri().toString());

        if (response.getStatusCode() == 200
            || response.getStatusCode() == 201
            || response.getStatusCode() == 202
            || response.getStatusCode() == 204) {
            Duration retryAfter = ImplUtils.getRetryAfterFromHeaders(response.getHeaders(), OffsetDateTime::now);
            return new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS,
                PollingUtils.convertResponse(response.getValue(), serializer, pollResponseType), retryAfter);
        }

        Response<BinaryData> binaryDataResponse = new Response<>(response.getRequest(), response.getStatusCode(),
            response.getHeaders(), BinaryData.fromObject(response.getValue()));

        throw LOGGER.throwableAtError()
            .addKeyValue("http.response.status_code", response.getStatusCode())
            .addKeyValue("http.response.body.content", serializeResponse(response.getValue(), serializer).toString())
            .addKeyValue("operationLocationHeaderName", operationLocationHeaderName.getValue())
            .addKeyValue("operationLocationHeaderValue",
                operationLocationHeader == null ? null : operationLocationHeader.getValue())
            .log("Operation failed or cancelled",
                message -> new HttpResponseException(message, binaryDataResponse, null));
    }

    @Override
    public PollResponse<T> poll(PollingContext<T> pollingContext, Type pollResponseType) {
        String url = pollingContext.getData(operationLocationHeaderName.getCaseSensitiveName());
        url = setServiceVersionQueryParam(url);
        HttpRequest request = new HttpRequest().setMethod(HttpMethod.GET).setUri(url).setContext(requestContext);

        try (Response<BinaryData> response = httpPipeline.send(request)) {
            BinaryData responseBody = response.getValue();
            PollResult pollResult
                = PollingUtils.deserializeResponse(responseBody, serializer, POLL_RESULT_TYPE_REFERENCE);

            String resourceLocation = pollResult.getResourceLocation();
            if (resourceLocation != null) {
                pollingContext.setData(PollingConstants.RESOURCE_LOCATION,
                    getAbsolutePath(resourceLocation, endpoint, LOGGER));
            }
            pollingContext.setData(PollingConstants.POLL_RESPONSE_BODY, responseBody.toString());

            Duration retryAfter = ImplUtils.getRetryAfterFromHeaders(response.getHeaders(), OffsetDateTime::now);

            return new PollResponse<>(pollResult.getStatus(),
                PollingUtils.deserializeResponse(responseBody, serializer, pollResponseType), retryAfter);
        }
    }

    @Override
    public U getResult(PollingContext<T> pollingContext, Type resultType) {
        if (pollingContext.getLatestResponse().getStatus() == LongRunningOperationStatus.FAILED) {
            throw LOGGER.throwableAtError().log("Long running operation failed.", CoreException::from);
        } else if (pollingContext.getLatestResponse().getStatus() == LongRunningOperationStatus.USER_CANCELLED) {
            throw LOGGER.throwableAtError().log("Long running operation cancelled.", CoreException::from);
        }
        String finalGetUrl = pollingContext.getData(PollingConstants.RESOURCE_LOCATION);
        if (finalGetUrl == null) {
            String httpMethod = pollingContext.getData(PollingConstants.HTTP_METHOD);
            if (HttpMethod.PUT.name().equalsIgnoreCase(httpMethod)
                || HttpMethod.PATCH.name().equalsIgnoreCase(httpMethod)) {
                finalGetUrl = pollingContext.getData(PollingConstants.REQUEST_URL);
            } else if (HttpMethod.POST.name().equalsIgnoreCase(httpMethod)) {
                finalGetUrl = pollingContext.getData(PollingConstants.LOCATION);
            } else {
                throw LOGGER.throwableAtError().log("Cannot get final result.", CoreException::from);
            }
        }

        if (finalGetUrl == null) {
            String latestResponseBody = pollingContext.getData(PollingConstants.POLL_RESPONSE_BODY);
            return PollingUtils.deserializeResponse(BinaryData.fromString(latestResponseBody), serializer, resultType);
        }
        finalGetUrl = setServiceVersionQueryParam(finalGetUrl);

        HttpRequest request
            = new HttpRequest().setMethod(HttpMethod.GET).setUri(finalGetUrl).setContext(requestContext);
        try (Response<BinaryData> response = httpPipeline.send(request)) {
            BinaryData responseBody = response.getValue();
            return PollingUtils.deserializeResponse(responseBody, serializer, resultType);
        }
    }

    private String setServiceVersionQueryParam(String url) {
        if (!CoreUtils.isNullOrEmpty(this.serviceVersion)) {
            UriBuilder urlBuilder = UriBuilder.parse(url);
            urlBuilder.setQueryParameter("api-version", this.serviceVersion);
            url = urlBuilder.toString();
        }
        return url;
    }
}
