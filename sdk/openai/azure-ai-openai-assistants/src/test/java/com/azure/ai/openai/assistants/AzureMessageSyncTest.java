// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.assistants;

import com.azure.ai.openai.assistants.models.MessageRole;
import com.azure.ai.openai.assistants.models.PageableList;
import com.azure.ai.openai.assistants.models.ThreadMessage;
import com.azure.ai.openai.assistants.models.ThreadMessageOptions;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AzureMessageSyncTest extends AssistantsClientTestBase {
    private AssistantsClient client;

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void messageOperationCreateRetrieveUpdate(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient, serviceVersion);
        String threadId = createThread(client);
        createMessageRunner(message -> {
            // Create a message
            ThreadMessage threadMessage = client.createMessage(threadId, new ThreadMessageOptions(MessageRole.USER, message));
            validateThreadMessage(threadMessage, threadId);
            // Retrieve the message
            String threadMessageId = threadMessage.getId();
            ThreadMessage messageRetrieved = client.getMessage(threadId, threadMessageId);
            validateThreadMessage(messageRetrieved, threadId);
            // Update the message
            Map<String, String> metadataUpdate = new HashMap<>();
            metadataUpdate.put("role", MessageRole.ASSISTANT.toString());
            metadataUpdate.put("content", message + " Message Updated");
            ThreadMessage updatedMessage = client.updateMessage(threadId, threadMessageId, metadataUpdate);
            validateThreadMessage(updatedMessage, threadId);
            Map<String, String> metaDataResponse = updatedMessage.getMetadata();
            assertEquals(2, metaDataResponse.size());
            assertTrue(metaDataResponse.containsKey("role"));
            assertTrue(metaDataResponse.containsKey("content"));
            assertEquals(metadataUpdate.get("role"), metaDataResponse.get("role"));
            assertEquals(metadataUpdate.get("content"), metaDataResponse.get("content"));
        });
        // Delete the created thread
        deleteThread(client, threadId);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void messageResponseOperationCreateRetrieveUpdate(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient, serviceVersion);
        String threadId = createThread(client);
        createMessageRunner(message -> {
            // Create a message
            Map<String, String> metadata = new HashMap<>();
            metadata.put("role", MessageRole.USER.toString());
            metadata.put("content", message);
            BinaryData request = BinaryData.fromObject(metadata);
            Response<BinaryData> response = client.createMessageWithResponse(threadId, request, new RequestOptions());
            ThreadMessage threadMessage = assertAndGetValueFromResponse(response, ThreadMessage.class, 200);
            validateThreadMessage(threadMessage, threadId);
            // Retrieve the message
            String threadMessageId = threadMessage.getId();
            Response<BinaryData> retrievedMessageResponse = client.getMessageWithResponse(threadId, threadMessageId, new RequestOptions());
            ThreadMessage messageRetrieved = assertAndGetValueFromResponse(retrievedMessageResponse, ThreadMessage.class, 200);
            validateThreadMessage(messageRetrieved, threadId);
            // Update the message
            Map<String, String> metadataUpdate = new HashMap<>();
            metadataUpdate.put("role", MessageRole.ASSISTANT.toString());
            metadataUpdate.put("content", message + " Message Updated");
            Map<String, Object> requestObj = new HashMap<>();
            requestObj.put("metadata", metadataUpdate);
            BinaryData requestUpdate = BinaryData.fromObject(requestObj);
            Response<BinaryData> updatedMessageResponse = client.updateMessageWithResponse(threadId, threadMessageId, requestUpdate, new RequestOptions());
            ThreadMessage updatedMessage = assertAndGetValueFromResponse(updatedMessageResponse, ThreadMessage.class, 200);
            validateThreadMessage(updatedMessage, threadId);
            Map<String, String> metaDataResponse = updatedMessage.getMetadata();
            assertEquals(2, metaDataResponse.size());
            assertTrue(metaDataResponse.containsKey("role"));
            assertTrue(metaDataResponse.containsKey("content"));
            assertEquals(metadataUpdate.get("role"), metaDataResponse.get("role"));
            assertEquals(metadataUpdate.get("content"), metaDataResponse.get("content"));
        });
        // Delete the created thread
        deleteThread(client, threadId);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void listMessages(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient, serviceVersion);
        String threadId = createThread(client);
        createMessageRunner(message -> {
            // Create two messages in user role
            ThreadMessage threadMessage = client.createMessage(threadId, new ThreadMessageOptions(MessageRole.USER, message));
            validateThreadMessage(threadMessage, threadId);
            ThreadMessage threadMessage2 = client.createMessage(threadId, new ThreadMessageOptions(MessageRole.USER, message + "second message"));
            validateThreadMessage(threadMessage2, threadId);
            // List messages
            PageableList<ThreadMessage> listedMessages = client.listMessages(threadId);
            assertNotNull(listedMessages);
            assertNotNull(listedMessages.getData());
            assertEquals(2, listedMessages.getData().size());
            // List messages with response
            Response<BinaryData> listedMessagesResponse = client.listMessagesWithResponse(threadId, new RequestOptions());
            PageableList<ThreadMessage> listedMessagesWithResponse = asserAndGetPageableListFromResponse(
                listedMessagesResponse, 200, reader -> reader.readArray(ThreadMessage::fromJson));
            assertNotNull(listedMessagesWithResponse);
            assertNotNull(listedMessagesWithResponse.getData());
            assertEquals(2, listedMessagesWithResponse.getData().size());
        });
        // Delete the created thread
        deleteThread(client, threadId);
    }
}
