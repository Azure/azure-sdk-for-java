// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.assistants;

import com.azure.ai.openai.assistants.models.ListSortOrder;
import com.azure.ai.openai.assistants.models.MessageFile;
import com.azure.ai.openai.assistants.models.MessageRole;
import com.azure.ai.openai.assistants.models.PageableList;
import com.azure.ai.openai.assistants.models.ThreadMessage;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;
import com.azure.core.util.serializer.TypeReference;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
            StepVerifier.create(client.createMessage(threadId, MessageRole.USER, message))
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
            StepVerifier.create(client.createMessage(threadId, MessageRole.USER, message))
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
            StepVerifier.create(client.createMessage(threadId, MessageRole.USER, message + "second message"))
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
                        PageableList<ThreadMessage> listedMessagesWithResponse = assertAndGetValueFromResponse(
                            response, new TypeReference<PageableList<ThreadMessage>>() {}, 200);
                        assertNotNull(listedMessagesWithResponse);
                        assertNotNull(listedMessagesWithResponse.getData());
                        assertEquals(2, listedMessagesWithResponse.getData().size());
                    })
                    .verifyComplete();
        });
        // Delete the created thread
        deleteThread(client, threadId);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void getMessageFile(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsAsyncClient(httpClient, serviceVersion);
        String threadId = createThread(client);
        String fileId = uploadFile(client);
        createMessageRunner(message -> {
            AtomicReference<String> messageIdRef = new AtomicReference<>();
            StepVerifier.create(client.createMessage(threadId, MessageRole.USER, message, Arrays.asList(fileId), null))
                    .assertNext(threadMessage -> {
                        validateThreadMessage(threadMessage, threadId);
                        messageIdRef.set(threadMessage.getId());
                    })
                    .verifyComplete();
            String messageId = messageIdRef.get();
            // Retrieve the message file
            StepVerifier.create(client.getMessageFile(threadId, messageId, fileId))
                    .assertNext(messageFile -> validateMessageFile(messageFile, messageId, fileId))
                    .verifyComplete();
            // Retrieve the message file with response
            StepVerifier.create(client.getMessageFileWithResponse(threadId, messageId, fileId, new RequestOptions()))
                    .assertNext(response -> {
                        MessageFile messageFileWithResponse = assertAndGetValueFromResponse(response, MessageFile.class, 200);
                        validateMessageFile(messageFileWithResponse, messageId, fileId);
                    })
                    .verifyComplete();
        });
        deleteFile(client, fileId);
        deleteThread(client, threadId);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void listMessageFiles(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsAsyncClient(httpClient, serviceVersion);
        String threadId = createThread(client);
        String fileId1 = uploadFile(client);
        String fileId2 = uploadFile(client);
        createMessageRunner(message -> {
            AtomicReference<String> messageIdRef = new AtomicReference<>();
            StepVerifier.create(client.createMessage(threadId, MessageRole.USER, message, Arrays.asList(fileId1, fileId2),
                            null))
                    .assertNext(threadMessage -> {
                        validateThreadMessage(threadMessage, threadId);
                        messageIdRef.set(threadMessage.getId());
                    })
                    .verifyComplete();
            String messageId = messageIdRef.get();
            // List message files
            StepVerifier.create(client.listMessageFiles(threadId, messageId))
                    .assertNext(listMessageFiles -> validateOpenAIPageableListOfMessageFile(listMessageFiles, messageId,
                            Arrays.asList(fileId1, fileId2)))
                    .verifyComplete();
            // List messages with response
            StepVerifier.create(client.listMessageFilesWithResponse(threadId, messageId, new RequestOptions()))
                    .assertNext(response -> {
                        PageableList<MessageFile> listMessageFilesResponse = assertAndGetValueFromResponse(
                            response, new TypeReference<PageableList<MessageFile>>() {}, 200);
                        validateOpenAIPageableListOfMessageFile(listMessageFilesResponse, messageId,
                                Arrays.asList(fileId1, fileId2));
                    })
                    .verifyComplete();
        });
        deleteFile(client, fileId1);
        deleteFile(client, fileId2);
        deleteThread(client, threadId);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void listMessageFilesBetweenTwoFileId(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsAsyncClient(httpClient, serviceVersion);
        String threadId = createThread(client);
        String fileId1 = uploadFile(client);
        String fileId2 = uploadFile(client);
        String fileId3 = uploadFile(client);
        String fileId4 = uploadFile(client);
        createMessageRunner(message -> {
            AtomicReference<String> messageIdRef = new AtomicReference<>();
            StepVerifier.create(client.createMessage(threadId, MessageRole.USER, message,
                            Arrays.asList(fileId1, fileId2, fileId3, fileId4), null))
                    .assertNext(threadMessage -> {
                        validateThreadMessage(threadMessage, threadId);
                        messageIdRef.set(threadMessage.getId());
                    })
                    .verifyComplete();
            String messageId = messageIdRef.get();
            // List message files between two file ids
            StepVerifier.create(client.listMessageFiles(threadId, messageId, 10,
                    ListSortOrder.ASCENDING, fileId1, fileId4))
                    .assertNext(listMessageFiles -> {
                        List<MessageFile> dataAscending = listMessageFiles.getData();
                        assertEquals(2, dataAscending.size());
                        validateOpenAIPageableListOfMessageFile(listMessageFiles, messageId,
                                Arrays.asList(fileId2, fileId3));
                    })
                    .verifyComplete();
        });
        deleteFile(client, fileId1);
        deleteFile(client, fileId2);
        deleteFile(client, fileId3);
        deleteFile(client, fileId4);
        deleteThread(client, threadId);
    }
}
