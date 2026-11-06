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
import reactor.core.publisher.Mono;

/**
 * Polls campaign monitors and retrieves the original campaign instance, or no result for deletion.
 * The initial Operation-Id guards the final GET against campaign replacement or name reuse.
 * A bodyless DELETE 204 completes immediately without polling.
 *
 * @param <T> the operation monitor type
 * @param <U> the final campaign type, BinaryData, or Void for deletion
 */
final class CampaignOperationLocationPollingStrategy<T, U> extends OperationLocationPollingStrategy<T, U> {
    private static final ClientLogger LOGGER = new ClientLogger(CampaignOperationLocationPollingStrategy.class);
    private final Function<RequestOptions, Mono<Response<BinaryData>>> getCampaign;
    private final ObjectSerializer serializer;
    private final Context context;

    CampaignOperationLocationPollingStrategy(PollingStrategyOptions options,
        Function<RequestOptions, Mono<Response<BinaryData>>> getCampaign) {
        super(options);
        this.getCampaign = Objects.requireNonNull(getCampaign, "'getCampaign' cannot be null.");
        this.serializer
            = options.getSerializer() == null ? JsonSerializerProviders.createInstance(true) : options.getSerializer();
        this.context = options.getContext() == null ? Context.NONE : options.getContext();
    }

    @Override
    public Mono<Boolean> canPoll(Response<?> response) {
        return Mono.fromSupplier(() -> AgentsServicePollUtils.canPollCampaign(response));
    }

    @Override
    public Mono<PollResponse<T>> onInitialResponse(Response<?> response, PollingContext<T> pollingContext,
        TypeReference<T> pollResponseType) {
        return Mono.defer(() -> {
            if (AgentsServicePollUtils.initializeCampaignPollingContext(response, pollingContext)) {
                return Mono.just(new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, null));
            }
            return super.onInitialResponse(response, pollingContext, pollResponseType);
        });
    }

    @Override
    public Mono<U> getResult(PollingContext<T> pollingContext, TypeReference<U> resultType) {
        return Mono.defer(() -> {
            if (AgentsServicePollUtils.isCompletedCampaignDeletion(pollingContext)) {
                return Mono.empty();
            }
            RequestOptions requestOptions
                = AgentsServicePollUtils.getCampaignFinalRequestOptions(pollingContext, context);
            return getCampaign.apply(requestOptions)
                .flatMap(response -> PollingUtils.deserializeResponse(
                    AgentsServicePollUtils.getCampaignResponseBody(response), serializer, resultType))
                .switchIfEmpty(Mono.defer(() -> Mono.error(LOGGER.logExceptionAsError(
                    new IllegalStateException("Campaign final GET did not contain a response body.")))));
        });
    }
}
