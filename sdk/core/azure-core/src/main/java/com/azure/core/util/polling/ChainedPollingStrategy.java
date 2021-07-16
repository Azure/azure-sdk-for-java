// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.polling;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.serializer.TypeReference;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Type;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A polling strategy that chains multiple polling strategies, finds the first strategy that can poll the current
 * long running operation, and polls with that strategy.
 */
public class ChainedPollingStrategy implements PollingStrategy {
    private final List<PollingStrategy> pollingStrategies;
    private PollingStrategy pollableStrategy = null;

    /**
     * Creates an empty chained polling strategy.
     */
    public ChainedPollingStrategy() {
        this.pollingStrategies = new ArrayList<>();
    }

    /**
     * Creates a chained polling strategy with 3 known polling strategies, {@link OperationResourcePollingStrategy},
     * {@link LocationPollingStrategy}, and {@link StatusCheckPollingStrategy}, in this order. The first strategy that
     * can poll on the initial response will be used. The created chained polling strategy is capable of handling most
     * of the polling scenarios in Azure.
     *
     * @param httpPipeline an instance of {@link HttpPipeline} to send requests with
     * @param context additional metadata to pass along with the request
     * @return the initialized chained polling strategy with the default chain
     */
    public static ChainedPollingStrategy createDefault(
            HttpPipeline httpPipeline,
            Context context) {
        return new ChainedPollingStrategy()
            .addPollingStrategy(new OperationResourcePollingStrategy(httpPipeline, context))
            .addPollingStrategy(new LocationPollingStrategy(httpPipeline, context))
            .addPollingStrategy(new StatusCheckPollingStrategy());
    }

    /**
     * Adds a polling strategy to the chain of polling strategies.
     * @param pollingStrategy the polling strategy to add
     * @return the modified ChainedPollingStrategy instance
     */
    public ChainedPollingStrategy addPollingStrategy(PollingStrategy pollingStrategy) {
        this.pollingStrategies.add(pollingStrategy);
        return this;
    }

    @Override
    public Mono<Boolean> canPoll(Response<?> initialResponse) {
        return Flux.fromIterable(pollingStrategies)
            .concatMap(strategy -> strategy.canPoll(initialResponse).doOnNext(canPoll -> {
                if (canPoll && pollableStrategy == null) {
                    this.pollableStrategy = strategy;
                }
            }))
            .any(canPoll -> canPoll)
            .switchIfEmpty(Mono.just(false));
    }

    @Override
    public <U> Mono<U> getResult(PollingContext<BinaryData> context, TypeReference<U> resultType) {
        return pollableStrategy.getResult(context, resultType);
    }

    @Override
    public Mono<LongRunningOperationStatus> onInitialResponse(Response<?> response, PollingContext<BinaryData> pollingContext) {
        return pollableStrategy.onInitialResponse(response, pollingContext);
    }

    @Override
    public Mono<PollResponse<BinaryData>> poll(PollingContext<BinaryData> context) {
        return pollableStrategy.poll(context);
    }

    @Override
    public Mono<BinaryData> cancel(PollingContext<BinaryData> pollingContext, PollResponse<BinaryData> initialResponse) {
        return pollableStrategy.cancel(pollingContext, initialResponse);
    }
}
