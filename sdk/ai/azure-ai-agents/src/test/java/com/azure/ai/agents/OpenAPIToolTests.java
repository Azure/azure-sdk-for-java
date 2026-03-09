// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.models.AgentReference;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.OpenApiAnonymousAuthDetails;
import com.azure.ai.agents.models.OpenApiFunctionDefinition;
import com.azure.ai.agents.models.OpenApiTool;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.core.http.HttpClient;
import com.azure.core.util.BinaryData;
import com.openai.models.conversations.Conversation;
import com.openai.models.conversations.items.ItemCreateParams;
import com.openai.models.responses.EasyInputMessage;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseStatus;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import static com.azure.ai.agents.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.*;

public class OpenAPIToolTests extends ClientTestBase {

    private static final String AGENT_NAME = "openapi-tool-test-agent-java";

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.TestUtils#getTestParameters")
    public void openApiToolEndToEnd(HttpClient httpClient, AgentsServiceVersion serviceVersion) throws IOException {
        AgentsClient agentsClient = getAgentsSyncClient(httpClient, serviceVersion);
        ResponsesClient responsesClient = getResponsesSyncClient(httpClient, serviceVersion);
        ConversationsClient conversationsClient = getConversationsSyncClient(httpClient, serviceVersion);

        // Load the OpenAPI spec from test resources
        Map<String, BinaryData> spec
            = OpenApiFunctionDefinition.readSpecFromFile(TestUtils.getTestResourcePath("assets/httpbin_openapi.json"));

        OpenApiFunctionDefinition toolDefinition
            = new OpenApiFunctionDefinition("httpbin_get", spec, new OpenApiAnonymousAuthDetails())
                .setDescription("Get request metadata from an OpenAPI endpoint.");

        PromptAgentDefinition agentDefinition
            = new PromptAgentDefinition("gpt-4o").setInstructions("Use the OpenAPI tool for HTTP request metadata.")
                .setTools(Arrays.asList(new OpenApiTool(toolDefinition)));

        AgentVersionDetails agentVersion = agentsClient.createAgentVersion(AGENT_NAME, agentDefinition);
        assertNotNull(agentVersion);
        assertNotNull(agentVersion.getId());
        assertEquals(AGENT_NAME, agentVersion.getName());

        try {
            // Create a conversation and add a user message
            Conversation conversation = conversationsClient.getConversationService().create();
            assertNotNull(conversation);
            assertNotNull(conversation.id());

            conversationsClient.getConversationService()
                .items()
                .create(ItemCreateParams.builder()
                    .conversationId(conversation.id())
                    .addItem(EasyInputMessage.builder()
                        .role(EasyInputMessage.Role.USER)
                        .content("Use the OpenAPI tool and summarize the returned URL and origin in one sentence.")
                        .build())
                    .build());

            // Create a response using the agent with the conversation
            AgentReference agentReference
                = new AgentReference(agentVersion.getName()).setVersion(agentVersion.getVersion());

            ResponseCreateParams.Builder options = ResponseCreateParams.builder().maxOutputTokens(300L);

            Response response = responsesClient.createWithAgentConversation(agentReference, conversation.id(), options);

            assertNotNull(response);
            assertTrue(response.id().startsWith("resp"));
            assertTrue(response.status().isPresent());
            assertEquals(ResponseStatus.COMPLETED, response.status().get());
            assertFalse(response.output().isEmpty());
            assertTrue(response.output().stream().anyMatch(item -> item.isMessage()));
        } finally {
            // Clean up
            agentsClient.deleteAgentVersion(agentVersion.getName(), agentVersion.getVersion());
        }
    }
}
