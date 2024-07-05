// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.assistants;

import com.azure.ai.openai.assistants.models.Assistant;
import com.azure.ai.openai.assistants.models.ListSortOrder;
import com.azure.ai.openai.assistants.models.PageableList;
import com.azure.ai.openai.assistants.models.UpdateAssistantOptions;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AzureAssistantsAsyncTest extends AssistantsClientTestBase {
    private AssistantsAsyncClient client;

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void assistantCreateRetrieveUpdateDelete(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsAsyncClient(httpClient, serviceVersion);
        createAssistantsRunner(assistantCreationOptions -> {
            AtomicReference<Assistant> assistantCreated = new AtomicReference<>();
            // Create an assistant
            StepVerifier.create(client.createAssistant(assistantCreationOptions))
                    .assertNext(assistant -> {
                        assistantCreated.set(assistant);
                        assertEquals(assistantCreationOptions.getName(), assistant.getName());
                        assertEquals(assistantCreationOptions.getDescription(), assistant.getDescription());
                        assertEquals(assistantCreationOptions.getInstructions(), assistant.getInstructions());
                    })
                    .verifyComplete();
            Assistant assistant = assistantCreated.get();
            String assistantId = assistant.getId();
            // Retrieve the created assistant
            StepVerifier.create(client.getAssistant(assistantId))
                    .assertNext(retrievedAssistant -> {
                        assistantCreated.set(assistant);
                        assertEquals(assistantId, retrievedAssistant.getId());
                        assertEquals(assistant.getName(), retrievedAssistant.getName());
                        assertEquals(assistant.getDescription(), retrievedAssistant.getDescription());
                        assertEquals(assistant.getInstructions(), retrievedAssistant.getInstructions());
                        assertEquals(assistant.getTools().get(0).getClass(), retrievedAssistant.getTools().get(0).getClass());
                    })
                    .verifyComplete();
            // Update the created assistant
            String updatedName = "updatedName";
            String updatedDescription = "updatedDescription";
            String updatedInstructions = "updatedInstructions";
            StepVerifier.create(client.updateAssistant(assistantId,
                            new UpdateAssistantOptions()
                                    .setName(updatedName)
                                    .setDescription(updatedDescription)
                                    .setInstructions(updatedInstructions)))
                    .assertNext(updatedAssistant -> {
                        assertEquals(assistantId, updatedAssistant.getId());
                        assertEquals(updatedName, updatedAssistant.getName());
                        assertEquals(updatedDescription, updatedAssistant.getDescription());
                        assertEquals(updatedInstructions, updatedAssistant.getInstructions());
                        assertEquals(assistant.getTools().get(0).getClass(), updatedAssistant.getTools().get(0).getClass());
                    })
                    .verifyComplete();
            // Deleted created assistant
            StepVerifier.create(client.deleteAssistant(assistantId))
                    .assertNext(assistantDeletionStatus -> {
                        assertEquals(assistantId, assistantDeletionStatus.getId());
                        assertTrue(assistantDeletionStatus.isDeleted());
                    })
                    .verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void assistantCrudWithResponse(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsAsyncClient(httpClient, serviceVersion);
        createAssistantsRunner(assistantCreationOptions -> {
            AtomicReference<Assistant> assistantCreated = new AtomicReference<>();
            // Create an assistant
            StepVerifier.create(client.createAssistantWithResponse(BinaryData.fromObject(assistantCreationOptions),
                            new RequestOptions()))
                    .assertNext(response -> {
                        Assistant assistant = assertAndGetValueFromResponse(response, Assistant.class, 200);
                        assistantCreated.set(assistant);
                        assertEquals(assistantCreationOptions.getName(), assistant.getName());
                        assertEquals(assistantCreationOptions.getDescription(), assistant.getDescription());
                        assertEquals(assistantCreationOptions.getInstructions(), assistant.getInstructions());
                    })
                    .verifyComplete();
            Assistant assistant = assistantCreated.get();
            String assistantId = assistant.getId();
            // Retrieve the created assistant
            StepVerifier.create(client.getAssistantWithResponse(assistantId, new RequestOptions()))
                    .assertNext(response -> {
                        Assistant retrievedAssistant = assertAndGetValueFromResponse(response, Assistant.class,
                                200);
                        assertEquals(assistantId, retrievedAssistant.getId());
                        assertEquals(assistant.getName(), retrievedAssistant.getName());
                        assertEquals(assistant.getDescription(), retrievedAssistant.getDescription());
                        assertEquals(assistant.getInstructions(), retrievedAssistant.getInstructions());
                        assertEquals(assistant.getTools().get(0).getClass(), retrievedAssistant.getTools().get(0).getClass());
                    })
                    .verifyComplete();
            // Update the created assistant
            String updatedName = "updatedName";
            String updatedDescription = "updatedDescription";
            String updatedInstructions = "updatedInstructions";
            StepVerifier.create(client.updateAssistantWithResponse(assistantId,
                            BinaryData.fromObject(new UpdateAssistantOptions()
                                    .setName(updatedName)
                                    .setDescription(updatedDescription)
                                    .setInstructions(updatedInstructions)),
                            new RequestOptions()))
                    .assertNext(response -> {
                        Assistant updatedAssistant = assertAndGetValueFromResponse(response, Assistant.class,
                                200);
                        assertEquals(assistantId, updatedAssistant.getId());
                        assertEquals(updatedName, updatedAssistant.getName());
                        assertEquals(updatedDescription, updatedAssistant.getDescription());
                        assertEquals(updatedInstructions, updatedAssistant.getInstructions());
                        assertEquals(assistant.getTools().get(0).getClass(), updatedAssistant.getTools().get(0).getClass());
                    })
                    .verifyComplete();
            // Deleted created assistant
            StepVerifier.create(client.deleteAssistant(assistantId))
                    .assertNext(assistantDeletionStatus -> {
                        assertEquals(assistantId, assistantDeletionStatus.getId());
                        assertTrue(assistantDeletionStatus.isDeleted());
                    })
                    .verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void listAssistants(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsAsyncClient(httpClient, serviceVersion);
        createAssistantsRunner(assistantCreationOptions -> {
            // Create two assistants
            String assistantId1 = createAssistant(client, assistantCreationOptions.setName("assistant1"));
            String assistantId2 = createAssistant(client, assistantCreationOptions.setName("assistant2"));
            // List all the assistants; sort by name ascending
            StepVerifier.create(client.listAssistants())
                    .assertNext(assistantsAscending -> {
                        List<Assistant> dataAscending = assistantsAscending.getData();
                        assertTrue(dataAscending.size() >= 2);
                    })
                    .verifyComplete();
            // List all the assistants with response; sort by name ascending
            StepVerifier.create(client.listAssistantsWithResponse(new RequestOptions()))
                    .assertNext(response -> {
                        PageableList<Assistant> assistantsAscending = asserAndGetPageableListFromResponse(response, 200,
                            reader -> reader.readArray(Assistant::fromJson));
                        List<Assistant> dataAscending = assistantsAscending.getData();
                        assertTrue(dataAscending.size() >= 2);
                    })
                    .verifyComplete();
            // Deleted created assistants
            deleteAssistant(client, assistantId1);
            deleteAssistant(client, assistantId2);
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void listAssistantsBetweenTwoAssistantId(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsAsyncClient(httpClient, serviceVersion);
        createAssistantsRunner(assistantCreationOptions -> {
            // Create assistants
            String assistantId1 = createAssistant(client, assistantCreationOptions.setName("assistant1"));
            String assistantId2 = createAssistant(client, assistantCreationOptions.setName("assistant2"));
            String assistantId3 = createAssistant(client, assistantCreationOptions.setName("assistant3"));
            String assistantId4 = createAssistant(client, assistantCreationOptions.setName("assistant4"));

            // List only the middle two assistants; sort by name ascending
            StepVerifier.create(client.listAssistants(100, ListSortOrder.ASCENDING, assistantId1,
                            assistantId4))
                    .assertNext(assistantsAscending -> {
                        List<Assistant> dataAscending = assistantsAscending.getData();
                        // consecutive re-runs will result in more than 2 assistants, we want to check for at least 2
                        assertTrue(2 <= dataAscending.size());
                        assertEquals(assistantId2, dataAscending.get(0).getId());
                        assertEquals(assistantId3, dataAscending.get(dataAscending.size() - 1).getId());
                    })
                    .verifyComplete();

            // List only the middle two assistants; sort by name descending
            StepVerifier.create(client.listAssistants(100,
                            ListSortOrder.DESCENDING, assistantId4, assistantId1))
                    .assertNext(assistantsDescending -> {
                        List<Assistant> dataDescending = assistantsDescending.getData();
                        // consecutive re-runs will result in more than 2 assistants, we want to check for at least 2
                        assertTrue(2 <= dataDescending.size());
                        assertEquals(assistantId3, dataDescending.get(0).getId());
                        assertEquals(assistantId2, dataDescending.get(dataDescending.size() - 1).getId());
                    })
                    .verifyComplete();

            // Deleted created assistant
            deleteAssistant(client, assistantId1);
            deleteAssistant(client, assistantId2);
            deleteAssistant(client, assistantId3);
            deleteAssistant(client, assistantId4);
        });
    }
}
