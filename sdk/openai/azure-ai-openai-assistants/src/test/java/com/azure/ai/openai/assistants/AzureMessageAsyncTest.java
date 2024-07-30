// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.assistants;

import com.azure.ai.openai.assistants.models.MessageRole;
import com.azure.ai.openai.assistants.models.PageableList;
import com.azure.ai.openai.assistants.models.ThreadMessage;
import com.azure.ai.openai.assistants.models.ThreadMessageOptions;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.ai.openai.assistants.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AzureMessageAsyncTest extends AssistantsClientTestBase {
    private AssistantsAsyncClient client;

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void messageOperationCreateRetrieveUpdate(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsAsyncClient(httpClient, serviceVersion);
        String threadId = createThread(client);
        createMessageRunner(message -> {
            AtomicReference<String> threadMessageIdReference = new AtomicReference<>();
            // Create a message
            StepVerifier.create(client.createMessage(threadId, new ThreadMessageOptions(MessageRole.USER, message)))
                    .assertNext(threadMessage -> {
                        String threadMessageId = threadMessage.getId();
                        assertNotNull(threadMessageId);
                        threadMessageIdReference.set(threadMessageId);
                        assertNotNull(threadMessage.getCreatedAt());
                        assertEquals("thread.message", threadMessage.getObject());
                        assertEquals(MessageRole.USER, threadMessage.getRole());
                        assertFalse(threadMessage.getContent().isEmpty());
                        assertEquals(threadId, threadMessage.getThreadId());
                    })
                    .verifyComplete();
            String threadMessageId = threadMessageIdReference.get();
            // Retrieve the message
            StepVerifier.create(client.getMessage(threadId, threadMessageId))
                    .assertNext(messageRetrieved -> {
                        assertEquals(threadMessageId, messageRetrieved.getId());
                        assertEquals(threadId, messageRetrieved.getThreadId());
                        assertNotNull(messageRetrieved.getCreatedAt());
                        assertEquals("thread.message", messageRetrieved.getObject());
                        assertEquals(MessageRole.USER, messageRetrieved.getRole());
                        assertFalse(messageRetrieved.getContent().isEmpty());
                    })
                    .verifyComplete();
            // Update the message
            Map<String, String> metadataUpdate = new HashMap<>();
            metadataUpdate.put("role", MessageRole.ASSISTANT.toString());
            metadataUpdate.put("content", message + " Message Updated");
            StepVerifier.create(client.updateMessage(threadId, threadMessageId, metadataUpdate))
                    .assertNext(updatedMessage -> {
                        assertEquals(threadMessageId, updatedMessage.getId());
                        assertEquals(threadId, updatedMessage.getThreadId());
                        assertNotNull(updatedMessage.getCreatedAt());
                        assertEquals("thread.message", updatedMessage.getObject());
                        assertEquals(MessageRole.USER, updatedMessage.getRole());
                        assertFalse(updatedMessage.getContent().isEmpty());
                        Map<String, String> metaDataResponse = updatedMessage.getMetadata();
                        assertEquals(2, metaDataResponse.size());
                        assertTrue(metaDataResponse.containsKey("role"));
                        assertTrue(metaDataResponse.containsKey("content"));
                        assertEquals(metadataUpdate.get("role"), metaDataResponse.get("role"));
                        assertEquals(metadataUpdate.get("content"), metaDataResponse.get("content"));
                    })
                    .verifyComplete();
        });
        // Delete the created thread
        deleteThread(client, threadId);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void messageResponseOperationCreateRetrieveUpdate(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsAsyncClient(httpClient, serviceVersion);
        String threadId = createThread(client);
        createMessageRunner(message -> {
            AtomicReference<String> threadMessageIdReference = new AtomicReference<>();
            // Create a message
            Map<String, String> metadata = new HashMap<>();
            metadata.put("role", MessageRole.USER.toString());
            metadata.put("content", message);
            BinaryData request = BinaryData.fromObject(metadata);
            StepVerifier.create(client.createMessageWithResponse(threadId, request, new RequestOptions()))
                    .assertNext(response -> {
                        ThreadMessage threadMessage = assertAndGetValueFromResponse(response, ThreadMessage.class, 200);
                        String threadMessageId = threadMessage.getId();
                        threadMessageIdReference.set(threadMessageId);
                        assertNotNull(threadMessageId);
                        assertNotNull(threadMessage.getCreatedAt());
                        assertEquals("thread.message", threadMessage.getObject());
                        assertEquals(MessageRole.USER, threadMessage.getRole());
                        assertFalse(threadMessage.getContent().isEmpty());
                        assertEquals(threadId, threadMessage.getThreadId());
                        threadMessageIdReference.set(threadMessageId);
                    })
                    .verifyComplete();
            String threadMessageId = threadMessageIdReference.get();
            // Retrieve the message
            StepVerifier.create(client.getMessageWithResponse(threadId, threadMessageId, new RequestOptions()))
                    .assertNext(response -> {
                        ThreadMessage messageRetrieved = assertAndGetValueFromResponse(response, ThreadMessage.class, 200);
                        assertEquals(threadMessageId, messageRetrieved.getId());
                        assertEquals(threadId, messageRetrieved.getThreadId());
                        assertNotNull(messageRetrieved.getCreatedAt());
                        assertEquals("thread.message", messageRetrieved.getObject());
                        assertEquals(MessageRole.USER, messageRetrieved.getRole());
                        assertFalse(messageRetrieved.getContent().isEmpty());
                    })
                    .verifyComplete();
            // Update the message
            Map<String, String> metadataUpdate = new HashMap<>();
            metadataUpdate.put("role", MessageRole.ASSISTANT.toString());
            metadataUpdate.put("content", message + " Message Updated");
            Map<String, Object> requestObj = new HashMap<>();
            requestObj.put("metadata", metadataUpdate);
            BinaryData requestUpdate = BinaryData.fromObject(requestObj);
            StepVerifier.create(client.updateMessageWithResponse(threadId, threadMessageId, requestUpdate, new RequestOptions()))
                    .assertNext(response -> {
                        ThreadMessage updatedMessage = assertAndGetValueFromResponse(response, ThreadMessage.class, 200);
                        assertEquals(threadMessageId, updatedMessage.getId());
                        assertEquals(threadId, updatedMessage.getThreadId());
                        assertNotNull(updatedMessage.getCreatedAt());
                        assertEquals("thread.message", updatedMessage.getObject());
                        assertEquals(MessageRole.USER, updatedMessage.getRole());
                        assertFalse(updatedMessage.getContent().isEmpty());
                        Map<String, String> metaDataResponse = updatedMessage.getMetadata();
                        assertEquals(2, metaDataResponse.size());
                        assertTrue(metaDataResponse.containsKey("role"));
                        assertTrue(metaDataResponse.containsKey("content"));
                        assertEquals(metadataUpdate.get("role"), metaDataResponse.get("role"));
                        assertEquals(metadataUpdate.get("content"), metaDataResponse.get("content"));
                    })
                    .verifyComplete();
        });
        // Delete the created thread
        deleteThread(client, threadId);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void listMessages(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsAsyncClient(httpClient, serviceVersion);
        String threadId = createThread(client);
        createMessageRunner(message -> {
            // Create two messages in user role
            StepVerifier.create(client.createMessage(threadId, new ThreadMessageOptions(MessageRole.USER, message)))
                    .assertNext(threadMessage -> {
                        String threadMessageId = threadMessage.getId();
                        assertNotNull(threadMessageId);
                        assertEquals(threadId, threadMessage.getThreadId());
                        assertNotNull(threadMessage.getCreatedAt());
                        assertEquals("thread.message", threadMessage.getObject());
                        assertEquals(MessageRole.USER, threadMessage.getRole());
                        assertFalse(threadMessage.getContent().isEmpty());
                    })
                    .verifyComplete();
            StepVerifier.create(client.createMessage(threadId, new ThreadMessageOptions(MessageRole.USER, message + "second message")))
                    .assertNext(threadMessage -> {
                        String threadMessageId = threadMessage.getId();
                        assertNotNull(threadMessageId);
                        assertEquals(threadId, threadMessage.getThreadId());
                        assertNotNull(threadMessage.getCreatedAt());
                        assertEquals("thread.message", threadMessage.getObject());
                        assertEquals(MessageRole.USER, threadMessage.getRole());
                        assertFalse(threadMessage.getContent().isEmpty());
                    })
                    .verifyComplete();
            // List messages
            StepVerifier.create(client.listMessages(threadId))
                    .assertNext(listedMessages -> {
                        assertNotNull(listedMessages);
                        assertNotNull(listedMessages.getData());
                        assertEquals(2, listedMessages.getData().size());
                    })
                    .verifyComplete();
            // List messages with response
            StepVerifier.create(client.listMessagesWithResponse(threadId, new RequestOptions()))
                    .assertNext(response -> {
                        PageableList<ThreadMessage> listedMessagesWithResponse = asserAndGetPageableListFromResponse(
                            response, 200, reader -> reader.readArray(ThreadMessage::fromJson));
                        assertNotNull(listedMessagesWithResponse);
                        assertNotNull(listedMessagesWithResponse.getData());
                        assertEquals(2, listedMessagesWithResponse.getData().size());
                    })
                    .verifyComplete();
        });
        // Delete the created thread
        deleteThread(client, threadId);
    }
}
