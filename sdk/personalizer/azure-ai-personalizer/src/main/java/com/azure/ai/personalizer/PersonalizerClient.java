// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.personalizer;

import com.azure.ai.personalizer.models.PersonalizerRankMultiSlotOptions;
import com.azure.ai.personalizer.models.PersonalizerRankMultiSlotResult;
import com.azure.ai.personalizer.models.PersonalizerRankOptions;
import com.azure.ai.personalizer.models.PersonalizerRankResult;
import com.azure.ai.personalizer.models.PersonalizerRankableAction;
import com.azure.ai.personalizer.models.PersonalizerRewardMultiSlotOptions;
import com.azure.ai.personalizer.models.PersonalizerSlotOptions;
import com.azure.ai.personalizer.models.PersonalizerSlotReward;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * This class provides a synchronous client that contains the operations that apply to Azure Personalizer.
 * Operations allowed by the client are ranking a set of actions, activation and sending rewards for a single
 * slot as well as multi-slot scenarios.
 *
 * <p><strong>Instantiating a synchronous Personalizer Client</strong></p>
 * <!-- src_embed com.azure.ai.personalizer.PersonalizerClient.instantiation -->
 * <pre>
 * PersonalizerClient personalizerClient = new PersonalizerClientBuilder&#40;&#41;
 *     .credential&#40;new AzureKeyCredential&#40;&quot;&#123;key&#125;&quot;&#41;&#41;
 *     .endpoint&#40;&quot;&#123;endpoint&#125;&quot;&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.ai.personalizer.PersonalizerClient.instantiation -->
 *
 * @see PersonalizerClientBuilder
 */
@ServiceClient(builder = PersonalizerClientBuilder.class, isAsync = false)
public final class PersonalizerClient {
    private final PersonalizerAsyncClient client;

    PersonalizerClient(PersonalizerAsyncClient client) {
        this.client = client;
    }

