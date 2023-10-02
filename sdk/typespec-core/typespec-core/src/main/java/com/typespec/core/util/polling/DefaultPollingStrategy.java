// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.util.polling;

import com.typespec.core.http.HttpPipeline;
import com.typespec.core.http.rest.Response;
import com.typespec.core.implementation.serializer.DefaultJsonSerializer;
import com.typespec.core.util.Context;
import com.typespec.core.util.serializer.JsonSerializer;
import com.typespec.core.util.serializer.TypeReference;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Objects;

/**
 * The default polling strategy to use with Azure data plane services. The default polling strategy will attempt 3
 * known strategies, {@link OperationResourcePollingStrategy}, {@link LocationPollingStrategy}, and
 * {@link StatusCheckPollingStrategy}, in this order. The first strategy that can poll on the initial response will be
 * used. The created chained polling strategy is capable of handling most of the polling scenarios in Azure.
 *
 * @param <T> the type of the response type from a polling call, or BinaryData if raw response body should be kept
 * @param <U> the type of the final result object to deserialize into, or BinaryData if raw response body should be
 *           kept
 */
public final class DefaultPollingStrategy<T, U> implements PollingStrategy<T, U> {
    private final ChainedPollingStrategy<T, U> chainedPollingStrategy;

    /**
     * Creates a chained polling strategy with 3 known polling strategies, {@link OperationResourcePollingStrategy},
     * {@link LocationPollingStrategy}, and {@link StatusCheckPollingStrategy}, in this order, with a JSON serializer.
     *
     * @param httpPipeline an instance of {@link HttpPipeline} to send requests with
     * @throws NullPointerException If {@code httpPipeline} is null.
     */
    public DefaultPollingStrategy(HttpPipeline httpPipeline) {
        this(httpPipeline, new DefaultJsonSerializer(), Context.NONE);
    }

    /**
     * Creates a chained polling strategy with 3 known polling strategies, {@link OperationResourcePollingStrategy},
     * {@link LocationPollingStrategy}, and {@link StatusCheckPollingStrategy}, in this order, with a custom
     * serializer.
     *
     * @param httpPipeline an instance of {@link HttpPipeline} to send requests with
     * @param serializer a custom serializer for serializing and deserializing polling responses
     * @throws NullPointerException If {@code httpPipeline} is null.
     */
    public DefaultPollingStrategy(HttpPipeline httpPipeline, JsonSerializer serializer) {
        this(httpPipeline, serializer, Context.NONE);
    }

    /**
     * Creates a chained polling strategy with 3 known polling strategies, {@link OperationResourcePollingStrategy},
     * {@link LocationPollingStrategy}, and {@link StatusCheckPollingStrategy}, in this order, with a custom
     * serializer.
     *
     * @param httpPipeline an instance of {@link HttpPipeline} to send requests with
     * @param serializer a custom serializer for serializing and deserializing polling responses
     * @param context an instance of {@link Context}
     * @throws NullPointerException If {@code httpPipeline} is null.
     */
    public DefaultPollingStrategy(HttpPipeline httpPipeline, JsonSerializer serializer, Context context) {
        this(httpPipeline, null, serializer, context);
    }

    /**
     * Creates a chained polling strategy with 3 known polling strategies, {@link OperationResourcePollingStrategy},
     * {@link LocationPollingStrategy}, and {@link StatusCheckPollingStrategy}, in this order, with a custom
     * serializer.
     *
     * @param httpPipeline an instance of {@link HttpPipeline} to send requests with.
     * @param endpoint an endpoint for creating an absolute path when the path itself is relative.
     * @param serializer a custom serializer for serializing and deserializing polling responses.
     * @param context an instance of {@link Context}.
     * @throws NullPointerException If {@code httpPipeline} is null.
     */
    public DefaultPollingStrategy(HttpPipeline httpPipeline, String endpoint, JsonSerializer serializer, Context context) {
        this(new PollingStrategyOptions(httpPipeline)
            .setEndpoint(endpoint)
            .setSerializer(serializer)
            .setContext(context));
    }

    /**
     * Creates a chained polling strategy with 3 known polling strategies, {@link OperationResourcePollingStrategy},
     * {@link LocationPollingStrategy}, and {@link StatusCheckPollingStrategy}, in this order, with a custom
     * serializer.
     *
     * @param pollingStrategyOptions options to configure this polling strategy.
     * @throws NullPointerException If {@code pollingStrategyOptions} is null.
     */
    public DefaultPollingStrategy(PollingStrategyOptions pollingStrategyOptions) {
        Objects.requireNonNull(pollingStrategyOptions, "'pollingStrategyOptions' cannot be null");
        this.chainedPollingStrategy = new ChainedPollingStrategy<>(Arrays.asList(
            new OperationResourcePollingStrategy<>(null, pollingStrategyOptions),
            new LocationPollingStrategy<>(pollingStrategyOptions),
            new StatusCheckPollingStrategy<>(pollingStrategyOptions.getSerializer())));
    }

    @Override
    public Mono<U> getResult(PollingContext<T> context, TypeReference<U> resultType) {
        return chainedPollingStrategy.getResult(context, resultType);
    }

    @Override
    public Mono<Boolean> canPoll(Response<?> initialResponse) {
        return chainedPollingStrategy.canPoll(initialResponse);
    }

    @Override
    public Mono<PollResponse<T>> onInitialResponse(Response<?> response, PollingContext<T> pollingContext,
                                                              TypeReference<T> pollResponseType) {
        return chainedPollingStrategy.onInitialResponse(response, pollingContext, pollResponseType);
    }

    @Override
    public Mono<PollResponse<T>> poll(PollingContext<T> context, TypeReference<T> pollResponseType) {
        return chainedPollingStrategy.poll(context, pollResponseType);
    }
}
