// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.polling.strategy;

import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollingContext;
import reactor.core.publisher.Mono;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * A polling strategy that chains multiple polling strategies, finds the first strategy that can poll the current
 * long running operation, and polls with that strategy.
 */
public class ChainedPollingStrategy implements PollingStrategy {
    private final List<PollingStrategy> pollingStrategies;
    private PollingStrategy pollableStrategy;

    /**
     * Creates an instance of the {@link ChainedPollingStrategy} with an empty chain.
     */
    public ChainedPollingStrategy() {
        this.pollingStrategies = new ArrayList<>();
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
    public boolean canPoll(Response<?> activationResponse) {
        for (PollingStrategy strategy : pollingStrategies) {
            if (strategy.canPoll(activationResponse)) {
                pollableStrategy = strategy;
                break;
            }
        }
        if (pollableStrategy != null) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String getPollingUrl(PollingContext<BinaryData> context) {
        return pollableStrategy.getPollingUrl(context);
    }

    @Override
    public String getFinalGetUrl(PollingContext<BinaryData> context) {
        return pollableStrategy.getFinalGetUrl(context);
    }

    @Override
    public <U> Mono<U> getFinalResult(HttpResponse response, PollingContext<BinaryData> context, Type resultType) {
        return pollableStrategy.getFinalResult(response, context, resultType);
    }

    @Override
    public Mono<LongRunningOperationStatus> onActivationResponse(Response<?> response, PollingContext<BinaryData> context) {
        return pollableStrategy.onActivationResponse(response, context);
    }

    @Override
    public Mono<LongRunningOperationStatus> onPollingResponse(HttpResponse response, PollingContext<BinaryData> context) {
        return pollableStrategy.onPollingResponse(response, context);
    }
}