    /**
     * Request a list of actions to be ranked.
     *
     * <p>Submit a Personalizer rank request. Receives a context and a list of actions. Returns which of the provided
     * actions should be used by your application, in rewardActionId.
     *
     * @param rankOptions A Personalizer Rank request.
     * @return returns which action to use as rewardActionId, and additional information about each action as a result
     * of a Rank request.
     * @throws IllegalArgumentException if rankOptions is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PersonalizerRankResult rank(PersonalizerRankOptions rankOptions) {
        return rankWithResponse(rankOptions, Context.NONE).getValue();
    }

    /**
     * Request a list of actions to be ranked.
     *
     * <p>Submit a Personalizer rank request. Receives a context and a list of actions. Returns which of the provided
     * actions should be used by your application, in rewardActionId.
     *
     * @param rankOptions A Personalizer Rank request.
     * @param context The context to associate with this operation.
     * @return returns which action to use as rewardActionId, and additional information about each action as a result
     * of a Rank request along with {@link Response}.
     * @throws IllegalArgumentException if rankOptions is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<PersonalizerRankResult> rankWithResponse(PersonalizerRankOptions rankOptions, Context context) {
        return client.rankWithResponse(rankOptions, context).block();
    }

    /**
     * Send a reward for an event.
     *
     * <p>Report reward between 0 and 1 that resulted from using the action specified in rewardActionId, for the
     * specified event.
     *
     * @param eventId The event id this reward applies to.
     * @param rewardValue The reward should be a floating point number, typically between 0 and 1.
     * @throws IllegalArgumentException if eventId is null or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void reward(String eventId, float rewardValue) {
        rewardWithResponse(eventId, rewardValue, Context.NONE).getValue();
    }

    /**
     * Send a reward for an event.
     *
     * <p>Report reward between 0 and 1 that resulted from using the action specified in rewardActionId, for the
     * specified event.
     *
     * @param eventId The event id this reward applies to.
     * @param rewardValue The reward should be a floating point number, typically between 0 and 1.
     * @param context The context to associate with this operation.
     * @return the {@link Response}.
     * @throws IllegalArgumentException if eventId is null or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> rewardWithResponse(String eventId, float rewardValue, Context context) {
        return client.rewardWithResponse(eventId, rewardValue, context).block();
    }

    /**
     * Activate Event.
     *
     * <p>Report that the specified event was actually used (e.g. by being displayed to the user) and a reward should be
     * expected for it.
     *
     * @param eventId The event ID to be activated.
     * @throws IllegalArgumentException if eventId is null or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void activate(String eventId) {
        activateWithResponse(eventId, Context.NONE).getValue();
    }

    /**
     * Activate Event.
     *
     * <p>Report that the specified event was actually used (e.g. by being displayed to the user) and a reward should be
     * expected for it.
     *
     * @param eventId The event ID to be activated.
     * @param context The context to associate with this operation.
     * @return the {@link Response}.
     * @throws IllegalArgumentException if eventId is null or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> activateWithResponse(String eventId, Context context) {
        return client.activateWithResponse(eventId, context).block();
    }

    /**
     * Post multi-slot Rank.
     *
     * <p>Submit a Personalizer multi-slot rank request. Receives a context, a list of actions, and a list of slots.
     * Returns which of the provided actions should be used in each slot, in each rewardActionId.
     *
     * @param actions List of actions.
     * @param slots List of slots.
     * @param contextFeatures List of context features.
     * @return the response body.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PersonalizerRankMultiSlotResult rankMultiSlot(List<PersonalizerRankableAction> actions, List<PersonalizerSlotOptions> slots, List<BinaryData> contextFeatures) {
        PersonalizerRankMultiSlotOptions request = new PersonalizerRankMultiSlotOptions().setActions(actions).setSlots(slots).setContextFeatures(contextFeatures);
        return rankMultiSlot(request);
    }

    /**
     * Post multi-slot Rank.
     *
     * <p>Submit a Personalizer multi-slot rank request. Receives a context, a list of actions, and a list of slots.
     * Returns which of the provided actions should be used in each slot, in each rewardActionId.
     *
     * @param rankMultiSlotOptions A Personalizer multi-slot Rank request.
     * @return the response body.
     * @throws IllegalArgumentException if rankMultiSlotOptions is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PersonalizerRankMultiSlotResult rankMultiSlot(PersonalizerRankMultiSlotOptions rankMultiSlotOptions) {
        return rankMultiSlotWithResponse(rankMultiSlotOptions, Context.NONE).getValue();
    }

    /**
     * Post multi-slot Rank.
     *
     * <p>Submit a Personalizer multi-slot rank request. Receives a context, a list of actions, and a list of slots.
     * Returns which of the provided actions should be used in each slot, in each rewardActionId.
     *
     * @param rankMultiSlotOptions A Personalizer multi-slot Rank request.
     * @param context The context to associate with this operation.
     * @return the response body along with {@link Response}.
     * @throws IllegalArgumentException if rankMultiSlotOptions is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<PersonalizerRankMultiSlotResult> rankMultiSlotWithResponse(PersonalizerRankMultiSlotOptions rankMultiSlotOptions, Context context) {
        return client.rankMultiSlotWithResponse(rankMultiSlotOptions, context).block();
    }

    /**
     * Post multi-slot Reward for a single slot.
     *
     * <p>Report reward that resulted from using the action specified in rewardActionId for the slot.
     *
     * @param eventId The event id this reward applies to.
     * @param slotId The slot id of the slot.
     * @param reward The associated reward for the given slot. The reward should be a floating point number, typically
     *                between 0 and 1.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void rewardMultiSlot(String eventId, String slotId, float reward) {
        ArrayList<PersonalizerSlotReward> slotRewards = new ArrayList<PersonalizerSlotReward>();
        slotRewards.add(new PersonalizerSlotReward().setSlotId(slotId).setValue(reward));
        PersonalizerRewardMultiSlotOptions rewardRequest = new PersonalizerRewardMultiSlotOptions().setReward(slotRewards);
        rewardMultiSlot(eventId, rewardRequest);
    }

    /**
     * Post multi-slot Rewards.
     *
     * <p>Report reward that resulted from using the action specified in rewardActionId for the slot.
     *
     * @param eventId The event id this reward applies to.
     * @param rewardMultiSlotOptions List of slot id and reward values. The reward should be a floating point number, typically
     *                               between 0 and 1.
     * @throws IllegalArgumentException if rewardMultiSlotOptions is null or eventId is null or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void rewardMultiSlot(String eventId, PersonalizerRewardMultiSlotOptions rewardMultiSlotOptions) {
        rewardMultiSlotWithResponse(eventId, rewardMultiSlotOptions, Context.NONE).getValue();
    }

    /**
     * Post multi-slot Rewards.
     *
     * <p>Report reward that resulted from using the action specified in rewardActionId for the slot.
     *
     * @param eventId The event id this reward applies to.
     * @param rewardMultiSlotOptions List of slot id and reward values. The reward should be a floating point number, typically
     *                               between 0 and 1.
     * @param context The context to associate with this operation.
     * @return the {@link Response}.
     * @throws IllegalArgumentException if rewardMultiSlotOptions is null or eventId is null or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> rewardMultiSlotWithResponse(String eventId, PersonalizerRewardMultiSlotOptions rewardMultiSlotOptions, Context context) {
        return client.rewardMultiSlotWithResponse(eventId, rewardMultiSlotOptions, context).block();
    }

    /**
     * Activate multi-slot Event.
     *
     * <p>Report that the specified event was actually used or displayed to the user and a rewards should be expected
     * for it.
     *
     * @param eventId The event ID this activation applies to.
     * @throws IllegalArgumentException if eventId is null or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void activateMultiSlot(String eventId) {
        activateMultiSlotWithResponse(eventId, Context.NONE).getValue();
    }

    /**
     * Activate multi-slot Event.
     *
     * <p>Report that the specified event was actually used or displayed to the user and a rewards should be expected
     * for it.
     *
     * @param eventId The event ID this activation applies to.
     * @param context The context to associate with this operation.
     * @return the {@link Response}.
     * @throws IllegalArgumentException if eventId is null or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> activateMultiSlotWithResponse(String eventId, Context context) {
        return client.activateMultiSlotWithResponse(eventId, context).block();
    }
}
