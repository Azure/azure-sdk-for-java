// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.agents.persistent;

import com.azure.ai.agents.persistent.models.CreateAgentOptions;
import com.azure.ai.agents.persistent.models.MessageRole;
import com.azure.ai.agents.persistent.models.PersistentAgent;
import com.azure.ai.agents.persistent.models.PersistentAgentThread;
import com.azure.ai.agents.persistent.models.ThreadMessage;
import com.azure.core.http.HttpClient;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static com.azure.ai.agents.persistent.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MessagesAsyncClientTest extends ClientTestBase {

    private PersistentAgentsClientBuilder clientBuilder;
    private PersistentAgentsAdministrationAsyncClient administrationAsyncClient;
    private ThreadsAsyncClient threadsAsyncClient;
    private MessagesAsyncClient messagesAsyncClient;
    private PersistentAgent agent;
    private PersistentAgentThread thread;

    private void createTestAgent(HttpClient httpClient) {
        clientBuilder = getClientBuilder(httpClient);
        PersistentAgentsAsyncClient agentsAsyncClient = clientBuilder.buildAsyncClient();
        administrationAsyncClient = agentsAsyncClient.getPersistentAgentsAdministrationAsyncClient();
        threadsAsyncClient = agentsAsyncClient.getThreadsAsyncClient();
        messagesAsyncClient = agentsAsyncClient.getMessagesAsyncClient();

        CreateAgentOptions options
            = new CreateAgentOptions("gpt-4o-mini").setName("TestAgent").setInstructions("You are a helpful agent");

        StepVerifier.create(administrationAsyncClient.createAgent(options)).assertNext(createdAgent -> {
            assertNotNull(createdAgent, "Persistent agent should not be null");
            agent = createdAgent;
        }).verifyComplete();

        StepVerifier.create(threadsAsyncClient.createThread()).assertNext(createdThread -> {
            assertNotNull(createdThread, "Thread should not be null");
            thread = createdThread;
        }).verifyComplete();
    }

    @Disabled
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testCreateAndRetrieveMessage(HttpClient httpClient) {
        createTestAgent(httpClient);

        // Test content
        BinaryData content = BinaryData.fromString("What do you know about Microsoft");

        // Create a message and verify
        StepVerifier.create(messagesAsyncClient.createMessage(thread.getId(), MessageRole.USER, content))
            .assertNext(createdMessage -> {
                assertNotNull(createdMessage, "Created message should not be null");
                assertNotNull(createdMessage.getId(), "Message ID should not be null");
                assertEquals(MessageRole.USER, createdMessage.getRole(), "Message role should be USER");

                // Verify by retrieving the message
                StepVerifier.create(messagesAsyncClient.getMessage(thread.getId(), createdMessage.getId()))
                    .assertNext(retrievedMessage -> {
                        assertNotNull(retrievedMessage, "Retrieved message should not be null");
                        assertEquals(createdMessage.getId(), retrievedMessage.getId(), "Message IDs should match");
                    })
                    .verifyComplete();
            })
            .verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testCreateMessageWithMetadata(HttpClient httpClient) {
        createTestAgent(httpClient);

        // Create a message with metadata
        Map<String, String> metadata = new HashMap<>();
        metadata.put("priority", "high");

        BinaryData content = BinaryData.fromString("This is a message with metadata");

        StepVerifier
            .create(messagesAsyncClient.createMessage(thread.getId(), MessageRole.USER, content, null, metadata))
            .assertNext(messageWithMetadata -> {
                assertNotNull(messageWithMetadata, "Message with metadata should not be null");
                assertNotNull(messageWithMetadata.getMetadata(), "Message metadata should not be null");
                assertEquals("high", messageWithMetadata.getMetadata().get("priority"),
                    "Metadata priority should match");
            })
            .verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testUpdateMessage(HttpClient httpClient) {
        createTestAgent(httpClient);

        // Create initial message
        BinaryData content = BinaryData.fromString("Initial message");
        ThreadMessage[] messageRef = new ThreadMessage[1];

        StepVerifier.create(messagesAsyncClient.createMessage(thread.getId(), MessageRole.USER, content))
            .assertNext(createdMessage -> {
                messageRef[0] = createdMessage;
                assertNotNull(createdMessage, "Created message should not be null");
            })
            .verifyComplete();

        // Update message metadata
        Map<String, String> updatedMetadata = new HashMap<>();
        updatedMetadata.put("updated", "true");
        updatedMetadata.put("timestamp", String.valueOf(System.currentTimeMillis()));

        StepVerifier.create(messagesAsyncClient.updateMessage(thread.getId(), messageRef[0].getId(), updatedMetadata))
            .assertNext(updatedMessage -> {
                assertNotNull(updatedMessage, "Updated message should not be null");
                assertNotNull(updatedMessage.getMetadata(), "Updated metadata should not be null");
                assertEquals("true", updatedMessage.getMetadata().get("updated"),
                    "Metadata updated flag should be true");
            })
            .verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testListMessagesDefault(HttpClient httpClient) {
        createTestAgent(httpClient);

        // Create at least two messages
        BinaryData content1 = BinaryData.fromString("Message 1");
        BinaryData content2 = BinaryData.fromString("Message 2");

        StepVerifier.create(messagesAsyncClient.createMessage(thread.getId(), MessageRole.USER, content1))
            .assertNext(message -> assertNotNull(message, "First message should not be null"))
            .verifyComplete();

        StepVerifier.create(messagesAsyncClient.createMessage(thread.getId(), MessageRole.USER, content2))
            .assertNext(message -> assertNotNull(message, "Second message should not be null"))
            .verifyComplete();

        // List messages and verify
        StepVerifier.create(messagesAsyncClient.listMessages(thread.getId()).collectList()).assertNext(messagesList -> {
            assertNotNull(messagesList, "Messages list should not be null");
            assertTrue(messagesList.size() >= 2, "There should be at least 2 messages");
        }).verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testListMessagesWithParameters(HttpClient httpClient) {
        createTestAgent(httpClient);

        // Create multiple messages
        for (int i = 0; i < 5; i++) {
            BinaryData content = BinaryData.fromString("Message " + i);

            StepVerifier.create(messagesAsyncClient.createMessage(thread.getId(), MessageRole.USER, content))
                .assertNext(message -> assertNotNull(message, "Created message should not be null"))
                .verifyComplete();
        }

        // List with parameters and verify
        StepVerifier.create(messagesAsyncClient.listMessages(thread.getId(), null, 10, null, null, null).collectList())
            .assertNext(filteredMessages -> {
                assertNotNull(filteredMessages, "Filtered messages should not be null");
                assertTrue(filteredMessages.size() <= 10, "Messages list should have at most 10 messages");
            })
            .verifyComplete();
    }

    @AfterEach
    public void cleanup() {
        if (thread != null && threadsAsyncClient != null) {
            threadsAsyncClient.deleteThread(thread.getId()).block(Duration.ofSeconds(30));
        }
        if (agent != null && administrationAsyncClient != null) {
            administrationAsyncClient.deleteAgent(agent.getId()).block(Duration.ofSeconds(30));
        }
    }
}
