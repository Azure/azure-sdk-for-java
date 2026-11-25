// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.models.AgentReference;
import com.azure.ai.agents.models.AzureCreateResponseOptions;
import com.azure.core.util.BinaryData;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.ai.agents.models.StructuredInputDefinition;
import com.azure.core.http.HttpClient;
import com.openai.models.responses.EasyInputMessage;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseInputItem;
import com.openai.models.responses.ResponseStatus;
import com.openai.services.async.ConversationServiceAsync;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static com.azure.ai.agents.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AgentsAsyncTests extends ClientTestBase {

    private static final String AGENT_NAME = "test-agent-java";

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.TestUtils#getTestParameters")
    public void basicCRUDOperations(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        AgentsAsyncClient client = getAgentsAsyncClient(httpClient, serviceVersion);
        String agentModel = "gpt-4o";

        PromptAgentDefinition request = new PromptAgentDefinition(agentModel);

        StepVerifier.create(client.createAgentVersion(AGENT_NAME, request).flatMap(created -> {
            // Assertions for created agent version
            assertNotNull(created);
            assertNotNull(created.getId());
            assertEquals(AGENT_NAME, created.getName());

            return client.getAgent(AGENT_NAME).doOnNext(retrieved -> {
                // Assertions for retrieved agent
                assertNotNull(retrieved);
                assertNotNull(retrieved.getId());
                assertNotNull(retrieved.getName());
                assertEquals(created.getId(), retrieved.getVersions().getLatest().getId());
                assertEquals(created.getName(), retrieved.getName());
            })
                .thenMany(client.listAgents())
                .filter(agent -> agent.getName().equals(AGENT_NAME))
                .next()
                .doOnNext(agent -> {
                    assertEquals(agent.getVersions().getLatest().getId(), created.getId());
                    assertEquals(agent.getVersions().getLatest().getName(), created.getName());
                })
                .then(client.deleteAgentVersion(AGENT_NAME, created.getVersion()));
        })).verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.TestUtils#getTestParameters")
    public void basicVersionedAgentCRUDOperations(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        AgentsAsyncClient client = getAgentsAsyncClient(httpClient, serviceVersion);
        String agentModel = "gpt-4o";

        PromptAgentDefinition request = new PromptAgentDefinition(agentModel);

        StepVerifier.create(client.createAgentVersion(AGENT_NAME, request).flatMap(created -> {
            // Assertions for created agent version
            assertNotNull(created);
            assertNotNull(created.getId());
            assertEquals(AGENT_NAME, created.getName());

            return client.getAgentVersionDetails(AGENT_NAME, created.getVersion()).doOnNext(retrieved -> {
                // Assertions for retrieved agent version
                assertNotNull(retrieved);
                assertNotNull(retrieved.getId());
                assertNotNull(retrieved.getName());
                assertEquals(created.getId(), retrieved.getId());
                assertEquals(created.getName(), retrieved.getName());
            })
                .thenMany(client.listAgentVersions(AGENT_NAME))
                .filter(agentVersion -> agentVersion.getVersion().equals(created.getVersion()))
                .next()
                .doOnNext(agentVersion -> {
                    assertEquals(agentVersion.getId(), created.getId());
                    assertEquals(agentVersion.getName(), created.getName());
                })
                .then(client.deleteAgentVersion(AGENT_NAME, created.getVersion()));
        })).verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.TestUtils#getTestParameters")
    public void promptAgentTest(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        AgentsAsyncClient agentsClient = getAgentsAsyncClient(httpClient, serviceVersion);
        ConversationServiceAsync conversationsClient = getConversationsAsyncClient(httpClient, serviceVersion);
        ResponsesAsyncClient responsesClient = getResponsesAsyncClient(httpClient, serviceVersion);
        String agentModel = "gpt-4o";
        String agentName = testResourceNamer.randomName("prompt-agent-async-", 32);

        PromptAgentDefinition promptAgentDefinition = new PromptAgentDefinition(agentModel);

        StepVerifier
            .create(Mono.usingWhen(agentsClient.createAgentVersion(agentName, promptAgentDefinition), createdAgent -> {
                assertNotNull(createdAgent);
                assertNotNull(createdAgent.getId());
                assertEquals(agentName, createdAgent.getName());

                AgentReference agentReference = new AgentReference(createdAgent.getName());
                agentReference.setVersion(createdAgent.getVersion());

                return Mono.usingWhen(Mono.fromFuture(conversationsClient.create()), conversation -> {
                    List<ResponseInputItem> inputItems = new ArrayList<>();
                    inputItems.add(ResponseInputItem.ofEasyInputMessage(EasyInputMessage.builder()
                        .type(EasyInputMessage.Type.MESSAGE)
                        .role(EasyInputMessage.Role.SYSTEM)
                        .content("You are a helpful assistant who speaks like a pirate. Today is a sunny and warm day.")
                        .build()));
                    inputItems.add(ResponseInputItem.ofEasyInputMessage(EasyInputMessage.builder()
                        .type(EasyInputMessage.Type.MESSAGE)
                        .role(EasyInputMessage.Role.USER)
                        .content("Could you help me decide what clothes to wear today?")
                        .build()));

                    ResponseCreateParams.Builder paramsBuilder
                        = ResponseCreateParams.builder().inputOfResponse(inputItems);

                    return Mono.usingWhen(responsesClient.createAzureResponse(
                        new AzureCreateResponseOptions().setAgentReference(agentReference),
                        paramsBuilder.conversation(conversation.id())), response -> Mono.fromCallable(() -> {
                            assertNotNull(response);
                            assertTrue(response.id().startsWith("resp"));
                            assertTrue(response.status().isPresent());
                            assertEquals(ResponseStatus.COMPLETED, response.status().get());
                            assertFalse(response.output().isEmpty());
                            assertTrue(response.output().get(0).isMessage());
                            assertFalse(response.output().get(0).asMessage().content().isEmpty());
                            return response;
                        }), response -> Mono.fromFuture(responsesClient.getResponseServiceAsync().delete(response.id()))
                            .then());
                }, conversation -> Mono.fromFuture(conversationsClient.delete(conversation.id())).then());
            }, createdAgent -> agentsClient.deleteAgent(createdAgent.getName()).then()))
            .assertNext(response -> assertTrue(response.id().startsWith("resp")))
            .verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.TestUtils#getTestParameters")
    public void structuredInputTest(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        AgentsAsyncClient agentsClient = getAgentsAsyncClient(httpClient, serviceVersion);
        ResponsesAsyncClient responsesClient = getResponsesAsyncClient(httpClient, serviceVersion);
        String agentModel = "gpt-4o";

        // Create an agent with structured input definitions
        Map<String, StructuredInputDefinition> structuredInputDefinitions = new LinkedHashMap<>();
        structuredInputDefinitions.put("userName",
            new StructuredInputDefinition().setDescription("User's name").setRequired(true));
        structuredInputDefinitions.put("userRole",
            new StructuredInputDefinition().setDescription("User's role").setRequired(true));

        StepVerifier.create(
            agentsClient
                .createAgentVersion(AGENT_NAME,
                    new PromptAgentDefinition(agentModel).setInstructions("You are a helpful assistant. "
                        + "The user's name is {{userName}} and their role is {{userRole}}. "
                        + "Greet them and confirm their details.").setStructuredInputs(structuredInputDefinitions))
                .flatMap(createdAgent -> {
                    assertNotNull(createdAgent);
                    assertNotNull(createdAgent.getId());
                    assertEquals(AGENT_NAME, createdAgent.getName());

                    Map<String, BinaryData> structuredInputValues = new LinkedHashMap<>();
                    structuredInputValues.put("userName", BinaryData.fromObject("Alice Smith"));
                    structuredInputValues.put("userRole", BinaryData.fromObject("Senior Developer"));

                    return responsesClient
                        .createAzureResponse(
                            new AzureCreateResponseOptions()
                                .setAgentReference(
                                    new AgentReference(createdAgent.getName()).setVersion(createdAgent.getVersion()))
                                .setStructuredInputs(structuredInputValues),
                            ResponseCreateParams.builder().input("Hello! Can you confirm my details?"))
                        .flatMap(response -> agentsClient
                            .deleteAgentVersion(createdAgent.getName(), createdAgent.getVersion())
                            .thenReturn(response));
                }))
            .assertNext(response -> {
                assertNotNull(response);
                assertTrue(response.id().startsWith("resp"));
                assertTrue(response.status().isPresent());
                assertEquals(ResponseStatus.COMPLETED, response.status().get());
                assertFalse(response.output().isEmpty());
                assertTrue(response.output().get(0).isMessage());
                assertFalse(response.output().get(0).asMessage().content().isEmpty());
            })
            .verifyComplete();
    }
}
