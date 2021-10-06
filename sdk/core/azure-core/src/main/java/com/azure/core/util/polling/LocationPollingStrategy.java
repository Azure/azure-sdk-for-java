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
import com.azure.core.implementation.serializer.DefaultJsonSerializer;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.implementation.PollingConstants;
import com.azure.core.util.polling.implementation.PollingUtils;
import com.azure.core.util.serializer.ObjectSerializer;
import com.azure.core.util.serializer.TypeReference;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.Objects;

/**
 * Implements a Location polling strategy.
 *
 * @param <T> the type of the response type from a polling call, or BinaryData if raw response body should be kept
 * @param <U> the type of the final result object to deserialize into, or BinaryData if raw response body should be
 *           kept
 */
public class LocationPollingStrategy<T, U> implements PollingStrategy<T, U> {

    private final HttpPipeline httpPipeline;
    private final ObjectSerializer serializer;

    /**
     * Creates an instance of the location polling strategy using a JSON serializer.
     *
     * @param httpPipeline an instance of {@link HttpPipeline} to send requests with
     */
    public LocationPollingStrategy(HttpPipeline httpPipeline) {
        this(httpPipeline, new DefaultJsonSerializer());
    }

    /**
     * Creates an instance of the location polling strategy.
     *
     * @param httpPipeline an instance of {@link HttpPipeline} to send requests with
     * @param serializer a custom serializer for serializing and deserializing polling responses
     */
    public LocationPollingStrategy(HttpPipeline httpPipeline, ObjectSerializer serializer) {
        this.httpPipeline = Objects.requireNonNull(httpPipeline, "'httpPipeline' cannot be null");
        this.serializer = Objects.requireNonNull(serializer, "'serializer' cannot be null");
    }

    @Override
    public Mono<Boolean> canPoll(Response<?> initialResponse) {
        HttpHeader locationHeader = initialResponse.getHeaders().get(PollingConstants.LOCATION);
        if (locationHeader != null) {
            try {
                new URL(locationHeader.getValue());
                return Mono.just(true);
            } catch (MalformedURLException e) {
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
            pollingContext.setData(PollingConstants.LOCATION, locationHeader.getValue());
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
                .switchIfEmpty(Mono.defer(() -> Mono.just(new PollResponse<>(
                    LongRunningOperationStatus.IN_PROGRESS, null, retryAfter))));
        } else {
            return Mono.error(new AzureException(String.format("Operation failed or cancelled with status code %d,"
                + ", 'Location' header: %s, and response body: %s", response.getStatusCode(), locationHeader,
                PollingUtils.serializeResponse(response.getValue(), serializer))));
        }
    }

    @Override
    public Mono<PollResponse<T>> poll(PollingContext<T> pollingContext, TypeReference<T> pollResponseType) {
        HttpRequest request = new HttpRequest(HttpMethod.GET, pollingContext.getData(PollingConstants.LOCATION));
        return httpPipeline.send(request).flatMap(response -> {
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
                String retryAfterValue = response.getHeaders().getValue(PollingConstants.RETRY_AFTER);
                Duration retryAfter = retryAfterValue == null ? null
                    : Duration.ofSeconds(Long.parseLong(retryAfterValue));
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
            return httpPipeline.send(request)
                .flatMap(HttpResponse::getBodyAsByteArray)
                .map(BinaryData::fromBytes)
                .flatMap(binaryData -> PollingUtils.deserializeResponse(binaryData, serializer, resultType));
        }
    }
}
