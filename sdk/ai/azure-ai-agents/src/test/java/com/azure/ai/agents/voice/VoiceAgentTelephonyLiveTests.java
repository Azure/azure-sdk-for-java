// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.voice;

import com.azure.ai.agents.AgentsClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.BetaVoiceAgentsTelephonyClient;
import com.azure.ai.agents.models.CreateAgentVersionInput;
import com.azure.ai.agents.models.CreateTelephonyCallJobInput;
import com.azure.ai.agents.models.CreateTwilioTelephonyBindingInput;
import com.azure.ai.agents.models.PstnTelephonyTransferDestination;
import com.azure.ai.agents.models.TelephonyBinding;
import com.azure.ai.agents.models.TelephonyBindingListItem;
import com.azure.ai.agents.models.TelephonyBindingStatus;
import com.azure.ai.agents.models.TelephonyCallJobSchedule;
import com.azure.ai.agents.models.TelephonyCallRecord;
import com.azure.ai.agents.models.TelephonyCallJob;
import com.azure.ai.agents.models.TelephonyCallSummary;
import com.azure.ai.agents.models.TelephonyOutboundDestination;
import com.azure.ai.agents.models.TelephonyOutboundDestinationType;
import com.azure.ai.agents.models.TelephonyProvider;
import com.azure.ai.agents.models.TelephonyTransferTarget;
import com.azure.ai.agents.models.TelephonyTransferTargets;
import com.azure.ai.agents.models.UpdateTelephonyBindingInput;
import com.azure.ai.agents.models.VoiceAgentAudioConfiguration;
import com.azure.ai.agents.models.VoiceAgentAudioOutputConfiguration;
import com.azure.ai.agents.models.VoiceAgentDefinition;
import com.azure.ai.agents.models.VoiceModelType;
import com.azure.ai.agents.models.VoiceOutputModality;
import com.azure.ai.agents.models.VoiceType;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Live Twilio validation for voice-agent telephony. This test places a real PSTN call and may incur provider charges.
 * It runs only when AZURE_TEST_MODE=LIVE, requires FOUNDRY_VOICE_MODEL_NAME, and uses
 * DefaultAzureCredential authentication. FOUNDRY_PROJECT_ENDPOINT, FOUNDRY_TELEPHONY_CONNECTION_1,
 * FOUNDRY_TELEPHONY_CONNECTION_2, FOUNDRY_TELEPHONY_NUMBER_1, and FOUNDRY_TELEPHONY_NUMBER_2 can override the test
 * project defaults.
 */
@Execution(ExecutionMode.SAME_THREAD)
public class VoiceAgentTelephonyLiveTests {
    private static final String DEFAULT_ENDPOINT
        = "https://voice-first-agents-df-tip.services.ai.azure.com/api/projects/voice-first-agents-df-tip";
    private static final String DEFAULT_CONNECTION_1 = "twilio-sdk-testing-1";
    private static final String DEFAULT_CONNECTION_2 = "twilio-sdk-testing-2";
    private static final String DEFAULT_NUMBER_1 = "+13853864628";
    private static final String DEFAULT_NUMBER_2 = "+18509702029";
    private static final Duration CALL_TIMEOUT = Duration.ofMinutes(2);
    private static final Duration POLL_INTERVAL = Duration.ofSeconds(2);

