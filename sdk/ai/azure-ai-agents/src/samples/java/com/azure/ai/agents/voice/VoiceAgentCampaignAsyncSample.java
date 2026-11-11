// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.voice;

import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.AgentsAsyncClient;
import com.azure.ai.agents.BetaVoiceAgentsTelephonyAsyncClient;
import com.azure.ai.agents.models.CreateAgentVersionInput;
import com.azure.ai.agents.models.TelephonyCampaign;
import com.azure.ai.agents.models.TelephonyCampaignAtTimeSchedule;
import com.azure.ai.agents.models.TelephonyCampaignOperation;
import com.azure.ai.agents.models.TelephonyPhoneNumber;
import com.azure.ai.agents.models.TelephonyRecipient;
import com.azure.ai.agents.models.VoiceModelType;
import com.azure.core.util.Configuration;
import com.azure.core.util.polling.AsyncPollResponse;
import com.azure.core.util.polling.PollerFlux;
import com.azure.identity.DefaultAzureCredentialBuilder;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.UUID;
import reactor.core.publisher.Mono;

/**
 * Creates, inspects, cancels, and deletes a campaign asynchronously.
 *
 * <p>Configure these environment variables before running:</p>
 * <ul>
 *   <li>{@code FOUNDRY_PROJECT_ENDPOINT} - Required. The Foundry project endpoint.</li>
 *   <li>{@code FOUNDRY_TELEPHONY_CONNECTION_1} - Required. An existing telephony provider connection.</li>
 *   <li>{@code FOUNDRY_TELEPHONY_NUMBER_1} - Required. The caller phone number.</li>
 *   <li>{@code FOUNDRY_TELEPHONY_NUMBER_2} - Required. A consenting recipient phone number.</li>
 *   <li>{@code FOUNDRY_VOICE_AGENT_NAME} - Optional. An existing voice agent. If omitted, the sample creates and
 *       deletes a temporary agent.</li>
 *   <li>{@code FOUNDRY_VOICE_MODEL} - Optional. Model for the temporary agent; defaults to {@code gpt-realtime}.</li>
 *   <li>{@code FOUNDRY_VOICE_MODEL_TYPE} - Optional. Model type for the temporary agent; defaults to
 *       {@code managed}.</li>
 * </ul>
 *
 * <p>This sample schedules calls one day ahead, then cancels and deletes only its newly created campaign. It authorizes
 * billable outbound calls; use a dedicated test connection and consenting recipient. If it fails or is interrupted,
 * cancel the printed campaign before dispatch. A failed preparation is retained for diagnosis. See
 * {@link VoiceAgentCampaignSample} for additional campaign lifecycle details.</p>
 */
public class VoiceAgentCampaignAsyncSample {
    public static void main(String[] args) {
        Configuration configuration = Configuration.getGlobalConfiguration();
        String endpoint = configuration.get("FOUNDRY_PROJECT_ENDPOINT");
        String agentName = configuration.get("FOUNDRY_VOICE_AGENT_NAME");
        boolean createAgent = agentName == null || agentName.trim().isEmpty();
        String campaignAgentName = createAgent ? "java-campaign-agent-" + UUID.randomUUID() : agentName;
        String model = configuration.get("FOUNDRY_VOICE_MODEL", "gpt-realtime");
        VoiceModelType modelType = VoiceModelType.fromString(configuration.get(
            "FOUNDRY_VOICE_MODEL_TYPE", VoiceModelType.MANAGED.toString()));
        String connectionName = configuration.get("FOUNDRY_TELEPHONY_CONNECTION_1");
        String caller = configuration.get("FOUNDRY_TELEPHONY_NUMBER_1");
        String recipient = configuration.get("FOUNDRY_TELEPHONY_NUMBER_2");
        if (endpoint == null || endpoint.trim().isEmpty()
            || connectionName == null || connectionName.trim().isEmpty()
            || caller == null || caller.trim().isEmpty()
            || recipient == null || recipient.trim().isEmpty()) {
            throw new IllegalArgumentException("Set FOUNDRY_PROJECT_ENDPOINT, FOUNDRY_TELEPHONY_CONNECTION_1, "
                + "FOUNDRY_TELEPHONY_NUMBER_1, and FOUNDRY_TELEPHONY_NUMBER_2.");
        }
        DefaultAzureCredentialBuilder credentialBuilder = new DefaultAzureCredentialBuilder();
        AgentsAsyncClient agentsClient = new AgentsClientBuilder()
            .endpoint(endpoint)
            .credential(credentialBuilder.build())
            .allowPreview(true)
            .buildAgentsAsyncClient();
        BetaVoiceAgentsTelephonyAsyncClient client = new AgentsClientBuilder()
            .endpoint(endpoint)
            .credential(credentialBuilder.build())
            .beta()
            .buildBetaVoiceAgentsTelephonyAsyncClient();
        String campaignName = "java-campaign-async-" + UUID.randomUUID();
        OffsetDateTime scheduledAt = OffsetDateTime.now().plusDays(1);
        TelephonyCampaign request = new TelephonyCampaign(connectionName, new TelephonyPhoneNumber(caller))
            .setRecipients(Collections.singletonList(new TelephonyRecipient(new TelephonyPhoneNumber(recipient))))
            .setSchedule(new TelephonyCampaignAtTimeSchedule(scheduledAt).setExpiresAt(scheduledAt.plusMinutes(5)));
        Mono<String> agentReady = createAgent
            ? agentsClient.createAgentVersion(campaignAgentName,
                new CreateAgentVersionInput(VoiceAgentRealtimeSampleUtils.createDefinition(modelType, model,
                    "You are a friendly voice assistant. Keep replies short and natural.")))
                .doOnNext(created -> System.out.printf("Created temporary voice agent: %s%n", created.getName()))
                .thenReturn(campaignAgentName)
            : Mono.just(campaignAgentName);
        Mono.usingWhen(agentReady,
            name -> runCampaign(client, name, campaignName, request, UUID.randomUUID().toString())
                .timeout(Duration.ofMinutes(5)),
            name -> deleteTemporaryAgent(agentsClient, name, createAgent),
            (name, error) -> deleteTemporaryAgentAfterFailure(agentsClient, name, createAgent),
            name -> deleteTemporaryAgentAfterFailure(agentsClient, name, createAgent))
            .block();
    }

