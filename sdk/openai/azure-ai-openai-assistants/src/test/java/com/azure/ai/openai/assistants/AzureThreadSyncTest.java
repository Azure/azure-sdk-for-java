// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.assistants;

import com.azure.ai.openai.assistants.models.AssistantThread;
import com.azure.ai.openai.assistants.models.ThreadDeletionStatus;
import com.azure.ai.openai.assistants.models.UpdateAssistantThreadOptions;
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

public class AzureThreadSyncTest extends AssistantsClientTestBase {
    private AssistantsClient client;

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void threadCRUD(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient, serviceVersion);
        createRunRunner(threadCreationOptions -> {
            // Create a thread
            AssistantThread assistantThread = client.createThread(threadCreationOptions);
            String threadId = assistantThread.getId();
            assertNotNull(threadId);
            assertNotNull(assistantThread.getCreatedAt());
            assertEquals("thread", assistantThread.getObject());

            // Get a thread
            AssistantThread retrievedThread = client.getThread(threadId);
            assertEquals(threadId, retrievedThread.getId());
            assertNotNull(retrievedThread.getCreatedAt());
            assertEquals("thread", retrievedThread.getObject());

            // Update a thread
            Map<String, String> metadata = new HashMap<>();
            metadata.put("role", "user");
            metadata.put("name", "John Doe");
            metadata.put("content", "Hello, I'm John Doe.");
            AssistantThread updatedThread = client.updateThread(assistantThread.getId(), new UpdateAssistantThreadOptions().setMetadata(metadata));
            assertEquals(threadId, updatedThread.getId());
            assertEquals("user", updatedThread.getMetadata().get("role"));
            assertEquals("John Doe", updatedThread.getMetadata().get("name"));
            assertEquals("Hello, I'm John Doe.", updatedThread.getMetadata().get("content"));

            // Delete the created thread
            ThreadDeletionStatus threadDeletionStatus = client.deleteThread(threadId);
            assertEquals(threadId, threadDeletionStatus.getId());
            assertTrue(threadDeletionStatus.isDeleted());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void threadCRUDWithResponse(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient, serviceVersion);
        createRunRunner(threadCreationOptions -> {
            // Create a thread
            Response<BinaryData> response = client.createThreadWithResponse(
                    BinaryData.fromObject(threadCreationOptions), new RequestOptions());
            AssistantThread assistantThread = assertAndGetValueFromResponse(response, AssistantThread.class, 200);
            String threadId = assistantThread.getId();
            assertNotNull(threadId);
            assertNotNull(assistantThread.getCreatedAt());
            assertEquals("thread", assistantThread.getObject());

            // Get a thread
            Response<BinaryData> retrievedThreadResponse = client.getThreadWithResponse(threadId, new RequestOptions());
            AssistantThread retrievedThread = assertAndGetValueFromResponse(retrievedThreadResponse, AssistantThread.class, 200);
            assertEquals(threadId, retrievedThread.getId());
            assertNotNull(retrievedThread.getCreatedAt());
            assertEquals("thread", retrievedThread.getObject());

            // Update a thread
            Map<String, String> metadata = new HashMap<>();
            metadata.put("role", "user");
            metadata.put("name", "John Doe");
            metadata.put("content", "Hello, I'm John Doe.");
            Map<String, Object> requestObj = new HashMap<>();
            requestObj.put("metadata", metadata);
            Response<BinaryData> updateThreadWithResponse = client.updateThreadWithResponse(threadId, BinaryData.fromObject(requestObj),
                    new RequestOptions());
            AssistantThread updatedThread = assertAndGetValueFromResponse(updateThreadWithResponse, AssistantThread.class, 200);
            assertEquals(threadId, updatedThread.getId());
            assertEquals("user", updatedThread.getMetadata().get("role"));
            assertEquals("John Doe", updatedThread.getMetadata().get("name"));
            assertEquals("Hello, I'm John Doe.", updatedThread.getMetadata().get("content"));

            // Delete the created thread
            Response<BinaryData> deletedThreadWithResponse = client.deleteThreadWithResponse(threadId, new RequestOptions());
            ThreadDeletionStatus deletionStatus = assertAndGetValueFromResponse(deletedThreadWithResponse, ThreadDeletionStatus.class, 200);
            assertEquals(threadId, deletionStatus.getId());
            assertTrue(deletionStatus.isDeleted());
        });
    }
}
