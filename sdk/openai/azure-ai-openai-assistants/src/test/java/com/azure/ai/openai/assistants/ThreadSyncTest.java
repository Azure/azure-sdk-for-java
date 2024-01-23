// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.assistants;

import com.azure.ai.openai.assistants.models.Assistant;
import com.azure.ai.openai.assistants.models.AssistantCreationOptions;
import com.azure.ai.openai.assistants.models.AssistantDeletionStatus;
import com.azure.ai.openai.assistants.models.AssistantThread;
import com.azure.ai.openai.assistants.models.AssistantThreadCreationOptions;
import com.azure.ai.openai.assistants.models.MessageRole;
import com.azure.ai.openai.assistants.models.OpenAIPageableListOfThreadMessage;
import com.azure.ai.openai.assistants.models.RunStatus;
import com.azure.ai.openai.assistants.models.ThreadMessage;
import com.azure.ai.openai.assistants.models.ThreadRun;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ThreadSyncTest extends AssistantsClientTestBase {

    private AssistantsClient client;
    private Assistant mathTutorAssistant;
    @Override
    protected void beforeTest() {
        client = getAssistantsClient(HttpClient.createDefault());

        // Create a Math tutor assistant
        AssistantCreationOptions assistantCreationOptions = new AssistantCreationOptions(GPT_4_1106_PREVIEW)
                .setName("Math Tutor")
                .setInstructions("You are a personal math tutor. Answer questions briefly, in a sentence or less.");
        Response<BinaryData> response = client.createAssistantWithResponse(BinaryData.fromObject(assistantCreationOptions), new RequestOptions());
        mathTutorAssistant = assertAndGetValueFromResponse(response, Assistant.class, 200);
        assertEquals(assistantCreationOptions.getName(), mathTutorAssistant.getName());
        assertEquals(assistantCreationOptions.getDescription(), mathTutorAssistant.getDescription());
        assertEquals(assistantCreationOptions.getInstructions(), mathTutorAssistant.getInstructions());
    }

    @Override
    protected void afterTest() {
        if (mathTutorAssistant != null) {
            Response<BinaryData> deletionStatusResponse = client.deleteAssistantWithResponse(mathTutorAssistant.getId(), new RequestOptions());
            AssistantDeletionStatus deletionStatus = assertAndGetValueFromResponse(deletionStatusResponse, AssistantDeletionStatus.class, 200);
            assertEquals(mathTutorAssistant.getId(), deletionStatus.getId());
            assertTrue(deletionStatus.isDeleted());
        }
    }

    private AssistantsClient getAssistantsClient(HttpClient httpClient) {
        return getAssistantsClientBuilder(buildAssertingClient(
                interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient, true))
                .buildClient();
    }

    private Assistant getMathTutorAssistant(HttpClient httpClient) {
        return getAssistantsClient(httpClient).getAssistant(mathTutorAssistant.getId());
    }
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void createThread(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient);
        createThreadRunner(threadCreationOptions -> {
            // Create a thread
            AssistantThread assistantThread = client.createThread(threadCreationOptions);

            assertNotNull(assistantThread.getId());
            assertNotNull(assistantThread.getCreatedAt());
            assertEquals("thread", assistantThread.getObject());

            // Delete the created thread
            client.deleteThread(assistantThread.getId());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void createThreadWithResponse(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient);
        createThreadRunner(threadCreationOptions -> {
            // Create a thread
            Response<BinaryData> response = client.createThreadWithResponse(BinaryData.fromObject(threadCreationOptions), new RequestOptions());
            AssistantThread assistantThread = assertAndGetValueFromResponse(response, AssistantThread.class, 200);

            assertNotNull(assistantThread.getId());
            assertNotNull(assistantThread.getCreatedAt());
            assertEquals("thread", assistantThread.getObject());

            // Delete the created thread
            client.deleteThread(assistantThread.getId());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void createMessage(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient);
        createMessageRunner(message -> {
            // Create a simple thread without a message
            AssistantThread assistantThread = client.createThread(new AssistantThreadCreationOptions());

            assertNotNull(assistantThread.getId());
            assertNotNull(assistantThread.getCreatedAt());
            assertEquals("thread", assistantThread.getObject());

            ThreadMessage threadMessage = client.createMessage(assistantThread.getId(), MessageRole.USER,
                    message);
            assertNotNull(threadMessage.getId());
            assertNotNull(threadMessage.getCreatedAt());
            assertEquals("thread.message", threadMessage.getObject());
            assertEquals(MessageRole.USER, threadMessage.getRole());
            assertFalse(threadMessage.getContent().isEmpty());
            assertEquals(assistantThread.getId(), threadMessage.getThreadId());

            // Delete the created thread
            client.deleteThread(assistantThread.getId());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void createMessageWithResponse(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient);
        createMessageRunner(message -> {
            // Create a simple thread without a message
            AssistantThread assistantThread = client.createThread(new AssistantThreadCreationOptions());

            assertNotNull(assistantThread.getId());
            assertNotNull(assistantThread.getCreatedAt());
            assertEquals("thread", assistantThread.getObject());

            Map<String, Object> requestObj = new HashMap<>();
            requestObj.put("role", MessageRole.USER);
            requestObj.put("content", message);
            BinaryData request = BinaryData.fromObject(requestObj);
            Response<BinaryData> response = client.createMessageWithResponse(assistantThread.getId(), request, new RequestOptions());

            ThreadMessage threadMessage = assertAndGetValueFromResponse(response, ThreadMessage.class, 200);
            assertNotNull(threadMessage.getId());
            assertNotNull(threadMessage.getCreatedAt());
            assertEquals("thread.message", threadMessage.getObject());
            assertEquals(MessageRole.USER, threadMessage.getRole());
            assertFalse(threadMessage.getContent().isEmpty());
            assertEquals(assistantThread.getId(), threadMessage.getThreadId());

            // Delete the created thread
            client.deleteThread(assistantThread.getId());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void submitMessageAndRun(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient);
        submitMessageAndRunRunner(message -> {
            // Create a simple thread without a message
            AssistantThread assistantThread = client.createThread(new AssistantThreadCreationOptions());
            String threadId = assistantThread.getId();

            assertNotNull(threadId);
            assertNotNull(assistantThread.getCreatedAt());
            assertEquals("thread", assistantThread.getObject());
            ThreadMessage threadMessage = client.createMessage(threadId, MessageRole.USER, message);
            assertNotNull(threadMessage.getId());
            assertNotNull(threadMessage.getCreatedAt());
            assertEquals("thread.message", threadMessage.getObject());
            assertEquals(MessageRole.USER, threadMessage.getRole());
            assertFalse(threadMessage.getContent().isEmpty());
            assertEquals(threadId, threadMessage.getThreadId());

            // Submit the message and run
            ThreadRun run = client.createRun(threadId, mathTutorAssistant.getId());
            assertNotNull(run.getId());
            assertNotNull(run.getCreatedAt());
            assertEquals("thread.run", run.getObject());
            assertEquals(mathTutorAssistant.getId(), run.getAssistantId());
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

            // Delete the created thread
            client.deleteThread(assistantThread.getId());
        });
    }

    @ParameterizedTest
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void createThreadAndRun(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient);
        createThreadAndRunRunner(createAndRunThreadOptions -> {
            // Create a simple thread without a message
            ThreadRun run = client.createThreadAndRun(createAndRunThreadOptions);

            String threadId = run.getThreadId();
            assertNotNull(run.getId());
            assertNotNull(run.getCreatedAt());
            assertEquals("thread.run", run.getObject());
            assertEquals(mathTutorAssistant.getId(), run.getAssistantId());
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
            client.deleteThread(threadId);
        }, mathTutorAssistant.getId());
    }
}
