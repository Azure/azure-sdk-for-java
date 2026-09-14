// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.voice;

import com.azure.ai.agents.AgentsAsyncClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.models.AgentDetails;
import com.azure.ai.agents.models.AgentState;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.CreateAgentVersionInput;
import com.azure.ai.agents.models.VoiceAgentAudioConfig;
import com.azure.ai.agents.models.VoiceAgentAudioOutputConfig;
import com.azure.ai.agents.models.VoiceAgentDefinition;
import com.azure.ai.agents.models.VoiceModelType;
import com.azure.ai.agents.models.VoiceOutputModality;
import com.azure.ai.agents.models.VoiceType;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import com.azure.identity.DefaultAzureCredentialBuilder;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Parity for Python test_voice_agent_crud_async.py: versioned CRUD, disable/enable and guided generation.
 * Offline tests use scripted HTTP responses, not recordings. Live tests require AZURE_TEST_MODE=LIVE,
 * FOUNDRY_PROJECT_ENDPOINT and DefaultAzureCredential authentication. CRUD and state tests additionally
 * require FOUNDRY_VOICE_MODEL_NAME. Every scenario deletes only its own uniquely named live agent.
 */
public class VoiceAgentCrudAsyncTests {
    private static final Duration TIMEOUT = Duration.ofMinutes(2);
    private static final String AGENT = "voice-agent-crud-async-java";
    private static final String MODEL = "voice-model";
    private static final String INSTRUCTIONS = "You are a helpful voice assistant.";
    private static final String UPDATED_INSTRUCTIONS = INSTRUCTIONS + " Always greet the caller by name.";

    enum Scenario {
        CRUD, DISABLE_ENABLE, GENERATE
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void voiceAgentOperations(Scenario scenario) {
        ScriptedTransport transport = new ScriptedTransport();
        String path = "/agents/" + AGENT;
        Map<String, Object> first = version("1", INSTRUCTIONS);
        switch (scenario) {
            case CRUD:
                Map<String, Object> second = version("2", UPDATED_INSTRUCTIONS);
                transport.expect(HttpMethod.POST, path + "/versions", createBody(INSTRUCTIONS), first);
                transport.expect(HttpMethod.POST, path + "/versions", createBody(UPDATED_INSTRUCTIONS), second);
                transport.expect(HttpMethod.GET, path, null, agent(second, "enabled"));
                transport.expect(HttpMethod.GET, path + "/versions/1", null, first);
                transport.expect(HttpMethod.GET, path + "/versions", null,
                    object("data", java.util.Arrays.asList(first, second), "has_more", false));
                break;

            case DISABLE_ENABLE:
                transport.expect(HttpMethod.POST, path + "/versions", createBody(INSTRUCTIONS), first);
                transport.expect(HttpMethod.POST, path + ":disable", null, null, 204);
                transport.expect(HttpMethod.GET, path, null, agent(first, "disabled"));
                transport.expect(HttpMethod.POST, path + ":enable", null, null, 204);
                transport.expect(HttpMethod.GET, path, null, agent(first, "enabled"));
                break;

            case GENERATE:
                transport.expect(HttpMethod.POST, "/agents:generate", object("kind", "voice", "name", AGENT),
                    agent(first, "enabled"));
                break;

            default:
                throw new AssertionError("Unexpected scenario: " + scenario);
        }
        transport.expect(HttpMethod.DELETE, path, null, object("deleted", true, "name", AGENT));
        AgentsClientBuilder builder = new AgentsClientBuilder().endpoint("https://localhost")
            .credential(new MockTokenCredential())
            .httpClient(transport)
            .allowPreview(true);
        StepVerifier.create(runScenario(builder, AGENT, MODEL, scenario)).expectComplete().verify(TIMEOUT);
        assertTrue(transport.requests.isEmpty(), "All expected REST operations must be exercised.");
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    @EnabledIfEnvironmentVariable(named = "AZURE_TEST_MODE", matches = "LIVE")
    public void voiceAgentOperationsLive(Scenario scenario) {
        Configuration configuration = Configuration.getGlobalConfiguration();
        String endpoint = configuration.get("FOUNDRY_PROJECT_ENDPOINT");
        String model = configuration.get("FOUNDRY_VOICE_MODEL_NAME");
        assertNotNull(endpoint, "FOUNDRY_PROJECT_ENDPOINT is required for live tests.");
        if (scenario != Scenario.GENERATE) {
            assertNotNull(model, "FOUNDRY_VOICE_MODEL_NAME is required for this live test.");
        }
        AgentsClientBuilder builder = new AgentsClientBuilder().endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder().build())
            .allowPreview(true);
        StepVerifier.create(runScenario(builder, "test-voice-crud-" + UUID.randomUUID(), model, scenario))
            .expectComplete()
            .verify(TIMEOUT);
    }

