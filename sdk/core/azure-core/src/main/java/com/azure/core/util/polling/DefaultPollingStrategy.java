// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.polling;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.serializer.JsonSerializer;
import com.azure.core.util.serializer.TypeReference;
import reactor.core.publisher.Mono;

import java.util.Arrays;

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
public class DefaultPollingStrategy<T, U> implements PollingStrategy<T, U> {
    private final ChainedPollingStrategy<T, U> chainedPollingStrategy;

    /**
     * Creates a chained polling strategy with 3 known polling strategies, {@link OperationResourcePollingStrategy},
     * {@link LocationPollingStrategy}, and {@link StatusCheckPollingStrategy}, in this order, with a JSON serializer.
     *
     * @param httpPipeline an instance of {@link HttpPipeline} to send requests with
     * @param context additional metadata to pass along with the request
     */
    public DefaultPollingStrategy(HttpPipeline httpPipeline, Context context) {
        this.chainedPollingStrategy = new ChainedPollingStrategy<>(Arrays.asList(
            new OperationResourcePollingStrategy<>(httpPipeline, context),
            new LocationPollingStrategy<>(httpPipeline, context),
            new StatusCheckPollingStrategy<>()));
    }

    /**
     * Creates a chained polling strategy with 3 known polling strategies, {@link OperationResourcePollingStrategy},
     * {@link LocationPollingStrategy}, and {@link StatusCheckPollingStrategy}, in this order, with a custom
     * serializer.
     *
     * @param httpPipeline an instance of {@link HttpPipeline} to send requests with
     * @param context additional metadata to pass along with the request
     * @param serializer a custom serializer for serializing and deserializing polling responses
     */
    public DefaultPollingStrategy(HttpPipeline httpPipeline, Context context, JsonSerializer serializer) {
        this.chainedPollingStrategy = new ChainedPollingStrategy<>(Arrays.asList(
            new OperationResourcePollingStrategy<>(httpPipeline, context, serializer, null),
            new LocationPollingStrategy<>(httpPipeline, context, serializer),
            new StatusCheckPollingStrategy<>(serializer)));
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

    @Override
    public Mono<T> cancel(PollingContext<T> pollingContext, PollResponse<T> initialResponse) {
        return chainedPollingStrategy.cancel(pollingContext, initialResponse);
    }
}
