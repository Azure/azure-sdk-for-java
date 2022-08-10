// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.personalizer;

import com.azure.ai.personalizer.models.PersonalizerRankMultiSlotOptions;
import com.azure.ai.personalizer.models.PersonalizerRankMultiSlotResult;
import com.azure.ai.personalizer.models.PersonalizerRewardMultiSlotOptions;
import com.azure.ai.personalizer.models.PersonalizerRankOptions;
import com.azure.ai.personalizer.models.PersonalizerRankResult;
import com.azure.ai.personalizer.models.PersonalizerRankableAction;
import com.azure.ai.personalizer.models.PersonalizerSlotOptions;
import com.azure.ai.personalizer.models.PersonalizerSlotReward;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * Client to call the Personalizer instance in a synchronous manner.
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
     * @param rankRequest A Personalizer Rank request.
     * @return returns which action to use as rewardActionId, and additional information about each action as a result
     *     of a Rank request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PersonalizerRankResult rank(PersonalizerRankOptions rankRequest) {
        return rankWithResponse(rankRequest, Context.NONE).getValue();
    }

    /**
     * Request a list of actions to be ranked.
     *
     * <p>Submit a Personalizer rank request. Receives a context and a list of actions. Returns which of the provided
     * actions should be used by your application, in rewardActionId.
     *
     * @param rankRequest A Personalizer Rank request.
     * @param context The context to associate with this operation.
     * @return returns which action to use as rewardActionId, and additional information about each action as a result
     *     of a Rank request along with {@link Response}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<PersonalizerRankResult> rankWithResponse(PersonalizerRankOptions rankRequest, Context context) {
        return client.rankWithResponse(rankRequest, context).block();
    }

    /**
     * Send a reward for an event.
     *
     * <p>Report reward between 0 and 1 that resulted from using the action specified in rewardActionId, for the
     * specified event.
     *
     * @param eventId The event id this reward applies to.
     * @param rewardValue The reward should be a floating point number, typically between 0 and 1.
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
     * @return List of slot responses for each slot along with eventId.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PersonalizerRankMultiSlotResult rankMultiSlot(List<PersonalizerRankableAction> actions, List<PersonalizerSlotOptions> slots, List<Object> contextFeatures) {
        PersonalizerRankMultiSlotOptions request = new PersonalizerRankMultiSlotOptions().setActions(actions).setSlots(slots).setContextFeatures(contextFeatures);
        return rankMultiSlot(request);
    }

    /**
     * Post multi-slot Rank.
     *
     * <p>Submit a Personalizer multi-slot rank request. Receives a context, a list of actions, and a list of slots.
     * Returns which of the provided actions should be used in each slot, in each rewardActionId.
     *
     * @param rankRequest A Personalizer multi-slot Rank request.
     * @return the response body.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PersonalizerRankMultiSlotResult rankMultiSlot(PersonalizerRankMultiSlotOptions rankRequest) {
        return rankMultiSlotWithResponse(rankRequest, Context.NONE).getValue();
    }

    /**
     * Post multi-slot Rank.
     *
     * <p>Submit a Personalizer multi-slot rank request. Receives a context, a list of actions, and a list of slots.
     * Returns which of the provided actions should be used in each slot, in each rewardActionId.
     *
     * @param rankRequest A Personalizer multi-slot Rank request.
     * @param context The context to associate with this operation.
     * @return the response body along with {@link Response}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<PersonalizerRankMultiSlotResult> rankMultiSlotWithResponse(PersonalizerRankMultiSlotOptions rankRequest, Context context) {
        return client.rankMultiSlotWithResponse(rankRequest, context).block();
    }

    /**
     * Post multi-slot Reward for a single slot.
     *
     * <p>Report reward that resulted from using the action specified in rewardActionId for the slot.
     *
     * @param eventId The event id this reward applies to.
     * @param slotId The slot id of the slot.
     * @param reward The associated reward for the given slot. The reward should be a floating point number, typically
     *               between 0 and 1.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void rewardMultiSlot(String eventId, String slotId, float reward) {
        PersonalizerRewardMultiSlotOptions rewardRequest = new PersonalizerRewardMultiSlotOptions().setReward(new ArrayList<PersonalizerSlotReward>() {
            {
                add(new PersonalizerSlotReward().setSlotId(slotId).setValue(reward));
            }
        });
        rewardMultiSlot(eventId, rewardRequest);
    }

    /**
     * Post multi-slot Rewards.
     *
     * <p>Report reward that resulted from using the action specified in rewardActionId for the slot.
     *
     * @param eventId The event id this reward applies to.
     * @param rewardRequest List of slot id and reward values. The reward should be a floating point number, typically
     *                      between 0 and 1.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void rewardMultiSlot(String eventId, PersonalizerRewardMultiSlotOptions rewardRequest) {
        rewardMultiSlotWithResponse(eventId, rewardRequest, Context.NONE).getValue();
    }

    /**
     * Post multi-slot Rewards.
     *
     * <p>Report reward that resulted from using the action specified in rewardActionId for the slot.
     *
     * @param eventId The event id this reward applies to.
     * @param rewardRequest List of slot id and reward values. The reward should be a floating point number, typically
     *                      between 0 and 1.
     * @param context The context to associate with this operation.
     * @return the {@link Response}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> rewardMultiSlotWithResponse(String eventId, PersonalizerRewardMultiSlotOptions rewardRequest, Context context) {
        return client.rewardMultiSlotWithResponse(eventId, rewardRequest, context).block();
    }

    /**
     * Activate multi-slot Event.
     *
     * <p>Report that the specified event was actually used or displayed to the user and a rewards should be expected
     * for it.
     *
     * @param eventId The event ID this activation applies to.
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
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> activateMultiSlotWithResponse(String eventId, Context context) {
        return client.activateMultiSlotWithResponse(eventId, context).block();
    }
}