    @Test
    @EnabledIfEnvironmentVariable(named = "AZURE_TEST_MODE", matches = "LIVE")
    public void bindingLifecycleLive() {
        Configuration configuration = Configuration.getGlobalConfiguration();
        String endpoint = configuration.get("FOUNDRY_PROJECT_ENDPOINT", DEFAULT_ENDPOINT);
        String model = configuration.get("FOUNDRY_VOICE_MODEL_NAME");
        assertNotNull(model, "FOUNDRY_VOICE_MODEL_NAME is required for live telephony testing.");
        String connection = configuration.get("FOUNDRY_TELEPHONY_CONNECTION_1", DEFAULT_CONNECTION_1);
        String number = e164(configuration, "FOUNDRY_TELEPHONY_NUMBER_1", DEFAULT_NUMBER_1);
        AgentsClientBuilder builder = new AgentsClientBuilder().endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder().build())
            .allowPreview(true);
        AgentsClient agents = builder.buildAgentsClient();
        BetaVoiceAgentsTelephonyClient telephony = builder.beta().buildBetaVoiceAgentsTelephonyClient();
        String agentName = "test-telephony-binding-" + shortId();
        boolean agentCreated = false;
        try {
            agents.createAgentVersion(agentName,
                new CreateAgentVersionInput(definition(model, "Greet the caller briefly, then say goodbye.")));
            agentCreated = true;
            TelephonyBinding binding = telephony.createTelephonyBinding(agentName,
                new CreateTwilioTelephonyBindingInput(connection, number).setLabel("Java SDK live test"));
            TelephonyBindingListItem listedBinding = findBinding(telephony, agentName, binding.getId());
            assertNotNull(listedBinding.getETag());

            TelephonyBinding retrieved = telephony.getTelephonyBinding(agentName, binding.getId());
            assertEquals(binding.getId(), retrieved.getId());
            TelephonyBinding updated = telephony.updateTelephonyBinding(agentName, binding.getId(),
                listedBinding.getETag(), new UpdateTelephonyBindingInput().setLabel("Updated Java SDK live test"));
            assertEquals("Updated Java SDK live test", updated.getLabel());

            String updatedEtag = findBinding(telephony, agentName, binding.getId()).getETag();
            assertNotNull(updatedEtag);
            telephony.deleteTelephonyBinding(agentName, binding.getId(), updatedEtag);
            assertTrue(telephony.listTelephonyBindings(agentName)
                .stream()
                .noneMatch(item -> binding.getId().equals(item.getId())));
        } finally {
            if (agentCreated) {
                safeCleanup("delete binding test agent", () -> agents.deleteAgent(agentName));
            }
        }
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "AZURE_TEST_MODE", matches = "LIVE")
    public void twilioBindingAndOutboundCallLive() throws InterruptedException {
        Configuration configuration = Configuration.getGlobalConfiguration();
        String endpoint = configuration.get("FOUNDRY_PROJECT_ENDPOINT", DEFAULT_ENDPOINT);
        String model = configuration.get("FOUNDRY_VOICE_MODEL_NAME");
        assertNotNull(model, "FOUNDRY_VOICE_MODEL_NAME is required for live telephony testing.");
        String connection1 = configuration.get("FOUNDRY_TELEPHONY_CONNECTION_1", DEFAULT_CONNECTION_1);
        String connection2 = configuration.get("FOUNDRY_TELEPHONY_CONNECTION_2", DEFAULT_CONNECTION_2);
        String number1 = e164(configuration, "FOUNDRY_TELEPHONY_NUMBER_1", DEFAULT_NUMBER_1);
        String number2 = e164(configuration, "FOUNDRY_TELEPHONY_NUMBER_2", DEFAULT_NUMBER_2);

        AgentsClientBuilder builder = new AgentsClientBuilder().endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder().build())
            .allowPreview(true);
        AgentsClient agents = builder.buildAgentsClient();
        BetaVoiceAgentsTelephonyClient telephony = builder.beta().buildBetaVoiceAgentsTelephonyClient();
        String suffix = UUID.randomUUID().toString();
        String inboundAgent = "test-telephony-inbound-" + suffix;
        String outboundAgent = "test-telephony-outbound-" + suffix;
        String callJobId = null;
        String scheduledCallJobId = null;
        String inboundCallId = null;
        boolean inboundAgentCreated = false;
        boolean outboundAgentCreated = false;
        try {
            agents.createAgentVersion(inboundAgent,
                new CreateAgentVersionInput(definition(model, "Greet the caller briefly, then say goodbye.")));
            inboundAgentCreated = true;
            agents.createAgentVersion(outboundAgent,
                new CreateAgentVersionInput(definition(model, "Say hello, wait for one reply, then say goodbye.")));
            outboundAgentCreated = true;

            TelephonyBinding binding = telephony.createTelephonyBinding(inboundAgent,
                new CreateTwilioTelephonyBindingInput(connection1, number1).setLabel("Java SDK live test"));
            assertNotNull(binding.getId());
            assertEquals(TelephonyProvider.TWILIO, binding.getProvider());
            assertEquals(TelephonyBindingStatus.ACTIVE, binding.getStatus());
            assertNotNull(binding.getIncomingCallUrl());

            Response<BinaryData> initialTargetsResponse
                = telephony.getTelephonyTransferTargetsWithResponse(inboundAgent, new RequestOptions());
            TelephonyTransferTargets initialTargets
                = initialTargetsResponse.getValue().toObject(TelephonyTransferTargets.class);
            assertTrue(initialTargets.getTransferTargets().isEmpty());
            TelephonyTransferTarget transferTarget = new TelephonyTransferTarget("test_number_2",
                "Java SDK live test target", new PstnTelephonyTransferDestination(number2));
            TelephonyTransferTargets replacedTargets = telephony.replaceTelephonyTransferTargets(inboundAgent,
                requireEtag(initialTargetsResponse, "telephony transfer targets"),
                Collections.singletonList(transferTarget));
            assertEquals(1, replacedTargets.getTransferTargets().size());

            CreateTelephonyCallJobInput request = new CreateTelephonyCallJobInput(
                new TelephonyOutboundDestination(TelephonyOutboundDestinationType.PHONE_NUMBER, number1), connection2,
                number2).setPurpose("Java SDK live telephony validation");
            TelephonyCallJob job
                = telephony.createTelephonyCallJob(outboundAgent, UUID.randomUUID().toString(), request);
            callJobId = job.getId();
            assertNotNull(callJobId);
            assertEquals(outboundAgent, job.getAgentName());
            assertEquals(connection2, job.getConnectionName());
            assertEquals(number2, job.getSource());

            TelephonyCallSummary inboundCall = waitForInboundCall(telephony, inboundAgent);
            inboundCallId = inboundCall.getId();
            assertNotNull(inboundCallId);
            assertEquals(TelephonyProvider.TWILIO, inboundCall.getProvider());
            assertEquals(number2, inboundCall.getCallerNumber());
            assertEquals(number1, inboundCall.getProviderNumber());

            TelephonyCallRecord callRecord = telephony.getTelephonyCall(inboundAgent, inboundCallId);
            assertEquals(inboundCallId, callRecord.getId());
            TelephonyCallRecord transferredCall
                = telephony.transferTelephonyCall(inboundAgent, inboundCallId, "test_number_2");
            assertEquals(inboundCallId, transferredCall.getId());
            inboundCallId = null;

            TelephonyCallJob dispatchedJob = telephony.getTelephonyCallJob(outboundAgent, callJobId);
            assertTrue(dispatchedJob.getAttemptCount() > 0, "The outbound call job did not create an attempt.");

            OffsetDateTime notBefore = OffsetDateTime.now().plusMinutes(10);
            CreateTelephonyCallJobInput scheduledRequest = new CreateTelephonyCallJobInput(
                new TelephonyOutboundDestination(TelephonyOutboundDestinationType.PHONE_NUMBER, number1), connection2,
                number2).setPurpose("Java SDK live cancellation validation")
                    .setSchedule(
                        new TelephonyCallJobSchedule().setNotBefore(notBefore).setExpiresAt(notBefore.plusMinutes(10)));
            TelephonyCallJob scheduledJob
                = telephony.createTelephonyCallJob(outboundAgent, UUID.randomUUID().toString(), scheduledRequest);
            scheduledCallJobId = scheduledJob.getId();
            TelephonyCallJob cancelledJob = telephony.cancelTelephonyCallJob(outboundAgent, scheduledCallJobId,
                Long.toString(scheduledJob.getRevision()));
            assertNotNull(cancelledJob.getCancellation());
            scheduledCallJobId = null;

            telephony.replaceTelephonyTransferTargets(inboundAgent, getTransferTargetsEtag(telephony, inboundAgent),
                Collections.emptyList());
        } finally {
            if (inboundCallId != null) {
                String callId = inboundCallId;
                safeCleanup("end inbound call", () -> telephony.endTelephonyCall(inboundAgent, callId));
            }
            if (callJobId != null) {
                String jobId = callJobId;
                safeCleanup("cancel outbound call job", () -> {
                    TelephonyCallJob currentJob = telephony.getTelephonyCallJob(outboundAgent, jobId);
                    telephony.cancelTelephonyCallJob(outboundAgent, jobId, Long.toString(currentJob.getRevision()));
                });
            }
            if (scheduledCallJobId != null) {
                String jobId = scheduledCallJobId;
                safeCleanup("cancel scheduled outbound call job", () -> {
                    TelephonyCallJob scheduledJob = telephony.getTelephonyCallJob(outboundAgent, jobId);
                    telephony.cancelTelephonyCallJob(outboundAgent, jobId, Long.toString(scheduledJob.getRevision()));
                });
            }
            if (inboundAgentCreated) {
                safeCleanup("clear telephony transfer targets",
                    () -> telephony.replaceTelephonyTransferTargets(inboundAgent,
                        getTransferTargetsEtag(telephony, inboundAgent), Collections.emptyList()));
            }
            if (outboundAgentCreated) {
                safeCleanup("delete outbound agent", () -> agents.deleteAgent(outboundAgent));
            }
            if (inboundAgentCreated) {
                safeCleanup("delete inbound agent", () -> agents.deleteAgent(inboundAgent));
            }
        }
    }

    private static TelephonyCallSummary waitForInboundCall(BetaVoiceAgentsTelephonyClient telephony, String agentName)
        throws InterruptedException {
        long deadline = System.nanoTime() + CALL_TIMEOUT.toNanos();
        while (System.nanoTime() < deadline) {
            for (TelephonyCallSummary call : telephony.listTelephonyCalls(agentName)) {
                return call;
            }
            Thread.sleep(POLL_INTERVAL.toMillis());
        }
        throw new AssertionError("No inbound Twilio call arrived within " + CALL_TIMEOUT + ".");
    }

    private static VoiceAgentDefinition definition(String model, String instructions) {
        return new VoiceAgentDefinition().setModelType(VoiceModelType.MANAGED)
            .setModel(model)
            .setInstructions(instructions)
            .setOutputModalities(Collections.singletonList(VoiceOutputModality.AUDIO))
            .setAudio(new VoiceAgentAudioConfiguration()
                .setOutput(new VoiceAgentAudioOutputConfiguration().setVoice("en-US-AvaNeural")
                    .setVoiceType(VoiceType.AZURE_STANDARD)));
    }

    private static String e164(Configuration configuration, String name, String defaultValue) {
        String value = configuration.get(name, defaultValue);
        assertNotNull(value, name + " is required for live telephony testing.");
        value = value.trim();
        assertTrue(value.matches("^\\+[1-9]\\d{7,14}$"), name + " must be an E.164 number such as +14255550123.");
        return value;
    }

    private static String shortId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    private static TelephonyBindingListItem findBinding(BetaVoiceAgentsTelephonyClient telephony, String agentName,
        String bindingId) {
        return telephony.listTelephonyBindings(agentName)
            .stream()
            .filter(item -> bindingId.equals(item.getId()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Created binding was not listed."));
    }

    private static String getTransferTargetsEtag(BetaVoiceAgentsTelephonyClient telephony, String agentName) {
        return requireEtag(telephony.getTelephonyTransferTargetsWithResponse(agentName, new RequestOptions()),
            "telephony transfer targets");
    }

    private static String requireEtag(Response<?> response, String resource) {
        String etag = response.getHeaders().getValue(HttpHeaderName.ETAG);
        assertNotNull(etag, "The service did not return an ETag for " + resource + ".");
        return etag;
    }

    private static void safeCleanup(String action, Runnable cleanup) {
        try {
            cleanup.run();
        } catch (RuntimeException exception) {
            System.err.printf("Failed to %s: %s%n", action, exception.getMessage());
        }
    }
}
