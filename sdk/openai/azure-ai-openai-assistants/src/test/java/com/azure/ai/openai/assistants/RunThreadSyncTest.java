// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.assistants;

import com.azure.ai.openai.assistants.models.CreateRunOptions;
import com.azure.ai.openai.assistants.models.MessageRole;
import com.azure.ai.openai.assistants.models.PageableList;
import com.azure.ai.openai.assistants.models.RunStatus;
import com.azure.ai.openai.assistants.models.RunStep;
import com.azure.ai.openai.assistants.models.ThreadMessage;
import com.azure.ai.openai.assistants.models.ThreadMessageOptions;
import com.azure.ai.openai.assistants.models.ThreadRun;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

import static com.azure.ai.openai.assistants.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RunThreadSyncTest extends AssistantsClientTestBase {
    private AssistantsClient client;

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void submitMessageAndRun(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient);
        String mathTutorAssistantId = createMathTutorAssistant(client);
        String threadId = createThread(client);
        submitMessageAndRunRunner(message -> {
            ThreadMessage threadMessage = client.createMessage(threadId, new ThreadMessageOptions(MessageRole.USER, message));
            validateThreadMessage(threadMessage, threadId);
            // Submit the message and run
            ThreadRun run = client.createRun(threadId, new CreateRunOptions(mathTutorAssistantId));
            assertNotNull(run.getId());
            assertNotNull(run.getCreatedAt());
            assertEquals("thread.run", run.getObject());
            assertEquals(mathTutorAssistantId, run.getAssistantId());
            assertEquals(threadId, run.getThreadId());
            assertNotNull(run.getInstructions());

            // Wait on Run and poll the Run in a loop
            do {
                run = client.getRun(run.getThreadId(), run.getId());
                sleepIfRunningAgainstService(1000);
            } while (run.getStatus() == RunStatus.IN_PROGRESS || run.getStatus() == RunStatus.QUEUED);

            assertSame(RunStatus.COMPLETED, run.getStatus());

            // List the messages, it should contain the answer other than the question.
            PageableList<ThreadMessage> openAIPageableListOfThreadMessage = client.listMessages(threadId);
            assertNotNull(openAIPageableListOfThreadMessage);
            assertTrue(openAIPageableListOfThreadMessage.getData().size() > 1);
        });
        // Delete the created thread
        deleteThread(client, threadId);
        // Delete the created assistant
        deleteAssistant(client, mathTutorAssistantId);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void submitMessageAndRunWithResponse(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient);
        String mathTutorAssistantId = createMathTutorAssistant(client);
        String threadId = createThread(client);
        submitMessageAndRunRunner(message -> {
            ThreadMessage threadMessage = client.createMessage(threadId, new ThreadMessageOptions(MessageRole.USER, message));
            validateThreadMessage(threadMessage, threadId);
            // Submit the message and run
            Response<BinaryData> runWithResponse = client.createRunWithResponse(threadId,
                    BinaryData.fromObject(new CreateRunOptions(mathTutorAssistantId)),
                    new RequestOptions());
            ThreadRun run = assertAndGetValueFromResponse(runWithResponse, ThreadRun.class, 200);
            assertNotNull(run.getId());
            assertNotNull(run.getCreatedAt());
            assertEquals("thread.run", run.getObject());
            assertEquals(mathTutorAssistantId, run.getAssistantId());
            assertEquals(threadId, run.getThreadId());
            assertNotNull(run.getInstructions());

            // Wait on Run and poll the Run in a loop
            do {
                run = client.getRun(run.getThreadId(), run.getId());
                sleepIfRunningAgainstService(1000);
            } while (run.getStatus() == RunStatus.IN_PROGRESS || run.getStatus() == RunStatus.QUEUED);

            assertSame(RunStatus.COMPLETED, run.getStatus());

            // List the messages, it should contain the answer other than the question.
            PageableList<ThreadMessage> openAIPageableListOfThreadMessage = client.listMessages(threadId);
            assertNotNull(openAIPageableListOfThreadMessage);
            assertTrue(openAIPageableListOfThreadMessage.getData().size() > 1);
        });
        // Delete the created thread
        deleteThread(client, threadId);
        // Delete the created assistant
        deleteAssistant(client, mathTutorAssistantId);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void createThreadAndRun(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient);
        String mathTutorAssistantId = createMathTutorAssistant(client);
        createThreadAndRunRunner(createAndRunThreadOptions -> {
            // Create a simple thread without a message
            ThreadRun run = client.createThreadAndRun(createAndRunThreadOptions);
            String threadId = run.getThreadId();
            assertNotNull(run.getId());
            assertNotNull(run.getCreatedAt());
            assertEquals("thread.run", run.getObject());
            assertEquals(mathTutorAssistantId, run.getAssistantId());
            assertNotNull(run.getInstructions());
            assertNotNull(threadId);

            // Wait on Run and poll the Run in a loop
            do {
                run = client.getRun(run.getThreadId(), run.getId());
                sleepIfRunningAgainstService(1000);
            } while (run.getStatus() == RunStatus.IN_PROGRESS || run.getStatus() == RunStatus.QUEUED);

            assertSame(RunStatus.COMPLETED, run.getStatus());

            // List the messages, it should contain the answer other than the question.
            PageableList<ThreadMessage> openAIPageableListOfThreadMessage = client.listMessages(threadId);
            assertNotNull(openAIPageableListOfThreadMessage);
            assertTrue(openAIPageableListOfThreadMessage.getData().size() > 1);

            // Delete the created thread
            deleteThread(client, threadId);
        }, mathTutorAssistantId);
        // Delete the created assistant
        deleteAssistant(client, mathTutorAssistantId);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void createThreadAndRunWithResponse(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient);
        String mathTutorAssistantId = createMathTutorAssistant(client);
        createThreadAndRunRunner(createAndRunThreadOptions -> {
            // Create a simple thread without a message
            Response<BinaryData> response = client.createThreadAndRunWithResponse(
                    BinaryData.fromObject(createAndRunThreadOptions), new RequestOptions());
            ThreadRun run = assertAndGetValueFromResponse(response, ThreadRun.class, 200);
            String threadId = run.getThreadId();
            assertNotNull(run.getId());
            assertNotNull(run.getCreatedAt());
            assertEquals("thread.run", run.getObject());
            assertEquals(mathTutorAssistantId, run.getAssistantId());
            assertNotNull(run.getInstructions());
            assertNotNull(threadId);

            // Wait on Run and poll the Run in a loop
            do {
                run = client.getRun(run.getThreadId(), run.getId());
                sleepIfRunningAgainstService(1000);
            } while (run.getStatus() == RunStatus.IN_PROGRESS || run.getStatus() == RunStatus.QUEUED);

            assertSame(RunStatus.COMPLETED, run.getStatus());

            // List the messages, it should contain the answer other than the question.
            PageableList<ThreadMessage> openAIPageableListOfThreadMessage = client.listMessages(threadId);
            assertNotNull(openAIPageableListOfThreadMessage);
            assertTrue(openAIPageableListOfThreadMessage.getData().size() > 1);

            // Delete the created thread
            deleteThread(client, threadId);
        }, mathTutorAssistantId);
        // Delete the created assistant
        deleteAssistant(client, mathTutorAssistantId);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void cancelRun(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient);
        String mathTutorAssistantId = createMathTutorAssistant(client);
        createThreadAndRunRunner(createAndRunThreadOptions -> {
            // Create a simple thread without a message
            ThreadRun run = createThreadAndRun(client, createAndRunThreadOptions);
            String threadId = run.getThreadId();
            ThreadRun cancelRun = client.cancelRun(threadId, run.getId());
            RunStatus status = cancelRun.getStatus();
            assertTrue(status == RunStatus.CANCELLING || status == RunStatus.CANCELLED);
            assertEquals(threadId, cancelRun.getThreadId());
            assertEquals(run.getId(), cancelRun.getId());
            // Delete the created thread
            deleteThread(client, threadId);
        }, mathTutorAssistantId);
        // Delete the created assistant
        deleteAssistant(client, mathTutorAssistantId);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void cancelRunWithResponse(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient);
        String mathTutorAssistantId = createMathTutorAssistant(client);
        createThreadAndRunRunner(createAndRunThreadOptions -> {
            ThreadRun run = createThreadAndRun(client, createAndRunThreadOptions);
            String threadId = run.getThreadId();
            String runId = run.getId();
            Response<BinaryData> response = client.cancelRunWithResponse(threadId, runId, new RequestOptions());
            ThreadRun cancelRun = assertAndGetValueFromResponse(response, ThreadRun.class, 200);
            RunStatus status = cancelRun.getStatus();
            assertTrue(status == RunStatus.CANCELLING || status == RunStatus.CANCELLED);
            assertEquals(threadId, cancelRun.getThreadId());
            assertEquals(runId, cancelRun.getId());
            // Delete the created thread
            deleteThread(client, threadId);
        }, mathTutorAssistantId);
        // Delete the created assistant
        deleteAssistant(client, mathTutorAssistantId);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void listRuns(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient);
        String mathTutorAssistantId = createMathTutorAssistant(client);
        createThreadAndRunRunner(createAndRunThreadOptions -> {
            ThreadRun run = createThreadAndRun(client, createAndRunThreadOptions);
            String threadId = run.getThreadId();
            // List runs
            PageableList<ThreadRun> runs = client.listRuns(threadId);
            List<ThreadRun> data = runs.getData();
            assertNotNull(data);
            assertEquals(1, data.size());
            validateThreadRun(run, data.get(0));
            // List runs with response
            Response<BinaryData> response = client.listRunsWithResponse(threadId, new RequestOptions());
            PageableList<ThreadRun> runsWithResponse = asserAndGetPageableListFromResponse(response, 200,
                reader -> reader.readArray(ThreadRun::fromJson));
            List<ThreadRun> dataWithResponse = runsWithResponse.getData();
            assertNotNull(dataWithResponse);
            assertEquals(1, dataWithResponse.size());
            validateThreadRun(run, dataWithResponse.get(0));
            // Delete the created thread
            deleteThread(client, threadId);
        }, mathTutorAssistantId);
        // Delete the created assistant
        deleteAssistant(client, mathTutorAssistantId);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void listAndGetRunSteps(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient);
        String mathTutorAssistantId = createMathTutorAssistant(client);
        createThreadAndRunRunner(createAndRunThreadOptions -> {
            ThreadRun run = createThreadAndRun(client, createAndRunThreadOptions);
            String threadId = run.getThreadId();
            String runId = run.getId();

            // Wait on Run and poll the Run in a loop
            do {
                run = client.getRun(run.getThreadId(), run.getId());
                sleepIfRunningAgainstService(1000);
            } while (run.getStatus() == RunStatus.IN_PROGRESS || run.getStatus() == RunStatus.QUEUED);

            assertSame(RunStatus.COMPLETED, run.getStatus());

            // List run steps
            PageableList<RunStep> runSteps = client.listRunSteps(threadId, runId);
            assertNotNull(runSteps);

            List<RunStep> runStepsData = runSteps.getData();
            assertNotNull(runStepsData);
            assertFalse(runStepsData.isEmpty());
            assertEquals("list", runSteps.getObject());
            RunStep runStep = runStepsData.get(0);
            // Get run step by id
            String runStepId = runStep.getId();
            RunStep retrievedStep = client.getRunStep(threadId, runId, runStepId);
            assertNotNull(retrievedStep);
            validateRunStep(runStep, retrievedStep);

            // WITH RESPONSE

            // List run steps with response
            Response<BinaryData> response = client.listRunStepsWithResponse(threadId, runId, new RequestOptions());
            PageableList<RunStep> runStepsWithResponse = asserAndGetPageableListFromResponse(response, 200,
                reader -> reader.readArray(RunStep::fromJson));
            assertNotNull(runStepsWithResponse);
            List<RunStep> runStepsDataWithResponse = runStepsWithResponse.getData();
            assertNotNull(runStepsDataWithResponse);
            assertFalse(runStepsDataWithResponse.isEmpty());
            assertEquals("list", runSteps.getObject());
            // Get run step with response
            Response<BinaryData> getRunStepResponse = client.getRunStepWithResponse(threadId, run.getId(),
                    runStepId, new RequestOptions());
            RunStep retrievedStepResponse = assertAndGetValueFromResponse(getRunStepResponse, RunStep.class, 200);
            assertNotNull(retrievedStepResponse);
            validateRunStep(runStep, retrievedStep);

            // Delete the created thread
            deleteThread(client, threadId);
        }, mathTutorAssistantId);
        // Delete the created assistant
        deleteAssistant(client, mathTutorAssistantId);
    }
}
