package com.azure.ai.personalizer;

import com.azure.ai.personalizer.implementation.PersonalizerClientV1Preview3Impl;
import com.azure.ai.personalizer.implementation.PersonalizerClientV1Preview3ImplBuilder;
import com.azure.ai.personalizer.implementation.models.*;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.AzureKeyCredentialPolicy;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import reactor.core.publisher.Mono;

public final class PersonalizerAsyncClient {

    private PersonalizerClientV1Preview3Impl impl;

    public PersonalizerAsyncClient(PersonalizerClientV1Preview3Impl service) {
    PersonalizerClientV1Preview3ImplBuilder builder = new PersonalizerClientV1Preview3ImplBuilder();
    impl = service;
    }

    public Mono<Response<RankResponse>> rank(RankRequest rankRequest, Context context) {
        context = context == null ? Context.NONE : context;
        return impl.rankWithResponseAsync(rankRequest);
    }

    public Mono<Response<Void>> reward(String eventId, RewardRequest rewardRequest, Context context) {
        context = context == null ? Context.NONE : context;
        return impl.getEvents().rewardWithResponseAsync(eventId, rewardRequest);
    }

    public Mono<Response<Void>> activate(String eventId, Context context) {
        context = context == null ? Context.NONE : context;
        return impl.getEvents().activateWithResponseAsync(eventId, context);
    }

    public Mono<Response<MultiSlotRankResponse>> rankMultiSlot(MultiSlotRankRequest rankRequest, Context context) {
        context = context == null ? Context.NONE : context;
        return impl.getMultiSlots().rankWithResponseAsync(rankRequest, context);
    }

    public Mono<Response<Void>> rewardMultiSlot(String eventId, MultiSlotRewardRequest rewardRequest, Context context) {
        context = context == null ? Context.NONE : context;
        return impl.getMultiSlotEvents().rewardWithResponseAsync(eventId, rewardRequest, context);
    }

    public Mono<Response<Void>> activateMultiSlot(String eventId, Context context) {
        context = context == null ? Context.NONE : context;
        return impl.getMultiSlotEvents().activateWithResponseAsync(eventId, context);
    }
}
