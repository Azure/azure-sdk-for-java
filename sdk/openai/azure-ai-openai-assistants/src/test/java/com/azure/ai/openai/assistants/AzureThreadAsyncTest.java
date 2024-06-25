// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.assistants;

import com.azure.ai.openai.assistants.models.AssistantThread;
import com.azure.ai.openai.assistants.models.ThreadDeletionStatus;
import com.azure.ai.openai.assistants.models.UpdateAssistantThreadOptions;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AzureThreadAsyncTest extends AssistantsClientTestBase {
    private AssistantsAsyncClient client;

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void threadCRUD(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsAsyncClient(httpClient, serviceVersion);
        createRunRunner(threadCreationOptions -> {
            AtomicReference<String> threadIdReference = new AtomicReference<>();
            // Create a thread
            StepVerifier.create(client.createThread(threadCreationOptions))
                    .assertNext(assistantThread -> {
                        threadIdReference.set(assistantThread.getId());
                        assertNotNull(assistantThread.getId());
                        assertNotNull(assistantThread.getCreatedAt());
                        assertEquals("thread", assistantThread.getObject());
                    })
                    .verifyComplete();

            String threadId = threadIdReference.get();
            // Get a thread
            StepVerifier.create(client.getThread(threadId))
                    .assertNext(assistantThread -> {
                        assertEquals(threadId, assistantThread.getId());
                        assertNotNull(assistantThread.getCreatedAt());
                        assertEquals("thread", assistantThread.getObject());
                    })
                    .verifyComplete();

            // Update a thread
            Map<String, String> metadata = new HashMap<>();
            metadata.put("role", "user");
            metadata.put("name", "John Doe");
            metadata.put("content", "Hello, I'm John Doe.");
            StepVerifier.create(client.updateThread(threadId, new UpdateAssistantThreadOptions().setMetadata(metadata)))
                    .assertNext(assistantThread -> {
                        assertEquals(threadId, assistantThread.getId());
                        assertEquals("user", assistantThread.getMetadata().get("role"));
                        assertEquals("John Doe", assistantThread.getMetadata().get("name"));
                        assertEquals("Hello, I'm John Doe.", assistantThread.getMetadata().get("content"));
                    })
                    .verifyComplete();

            // Delete the created thread
            StepVerifier.create(client.deleteThread(threadId))
                    .assertNext(deletionStatus -> {
                        assertEquals(threadId, deletionStatus.getId());
                        assertTrue(deletionStatus.isDeleted());
                    })
                    .verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void threadCRUDWithResponse(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsAsyncClient(httpClient, serviceVersion);
        createRunRunner(threadCreationOptions -> {
            AtomicReference<String> threadIdReference = new AtomicReference<>();
            // Create a thread
            StepVerifier.create(client.createThreadWithResponse(
                            BinaryData.fromObject(threadCreationOptions), new RequestOptions()))
                    .assertNext(response -> {
                        AssistantThread assistantThread = assertAndGetValueFromResponse(response,
                                AssistantThread.class, 200);
                        String threadId = assistantThread.getId();
                        threadIdReference.set(threadId);
                        assertNotNull(threadId);
                        assertNotNull(assistantThread.getCreatedAt());
                        assertEquals("thread", assistantThread.getObject());
                    })
                    .verifyComplete();

            String threadId = threadIdReference.get();

            // Get a thread
            StepVerifier.create(client.getThreadWithResponse(threadId, new RequestOptions()))
                    .assertNext(response -> {
                        AssistantThread assistantThread = assertAndGetValueFromResponse(response,
                                AssistantThread.class, 200);
                        assertEquals(threadId, assistantThread.getId());
                        assertNotNull(assistantThread.getCreatedAt());
                        assertEquals("thread", assistantThread.getObject());
                    })
                    .verifyComplete();

            // Update a thread
            Map<String, String> metadata = new HashMap<>();
            metadata.put("role", "user");
            metadata.put("name", "John Doe");
            metadata.put("content", "Hello, I'm John Doe.");
            Map<String, Object> requestObj = new HashMap<>();
            requestObj.put("metadata", metadata);
            StepVerifier.create(client.updateThreadWithResponse(threadId, BinaryData.fromObject(requestObj),
                            new RequestOptions()))
                    .assertNext(response -> {
                        AssistantThread assistantThread = assertAndGetValueFromResponse(response,
                                AssistantThread.class, 200);
                        assertEquals(threadId, assistantThread.getId());
                        assertEquals("user", assistantThread.getMetadata().get("role"));
                        assertEquals("John Doe", assistantThread.getMetadata().get("name"));
                        assertEquals("Hello, I'm John Doe.", assistantThread.getMetadata().get("content"));
                    })
                    .verifyComplete();

            // Delete the created thread
            StepVerifier.create(client.deleteThreadWithResponse(threadId, new RequestOptions()))
                    .assertNext(response -> {
                        ThreadDeletionStatus deletionStatus = assertAndGetValueFromResponse(response,
                                ThreadDeletionStatus.class, 200);
                        assertEquals(threadId, deletionStatus.getId());
                        assertTrue(deletionStatus.isDeleted());
                    })
                    .verifyComplete();
        });
    }
}
