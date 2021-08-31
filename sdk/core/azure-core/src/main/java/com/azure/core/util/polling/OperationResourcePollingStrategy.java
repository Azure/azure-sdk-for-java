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
import com.fasterxml.jackson.annotation.JsonSetter;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Implements a operation resource polling strategy, typically from Operation-Location.
 *
 * @param <T> the type of the response type from a polling call, or BinaryData if raw response body should be kept
 * @param <U> the type of the final result object to deserialize into, or BinaryData if raw response body should be
 *           kept
 */
public class OperationResourcePollingStrategy<T, U> implements PollingStrategy<T, U> {
    private static final SerializerAdapter SERIALIZER = JacksonAdapter.createDefaultSerializerAdapter();

    private final HttpPipeline httpPipeline;
    private final Context context;
    private final String operationLocationHeaderName;

    /**
     * Creates an instance of the operation resource polling strategy.
     *
     * @param httpPipeline an instance of {@link HttpPipeline} to send requests with
     * @param context additional metadata to pass along with the request
     */
    public OperationResourcePollingStrategy(HttpPipeline httpPipeline, Context context) {
        this.httpPipeline = httpPipeline;
        this.context = context;
        this.operationLocationHeaderName = "Operation-Location";
    }

    /**
     * Creates an instance of the operation resource polling strategy.
     *
     * @param httpPipeline an instance of {@link HttpPipeline} to send requests with
     * @param context additional metadata to pass along with the request
     */
    public OperationResourcePollingStrategy(HttpPipeline httpPipeline, Context context,
                                            String operationLocationHeaderName) {
        this.httpPipeline = httpPipeline;
        this.context = context;
        this.operationLocationHeaderName = operationLocationHeaderName;
    }

    @Override
    public Mono<Boolean> canPoll(Response<?> initialResponse) {
        HttpHeader operationLocationHeader = initialResponse.getHeaders().get(operationLocationHeaderName);
        return Mono.just(operationLocationHeader != null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Mono<PollResponse<T>> onInitialResponse(Response<?> response, PollingContext<T> pollingContext,
                                                   TypeReference<T> pollResponseType) {
        HttpHeader operationLocationHeader = response.getHeaders().get(operationLocationHeaderName);
        HttpHeader locationHeader = response.getHeaders().get(PollingConstants.LOCATION);
        if (operationLocationHeader != null) {
            pollingContext.setData(operationLocationHeaderName, operationLocationHeader.getValue());
        }
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
        HttpRequest request = new HttpRequest(HttpMethod.GET, pollingContext.getData(operationLocationHeaderName));
        return httpPipeline.send(request, context).flatMap(res -> res.getBodyAsString()
            .flatMap(body -> Mono.fromCallable(() ->
                    SERIALIZER.<PollResult>deserialize(body, PollResult.class, SerializerEncoding.JSON))
                .map(pollResult -> {
                    if (pollResult.getResourceLocation() != null) {
                        pollingContext.setData(PollingConstants.RESOURCE_LOCATION, pollResult.getResourceLocation());
                    }
                    pollingContext.setData(PollingConstants.POLL_RESPONSE_BODY, body);
                    return pollResult.getStatus();
                })
                .flatMap(status -> {
                    String retryAfter = res.getHeaderValue(PollingConstants.RETRY_AFTER);
                    return Mono.fromCallable(() -> {
                        if (TypeUtil.isTypeOrSubTypeOf(BinaryData.class, pollResponseType.getJavaType())) {
                            return (T) BinaryData.fromString(body);
                        } else {
                            return SERIALIZER.deserialize(body, pollResponseType.getJavaType(),
                                SerializerEncoding.JSON);
                        }
                    }).map(pollResponse -> {
                        if (retryAfter != null) {
                            return new PollResponse<>(status, pollResponse,
                                Duration.ofSeconds(Long.parseLong(retryAfter)));
                        } else {
                            return new PollResponse<>(status, pollResponse);
                        }
                    });
                })));
    }

    @SuppressWarnings("unchecked")
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
            } else if (HttpMethod.POST.name().equalsIgnoreCase(httpMethod)
                    && pollingContext.getData(PollingConstants.LOCATION) != null) {
                finalGetUrl = pollingContext.getData(PollingConstants.LOCATION);
            } else {
                return Mono.error(new AzureException("Cannot get final result"));
            }
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

    /**
     * A simple structure representing the partial response received from an operation location URL, containing the
     * information of the status of the long running operation.
     */
    private static class PollResult {
        private LongRunningOperationStatus status;
        private String resourceLocation;

        /**
         * Gets the status of the long running operation.
         * @return the status represented as a {@link LongRunningOperationStatus}
         */
        public LongRunningOperationStatus getStatus() {
            return status;
        }

        /**
         * Sets the long running operation status in the format of a string returned by the service. This is called by
         * the deserializer when a response is received.
         *
         * @param status the status of the long running operation
         * @return the modified PollResult instance
         */
        @JsonSetter
        public PollResult setStatus(String status) {
            if (PollingConstants.STATUS_NOT_STARTED.equalsIgnoreCase(status)) {
                this.status = LongRunningOperationStatus.NOT_STARTED;
            } else if (PollingConstants.STATUS_IN_PROGRESS.equalsIgnoreCase(status)
                || PollingConstants.STATUS_RUNNING.equalsIgnoreCase(status)) {
                this.status = LongRunningOperationStatus.IN_PROGRESS;
            } else if (PollingConstants.STATUS_SUCCEEDED.equalsIgnoreCase(status)) {
                this.status = LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;
            } else if (PollingConstants.STATUS_FAILED.equalsIgnoreCase(status)) {
                this.status = LongRunningOperationStatus.FAILED;
            } else {
                this.status = LongRunningOperationStatus.fromString(status, true);
            }
            return this;
        }

        /**
         * Sets the long running operation status in the format of the {@link LongRunningOperationStatus} enum.
         *
         * @param status the status of the long running operation
         * @return the modified PollResult instance
         */
        public PollResult setStatus(LongRunningOperationStatus status) {
            this.status = status;
            return this;
        }

        /**
         * Gets the resource location URL to get the final result. This is often available in the response when the
         * long running operation has been successfully completed.
         *
         * @return the resource location URL to get he final result
         */
        public String getResourceLocation() {
            return resourceLocation;
        }

        /**
         * Sets the resource location URL. this should only be called by the deserializer when a response is received.
         *
         * @param resourceLocation the resource location URL
         * @return the modified PollResult instance
         */
        public PollResult setResourceLocation(String resourceLocation) {
            this.resourceLocation = resourceLocation;
            return this;
        }
    }
}
