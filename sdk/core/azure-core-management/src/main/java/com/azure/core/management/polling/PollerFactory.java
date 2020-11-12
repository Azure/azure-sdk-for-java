// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management.polling;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.management.implementation.polling.PollOperation;
import com.azure.core.management.implementation.polling.PollingState;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.PollerFlux;
import com.azure.core.util.polling.PollingContext;
import com.azure.core.util.serializer.SerializerAdapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;
import java.util.function.Function;

/**
 * Factory to create PollerFlux for Azure resource manager (ARM) long-running-operation (LRO).
 */
public final class PollerFactory {

    private PollerFactory() {
    }

    /**
     * Creates a PollerFlux with default ARM LRO init operation.
     *
     * @param serializerAdapter the serializer for any encoding and decoding
     * @param pipeline the HttpPipeline for making any Http request (e.g. poll)
     * @param pollResultType the type of the poll result, if no result is expecting then this should be Void.class
     * @param finalResultType the type of the final result, if no result is expecting then this should be Void.class
     * @param defaultPollInterval the default poll interval to use if service does not return retry-after
     * @param lroInitMono the Mono on subscribe send the service request to initiate the long-running-operation
     * @param <T> the type of poll result
     * @param <U> the type of final result
     * @return PollerFlux
     */
    public static <T, U> PollerFlux<PollResult<T>, U> create(
        SerializerAdapter serializerAdapter,
        HttpPipeline pipeline,
        Type pollResultType,
        Type finalResultType,
        Duration defaultPollInterval,
        Mono<Response<Flux<ByteBuffer>>> lroInitMono) {
        return create(serializerAdapter, pipeline, pollResultType, finalResultType, defaultPollInterval, lroInitMono,
            Context.NONE);
    }

    /**
     * Creates a PollerFlux with default ARM LRO init operation.
     *
     * @param serializerAdapter the serializer for any encoding and decoding
     * @param pipeline the HttpPipeline for making any Http request (e.g. poll)
     * @param pollResultType the type of the poll result, if no result is expecting then this should be Void.class
     * @param finalResultType the type of the final result, if no result is expecting then this should be Void.class
     * @param defaultPollInterval the default poll interval to use if service does not return retry-after
     * @param lroInitMono the Mono on subscribe send the service request to initiate the long-running-operation
     * @param context the context shared by all requests
     * @param <T> the type of poll result
     * @param <U> the type of final result
     * @return PollerFlux
     */
    public static <T, U> PollerFlux<PollResult<T>, U> create(
        SerializerAdapter serializerAdapter,
        HttpPipeline pipeline,
        Type pollResultType,
        Type finalResultType,
        Duration defaultPollInterval,
        Mono<Response<Flux<ByteBuffer>>> lroInitMono,
        Context context) {
        Objects.requireNonNull(serializerAdapter, "'serializerAdapter' cannot be null.");
        Objects.requireNonNull(pipeline, "'pipeline' cannot be null.");
        Objects.requireNonNull(pollResultType, "'pollResultType' cannot be null.");
        Objects.requireNonNull(finalResultType, "'finalResultType' cannot be null.");
        Objects.requireNonNull(defaultPollInterval, "'defaultPollInterval' cannot be null.");
        Objects.requireNonNull(lroInitMono, "'lroInitMono' cannot be null.");
        Objects.requireNonNull(context, "'context' cannot be null.");
        Function<PollingContext<PollResult<T>>, Mono<PollResponse<PollResult<T>>>> defaultLroInitOperation =
            pollingContext -> lroInitMono.flatMap(
                response -> FluxUtil.collectBytesInByteBufferStream(response.getValue())
                    .map(contentBytes -> {
                        String content = new String(contentBytes, StandardCharsets.UTF_8);
                        PollingState state = PollingState.create(serializerAdapter,
                            response.getRequest(),
                            response.getStatusCode(),
                            response.getHeaders(),
                            content);
                        state.store(pollingContext);
                        T result = PollOperation.deserialize(serializerAdapter, content, pollResultType);
                        return new PollResponse<>(state.getOperationStatus(),
                            new PollResult<>(result),
                            state.getPollDelay());
                    }));
        return PollerFlux.create(defaultPollInterval,
            defaultLroInitOperation,
            PollOperation.pollFunction(serializerAdapter, pipeline, pollResultType, context),
            PollOperation.cancelFunction(context),
            PollOperation.fetchResultFunction(serializerAdapter, pipeline, finalResultType, context));
    }

    /**
     * Creates a PollerFlux.
     *
     * @param serializerAdapter the serializer for any encoding and decoding
     * @param pipeline the HttpPipeline for making any Http request (e.g. poll)
     * @param pollResultType the type of the poll result, if no result is expecting then this should be Void.class
     * @param finalResultType the type of the final result, if no result is expecting then this should be Void.class
     * @param defaultPollInterval the default poll interval to use if service does not return retry-after
     * @param lroInitOperation the function upon invoking should initiate the long-running-operation
     * @param <T> the type of poll result
     * @param <U> the type of final result
     * @return PollerFlux
     */
    public static <T, U> PollerFlux<PollResult<T>, U> create(
        SerializerAdapter serializerAdapter,
        HttpPipeline pipeline,
        Type pollResultType,
        Type finalResultType,
        Duration defaultPollInterval,
        Function<PollingContext<PollResult<T>>, Mono<PollResult<T>>> lroInitOperation) {
        return create(serializerAdapter, pipeline, pollResultType, finalResultType, defaultPollInterval,
            lroInitOperation, Context.NONE);
    }

