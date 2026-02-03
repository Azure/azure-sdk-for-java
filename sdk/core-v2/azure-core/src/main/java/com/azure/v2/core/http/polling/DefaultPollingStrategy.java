// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.core.http.polling;

import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.models.RequestContext;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.serialization.json.JsonSerializer;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;

/**
 * The default polling strategy to use with Azure data plane services. The default polling strategy will
 * attempt three known strategies, {@link OperationResourcePollingStrategy}, {@link LocationPollingStrategy},
 * and {@link StatusCheckPollingStrategy}, in this order. The first strategy that can poll on the initial response
 * will be used. The created chained polling strategy is capable of handling most of the polling scenarios in Azure.
 *
 * @param <T> the type of the response type from a polling call, or BinaryData if raw response body should be kept
 * @param <U> the type of the final result object to deserialize into, or BinaryData if raw response body should be
 * kept
 */
public final class DefaultPollingStrategy<T, U> implements PollingStrategy<T, U> {
    private final ChainedPollingStrategy<T, U> chainedPollingStrategy;

    /**
     * Creates a chained polling strategy with three known polling strategies,
     * {@link OperationResourcePollingStrategy}, {@link LocationPollingStrategy}, and
     * {@link StatusCheckPollingStrategy}, in this order, with a JSON serializer.
     *
     * @param httpPipeline an instance of {@link HttpPipeline} to send requests with
     * @throws NullPointerException If {@code httpPipeline} is null.
     */
    public DefaultPollingStrategy(HttpPipeline httpPipeline) {
        this(httpPipeline, new JsonSerializer(), RequestContext.none());
    }

    /**
     * Creates a chained polling strategy with three known polling strategies,
     * {@link OperationResourcePollingStrategy}, {@link LocationPollingStrategy}, and
     * {@link StatusCheckPollingStrategy}, in this order, with a JSON serializer.
     *
     * @param httpPipeline an instance of {@link HttpPipeline} to send requests with
     * @param serializer a custom serializer for serializing and deserializing polling responses
     * @throws NullPointerException If {@code httpPipeline} is null.
     */
    public DefaultPollingStrategy(HttpPipeline httpPipeline, JsonSerializer serializer) {
        this(httpPipeline, serializer, RequestContext.none());
    }

    /**
     * Creates a chained polling strategy with three known polling strategies,
     * {@link OperationResourcePollingStrategy}, {@link LocationPollingStrategy}, and
     * {@link StatusCheckPollingStrategy}, in this order, with a JSON serializer.
     *
     * @param httpPipeline an instance of {@link HttpPipeline} to send requests with
     * @param serializer a custom serializer for serializing and deserializing polling responses
     * @param requestContext an instance of {@link RequestContext}
     * @throws NullPointerException If {@code httpPipeline} is null.
     */
    public DefaultPollingStrategy(HttpPipeline httpPipeline, JsonSerializer serializer, RequestContext requestContext) {
        this(httpPipeline, null, serializer, requestContext);
    }

    /**
     * Creates a chained polling strategy with three known polling strategies,
     * {@link OperationResourcePollingStrategy}, {@link LocationPollingStrategy}, and
     * {@link StatusCheckPollingStrategy}, in this order, with a JSON serializer.
     *
     * @param httpPipeline an instance of {@link HttpPipeline} to send requests with.
     * @param endpoint an endpoint for creating an absolute path when the path itself is relative.
     * @param serializer a custom serializer for serializing and deserializing polling responses.
     * @param requestContext an instance of {@link RequestContext}.
     * @throws NullPointerException If {@code httpPipeline} is null.
     */
    public DefaultPollingStrategy(HttpPipeline httpPipeline, String endpoint, JsonSerializer serializer,
        RequestContext requestContext) {
        this.chainedPollingStrategy = new ChainedPollingStrategy<>(Arrays.asList(
            new OperationResourcePollingStrategy<>(httpPipeline, endpoint, serializer, null, requestContext),
            new LocationPollingStrategy<>(httpPipeline, endpoint, serializer, requestContext),
            new StatusCheckPollingStrategy<>(serializer)));
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
        this.chainedPollingStrategy = new ChainedPollingStrategy<>(
            Arrays.asList(new OperationResourcePollingStrategy<>(null, pollingStrategyOptions),
                new LocationPollingStrategy<>(pollingStrategyOptions),
                new StatusCheckPollingStrategy<>(pollingStrategyOptions.getSerializer())));
    }

    @Override
    public U getResult(PollingContext<T> pollingContext, Type resultType) {
        return chainedPollingStrategy.getResult(pollingContext, resultType);
    }

    @Override
    public boolean canPoll(Response<T> initialResponse) {
        return chainedPollingStrategy.canPoll(initialResponse);
    }

    @Override
    public PollResponse<T> onInitialResponse(Response<T> response, PollingContext<T> pollingContext,
        Type pollResponseType) {
        return chainedPollingStrategy.onInitialResponse(response, pollingContext, pollResponseType);
    }

    @Override
    public PollResponse<T> poll(PollingContext<T> pollingContext, Type pollResponseType) {
        return chainedPollingStrategy.poll(pollingContext, pollResponseType);
    }

    @Override
    public T cancel(PollingContext<T> pollingContext, PollResponse<T> initialResponse) {
        return chainedPollingStrategy.cancel(pollingContext, initialResponse);
    }
}
