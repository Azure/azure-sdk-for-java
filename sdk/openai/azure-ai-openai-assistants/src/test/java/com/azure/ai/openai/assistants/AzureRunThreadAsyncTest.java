// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.assistants;

import com.azure.ai.openai.assistants.models.Assistant;
import com.azure.ai.openai.assistants.models.AssistantCreationOptions;
import com.azure.ai.openai.assistants.models.AssistantDeletionStatus;
import com.azure.ai.openai.assistants.models.AssistantThreadCreationOptions;
import com.azure.ai.openai.assistants.models.CreateRunOptions;
import com.azure.ai.openai.assistants.models.MessageRole;
import com.azure.ai.openai.assistants.models.RunStatus;
import com.azure.ai.openai.assistants.models.ThreadRun;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.util.concurrent.atomic.AtomicReference;

import static com.azure.ai.openai.assistants.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AzureRunThreadAsyncTest extends AssistantsClientTestBase {
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
                }).verifyComplete();
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

    @Disabled("tear down failed when deleting assistant")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void submitMessageAndRun(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getAssistantsAsyncClient(httpClient, serviceVersion);
        submitMessageAndRunRunner(message -> {
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

            // Submit the message and run
            AtomicReference<ThreadRun> runReference = new AtomicReference<>();
            StepVerifier.create(client.createRun(threadId, new CreateRunOptions(mathTutorAssistant.getId(), null)))
                    .assertNext(run -> {
                        assertNotNull(run.getId());
                        assertNotNull(run.getCreatedAt());
                        assertEquals("thread.run", run.getObject());
                        assertEquals(mathTutorAssistant.getId(), run.getAssistantId());
                        assertNotNull(run.getInstructions());
                        runReference.set(run);
                    })
                    .verifyComplete();

            ThreadRun run = runReference.get();
            // Wait on Run and poll the Run in a loop
            while (run.getStatus() == RunStatus.QUEUED || run.getStatus() == RunStatus.IN_PROGRESS) {
                String runId = run.getId();
                StepVerifier.create(client.getRun(run.getThreadId(), runId))
                        .assertNext(threadRun -> {
                            assertNotNull(threadRun.getId());
                            assertNotNull(threadRun.getCreatedAt());
                            assertEquals("thread.run", threadRun.getObject());
                            assertEquals(mathTutorAssistant.getId(), threadRun.getAssistantId());
                            assertEquals(threadId, threadRun.getThreadId());
                            assertNotNull(threadRun.getInstructions());
                            runReference.set(threadRun);
                        })
                        .verifyComplete();
                run = runReference.get();
            }

            assertSame(RunStatus.COMPLETED, run.getStatus());

            // List the messages, it should contain the answer other than the question.
            StepVerifier.create(client.listMessages(threadId))
                    .assertNext(openAIPageableListOfThreadMessage -> {
                        assertNotNull(openAIPageableListOfThreadMessage);
                        assertTrue(openAIPageableListOfThreadMessage.getData().size() > 1);
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

    @Disabled("tear down failed when deleting assistant")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void createThreadAndRun(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getAssistantsAsyncClient(httpClient, serviceVersion);
        createThreadAndRunRunner(createAndRunThreadOptions -> {
            AtomicReference<ThreadRun> runReference = new AtomicReference<>();
            AtomicReference<String> threadIdReference = new AtomicReference<>();
            StepVerifier.create(client.createThreadAndRun(createAndRunThreadOptions))
                    .assertNext(run -> {
                        assertNotNull(run.getId());
                        assertNotNull(run.getCreatedAt());
                        assertEquals("thread.run", run.getObject());
                        assertEquals(mathTutorAssistant.getId(), run.getAssistantId());
                        assertNotNull(run.getInstructions());
                        assertNotNull(run.getThreadId());
                        threadIdReference.set(run.getThreadId());
                        runReference.set(run);
                    }).verifyComplete();

            ThreadRun run = runReference.get();

            // Wait on Run and poll the Run in a loop
            while (run.getStatus() == RunStatus.QUEUED || run.getStatus() == RunStatus.IN_PROGRESS) {
                String runId = run.getId();
                StepVerifier.create(client.getRun(run.getThreadId(), runId))
                        .assertNext(threadRun -> {
                            assertNotNull(threadRun.getId());
                            assertNotNull(threadRun.getCreatedAt());
                            assertEquals("thread.run", threadRun.getObject());
                            assertEquals(mathTutorAssistant.getId(), threadRun.getAssistantId());
                            assertNotNull(threadRun.getInstructions());
                            runReference.set(threadRun);
                        })
                        .verifyComplete();
                run = runReference.get();
            }

            assertSame(RunStatus.COMPLETED, run.getStatus());

            String threadId = threadIdReference.get();

            // List the messages, it should contain the answer other than the question.
            StepVerifier.create(client.listMessages(threadId))
                    .assertNext(openAIPageableListOfThreadMessage -> {
                        assertNotNull(openAIPageableListOfThreadMessage);
                        assertTrue(openAIPageableListOfThreadMessage.getData().size() > 1);
                    })
                    .verifyComplete();


            // Delete the created thread
            StepVerifier.create(client.deleteThread(threadId))
                    .assertNext(deletionStatus -> {
                        assertEquals(threadId, deletionStatus.getId());
                        assertTrue(deletionStatus.isDeleted());
                    })
                    .verifyComplete();
        }, mathTutorAssistant.getId());
    }
}
