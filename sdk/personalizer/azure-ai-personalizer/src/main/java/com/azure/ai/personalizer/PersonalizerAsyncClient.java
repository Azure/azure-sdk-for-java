// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.personalizer;

import com.azure.ai.personalizer.implementation.PersonalizerClientV1Preview3Impl;
import com.azure.ai.personalizer.implementation.util.Transforms;
import com.azure.ai.personalizer.models.PersonalizerRankMultiSlotOptions;
import com.azure.ai.personalizer.models.PersonalizerRankMultiSlotResult;
import com.azure.ai.personalizer.models.PersonalizerRankOptions;
import com.azure.ai.personalizer.models.PersonalizerRankResult;
import com.azure.ai.personalizer.models.PersonalizerRewardMultiSlotOptions;
import com.azure.ai.personalizer.models.PersonalizerRewardOptions;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;

/**
 * This class provides an asynchronous client that contains the operations that apply to Azure Personalizer.
 * Operations allowed by the client are ranking a set of actions, activation and sending rewards for a single
 * slot as well as multi-slot scenarios.
 *
 * <p><strong>Instantiating an asynchronous Personalizer Client</strong></p>
 * <!-- src_embed com.azure.ai.personalizer.PersonalizerAsyncClient.instantiation -->
 * <pre>
 * PersonalizerAsyncClient personalizerClient = new PersonalizerClientBuilder&#40;&#41;
 *     .credential&#40;new AzureKeyCredential&#40;&quot;&#123;key&#125;&quot;&#41;&#41;
 *     .endpoint&#40;&quot;&#123;endpoint&#125;&quot;&#41;
 *     .buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.ai.personalizer.PersonalizerAsyncClient.instantiation -->
 *
 * @see PersonalizerClientBuilder
 */
@ServiceClient(builder = PersonalizerClientBuilder.class, isAsync = true)
public final class PersonalizerAsyncClient {

    private final ClientLogger logger = new ClientLogger(PersonalizerAsyncClient.class);
    private final PersonalizerClientV1Preview3Impl service;

    PersonalizerAsyncClient(PersonalizerClientV1Preview3Impl service) {
        this.service = service;
    }

