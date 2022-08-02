// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.personalizer;

import com.azure.ai.personalizer.models.*;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;

import java.util.ArrayList;
import java.util.List;

final class PersonalizerClient {
 PersonalizerAsyncClient client;
 PersonalizerClient(PersonalizerAsyncClient client) {
        this.client = client;
    }

    public RankResponse rank(List<RankableAction> actions, List<Object> contextFeatures) {
     return rank(new RankRequest().setActions(actions).setContextFeatures(contextFeatures));
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public RankResponse rank(RankRequest rankRequest) {
        return rankWithResponse(rankRequest, Context.NONE).getValue();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<RankResponse> rankWithResponse(RankRequest rankRequest, Context context) {
        return client.rankWithResponse(rankRequest, context).block();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public void reward(String eventId, float rewardValue) {
        rewardWithResponse(eventId, rewardValue, Context.NONE).getValue();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> rewardWithResponse(String eventId, float rewardValue, Context context) {
        return client.rewardWithResponse(eventId, rewardValue, context).block();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public void activate(String eventId) {
        activateWithResponse(eventId, Context.NONE).getValue();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> activateWithResponse(String eventId, Context context) {
        return client.activateWithResponse(eventId, context).block();
    }

     @ServiceMethod(returns = ReturnType.SINGLE)
     public MultiSlotRankResponse rankMultiSlot(List<RankableAction> actions, List<SlotRequest> slots, List<Object> contextFeatures) {
         MultiSlotRankRequest request = new MultiSlotRankRequest().setActions(actions).setSlots(slots).setContextFeatures(contextFeatures);
         return rankMultiSlot(request);
     }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public MultiSlotRankResponse rankMultiSlot(MultiSlotRankRequest rankRequest) {
        return rankMultiSlotWithResponse(rankRequest, Context.NONE).getValue();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<MultiSlotRankResponse> rankMultiSlotWithResponse(MultiSlotRankRequest rankRequest, Context context) {
        return client.rankMultiSlotWithResponse(rankRequest, context).block();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public void rewardMultiSlot(String eventId, String slotId, float reward) {
        MultiSlotRewardRequest rewardRequest = new MultiSlotRewardRequest().setReward(new ArrayList<SlotReward>() {
            {
                add(new SlotReward().setSlotId(slotId).setValue(reward));
            }
        });
        rewardMultiSlot(eventId, rewardRequest);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public void rewardMultiSlot(String eventId, MultiSlotRewardRequest rewardRequest) {
        rewardMultiSlotWithResponse(eventId, rewardRequest, Context.NONE).getValue();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> rewardMultiSlotWithResponse(String eventId, MultiSlotRewardRequest rewardRequest, Context context) {
        return client.rewardMultiSlotWithResponse(eventId, rewardRequest, context).block();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public void activateMultiSlot(String eventId) {
        activateMultiSlotWithResponse(eventId, Context.NONE).getValue();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> activateMultiSlotWithResponse(String eventId, Context context) {
        return client.activateMultiSlotWithResponse(eventId, context).block();
    }
}
