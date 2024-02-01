// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.assistants;

import com.azure.ai.openai.assistants.models.Assistant;
import com.azure.ai.openai.assistants.models.AssistantDeletionStatus;
import com.azure.ai.openai.assistants.models.AssistantFile;
import com.azure.ai.openai.assistants.models.AssistantFileDeletionStatus;
import com.azure.ai.openai.assistants.models.ListSortOrder;
import com.azure.ai.openai.assistants.models.OpenAIPageableListOfAssistant;
import com.azure.ai.openai.assistants.models.OpenAIPageableListOfAssistantFile;
import com.azure.ai.openai.assistants.models.UpdateAssistantOptions;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.azure.ai.openai.assistants.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AssistantsSyncTest extends AssistantsClientTestBase {
    private AssistantsClient client;

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void assistantCreateRetrieveUpdateDelete(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
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
    public void listAssistants(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient);
        createAssistantsRunner(assistantCreationOptions -> {
            // Create assistants
            String assistantId1 = createAssistant(client, assistantCreationOptions.setName("assistant1"));
            String assistantId2 = createAssistant(client, assistantCreationOptions.setName("assistant2"));

            OpenAIPageableListOfAssistant assistantsAscending = client.listAssistants();
            List<Assistant> dataAscending = assistantsAscending.getData();
            assertTrue(dataAscending.size() >= 2);

            Response<BinaryData> response = client.listAssistantsWithResponse(new RequestOptions());
            OpenAIPageableListOfAssistant assistantsAscendingResponse = assertAndGetValueFromResponse(response,
                    OpenAIPageableListOfAssistant.class, 200);
            List<Assistant> dataAscendingResponse = assistantsAscendingResponse.getData();
            assertTrue(dataAscendingResponse.size() >= 2);

            // Deleted created assistant
            deleteAssistant(client, assistantId1);
            deleteAssistant(client, assistantId2);
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void listAssistantsBetweenTwoAssistantId(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient);
        createAssistantsRunner(assistantCreationOptions -> {
            // Create assistants
            String assistantId1 = createAssistant(client, assistantCreationOptions.setName("assistant1"));
            String assistantId2 = createAssistant(client, assistantCreationOptions.setName("assistant2"));
            String assistantId3 = createAssistant(client, assistantCreationOptions.setName("assistant3"));
            String assistantId4 = createAssistant(client, assistantCreationOptions.setName("assistant4"));

            // List only the middle two assistants; sort by name ascending
            OpenAIPageableListOfAssistant assistantsAscending = client.listAssistants(100,
                    ListSortOrder.ASCENDING, assistantId1, assistantId4);
            List<Assistant> dataAscending = assistantsAscending.getData();
            assertEquals(2, dataAscending.size());
            assertEquals(assistantId2, dataAscending.get(0).getId());
            assertEquals(assistantId3, dataAscending.get(1).getId());

            // List only the middle two assistants; sort by name descending
            OpenAIPageableListOfAssistant assistantsDescending = client.listAssistants(100,
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

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void assistantFileCreateRetrieveDelete(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient);
        String assistantId = createMathTutorAssistant(client);
        String fileId = uploadFile(client);
        createAssistantsFileRunner(assistantCreationOptions -> {
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
        });
        deleteFile(client, fileId);
        deleteAssistant(client, assistantId);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void assistantFileCreateRetrieveDeleteWithResponse(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient);
        String assistantId = createMathTutorAssistant(client);
        String fileId = uploadFile(client);
        createAssistantsFileRunner(assistantCreationOptions -> {
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
        });
        deleteFile(client, fileId);
        deleteAssistant(client, assistantId);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void listAssistantFiles(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient);
        String assistantId = createMathTutorAssistant(client);
        String fileId = uploadFile(client);
        createAssistantsFileRunner(assistantCreationOptions -> {
            // Attach a file to the assistant created above and return the assistant file
            AssistantFile assistantFile = client.createAssistantFile(assistantId, fileId);
            assertNotNull(assistantFile.getCreatedAt());
            assertEquals(assistantId, assistantFile.getAssistantId());
            assertEquals("assistant.file", assistantFile.getObject());
            assertEquals(fileId, assistantFile.getId());

            OpenAIPageableListOfAssistantFile assistantFiles = client.listAssistantFiles(assistantId);

            List<AssistantFile> assistantFilesData = assistantFiles.getData();
            assertEquals(1, assistantFilesData.size());
            AssistantFile assistantFileOnly = assistantFilesData.get(0);
            assertEquals(assistantId, assistantFileOnly.getAssistantId());
            assertEquals("assistant.file", assistantFileOnly.getObject());
            assertEquals(fileId, assistantFileOnly.getId());
        });
        deleteFile(client, fileId);
        deleteAssistant(client, assistantId);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void listAssistantFilesAddSameFile(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient);
        String assistantId = createMathTutorAssistant(client);
        String fileId = uploadFile(client);
        createAssistantsFileRunner(assistantCreationOptions -> {
            // Attach a file to the assistant created above and return the assistant file
            AssistantFile assistantFile1 = client.createAssistantFile(assistantId, fileId);
            AssistantFile assistantFile2 = client.createAssistantFile(assistantId, fileId);
            assertEquals(assistantId, assistantFile1.getAssistantId());
            assertEquals(assistantFile1.getId(), assistantFile2.getId());

            // Listing will only return one file
            OpenAIPageableListOfAssistantFile assistantFilesAscending = client.listAssistantFiles(assistantId, 100,
                    ListSortOrder.ASCENDING, null, null);
            List<AssistantFile> dataAscending = assistantFilesAscending.getData();
            assertEquals(1, dataAscending.size());
            assertEquals(fileId, dataAscending.get(0).getId());
        });
        deleteFile(client, fileId);
        deleteAssistant(client, assistantId);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void listAssistantFilesWithResponse(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient);
        String assistantId = createMathTutorAssistant(client);
        String fileId = uploadFile(client);
        createAssistantsFileRunner(assistantCreationOptions -> {
            // Attach a file to the assistant created above and return the assistant file
            AssistantFile assistantFile = client.createAssistantFile(assistantId, fileId);
            assertNotNull(assistantFile.getCreatedAt());
            assertEquals(assistantId, assistantFile.getAssistantId());
            assertEquals("assistant.file", assistantFile.getObject());
            assertEquals(fileId, assistantFile.getId());

            Response<BinaryData> response = client.listAssistantFilesWithResponse(assistantId,
                    new RequestOptions());

            OpenAIPageableListOfAssistantFile assistantFileList = assertAndGetValueFromResponse(response,
                    OpenAIPageableListOfAssistantFile.class, 200);
            List<AssistantFile> assistantFilesData = assistantFileList.getData();
            assertEquals(1, assistantFilesData.size());
            AssistantFile assistantFileOnly = assistantFilesData.get(0);
            assertEquals(assistantId, assistantFileOnly.getAssistantId());
            assertEquals("assistant.file", assistantFileOnly.getObject());
            assertEquals(fileId, assistantFileOnly.getId());
        });
        deleteFile(client, fileId);
        deleteAssistant(client, assistantId);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void listAssistantFilesBetweenTwoAssistantId(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient);
        String assistantId = createMathTutorAssistant(client);
        String fileId1 = uploadFile(client);
        String fileId2 = uploadFile(client);
        String fileId3 = uploadFile(client);
        String fileId4 = uploadFile(client);

        createAssistantsFileRunner(assistantCreationOptions -> {
            // Attach a file to the assistant created above and return the assistant file
            AssistantFile assistantFile1 = client.createAssistantFile(assistantId, fileId1);
            AssistantFile assistantFile2 = client.createAssistantFile(assistantId, fileId2);
            AssistantFile assistantFile3 = client.createAssistantFile(assistantId, fileId3);
            AssistantFile assistantFile4 = client.createAssistantFile(assistantId, fileId4);
            assertEquals(assistantId, assistantFile1.getAssistantId());
            assertEquals(assistantId, assistantFile2.getAssistantId());
            assertEquals(assistantId, assistantFile3.getAssistantId());
            assertEquals(assistantId, assistantFile4.getAssistantId());
            // List only the middle two assistants; sort by name ascending
            OpenAIPageableListOfAssistantFile assistantFilesAscending = client.listAssistantFiles(assistantId, 100,
                    ListSortOrder.ASCENDING, assistantFile1.getId(), assistantFile4.getId());
            List<AssistantFile> dataAscending = assistantFilesAscending.getData();
            assertEquals(2, dataAscending.size());
            assertEquals(assistantFile2.getId(), dataAscending.get(0).getId());
            assertEquals(assistantFile3.getId(), dataAscending.get(1).getId());
        });

        deleteFile(client, fileId1);
        deleteFile(client, fileId2);
        deleteFile(client, fileId3);
        deleteFile(client, fileId4);
        deleteAssistant(client, assistantId);
    }
}
