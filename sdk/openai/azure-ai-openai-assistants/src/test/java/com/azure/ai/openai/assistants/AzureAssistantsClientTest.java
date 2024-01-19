// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.assistants;

import com.azure.ai.openai.assistants.models.Assistant;
import com.azure.ai.openai.assistants.models.AssistantDeletionStatus;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static com.azure.ai.openai.assistants.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AzureAssistantsClientTest extends AssistantsClientTestBase {
    private AssistantsClient client;

    private AssistantsClient getAssistantsClient(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        return getAzureAssistantsClientBuilder(buildAssertingClient(
                interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient, true),
                serviceVersion)
                .buildClient();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void createAndThenDeleteAssistant(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient, serviceVersion);
        createAssistantsRunner(assistantCreationOptions -> {
            Assistant assistant = client.createAssistant(assistantCreationOptions);
            // Create an assistant
            assertEquals(assistantCreationOptions.getName(), assistant.getName());
            assertEquals(assistantCreationOptions.getDescription(), assistant.getDescription());
            assertEquals(assistantCreationOptions.getInstructions(), assistant.getInstructions());
            // Delete the created assistant
            AssistantDeletionStatus assistantDeletionStatus = client.deleteAssistant(assistant.getId());
            assertEquals(assistant.getId(), assistantDeletionStatus.getId());
            assertTrue(assistantDeletionStatus.isDeleted());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void createAndThenDeleteAssistantWithResponse(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient, serviceVersion);
        createAssistantsRunner(assistantCreationOptions -> {
            Response<BinaryData> response = client.createAssistantWithResponse(BinaryData.fromObject(assistantCreationOptions), new RequestOptions());

            Assistant assistant = assertAndGetValueFromResponse(response, Assistant.class, 200);
            // Create an assistant
            assertEquals(assistantCreationOptions.getName(), assistant.getName());
            assertEquals(assistantCreationOptions.getDescription(), assistant.getDescription());
            assertEquals(assistantCreationOptions.getInstructions(), assistant.getInstructions());
            // Delete the created assistant
            Response<BinaryData> deletionStatusResponse = client.deleteAssistantWithResponse(assistant.getId(), new RequestOptions());
            AssistantDeletionStatus deletionStatus = assertAndGetValueFromResponse(deletionStatusResponse, AssistantDeletionStatus.class, 200);
            assertEquals(assistant.getId(), deletionStatus.getId());
            assertTrue(deletionStatus.isDeleted());
        });
    }
}
