package com.azure.ai.personalizer;

import com.azure.ai.personalizer.implementation.PersonalizerClientV1Preview3Impl;
import com.azure.ai.personalizer.implementation.PersonalizerClientV1Preview3ImplBuilder;
import com.azure.ai.personalizer.implementation.models.*;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import reactor.core.publisher.Mono;

public final class PersonalizerAsyncClient {

    private PersonalizerClientV1Preview3Impl service;

    public PersonalizerAsyncClient(PersonalizerClientV1Preview3Impl service) {
    PersonalizerClientV1Preview3ImplBuilder builder = new PersonalizerClientV1Preview3ImplBuilder();
    this.service = service;
    }

    public Mono<Response<RankResponse>> rank(RankRequest rankRequest, Context context) {
        context = context == null ? Context.NONE : context;
        return service.rankWithResponseAsync(rankRequest);
    }

    public Mono<Response<Void>> reward(String eventId, RewardRequest rewardRequest, Context context) {
        context = context == null ? Context.NONE : context;
        return service.getEvents().rewardWithResponseAsync(eventId, rewardRequest);
    }

    public Mono<Response<Void>> activate(String eventId, Context context) {
        context = context == null ? Context.NONE : context;
        return service.getEvents().activateWithResponseAsync(eventId, context);
    }

    public Mono<Response<MultiSlotRankResponse>> rankMultiSlot(MultiSlotRankRequest rankRequest, Context context) {
        context = context == null ? Context.NONE : context;
        return service.getMultiSlots().rankWithResponseAsync(rankRequest, context);
    }

    public Mono<Response<Void>> rewardMultiSlot(String eventId, MultiSlotRewardRequest rewardRequest, Context context) {
        context = context == null ? Context.NONE : context;
        return service.getMultiSlotEvents().rewardWithResponseAsync(eventId, rewardRequest, context);
    }

    public Mono<Response<Void>> activateMultiSlot(String eventId, Context context) {
        context = context == null ? Context.NONE : context;
        return service.getMultiSlotEvents().activateWithResponseAsync(eventId, context);
    }
}
