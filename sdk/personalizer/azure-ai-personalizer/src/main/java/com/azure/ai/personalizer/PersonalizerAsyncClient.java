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

    public PersonalizerAsyncClient(String endpoint, AzureKeyCredential keyCredential) {
    PersonalizerClientV1Preview3ImplBuilder builder = new PersonalizerClientV1Preview3ImplBuilder();
    HttpClient httpClient = HttpClient.createDefault();
    impl = builder
        .endpoint(endpoint)
        .httpClient(httpClient)
        .addPolicy(new AzureKeyCredentialPolicy(Constants.OCP_APIM_SUBSCRIPTION_KEY, keyCredential))
        .retryPolicy(new RetryPolicy())
        .httpLogOptions(new HttpLogOptions())
        .buildClient();
    }

    public Mono<Response<RankResponse>> rank(RankRequest rankRequest, Context context) {
        context = context == null ? Context.NONE : context;
        return impl.rankWithResponseAsync(rankRequest);
    }

    public Mono<Response<Void>> reward(String eventId, RewardRequest rewardRequest, Context context) {
        context = context == null ? Context.NONE : context;
        return impl.getEvents().rewardWithResponseAsync(eventId, rewardRequest);
    }

    public Mono<Response<Void>> activate(String eventId) {
        return impl.getEvents().activateWithResponseAsync(eventId);
    }

    public Mono<Response<MultiSlotRankResponse>> rankMultiSlot(MultiSlotRankRequest rankRequest) {
        return impl.getMultiSlots().rankWithResponseAsync(rankRequest);
    }

    public Mono<Response<Void>> rewardMultiSlot(String eventId, MultiSlotRewardRequest rewardRequest) {
        return impl.getMultiSlotEvents().rewardWithResponseAsync(eventId, rewardRequest);
    }

    public Mono<Response<Void>> activateMultiSlot(String eventId) {
        return impl.getMultiSlotEvents().activateWithResponseAsync(eventId);
    }
}
