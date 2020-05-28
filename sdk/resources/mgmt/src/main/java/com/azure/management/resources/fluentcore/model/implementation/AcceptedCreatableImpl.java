// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.management.resources.fluentcore.model.implementation;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.management.polling.PollResult;
import com.azure.core.management.polling.PollerFactory;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.polling.AsyncPollResponse;
import com.azure.core.util.polling.PollerFlux;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.management.resources.fluentcore.model.AcceptedCreatable;
import com.azure.management.resources.fluentcore.model.HasInner;
import com.azure.management.resources.fluentcore.utils.SdkContext;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.function.Function;

public class AcceptedCreatableImpl<InnerT, T extends HasInner<InnerT>> implements AcceptedCreatable<InnerT, T> {

    private final Response<Flux<ByteBuffer>> activationResponse;
    private byte[] responseBytes;
    private final SerializerAdapter serializerAdapter;
    private final HttpPipeline httpPipeline;
    private final Type pollResultType;
    private final Type finalResultType;
    private final Function<InnerT, T> wrapOperation;

    private PollerFlux<PollResult<InnerT>, InnerT> pollerFlux;

    public AcceptedCreatableImpl(Response<Flux<ByteBuffer>> activationResponse,
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
    public PollerFlux<PollResult<InnerT>, InnerT> beginPolling() {
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
        return beginPolling().last().flatMap(AsyncPollResponse::getFinalResult).map(wrapOperation).block();
    }

    private byte[] getResponse() {
        if (responseBytes == null) {
            responseBytes = FluxUtil.collectBytesInByteBufferStream(activationResponse.getValue()).block();
        }
        return responseBytes;
    }
}
