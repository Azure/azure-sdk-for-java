// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.polling;

import com.azure.core.exception.AzureException;
import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.Response;
import com.azure.core.implementation.TypeUtil;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.polling.implementation.PollingConstants;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.core.util.serializer.TypeReference;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Implements a Location polling strategy.
 *
 * @param <T> the type of the response type from a polling call, or BinaryData if raw response body should be kept
 * @param <U> the type of the final result object to deserialize into, or BinaryData if raw response body should be
 *           kept
 */
public class LocationPollingStrategy<T, U> implements PollingStrategy<T, U> {
    private static final SerializerAdapter SERIALIZER = JacksonAdapter.createDefaultSerializerAdapter();

    private final HttpPipeline httpPipeline;
    private final Context context;

    /**
     * Creates an instance of the location polling strategy.
     *
     * @param httpPipeline an instance of {@link HttpPipeline} to send requests with
     * @param context additional metadata to pass along with the request
     */
    public LocationPollingStrategy(
            HttpPipeline httpPipeline,
            Context context) {
        this.httpPipeline = httpPipeline;
        this.context = context;
    }

    @Override
    public Mono<Boolean> canPoll(Response<?> initialResponse) {
        HttpHeader locationHeader = initialResponse.getHeaders().get(PollingConstants.LOCATION);
        return Mono.just(locationHeader != null);
    }

    @SuppressWarnings("unchecked")
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
            return Mono.just(LongRunningOperationStatus.IN_PROGRESS).flatMap(status -> {
                if (response.getValue() == null) {
                    return Mono.just(new PollResponse<>(status, null));
                } else if (TypeUtil.isTypeOrSubTypeOf(
                        response.getValue().getClass(), pollResponseType.getJavaType())) {
                    return Mono.just(new PollResponse<>(status, (T) response.getValue()));
                } else {
                    Mono<BinaryData> binaryDataMono;
                    if (response.getValue() instanceof BinaryData) {
                        binaryDataMono = Mono.just((BinaryData) response.getValue());
                    } else {
                        binaryDataMono = BinaryData.fromObjectAsync(response.getValue());
                    }
                    if (TypeUtil.isTypeOrSubTypeOf(BinaryData.class, pollResponseType.getJavaType())) {
                        return binaryDataMono.map(binaryData -> new PollResponse<>(status, (T) binaryData));
                    } else {
                        return binaryDataMono.flatMap(binaryData -> binaryData.toObjectAsync(pollResponseType))
                            .map(value -> new PollResponse<>(status, value));
                    }
                }
            });
        } else {
            return Mono.error(new AzureException("Operation failed or cancelled: " + response.getStatusCode()));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Mono<PollResponse<T>> poll(PollingContext<T> pollingContext, TypeReference<T> pollResponseType) {
        HttpRequest request = new HttpRequest(HttpMethod.GET, pollingContext.getData(PollingConstants.LOCATION));
        return httpPipeline.send(request, context).flatMap(res -> {
            HttpHeader locationHeader = res.getHeaders().get(PollingConstants.LOCATION);
            if (locationHeader != null) {
                pollingContext.setData(PollingConstants.LOCATION, locationHeader.getValue());
            }

            LongRunningOperationStatus status;
            if (res.getStatusCode() == 202) {
                status = LongRunningOperationStatus.IN_PROGRESS;
            } else if (res.getStatusCode() >= 200 && res.getStatusCode() <= 204) {
                status = LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;
            } else {
                status = LongRunningOperationStatus.FAILED;
            }

            return res.getBodyAsString().map(BinaryData::fromString).flatMap(binaryData -> {
                pollingContext.setData(PollingConstants.POLL_RESPONSE_BODY, binaryData.toString());
                if (TypeUtil.isTypeOrSubTypeOf(BinaryData.class, pollResponseType.getJavaType())) {
                    return (Mono<T>) Mono.just(binaryData);
                } else {
                    return binaryData.toObjectAsync(pollResponseType);
                }
            }).map(pollResponse -> {
                String retryAfter = res.getHeaderValue(PollingConstants.RETRY_AFTER);
                if (retryAfter != null) {
                    return new PollResponse<>(status, pollResponse,
                        Duration.ofSeconds(Long.parseLong(retryAfter)));
                } else {
                    return new PollResponse<>(status, pollResponse);
                }
            });
        });
    }

    @SuppressWarnings("unchecked")
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
            if (TypeUtil.isTypeOrSubTypeOf(BinaryData.class, resultType.getJavaType())) {
                return (Mono<U>) Mono.just(BinaryData.fromString(latestResponseBody));
            } else {
                return Mono.fromCallable(() -> SERIALIZER.deserialize(latestResponseBody, resultType.getJavaType(),
                    SerializerEncoding.JSON));
            }
        } else {
            HttpRequest request = new HttpRequest(HttpMethod.GET, finalGetUrl);
            return httpPipeline.send(request, context).flatMap(res -> {
                if (TypeUtil.isTypeOrSubTypeOf(BinaryData.class, resultType.getJavaType())) {
                    return (Mono<U>) BinaryData.fromFlux(res.getBody());
                } else {
                    return res.getBodyAsByteArray().flatMap(body -> Mono.fromCallable(() ->
                        SERIALIZER.deserialize(body, resultType.getJavaType(), SerializerEncoding.JSON)));
                }
            });
        }
    }

    @Override
    public Mono<T> cancel(PollingContext<T> pollingContext, PollResponse<T> initialResponse) {
        return Mono.error(new IllegalStateException("Cancellation is not supported."));
    }
}
