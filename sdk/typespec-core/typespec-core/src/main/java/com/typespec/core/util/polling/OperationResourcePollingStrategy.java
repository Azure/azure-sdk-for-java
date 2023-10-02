// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.util.polling;

import com.typespec.core.exception.AzureException;
import com.typespec.core.http.HttpHeader;
import com.typespec.core.http.HttpHeaderName;
import com.typespec.core.http.HttpMethod;
import com.typespec.core.http.HttpPipeline;
import com.typespec.core.http.HttpRequest;
import com.typespec.core.http.HttpResponse;
import com.typespec.core.http.rest.Response;
import com.typespec.core.implementation.ImplUtils;
import com.typespec.core.implementation.serializer.DefaultJsonSerializer;
import com.typespec.core.util.BinaryData;
import com.typespec.core.util.Context;
import com.typespec.core.util.CoreUtils;
import com.typespec.core.util.FluxUtil;
import com.typespec.core.util.UrlBuilder;
import com.typespec.core.util.logging.ClientLogger;
import com.typespec.core.util.polling.implementation.PollResult;
import com.typespec.core.util.polling.implementation.PollingConstants;
import com.typespec.core.util.polling.implementation.PollingUtils;
import com.typespec.core.util.serializer.ObjectSerializer;
import com.typespec.core.util.serializer.TypeReference;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Objects;

import static com.typespec.core.util.polling.PollingUtil.operationResourceCanPoll;
import static com.typespec.core.util.polling.implementation.PollingUtils.getAbsolutePath;

/**
 * Implements an operation resource polling strategy, typically from Operation-Location.
 *
 * @param <T> the type of the response type from a polling call, or BinaryData if raw response body should be kept
 * @param <U> the type of the final result object to deserialize into, or BinaryData if raw response body should be
 *           kept
 */
public class OperationResourcePollingStrategy<T, U> implements PollingStrategy<T, U> {
    private static final ClientLogger LOGGER = new ClientLogger(OperationResourcePollingStrategy.class);
    private static final HttpHeaderName DEFAULT_OPERATION_LOCATION_HEADER
        = HttpHeaderName.fromString("Operation-Location");
    private static final TypeReference<PollResult> POLL_RESULT_TYPE_REFERENCE
        = TypeReference.createInstance(PollResult.class);

    private final HttpPipeline httpPipeline;
    private final ObjectSerializer serializer;
    private final String endpoint;
    private final HttpHeaderName operationLocationHeaderName;
    private final Context context;
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
     * @param httpPipeline an instance of {@link HttpPipeline} to send requests with
     * @param serializer a custom serializer for serializing and deserializing polling responses
     * @param operationLocationHeaderName a custom header for polling the long-running operation
     */
    public OperationResourcePollingStrategy(HttpPipeline httpPipeline, ObjectSerializer serializer,
                                            String operationLocationHeaderName) {
        this(httpPipeline, serializer, operationLocationHeaderName, Context.NONE);
    }

    /**
     * Creates an instance of the operation resource polling strategy.
     * @param httpPipeline an instance of {@link HttpPipeline} to send requests with
     * @param serializer a custom serializer for serializing and deserializing polling responses
     * @param operationLocationHeaderName a custom header for polling the long-running operation
     * @param context an instance of {@link com.typespec.core.util.Context}
     */
    public OperationResourcePollingStrategy(HttpPipeline httpPipeline, ObjectSerializer serializer,
                                            String operationLocationHeaderName, Context context) {
        this(httpPipeline, null, serializer, operationLocationHeaderName, context);
    }

    /**
     * Creates an instance of the operation resource polling strategy.
     *
     * @param httpPipeline an instance of {@link HttpPipeline} to send requests with.
     * @param endpoint an endpoint for creating an absolute path when the path itself is relative.
     * @param serializer a custom serializer for serializing and deserializing polling responses.
     * @param operationLocationHeaderName a custom header for polling the long-running operation.
     * @param context an instance of {@link com.typespec.core.util.Context}.
     */
    public OperationResourcePollingStrategy(HttpPipeline httpPipeline, String endpoint, ObjectSerializer serializer,
        String operationLocationHeaderName, Context context) {
        this(operationLocationHeaderName == null ? null : HttpHeaderName.fromString(operationLocationHeaderName),
            new PollingStrategyOptions(httpPipeline)
                .setEndpoint(endpoint)
                .setSerializer(serializer)
                .setContext(context));
    }

    /**
     * Creates an instance of the operation resource polling strategy.
     *
     * @param operationLocationHeaderName a custom header for polling the long-running operation.
     * @param pollingStrategyOptions options to configure this polling strategy.
     * @throws NullPointerException if {@code pollingStrategyOptions} is null.
     */
    public OperationResourcePollingStrategy(HttpHeaderName operationLocationHeaderName, PollingStrategyOptions pollingStrategyOptions) {
        Objects.requireNonNull(pollingStrategyOptions, "'pollingStrategyOptions' cannot be null");
        this.httpPipeline = pollingStrategyOptions.getHttpPipeline();
        this.endpoint = pollingStrategyOptions.getEndpoint();
        this.serializer = pollingStrategyOptions.getSerializer() != null ? pollingStrategyOptions.getSerializer() : new DefaultJsonSerializer();
        this.operationLocationHeaderName = (operationLocationHeaderName == null)
            ? DEFAULT_OPERATION_LOCATION_HEADER : operationLocationHeaderName;

        this.serviceVersion = pollingStrategyOptions.getServiceVersion();
        this.context = pollingStrategyOptions.getContext() == null ? Context.NONE : pollingStrategyOptions.getContext();
    }

