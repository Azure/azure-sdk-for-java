// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.model.implementation;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.management.polling.PollResult;
import com.azure.core.management.polling.PollerFactory;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.PollerFlux;
import com.azure.core.util.polling.SyncPoller;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.resources.fluentcore.model.Accepted;
import com.azure.resourcemanager.resources.fluentcore.utils.SdkContext;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;
import java.util.function.Function;

public class AcceptedImpl<InnerT, T> implements Accepted<T> {

    private final Response<Flux<ByteBuffer>> activationResponse;
    private byte[] responseBytes;
    private final SerializerAdapter serializerAdapter;
    private final HttpPipeline httpPipeline;
    private final Type pollResultType;
    private final Type finalResultType;
    private final Function<InnerT, T> wrapOperation;

    private PollerFlux<PollResult<InnerT>, InnerT> pollerFlux;
    private SyncPoller<Void, T> syncPoller;

    public AcceptedImpl(Response<Flux<ByteBuffer>> activationResponse,
                        SerializerAdapter serializerAdapter,
                        HttpPipeline httpPipeline,
                        Type pollResultType,
                        Type finalResultType,
                        Function<InnerT, T> wrapOperation) {
        this.activationResponse = Objects.requireNonNull(activationResponse);
        this.serializerAdapter = Objects.requireNonNull(serializerAdapter);
        this.httpPipeline = Objects.requireNonNull(httpPipeline);
        this.pollResultType = Objects.requireNonNull(pollResultType);
        this.finalResultType = Objects.requireNonNull(finalResultType);
        this.wrapOperation = Objects.requireNonNull(wrapOperation);
    }

    @Override
    public T getAcceptedResult() {
        try {
            return wrapOperation.apply(serializerAdapter.deserialize(
                new String(getResponse(), StandardCharsets.UTF_8),
                finalResultType,
                SerializerEncoding.JSON));
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public SyncPoller<Void, T> getSyncPoller() {
        if (syncPoller == null) {
            syncPoller = new SyncPollerImpl<InnerT, T>(this.getPollerFlux().getSyncPoller(), wrapOperation);
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
                SdkContext.getLroRetryDuration(),
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
        private T finalResult;

        SyncPollerImpl(SyncPoller<PollResult<InnerT>, InnerT> syncPoller, Function<InnerT, T> wrapOperation) {
            this.syncPoller = syncPoller;
            this.wrapOperation = wrapOperation;
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
            if (finalResult == null) {
                finalResult = wrapOperation.apply(syncPoller.getFinalResult());
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
}
