// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.implementation;

import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.PollingContext;
import com.azure.core.util.polling.PollingStrategyOptions;
import com.azure.core.util.serializer.JsonSerializerProviders;
import com.azure.core.util.serializer.ObjectSerializer;
import com.azure.core.util.serializer.TypeReference;
import java.util.Objects;
import java.util.function.Function;

/**
 * Polls campaign monitors synchronously and retrieves the original campaign instance, or no result for deletion.
 * The initial Operation-Id guards the final GET against campaign replacement or name reuse.
 * A bodyless DELETE 204 completes immediately without polling.
 *
 * @param <T> the operation monitor type
 * @param <U> the final campaign type, BinaryData, or Void for deletion
 */
final class SyncCampaignOperationLocationPollingStrategy<T, U> extends SyncOperationLocationPollingStrategy<T, U> {
    private static final ClientLogger LOGGER = new ClientLogger(SyncCampaignOperationLocationPollingStrategy.class);
    private final Function<RequestOptions, Response<BinaryData>> getCampaign;
    private final ObjectSerializer serializer;
    private final Context context;

    SyncCampaignOperationLocationPollingStrategy(PollingStrategyOptions options,
        Function<RequestOptions, Response<BinaryData>> getCampaign) {
        super(options);
        this.getCampaign = Objects.requireNonNull(getCampaign, "'getCampaign' cannot be null.");
        this.serializer
            = options.getSerializer() == null ? JsonSerializerProviders.createInstance(true) : options.getSerializer();
        this.context = options.getContext() == null ? Context.NONE : options.getContext();
    }

    @Override
    public boolean canPoll(Response<?> response) {
        return AgentsServicePollUtils.canPollCampaign(response);
    }

    @Override
    public PollResponse<T> onInitialResponse(Response<?> response, PollingContext<T> pollingContext,
        TypeReference<T> pollResponseType) {
        if (AgentsServicePollUtils.initializeCampaignPollingContext(response, pollingContext)) {
            return new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, null);
        }
        return super.onInitialResponse(response, pollingContext, pollResponseType);
    }

    @Override
    public U getResult(PollingContext<T> pollingContext, TypeReference<U> resultType) {
        if (AgentsServicePollUtils.isCompletedCampaignDeletion(pollingContext)) {
            return null;
        }
        RequestOptions requestOptions = AgentsServicePollUtils.getCampaignFinalRequestOptions(pollingContext, context);
        BinaryData body = AgentsServicePollUtils.getCampaignResponseBody(getCampaign.apply(requestOptions));
        U result = PollingUtils.deserializeResponseSync(body, serializer, resultType);
        if (result == null) {
            throw LOGGER
                .logExceptionAsError(new IllegalStateException("Campaign final GET did not contain a response body."));
        }
        return result;
    }
}
