// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.models.AgentDetails;
import com.azure.ai.agents.models.AgentKind;
import com.azure.ai.agents.models.AgentState;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.CreateAgentVersionInput;
import com.azure.ai.agents.models.VoiceAgentAudioConfig;
import com.azure.ai.agents.models.VoiceAgentAudioOutputConfig;
import com.azure.ai.agents.models.VoiceAgentDefinition;
import com.azure.ai.agents.models.VoiceModelType;
import com.azure.ai.agents.models.VoiceOutputModality;
import com.azure.ai.agents.models.VoiceType;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.test.utils.MockTokenCredential;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VoiceAgentsMockTests {
    private static final String ENDPOINT = "https://localhost:8080/api/projects/project";
    private static final String AGENT_NAME = "voice-agent-test";
    private static final HttpHeaderName FOUNDRY_FEATURES = HttpHeaderName.fromString("Foundry-Features");

    @Test
    public void syncVoiceAgentCrudUsesExpectedWireShapeAndPaths() {
        DeterministicHttpClient httpClient
            = new DeterministicHttpClient().enqueueJson(200, versionJson("1", "Initial instructions"))
                .enqueueJson(200, agentJson("disabled", "1"))
                .enqueueJson(204, "")
                .enqueueJson(200, agentJson("enabled", "1"))
                .enqueueJson(200, "");
        AgentsClient client = createBuilder(httpClient).allowPreview(true).buildAgentsClient();

        AgentVersionDetails created = client.createAgentVersion(AGENT_NAME,
            new CreateAgentVersionInput(createVoiceDefinition("Initial instructions")));
        assertEquals(AGENT_NAME, created.getName());
        assertEquals("1", created.getVersion());
        assertInstanceOf(VoiceAgentDefinition.class, created.getDefinition());

        HttpRequest createRequest = httpClient.getRequest(0);
        assertEquals(HttpMethod.POST, createRequest.getHttpMethod());
        assertTrue(createRequest.getUrl().getPath().endsWith("/agents/" + AGENT_NAME + "/versions"));
        String requestBody = createRequest.getBodyAsBinaryData().toString();
        assertTrue(requestBody.contains("\"kind\":\"voice\""));
        assertTrue(requestBody.contains("\"model_type\":\"managed\""));
        assertTrue(requestBody.contains("\"voice\":\"en-US-AvaNeural\""));
        assertTrue(requestBody.contains("\"output_modalities\":[\"audio\"]"));
        assertTrue(createRequest.getHeaders().getValue(FOUNDRY_FEATURES).contains("VoiceAgents=V1Preview"));

        AgentDetails disabled = client.getAgent(AGENT_NAME);
        assertEquals(AgentState.DISABLED, disabled.getState());
        client.enableAgent(AGENT_NAME);
        AgentDetails enabled = client.getAgent(AGENT_NAME);
        assertEquals(AgentState.ENABLED, enabled.getState());
        client.deleteAgent(AGENT_NAME);

        assertTrue(httpClient.getRequest(1).getUrl().getPath().endsWith("/agents/" + AGENT_NAME));
        assertTrue(httpClient.getRequest(2).getUrl().getPath().endsWith("/agents/" + AGENT_NAME + ":enable"));
        assertTrue(httpClient.getRequest(3).getUrl().getPath().endsWith("/agents/" + AGENT_NAME));
        assertEquals(HttpMethod.DELETE, httpClient.getRequest(4).getHttpMethod());
    }

    @Test
    public void asyncVoiceAgentCrudUsesExpectedWireShapeAndPaths() {
        DeterministicHttpClient httpClient
            = new DeterministicHttpClient().enqueueJson(200, versionJson("2", "Async instructions"))
                .enqueueJson(200, agentJson("enabled", "2"))
                .enqueueJson(204, "")
                .enqueueJson(200, agentJson("disabled", "2"))
                .enqueueJson(200, "");
        AgentsAsyncClient client = createBuilder(httpClient).allowPreview(true).buildAgentsAsyncClient();

        StepVerifier.create(client
            .createAgentVersion(AGENT_NAME, new CreateAgentVersionInput(createVoiceDefinition("Async instructions")))
            .doOnNext(created -> {
                assertEquals("2", created.getVersion());
                assertInstanceOf(VoiceAgentDefinition.class, created.getDefinition());
            })
            .then(client.getAgent(AGENT_NAME))
            .doOnNext(agent -> assertEquals(AgentState.ENABLED, agent.getState()))
            .then(client.disableAgent(AGENT_NAME))
            .then(client.getAgent(AGENT_NAME))
            .doOnNext(agent -> assertEquals(AgentState.DISABLED, agent.getState()))
            .then(client.deleteAgent(AGENT_NAME))).verifyComplete();

        assertEquals(5, httpClient.getRequests().size());
        HttpRequest createRequest = httpClient.getRequest(0);
        assertTrue(createRequest.getBodyAsBinaryData().toString().contains("\"kind\":\"voice\""));
        assertTrue(createRequest.getHeaders().getValue(FOUNDRY_FEATURES).contains("VoiceAgents=V1Preview"));
        assertTrue(httpClient.getRequest(2).getUrl().getPath().endsWith("/agents/" + AGENT_NAME + ":disable"));
    }

    @Test
    public void syncVoiceAgentVersionLifecycleMatchesPythonCrud() {
        DeterministicHttpClient httpClient
            = new DeterministicHttpClient().enqueueJson(200, versionJson("1", "Initial instructions"))
                .enqueueJson(200, versionJson("2", "Updated instructions"))
                .enqueueJson(200, agentJson("enabled", "2"))
                .enqueueJson(200, versionJson("1", "Initial instructions"))
                .enqueueJson(200, versionsJson())
                .enqueueJson(200, "");
        AgentsClient client = createBuilder(httpClient).allowPreview(true).buildAgentsClient();

        AgentVersionDetails first = client.createAgentVersion(AGENT_NAME,
            new CreateAgentVersionInput(createVoiceDefinition("Initial instructions")));
        AgentVersionDetails second = client.createAgentVersion(AGENT_NAME,
            new CreateAgentVersionInput(createVoiceDefinition("Updated instructions")));
        AgentDetails agent = client.getAgent(AGENT_NAME);
        AgentVersionDetails retrieved = client.getAgentVersionDetails(AGENT_NAME, first.getVersion());
        List<AgentVersionDetails> versions = new ArrayList<>();
        client.listAgentVersions(AGENT_NAME).forEach(versions::add);
        client.deleteAgent(AGENT_NAME);

        assertEquals("1", first.getVersion());
        assertEquals("2", second.getVersion());
        assertEquals("2", agent.getVersions().getLatest().getVersion());
        assertEquals("1", retrieved.getVersion());
        assertEquals(2, versions.size());
        assertEquals("1", versions.get(0).getVersion());
        assertEquals("2", versions.get(1).getVersion());
        assertTrue(httpClient.getRequest(3).getUrl().getPath().endsWith("/versions/1"));
        assertTrue(httpClient.getRequest(4).getUrl().getPath().endsWith("/versions"));
        assertEquals(HttpMethod.DELETE, httpClient.getRequest(5).getHttpMethod());
        httpClient.assertResponsesConsumed();
    }

    @Test
    public void asyncVoiceAgentVersionLifecycleMatchesPythonCrud() {
        DeterministicHttpClient httpClient
            = new DeterministicHttpClient().enqueueJson(200, versionJson("1", "Initial instructions"))
                .enqueueJson(200, versionJson("2", "Updated instructions"))
                .enqueueJson(200, agentJson("enabled", "2"))
                .enqueueJson(200, versionJson("1", "Initial instructions"))
                .enqueueJson(200, versionsJson())
                .enqueueJson(200, "");
        AgentsAsyncClient client = createBuilder(httpClient).allowPreview(true).buildAgentsAsyncClient();

        StepVerifier.create(client
            .createAgentVersion(AGENT_NAME, new CreateAgentVersionInput(createVoiceDefinition("Initial instructions")))
            .doOnNext(version -> assertEquals("1", version.getVersion()))
            .then(client.createAgentVersion(AGENT_NAME,
                new CreateAgentVersionInput(createVoiceDefinition("Updated instructions"))))
            .doOnNext(version -> assertEquals("2", version.getVersion()))
            .then(client.getAgent(AGENT_NAME))
            .doOnNext(agent -> assertEquals("2", agent.getVersions().getLatest().getVersion()))
            .then(client.getAgentVersionDetails(AGENT_NAME, "1"))
            .doOnNext(version -> assertEquals("1", version.getVersion()))
            .thenMany(client.listAgentVersions(AGENT_NAME))
            .collectList()
            .doOnNext(versions -> {
                assertEquals(2, versions.size());
                assertEquals("1", versions.get(0).getVersion());
                assertEquals("2", versions.get(1).getVersion());
            })
            .then(client.deleteAgent(AGENT_NAME))).verifyComplete();

        assertTrue(httpClient.getRequest(3).getUrl().getPath().endsWith("/versions/1"));
        assertTrue(httpClient.getRequest(4).getUrl().getPath().endsWith("/versions"));
        assertEquals(HttpMethod.DELETE, httpClient.getRequest(5).getHttpMethod());
        httpClient.assertResponsesConsumed();
    }

    @Test
    public void getVoiceVersionDeserializesRequestedVersion() {
        DeterministicHttpClient httpClient
            = new DeterministicHttpClient().enqueueJson(200, versionJson("7", "Version seven"));
        AgentsClient client = createBuilder(httpClient).allowPreview(true).buildAgentsClient();

        AgentVersionDetails version = client.getAgentVersionDetails(AGENT_NAME, "7");

        assertEquals("7", version.getVersion());
        assertEquals(AgentKind.VOICE, version.getDefinition().getKind());
        assertNotNull(((VoiceAgentDefinition) version.getDefinition()).getAudio());
        assertTrue(httpClient.getRequest(0).getUrl().getPath().endsWith("/agents/" + AGENT_NAME + "/versions/7"));
    }

    private static AgentsClientBuilder createBuilder(DeterministicHttpClient httpClient) {
        return new AgentsClientBuilder().endpoint(ENDPOINT)
            .credential(new MockTokenCredential())
            .httpClient(httpClient)
            .serviceVersion(AgentsServiceVersion.V1);
    }

    private static VoiceAgentDefinition createVoiceDefinition(String instructions) {
        VoiceAgentAudioOutputConfig output
            = new VoiceAgentAudioOutputConfig().setVoice("en-US-AvaNeural").setVoiceType(VoiceType.AZURE_STANDARD);
        return new VoiceAgentDefinition().setModelType(VoiceModelType.MANAGED)
            .setModel("gpt-realtime")
            .setInstructions(instructions)
            .setAudio(new VoiceAgentAudioConfig().setOutput(output))
            .setOutputModalities(Collections.singletonList(VoiceOutputModality.AUDIO))
            .setStore(true);
    }

    private static String versionJson(String version, String instructions) {
        return "{\"object\":\"agent.version\",\"id\":\"agent-" + version + "\"," + "\"name\":\"" + AGENT_NAME
            + "\",\"version\":\"" + version + "\","
            + "\"created_at\":1,\"metadata\":{},\"definition\":{\"kind\":\"voice\","
            + "\"model_type\":\"managed\",\"model\":\"gpt-realtime\"," + "\"instructions\":\"" + instructions
            + "\",\"audio\":{\"output\":{" + "\"voice\":\"en-US-AvaNeural\",\"voice_type\":\"azure-standard\"}},"
            + "\"output_modalities\":[\"audio\"],\"store\":true}}";
    }

    private static String versionsJson() {
        return "{\"object\":\"list\",\"data\":[" + versionJson("1", "Initial instructions") + ","
            + versionJson("2", "Updated instructions") + "],\"has_more\":false}";
    }

    private static String agentJson(String state, String latestVersion) {
        return "{\"object\":\"agent\",\"id\":\"agent-id\",\"name\":\"" + AGENT_NAME + "\",\"state\":\"" + state
            + "\",\"versions\":{\"latest\":" + versionJson(latestVersion, "Latest instructions") + "}}";
    }
}
