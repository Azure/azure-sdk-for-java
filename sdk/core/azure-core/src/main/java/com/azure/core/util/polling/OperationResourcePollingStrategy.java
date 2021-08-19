// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.polling;

import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.implementation.TypeUtil;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
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
 * @param <T> the {@link TypeReference} of the response type from a polling call, or BinaryData if raw response body
 *            should be kept
 * @param <U> the {@link TypeReference} of the final result object to deserialize into, or BinaryData if raw response
 *            body should be kept
 */
public class OperationResourcePollingStrategy<T, U> implements PollingStrategy<T, U> {
    private static final String OPERATION_LOCATION = "Operation-Location";
    private static final String LOCATION = "Location";
    private static final String REQUEST_URL = "requestURL";
    private static final String HTTP_METHOD = "httpMethod";
    private static final String RESOURCE_LOCATION = "resourceLocation";
    private static final String RETRY_AFTER = "Retry-After";
    private static final String POLL_RESPONSE_BODY = "pollResponseBody";

    private final SerializerAdapter serializer = new JacksonAdapter();
    private final ClientLogger logger = new ClientLogger(OperationResourcePollingStrategy.class);

    private final HttpPipeline httpPipeline;
    private final Context context;

    /**
     * Creates an instance of the operation resource polling strategy.
     *
     * @param httpPipeline an instance of {@link HttpPipeline} to send requests with
     * @param context additional metadata to pass along with the request
     */
    public OperationResourcePollingStrategy(
            HttpPipeline httpPipeline,
            Context context) {
        this.httpPipeline = httpPipeline;
        this.context = context;
    }


    /**
     * Gets the name of the operation location header. By default it's "Operation-Location".
     * @return the name of the operation location header
     */
    public String getOperationLocationHeaderName() {
        return OPERATION_LOCATION;
    }

    @Override
    public Mono<Boolean> canPoll(Response<?> initialResponse) {
        HttpHeader operationLocationHeader = initialResponse.getHeaders().get(getOperationLocationHeaderName());
        return Mono.just(operationLocationHeader != null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Mono<LongRunningOperationStatus> onInitialResponse(Response<?> response, PollingContext<T> pollingContext,
                                                   TypeReference<T> pollResponseType) {
        HttpHeader operationLocationHeader = response.getHeaders().get(getOperationLocationHeaderName());
        HttpHeader locationHeader = response.getHeaders().get(LOCATION);
        if (operationLocationHeader != null) {
            pollingContext.setData(OPERATION_LOCATION, operationLocationHeader.getValue());
        }
        if (locationHeader != null) {
            pollingContext.setData(LOCATION, locationHeader.getValue());
        }
        pollingContext.setData(HTTP_METHOD, response.getRequest().getHttpMethod().name());
        pollingContext.setData(REQUEST_URL, response.getRequest().getUrl().toString());

        if (response.getStatusCode() == 200
                || response.getStatusCode() == 201
                || response.getStatusCode() == 202
                || response.getStatusCode() == 204) {
            return Mono.just(LongRunningOperationStatus.IN_PROGRESS);
        } else {
            return Mono.error(new RuntimeException("Operation failed or cancelled: " + response.getStatusCode()));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Mono<PollResponse<T>> poll(PollingContext<T> pollingContext, TypeReference<T> pollResponseType) {
        HttpRequest request = new HttpRequest(HttpMethod.GET, pollingContext.getData(OPERATION_LOCATION));
        Mono<HttpResponse> responseMono;
        if (context == null) {
            responseMono = httpPipeline.send(request);
        } else {
            responseMono = httpPipeline.send(request, context);
        }
        return responseMono.flatMap(res -> res.getBodyAsString()
            .flatMap(body -> Mono.fromCallable(() ->
                    serializer.<PollResult>deserialize(body, PollResult.class, SerializerEncoding.JSON))
                .map(pollResult -> {
                    if (pollResult.getResourceLocation() != null) {
                        pollingContext.setData(RESOURCE_LOCATION, pollResult.getResourceLocation());
                    }
                    pollingContext.setData(POLL_RESPONSE_BODY, body);
                    return pollResult.getStatus();
                })
                .flatMap(status -> {
                    String retryAfter = res.getHeaderValue(RETRY_AFTER);
                    return Mono.fromCallable(() -> {
                        if (TypeUtil.isTypeOrSubTypeOf(BinaryData.class, pollResponseType.getJavaType())) {
                            return (T) BinaryData.fromString(body);
                        } else {
                            return serializer.deserialize(body, pollResponseType.getJavaType(),
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
        String finalGetUrl = pollingContext.getData(RESOURCE_LOCATION);
        if (finalGetUrl == null) {
            String httpMethod = pollingContext.getData(HTTP_METHOD);
            if ("PUT".equalsIgnoreCase(httpMethod) || "PATCH".equalsIgnoreCase(httpMethod)) {
                finalGetUrl = pollingContext.getData(REQUEST_URL);
            } else if ("POST".equalsIgnoreCase(httpMethod) && pollingContext.getData(LOCATION) != null) {
                finalGetUrl = pollingContext.getData(LOCATION);
            } else {
                throw logger.logExceptionAsError(new RuntimeException("Cannot get final result"));
            }
        }

        if (finalGetUrl == null) {
            String latestResponseBody = pollingContext.getData(POLL_RESPONSE_BODY);
            if (TypeUtil.isTypeOrSubTypeOf(BinaryData.class, resultType.getJavaType())) {
                return (Mono<U>) Mono.just(BinaryData.fromString(latestResponseBody));
            } else {
                return Mono.fromCallable(() -> serializer.deserialize(latestResponseBody, resultType.getJavaType(),
                    SerializerEncoding.JSON));
            }
        } else {
            HttpRequest request = new HttpRequest(HttpMethod.GET, finalGetUrl);
            Mono<HttpResponse> responseMono;
            if (context == null) {
                responseMono = httpPipeline.send(request);
            } else {
                responseMono = httpPipeline.send(request, context);
            }
            return responseMono.flatMap(res -> {
                if (TypeUtil.isTypeOrSubTypeOf(BinaryData.class, resultType.getJavaType())) {
                    return (Mono<U>) BinaryData.fromFlux(res.getBody());
                } else {
                    return res.getBodyAsString().flatMap(body -> Mono.fromCallable(() ->
                        serializer.deserialize(body, resultType.getJavaType(), SerializerEncoding.JSON)));
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
            if ("NotStarted".equalsIgnoreCase(status)) {
                this.status = LongRunningOperationStatus.NOT_STARTED;
            } else if ("InProgress".equalsIgnoreCase(status)
                || "Running".equalsIgnoreCase(status)) {
                this.status = LongRunningOperationStatus.IN_PROGRESS;
            } else if ("Succeeded".equalsIgnoreCase(status)) {
                this.status = LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;
            } else if ("Failed".equalsIgnoreCase(status)) {
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
