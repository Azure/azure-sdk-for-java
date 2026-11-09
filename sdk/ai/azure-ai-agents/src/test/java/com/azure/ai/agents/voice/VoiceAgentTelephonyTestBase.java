// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.voice;

import com.azure.ai.agents.AgentsServiceVersion;
import com.azure.ai.agents.AgentsClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.VoiceAgentsTelephonyClient;
import com.azure.ai.agents.VoiceAgentsTelephonyAsyncClient;
import com.azure.ai.agents.ClientTestBase;
import com.azure.ai.agents.models.TelephonyCallJob;
import com.azure.ai.agents.models.TelephonyCallJobCreate;
import com.azure.ai.agents.models.TelephonyCallJobSchedule;
import com.azure.ai.agents.models.TelephonyCallJobStatus;
import com.azure.ai.agents.models.TelephonyCampaign;
import com.azure.ai.agents.models.TelephonyCampaignAtTimeSchedule;
import com.azure.ai.agents.models.TelephonyPhoneNumber;
import com.azure.ai.agents.models.TelephonyRecipient;
import com.azure.ai.agents.models.VoiceAgentAudioConfiguration;
import com.azure.ai.agents.models.VoiceAgentAudioOutputConfiguration;
import com.azure.ai.agents.models.VoiceAgentDefinition;
import com.azure.ai.agents.models.VoiceModelType;
import com.azure.ai.agents.models.VoiceOutputModality;
import com.azure.ai.agents.models.VoiceType;
import com.azure.core.test.TestMode;
import com.azure.core.http.HttpClient;
import com.azure.core.test.models.TestProxySanitizer;
import com.azure.core.test.models.TestProxySanitizerType;
import com.azure.core.util.Configuration;
import org.junit.jupiter.params.provider.Arguments;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

abstract class VoiceAgentTelephonyTestBase extends ClientTestBase {
    static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";
    static final Duration TIMEOUT = Duration.ofMinutes(3);

    public static Stream<Arguments> getTestParameters() {
        return getHttpClients().map(client -> Arguments.of(client, AgentsServiceVersion.V1));
    }

    @Override
    protected List<String> getAdditionalTestProxyExcludedHeaders() {
        return Collections.singletonList("If-Match");
    }

    @Override
    protected List<String> getAdditionalTestProxySanitizersToRemove() {
        return Collections.singletonList("AZSDK3423");
    }

    VoiceAgentDefinition createDefinition() {
        String model = getTestMode() == TestMode.PLAYBACK
            ? testResourceNamer.recordValueFromConfig("FOUNDRY_VOICE_MODEL_NAME")
            : testResourceNamer.recordValueFromConfig(
                Configuration.getGlobalConfiguration().get("FOUNDRY_VOICE_MODEL_NAME", "gpt-5.1"));
        return new VoiceAgentDefinition().setModelType(VoiceModelType.MANAGED)
            .setModel(model)
            .setInstructions("Greet the caller briefly, then say goodbye.")
            .setOutputModalities(Collections.singletonList(VoiceOutputModality.AUDIO))
            .setAudio(new VoiceAgentAudioConfiguration()
                .setOutput(new VoiceAgentAudioOutputConfiguration().setVoice("en-US-AvaNeural")
                    .setVoiceType(VoiceType.AZURE_STANDARD)));
    }

    TelephonyCampaign createCampaignRequest() {
        TelephonyCallJobCreate job = createCallJobRequest();
        return new TelephonyCampaign(job.getConnectionName(), job.getSource())
            .setRecipients(Collections.singletonList(new TelephonyRecipient(job.getDestination())))
            .setSchedule(new TelephonyCampaignAtTimeSchedule(job.getSchedule().getNotBefore())
                .setExpiresAt(job.getSchedule().getExpiresAt()));
    }