    /**
     * Request a list of actions to be ranked.
     *
     * <p>Submit a Personalizer rank request. Receives a context and a list of actions. Returns which of the provided
     * actions should be used by your application, in rewardActionId.
     *
     * @param rankRequest A Personalizer Rank request.
     * @return returns which action to use as rewardActionId, and additional information about each action as a result
     * of a Rank request.
     * @throws IllegalArgumentException if rankOptions is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PersonalizerRankResult> rank(PersonalizerRankOptions rankRequest) {
        return rankWithResponse(rankRequest).flatMap(FluxUtil::toMono);
    }

    /**
     * Request a list of actions to be ranked.
     *
     * <p>Submit a Personalizer rank request. Receives a context and a list of actions. Returns which of the provided
     * actions should be used by your application, in rewardActionId.
     *
     * @param rankOptions A Personalizer Rank request.
     * @return returns which action to use as rewardActionId, and additional information about each action as a result
     * of a Rank request along with {@link Response} on successful completion of {@link Mono}.
     * @throws IllegalArgumentException if rankOptions is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<PersonalizerRankResult>> rankWithResponse(PersonalizerRankOptions rankOptions) {
        try {
            return withContext(context -> rankWithResponse(rankOptions, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<PersonalizerRankResult>> rankWithResponse(PersonalizerRankOptions rankOptions, Context context) {
        if (rankOptions == null) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'rankOptions' is required and cannot be null"));
        }
        return service.rankWithResponseAsync(rankOptions, context)
            .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists)
            .map(response -> new SimpleResponse<>(response, response.getValue()));
    }

    /**
     * Send a reward for an event.
     *
     * <p>Report reward between 0 and 1 that resulted from using the action specified in rewardActionId, for the
     * specified event.
     *
     * @param eventId The event id this reward applies to.
     * @param rewardValue The reward should be a floating point number, typically between 0 and 1.
     * @return the completion of {@link Mono}.
     * @throws IllegalArgumentException if eventId is null or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> reward(String eventId, float rewardValue) {
        return rewardWithResponse(eventId, rewardValue).flatMap(FluxUtil::toMono);
    }

    /**
     * Send a reward for an event.
     *
     * <p>Report reward between 0 and 1 that resulted from using the action specified in rewardActionId, for the
     * specified event.
     *
     * @param eventId The event id this reward applies to.
     * @param rewardValue The reward should be a floating point number, typically between 0 and 1.
     * @return the {@link Response} on successful completion of {@link Mono}.
     * @throws IllegalArgumentException if eventId is null or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> rewardWithResponse(String eventId, float rewardValue) {
        try {
            return withContext(context -> rewardWithResponse(eventId, rewardValue, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> rewardWithResponse(String eventId, float rewardValue, Context context) {
        if (CoreUtils.isNullOrEmpty(eventId)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'eventId' is required and cannot be null or empty"));
        }
        return service.getEvents().rewardWithResponseAsync(eventId, new PersonalizerRewardOptions().setValue(rewardValue), context)
            .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists)
            .map(response -> new SimpleResponse<>(response, null));
    }

    /**
     * Activate Event.
     *
     * <p>Report that the specified event was actually used (e.g. by being displayed to the user) and a reward should be
     * expected for it.
     *
     * @param eventId The event ID to be activated.
     * @return the completion of {@link Mono}.
     * @throws IllegalArgumentException if eventId is null or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> activate(String eventId) {
        return activateWithResponse(eventId).flatMap(FluxUtil::toMono);
    }

    /**
     * Activate Event.
     *
     * <p>Report that the specified event was actually used (e.g. by being displayed to the user) and a reward should be
     * expected for it.
     *
     * @param eventId The event ID to be activated.
     * @return the {@link Response} on successful completion of {@link Mono}.
     * @throws IllegalArgumentException if eventId is null or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> activateWithResponse(String eventId) {
        try {
            return withContext(context -> activateWithResponse(eventId, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> activateWithResponse(String eventId, Context context) {
        if (CoreUtils.isNullOrEmpty(eventId)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'eventId' is required and cannot be null or empty"));
        }
        return service.getEvents().activateWithResponseAsync(eventId, context)
            .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists)
            .map(response -> new SimpleResponse<>(response, null));
    }

    /**
     * Post multi-slot Rank.
     *
     * <p>Submit a Personalizer multi-slot rank request. Receives a context, a list of actions, and a list of slots.
     * Returns which of the provided actions should be used in each slot, in each rewardActionId.
     *
     * @param rankMultiSlotOptions A Personalizer multi-slot Rank request.
     * @return the response body on successful completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PersonalizerRankMultiSlotResult> rankMultiSlot(PersonalizerRankMultiSlotOptions rankMultiSlotOptions) {
        return rankMultiSlotWithResponse(rankMultiSlotOptions).flatMap(FluxUtil::toMono);
    }

    /**
     * Post multi-slot Rank.
     *
     * <p>Submit a Personalizer multi-slot rank request. Receives a context, a list of actions, and a list of slots.
     * Returns which of the provided actions should be used in each slot, in each rewardActionId.
     *
     * @param rankMultiSlotOptions A Personalizer multi-slot Rank request.
     * @return the response body along with {@link Response} on successful completion of {@link Mono}.
     * @throws IllegalArgumentException if rankMultiSlotOptions is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<PersonalizerRankMultiSlotResult>> rankMultiSlotWithResponse(PersonalizerRankMultiSlotOptions rankMultiSlotOptions) {
        try {
            return withContext(context -> rankMultiSlotWithResponse(rankMultiSlotOptions, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<PersonalizerRankMultiSlotResult>> rankMultiSlotWithResponse(PersonalizerRankMultiSlotOptions rankMultiSlotOptions, Context context) {
        if (rankMultiSlotOptions == null) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'rankMultiSlotOptions' is required and cannot be null"));
        }
        return service.getMultiSlots().rankWithResponseAsync(rankMultiSlotOptions, context)
            .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists)
            .map(response -> new SimpleResponse<>(response, response.getValue()));
    }

    /**
     * Post multi-slot Rewards.
     *
     * <p>Report reward that resulted from using the action specified in rewardActionId for the slot.
     *
     * @param eventId The event id this reward applies to.
     * @param rewardMultiSlotOptions List of slot id and reward values. The reward should be a floating point number, typically between 0
     *                      and 1.
     * @return the completion of {@link Mono}.
     * @throws IllegalArgumentException if rewardMultiSlotOptions is null or eventId is null or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> rewardMultiSlot(String eventId, PersonalizerRewardMultiSlotOptions rewardMultiSlotOptions) {
        return rewardMultiSlotWithResponse(eventId, rewardMultiSlotOptions).flatMap(FluxUtil::toMono);
    }

    /**
     * Post multi-slot Rewards.
     *
     * <p>Report reward that resulted from using the action specified in rewardActionId for the slot.
     *
     * @param eventId The event id this reward applies to.
     * @param rewardMultiSlotOptions List of slot id and reward values. The reward should be a floating point number, typically between 0
     *                      and 1.
     * @return the {@link Response} on successful completion of {@link Mono}.
     * @throws IllegalArgumentException if rewardMultiSlotOptions is null or eventId is null or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> rewardMultiSlotWithResponse(String eventId, PersonalizerRewardMultiSlotOptions rewardMultiSlotOptions) {
        try {
            return withContext(context -> rewardMultiSlotWithResponse(eventId, rewardMultiSlotOptions, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> rewardMultiSlotWithResponse(String eventId, PersonalizerRewardMultiSlotOptions rewardMultiSlotOptions, Context context) {
        if (CoreUtils.isNullOrEmpty(eventId)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'eventId' is required and cannot be null or empty"));
        }
        if (rewardMultiSlotOptions == null) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'rewardMultiSlotOptions' is required and cannot be null"));
        }
        return service.getMultiSlotEvents().rewardWithResponseAsync(eventId, rewardMultiSlotOptions, context)
            .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists)
            .map(response -> new SimpleResponse<>(response, null));
    }

    /**
     * Activate multi-slot Event.
     *
     * <p>Report that the specified event was actually used or displayed to the user and a rewards should be expected
     * for it.
     *
     * @param eventId The event ID this activation applies to.
     * @return the completion of {@link Mono}.
     * @throws IllegalArgumentException if eventId is null or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> activateMultiSlot(String eventId) {
        return activateMultiSlotWithResponse(eventId).flatMap(FluxUtil::toMono);
    }

    /**
     * Activate multi-slot Event.
     *
     * <p>Report that the specified event was actually used or displayed to the user and a rewards should be expected
     * for it.
     *
     * @param eventId The event ID this activation applies to.
     * @return the {@link Response} on successful completion of {@link Mono}.
     * @throws IllegalArgumentException if eventId is null or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> activateMultiSlotWithResponse(String eventId) {
        try {
            return withContext(context -> activateMultiSlotWithResponse(eventId, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> activateMultiSlotWithResponse(String eventId, Context context) {
        if (CoreUtils.isNullOrEmpty(eventId)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'eventId' is required and cannot be null or empty"));
        }
        return service.getMultiSlotEvents().activateWithResponseAsync(eventId, context)
            .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists)
            .map(response -> new SimpleResponse<>(response, null));
    }
}
