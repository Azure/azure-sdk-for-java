// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.implementation;

import com.azure.core.http.rest.Response;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.PollingContext;
import com.azure.core.util.polling.PollingStrategyOptions;
import com.azure.core.util.serializer.TypeReference;

/**
 * Polls agent optimization jobs through their public project route.
 *
 * @param <T> the poll response type
 * @param <U> the final result type
 */
final class SyncAgentOptimizationOperationLocationPollingStrategy<T, U>
    extends SyncOperationLocationPollingStrategy<T, U> {
    private final String endpoint;

    SyncAgentOptimizationOperationLocationPollingStrategy(PollingStrategyOptions options, String propertyName) {
        super(options, propertyName);
        this.endpoint = options.getEndpoint();
    }

    @Override
    public PollResponse<T> onInitialResponse(Response<?> response, PollingContext<T> pollingContext,
        TypeReference<T> pollResponseType) {
        PollResponse<T> pollResponse = super.onInitialResponse(response, pollingContext, pollResponseType);
        pollingContext.setData(PollingUtils.OPERATION_LOCATION_HEADER.getCaseSensitiveName(),
            AgentsServicePollUtils.getOptimizationPollingUrl(response.getValue(), endpoint));
        return pollResponse;
    }
}
