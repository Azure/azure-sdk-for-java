// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.polling;

import com.azure.core.exception.AzureException;
import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.implementation.ImplUtils;
import com.azure.core.implementation.serializer.DefaultJsonSerializer;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.polling.implementation.PollingConstants;
import com.azure.core.util.polling.implementation.PollingUtils;
import com.azure.core.util.serializer.ObjectSerializer;
import com.azure.core.util.serializer.TypeReference;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Objects;

import static com.azure.core.util.polling.implementation.PollingUtils.getAbsolutePath;

/**
 * Implements a Location polling strategy.
 *
 * @param <T> the type of the response type from a polling call, or BinaryData if raw response body should be kept
 * @param <U> the type of the final result object to deserialize into, or BinaryData if raw response body should be
 *           kept
 */
public class LocationPollingStrategy<T, U> implements PollingStrategy<T, U> {
    private static final ObjectSerializer DEFAULT_SERIALIZER = new DefaultJsonSerializer();

    private static final ClientLogger LOGGER = new ClientLogger(LocationPollingStrategy.class);

    private final String endpoint;
    private final HttpPipeline httpPipeline;
    private final ObjectSerializer serializer;
    private final Context context;

    /**
     * Creates an instance of the location polling strategy using a JSON serializer.
     *
     * @param httpPipeline an instance of {@link HttpPipeline} to send requests with
     * @throws NullPointerException If {@code httpPipeline} is null.
     */
    public LocationPollingStrategy(HttpPipeline httpPipeline) {
        this(httpPipeline, DEFAULT_SERIALIZER, Context.NONE);
    }

    /**
     * Creates an instance of the location polling strategy.
     *
     * @param httpPipeline an instance of {@link HttpPipeline} to send requests with
     * @param serializer a custom serializer for serializing and deserializing polling responses
     * @throws NullPointerException If {@code httpPipeline} is null.
     */
    public LocationPollingStrategy(HttpPipeline httpPipeline, ObjectSerializer serializer) {
        this(httpPipeline, serializer, Context.NONE);
    }

    /**
     * Creates an instance of the location polling strategy.
     *
     * @param httpPipeline an instance of {@link HttpPipeline} to send requests with
     * @param serializer a custom serializer for serializing and deserializing polling responses
     * @param context an instance of {@link Context}
     * @throws NullPointerException If {@code httpPipeline} is null.
     */
    public LocationPollingStrategy(HttpPipeline httpPipeline, ObjectSerializer serializer, Context context) {
      this(httpPipeline, null, serializer, context);
    }

    /**
     * Creates an instance of the location polling strategy.
     *
     * @param httpPipeline an instance of {@link HttpPipeline} to send requests with
     * @param endpoint an endpoint for create an absolute path when the path itself is relative.
     * @param serializer a custom serializer for serializing and deserializing polling responses
     * @param context an instance of {@link Context}
     * @throws NullPointerException If {@code httpPipeline} is null.
     */
    public LocationPollingStrategy(HttpPipeline httpPipeline, String endpoint, ObjectSerializer serializer, Context context) {
        this.httpPipeline = Objects.requireNonNull(httpPipeline, "'httpPipeline' cannot be null");
        this.endpoint = endpoint;
        this.serializer = (serializer == null) ? DEFAULT_SERIALIZER : serializer;
        this.context = context == null ? Context.NONE : context;
    }

    @Override
    public Mono<Boolean> canPoll(Response<?> initialResponse) {
        HttpHeader locationHeader = initialResponse.getHeaders().get(PollingConstants.LOCATION);
        if (locationHeader != null) {
            try {
                new URL(getAbsolutePath(locationHeader.getValue(), endpoint, LOGGER));
                return Mono.just(true);
            } catch (MalformedURLException e) {
                LOGGER.info("Failed to parse Location header into a URL.", e);
                return Mono.just(false);
            }
        }
        return Mono.just(false);
    }