    private static Mono<Void> runScenario(AgentsClientBuilder builder, String name, String model, Scenario scenario) {
        AgentsAsyncClient client = builder.buildAgentsAsyncClient();
        Mono<?> create = scenario == Scenario.GENERATE
            ? builder.beta()
                .buildBetaAgentsAsyncClient()
                .generateAgent(BinaryData.fromObject(object("kind", "voice", "name", name)))
            : client.createAgentVersion(name, new CreateAgentVersionInput(definition(model, INSTRUCTIONS)));
        return Mono.usingWhen(create, created -> {
            if (scenario == Scenario.GENERATE) {
                AgentDetails generated = (AgentDetails) created;
                validateAgent(generated, name, null);
                VoiceAgentDefinition voice
                    = assertInstanceOf(VoiceAgentDefinition.class, generated.getVersions().getLatest().getDefinition());
                assertNotNull(voice.getInstructions());
                assertFalse(voice.getInstructions().isEmpty());
                return Mono.<Void>empty();
            }
            AgentVersionDetails first = (AgentVersionDetails) created;
            validateVersion(first, name, null);
            validateDefinition(first, model, INSTRUCTIONS);
            if (scenario == Scenario.DISABLE_ENABLE) {
                return client.disableAgent(name)
                    .then(client.getAgent(name))
                    .doOnNext(agent -> assertEquals(AgentState.DISABLED, agent.getState()))
                    .then(client.enableAgent(name))
                    .then(client.getAgent(name))
                    .doOnNext(agent -> assertEquals(AgentState.ENABLED, agent.getState()))
                    .then();
            }
            return client.createAgentVersion(name, new CreateAgentVersionInput(definition(model, UPDATED_INSTRUCTIONS)))
                .flatMap(second -> {
                    validateVersion(second, name, null);
                    validateDefinition(second, model, UPDATED_INSTRUCTIONS);
                    assertNotEquals(first.getVersion(), second.getVersion());
                    return client.getAgent(name)
                        .doOnNext(agent -> validateAgent(agent, name, second.getVersion()))
                        .then(client.getAgentVersionDetails(name, first.getVersion()))
                        .doOnNext(version -> {
                            validateVersion(version, name, first.getVersion());
                            validateDefinition(version, model, INSTRUCTIONS);
                        })
                        .thenMany(client.listAgentVersions(name))
                        .collectList()
                        .doOnNext(versions -> {
                            assertTrue(versions.size() >= 2);
                            versions.forEach(version -> validateVersion(version, name, null));
                            assertTrue(
                                versions.stream().anyMatch(version -> first.getVersion().equals(version.getVersion())));
                            assertTrue(versions.stream()
                                .anyMatch(version -> second.getVersion().equals(version.getVersion())));
                        })
                        .then();
                });
        }, created -> client.deleteAgent(name), (created, error) -> client.deleteAgent(name),
            created -> client.deleteAgent(name));
    }

    private static VoiceAgentDefinition definition(String model, String instructions) {
        return new VoiceAgentDefinition().setModelType(VoiceModelType.MANAGED)
            .setModel(model)
            .setInstructions(instructions)
            .setAudio(new VoiceAgentAudioConfig().setOutput(
                new VoiceAgentAudioOutputConfig().setVoice("en-US-AvaNeural").setVoiceType(VoiceType.AZURE_STANDARD)))
            .setOutputModalities(Collections.singletonList(VoiceOutputModality.AUDIO));
    }

