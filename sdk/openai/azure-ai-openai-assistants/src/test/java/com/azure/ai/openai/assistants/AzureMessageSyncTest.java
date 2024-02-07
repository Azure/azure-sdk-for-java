// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.assistants;

import com.azure.ai.openai.assistants.models.ListSortOrder;
import com.azure.ai.openai.assistants.models.MessageFile;
import com.azure.ai.openai.assistants.models.MessageRole;
import com.azure.ai.openai.assistants.models.OpenAIPageableListOfMessageFile;
import com.azure.ai.openai.assistants.models.OpenAIPageableListOfThreadMessage;
import com.azure.ai.openai.assistants.models.PagedResult;
import com.azure.ai.openai.assistants.models.ThreadMessage;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.serializer.TypeReference;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
            ThreadMessage threadMessage = client.createMessage(threadId, MessageRole.USER, message);
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
            ThreadMessage threadMessage = client.createMessage(threadId, MessageRole.USER, message);
            validateThreadMessage(threadMessage, threadId);
            ThreadMessage threadMessage2 = client.createMessage(threadId, MessageRole.USER, message + "second message");
            validateThreadMessage(threadMessage2, threadId);
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
        });
        // Delete the created thread
        deleteThread(client, threadId);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void getMessageFile(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient, serviceVersion);
        String threadId = createThread(client);
        String fileId = uploadFile(client);
        createMessageRunner(message -> {
            ThreadMessage threadMessage = client.createMessage(threadId, MessageRole.USER, message,
                    Arrays.asList(fileId), null);
            validateThreadMessage(threadMessage, threadId);
            String messageId = threadMessage.getId();
            // Retrieve the message file
            MessageFile messageFile = client.getMessageFile(threadId, messageId, fileId);
            validateMessageFile(messageFile, messageId, fileId);
            // Retrieve the message file with response
            Response<BinaryData> messageFileWithResponse = client.getMessageFileWithResponse(threadId, messageId, fileId, new RequestOptions());
            MessageFile messageFileResponse = assertAndGetValueFromResponse(messageFileWithResponse, MessageFile.class, 200);
            validateMessageFile(messageFileResponse, messageId, fileId);

        });
        deleteFile(client, fileId);
        deleteThread(client, threadId);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void listMessageFiles(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient, serviceVersion);
        String threadId = createThread(client);
        String fileId1 = uploadFile(client);
        String fileId2 = uploadFile(client);
        createMessageRunner(message -> {
            ThreadMessage threadMessage = client.createMessage(threadId, MessageRole.USER, message,
                    Arrays.asList(fileId1, fileId2), null);
            validateThreadMessage(threadMessage, threadId);
            String messageId = threadMessage.getId();
            // List message files
            PagedResult<MessageFile> listMessageFiles = client.listMessageFiles(threadId, messageId);
            validateOpenAIPageableListOfMessageFile(listMessageFiles, messageId, Arrays.asList(fileId1, fileId2));
            // List messages with response
            Response<BinaryData> listedMessagesResponse = client.listMessageFilesWithResponse(threadId, messageId, new RequestOptions());
            PagedResult<MessageFile> listMessageFilesResponse = assertAndGetValueFromResponse(
                listedMessagesResponse, new TypeReference<PagedResult<MessageFile>>() {}, 200);
            validateOpenAIPageableListOfMessageFile(listMessageFilesResponse, messageId, Arrays.asList(fileId1, fileId2));
        });
        deleteFile(client, fileId1);
        deleteFile(client, fileId2);
        deleteThread(client, threadId);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void listMessageFilesBetweenTwoFileId(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient, serviceVersion);
        String threadId = createThread(client);
        String fileId1 = uploadFile(client);
        String fileId2 = uploadFile(client);
        String fileId3 = uploadFile(client);
        String fileId4 = uploadFile(client);
        createMessageRunner(message -> {
            ThreadMessage threadMessage = client.createMessage(threadId, MessageRole.USER, message,
                    Arrays.asList(fileId1, fileId2, fileId3, fileId4), null);
            validateThreadMessage(threadMessage, threadId);
            String messageId = threadMessage.getId();
            // List message files between two file ids
            PagedResult<MessageFile> listMessageFiles = client.listMessageFiles(threadId, messageId, 10,
                    ListSortOrder.ASCENDING, fileId1, fileId4);
            List<MessageFile> dataAscending = listMessageFiles.getData();
            assertEquals(2, dataAscending.size());
            validateOpenAIPageableListOfMessageFile(listMessageFiles, messageId, Arrays.asList(fileId2, fileId3));
        });
        deleteFile(client, fileId1);
        deleteFile(client, fileId2);
        deleteFile(client, fileId3);
        deleteFile(client, fileId4);
        deleteThread(client, threadId);
    }
}
