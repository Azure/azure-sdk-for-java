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

public final class PersonalizerClient {

    private PersonalizerAsyncClient client;

    public PersonalizerClient(PersonalizerAsyncClient client) {
        this.client = client;
    }

    public RankResponse rank(RankRequest rankRequest) {
        Mono<Response<RankResponse>> response = client.rank(rankRequest, Context.NONE);
        return response.block().getValue();
    }

    public void reward(String eventId, RewardRequest rewardRequest) {
        Mono<Response<Void>> response = client.reward(eventId, rewardRequest, Context.NONE);
        response.block();
    }

    public void activate(String eventId) {
        Mono<Response<Void>> response = client.activate(eventId, Context.NONE);
        response.block();
    }

    public MultiSlotRankResponse rankMultiSlot(MultiSlotRankRequest rankRequest) {
        Mono<Response<MultiSlotRankResponse>> response = client.rankMultiSlot(rankRequest, Context.NONE);
        return response.block().getValue();
    }

    public void rewardMultiSlot(String eventId, MultiSlotRewardRequest rewardRequest) {
        Mono<Response<Void>> response = client.rewardMultiSlot(eventId, rewardRequest, Context.NONE);
        response.block();
    }

    public void activateMultiSlot(String eventId) {
        Mono<Response<Void>> response = client.activateMultiSlot(eventId, Context.NONE);
        response.block();
    }
}