    @Override
    public Mono<PollResponse<T>> onInitialResponse(Response<?> response, PollingContext<T> pollingContext,
                                                   TypeReference<T> pollResponseType) {
        HttpHeader locationHeader = response.getHeaders().get(PollingConstants.LOCATION);
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
            String retryAfterValue = response.getHeaders().getValue(PollingConstants.RETRY_AFTER);
            Duration retryAfter = retryAfterValue == null ? null : Duration.ofSeconds(Long.parseLong(retryAfterValue));
            return PollingUtils.convertResponse(response.getValue(), serializer, pollResponseType)
                .map(value -> new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS, value, retryAfter))
                .switchIfEmpty(Mono.fromSupplier(() -> new PollResponse<>(
                    LongRunningOperationStatus.IN_PROGRESS, null, retryAfter)));
        } else {
            return Mono.error(new AzureException(String.format("Operation failed or cancelled with status code %d,"
                + ", 'Location' header: %s, and response body: %s", response.getStatusCode(), locationHeader,
                PollingUtils.serializeResponse(response.getValue(), serializer))));
        }
    }

    @Override
    public Mono<PollResponse<T>> poll(PollingContext<T> pollingContext, TypeReference<T> pollResponseType) {
        HttpRequest request = new HttpRequest(HttpMethod.GET, pollingContext.getData(PollingConstants.LOCATION));
        return FluxUtil.withContext(context1 -> httpPipeline.send(request,
                CoreUtils.mergeContexts(context1, this.context)))
            .flatMap(response -> {
                HttpHeader locationHeader = response.getHeaders().get(PollingConstants.LOCATION);
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

                return response.getBodyAsByteArray().map(BinaryData::fromBytes).flatMap(binaryData -> {
                    pollingContext.setData(PollingConstants.POLL_RESPONSE_BODY, binaryData.toString());
                    Duration retryAfter = ImplUtils.getRetryAfterFromHeaders(response.getHeaders(), OffsetDateTime::now);
                    return PollingUtils.deserializeResponse(binaryData, serializer, pollResponseType)
                        .map(value -> new PollResponse<>(status, value, retryAfter));
                });
            });
    }

    @Override
    public Mono<U> getResult(PollingContext<T> pollingContext, TypeReference<U> resultType) {
        if (pollingContext.getLatestResponse().getStatus() == LongRunningOperationStatus.FAILED) {
            return Mono.error(new AzureException("Long running operation failed."));
        } else if (pollingContext.getLatestResponse().getStatus() == LongRunningOperationStatus.USER_CANCELLED) {
            return Mono.error(new AzureException("Long running operation cancelled."));
        }

        String finalGetUrl;
        String httpMethod = pollingContext.getData(PollingConstants.HTTP_METHOD);
        if (HttpMethod.PUT.name().equalsIgnoreCase(httpMethod)
                || HttpMethod.PATCH.name().equalsIgnoreCase(httpMethod)) {
            finalGetUrl = pollingContext.getData(PollingConstants.REQUEST_URL);
        } else if (HttpMethod.POST.name().equalsIgnoreCase(httpMethod)
                && pollingContext.getData(PollingConstants.LOCATION) != null) {
            finalGetUrl = pollingContext.getData(PollingConstants.LOCATION);
        } else {
            return Mono.error(new AzureException("Cannot get final result"));
        }

        if (finalGetUrl == null) {
            String latestResponseBody = pollingContext.getData(PollingConstants.POLL_RESPONSE_BODY);
            return PollingUtils.deserializeResponse(BinaryData.fromString(latestResponseBody), serializer, resultType);
        } else {
            HttpRequest request = new HttpRequest(HttpMethod.GET, finalGetUrl);
            return FluxUtil.withContext(context1 -> httpPipeline.send(request,
                    CoreUtils.mergeContexts(context1, this.context)))
                .flatMap(HttpResponse::getBodyAsByteArray)
                .map(BinaryData::fromBytes)
                .flatMap(binaryData -> PollingUtils.deserializeResponse(binaryData, serializer, resultType));
        }
    }
}
