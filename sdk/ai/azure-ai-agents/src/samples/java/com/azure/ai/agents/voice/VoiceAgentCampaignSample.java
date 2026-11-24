// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.voice;

import com.azure.ai.agents.AgentsClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.BetaVoiceAgentsTelephonyClient;
import com.azure.ai.agents.models.CreateAgentVersionInput;
import com.azure.ai.agents.models.TelephonyCampaign;
import com.azure.ai.agents.models.TelephonyCampaignAtTimeSchedule;
import com.azure.ai.agents.models.TelephonyCampaignValidationError;
import com.azure.ai.agents.models.TelephonyCampaignOperation;
import com.azure.ai.agents.models.TelephonyPhoneNumber;
import com.azure.ai.agents.models.TelephonyRecipient;
import com.azure.ai.agents.models.VoiceModelType;
import com.azure.core.credential.TokenCredential;
import com.azure.core.util.Configuration;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import com.azure.identity.DefaultAzureCredentialBuilder;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.UUID;

/**
 * Creates, inspects, cancels, and deletes a voice-agent campaign synchronously.
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
 * <p>This sample authorizes billable outbound calls, schedules them one day ahead, then cancels and deletes only its
 * newly created campaign. Use a dedicated test connection and consenting recipient. If interrupted or an operation
 * fails, the campaign may remain scheduled: use the printed name to cancel it before dispatch. Preparation completion
 * is not call completion. Deletion removes campaign data, not call history. Validation failures are reported before
 * the preparation failure is propagated; a failed campaign is retained for diagnosis. Any temporary voice agent is
 * deleted after the campaign workflow, including on failure.</p>
 */
public class VoiceAgentCampaignSample {
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
        TokenCredential credential = new DefaultAzureCredentialBuilder().build();
        AgentsClient agentsClient = new AgentsClientBuilder()
            .endpoint(endpoint)
            .credential(credential)
            .allowPreview(true)
            .buildAgentsClient();
        BetaVoiceAgentsTelephonyClient client = new AgentsClientBuilder()
            .endpoint(endpoint)
            .credential(credential)
            .beta()
            .buildBetaVoiceAgentsTelephonyClient();
        String campaignName = "java-campaign-" + UUID.randomUUID();
        OffsetDateTime scheduledAt = OffsetDateTime.now().plusDays(1);
        TelephonyCampaign request = new TelephonyCampaign(connectionName, new TelephonyPhoneNumber(caller))
            .setRecipients(Collections.singletonList(new TelephonyRecipient(new TelephonyPhoneNumber(recipient))))
            .setSchedule(new TelephonyCampaignAtTimeSchedule(scheduledAt).setExpiresAt(scheduledAt.plusMinutes(5)));
        boolean temporaryAgentCreated = false;
        try {
            if (createAgent) {
                agentsClient.createAgentVersion(campaignAgentName,
                    new CreateAgentVersionInput(VoiceAgentRealtimeSampleUtils.createDefinition(modelType, model,
                        "You are a friendly voice assistant. Keep replies short and natural.")));
                temporaryAgentCreated = true;
                System.out.println("Created temporary voice agent: " + campaignAgentName);
            }
            runCampaign(client, campaignAgentName, campaignName, request, UUID.randomUUID().toString());
        } finally {
            if (temporaryAgentCreated) {
                try {
                    agentsClient.deleteAgent(campaignAgentName);
                    System.out.println("Deleted temporary voice agent: " + campaignAgentName);
                } catch (RuntimeException error) {
                    System.err.printf("Failed to delete temporary voice agent %s: %s%n",
                        campaignAgentName, error.getMessage());
                }
            }
        }
    }

    static void runCampaign(BetaVoiceAgentsTelephonyClient client, String agentName, String campaignName,
        TelephonyCampaign request, String operationId) {
        Duration timeout = Duration.ofMinutes(5);
        System.out.println("Creating scheduled campaign: " + campaignName + "; operation: " + operationId);
        SyncPoller<TelephonyCampaignOperation, TelephonyCampaign> creation
            = client.beginCreateCampaign(agentName, campaignName, request, operationId, null);
        Instant deadline = Instant.now().plus(timeout);
        PollResponse<TelephonyCampaignOperation> response = creation.poll();
        while (!response.getStatus().isComplete()) {
            System.out.printf("Campaign operation status: %s%n", response.getStatus());
            if (!Instant.now().isBefore(deadline)) {
                throw new IllegalStateException("Timed out waiting for campaign preparation: " + operationId);
            }
            sleep(Duration.ofSeconds(10));
            response = creation.poll();
        }
        System.out.printf("Campaign operation status: %s%n", response.getStatus());

        TelephonyCampaignOperation monitor = client.getCampaignOperation(agentName, campaignName, operationId);
        System.out.printf("Preparation %s: kind=%s, status=%s%n",
            monitor.getId(), monitor.getKind(), monitor.getStatus());
        for (TelephonyCampaignValidationError error : client.listCampaignValidationErrors(agentName, campaignName)) {
            System.out.printf("Recipient row %d: %s%n", error.getRowNumber(), error.getError().getCode());
        }
        creation.getFinalResult(timeout);

        TelephonyCampaign campaign = client.getCampaign(agentName, campaignName, operationId);
        System.out.printf("Campaign %s: status=%s, pinned agent version=%s%n",
            campaign.getName(), campaign.getStatus(), campaign.getLockedAgentVersion());
        for (TelephonyCampaign item : client.listCampaigns(agentName)) {
            System.out.printf("Campaign %s: status=%s%n", item.getName(), item.getStatus());
        }

        TelephonyCampaign cancelled = client.beginCancelCampaign(agentName, campaignName, campaign.getETag())
            .getFinalResult(timeout);
        System.out.printf("Campaign %s: status=%s%n", cancelled.getName(), cancelled.getStatus());
        client.beginDeleteCampaign(agentName, campaignName, cancelled.getETag()).getFinalResult(timeout);
        System.out.println("Deleted campaign: " + campaignName);
    }

    private static void sleep(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for campaign preparation.", exception);
        }
    }
}
