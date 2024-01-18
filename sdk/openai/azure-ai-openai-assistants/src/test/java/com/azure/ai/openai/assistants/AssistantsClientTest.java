// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.assistants;

import com.azure.ai.openai.assistants.models.Assistant;
import com.azure.ai.openai.assistants.models.AssistantDeletionStatus;
import com.azure.core.http.HttpClient;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static com.azure.ai.openai.assistants.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AssistantsClientTest extends AssistantsClientTestBase {
    private AssistantsClient client;

    private AssistantsClient getAssistantsClient(HttpClient httpClient) {
        return getAssistantsClientBuilder(
                interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient)
                .buildClient();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void createAndThenDeleteAssistant(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient);
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
}
