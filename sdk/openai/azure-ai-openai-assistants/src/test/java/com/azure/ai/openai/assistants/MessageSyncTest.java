// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.assistants;

import com.azure.ai.openai.assistants.models.AssistantThread;
import com.azure.ai.openai.assistants.models.AssistantThreadCreationOptions;
import com.azure.ai.openai.assistants.models.MessageRole;
import com.azure.ai.openai.assistants.models.OpenAIPageableListOfThreadMessage;
import com.azure.ai.openai.assistants.models.ThreadDeletionStatus;
import com.azure.ai.openai.assistants.models.ThreadMessage;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashMap;
import java.util.Map;

import static com.azure.ai.openai.assistants.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MessageSyncTest extends AssistantsClientTestBase {
    private AssistantsClient client;

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void messageOperationCreateRetrieveUpdate(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient);
        createMessageRunner(message -> {
            // Create a simple thread without a message
            AssistantThread assistantThread = client.createThread(new AssistantThreadCreationOptions());
            String threadId = assistantThread.getId();
            assertNotNull(assistantThread.getId());
            assertNotNull(assistantThread.getCreatedAt());
            assertEquals("thread", assistantThread.getObject());
            // Create a message
            ThreadMessage threadMessage = client.createMessage(threadId, MessageRole.USER, message);
            String threadMessageId = threadMessage.getId();
            assertNotNull(threadMessageId);
            assertEquals(threadId, threadMessage.getThreadId());
            assertNotNull(threadMessage.getCreatedAt());
            assertEquals("thread.message", threadMessage.getObject());
            assertEquals(MessageRole.USER, threadMessage.getRole());
            assertFalse(threadMessage.getContent().isEmpty());
            // Retrieve the message
            ThreadMessage messageRetrieved = client.getMessage(threadId, threadMessageId);
            assertEquals(threadMessageId, messageRetrieved.getId());
            assertEquals(threadId, messageRetrieved.getThreadId());
            assertNotNull(messageRetrieved.getCreatedAt());
            assertEquals("thread.message", messageRetrieved.getObject());
            assertEquals(MessageRole.USER, messageRetrieved.getRole());
            assertFalse(messageRetrieved.getContent().isEmpty());
            // Update the message
            Map<String, String> metadataUpdate = new HashMap<>();
            metadataUpdate.put("role", MessageRole.ASSISTANT.toString());
            metadataUpdate.put("content", message + " Message Updated");
            ThreadMessage updatedMessage = client.updateMessage(threadId, threadMessageId, metadataUpdate);
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
            // Delete the created thread
            ThreadDeletionStatus threadDeletionStatus = client.deleteThread(threadId);
            assertEquals(threadId, threadDeletionStatus.getId());
            assertTrue(threadDeletionStatus.isDeleted());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void messageResponseOperationCreateRetrieveUpdate(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient);
        createMessageRunner(message -> {
            // Create a simple thread without a message
            AssistantThread assistantThread = client.createThread(new AssistantThreadCreationOptions());
            String threadId = assistantThread.getId();
            assertNotNull(threadId);
            assertNotNull(assistantThread.getCreatedAt());
            assertEquals("thread", assistantThread.getObject());
            // Create a message
            Map<String, String> metadata = new HashMap<>();
            metadata.put("role", MessageRole.USER.toString());
            metadata.put("content", message);
            BinaryData request = BinaryData.fromObject(metadata);
            Response<BinaryData> response = client.createMessageWithResponse(threadId, request, new RequestOptions());
            ThreadMessage threadMessage = assertAndGetValueFromResponse(response, ThreadMessage.class, 200);
            String threadMessageId = threadMessage.getId();
            assertNotNull(threadMessageId);
            assertNotNull(threadMessage.getCreatedAt());
            assertEquals("thread.message", threadMessage.getObject());
            assertEquals(MessageRole.USER, threadMessage.getRole());
            assertFalse(threadMessage.getContent().isEmpty());
            assertEquals(threadId, threadMessage.getThreadId());
            // Retrieve the message
            Response<BinaryData> retrievedMessageResponse = client.getMessageWithResponse(threadId, threadMessageId, new RequestOptions());
            ThreadMessage messageRetrieved = assertAndGetValueFromResponse(retrievedMessageResponse, ThreadMessage.class, 200);
            assertEquals(threadMessageId, messageRetrieved.getId());
            assertEquals(threadId, messageRetrieved.getThreadId());
            assertNotNull(messageRetrieved.getCreatedAt());
            assertEquals("thread.message", messageRetrieved.getObject());
            assertEquals(MessageRole.USER, messageRetrieved.getRole());
            assertFalse(messageRetrieved.getContent().isEmpty());
            // Update the message
            Map<String, String> metadataUpdate = new HashMap<>();
            metadataUpdate.put("role", MessageRole.ASSISTANT.toString());
            metadataUpdate.put("content", message + " Message Updated");
            Map<String, Object> requestObj = new HashMap<>();
            requestObj.put("metadata", metadataUpdate);
            BinaryData requestUpdate = BinaryData.fromObject(requestObj);
            Response<BinaryData> updatedMessageResponse = client.updateMessageWithResponse(threadId, threadMessageId, requestUpdate, new RequestOptions());
            ThreadMessage updatedMessage = assertAndGetValueFromResponse(updatedMessageResponse, ThreadMessage.class, 200);
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
            // Delete the created thread
            ThreadDeletionStatus threadDeletionStatus = client.deleteThread(threadId);
            assertEquals(threadId, threadDeletionStatus.getId());
            assertTrue(threadDeletionStatus.isDeleted());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void listMessages(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient);
        createMessageRunner(message -> {
            // Create a simple thread without a message
            AssistantThread assistantThread = client.createThread(new AssistantThreadCreationOptions());
            String threadId = assistantThread.getId();
            assertNotNull(assistantThread.getId());
            assertNotNull(assistantThread.getCreatedAt());
            assertEquals("thread", assistantThread.getObject());
            // Create two messages in user role
            ThreadMessage threadMessage = client.createMessage(threadId, MessageRole.USER, message);
            String threadMessageId = threadMessage.getId();
            assertNotNull(threadMessageId);
            assertEquals(threadId, threadMessage.getThreadId());
            assertNotNull(threadMessage.getCreatedAt());
            assertEquals("thread.message", threadMessage.getObject());
            assertEquals(MessageRole.USER, threadMessage.getRole());
            assertFalse(threadMessage.getContent().isEmpty());
            ThreadMessage threadMessage2 = client.createMessage(threadId, MessageRole.USER, message + "second message");
            String threadMessageId2 = threadMessage2.getId();
            assertNotNull(threadMessageId2);
            assertEquals(threadId, threadMessage2.getThreadId());
            assertNotNull(threadMessage2.getCreatedAt());
            assertEquals("thread.message", threadMessage2.getObject());
            assertEquals(MessageRole.USER, threadMessage2.getRole());
            assertFalse(threadMessage2.getContent().isEmpty());
            // List messages
            OpenAIPageableListOfThreadMessage listedMessages = client.listMessages(threadId);
            assertNotNull(listedMessages);
            assertNotNull(listedMessages.getData());
            assertEquals(2, listedMessages.getData().size());
            // List messages with response
            Response<BinaryData> listedMessagesResponse = client.listMessagesWithResponse(threadId, new RequestOptions());
            OpenAIPageableListOfThreadMessage listedMessagesWithResponse = assertAndGetValueFromResponse(
                    listedMessagesResponse, OpenAIPageableListOfThreadMessage.class, 200);
            assertNotNull(listedMessagesWithResponse);
            assertNotNull(listedMessagesWithResponse.getData());
            assertEquals(2, listedMessagesWithResponse.getData().size());
            // Delete the created thread
            ThreadDeletionStatus threadDeletionStatus = client.deleteThread(threadId);
            assertEquals(threadId, threadDeletionStatus.getId());
            assertTrue(threadDeletionStatus.isDeleted());
        });
    }
}
