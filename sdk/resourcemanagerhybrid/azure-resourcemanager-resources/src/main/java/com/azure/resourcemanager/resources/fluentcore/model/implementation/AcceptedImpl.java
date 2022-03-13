// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.model.implementation;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.management.exception.ManagementError;
import com.azure.core.management.exception.ManagementException;
import com.azure.core.management.polling.PollResult;
import com.azure.core.management.polling.PollerFactory;
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.PollerFlux;
import com.azure.core.util.polling.SyncPoller;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.resources.fluentcore.model.Accepted;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import com.azure.resourcemanager.resources.fluentcore.rest.ActivationResponse;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.fasterxml.jackson.annotation.JsonProperty;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class AcceptedImpl<InnerT, T> implements Accepted<T> {

    private final ClientLogger logger = new ClientLogger(getClass());

    private final Response<Flux<ByteBuffer>> activationResponse;
    private byte[] responseBytes;
    private final SerializerAdapter serializerAdapter;
    private final HttpPipeline httpPipeline;
    private final Duration defaultPollInterval;
    private final Type pollResultType;
    private final Type finalResultType;
    private final Function<InnerT, T> wrapOperation;
    private final Context context;

    private PollerFlux<PollResult<InnerT>, InnerT> pollerFlux;
    private SyncPoller<Void, T> syncPoller;

    public AcceptedImpl(Response<Flux<ByteBuffer>> activationResponse,
                        SerializerAdapter serializerAdapter,
                        HttpPipeline httpPipeline,
                        Duration defaultPollInterval,
                        Type pollResultType,
                        Type finalResultType,
                        Function<InnerT, T> wrapOperation,
                        Context context) {
        this.activationResponse = Objects.requireNonNull(activationResponse);
        this.serializerAdapter = Objects.requireNonNull(serializerAdapter);
        this.httpPipeline = Objects.requireNonNull(httpPipeline);
        this.defaultPollInterval = Objects.requireNonNull(defaultPollInterval);
        this.pollResultType = Objects.requireNonNull(pollResultType);
        this.finalResultType = Objects.requireNonNull(finalResultType);
        this.wrapOperation = Objects.requireNonNull(wrapOperation);
        this.context = context;
    }

    @Override
    public ActivationResponse<T> getActivationResponse() {
        try {
            T value = wrapOperation.apply(serializerAdapter.deserialize(
                new String(getResponse(), StandardCharsets.UTF_8),
                finalResultType,
                SerializerEncoding.JSON));
            Duration retryAfter = getRetryAfter(activationResponse.getHeaders());
            return new ActivationResponse<>(activationResponse.getRequest(), activationResponse.getStatusCode(),
                activationResponse.getHeaders(), value,
                getActivationResponseStatus(), retryAfter);
        } catch (IOException e) {
            throw logger.logExceptionAsError(
                new IllegalStateException("Failed to deserialize activation response body", e));
        }
    }

    @Override
    public SyncPoller<Void, T> getSyncPoller() {
        if (syncPoller == null) {
            // refer to AzureServiceClient.getLroFinalResultOrError
            Function<PollResponse<PollResult<InnerT>>, ManagementException> errorOperation = response -> {
                String errorMessage;
                ManagementError managementError = null;
                HttpResponse errorResponse = null;
                PollResult.Error lroError = response.getValue().getError();
                if (response.getValue().getError() != null) {
                    errorResponse = new HttpResponseImpl(lroError.getResponseStatusCode(),
                        lroError.getResponseHeaders(), lroError.getResponseBody());

                    errorMessage = response.getValue().getError().getMessage();
                    String errorBody = response.getValue().getError().getResponseBody();
                    if (errorBody != null) {
                        // try to deserialize error body to ManagementError
                        try {
                            managementError = serializerAdapter.deserialize(
                                errorBody,
                                ManagementError.class,
                                SerializerEncoding.JSON);
                            if (managementError.getCode() == null || managementError.getMessage() == null) {
                                managementError = null;
                            }
                        } catch (IOException | RuntimeException ioe) {
                            logger.logThrowableAsWarning(ioe);
                        }
                    }
                } else {
                    // fallback to default error message
                    errorMessage = "Long running operation failed.";
                }
                if (managementError == null) {
                    // fallback to default ManagementError
                    managementError = new ManagementError(response.getStatus().toString(), errorMessage);
                }
                return new ManagementException(errorMessage, errorResponse, managementError);
            };

            syncPoller = new SyncPollerImpl<InnerT, T>(this.getPollerFlux().getSyncPoller(),
                wrapOperation, errorOperation);
        }
        return syncPoller;
    }

    private PollerFlux<PollResult<InnerT>, InnerT> getPollerFlux() {
        if (pollerFlux == null) {
            Flux<ByteBuffer> content = Flux.just(ByteBuffer.wrap(getResponse()));
            Response<Flux<ByteBuffer>> clonedResponse = new SimpleResponse<>(activationResponse, content);

            pollerFlux = PollerFactory.create(
                serializerAdapter,
                httpPipeline,
                pollResultType,
                finalResultType,
                defaultPollInterval,
                Mono.just(clonedResponse),
                context
            );
        }
        return pollerFlux;
    }

    @Override
    public T getFinalResult() {
        return this.getSyncPoller().getFinalResult();
    }

    private LongRunningOperationStatus getActivationResponseStatus() {
        String responseBody = new String(getResponse(), StandardCharsets.UTF_8);
        String provisioningState = null;
        // try get "provisioningState" property.
        if (!CoreUtils.isNullOrEmpty(responseBody)) {
            try {
                ResourceWithProvisioningState resource = serializerAdapter.deserialize(responseBody,
                    ResourceWithProvisioningState.class, SerializerEncoding.JSON);
                provisioningState = resource != null
                    ? resource.getProvisioningState()
                    : null;
            } catch (IOException ignored) {

            }
        }

        // get LRO status, default is IN_PROGRESS
        LongRunningOperationStatus status = LongRunningOperationStatus.IN_PROGRESS;
        if (!CoreUtils.isNullOrEmpty(provisioningState)) {
            // LRO status based on provisioningState.
            status = toLongRunningOperationStatus(provisioningState);
        } else {
            // LRO status based on status code.
            int statusCode = activationResponse.getStatusCode();
            if (statusCode == 200 || statusCode == 201 || statusCode == 204) {
                status = LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;
            }
        }
        return status;
    }

    private static LongRunningOperationStatus toLongRunningOperationStatus(String value) {
        if (ProvisioningState.SUCCEEDED.equalsIgnoreCase(value)) {
            return LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;
        } else if (ProvisioningState.FAILED.equalsIgnoreCase(value)) {
            return LongRunningOperationStatus.FAILED;
        } else if (ProvisioningState.CANCELED.equalsIgnoreCase(value)) {
            return LongRunningOperationStatus.USER_CANCELLED;
        } else if (ProvisioningState.IN_PROGRESS.equalsIgnoreCase(value)) {
            return LongRunningOperationStatus.IN_PROGRESS;
        }
        return LongRunningOperationStatus.fromString(value, false);
    }

    private static Duration getRetryAfter(HttpHeaders headers) {
        if (headers != null) {
            final String value = headers.getValue("Retry-After");
            if (value != null) {
                return Duration.ofSeconds(Long.parseLong(value));
            }
        }
        return null;
    }

    private byte[] getResponse() {
        if (responseBytes == null) {
            responseBytes = FluxUtil.collectBytesInByteBufferStream(activationResponse.getValue()).block();
        }
        return responseBytes;
    }

    private static class SyncPollerImpl<InnerT, T>
        implements SyncPoller<Void, T> {

        private final SyncPoller<PollResult<InnerT>, InnerT> syncPoller;
        private final Function<InnerT, T> wrapOperation;
        private final Function<PollResponse<PollResult<InnerT>>, ManagementException> errorOperation;
        private T finalResult;
        private ManagementException exception;

        SyncPollerImpl(SyncPoller<PollResult<InnerT>, InnerT> syncPoller, Function<InnerT, T> wrapOperation,
                       Function<PollResponse<PollResult<InnerT>>, ManagementException> errorOperation) {
            this.syncPoller = syncPoller;
            this.wrapOperation = wrapOperation;
            this.errorOperation = errorOperation;
        }

        @Override
        public PollResponse<Void> poll() {
            return voidResponse(syncPoller.poll());
        }

        @Override
        public PollResponse<Void> waitForCompletion() {
            return voidResponse(syncPoller.waitForCompletion());
        }

        @Override
        public PollResponse<Void> waitForCompletion(Duration duration) {
            return voidResponse(syncPoller.waitForCompletion(duration));
        }

        @Override
        public PollResponse<Void> waitUntil(LongRunningOperationStatus longRunningOperationStatus) {
            return voidResponse(syncPoller.waitUntil(longRunningOperationStatus));
        }

        @Override
        public PollResponse<Void> waitUntil(Duration duration, LongRunningOperationStatus longRunningOperationStatus) {
            return voidResponse(syncPoller.waitUntil(duration, longRunningOperationStatus));
        }

        @Override
        public T getFinalResult() {
            if (exception != null) {
                throw exception;
            }
            if (finalResult == null) {
                final InnerT innerFinalResult = syncPoller.getFinalResult();
                if (innerFinalResult == null) {
                    // possible failure
                    PollResponse<PollResult<InnerT>> response = syncPoller.poll();
                    if (response.getStatus() == LongRunningOperationStatus.FAILED
                        || response.getStatus() == LongRunningOperationStatus.USER_CANCELLED) {
                        exception = errorOperation.apply(response);
                        throw exception;
                    }
                }
                finalResult = wrapOperation.apply(innerFinalResult);
            }
            return finalResult;
        }

        @Override
        public void cancelOperation() {
            syncPoller.cancelOperation();
        }

        private PollResponse<Void> voidResponse(PollResponse<PollResult<InnerT>> pollResponse) {
            return new PollResponse<>(pollResponse.getStatus(), null, pollResponse.getRetryAfter());
        }
    }

    private static class ResourceWithProvisioningState {
        @JsonProperty(value = "properties")
        private Properties properties;

        private String getProvisioningState() {
            if (this.properties != null) {
                return this.properties.provisioningState;
            } else {
                return null;
            }
        }

        private static class Properties {
            @JsonProperty(value = "provisioningState")
            private String provisioningState;
        }
    }

    private static class ProvisioningState {
        static final String IN_PROGRESS = "InProgress";
        static final String SUCCEEDED = "Succeeded";
        static final String FAILED = "Failed";
        static final String CANCELED = "Canceled";
    }

    private static class HttpResponseImpl extends HttpResponse {
        private final int statusCode;
        private final byte[] responseBody;
        private final HttpHeaders httpHeaders;

        HttpResponseImpl(int statusCode, HttpHeaders httpHeaders, String responseBody) {
            super(null);
            this.statusCode = statusCode;
            this.httpHeaders = httpHeaders;
            this.responseBody = responseBody == null ? null : responseBody.getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public int getStatusCode() {
            return statusCode;
        }

        @Override
        public String getHeaderValue(String s) {
            return httpHeaders.getValue(s);
        }

        @Override
        public HttpHeaders getHeaders() {
            return httpHeaders;
        }

        @Override
        public Flux<ByteBuffer> getBody() {
            return Flux.just(ByteBuffer.wrap(responseBody));
        }

        @Override
        public Mono<byte[]> getBodyAsByteArray() {
            return Mono.just(responseBody);
        }

        @Override
        public Mono<String> getBodyAsString() {
            return Mono.just(new String(responseBody, StandardCharsets.UTF_8));
        }

        @Override
        public Mono<String> getBodyAsString(Charset charset) {
            return Mono.just(new String(responseBody, charset));
        }
    }

    public static <T, InnerT> Accepted<T> newAccepted(
        ClientLogger logger,
        HttpPipeline httpPipeline,
        Duration pollInterval,
        Supplier<Response<Flux<ByteBuffer>>> activationOperation,
        Function<InnerT, T> convertOperation,
        Type innerType,
        Runnable preActivation,
        Context context) {

        if (preActivation != null) {
            preActivation.run();
        }

        Response<Flux<ByteBuffer>> activationResponse = activationOperation.get();
        if (activationResponse == null) {
            throw logger.logExceptionAsError(new NullPointerException());
        } else {
            Accepted<T> accepted = new AcceptedImpl<InnerT, T>(
                activationResponse,
                SerializerFactory.createDefaultManagementSerializerAdapter(),
                httpPipeline,
                ResourceManagerUtils.InternalRuntimeContext.getDelayDuration(pollInterval),
                innerType, innerType,
                convertOperation,
                context);

            return accepted;
        }
    }

    public static <T extends HasInnerModel<InnerT>, InnerT> Accepted<T> newAccepted(
        ClientLogger logger,
        HttpPipeline httpPipeline,
        Duration pollInterval,
        Supplier<Response<Flux<ByteBuffer>>> activationOperation,
        Function<InnerT, T> convertOperation,
        Type innerType,
        Runnable preActivation, Consumer<InnerT> postActivation,
        Context context) {

        if (preActivation != null) {
            preActivation.run();
        }

        Response<Flux<ByteBuffer>> activationResponse = activationOperation.get();
        if (activationResponse == null) {
            throw logger.logExceptionAsError(new NullPointerException());
        } else {
            Accepted<T> accepted = new AcceptedImpl<InnerT, T>(
                activationResponse,
                SerializerFactory.createDefaultManagementSerializerAdapter(),
                httpPipeline,
                ResourceManagerUtils.InternalRuntimeContext.getDelayDuration(pollInterval),
                innerType, innerType,
                convertOperation,
                context);

            if (postActivation != null) {
                postActivation.accept(accepted.getActivationResponse().getValue().innerModel());
            }

            return accepted;
        }
    }
}