    private static void validateDefinition(AgentVersionDetails version, String model, String instructions) {
        VoiceAgentDefinition voice = assertInstanceOf(VoiceAgentDefinition.class, version.getDefinition());
        assertEquals(VoiceModelType.MANAGED, voice.getModelType());
        assertEquals(model, voice.getModel());
        assertEquals(instructions, voice.getInstructions());
        assertNotNull(voice.getAudio());
        assertNotNull(voice.getAudio().getOutput());
        assertEquals("en-US-AvaNeural", voice.getAudio().getOutput().getVoice());
        assertEquals(VoiceType.AZURE_STANDARD, voice.getAudio().getOutput().getVoiceType());
        assertEquals(Collections.singletonList(VoiceOutputModality.AUDIO), voice.getOutputModalities());
    }

    private static void validateVersion(AgentVersionDetails version, String name, String expectedVersion) {
        assertNotNull(version);
        assertNotNull(version.getId());
        assertEquals(name, version.getName());
        assertNotNull(version.getVersion());
        assertFalse(version.getVersion().isEmpty());
        assertNotNull(version.getCreatedAt());
        assertInstanceOf(VoiceAgentDefinition.class, version.getDefinition());
        if (expectedVersion != null) {
            assertEquals(expectedVersion, version.getVersion());
        }
    }

    private static void validateAgent(AgentDetails agent, String name, String expectedVersion) {
        assertNotNull(agent);
        assertNotNull(agent.getId());
        assertEquals(name, agent.getName());
        assertNotNull(agent.getVersions());
        validateVersion(agent.getVersions().getLatest(), name, expectedVersion);
    }

    private static Map<String, Object> createBody(String instructions) {
        return object("definition",
            object("kind", "voice", "model_type", "managed", "model", MODEL, "instructions", instructions, "audio",
                object("output", object("voice", "en-US-AvaNeural", "voice_type", "azure-standard")),
                "output_modalities", Collections.singletonList("audio")));
    }

    private static Map<String, Object> version(String version, String instructions) {
        return object("id", AGENT + ":" + version, "name", AGENT, "version", version, "object", "agent.version",
            "created_at", 1700000000, "definition", createBody(instructions).get("definition"));
    }

    private static Map<String, Object> agent(Map<String, Object> latest, String state) {
        return object("id", AGENT, "name", AGENT, "object", "agent", "state", state, "versions",
            object("latest", latest));
    }

    private static Map<String, Object> object(Object... entries) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (int index = 0; index < entries.length; index += 2) {
            result.put((String) entries[index], entries[index + 1]);
        }
        return result;
    }

    private static final class ScriptedTransport implements HttpClient {
        private final Deque<Function<HttpRequest, Mono<HttpResponse>>> requests = new ArrayDeque<>();

        void expect(HttpMethod method, String path, Map<String, Object> body, Map<String, Object> response) {
            expect(method, path, body, response, 200);
        }

        void expect(HttpMethod method, String path, Map<String, Object> body, Map<String, Object> response,
            int status) {
            requests.add(request -> {
                assertEquals(method, request.getHttpMethod());
                assertEquals(path, request.getUrl().getPath());
                Mono<Void> check = body == null
                    ? Mono.empty()
                    : BinaryData.fromFlux(request.getBody())
                        .doOnNext(actual -> assertEquals(body, actual.toObject(Map.class)))
                        .then();
                return check.thenReturn(new MockHttpResponse(request, status,
                    new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/json"),
                    response == null ? new byte[0] : BinaryData.fromObject(response).toBytes()));
            });
        }

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            return Mono.defer(() -> {
                assertFalse(requests.isEmpty(), "Unexpected request: " + request.getUrl());
                return requests.removeFirst().apply(request);
            });
        }

        @Override
        public HttpResponse sendSync(HttpRequest request, Context context) {
            throw new AssertionError("The async client must not use synchronous HTTP.");
        }
    }
}
