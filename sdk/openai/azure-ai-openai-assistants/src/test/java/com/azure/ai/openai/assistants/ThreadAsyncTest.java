// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.assistants;

import com.azure.ai.openai.assistants.models.Assistant;
import com.azure.ai.openai.assistants.models.AssistantCreationOptions;
import com.azure.ai.openai.assistants.models.AssistantDeletionStatus;
import com.azure.ai.openai.assistants.models.AssistantThread;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.util.concurrent.atomic.AtomicReference;

import static com.azure.ai.openai.assistants.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ThreadAsyncTest extends AssistantsClientTestBase {
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
                })
                .verifyComplete();

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
                    })
                    .verifyComplete();
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void createThread(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getAssistantsAsyncClient(httpClient);
        createThreadRunner(threadCreationOptions -> {
            AtomicReference<String> threadIdReference = new AtomicReference<>();

            // Create a simple thread without a message
            StepVerifier.create(client.createThread(threadCreationOptions))
                    .assertNext(assistantThread -> {
                        assertNotNull(assistantThread.getId());
                        assertNotNull(assistantThread.getCreatedAt());
                        assertEquals("thread", assistantThread.getObject());
                        threadIdReference.set(assistantThread.getId());
                    })
                    .verifyComplete();

            String threadId = threadIdReference.get();

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
    public void createThreadWithResponse(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getAssistantsAsyncClient(httpClient);
        createThreadRunner(threadCreationOptions -> {
            AtomicReference<String> threadIdReference = new AtomicReference<>();
            // Create a thread
            StepVerifier.create(client.createThreadWithResponse(
                    BinaryData.fromObject(threadCreationOptions), new RequestOptions()))
                    .assertNext(response -> {
                        AssistantThread assistantThread = assertAndGetValueFromResponse(response, AssistantThread.class, 200);
                        assertNotNull(assistantThread.getId());
                        assertNotNull(assistantThread.getCreatedAt());
                        assertEquals("thread", assistantThread.getObject());
                        threadIdReference.set(assistantThread.getId());
                    }).verifyComplete();


            String threadId = threadIdReference.get();

            // Delete the created thread
            StepVerifier.create(client.deleteThread(threadId))
                    .assertNext(deletionStatus -> {
                        assertEquals(threadId, deletionStatus.getId());
                        assertTrue(deletionStatus.isDeleted());
                    }).verifyComplete();
        });
    }
}
