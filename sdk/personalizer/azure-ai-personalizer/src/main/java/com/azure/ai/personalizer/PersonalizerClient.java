// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.personalizer;

import com.azure.ai.personalizer.implementation.models.MultiSlotRankRequest;
import com.azure.ai.personalizer.implementation.models.MultiSlotRankResponse;
import com.azure.ai.personalizer.implementation.models.MultiSlotRewardRequest;
import com.azure.ai.personalizer.implementation.models.RankRequest;
import com.azure.ai.personalizer.implementation.models.RankResponse;
import com.azure.ai.personalizer.implementation.models.RewardRequest;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;

public final class PersonalizerClient {

    private PersonalizerAsyncClient client;

    public PersonalizerClient(PersonalizerAsyncClient client) {
        this.client = client;
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
    public void reward(String eventId, RewardRequest rewardRequest) {
        rewardWithResponse(eventId, rewardRequest, Context.NONE).getValue();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> rewardWithResponse(String eventId, RewardRequest rewardRequest, Context context) {
        return client.rewardWithResponse(eventId, rewardRequest, context).block();
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
    public MultiSlotRankResponse rankMultiSlot(MultiSlotRankRequest rankRequest) {
        return rankMultiSlotWithResponse(rankRequest, Context.NONE).getValue();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<MultiSlotRankResponse> rankMultiSlotWithResponse(MultiSlotRankRequest rankRequest, Context context) {
        return client.rankMultiSlotWithResponse(rankRequest, context).block();
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
