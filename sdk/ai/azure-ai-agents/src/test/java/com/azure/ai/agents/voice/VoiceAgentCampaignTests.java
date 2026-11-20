// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.voice;

import com.azure.ai.agents.AgentsClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.AgentsServiceVersion;
import com.azure.ai.agents.BetaVoiceAgentsTelephonyClient;
import com.azure.ai.agents.models.TelephonyCampaign;
import com.azure.ai.agents.models.TelephonyCampaignOperation;
import com.azure.ai.agents.models.TelephonyCampaignOperationKind;
import com.azure.ai.agents.models.JobStatus;
import com.azure.ai.agents.models.TelephonyCampaignStatus;
import com.azure.ai.agents.models.PageOrder;
import com.azure.ai.agents.models.TelephonyCampaignValidationError;
import com.azure.ai.agents.models.StructuredInputDefinition;
import com.azure.ai.agents.models.VoiceAgentDefinition;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpClient;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.function.BiConsumer;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Campaign record/playback tests. Live and record modes schedule calls one day ahead and cancel them during cleanup.
 */
public class VoiceAgentCampaignTests extends VoiceAgentTelephonyTestBase {
    private static final String CAMPAIGN_NAME = "sdk-campaign";

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getFailedCampaignOperation(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        withCampaign(httpClient, serviceVersion, true, (client, agentName) -> {
            String operationId = client.getCampaign(agentName, CAMPAIGN_NAME).getOperationId();
            TelephonyCampaignOperation operation = client.getCampaignOperation(agentName, CAMPAIGN_NAME, operationId);
            assertEquals(operationId, operation.getId());
            assertEquals(JobStatus.FAILED, operation.getStatus());
            assertEquals(TelephonyCampaignOperationKind.VALIDATION, operation.getKind());
            assertNotNull(operation.getCreatedAt());
            assertNotNull(operation.getFinishedAt());
            assertNotNull(operation.getError());
            assertNotNull(operation.getError().getCode());
            assertThrows(ResourceNotFoundException.class,
                () -> client.getCampaignOperation(agentName, CAMPAIGN_NAME, testResourceNamer.randomUuid()));
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void listCampaignValidationErrors(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        withCampaign(httpClient, serviceVersion, true, (client, agentName) -> {
            assertEquals(TelephonyCampaignStatus.FAILED, client.getCampaign(agentName, CAMPAIGN_NAME).getStatus());
            List<TelephonyCampaignValidationError> errors
                = client.listCampaignValidationErrors(agentName, CAMPAIGN_NAME, 1, null, PageOrder.ASC)
                    .stream()
                    .collect(Collectors.toList());
            assertEquals(1, errors.size());
            assertEquals(1L, errors.get(0).getRowNumber());
            assertNotNull(errors.get(0).getError());
            assertNotNull(errors.get(0).getError().getCode());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void deleteCampaign(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        withCampaign(httpClient, serviceVersion, (client, agentName) -> {
            TelephonyCampaign running = client.getCampaign(agentName, CAMPAIGN_NAME);
            TelephonyCampaign cancelled = client.beginCancelCampaign(agentName, CAMPAIGN_NAME, running.getETag())
                .setPollInterval(pollInterval())
                .getFinalResult(TIMEOUT);
            SyncPoller<TelephonyCampaignOperation, Void> deletion
                = client.beginDeleteCampaign(agentName, CAMPAIGN_NAME, cancelled.getETag())
                    .setPollInterval(pollInterval());
            assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                deletion.waitForCompletion(TIMEOUT).getStatus());
            assertNull(deletion.getFinalResult());
            assertThrows(ResourceNotFoundException.class, () -> client.getCampaign(agentName, CAMPAIGN_NAME));
            assertEquals(0, client.listCampaigns(agentName).stream().count());
            assertNull(
                client.beginDeleteCampaign(agentName, CAMPAIGN_NAME, cancelled.getETag()).getFinalResult(TIMEOUT));
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void cancelCampaign(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        withCampaign(httpClient, serviceVersion, (client, agentName) -> {
            TelephonyCampaign running = client.getCampaign(agentName, CAMPAIGN_NAME);
            SyncPoller<TelephonyCampaignOperation, TelephonyCampaign> poller = client
                .beginCancelCampaign(agentName, CAMPAIGN_NAME, running.getETag(), testResourceNamer.randomUuid())
                .setPollInterval(pollInterval());
            assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                poller.waitForCompletion(TIMEOUT).getStatus());
            TelephonyCampaign cancelled = poller.getFinalResult();
            assertNotNull(cancelled);
            assertEquals(CAMPAIGN_NAME, cancelled.getName());
            assertEquals(TelephonyCampaignStatus.CANCELLED, cancelled.getStatus());
            assertEquals(TelephonyCampaignStatus.CANCELLED, client.getCampaign(agentName, CAMPAIGN_NAME).getStatus());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void listCampaigns(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        withCampaign(httpClient, serviceVersion, (client, agentName) -> {
            List<TelephonyCampaign> campaigns
                = client.listCampaigns(agentName, 1, PageOrder.ASC, null, TelephonyCampaignStatus.RUNNING)
                    .stream()
                    .collect(Collectors.toList());
            assertEquals(1, campaigns.size());
            assertEquals(CAMPAIGN_NAME, campaigns.get(0).getName());
            assertEquals(TelephonyCampaignStatus.RUNNING, campaigns.get(0).getStatus());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getCampaign(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        withCampaign(httpClient, serviceVersion, (client, agentName) -> {
            TelephonyCampaign campaign = client.getCampaign(agentName, CAMPAIGN_NAME);
            assertEquals(CAMPAIGN_NAME, campaign.getName());
            assertEquals(TelephonyCampaignStatus.RUNNING, campaign.getStatus());
            assertNotNull(campaign.getETag());
            TelephonyCampaign guarded = client.getCampaign(agentName, CAMPAIGN_NAME, campaign.getOperationId());
            assertEquals(campaign.getOperationId(), guarded.getOperationId());
            assertEquals(campaign.getLockedAgentVersion(), guarded.getLockedAgentVersion());
            assertThrows(ResourceNotFoundException.class,
                () -> client.getCampaign(agentName, CAMPAIGN_NAME, testResourceNamer.randomUuid()));
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void createCampaign(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        AgentsClientBuilder builder = getClientBuilder(httpClient, serviceVersion, true);
        AgentsClient agents = builder.buildAgentsClient();
        BetaVoiceAgentsTelephonyClient client = builder.beta().buildBetaVoiceAgentsTelephonyClient();
        String agentName = testResourceNamer.randomName("campaign-sync-", 32);
        String campaignName = testResourceNamer.randomName("campaign-", 24);
        TelephonyCampaign request = createCampaignRequest();
        agents.createAgentVersion(agentName, createDefinition());
        try {
            SyncPoller<TelephonyCampaignOperation, TelephonyCampaign> poller
                = client.beginCreateCampaign(agentName, campaignName, request, testResourceNamer.randomUuid(), null)
                    .setPollInterval(pollInterval());
            PollResponse<TelephonyCampaignOperation> completed = poller.waitForCompletion(TIMEOUT);
            assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, completed.getStatus());
            TelephonyCampaign campaign = poller.getFinalResult();
            assertNotNull(campaign);
            assertEquals(campaignName, campaign.getName());
            assertEquals(TelephonyCampaignStatus.RUNNING, campaign.getStatus());
            assertNotNull(campaign.getOperationId());
            assertNotNull(campaign.getETag());
            assertEquals(request.getConnectionName(), campaign.getConnectionName());
            assertEquals(BinaryData.fromObject(request.getSource()).toString(),
                BinaryData.fromObject(campaign.getSource()).toString());
        } finally {
            try {
                cleanupCampaign(client, agentName, campaignName);
            } finally {
                agents.deleteAgent(agentName);
            }
        }
    }

    private void withCampaign(HttpClient httpClient, AgentsServiceVersion serviceVersion,
        BiConsumer<BetaVoiceAgentsTelephonyClient, String> test) {
        withCampaign(httpClient, serviceVersion, false, test);
    }

    private void withCampaign(HttpClient httpClient, AgentsServiceVersion serviceVersion, boolean missingRequiredInput,
        BiConsumer<BetaVoiceAgentsTelephonyClient, String> test) {
        AgentsClientBuilder builder = getClientBuilder(httpClient, serviceVersion, true);
        AgentsClient agents = builder.buildAgentsClient();
        BetaVoiceAgentsTelephonyClient client = builder.beta().buildBetaVoiceAgentsTelephonyClient();
        String agentName = testResourceNamer.randomName("campaign-sync-", 32);
        TelephonyCampaign request = createCampaignRequest();
        VoiceAgentDefinition definition = createDefinition();
        if (missingRequiredInput) {
            definition.setStructuredInputs(
                Collections.singletonMap("customer_name", new StructuredInputDefinition().setRequired(true)));
        }
        agents.createAgentVersion(agentName, definition);
        try {
            SyncPoller<TelephonyCampaignOperation, TelephonyCampaign> creation
                = client.beginCreateCampaign(agentName, CAMPAIGN_NAME, request).setPollInterval(pollInterval());
            assertEquals(
                missingRequiredInput
                    ? LongRunningOperationStatus.FAILED
                    : LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                creation.waitForCompletion(TIMEOUT).getStatus());
            if (!missingRequiredInput) {
                assertEquals(TelephonyCampaignStatus.RUNNING, creation.getFinalResult().getStatus());
            }
            test.accept(client, agentName);
        } finally {
            try {
                cleanupCampaign(client, agentName, CAMPAIGN_NAME);
            } finally {
                agents.deleteAgent(agentName);
            }
        }
    }

    private void cleanupCampaign(BetaVoiceAgentsTelephonyClient client, String agentName, String campaignName) {
        TelephonyCampaign campaign;
        try {
            campaign = client.getCampaign(agentName, campaignName);
        } catch (ResourceNotFoundException exception) {
            return;
        }
        if (TelephonyCampaignStatus.RUNNING.equals(campaign.getStatus())
            || TelephonyCampaignStatus.VALIDATING.equals(campaign.getStatus())) {
            campaign = client.beginCancelCampaign(agentName, campaignName, campaign.getETag())
                .setPollInterval(pollInterval())
                .getFinalResult(TIMEOUT);
        }
        client.beginDeleteCampaign(agentName, campaignName, campaign.getETag())
            .setPollInterval(pollInterval())
            .getFinalResult(TIMEOUT);
    }
}
