// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.voice;

import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.BetaVoiceAgentsTelephonyClient;
import com.azure.ai.agents.models.TelephonyCampaign;
import com.azure.ai.agents.models.TelephonyCampaignAtTimeSchedule;
import com.azure.ai.agents.models.TelephonyCampaignValidationError;
import com.azure.ai.agents.models.TelephonyCampaignOperation;
import com.azure.ai.agents.models.TelephonyPhoneNumber;
import com.azure.ai.agents.models.TelephonyRecipient;
import com.azure.core.util.Configuration;
import com.azure.core.util.polling.SyncPoller;
import com.azure.identity.DefaultAzureCredentialBuilder;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.UUID;

/**
 * Creates, inspects, cancels, and deletes a voice-agent campaign synchronously.
 *
 * <p>Set {@code FOUNDRY_PROJECT_ENDPOINT}, {@code FOUNDRY_VOICE_AGENT_NAME},
 * {@code FOUNDRY_TELEPHONY_CONNECTION_1}, {@code FOUNDRY_TELEPHONY_NUMBER_1} (caller), and
 * {@code FOUNDRY_TELEPHONY_NUMBER_2} (consenting recipient). The agent and provider connection must already exist.</p>
 *
 * <p>This sample authorizes billable outbound calls, schedules them one day ahead, then cancels and deletes only its
 * newly created campaign. Use a dedicated test connection and consenting recipient. If interrupted or an operation
 * fails, the campaign may remain scheduled: use the printed name to cancel it before dispatch. Preparation completion
 * is not call completion. Deletion removes campaign data, not call history. Validation failures are reported before
 * the preparation failure is propagated; a failed campaign is retained for diagnosis.</p>
 */
public class VoiceAgentCampaignSample {
    public static void main(String[] args) {
        Configuration configuration = Configuration.getGlobalConfiguration();
        String endpoint = configuration.get("FOUNDRY_PROJECT_ENDPOINT");
        String agentName = configuration.get("FOUNDRY_VOICE_AGENT_NAME");
        String connectionName = configuration.get("FOUNDRY_TELEPHONY_CONNECTION_1");
        String caller = configuration.get("FOUNDRY_TELEPHONY_NUMBER_1");
        String recipient = configuration.get("FOUNDRY_TELEPHONY_NUMBER_2");
        if (endpoint == null || endpoint.trim().isEmpty()
            || agentName == null || agentName.trim().isEmpty()
            || connectionName == null || connectionName.trim().isEmpty()
            || caller == null || caller.trim().isEmpty()
            || recipient == null || recipient.trim().isEmpty()) {
            throw new IllegalArgumentException("Set FOUNDRY_PROJECT_ENDPOINT, FOUNDRY_VOICE_AGENT_NAME, "
                + "FOUNDRY_TELEPHONY_CONNECTION_1, FOUNDRY_TELEPHONY_NUMBER_1, and FOUNDRY_TELEPHONY_NUMBER_2.");
        }
        BetaVoiceAgentsTelephonyClient client = new AgentsClientBuilder()
            .endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder().build())
            .beta()
            .buildBetaVoiceAgentsTelephonyClient();
        String campaignName = "java-campaign-" + UUID.randomUUID();
        OffsetDateTime scheduledAt = OffsetDateTime.now().plusDays(1);
        TelephonyCampaign request = new TelephonyCampaign(connectionName, new TelephonyPhoneNumber(caller))
            .setRecipients(Collections.singletonList(new TelephonyRecipient(new TelephonyPhoneNumber(recipient))))
            .setSchedule(new TelephonyCampaignAtTimeSchedule(scheduledAt).setExpiresAt(scheduledAt.plusMinutes(5)));
        runCampaign(client, agentName, campaignName, request, UUID.randomUUID().toString());
    }

    static void runCampaign(BetaVoiceAgentsTelephonyClient client, String agentName, String campaignName,
        TelephonyCampaign request, String operationId) {
        Duration timeout = Duration.ofMinutes(5);
        System.out.println("Creating scheduled campaign: " + campaignName + "; operation: " + operationId);
        SyncPoller<TelephonyCampaignOperation, TelephonyCampaign> creation
            = client.beginCreateCampaign(agentName, campaignName, request, operationId, null);
        creation.waitForCompletion(timeout);

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
}
