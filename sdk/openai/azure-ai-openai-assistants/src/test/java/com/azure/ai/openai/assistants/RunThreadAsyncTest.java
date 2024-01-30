// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.assistants;

import com.azure.ai.openai.assistants.models.CreateRunOptions;
import com.azure.ai.openai.assistants.models.MessageRole;
import com.azure.ai.openai.assistants.models.RunStatus;
import com.azure.ai.openai.assistants.models.ThreadRun;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;
import com.azure.core.util.logging.ClientLogger;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.util.concurrent.atomic.AtomicReference;

import static com.azure.ai.openai.assistants.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RunThreadAsyncTest extends AssistantsClientTestBase {
    private static final ClientLogger LOGGER = new ClientLogger(RunThreadAsyncTest.class);
    private AssistantsAsyncClient client;

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void submitMessageAndRun(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getAssistantsAsyncClient(httpClient);
        String mathTutorAssistantId = createMathTutorAssistant(client, LOGGER);
        String threadId = createThread(client, LOGGER);
        submitMessageAndRunRunner(message -> {
            StepVerifier.create(client.createMessage(threadId, MessageRole.USER, message))
                    .assertNext(threadMessage -> validateThreadMessage(threadMessage, threadId))
                    .verifyComplete();

            // Submit the message and run
            AtomicReference<ThreadRun> runReference = new AtomicReference<>();
            StepVerifier.create(client.createRun(threadId, new CreateRunOptions(mathTutorAssistantId, null)))
                    .assertNext(run -> {
                        assertNotNull(run.getId());
                        assertNotNull(run.getCreatedAt());
                        assertEquals("thread.run", run.getObject());
                        assertEquals(mathTutorAssistantId, run.getAssistantId());
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
                            assertEquals(mathTutorAssistantId, threadRun.getAssistantId());
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

        });
        // Delete the created thread
        deleteThread(client, threadId, LOGGER);
        // Delete the created assistant
        deleteMathTutorAssistant(client, mathTutorAssistantId, LOGGER);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void submitMessageAndRunWithResponse(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getAssistantsAsyncClient(httpClient);
        String mathTutorAssistantId = createMathTutorAssistant(client, LOGGER);
        String threadId = createThread(client, LOGGER);
        submitMessageAndRunRunner(message -> {
            StepVerifier.create(client.createMessage(threadId, MessageRole.USER, message))
                    .assertNext(threadMessage -> validateThreadMessage(threadMessage, threadId))
                    .verifyComplete();

            // Submit the message and run
            AtomicReference<ThreadRun> runReference = new AtomicReference<>();
            StepVerifier.create(client.createRunWithResponse(threadId,
                    BinaryData.fromObject(new CreateRunOptions(mathTutorAssistantId, null)),
                    new RequestOptions()))
                    .assertNext(response -> {
                        ThreadRun run = assertAndGetValueFromResponse(response, ThreadRun.class, 200);
                        assertNotNull(run.getId());
                        assertNotNull(run.getCreatedAt());
                        assertEquals("thread.run", run.getObject());
                        assertEquals(mathTutorAssistantId, run.getAssistantId());
                        assertEquals(threadId, run.getThreadId());
                        assertNotNull(run.getInstructions());
                        runReference.set(run);
                    })
                    .verifyComplete();

            ThreadRun run = runReference.get();
            // Wait on Run and poll the Run in a loop
            while (run.getStatus() == RunStatus.QUEUED || run.getStatus() == RunStatus.IN_PROGRESS) {
                String runId = run.getId();
                StepVerifier.create(client.getRunWithResponse(threadId, runId, new RequestOptions()))
                        .assertNext(response -> {
                            ThreadRun threadRun = assertAndGetValueFromResponse(response, ThreadRun.class, 200);
                            assertNotNull(threadRun.getId());
                            assertNotNull(threadRun.getCreatedAt());
                            assertEquals("thread.run", threadRun.getObject());
                            assertEquals(mathTutorAssistantId, threadRun.getAssistantId());
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
        });
        // Delete the created thread
        deleteThread(client, threadId, LOGGER);
        // Delete the created assistant
        deleteMathTutorAssistant(client, mathTutorAssistantId, LOGGER);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void createThreadAndRun(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getAssistantsAsyncClient(httpClient);
        String mathTutorAssistantId = createMathTutorAssistant(client, LOGGER);
        createThreadAndRunRunner(createAndRunThreadOptions -> {
            AtomicReference<ThreadRun> runReference = new AtomicReference<>();
            AtomicReference<String> threadIdReference = new AtomicReference<>();
            StepVerifier.create(client.createThreadAndRun(createAndRunThreadOptions))
                    .assertNext(run -> {
                        assertNotNull(run.getId());
                        assertNotNull(run.getCreatedAt());
                        assertEquals("thread.run", run.getObject());
                        assertEquals(mathTutorAssistantId, run.getAssistantId());
                        assertNotNull(run.getInstructions());
                        assertNotNull(run.getThreadId());
                        threadIdReference.set(run.getThreadId());
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
                            assertEquals(mathTutorAssistantId, threadRun.getAssistantId());
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
            deleteThread(client, threadId, LOGGER);
        }, mathTutorAssistantId);
        // Delete the created assistant
        deleteMathTutorAssistant(client, mathTutorAssistantId, LOGGER);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void createThreadAndRunWithResponse(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getAssistantsAsyncClient(httpClient);
        String mathTutorAssistantId = createMathTutorAssistant(client, LOGGER);
        createThreadAndRunRunner(createAndRunThreadOptions -> {
            AtomicReference<ThreadRun> runReference = new AtomicReference<>();
            AtomicReference<String> threadIdReference = new AtomicReference<>();
            // Create a simple thread without a message
            StepVerifier.create(client.createThreadAndRunWithResponse(BinaryData.fromObject(createAndRunThreadOptions),
                    new RequestOptions()))
                    .assertNext(response -> {
                        ThreadRun run = assertAndGetValueFromResponse(response, ThreadRun.class, 200);
                        assertNotNull(run.getId());
                        assertNotNull(run.getCreatedAt());
                        assertEquals("thread.run", run.getObject());
                        assertEquals(mathTutorAssistantId, run.getAssistantId());
                        assertNotNull(run.getInstructions());
                        assertNotNull(run.getThreadId());
                        threadIdReference.set(run.getThreadId());
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
                            assertEquals(mathTutorAssistantId, threadRun.getAssistantId());
                            assertNotNull(threadRun.getInstructions());
                            runReference.set(threadRun);
                        })
                        .verifyComplete();
                run = runReference.get();
            }
            String threadId = threadIdReference.get();

            assertSame(RunStatus.COMPLETED, run.getStatus());

            // List the messages, it should contain the answer other than the question.
            StepVerifier.create(client.listMessages(threadId))
                    .assertNext(openAIPageableListOfThreadMessage -> {
                        assertNotNull(openAIPageableListOfThreadMessage);
                        assertTrue(openAIPageableListOfThreadMessage.getData().size() > 1);
                    })
                    .verifyComplete();

            // Delete the created thread
            deleteThread(client, threadId, LOGGER);
        }, mathTutorAssistantId);
        // Delete the created assistant
        deleteMathTutorAssistant(client, mathTutorAssistantId, LOGGER);
    }
}
