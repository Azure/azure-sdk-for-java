// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.assistants;

import com.azure.ai.openai.assistants.models.Assistant;
import com.azure.ai.openai.assistants.models.AssistantDeletionStatus;
import com.azure.ai.openai.assistants.models.AssistantFile;
import com.azure.ai.openai.assistants.models.AssistantFileDeletionStatus;
import com.azure.ai.openai.assistants.models.CodeInterpreterToolDefinition;
import com.azure.ai.openai.assistants.models.UpdateAssistantOptions;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AssistantsSyncTest extends AssistantsClientTestBase {
    private AssistantsClient client;

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void assistantCrud(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
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
    public void assistantCrudWithResponse(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
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
    public void assistantFileCrd(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient);
        createAssistantsFileRunner((assistantCreationOptions, fileId) -> {
            Assistant assistant = client.createAssistant(assistantCreationOptions);
            // Create an assistant
            assertEquals(assistantCreationOptions.getName(), assistant.getName());
            assertEquals(assistantCreationOptions.getDescription(), assistant.getDescription());
            assertEquals(assistantCreationOptions.getInstructions(), assistant.getInstructions());
            assertEquals(CodeInterpreterToolDefinition.class, assistant.getTools().get(0).getClass());
            String assistantId = assistant.getId();

            // Attach a file to the assistant created above and return the assistant file
            AssistantFile assistantFile = client.createAssistantFile(assistantId, fileId);
            assertNotNull(assistantFile.getCreatedAt());
            assertEquals(assistantId, assistantFile.getAssistantId());
            assertEquals("assistant.file", assistantFile.getObject());
            assertEquals(fileId, assistantFile.getId());

            // Retrieve the assistant file
            AssistantFile retrievedAssistantFile = client.getAssistantFile(assistantId, fileId);
            assertEquals(assistantFile.getCreatedAt(), retrievedAssistantFile.getCreatedAt());
            assertEquals(assistantId, retrievedAssistantFile.getAssistantId());
            assertEquals("assistant.file", retrievedAssistantFile.getObject());
            assertEquals(fileId, retrievedAssistantFile.getId());

            // Unlinks the attached file from the assistant
            AssistantFileDeletionStatus assistantFileDeletionStatus = client.deleteAssistantFile(assistantId, fileId);
            assertEquals(fileId, assistantFileDeletionStatus.getId());
            assertTrue(assistantFileDeletionStatus.isDeleted());

            // Delete the created assistant
            AssistantDeletionStatus assistantDeletionStatus = client.deleteAssistant(assistantId);
            assertEquals(assistantId, assistantDeletionStatus.getId());
            assertTrue(assistantDeletionStatus.isDeleted());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void assistantFileCrdWithResponse(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient);
        createAssistantsFileRunner((assistantCreationOptions, fileId) -> {
            Assistant assistant = client.createAssistant(assistantCreationOptions);
            // Create an assistant
            assertEquals(assistantCreationOptions.getName(), assistant.getName());
            assertEquals(assistantCreationOptions.getDescription(), assistant.getDescription());
            assertEquals(assistantCreationOptions.getInstructions(), assistant.getInstructions());
            assertEquals(CodeInterpreterToolDefinition.class, assistant.getTools().get(0).getClass());
            String assistantId = assistant.getId();

            // Attach a file to the assistant created above and return the assistant file
            Map<String, String> requestObj = new HashMap<>();
            requestObj.put("file_id", fileId);
            BinaryData request = BinaryData.fromObject(requestObj);
            Response<BinaryData> assistantFileResponse = client.createAssistantFileWithResponse(assistantId, request,
                    new RequestOptions());

            AssistantFile assistantFile = assertAndGetValueFromResponse(assistantFileResponse, AssistantFile.class, 200);
            assertNotNull(assistantFile.getCreatedAt());
            assertEquals(assistantId, assistantFile.getAssistantId());
            assertEquals("assistant.file", assistantFile.getObject());
            assertEquals(fileId, assistantFile.getId());

            // Retrieve the assistant file
            Response<BinaryData> retrievedAssistantFileResponse = client.getAssistantFileWithResponse(assistantId, fileId, new RequestOptions());
            AssistantFile retrievedAssistantFile = assertAndGetValueFromResponse(retrievedAssistantFileResponse,
                    AssistantFile.class, 200);
            assertEquals(assistantFile.getCreatedAt(), retrievedAssistantFile.getCreatedAt());
            assertEquals(assistantId, retrievedAssistantFile.getAssistantId());
            assertEquals("assistant.file", retrievedAssistantFile.getObject());
            assertEquals(fileId, retrievedAssistantFile.getId());

            // Unlinks the attached file from the assistant
            Response<BinaryData> assistantFileDeletionStatusResponse = client.deleteAssistantFileWithResponse(
                    assistantId, fileId, new RequestOptions());
            AssistantFileDeletionStatus assistantFileDeletionStatus = assertAndGetValueFromResponse(
                    assistantFileDeletionStatusResponse, AssistantFileDeletionStatus.class, 200);
            assertEquals(fileId, assistantFileDeletionStatus.getId());
            assertTrue(assistantFileDeletionStatus.isDeleted());

            // Delete the created assistant
            AssistantDeletionStatus assistantDeletionStatus = client.deleteAssistant(assistantId);
            assertEquals(assistantId, assistantDeletionStatus.getId());
            assertTrue(assistantDeletionStatus.isDeleted());
        });
    }
}
