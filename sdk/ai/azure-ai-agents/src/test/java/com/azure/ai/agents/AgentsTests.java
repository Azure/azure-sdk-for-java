// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.models.*;
import com.azure.core.http.HttpClient;
import com.openai.models.conversations.Conversation;
import com.openai.models.responses.*;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;

import static com.azure.ai.agents.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.*;

@Disabled("Disabled for lack of recordings. Needs to be enabled on the Public Preview release.")
public class AgentsTests extends ClientTestBase {

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.TestUtils#getTestParameters")
    public void basicCRUDOperations(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        AgentsClient client = getAgentsSyncClient(httpClient, serviceVersion);
        String agentName = "test_agent_java";
        String agentModel = "gpt-4o";

        AgentDefinition request = new PromptAgentDefinition(agentModel);

        AgentVersionDetails createdAgent = client.createAgentVersion(agentName, request);

        assertNotNull(createdAgent);
        assertNotNull(createdAgent.getId());
        assertEquals(agentName, createdAgent.getName());

        AgentDetails retrievedAgent = client.getAgent(agentName);

        assertNotNull(retrievedAgent);
        assertNotNull(retrievedAgent.getId());
        assertNotNull(retrievedAgent.getName());
        assertEquals(createdAgent.getId(), retrievedAgent.getVersions().getLatest().getId());
        assertEquals(createdAgent.getName(), retrievedAgent.getName());

        for (AgentDetails agent : client.listAgents()) {
            if (agent.getName().equals(agentName)) {
                assertEquals(agent.getVersions().getLatest().getId(), createdAgent.getId());
                assertEquals(agent.getVersions().getLatest().getName(), createdAgent.getName());
                break;
            }
        }

        DeleteAgentResponse deletedAgent = client.deleteAgent(agentName);
        assertEquals(agentName, deletedAgent.getName());
        assertTrue(deletedAgent.isDeleted());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.TestUtils#getTestParameters")
    public void basicVersionedAgentCRUDOperations(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        AgentsClient client = getAgentsSyncClient(httpClient, serviceVersion);
        String agentName = "test_agent_java";
        String agentModel = "gpt-4o";

        AgentDefinition request = new PromptAgentDefinition(agentModel);

        // Creation
        AgentVersionDetails createdAgent = client.createAgentVersion(agentName, request);

        assertNotNull(createdAgent);
        assertNotNull(createdAgent.getId());
        assertEquals(agentName, createdAgent.getName());

        // Retrieval
        AgentVersionDetails retrievedAgent
            = client.getAgentVersionDetails(createdAgent.getName(), createdAgent.getVersion());
        assertNotNull(retrievedAgent);
        assertNotNull(retrievedAgent.getId());
        assertEquals(createdAgent.getId(), retrievedAgent.getId());
        assertEquals(createdAgent.getName(), retrievedAgent.getName());
        assertEquals(createdAgent.getVersion(), retrievedAgent.getVersion());

        // List
        for (AgentVersionDetails agent : client.listAgentVersions(createdAgent.getName())) {
            if (agent.getId().equals(createdAgent.getId())) {
                assertEquals(agent.getName(), createdAgent.getName());
                assertEquals(agent.getVersion(), createdAgent.getVersion());
                break;
            }
            fail("Created agent not found.");
        }

        // Deletion
        DeleteAgentVersionResponse deletedAgent
            = client.deleteAgentVersion(createdAgent.getName(), createdAgent.getVersion());
        assertNotNull(deletedAgent);
        assertEquals(createdAgent.getName(), deletedAgent.getName());
        assertEquals(createdAgent.getVersion(), deletedAgent.getVersion());
        assertTrue(deletedAgent.isDeleted());
    }

    //    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    //    @MethodSource("com.azure.ai.agents.TestUtils#getTestParameters")
    //    public void operationOperations(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
    //        AgentsClient client = getAgentsSyncClient(httpClient, serviceVersion);
    //        String agentName = "test_agent_java";
    //        String agentModel = "gpt-4o";
    //
    //        AgentDefinition request = new PromptAgentDefinition(agentModel);
    //
    //        AgentVersionObject createdAgent = client.createAgentVersion(agentName, request);
    //
    //        assertNotNull(createdAgent);
    //        assertNotNull(createdAgent.getId());
    //        assertEquals(agentName, createdAgent.getName());
    //
    //        for (AgentContainerOperationObject agentOperation : client
    //            .listAgentContainerOperations(createdAgent.getName())) {
    //            assertNotNull(agentOperation.getAgentId());
    //            assertNotNull(agentOperation.getId());
    //            assertFalse(agentOperation.getId().trim().isEmpty());
    //
    //            AgentContainerOperationObject retrievedAgentOperation
    //                = client.getAgentContainerOperation(createdAgent.getName(), agentOperation.getId());
    //            assertNotNull(retrievedAgentOperation);
    //            assertEquals(agentOperation.getId(), retrievedAgentOperation.getId());
    //            assertEquals(agentOperation.getAgentId(), retrievedAgentOperation.getAgentId());
    //            assertEquals(agentOperation.getStatus(), retrievedAgentOperation.getStatus());
    //        }
    //
    //        // Clean up
    //        DeleteAgentVersionResponse agentDeletion
    //            = client.deleteAgentVersion(createdAgent.getName(), createdAgent.getVersion());
    //        assertEquals(agentName, agentDeletion.getName());
    //        assertTrue(agentDeletion.isDeleted());
    //    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.TestUtils#getTestParameters")
    public void promptAgentTest(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        AgentsClient agentsClient = getAgentsSyncClient(httpClient, serviceVersion);
        ConversationsClient conversationsClient = getConversationsSyncClient(httpClient, serviceVersion);
        ResponsesClient responsesClient = getResponsesSyncClient(httpClient, serviceVersion);

        String agentName = "test_prompt_agent_java";
        String agentModel = "gpt-4o";

        PromptAgentDefinition promptAgentDefinition = new PromptAgentDefinition(agentModel);

        AgentVersionDetails createdAgent = agentsClient.createAgentVersion(agentName, promptAgentDefinition);

        AgentReference agentReference = new AgentReference(createdAgent.getName());
        agentReference.setVersion(createdAgent.getVersion());

        Conversation conversation = conversationsClient.getConversationService().create();

        List<ResponseInputItem> inputItems = new ArrayList<>();
        inputItems.add(ResponseInputItem.ofEasyInputMessage(EasyInputMessage.builder()
            .role(EasyInputMessage.Role.SYSTEM)
            .content("You are a helpful assistant who speaks like a pirate. Today is a sunny and warm day.")
            .build()));
        inputItems.add(ResponseInputItem.ofEasyInputMessage(EasyInputMessage.builder()
            .role(EasyInputMessage.Role.USER)
            .content("Could you help me decide what clothes to wear today?")
            .build()));

        Response response = responsesClient.createWithAgentConversation(agentReference, conversation.id(),
            ResponseCreateParams.builder().inputOfResponse(inputItems));

        assertNotNull(createdAgent);
        assertNotNull(createdAgent.getId());
        assertEquals(agentName, createdAgent.getName());

        assertNotNull(response);
        assertTrue(response.id().startsWith("resp"));
        assertTrue(response.status().isPresent());
        assertEquals(ResponseStatus.COMPLETED, response.status().get());
        assertFalse(response.output().isEmpty());

        assertFalse(response.output().isEmpty());
        assertTrue(response.output().get(0).isMessage());
        assertFalse(response.output().get(0).asMessage().content().isEmpty());

        // Clean up
        agentsClient.deleteAgent(createdAgent.getId());
        conversationsClient.getConversationService().delete(conversation.id());
        // Deleting response causes a 500
        //        responsesClient.getOpenAIClient().delete(response.id());
    }
    //
    //    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    //    @MethodSource("com.azure.ai.agents.TestUtils#getTestParameters")
    //    public void liveWorkflowAgentTest(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
    //        AgentsClient agentsClient = getAgentsSyncClient(httpClient, serviceVersion);
    //        ConversationsClient conversationsClient = getConversationsSyncClient(httpClient, serviceVersion);
    //        ResponsesClient responsesClient = getResponsesSyncClient(httpClient, serviceVersion);
    //
    //        String agentName = "test_workflow_agent_java";
    //
    //        // Using the new utility function to get workflow data from test resources
    //        WorkflowDefinition workflowAgentDefinition = new WorkflowDefinition();
    //
    //        workflowAgentDefinition.setTrigger(TestUtils.getBeginWorkflowMap("test_workflow.json"));
    //
    //        AgentVersionObject createdAgent = agentsClient.createAgentVersion(agentName, workflowAgentDefinition);
    //
    //        AgentReference agentReference = new AgentReference(createdAgent.getName());
    //        agentReference.setVersion(createdAgent.getVersion());
    //
    //        Conversation conversation = conversationsClient.getOpenAIClient().create();
    //        Response response = responsesClient.createWithAgentConversation(agentReference, conversation.id(),
    //            ResponseCreateParams.builder().input("Hello, agent!"));
    //
    //        assertNotNull(createdAgent);
    //        assertNotNull(createdAgent.getId());
    //        assertEquals(agentName, createdAgent.getName());
    //
    //        assertNotNull(response);
    //        assertTrue(response.id().startsWith("wfresp"));
    //        assertTrue(response.status().isPresent());
    //        assertEquals(ResponseStatus.COMPLETED, response.status().get());
    //
    //        assertTrue(response._additionalProperties().containsKey("output_text"));
    //
    //        // this should be uncommented once I figure why the service hangs when attaching an input to the request
    //        //        assertFalse(response.output().isEmpty());
    //        //        AgentResponseItem agentResponseItem = response.OutputItems[0].AsAgentResponseItem();
    //        //        Assert.That(agentResponseItem, Is.InstanceOf<AgentWorkflowActionResponseItem>());
    //
    //        // Clean up
    //        agentsClient.deleteAgent(createdAgent.getId());
    //        conversationsClient.getOpenAIClient().delete(conversation.id());
    //        // Deleting response causes a 500
    //        //        responsesClient.getOpenAIClient().delete(response.id());
    //    }
}
