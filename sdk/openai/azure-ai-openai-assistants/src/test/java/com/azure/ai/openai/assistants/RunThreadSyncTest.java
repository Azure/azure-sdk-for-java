// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.assistants;

import com.azure.ai.openai.assistants.models.CreateRunOptions;
import com.azure.ai.openai.assistants.models.MessageRole;
import com.azure.ai.openai.assistants.models.OpenAIPageableListOfThreadMessage;
import com.azure.ai.openai.assistants.models.RunStatus;
import com.azure.ai.openai.assistants.models.ThreadMessage;
import com.azure.ai.openai.assistants.models.ThreadRun;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.logging.ClientLogger;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static com.azure.ai.openai.assistants.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RunThreadSyncTest extends AssistantsClientTestBase {
    private static final ClientLogger LOGGER = new ClientLogger(RunThreadSyncTest.class);

    private AssistantsClient client;

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void submitMessageAndRun(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient);
        String mathTutorAssistantId = createMathTutorAssistant(client, LOGGER);
        String threadId = createThread(client, LOGGER);
        submitMessageAndRunRunner(message -> {
            ThreadMessage threadMessage = client.createMessage(threadId, MessageRole.USER, message);
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
            while (run.getStatus() == RunStatus.QUEUED || run.getStatus() == RunStatus.IN_PROGRESS) {
                String runId = run.getId();
                run = client.getRun(threadId, runId);
            }

            assertSame(RunStatus.COMPLETED, run.getStatus());

            // List the messages, it should contain the answer other than the question.
            OpenAIPageableListOfThreadMessage openAIPageableListOfThreadMessage = client.listMessages(threadId);
            assertNotNull(openAIPageableListOfThreadMessage);
            assertTrue(openAIPageableListOfThreadMessage.getData().size() > 1);
        });
        // Delete the created thread
        deleteThread(client, threadId, LOGGER);
        // Delete the created assistant
        deleteMathTutorAssistant(client, mathTutorAssistantId, LOGGER);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void submitMessageAndRunWithResponse(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient);
        String mathTutorAssistantId = createMathTutorAssistant(client, LOGGER);
        String threadId = createThread(client, LOGGER);
        submitMessageAndRunRunner(message -> {
            ThreadMessage threadMessage = client.createMessage(threadId, MessageRole.USER, message);
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
            while (run.getStatus() == RunStatus.QUEUED || run.getStatus() == RunStatus.IN_PROGRESS) {
                String runId = run.getId();
                run = client.getRun(threadId, runId);
            }

            assertSame(RunStatus.COMPLETED, run.getStatus());

            // List the messages, it should contain the answer other than the question.
            OpenAIPageableListOfThreadMessage openAIPageableListOfThreadMessage = client.listMessages(threadId);
            assertNotNull(openAIPageableListOfThreadMessage);
            assertTrue(openAIPageableListOfThreadMessage.getData().size() > 1);
        });
        // Delete the created thread
        deleteThread(client, threadId, LOGGER);
        // Delete the created assistant
        deleteMathTutorAssistant(client, mathTutorAssistantId, LOGGER);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void createThreadAndRun(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient);
        String mathTutorAssistantId = createMathTutorAssistant(client, LOGGER);
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
            while (run.getStatus() == RunStatus.QUEUED || run.getStatus() == RunStatus.IN_PROGRESS) {
                String runId = run.getId();
                run = client.getRun(run.getThreadId(), runId);
            }

            assertSame(RunStatus.COMPLETED, run.getStatus());

            // List the messages, it should contain the answer other than the question.
            OpenAIPageableListOfThreadMessage openAIPageableListOfThreadMessage = client.listMessages(threadId);
            assertNotNull(openAIPageableListOfThreadMessage);
            assertTrue(openAIPageableListOfThreadMessage.getData().size() > 1);

            // Delete the created thread
            deleteThread(client, threadId, LOGGER);
        }, mathTutorAssistantId);
        // Delete the created assistant
        deleteMathTutorAssistant(client, mathTutorAssistantId, LOGGER);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void createThreadAndRunWithResponse(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient);
        String mathTutorAssistantId = createMathTutorAssistant(client, LOGGER);
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
            while (run.getStatus() == RunStatus.QUEUED || run.getStatus() == RunStatus.IN_PROGRESS) {
                String runId = run.getId();
                run = client.getRun(run.getThreadId(), runId);
            }

            assertSame(RunStatus.COMPLETED, run.getStatus());

            // List the messages, it should contain the answer other than the question.
            OpenAIPageableListOfThreadMessage openAIPageableListOfThreadMessage = client.listMessages(threadId);
            assertNotNull(openAIPageableListOfThreadMessage);
            assertTrue(openAIPageableListOfThreadMessage.getData().size() > 1);

            // Delete the created thread
            deleteThread(client, threadId, LOGGER);
        }, mathTutorAssistantId);
        // Delete the created assistant
        deleteMathTutorAssistant(client, mathTutorAssistantId, LOGGER);
    }
}
