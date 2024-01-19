// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.assistants;

import com.azure.ai.openai.assistants.models.Assistant;
import com.azure.ai.openai.assistants.models.AssistantDeletionStatus;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.util.concurrent.atomic.AtomicReference;

import static com.azure.ai.openai.assistants.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AzureAssistantsAsyncClientTest extends AssistantsClientTestBase {
    private AssistantsAsyncClient client;

    private AssistantsAsyncClient getAssistantsAsyncClient(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        return getAzureAssistantsClientBuilder(buildAssertingClient(
                interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient,
                false), serviceVersion)
                .buildAsyncClient();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void createAndThenDeleteAssistant(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getAssistantsAsyncClient(httpClient, serviceVersion);
        createAssistantsRunner(assistantCreationOptions -> {
            AtomicReference<String> assistantId = new AtomicReference<>();
            // create assistant test
            StepVerifier.create(client.createAssistant(assistantCreationOptions))
                    .assertNext(assistant -> {
                        assistantId.set(assistant.getId());
                        assertEquals(assistantCreationOptions.getName(), assistant.getName());
                        assertEquals(assistantCreationOptions.getDescription(), assistant.getDescription());
                        assertEquals(assistantCreationOptions.getInstructions(), assistant.getInstructions());
                    })
                    .verifyComplete();

            // Deleted created assistant
            StepVerifier.create(client.deleteAssistant(assistantId.get()))
                    .assertNext(assistantDeletionStatus -> {
                        assertEquals(assistantId.get(), assistantDeletionStatus.getId());
                        assertTrue(assistantDeletionStatus.isDeleted());
                    })
                    .verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void createAndThenDeleteAssistantWithResponse(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getAssistantsAsyncClient(httpClient, serviceVersion);
        createAssistantsRunner(assistantCreationOptions -> {
            AtomicReference<String> assistantId = new AtomicReference<>();
            // create assistant test
            StepVerifier.create(client.createAssistantWithResponse(BinaryData.fromObject(assistantCreationOptions),
                            new RequestOptions()))
                    .assertNext(response -> {
                        Assistant assistant = assertAndGetValueFromResponse(response, Assistant.class, 200);
                        assistantId.set(assistant.getId());
                        assertEquals(assistantCreationOptions.getName(), assistant.getName());
                        assertEquals(assistantCreationOptions.getDescription(), assistant.getDescription());
                        assertEquals(assistantCreationOptions.getInstructions(), assistant.getInstructions());
                    })
                    .verifyComplete();

            // Deleted created assistant
            StepVerifier.create(client.deleteAssistantWithResponse(assistantId.get(), new RequestOptions()))
                    .assertNext(response -> {
                        AssistantDeletionStatus deletionStatus = assertAndGetValueFromResponse(response,
                                AssistantDeletionStatus.class, 200);
                        assertEquals(assistantId.get(), deletionStatus.getId());
                        assertTrue(deletionStatus.isDeleted());
                    })
                    .verifyComplete();
        });
    }
}
