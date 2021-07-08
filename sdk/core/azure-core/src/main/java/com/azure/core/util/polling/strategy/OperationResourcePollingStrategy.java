// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.polling.strategy;

import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.implementation.TypeUtil;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollingContext;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import reactor.core.publisher.Mono;

import java.lang.reflect.Type;

/**
 * Implements a operation resource polling strategy, typically from Operation-Location.
 */
public class OperationResourcePollingStrategy implements PollingStrategy {
    private static final String OPERATION_LOCATION = "Operation-Location";
    private static final String LOCATION = "Location";
    private static final String REQUEST_URL = "requestURL";
    private static final String HTTP_METHOD = "httpMethod";
    private static final String RESOURCE_LOCATION = "resourceLocation";

    private final SerializerAdapter serializer = new JacksonAdapter();

    /**
     * Gets the name of the operation location header. By default it's "Operation-Location".
     * @return the name of the operation location header
     */
    public String getOperationLocationHeaderName() {
        return OPERATION_LOCATION;
    }

    @Override
    public boolean canPoll(Response<?> activationResponse) {
        HttpHeader operationLocationHeader = activationResponse.getHeaders().get(getOperationLocationHeaderName());
        return operationLocationHeader != null;
    }

    @Override
    public String getPollingUrl(PollingContext<BinaryData> context) {
        return context.getData(OPERATION_LOCATION);
    }

    @Override
    public String getFinalGetUrl(PollingContext<BinaryData> context) {
        String finalGetUrl = context.getData(RESOURCE_LOCATION);
        if (finalGetUrl == null) {
            String httpMethod = context.getData(HTTP_METHOD);
            if ("PUT".equalsIgnoreCase(httpMethod) || "PATCH".equalsIgnoreCase(httpMethod)) {
                finalGetUrl = context.getData(REQUEST_URL);
            } else if ("POST".equalsIgnoreCase(httpMethod) && context.getData(LOCATION) != null) {
                finalGetUrl = context.getData(LOCATION);
            } else {
                throw new RuntimeException("Cannot get final result");
            }
        }

        return finalGetUrl;
    }

    @Override
    public Mono<LongRunningOperationStatus> onActivationResponse(Response<?> response, PollingContext<BinaryData> context) {
        HttpHeader operationLocationHeader = response.getHeaders().get(getOperationLocationHeaderName());
        HttpHeader locationHeader = response.getHeaders().get(LOCATION);
        if (operationLocationHeader != null) {
            context.setData(OPERATION_LOCATION, operationLocationHeader.getValue());
        }
        if (locationHeader != null) {
            context.setData(LOCATION, locationHeader.getValue());
        }
        context.setData(HTTP_METHOD, response.getRequest().getHttpMethod().name());
        context.setData(REQUEST_URL, response.getRequest().getUrl().toString());

        if (response.getStatusCode() == 200
                || response.getStatusCode() == 201
                || response.getStatusCode() == 202
                || response.getStatusCode() == 204) {
            return Mono.just(LongRunningOperationStatus.IN_PROGRESS);
        } else {
            return Mono.error(new RuntimeException("Operation failed or cancelled: " + response.getStatusCode()));
        }
    }

    @Override
    public Mono<LongRunningOperationStatus> onPollingResponse(HttpResponse response, PollingContext<BinaryData> context) {
        return response.getBodyAsString().flatMap(body -> Mono.fromCallable(() ->
                serializer.<PollResult>deserialize(body, PollResult.class, SerializerEncoding.JSON)))
            .map(pollResult -> {
                if (pollResult.getResourceLocation() != null) {
                    context.setData(RESOURCE_LOCATION, pollResult.getResourceLocation());
                }
                return pollResult.getStatus();
            });
    }

    @Override
    @SuppressWarnings("unchecked")
    public <U> Mono<U> getFinalResult(HttpResponse response, PollingContext<BinaryData> context, Type resultType) {
        if (TypeUtil.isTypeOrSubTypeOf(BinaryData.class, resultType)) {
            return (Mono<U>) BinaryData.fromFlux(response.getBody());
        } else {
            return response.getBodyAsString().flatMap(body -> Mono.fromCallable(() ->
                    serializer.deserialize(body, resultType, SerializerEncoding.JSON)));
        }
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
