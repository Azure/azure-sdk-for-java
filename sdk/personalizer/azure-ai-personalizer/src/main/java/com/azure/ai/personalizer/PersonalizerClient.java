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
import reactor.core.publisher.Mono;

public final class PersonalizerClient {

    private PersonalizerClientV1Preview3Impl impl;

    public PersonalizerClient(String endpoint, AzureKeyCredential keyCredential) {
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

    public RankResponse rank(RankRequest rankRequest) {
        Mono<Response<RankResponse>> response = impl.rankWithResponseAsync(rankRequest);
        return response.block().getValue();
    }

    public void reward(String eventId, RewardRequest rewardRequest) {
        Mono<Response<Void>> response = impl.getEvents().rewardWithResponseAsync(eventId, rewardRequest);
        response.block();
    }

    public void activate(String eventId) {
        Mono<Response<Void>> response = impl.getEvents().activateWithResponseAsync(eventId);
        response.block();
    }

    public MultiSlotRankResponse rankMultiSlot(MultiSlotRankRequest rankRequest) {
        Mono<Response<MultiSlotRankResponse>> response = impl.getMultiSlots().rankWithResponseAsync(rankRequest);
        return response.block().getValue();
    }

    public void rewardMultiSlot(String eventId, MultiSlotRewardRequest rewardRequest) {
        Mono<Response<Void>> response = impl.getMultiSlotEvents().rewardWithResponseAsync(eventId, rewardRequest);
        response.block();
    }

    public void activateMultiSlot(String eventId) {
        Mono<Response<Void>> response = impl.getMultiSlotEvents().activateWithResponseAsync(eventId);
        response.block();
    }
}
