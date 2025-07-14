// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.agents.persistent;

import com.azure.ai.agents.persistent.models.CreateAgentOptions;
import com.azure.ai.agents.persistent.models.MessageRole;
import com.azure.ai.agents.persistent.models.PersistentAgent;
import com.azure.ai.agents.persistent.models.PersistentAgentThread;
import com.azure.ai.agents.persistent.models.ThreadMessage;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedIterable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.HashMap;
import java.util.Map;

import static com.azure.ai.agents.persistent.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MessagesClientTest extends ClientTestBase {

    private PersistentAgentsClientBuilder clientBuilder;
    private PersistentAgentsAdministrationClient administrationClient;
    private ThreadsClient threadsClient;
    private MessagesClient messagesClient;
    private PersistentAgent agent;
    private PersistentAgentThread thread;

    private PersistentAgent createAgent(String agentName) {
        CreateAgentOptions options
            = new CreateAgentOptions("gpt-4o-mini").setName(agentName).setInstructions("You are a helpful agent");
        PersistentAgent createdAgent = administrationClient.createAgent(options);
        assertNotNull(createdAgent, "Persistent agent should not be null");
        return createdAgent;
    }

    private void setup(HttpClient httpClient) {
        clientBuilder = getClientBuilder(httpClient);
        PersistentAgentsClient agentsClient = clientBuilder.buildClient();
        administrationClient = agentsClient.getPersistentAgentsAdministrationClient();
        threadsClient = agentsClient.getThreadsClient();
        messagesClient = agentsClient.getMessagesClient();
        agent = createAgent("TestAgent");
        thread = threadsClient.createThread();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testCreateAndRetrieveMessage(HttpClient httpClient) {
        setup(httpClient);
        // Create message
        ThreadMessage createdMessage
            = messagesClient.createMessage(thread.getId(), MessageRole.USER, "What do you know about Microsoft");
        assertNotNull(createdMessage, "Created message should not be null");
        assertNotNull(createdMessage.getId(), "Message ID should not be null");
        assertEquals(MessageRole.USER, createdMessage.getRole(), "Message role should be USER");
        // Retrieve message
        ThreadMessage retrievedMessage = messagesClient.getMessage(thread.getId(), createdMessage.getId());
        assertNotNull(retrievedMessage, "Retrieved message should not be null");
        assertEquals(createdMessage.getId(), retrievedMessage.getId(), "Message IDs should match");
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testCreateMessageWithMetadata(HttpClient httpClient) {
        setup(httpClient);
        Map<String, String> metadata = new HashMap<>();
        metadata.put("priority", "high");
        ThreadMessage messageWithMetadata = messagesClient.createMessage(thread.getId(), MessageRole.USER,
            "This is a message with metadata", null, metadata);
        assertNotNull(messageWithMetadata, "Message with metadata should not be null");
        assertNotNull(messageWithMetadata.getMetadata(), "Message metadata should not be null");
        assertEquals("high", messageWithMetadata.getMetadata().get("priority"), "Metadata priority should match");
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testUpdateMessage(HttpClient httpClient) {
        setup(httpClient);
        // Create initial message
        ThreadMessage createdMessage
            = messagesClient.createMessage(thread.getId(), MessageRole.USER, "Initial message");
        // Update message metadata
        Map<String, String> updatedMetadata = new HashMap<>();
        updatedMetadata.put("updated", "true");
        updatedMetadata.put("timestamp", String.valueOf(System.currentTimeMillis()));
        ThreadMessage updatedMessage
            = messagesClient.updateMessage(thread.getId(), createdMessage.getId(), updatedMetadata);
        assertNotNull(updatedMessage, "Updated message should not be null");
        assertNotNull(updatedMessage.getMetadata(), "Updated metadata should not be null");
        assertEquals("true", updatedMessage.getMetadata().get("updated"), "Metadata updated flag should be true");
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testListMessagesDefault(HttpClient httpClient) {
        setup(httpClient);
        // Create at least two messages
        messagesClient.createMessage(thread.getId(), MessageRole.USER, "Message 1");
        messagesClient.createMessage(thread.getId(), MessageRole.USER, "Message 2");
        PagedIterable<ThreadMessage> messagesList = messagesClient.listMessages(thread.getId());

        assertNotNull(messagesList, "Messages list should not be null");
        assertTrue(messagesList.stream().count() >= 2, "There should be at least 2 messages");
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testListMessagesWithParameters(HttpClient httpClient) {
        setup(httpClient);
        // Create multiple messages
        for (int i = 0; i < 3; i++) {
            messagesClient.createMessage(thread.getId(), MessageRole.USER, "Message " + i);
        }
        PagedIterable<ThreadMessage> filteredMessages = messagesClient.listMessages(thread.getId(), null,    // runId
            10,      // limit
            null,    // order
            null,    // after
            null);   // before

        assertNotNull(filteredMessages, "Filtered messages should not be null");
        assertTrue(filteredMessages.stream().count() <= 10, "Messages list should have at most 10 messages");
    }

    @AfterEach
    public void cleanup() {
        try {
            if (thread != null) {
                threadsClient.deleteThread(thread.getId());
            }
            if (agent != null) {
                administrationClient.deleteAgent(agent.getId());
            }
        } catch (Exception e) {
            // Log the exception but do not fail the test
            System.out.println("Cleanup issue: " + e.getMessage());
        }
    }
}