    private static Mono<Void> deleteTemporaryAgent(AgentsAsyncClient agentsClient, String agentName,
        boolean createdBySample) {
        if (!createdBySample) {
            return Mono.empty();
        }
        return agentsClient.deleteAgent(agentName)
            .timeout(Duration.ofSeconds(30))
            .doOnSuccess(ignored -> System.out.println("Deleted temporary voice agent: " + agentName));
    }

    private static Mono<Void> deleteTemporaryAgentAfterFailure(AgentsAsyncClient agentsClient, String agentName,
        boolean createdBySample) {
        return deleteTemporaryAgent(agentsClient, agentName, createdBySample)
            .onErrorResume(error -> {
                System.err.printf("Failed to delete temporary voice agent %s: %s%n", agentName, error.getMessage());
                return Mono.empty();
            });
    }

    static Mono<Void> runCampaign(BetaVoiceAgentsTelephonyAsyncClient client, String agentName, String campaignName,
        TelephonyCampaign request, String operationId) {
        return Mono.defer(() -> {
            System.out.println("Creating scheduled campaign: " + campaignName + "; operation: " + operationId);
            PollerFlux<TelephonyCampaignOperation, TelephonyCampaign> creation
                = client.beginCreateCampaign(agentName, campaignName, request, operationId, null);
            return creation
                .doOnNext(response -> System.out.printf("Campaign operation status: %s%n", response.getStatus()))
                .last()
                .flatMap(completed -> client.getCampaignOperation(agentName, campaignName, operationId)
                    .doOnNext(monitor -> System.out.printf("Preparation %s: kind=%s, status=%s%n",
                        monitor.getId(), monitor.getKind(), monitor.getStatus()))
                    .thenMany(client.listCampaignValidationErrors(agentName, campaignName))
                    .doOnNext(error -> System.out.printf("Recipient row %d: %s%n",
                        error.getRowNumber(), error.getError().getCode()))
                    .then(Mono.defer(completed::getFinalResult)))
                .then(Mono.defer(() -> client.getCampaign(agentName, campaignName, operationId)))
                .doOnNext(campaign -> System.out.printf("Campaign %s: status=%s, pinned agent version=%s%n",
                    campaign.getName(), campaign.getStatus(), campaign.getLockedAgentVersion()))
                .flatMap(campaign -> client.listCampaigns(agentName)
                    .doOnNext(item -> System.out.printf("Campaign %s: status=%s%n", item.getName(), item.getStatus()))
                    .then(Mono.defer(() -> client.beginCancelCampaign(agentName, campaignName, campaign.getETag())
                        .last()
                        .flatMap(AsyncPollResponse::getFinalResult))))
                .doOnNext(cancelled -> System.out.printf("Campaign %s: status=%s%n",
                    cancelled.getName(), cancelled.getStatus()))
                .flatMap(cancelled -> client.beginDeleteCampaign(agentName, campaignName, cancelled.getETag())
                    .last()
                    .flatMap(AsyncPollResponse::getFinalResult))
                .doOnSuccess(ignored -> System.out.println("Deleted campaign: " + campaignName));
        });
    }
}
