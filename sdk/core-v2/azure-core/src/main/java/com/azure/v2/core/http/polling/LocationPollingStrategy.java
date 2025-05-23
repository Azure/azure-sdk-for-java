// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.core.http.polling;

import com.azure.v2.core.implementation.ImplUtils;
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
import static com.azure.v2.core.implementation.polling.PollingUtils.locationCanPoll;
import static com.azure.v2.core.implementation.polling.PollingUtils.serializeResponse;

/**
 * Implements a Location polling strategy.
 *
 * @param <T> the type of the response type from a polling call, or BinaryData if raw response body should be kept
 * @param <U> the type of the final result object to deserialize into, or BinaryData if raw response body should be
 * kept
 */
public class LocationPollingStrategy<T, U> implements PollingStrategy<T, U> {

    private static final ClientLogger LOGGER = new ClientLogger(LocationPollingStrategy.class);

    private final String endpoint;
    private final HttpPipeline httpPipeline;
    private final ObjectSerializer serializer;
    private final RequestContext requestContext;
    private final String serviceVersion;

    /**
     * Creates an instance of the location polling strategy using a JSON serializer.
     *
     * @param httpPipeline an instance of {@link HttpPipeline} to send requests with
     * @throws NullPointerException If {@code httpPipeline} is null.
     */
    public LocationPollingStrategy(HttpPipeline httpPipeline) {
        this(httpPipeline, JsonSerializer.getInstance(), RequestContext.none());
    }

    /**
     * Creates an instance of the location polling strategy.
     *
     * @param httpPipeline an instance of {@link HttpPipeline} to send requests with
     * @param serializer a custom serializer for serializing and deserializing polling responses
     * @throws NullPointerException If {@code httpPipeline} is null.
     */
    public LocationPollingStrategy(HttpPipeline httpPipeline, ObjectSerializer serializer) {
        this(httpPipeline, serializer, RequestContext.none());
    }

    /**
     * Creates an instance of the location polling strategy.
     *
     * @param httpPipeline an instance of {@link HttpPipeline} to send requests with
     * @param serializer a custom serializer for serializing and deserializing polling responses
     * @param requestContext an instance of {@link RequestContext}
     * @throws NullPointerException If {@code httpPipeline} is null.
     */
    public LocationPollingStrategy(HttpPipeline httpPipeline, ObjectSerializer serializer,
        RequestContext requestContext) {
        this(httpPipeline, null, serializer, requestContext);
    }

    /**
     * Creates an instance of the location polling strategy.
     *
     * @param httpPipeline an instance of {@link HttpPipeline} to send requests with
     * @param endpoint an endpoint for creating an absolute path when the path itself is relative.
     * @param serializer a custom serializer for serializing and deserializing polling responses
     * @param requestContext an instance of {@link RequestContext}
     * @throws NullPointerException If {@code httpPipeline} is null.
     */
    public LocationPollingStrategy(HttpPipeline httpPipeline, String endpoint, ObjectSerializer serializer,
        RequestContext requestContext) {
        this(new PollingStrategyOptions(httpPipeline).setEndpoint(endpoint)
            .setSerializer(serializer)
            .setRequestContext(requestContext));
    }

    /**
     * Creates an instance of the location polling strategy.
     *
     * @param pollingStrategyOptions options to configure this polling strategy.
     * @throws NullPointerException If {@code pollingStrategyOptions} is null.
     */
    public LocationPollingStrategy(PollingStrategyOptions pollingStrategyOptions) {
        Objects.requireNonNull(pollingStrategyOptions, "'pollingStrategyOptions' cannot be null");
        this.httpPipeline = pollingStrategyOptions.getHttpPipeline();
        this.endpoint = pollingStrategyOptions.getEndpoint();
        this.serializer = (pollingStrategyOptions.getSerializer() == null)
            ? JsonSerializer.getInstance()
            : pollingStrategyOptions.getSerializer();
        this.serviceVersion = pollingStrategyOptions.getServiceVersion();
        this.requestContext = pollingStrategyOptions.getRequestContext() == null
            ? RequestContext.none()
            : pollingStrategyOptions.getRequestContext();
    }

    @Override
    public boolean canPoll(Response<T> initialResponse) {
        return locationCanPoll(initialResponse, endpoint, LOGGER);
    }

    @Override
    public PollResponse<T> onInitialResponse(Response<T> response, PollingContext<T> pollingContext,
        Type pollResponseType) {
        HttpHeader locationHeader = response.getHeaders().get(HttpHeaderName.LOCATION);
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
            .addKeyValue("http.response.header.location", locationHeader == null ? null : locationHeader.getValue())
            .addKeyValue("http.response.body.content", serializeResponse(response.getValue(), serializer).toString())
            .log("Operation failed or cancelled",
                message -> new HttpResponseException(message, binaryDataResponse, null));
    }

    @Override
    public PollResponse<T> poll(PollingContext<T> pollingContext, Type pollResponseType) {
        String uri = pollingContext.getData(PollingConstants.LOCATION);
        uri = setServiceVersionQueryParam(uri);
        HttpRequest request = new HttpRequest().setMethod(HttpMethod.GET).setUri(uri).setContext(requestContext);

        try (Response<BinaryData> response = httpPipeline.send(request)) {
            HttpHeader locationHeader = response.getHeaders().get(HttpHeaderName.LOCATION);

            if (locationHeader != null) {
                pollingContext.setData(PollingConstants.LOCATION, locationHeader.getValue());
            }

            LongRunningOperationStatus status;
            if (response.getStatusCode() == 202) {
                status = LongRunningOperationStatus.IN_PROGRESS;
            } else if (response.getStatusCode() >= 200 && response.getStatusCode() <= 204) {
                status = LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;
            } else {
                status = LongRunningOperationStatus.FAILED;
            }

            BinaryData responseBody = response.getValue().toReplayableBinaryData();
            pollingContext.setData(PollingConstants.POLL_RESPONSE_BODY, responseBody.toString());
            Duration retryAfter = ImplUtils.getRetryAfterFromHeaders(response.getHeaders(), OffsetDateTime::now);

            return new PollResponse<>(status,
                PollingUtils.deserializeResponse(responseBody, serializer, pollResponseType), retryAfter);
        }
    }

    @Override
    public U getResult(PollingContext<T> pollingContext, Type resultType) {
        if (pollingContext.getLatestResponse().getStatus() == LongRunningOperationStatus.FAILED) {
            throw LOGGER.throwableAtError().log("Long-running operation failed.", CoreException::from);
        } else if (pollingContext.getLatestResponse().getStatus() == LongRunningOperationStatus.USER_CANCELLED) {
            throw LOGGER.throwableAtError().log("Long-running operation cancelled.", CoreException::from);
        }

        String finalGetUrl;
        String httpMethod = pollingContext.getData(PollingConstants.HTTP_METHOD);
        if (HttpMethod.PUT.name().equalsIgnoreCase(httpMethod)
            || HttpMethod.PATCH.name().equalsIgnoreCase(httpMethod)) {
            finalGetUrl = pollingContext.getData(PollingConstants.REQUEST_URL);
        } else if (HttpMethod.POST.name().equalsIgnoreCase(httpMethod)) {
            finalGetUrl = pollingContext.getData(PollingConstants.LOCATION);
        } else {
            throw LOGGER.throwableAtError().log("Cannot get final result", CoreException::from);
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
            UriBuilder uriBuilder = UriBuilder.parse(url);
            uriBuilder.setQueryParameter("api-version", this.serviceVersion);
            url = uriBuilder.toString();
        }
        return url;
    }
}
