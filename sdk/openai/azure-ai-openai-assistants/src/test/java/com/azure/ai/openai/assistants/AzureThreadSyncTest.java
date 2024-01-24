// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.assistants;

import com.azure.ai.openai.assistants.models.Assistant;
import com.azure.ai.openai.assistants.models.AssistantCreationOptions;
import com.azure.ai.openai.assistants.models.AssistantDeletionStatus;
import com.azure.ai.openai.assistants.models.AssistantThread;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static com.azure.ai.openai.assistants.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AzureThreadSyncTest extends AssistantsClientTestBase {
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

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void createThread(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient, serviceVersion);
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
        client = getAssistantsClient(httpClient, serviceVersion);
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
}