    /**
     * Creates a PollerFlux.
     *
     * @param serializerAdapter the serializer for any encoding and decoding
     * @param pipeline the HttpPipeline for making any Http request (e.g. poll)
     * @param pollResultType the type of the poll result, if no result is expecting then this should be Void.class
     * @param finalResultType the type of the final result, if no result is expecting then this should be Void.class
     * @param defaultPollInterval the default poll interval to use if service does not return retry-after
     * @param lroInitOperation the function upon invoking should initiate the long-running-operation
     * @param context the context shared by all requests
     * @param <T> the type of poll result
     * @param <U> the type of final result
     * @return PollerFlux
     */
    public static <T, U> PollerFlux<PollResult<T>, U> create(
        SerializerAdapter serializerAdapter,
        HttpPipeline pipeline,
        Type pollResultType,
        Type finalResultType,
        Duration defaultPollInterval,
        Function<PollingContext<PollResult<T>>, Mono<PollResult<T>>> lroInitOperation,
        Context context) {
        Objects.requireNonNull(serializerAdapter, "'serializerAdapter' cannot be null.");
        Objects.requireNonNull(pipeline, "'pipeline' cannot be null.");
        Objects.requireNonNull(pollResultType, "'pollResultType' cannot be null.");
        Objects.requireNonNull(finalResultType, "'finalResultType' cannot be null.");
        Objects.requireNonNull(defaultPollInterval, "'defaultPollInterval' cannot be null.");
        Objects.requireNonNull(lroInitOperation, "'lroInitOperation' cannot be null.");
        Objects.requireNonNull(context, "'context' cannot be null.");

        return new PollerFlux<>(defaultPollInterval,
            lroInitOperation,
            PollOperation.pollFunction(serializerAdapter, pipeline, pollResultType, context),
            PollOperation.cancelFunction(context),
            PollOperation.fetchResultFunction(serializerAdapter, pipeline, finalResultType, context));
    }

    /**
     * Dehydrate a PollerFlux from a string.
     *
     * @param serializerAdapter the serializer for any encoding and decoding
     * @param pipeline the HttpPipeline for making any Http request (e.g. poll)
     * @param pollResultType the type of the poll result, if no result is expecting then this should be Void.class
     * @param finalResultType the type of the final result, if no result is expecting then this should be Void.class
     * @param defaultPollInterval the default poll interval to use if service does not return retry-after
     * @param pollingStateStr the string to dehydrate PollerFlux from
     * @param <T> the type of poll result
     * @param <U> the type of final result
     * @return PollerFlux
     */
    public static <T, U> PollerFlux<PollResult<T>, U> create(SerializerAdapter serializerAdapter,
        HttpPipeline pipeline,
        Type pollResultType,
        Type finalResultType,
        Duration defaultPollInterval,
        String pollingStateStr) {
        return create(serializerAdapter, pipeline, pollResultType, finalResultType, defaultPollInterval,
            pollingStateStr, Context.NONE);
    }

    /**
     * Dehydrate a PollerFlux from a string.
     *
     * @param serializerAdapter the serializer for any encoding and decoding
     * @param pipeline the HttpPipeline for making any Http request (e.g. poll)
     * @param pollResultType the type of the poll result, if no result is expecting then this should be Void.class
     * @param finalResultType the type of the final result, if no result is expecting then this should be Void.class
     * @param defaultPollInterval the default poll interval to use if service does not return retry-after
     * @param pollingStateStr the string to dehydrate PollerFlux from
     * @param context the context shared by all requests
     * @param <T> the type of poll result
     * @param <U> the type of final result
     * @return PollerFlux
     */
    public static <T, U> PollerFlux<PollResult<T>, U> create(SerializerAdapter serializerAdapter,
        HttpPipeline pipeline,
        Type pollResultType,
        Type finalResultType,
        Duration defaultPollInterval,
        String pollingStateStr,
        Context context) {
        Objects.requireNonNull(serializerAdapter, "'serializerAdapter' cannot be null.");
        Objects.requireNonNull(pipeline, "'pipeline' cannot be null.");
        Objects.requireNonNull(pollResultType, "'pollResultType' cannot be null.");
        Objects.requireNonNull(finalResultType, "'finalResultType' cannot be null.");
        Objects.requireNonNull(defaultPollInterval, "'defaultPollInterval' cannot be null.");
        Objects.requireNonNull(pollingStateStr, "'pollingStateStr' cannot be null.");
        Objects.requireNonNull(context, "'context' cannot be null.");
        return create(serializerAdapter, pipeline, pollResultType, finalResultType, defaultPollInterval,
            pollingContext -> {
                PollingState.from(serializerAdapter, pollingStateStr).store(pollingContext);
                return Mono.empty();
            }, context);
    }
}
