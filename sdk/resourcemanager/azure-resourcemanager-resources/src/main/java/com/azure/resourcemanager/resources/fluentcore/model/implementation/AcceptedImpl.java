// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.model.implementation;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.management.exception.ManagementError;
import com.azure.core.management.exception.ManagementException;
import com.azure.core.management.polling.PollResult;
import com.azure.core.management.polling.PollerFactory;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.PollerFlux;
import com.azure.core.util.polling.SyncPoller;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.resources.fluentcore.AzureServiceClient;
import com.azure.resourcemanager.resources.fluentcore.model.Accepted;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;
import com.azure.resourcemanager.resources.fluentcore.rest.ActivationResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
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

    private PollerFlux<PollResult<InnerT>, InnerT> pollerFlux;
    private SyncPoller<Void, T> syncPoller;

    public AcceptedImpl(Response<Flux<ByteBuffer>> activationResponse,
                        SerializerAdapter serializerAdapter,
                        HttpPipeline httpPipeline,
                        Duration defaultPollInterval,
                        Type pollResultType,
                        Type finalResultType,
                        Function<InnerT, T> wrapOperation) {
        this.activationResponse = Objects.requireNonNull(activationResponse);
        this.serializerAdapter = Objects.requireNonNull(serializerAdapter);
        this.httpPipeline = Objects.requireNonNull(httpPipeline);
        this.defaultPollInterval = Objects.requireNonNull(defaultPollInterval);
        this.pollResultType = Objects.requireNonNull(pollResultType);
        this.finalResultType = Objects.requireNonNull(finalResultType);
        this.wrapOperation = Objects.requireNonNull(wrapOperation);
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
                LongRunningOperationStatus.IN_PROGRESS, retryAfter);
        } catch (IOException e) {
            throw logger.logExceptionAsError(
                new IllegalStateException("Failed to deserialize activation response body", e));
        }
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

    @Override
    public SyncPoller<Void, T> getSyncPoller() {
        if (syncPoller == null) {
            // refer to AzureServiceClient.getLroFinalResultOrError
            Function<PollResponse<PollResult<InnerT>>, ManagementException> errorOperation = response -> {
                String errorMessage;
                ManagementError managementError = null;
                if (response.getValue().getError() != null) {
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
                        } catch (IOException ioe) {
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
                return new ManagementException(errorMessage, null, managementError);
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
                Mono.just(clonedResponse)
            );
        }
        return pollerFlux;
    }

    @Override
    public T getFinalResult() {
        return this.getSyncPoller().getFinalResult();
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

    public static <T, InnerT> Accepted<T> newAccepted(
        ClientLogger logger,
        AzureServiceClient client,
        Supplier<Response<Flux<ByteBuffer>>> activationOperation,
        Function<InnerT, T> convertOperation,
        Type innerType,
        Runnable preActivation) {

        if (preActivation != null) {
            preActivation.run();
        }

        Response<Flux<ByteBuffer>> activationResponse = activationOperation.get();
        if (activationResponse == null) {
            throw logger.logExceptionAsError(new NullPointerException());
        } else {
            Accepted<T> accepted = new AcceptedImpl<InnerT, T>(
                activationResponse,
                client.getSerializerAdapter(),
                client.getHttpPipeline(),
                client.getDefaultPollInterval(),
                innerType, innerType,
                convertOperation);

            return accepted;
        }
    }

    public static <T extends HasInner<InnerT>, InnerT> Accepted<T> newAccepted(
        ClientLogger logger,
        AzureServiceClient client,
        Supplier<Response<Flux<ByteBuffer>>> activationOperation,
        Function<InnerT, T> convertOperation,
        Type innerType,
        Runnable preActivation, Consumer<InnerT> postActivation) {

        if (preActivation != null) {
            preActivation.run();
        }

        Response<Flux<ByteBuffer>> activationResponse = activationOperation.get();
        if (activationResponse == null) {
            throw logger.logExceptionAsError(new NullPointerException());
        } else {
            Accepted<T> accepted = new AcceptedImpl<InnerT, T>(
                activationResponse,
                client.getSerializerAdapter(),
                client.getHttpPipeline(),
                client.getDefaultPollInterval(),
                innerType, innerType,
                convertOperation);

            if (postActivation != null) {
                postActivation.accept(accepted.getActivationResponse().getValue().inner());
            }

            return accepted;
        }
    }
}
