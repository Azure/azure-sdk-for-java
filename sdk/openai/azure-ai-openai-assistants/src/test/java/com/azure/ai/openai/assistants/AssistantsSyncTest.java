// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.assistants;

import com.azure.ai.openai.assistants.models.Assistant;
import com.azure.ai.openai.assistants.models.AssistantDeletionStatus;
import com.azure.ai.openai.assistants.models.ListSortOrder;
import com.azure.ai.openai.assistants.models.PageableList;
import com.azure.ai.openai.assistants.models.UpdateAssistantOptions;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

import static com.azure.ai.openai.assistants.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AssistantsSyncTest extends AssistantsClientTestBase {
    private AssistantsClient client;

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void assistantCreateRetrieveUpdateDelete(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient);
        createAssistantsRunner(assistantCreationOptions -> {
            Assistant assistant = client.createAssistant(assistantCreationOptions);
            // Create an assistant
            assertEquals(assistantCreationOptions.getName(), assistant.getName());
            assertEquals(assistantCreationOptions.getDescription(), assistant.getDescription());
            assertEquals(assistantCreationOptions.getInstructions(), assistant.getInstructions());

            // Retrieve the created assistant
            Assistant retrievedAssistant = client.getAssistant(assistant.getId());
            assertEquals(assistant.getId(), retrievedAssistant.getId());
            assertEquals(assistant.getName(), retrievedAssistant.getName());
            assertEquals(assistant.getDescription(), retrievedAssistant.getDescription());
            assertEquals(assistant.getInstructions(), retrievedAssistant.getInstructions());
            assertEquals(assistant.getTools().get(0).getClass(), retrievedAssistant.getTools().get(0).getClass());

            // Update the created assistant
            String updatedName = "updatedName";
            String updatedDescription = "updatedDescription";
            String updatedInstructions = "updatedInstructions";
            Assistant updatedAssistant = client.updateAssistant(assistant.getId(),
                    new UpdateAssistantOptions()
                            .setName(updatedName)
                            .setDescription(updatedDescription)
                            .setInstructions(updatedInstructions));
            assertEquals(assistant.getId(), updatedAssistant.getId());
            assertEquals(updatedName, updatedAssistant.getName());
            assertEquals(updatedDescription, updatedAssistant.getDescription());
            assertEquals(updatedInstructions, updatedAssistant.getInstructions());
            assertEquals(assistant.getTools().get(0).getClass(), updatedAssistant.getTools().get(0).getClass());

            // Delete the created assistant
            AssistantDeletionStatus assistantDeletionStatus = client.deleteAssistant(assistant.getId());
            assertEquals(assistant.getId(), assistantDeletionStatus.getId());
            assertTrue(assistantDeletionStatus.isDeleted());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void assistantCrudWithResponse(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient);
        createAssistantsRunner(assistantCreationOptions -> {
            // Create an assistant
            Response<BinaryData> response = client.createAssistantWithResponse(BinaryData.fromObject(assistantCreationOptions), new RequestOptions());
            Assistant assistant = assertAndGetValueFromResponse(response, Assistant.class, 200);
            assertEquals(assistantCreationOptions.getName(), assistant.getName());
            assertEquals(assistantCreationOptions.getDescription(), assistant.getDescription());
            assertEquals(assistantCreationOptions.getInstructions(), assistant.getInstructions());


            // Retrieve the created assistant
            Response<BinaryData> retrievedAssistantResponse = client.getAssistantWithResponse(assistant.getId(),
                    new RequestOptions());
            Assistant retrievedAssistant = assertAndGetValueFromResponse(retrievedAssistantResponse, Assistant.class,
                    200);
            assertEquals(assistant.getId(), retrievedAssistant.getId());
            assertEquals(assistant.getName(), retrievedAssistant.getName());
            assertEquals(assistant.getDescription(), retrievedAssistant.getDescription());
            assertEquals(assistant.getInstructions(), retrievedAssistant.getInstructions());
            assertEquals(assistant.getTools().get(0).getClass(), retrievedAssistant.getTools().get(0).getClass());

            // Update the created assistant
            String updatedName = "updatedName";
            String updatedDescription = "updatedDescription";
            String updatedInstructions = "updatedInstructions";
            Response<BinaryData> updatedAssistantWithResponse = client.updateAssistantWithResponse(assistant.getId(),
                    BinaryData.fromObject(new UpdateAssistantOptions()
                            .setName(updatedName)
                            .setDescription(updatedDescription)
                            .setInstructions(updatedInstructions)),
                    new RequestOptions());
            Assistant updatedAssistant = assertAndGetValueFromResponse(updatedAssistantWithResponse, Assistant.class,
                    200);
            assertEquals(assistant.getId(), updatedAssistant.getId());
            assertEquals(updatedName, updatedAssistant.getName());
            assertEquals(updatedDescription, updatedAssistant.getDescription());
            assertEquals(updatedInstructions, updatedAssistant.getInstructions());
            assertEquals(assistant.getTools().get(0).getClass(), updatedAssistant.getTools().get(0).getClass());

            // Delete the created assistant
            Response<BinaryData> deletionStatusResponse = client.deleteAssistantWithResponse(assistant.getId(), new RequestOptions());
            AssistantDeletionStatus deletionStatus = assertAndGetValueFromResponse(deletionStatusResponse, AssistantDeletionStatus.class, 200);
            assertEquals(assistant.getId(), deletionStatus.getId());
            assertTrue(deletionStatus.isDeleted());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void listAssistants(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient);
        createAssistantsRunner(assistantCreationOptions -> {
            // Create assistants
            String assistantId1 = createAssistant(client, assistantCreationOptions.setName("assistant1"));
            String assistantId2 = createAssistant(client, assistantCreationOptions.setName("assistant2"));

            PageableList<Assistant> assistantsAscending = client.listAssistants();
            List<Assistant> dataAscending = assistantsAscending.getData();
            assertTrue(dataAscending.size() >= 2);

            Response<BinaryData> response = client.listAssistantsWithResponse(new RequestOptions());
            PageableList<Assistant> assistantsAscendingResponse = asserAndGetPageableListFromResponse(response, 200,
                reader -> reader.readArray(Assistant::fromJson));
            List<Assistant> dataAscendingResponse = assistantsAscendingResponse.getData();
            assertTrue(dataAscendingResponse.size() >= 2);

            // Deleted created assistant
            deleteAssistant(client, assistantId1);
            deleteAssistant(client, assistantId2);
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void listAssistantsBetweenTwoAssistantId(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient);
        createAssistantsRunner(assistantCreationOptions -> {
            // Create assistants
            String assistantId1 = createAssistant(client, assistantCreationOptions.setName("assistant1"));
            String assistantId2 = createAssistant(client, assistantCreationOptions.setName("assistant2"));
            String assistantId3 = createAssistant(client, assistantCreationOptions.setName("assistant3"));
            String assistantId4 = createAssistant(client, assistantCreationOptions.setName("assistant4"));

            // List only the middle two assistants; sort by name ascending
            PageableList<Assistant> assistantsAscending = client.listAssistants(100,
                    ListSortOrder.ASCENDING, assistantId1, assistantId4);
            List<Assistant> dataAscending = assistantsAscending.getData();
            assertEquals(2, dataAscending.size());
            assertEquals(assistantId2, dataAscending.get(0).getId());
            assertEquals(assistantId3, dataAscending.get(1).getId());

            // List only the middle two assistants; sort by name descending
            PageableList<Assistant> assistantsDescending = client.listAssistants(100,
                    ListSortOrder.DESCENDING, assistantId4, assistantId1);
            List<Assistant> dataDescending = assistantsDescending.getData();
            assertEquals(2, dataDescending.size());
            assertEquals(assistantId3, dataDescending.get(0).getId());
            assertEquals(assistantId2, dataDescending.get(1).getId());

            // Deleted created assistant
            deleteAssistant(client, assistantId1);
            deleteAssistant(client, assistantId2);
            deleteAssistant(client, assistantId3);
            deleteAssistant(client, assistantId4);
        });
    }
}
