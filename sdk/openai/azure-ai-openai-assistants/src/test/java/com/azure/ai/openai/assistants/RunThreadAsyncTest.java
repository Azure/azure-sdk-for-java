// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.assistants;

import com.azure.ai.openai.assistants.models.CreateRunOptions;
import com.azure.ai.openai.assistants.models.MessageRole;
import com.azure.ai.openai.assistants.models.PageableList;
import com.azure.ai.openai.assistants.models.RunStatus;
import com.azure.ai.openai.assistants.models.RunStep;
import com.azure.ai.openai.assistants.models.ThreadMessageOptions;
import com.azure.ai.openai.assistants.models.ThreadRun;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.ai.openai.assistants.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RunThreadAsyncTest extends AssistantsClientTestBase {
    private AssistantsAsyncClient client;

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void submitMessageAndRun(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsAsyncClient(httpClient);
        String mathTutorAssistantId = createMathTutorAssistant(client);
        String threadId = createThread(client);
        submitMessageAndRunRunner(message -> {
            StepVerifier.create(client.createMessage(threadId, new ThreadMessageOptions(MessageRole.USER, message)))
                    .assertNext(threadMessage -> validateThreadMessage(threadMessage, threadId))
                    .verifyComplete();

            // Submit the message and run
            AtomicReference<ThreadRun> runReference = new AtomicReference<>();
            StepVerifier.create(client.createRun(threadId, new CreateRunOptions(mathTutorAssistantId)))
                    .assertNext(run -> {
                        assertNotNull(run.getId());
                        assertNotNull(run.getCreatedAt());
                        assertEquals("thread.run", run.getObject());
                        assertEquals(mathTutorAssistantId, run.getAssistantId());
                        assertNotNull(run.getInstructions());
                        runReference.set(run);
                    })
                    .verifyComplete();

            // Wait on Run and poll the Run in a loop
            ThreadRun run = runReference.get();
            do {
                StepVerifier.create(client.getRun(run.getThreadId(), run.getId()))
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

                sleepIfRunningAgainstService(1000);
            } while (run.getStatus() == RunStatus.IN_PROGRESS || run.getStatus() == RunStatus.QUEUED);

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
        deleteThread(client, threadId);
        // Delete the created assistant
        deleteAssistant(client, mathTutorAssistantId);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void submitMessageAndRunWithResponse(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsAsyncClient(httpClient);
        String mathTutorAssistantId = createMathTutorAssistant(client);
        String threadId = createThread(client);
        submitMessageAndRunRunner(message -> {
            StepVerifier.create(client.createMessage(threadId, new ThreadMessageOptions(MessageRole.USER, message)))
                    .assertNext(threadMessage -> validateThreadMessage(threadMessage, threadId))
                    .verifyComplete();

            // Submit the message and run
            AtomicReference<ThreadRun> runReference = new AtomicReference<>();
            StepVerifier.create(client.createRunWithResponse(threadId,
                    BinaryData.fromObject(new CreateRunOptions(mathTutorAssistantId)),
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

            // Wait on Run and poll the Run in a loop
            ThreadRun run = runReference.get();
            do {
                StepVerifier.create(client.getRunWithResponse(threadId, run.getId(), new RequestOptions()))
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

                sleepIfRunningAgainstService(1000);
            } while (run.getStatus() == RunStatus.IN_PROGRESS || run.getStatus() == RunStatus.QUEUED);

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
        deleteThread(client, threadId);
        // Delete the created assistant
        deleteAssistant(client, mathTutorAssistantId);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void createThreadAndRun(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsAsyncClient(httpClient);
        String mathTutorAssistantId = createMathTutorAssistant(client);
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

            // Wait on Run and poll the Run in a loop
            ThreadRun run = runReference.get();
            do {
                StepVerifier.create(client.getRun(run.getThreadId(), run.getId()))
                        .assertNext(threadRun -> {
                            assertNotNull(threadRun.getId());
                            assertNotNull(threadRun.getCreatedAt());
                            assertEquals("thread.run", threadRun.getObject());
                            assertEquals(mathTutorAssistantId, threadRun.getAssistantId());
                            assertNotNull(threadRun.getThreadId());
                            assertNotNull(threadRun.getInstructions());
                            runReference.set(threadRun);
                        })
                        .verifyComplete();
                run = runReference.get();

                sleepIfRunningAgainstService(1000);
            } while (run.getStatus() == RunStatus.IN_PROGRESS || run.getStatus() == RunStatus.QUEUED);

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
            deleteThread(client, threadId);
        }, mathTutorAssistantId);
        // Delete the created assistant
        deleteAssistant(client, mathTutorAssistantId);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void createThreadAndRunWithResponse(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsAsyncClient(httpClient);
        String mathTutorAssistantId = createMathTutorAssistant(client);
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

            // Wait on Run and poll the Run in a loop
            ThreadRun run = runReference.get();
            do {
                StepVerifier.create(client.getRun(run.getThreadId(), run.getId()))
                        .assertNext(threadRun -> {
                            assertNotNull(threadRun.getId());
                            assertNotNull(threadRun.getCreatedAt());
                            assertEquals("thread.run", threadRun.getObject());
                            assertEquals(mathTutorAssistantId, threadRun.getAssistantId());
                            assertNotNull(threadRun.getThreadId());
                            assertNotNull(threadRun.getInstructions());
                            runReference.set(threadRun);
                        })
                        .verifyComplete();
                run = runReference.get();

                sleepIfRunningAgainstService(1000);
            } while (run.getStatus() == RunStatus.IN_PROGRESS || run.getStatus() == RunStatus.QUEUED);

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
            deleteThread(client, threadId);
        }, mathTutorAssistantId);
        // Delete the created assistant
        deleteAssistant(client, mathTutorAssistantId);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void cancelRun(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsAsyncClient(httpClient);
        String mathTutorAssistantId = createMathTutorAssistant(client);
        createThreadAndRunRunner(createAndRunThreadOptions -> {
            ThreadRun run = createThreadAndRun(client, createAndRunThreadOptions);
            String threadId = run.getThreadId();
            // Cancel the run
            StepVerifier.create(client.cancelRun(threadId, run.getId()))
                    .assertNext(cancelRun -> {
                        RunStatus status = cancelRun.getStatus();
                        assertTrue(status == RunStatus.CANCELLING || status == RunStatus.CANCELLED);
                        assertEquals(threadId, cancelRun.getThreadId());
                        assertEquals(run.getId(), cancelRun.getId());
                    })
                    .verifyComplete();
            // Delete the created thread
            deleteThread(client, threadId);
        }, mathTutorAssistantId);
        // Delete the created assistant
        deleteAssistant(client, mathTutorAssistantId);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void cancelRunWithResponse(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsAsyncClient(httpClient);
        String mathTutorAssistantId = createMathTutorAssistant(client);
        createThreadAndRunRunner(createAndRunThreadOptions -> {
            ThreadRun run = createThreadAndRun(client, createAndRunThreadOptions);
            String threadId = run.getThreadId();
            String runId = run.getId();
            StepVerifier.create(client.cancelRunWithResponse(threadId, runId, new RequestOptions()))
                    .assertNext(response -> {
                        ThreadRun cancelRun = assertAndGetValueFromResponse(response, ThreadRun.class, 200);
                        RunStatus status = cancelRun.getStatus();
                        assertTrue(status == RunStatus.CANCELLING || status == RunStatus.CANCELLED);
                        assertEquals(threadId, cancelRun.getThreadId());
                        assertEquals(runId, cancelRun.getId());
                    })
                    .verifyComplete();
            // Delete the created thread
            deleteThread(client, threadId);
        }, mathTutorAssistantId);
        // Delete the created assistant
        deleteAssistant(client, mathTutorAssistantId);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void listRuns(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsAsyncClient(httpClient);
        String mathTutorAssistantId = createMathTutorAssistant(client);
        createThreadAndRunRunner(createAndRunThreadOptions -> {
            ThreadRun run = createThreadAndRun(client, createAndRunThreadOptions);
            String threadId = run.getThreadId();
            // List runs
            StepVerifier.create(client.listRuns(threadId))
                    .assertNext(runs -> {
                        List<ThreadRun> data = runs.getData();
                        assertNotNull(data);
                        assertEquals(1, data.size());
                        validateThreadRun(run, data.get(0));
                    })
                    .verifyComplete();
            // List runs with response
            StepVerifier.create(client.listRunsWithResponse(threadId, new RequestOptions()))
                    .assertNext(response -> {
                        PageableList<ThreadRun> runs = asserAndGetPageableListFromResponse(response, 200,
                            reader -> reader.readArray(ThreadRun::fromJson));
                        List<ThreadRun> data = runs.getData();
                        assertNotNull(data);
                        assertEquals(1, data.size());
                        validateThreadRun(run, data.get(0));
                    })
                    .verifyComplete();
            // Delete the created thread
            deleteThread(client, threadId);
        }, mathTutorAssistantId);
        // Delete the created assistant
        deleteAssistant(client, mathTutorAssistantId);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void listAndGetRunSteps(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsAsyncClient(httpClient);
        String mathTutorAssistantId = createMathTutorAssistant(client);
        createThreadAndRunRunner(createAndRunThreadOptions -> {
            ThreadRun run = createThreadAndRun(client, createAndRunThreadOptions);
            String threadId = run.getThreadId();
            String runId = run.getId();

            // Wait on Run and poll the Run in a loop
            AtomicReference<ThreadRun> runReference = new AtomicReference<>(run);
            do {
                StepVerifier.create(client.getRun(run.getThreadId(), run.getId()))
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
                sleepIfRunningAgainstService(1000);
                run = runReference.get();
            } while (run.getStatus() == RunStatus.IN_PROGRESS || run.getStatus() == RunStatus.QUEUED);
            assertSame(RunStatus.COMPLETED, run.getStatus());

            // List run steps
            AtomicReference<RunStep> runStepRef = new AtomicReference<>();
            StepVerifier.create(client.listRunSteps(threadId, runId))
                    .assertNext(runSteps -> {
                        assertNotNull(runSteps);
                        List<RunStep> runStepsData = runSteps.getData();
                        assertNotNull(runStepsData);
                        assertFalse(runStepsData.isEmpty());
                        assertEquals("list", runSteps.getObject());
                        runStepRef.set(runStepsData.get(0));
                    })
                    .verifyComplete();

            RunStep runStep = runStepRef.get();
            // Get run step by id
            String runStepId = runStep.getId();
            StepVerifier.create(client.getRunStep(threadId, runId, runStepId))
                    .assertNext(retrievedStep -> validateRunStep(runStep, retrievedStep));

            // WITH RESPONSE

            // List run steps with response
            StepVerifier.create(client.listRunStepsWithResponse(threadId, runId, new RequestOptions()))
                    .assertNext(response -> {
                        PageableList<RunStep> runStepsWithResponse = asserAndGetPageableListFromResponse(response, 200,
                            reader -> reader.readArray(RunStep::fromJson));
                        assertNotNull(runStepsWithResponse);
                        List<RunStep> runStepsDataWithResponse = runStepsWithResponse.getData();
                        assertNotNull(runStepsDataWithResponse);
                        assertFalse(runStepsDataWithResponse.isEmpty());
                        assertEquals("list", runStepsWithResponse.getObject());
                    })
                    .verifyComplete();
            // Get run step with response
            StepVerifier.create(client.getRunStepWithResponse(threadId, run.getId(), runStepId, new RequestOptions()))
                    .assertNext(response -> {
                        RunStep retrievedStepResponse = assertAndGetValueFromResponse(response, RunStep.class, 200);
                        assertNotNull(retrievedStepResponse);
                        validateRunStep(runStep, retrievedStepResponse);
                    })
                    .verifyComplete();

            // Delete the created thread
            deleteThread(client, threadId);
        }, mathTutorAssistantId);
        // Delete the created assistant
        deleteAssistant(client, mathTutorAssistantId);
    }
}
