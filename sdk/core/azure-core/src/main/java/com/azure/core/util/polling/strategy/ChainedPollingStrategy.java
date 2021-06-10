// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.polling.strategy;

import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.util.polling.PollResult;
import com.azure.core.util.polling.PollingContext;

import java.util.Arrays;
import java.util.List;

public class ChainedPollingStrategy implements PollingStrategy {
    private final List<PollingStrategy> pollingStrategies;
    private PollingStrategy pollableStrategy;

    public ChainedPollingStrategy() {
        this.pollingStrategies = Arrays.asList(
                new OperationResourcePollingStrategy(),
                new LocationPollingStrategy());
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
    public String getPollingUrl(PollingContext<PollResult> ctx) {
        return pollableStrategy.getPollingUrl(ctx);
    }

    @Override
    public String getFinalGetUrl(PollingContext<PollResult> ctx) {
        return pollableStrategy.getFinalGetUrl(ctx);
    }

    @Override
    public PollResult onActivationResponse(Response<?> response, PollingContext<PollResult> ctx) {
        return pollableStrategy.onActivationResponse(response, ctx);
    }

    @Override
    public PollResult onPollingResponse(HttpResponse response, String responseBody, PollingContext<PollResult> ctx) {
        return pollableStrategy.onPollingResponse(response, responseBody, ctx);
    }
}
