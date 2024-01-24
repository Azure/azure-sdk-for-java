// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.assistants;

import com.azure.ai.openai.assistants.models.Assistant;
import com.azure.ai.openai.assistants.models.AssistantCreationOptions;
import com.azure.ai.openai.assistants.models.AssistantDeletionStatus;
import com.azure.ai.openai.assistants.models.AssistantThreadCreationOptions;
import com.azure.ai.openai.assistants.models.MessageRole;
import com.azure.ai.openai.assistants.models.ThreadMessage;
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
    private Assistant mathTutorAssistant;
    @Override
    protected void beforeTest() {
        client = getAssistantsAsyncClient(HttpClient.createDefault());

        // Create a Math tutor assistant
        AssistantCreationOptions assistantCreationOptions = new AssistantCreationOptions(GPT_4_1106_PREVIEW)
                .setName("Math Tutor")
                .setInstructions("You are a personal math tutor. Answer questions briefly, in a sentence or less.");
        StepVerifier.create(client.createAssistantWithResponse(BinaryData.fromObject(assistantCreationOptions), new RequestOptions()))
                .assertNext(response -> {
                    mathTutorAssistant = assertAndGetValueFromResponse(response, Assistant.class, 200);
                    assertEquals(assistantCreationOptions.getName(), mathTutorAssistant.getName());
                    assertEquals(assistantCreationOptions.getDescription(), mathTutorAssistant.getDescription());
                    assertEquals(assistantCreationOptions.getInstructions(), mathTutorAssistant.getInstructions());
                });

    }

    @Override
    protected void afterTest() {
        if (mathTutorAssistant != null) {
            StepVerifier.create(client.deleteAssistantWithResponse(mathTutorAssistant.getId(), new RequestOptions()))
                    .assertNext(response -> {
                        AssistantDeletionStatus deletionStatus = assertAndGetValueFromResponse(response,
                                AssistantDeletionStatus.class, 200);
                        assertEquals(mathTutorAssistant.getId(), deletionStatus.getId());
                        assertTrue(deletionStatus.isDeleted());
                    });
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void createMessage(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getAssistantsAsyncClient(httpClient, serviceVersion);
        createMessageRunner(message -> {
            AtomicReference<String> threadIdReference = new AtomicReference<>();
            // Create a simple thread without a message
            StepVerifier.create(client.createThread(new AssistantThreadCreationOptions()))
                    .assertNext(assistantThread -> {
                        assertNotNull(assistantThread.getId());
                        assertNotNull(assistantThread.getCreatedAt());
                        assertEquals("thread", assistantThread.getObject());
                        threadIdReference.set(assistantThread.getId());
                    })
                    .verifyComplete();

            String threadId = threadIdReference.get();

            StepVerifier.create(client.createMessage(threadId, MessageRole.USER, message))
                    .assertNext(threadMessage -> {
                        assertNotNull(threadMessage.getId());
                        assertNotNull(threadMessage.getCreatedAt());
                        assertEquals("thread.message", threadMessage.getObject());
                        assertEquals(MessageRole.USER, threadMessage.getRole());
                        assertFalse(threadMessage.getContent().isEmpty());
                        assertEquals(threadId, threadMessage.getThreadId());
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
    public void createMessageWithResponse(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getAssistantsAsyncClient(httpClient, serviceVersion);
        createMessageRunner(message -> {
            AtomicReference<String> threadIdReference = new AtomicReference<>();

            // Create a simple thread without a message
            StepVerifier.create(client.createThread(new AssistantThreadCreationOptions()))
                    .assertNext(assistantThread -> {
                        assertNotNull(assistantThread.getId());
                        assertNotNull(assistantThread.getCreatedAt());
                        assertEquals("thread", assistantThread.getObject());
                        threadIdReference.set(assistantThread.getId());
                    })
                    .verifyComplete();

            String threadId = threadIdReference.get();


            Map<String, Object> requestObj = new HashMap<>();
            requestObj.put("role", MessageRole.USER);
            requestObj.put("content", message);
            BinaryData request = BinaryData.fromObject(requestObj);

            StepVerifier.create(client.createMessageWithResponse(threadId, request, new RequestOptions()))
                    .assertNext(response -> {
                        ThreadMessage threadMessage = assertAndGetValueFromResponse(response, ThreadMessage.class, 200);
                        assertNotNull(threadMessage.getId());
                        assertNotNull(threadMessage.getCreatedAt());
                        assertEquals("thread.message", threadMessage.getObject());
                        assertEquals(MessageRole.USER, threadMessage.getRole());
                        assertFalse(threadMessage.getContent().isEmpty());
                        assertEquals(threadId, threadMessage.getThreadId());
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
}