    private TelephonyCallJobCreate createCallJobRequest() {
        Configuration configuration = Configuration.getGlobalConfiguration();
        String connection = getTestMode() == TestMode.PLAYBACK
            ? "sanitized-connection-1"
            : configuration.get("FOUNDRY_TELEPHONY_CONNECTION_1", "twilio-sdk-testing-1");
        String source
            = getTestMode() == TestMode.PLAYBACK ? "+15555550101" : configuration.get("FOUNDRY_TELEPHONY_NUMBER_1");
        String destination
            = getTestMode() == TestMode.PLAYBACK ? "+15555550102" : configuration.get("FOUNDRY_TELEPHONY_NUMBER_2");
        assertNotNull(source, "FOUNDRY_TELEPHONY_NUMBER_1 must identify a dedicated test caller.");
        assertNotNull(destination, "FOUNDRY_TELEPHONY_NUMBER_2 must identify a consenting test recipient.");
        assertTrue(source.matches("^\\+[1-9]\\d{7,14}$"), "The caller must be an E.164 number.");
        assertTrue(destination.matches("^\\+[1-9]\\d{7,14}$"), "The recipient must be an E.164 number.");
        interceptorManager.addSanitizers(Arrays.asList(
            new TestProxySanitizer("$..connection_name", null, "sanitized-connection-1",
                TestProxySanitizerType.BODY_KEY),
            new TestProxySanitizer("$..source.value", null, "+15555550101", TestProxySanitizerType.BODY_KEY),
            new TestProxySanitizer("$..destination.value", null, "+15555550102", TestProxySanitizerType.BODY_KEY)));
        OffsetDateTime scheduledAt = testResourceNamer.now().plusDays(1);
        return new TelephonyCallJobCreate(new TelephonyPhoneNumber(destination), connection,
            new TelephonyPhoneNumber(source)).setSchedule(
                new TelephonyCallJobSchedule().setNotBefore(scheduledAt).setExpiresAt(scheduledAt.plusMinutes(5)));
    }

    ScheduledCallJobResource createScheduledCallJob(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        AgentsClientBuilder builder = getClientBuilder(httpClient, serviceVersion, true);
        AgentsClient agents = builder.buildAgentsClient();
        VoiceAgentsTelephonyClient client = builder.buildVoiceAgentsTelephonyClient();
        String agentName = testResourceNamer.randomName("call-job-", 32);
        TelephonyCallJobCreate request = createCallJobRequest();
        agents.createAgentVersion(agentName, createDefinition());
        boolean ready = false;
        try {
            TelephonyCallJob job = client.createTelephonyCallJob(agentName, testResourceNamer.randomUuid(), request);
            assertNotNull(job.getId());
            TelephonyCallJob cancelled
                = client.cancelTelephonyCallJob(agentName, job.getId(), Long.toString(job.getRevision()));
            assertEquals(TelephonyCallJobStatus.CANCELLED, cancelled.getStatus());
            assertEquals(0, cancelled.getAttemptCount());
            ScheduledCallJobResource resource = new ScheduledCallJobResource(agents, client,
                builder.buildVoiceAgentsTelephonyAsyncClient(), agentName, job.getId());
            ready = true;
            return resource;
        } finally {
            if (!ready) {
                agents.deleteAgent(agentName);
            }
        }
    }

    static final class ScheduledCallJobResource implements AutoCloseable {
        final VoiceAgentsTelephonyClient client;
        final VoiceAgentsTelephonyAsyncClient asyncClient;
        final String agentName;
        final String jobId;
        private final AgentsClient agents;
        boolean deleted;

        ScheduledCallJobResource(AgentsClient agents, VoiceAgentsTelephonyClient client,
            VoiceAgentsTelephonyAsyncClient asyncClient, String agentName, String jobId) {
            this.agents = agents;
            this.client = client;
            this.asyncClient = asyncClient;
            this.agentName = agentName;
            this.jobId = jobId;
        }

        @Override
        public void close() {
            try {
                if (!deleted) {
                    TelephonyCallJob job = client.getTelephonyCallJob(agentName, jobId);
                    if (!TelephonyCallJobStatus.CANCELLED.equals(job.getStatus())) {
                        client.cancelTelephonyCallJob(agentName, jobId, Long.toString(job.getRevision()));
                    }
                }
            } finally {
                agents.deleteAgent(agentName);
            }
        }
    }

    Duration pollInterval() {
        return getTestMode() == TestMode.PLAYBACK ? Duration.ofMillis(1) : Duration.ofSeconds(2);
    }
}