    @Override
    public Mono<Boolean> canPoll(Response<?> initialResponse) {
        return Mono.fromSupplier(() ->
            operationResourceCanPoll(initialResponse, operationLocationHeaderName, endpoint, LOGGER));
    }

    @Override
    public Mono<PollResponse<T>> onInitialResponse(Response<?> response, PollingContext<T> pollingContext,
                                                   TypeReference<T> pollResponseType) {
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
        pollingContext.setData(PollingConstants.REQUEST_URL, response.getRequest().getUrl().toString());

        if (response.getStatusCode() == 200
                || response.getStatusCode() == 201
                || response.getStatusCode() == 202
                || response.getStatusCode() == 204) {
            Duration retryAfter = ImplUtils.getRetryAfterFromHeaders(response.getHeaders(), OffsetDateTime::now);
            return PollingUtils.convertResponse(response.getValue(), serializer, pollResponseType)
                .map(value -> new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS, value, retryAfter))
                .switchIfEmpty(Mono.fromSupplier(() -> new PollResponse<>(
                    LongRunningOperationStatus.IN_PROGRESS, null, retryAfter)));
        } else {
            return Mono.error(new AzureException(String.format("Operation failed or cancelled with status code %d,"
                + ", '%s' header: %s, and response body: %s", response.getStatusCode(), operationLocationHeaderName,
                operationLocationHeader, PollingUtils.serializeResponse(response.getValue(), serializer))));
        }
    }

    @Override
    public Mono<PollResponse<T>> poll(PollingContext<T> pollingContext, TypeReference<T> pollResponseType) {
        String url = pollingContext.getData(operationLocationHeaderName
            .getCaseSensitiveName());

        url = setServiceVersionQueryParam(url);
        HttpRequest request = new HttpRequest(HttpMethod.GET, url);
        return FluxUtil.withContext(context1 -> httpPipeline.send(request,
                CoreUtils.mergeContexts(context1, this.context))).flatMap(response -> response.getBodyAsByteArray()
            .map(BinaryData::fromBytes)
            .flatMap(binaryData -> PollingUtils.deserializeResponse(binaryData, serializer, POLL_RESULT_TYPE_REFERENCE)
                .map(pollResult -> {
                    final String resourceLocation = pollResult.getResourceLocation();
                    if (resourceLocation != null) {
                        pollingContext.setData(PollingConstants.RESOURCE_LOCATION,
                            getAbsolutePath(resourceLocation, endpoint, LOGGER));
                    }
                    pollingContext.setData(PollingConstants.POLL_RESPONSE_BODY, binaryData.toString());
                    return pollResult.getStatus();
                })
                .flatMap(status -> {
                    Duration retryAfter = ImplUtils.getRetryAfterFromHeaders(response.getHeaders(),
                        OffsetDateTime::now);
                    return PollingUtils.deserializeResponse(binaryData, serializer, pollResponseType)
                        .map(value -> new PollResponse<>(status, value, retryAfter));
                })));
    }

    private String setServiceVersionQueryParam(String url) {
        if (!CoreUtils.isNullOrEmpty(this.serviceVersion)) {
            UrlBuilder urlBuilder = UrlBuilder.parse(url);
            urlBuilder.setQueryParameter("api-version", this.serviceVersion);
            url = urlBuilder.toString();
        }
        return url;
    }

    @Override
    public Mono<U> getResult(PollingContext<T> pollingContext, TypeReference<U> resultType) {
        if (pollingContext.getLatestResponse().getStatus() == LongRunningOperationStatus.FAILED) {
            return Mono.error(new AzureException("Long running operation failed."));
        } else if (pollingContext.getLatestResponse().getStatus() == LongRunningOperationStatus.USER_CANCELLED) {
            return Mono.error(new AzureException("Long running operation cancelled."));
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
                return Mono.error(new AzureException("Cannot get final result"));
            }
        }

        if (finalGetUrl == null) {
            String latestResponseBody = pollingContext.getData(PollingConstants.POLL_RESPONSE_BODY);
            return PollingUtils.deserializeResponse(BinaryData.fromString(latestResponseBody), serializer, resultType);
        } else {
            finalGetUrl = setServiceVersionQueryParam(finalGetUrl);
            HttpRequest request = new HttpRequest(HttpMethod.GET, finalGetUrl);
            return FluxUtil.withContext(context1 -> httpPipeline.send(request,
                    CoreUtils.mergeContexts(context1, this.context)))
                .flatMap(HttpResponse::getBodyAsByteArray)
                .map(BinaryData::fromBytes)
                .flatMap(binaryData -> PollingUtils.deserializeResponse(binaryData, serializer, resultType));
        }
    }
}
