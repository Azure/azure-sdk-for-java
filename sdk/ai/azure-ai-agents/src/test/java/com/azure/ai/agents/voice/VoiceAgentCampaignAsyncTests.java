// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.voice;

import com.azure.ai.agents.AgentsAsyncClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.AgentsServiceVersion;
import com.azure.ai.agents.BetaVoiceAgentsTelephonyAsyncClient;
import com.azure.ai.agents.models.TelephonyCampaign;
import com.azure.ai.agents.models.TelephonyCampaignStatus;
import com.azure.ai.agents.models.TelephonyCampaignOperationKind;
import com.azure.ai.agents.models.JobStatus;
import com.azure.ai.agents.models.PageOrder;
import com.azure.ai.agents.models.StructuredInputDefinition;
import com.azure.ai.agents.models.VoiceAgentDefinition;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpClient;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.AsyncPollResponse;
import com.azure.core.util.polling.LongRunningOperationStatus;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.function.BiFunction;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Asynchronous campaign record/playback tests, using future schedules and cleanup rather than immediate calls.
 */
public class VoiceAgentCampaignAsyncTests extends VoiceAgentTelephonyTestBase {
    private static final String CAMPAIGN_NAME = "sdk-campaign";

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getFailedCampaignOperation(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        withCampaign(httpClient, serviceVersion, true,
            (client, agentName) -> client.getCampaign(agentName, CAMPAIGN_NAME)
                .flatMap(campaign -> client.getCampaignOperation(agentName, CAMPAIGN_NAME, campaign.getOperationId())
                    .doOnNext(operation -> {
                        assertEquals(campaign.getOperationId(), operation.getId());
                        assertEquals(JobStatus.FAILED, operation.getStatus());
                        assertEquals(TelephonyCampaignOperationKind.VALIDATION, operation.getKind());
                        assertNotNull(operation.getCreatedAt());
                        assertNotNull(operation.getFinishedAt());
                        assertNotNull(operation.getError());
                        assertNotNull(operation.getError().getCode());
                    }))
                .then(Mono
                    .defer(() -> client.getCampaignOperation(agentName, CAMPAIGN_NAME, testResourceNamer.randomUuid())
                        .then(Mono.<Void>error(new AssertionError("An unknown operation must return 404.")))
                        .onErrorResume(ResourceNotFoundException.class, exception -> Mono.empty()))));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void listCampaignValidationErrors(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        withCampaign(httpClient, serviceVersion, true,
            (client, agentName) -> client.getCampaign(agentName, CAMPAIGN_NAME)
                .doOnNext(campaign -> assertEquals(TelephonyCampaignStatus.FAILED, campaign.getStatus()))
                .thenMany(client.listCampaignValidationErrors(agentName, CAMPAIGN_NAME, 1, null, PageOrder.ASC))
                .collectList()
                .doOnNext(errors -> {
                    assertEquals(1, errors.size());
                    assertEquals(1L, errors.get(0).getRowNumber());
                    assertNotNull(errors.get(0).getError());
                    assertNotNull(errors.get(0).getError().getCode());
                })
                .then());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void deleteCampaign(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        withCampaign(httpClient, serviceVersion, (client, agentName) -> client.getCampaign(agentName, CAMPAIGN_NAME)
            .flatMap(running -> client.beginCancelCampaign(agentName, CAMPAIGN_NAME, running.getETag())
                .setPollInterval(pollInterval())
                .last()
                .flatMap(AsyncPollResponse::getFinalResult))
            .flatMap(cancelled -> client.beginDeleteCampaign(agentName, CAMPAIGN_NAME, cancelled.getETag())
                .setPollInterval(pollInterval())
                .last()
                .doOnNext(
                    completed -> assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, completed.getStatus()))
                .flatMap(AsyncPollResponse::getFinalResult)
                .then(Mono.defer(() -> client.getCampaign(agentName, CAMPAIGN_NAME)
                    .then(Mono.<Void>error(new AssertionError("A deleted campaign must return 404.")))
                    .onErrorResume(ResourceNotFoundException.class, exception -> Mono.empty())))
                .then(client.listCampaigns(agentName).count())
                .doOnNext(count -> assertEquals(0L, count.longValue()))
                .then(Mono.defer(() -> client.beginDeleteCampaign(agentName, CAMPAIGN_NAME, cancelled.getETag())
                    .last()
                    .flatMap(AsyncPollResponse::getFinalResult)))));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void cancelCampaign(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        withCampaign(httpClient, serviceVersion,
            (client, agentName) -> client.getCampaign(agentName, CAMPAIGN_NAME)
                .flatMap(running -> client
                    .beginCancelCampaign(agentName, CAMPAIGN_NAME, running.getETag(), testResourceNamer.randomUuid())
                    .setPollInterval(pollInterval())
                    .last())
                .doOnNext(
                    completed -> assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, completed.getStatus()))
                .flatMap(AsyncPollResponse::getFinalResult)
                .doOnNext(cancelled -> {
                    assertEquals(CAMPAIGN_NAME, cancelled.getName());
                    assertEquals(TelephonyCampaignStatus.CANCELLED, cancelled.getStatus());
                })
                .then(client.getCampaign(agentName, CAMPAIGN_NAME))
                .doOnNext(cancelled -> assertEquals(TelephonyCampaignStatus.CANCELLED, cancelled.getStatus()))
                .then());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void listCampaigns(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        withCampaign(httpClient, serviceVersion,
            (client, agentName) -> client
                .listCampaigns(agentName, 1, PageOrder.ASC, null, TelephonyCampaignStatus.RUNNING)
                .collectList()
                .doOnNext(campaigns -> {
                    assertEquals(1, campaigns.size());
                    assertEquals(CAMPAIGN_NAME, campaigns.get(0).getName());
                    assertEquals(TelephonyCampaignStatus.RUNNING, campaigns.get(0).getStatus());
                })
                .then());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getCampaign(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        withCampaign(httpClient, serviceVersion,
            (client, agentName) -> client.getCampaign(agentName, CAMPAIGN_NAME).flatMap(campaign -> {
                assertEquals(CAMPAIGN_NAME, campaign.getName());
                assertEquals(TelephonyCampaignStatus.RUNNING, campaign.getStatus());
                assertNotNull(campaign.getETag());
                return client.getCampaign(agentName, CAMPAIGN_NAME, campaign.getOperationId()).doOnNext(guarded -> {
                    assertEquals(campaign.getOperationId(), guarded.getOperationId());
                    assertEquals(campaign.getLockedAgentVersion(), guarded.getLockedAgentVersion());
                });
            })
                .then(Mono.defer(() -> client.getCampaign(agentName, CAMPAIGN_NAME, testResourceNamer.randomUuid())
                    .then(Mono.<Void>error(new AssertionError("An unknown operation guard must return 404.")))
                    .onErrorResume(ResourceNotFoundException.class, exception -> Mono.empty()))));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void createCampaign(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        AgentsClientBuilder builder = getClientBuilder(httpClient, serviceVersion, true);
        AgentsAsyncClient agents = builder.buildAgentsAsyncClient();
        BetaVoiceAgentsTelephonyAsyncClient client = builder.beta().buildBetaVoiceAgentsTelephonyAsyncClient();
        String agentName = testResourceNamer.randomName("campaign-async-", 32);
        String campaignName = testResourceNamer.randomName("campaign-", 24);
        TelephonyCampaign request = createCampaignRequest();
        agents.createAgentVersion(agentName, createDefinition()).block(TIMEOUT);
        try {
            Mono<TelephonyCampaign> creation = client
                .beginCreateCampaign(agentName, campaignName, request, testResourceNamer.randomUuid(), null)
                .setPollInterval(pollInterval())
                .last()
                .doOnNext(
                    completed -> assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, completed.getStatus()))
                .flatMap(AsyncPollResponse::getFinalResult)
                .timeout(TIMEOUT);
            StepVerifier.create(creation).assertNext(campaign -> {
                assertEquals(campaignName, campaign.getName());
                assertEquals(TelephonyCampaignStatus.RUNNING, campaign.getStatus());
                assertNotNull(campaign.getOperationId());
                assertNotNull(campaign.getETag());
                assertEquals(request.getConnectionName(), campaign.getConnectionName());
                assertEquals(BinaryData.fromObject(request.getSource()).toString(),
                    BinaryData.fromObject(campaign.getSource()).toString());
            }).verifyComplete();
        } finally {
            try {
                cleanupCampaign(client, agentName, campaignName).block(TIMEOUT);
            } finally {
                agents.deleteAgent(agentName).block(TIMEOUT);
            }
        }
    }

    private void withCampaign(HttpClient httpClient, AgentsServiceVersion serviceVersion,
        BiFunction<BetaVoiceAgentsTelephonyAsyncClient, String, Mono<Void>> test) {
        withCampaign(httpClient, serviceVersion, false, test);
    }

    private void withCampaign(HttpClient httpClient, AgentsServiceVersion serviceVersion, boolean missingRequiredInput,
        BiFunction<BetaVoiceAgentsTelephonyAsyncClient, String, Mono<Void>> test) {
        AgentsClientBuilder builder = getClientBuilder(httpClient, serviceVersion, true);
        AgentsAsyncClient agents = builder.buildAgentsAsyncClient();
        BetaVoiceAgentsTelephonyAsyncClient client = builder.beta().buildBetaVoiceAgentsTelephonyAsyncClient();
        String agentName = testResourceNamer.randomName("campaign-async-", 32);
        TelephonyCampaign request = createCampaignRequest();
        VoiceAgentDefinition definition = createDefinition();
        if (missingRequiredInput) {
            definition.setStructuredInputs(
                Collections.singletonMap("customer_name", new StructuredInputDefinition().setRequired(true)));
        }
        agents.createAgentVersion(agentName, definition).block(TIMEOUT);
        try {
            Mono<Void> lifecycle = client.beginCreateCampaign(agentName, CAMPAIGN_NAME, request)
                .setPollInterval(pollInterval())
                .last()
                .flatMap(completed -> {
                    assertEquals(missingRequiredInput
                        ? LongRunningOperationStatus.FAILED
                        : LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, completed.getStatus());
                    return missingRequiredInput
                        ? Mono.empty()
                        : completed.getFinalResult()
                            .doOnNext(campaign -> assertEquals(TelephonyCampaignStatus.RUNNING, campaign.getStatus()));
                })
                .then(Mono.defer(() -> test.apply(client, agentName)))
                .timeout(TIMEOUT);
            StepVerifier.create(lifecycle).verifyComplete();
        } finally {
            try {
                cleanupCampaign(client, agentName, CAMPAIGN_NAME).block(TIMEOUT);
            } finally {
                agents.deleteAgent(agentName).block(TIMEOUT);
            }
        }
    }

    private Mono<Void> cleanupCampaign(BetaVoiceAgentsTelephonyAsyncClient client, String agentName,
        String campaignName) {
        return client.getCampaign(agentName, campaignName)
            .onErrorResume(ResourceNotFoundException.class, exception -> Mono.empty())
            .flatMap(campaign -> TelephonyCampaignStatus.RUNNING.equals(campaign.getStatus())
                || TelephonyCampaignStatus.VALIDATING.equals(campaign.getStatus())
                    ? client.beginCancelCampaign(agentName, campaignName, campaign.getETag())
                        .setPollInterval(pollInterval())
                        .last()
                        .flatMap(AsyncPollResponse::getFinalResult)
                    : Mono.just(campaign))
            .flatMap(campaign -> client.beginDeleteCampaign(agentName, campaignName, campaign.getETag())
                .setPollInterval(pollInterval())
                .last()
                .flatMap(AsyncPollResponse::getFinalResult));
    }
}
