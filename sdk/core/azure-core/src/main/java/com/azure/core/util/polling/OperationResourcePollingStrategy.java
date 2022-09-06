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
import com.azure.core.implementation.http.HttpHeadersHelper;
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
import com.fasterxml.jackson.annotation.JsonSetter;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.Objects;

import static com.azure.core.util.polling.implementation.PollingUtils.getAbsolutePath;

/**
 * Implements an operation resource polling strategy, typically from Operation-Location.
 *
 * @param <T> the type of the response type from a polling call, or BinaryData if raw response body should be kept
 * @param <U> the type of the final result object to deserialize into, or BinaryData if raw response body should be
 *           kept
 */
public class OperationResourcePollingStrategy<T, U> implements PollingStrategy<T, U> {
    private static final ClientLogger LOGGER = new ClientLogger(OperationResourcePollingStrategy.class);
    private static final String DEFAULT_OPERATION_LOCATION_HEADER = "Operation-Location";
    private static final String DEFAULT_OPERATION_LOCATION_HEADER_LOWER_CASE = "operation-location";

    private final HttpPipeline httpPipeline;
    private final ObjectSerializer serializer;
    private final String endpoint;
    private final String operationLocationHeaderName;
    private final String operationLocationHeaderNameLowerCase;
    private final Context context;

    /**
     * Creates an instance of the operation resource polling strategy using a JSON serializer and "Operation-Location"
     * as the header for polling.
     *
     * @param httpPipeline an instance of {@link HttpPipeline} to send requests with
     */
    public OperationResourcePollingStrategy(HttpPipeline httpPipeline) {
        this(httpPipeline, new DefaultJsonSerializer(), DEFAULT_OPERATION_LOCATION_HEADER, Context.NONE);
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
     * @param context an instance of {@link com.azure.core.util.Context}
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
     * @param context an instance of {@link com.azure.core.util.Context}.
     */
    public OperationResourcePollingStrategy(HttpPipeline httpPipeline, String endpoint, ObjectSerializer serializer,
        String operationLocationHeaderName, Context context) {
        this.httpPipeline = Objects.requireNonNull(httpPipeline, "'httpPipeline' cannot be null");
        this.endpoint = endpoint;
        this.serializer = serializer != null ? serializer : new DefaultJsonSerializer();
        if (operationLocationHeaderName != null) {
            this.operationLocationHeaderName = operationLocationHeaderName;
            this.operationLocationHeaderNameLowerCase = operationLocationHeaderName.toLowerCase(Locale.ROOT);
        } else {
            this.operationLocationHeaderName = DEFAULT_OPERATION_LOCATION_HEADER;
            this.operationLocationHeaderNameLowerCase = DEFAULT_OPERATION_LOCATION_HEADER_LOWER_CASE;
        }
        this.context = context == null ? Context.NONE : context;
    }

    @Override
    public Mono<Boolean> canPoll(Response<?> initialResponse) {
        HttpHeader operationLocationHeader = HttpHeadersHelper.getNoKeyFormatting(initialResponse.getHeaders(),
            operationLocationHeaderNameLowerCase);
        if (operationLocationHeader != null) {
            try {
                new URL(getAbsolutePath(operationLocationHeader.getValue(), endpoint, LOGGER));
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
        HttpHeader operationLocationHeader = HttpHeadersHelper.getNoKeyFormatting(response.getHeaders(),
            operationLocationHeaderNameLowerCase);
        HttpHeader locationHeader = HttpHeadersHelper.getNoKeyFormatting(response.getHeaders(),
            PollingConstants.LOCATION_LOWER_CASE);
        if (operationLocationHeader != null) {
            pollingContext.setData(operationLocationHeaderName,
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
        HttpRequest request = new HttpRequest(HttpMethod.GET, pollingContext.getData(operationLocationHeaderName));
        return FluxUtil.withContext(context1 -> httpPipeline.send(request,
                CoreUtils.mergeContexts(context1, this.context))).flatMap(response -> response.getBodyAsByteArray()
            .map(BinaryData::fromBytes)
            .flatMap(binaryData -> PollingUtils.deserializeResponse(
                    binaryData, serializer, new TypeReference<PollResult>() { })
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
