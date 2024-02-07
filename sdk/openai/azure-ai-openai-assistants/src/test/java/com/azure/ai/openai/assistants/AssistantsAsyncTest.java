// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.assistants;

import com.azure.ai.openai.assistants.models.Assistant;
import com.azure.ai.openai.assistants.models.AssistantFile;
import com.azure.ai.openai.assistants.models.AssistantFileDeletionStatus;
import com.azure.ai.openai.assistants.models.ListSortOrder;
import com.azure.ai.openai.assistants.models.PageableList;
import com.azure.ai.openai.assistants.models.UpdateAssistantOptions;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;
import com.azure.core.util.serializer.TypeReference;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.ai.openai.assistants.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AssistantsAsyncTest extends AssistantsClientTestBase {
    private AssistantsAsyncClient client;

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void assistantCreateRetrieveUpdateDelete(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsAsyncClient(httpClient);
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
        client = getAssistantsAsyncClient(httpClient);
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
        client = getAssistantsAsyncClient(httpClient);
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
                        PageableList<Assistant> assistantsAscending = assertAndGetValueFromResponse(response,
                            new TypeReference<PageableList<Assistant>>() {}, 200);
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
        client = getAssistantsAsyncClient(httpClient);
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
                        assertEquals(2, dataAscending.size());
                        assertEquals(assistantId2, dataAscending.get(0).getId());
                        assertEquals(assistantId3, dataAscending.get(1).getId());
                    })
                    .verifyComplete();

            // List only the middle two assistants; sort by name descending
            StepVerifier.create(client.listAssistants(100,
                    ListSortOrder.DESCENDING, assistantId4, assistantId1))
                    .assertNext(assistantsDescending -> {
                        List<Assistant> dataDescending = assistantsDescending.getData();
                        assertEquals(2, dataDescending.size());
                        assertEquals(assistantId3, dataDescending.get(0).getId());
                        assertEquals(assistantId2, dataDescending.get(1).getId());
                    })
                    .verifyComplete();

            // Deleted created assistant
            deleteAssistant(client, assistantId1);
            deleteAssistant(client, assistantId2);
            deleteAssistant(client, assistantId3);
            deleteAssistant(client, assistantId4);
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void assistantFileCreateRetrieveDelete(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsAsyncClient(httpClient);
        String assistantId = createMathTutorAssistant(client);
        String fileId = uploadFile(client);
        createAssistantsFileRunner(assistantCreationOptions -> {
            // Attach a file to the assistant created above and return the assistant file
            StepVerifier.create(client.createAssistantFile(assistantId, fileId))
                    .assertNext(assistantFile -> {
                        assertNotNull(assistantFile.getCreatedAt());
                        assertEquals(assistantId, assistantFile.getAssistantId());
                        assertEquals("assistant.file", assistantFile.getObject());
                        assertEquals(fileId, assistantFile.getId());
                    })
                    .verifyComplete();

            // Retrieve the assistant file
            StepVerifier.create(client.getAssistantFile(assistantId, fileId))
                    .assertNext(retrievedAssistantFile -> {
                        assertEquals(retrievedAssistantFile.getCreatedAt(), retrievedAssistantFile.getCreatedAt());
                        assertEquals(assistantId, retrievedAssistantFile.getAssistantId());
                        assertEquals("assistant.file", retrievedAssistantFile.getObject());
                        assertEquals(fileId, retrievedAssistantFile.getId());
                    })
                    .verifyComplete();

            // Unlinks the attached file from the assistant
            StepVerifier.create(client.deleteAssistantFile(assistantId, fileId))
                    .assertNext(assistantFileDeletionStatus -> {
                        assertEquals(fileId, assistantFileDeletionStatus.getId());
                        assertTrue(assistantFileDeletionStatus.isDeleted());
                    })
                    .verifyComplete();

        });
        deleteFile(client, fileId);
        deleteAssistant(client, assistantId);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void assistantFileCreateRetrieveDeleteWithResponse(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsAsyncClient(httpClient);
        String assistantId = createMathTutorAssistant(client);
        String fileId = uploadFile(client);
        createAssistantsFileRunner(assistantCreationOptions -> {
            // Attach a file to the assistant created above and return the assistant file
            Map<String, String> requestObj = new HashMap<>();
            requestObj.put("file_id", fileId);
            BinaryData request = BinaryData.fromObject(requestObj);
            StepVerifier.create(client.createAssistantFileWithResponse(assistantId, request, new RequestOptions()))
                    .assertNext(response -> {
                        AssistantFile assistantFile = assertAndGetValueFromResponse(response, AssistantFile.class, 200);
                        assertNotNull(assistantFile.getCreatedAt());
                        assertEquals(assistantId, assistantFile.getAssistantId());
                        assertEquals("assistant.file", assistantFile.getObject());
                        assertEquals(fileId, assistantFile.getId());
                    })
                    .verifyComplete();

            // Retrieve the assistant file
            StepVerifier.create(client.getAssistantFileWithResponse(assistantId, fileId, new RequestOptions()))
                    .assertNext(response -> {
                        AssistantFile retrievedAssistantFile = assertAndGetValueFromResponse(response,
                                AssistantFile.class, 200);
                        assertNotNull(retrievedAssistantFile.getCreatedAt());
                        assertEquals(assistantId, retrievedAssistantFile.getAssistantId());
                        assertEquals("assistant.file", retrievedAssistantFile.getObject());
                        assertEquals(fileId, retrievedAssistantFile.getId());
                    })
                    .verifyComplete();


            // Unlinks the attached file from the assistant
            StepVerifier.create(client.deleteAssistantFileWithResponse(assistantId, fileId, new RequestOptions()))
                    .assertNext(response -> {
                        AssistantFileDeletionStatus assistantFileDeletionStatus = assertAndGetValueFromResponse(
                                response, AssistantFileDeletionStatus.class, 200);
                        assertEquals(fileId, assistantFileDeletionStatus.getId());
                        assertTrue(assistantFileDeletionStatus.isDeleted());
                    })
                    .verifyComplete();

        });
        deleteFile(client, fileId);
        deleteAssistant(client, assistantId);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void listAssistantFiles(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsAsyncClient(httpClient);
        String assistantId = createMathTutorAssistant(client);
        String fileId = uploadFile(client);
        createAssistantsFileRunner(assistantCreationOptions -> {
            // Attach a file to the assistant created above and return the assistant file
            StepVerifier.create(client.createAssistantFile(assistantId, fileId))
                    .assertNext(assistantFile -> {
                        assertNotNull(assistantFile.getCreatedAt());
                        assertEquals(assistantId, assistantFile.getAssistantId());
                        assertEquals("assistant.file", assistantFile.getObject());
                        assertEquals(fileId, assistantFile.getId());
                    })
                    .verifyComplete();

            StepVerifier.create(client.listAssistantFiles(assistantId))
                    .assertNext(assistantFiles -> {
                        List<AssistantFile> assistantFilesData = assistantFiles.getData();
                        assertEquals(1, assistantFilesData.size());
                        AssistantFile assistantFileOnly = assistantFilesData.get(0);
                        assertEquals(assistantId, assistantFileOnly.getAssistantId());
                        assertEquals("assistant.file", assistantFileOnly.getObject());
                        assertEquals(fileId, assistantFileOnly.getId());
                    })
                    .verifyComplete();

            StepVerifier.create(client.listAssistantFilesWithResponse(assistantId,
                            new RequestOptions()))
                    .assertNext(response -> {
                        PageableList<AssistantFile> assistantFileList = assertAndGetValueFromResponse(response,
                            new TypeReference<PageableList<AssistantFile>>() {}, 200);
                        List<AssistantFile> assistantFilesData = assistantFileList.getData();
                        assertEquals(1, assistantFilesData.size());
                        AssistantFile assistantFileOnly = assistantFilesData.get(0);
                        assertEquals(assistantId, assistantFileOnly.getAssistantId());
                        assertEquals("assistant.file", assistantFileOnly.getObject());
                        assertEquals(fileId, assistantFileOnly.getId());
                    })
                    .verifyComplete();

        });
        deleteFile(client, fileId);
        deleteAssistant(client, assistantId);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void listAssistantFilesAddSameFile(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsAsyncClient(httpClient);
        String assistantId = createMathTutorAssistant(client);
        String fileId = uploadFile(client);
        createAssistantsFileRunner(assistantCreationOptions -> {
            // Attach a file to the assistant created above and return the assistant file
            StepVerifier.create(client.createAssistantFile(assistantId, fileId))
                    .assertNext(assistantFile -> {
                        assertNotNull(assistantFile.getCreatedAt());
                        assertEquals(assistantId, assistantFile.getAssistantId());
                        assertEquals("assistant.file", assistantFile.getObject());
                        assertEquals(fileId, assistantFile.getId());
                    }).verifyComplete();

            StepVerifier.create(client.createAssistantFile(assistantId, fileId))
                    .assertNext(assistantFile -> {
                        assertNotNull(assistantFile.getCreatedAt());
                        assertEquals(assistantId, assistantFile.getAssistantId());
                        assertEquals("assistant.file", assistantFile.getObject());
                        assertEquals(fileId, assistantFile.getId());
                    }).verifyComplete();

            // Listing will only return one file
            StepVerifier.create(client.listAssistantFiles(assistantId, 100, ListSortOrder.ASCENDING, null, null))
                    .assertNext(assistantFilesAscending -> {
                        List<AssistantFile> dataAscending = assistantFilesAscending.getData();
                        assertEquals(1, dataAscending.size());
                        assertEquals(fileId, dataAscending.get(0).getId());
                    })
                    .verifyComplete();

        });
        deleteFile(client, fileId);
        deleteAssistant(client, assistantId);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void listAssistantFilesBetweenTwoAssistantId(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsAsyncClient(httpClient);
        String assistantId = createMathTutorAssistant(client);
        String fileId1 = uploadFile(client);
        String fileId2 = uploadFile(client);
        String fileId3 = uploadFile(client);
        String fileId4 = uploadFile(client);
        createAssistantsFileRunner(assistantCreationOptions -> {
            AtomicReference<AssistantFile> fileAtomicReference1 = new AtomicReference<>();
            AtomicReference<AssistantFile> fileAtomicReference2 = new AtomicReference<>();
            AtomicReference<AssistantFile> fileAtomicReference3 = new AtomicReference<>();
            AtomicReference<AssistantFile> fileAtomicReference4 = new AtomicReference<>();

            // Attach a file to the assistant created above and return the assistant file
            StepVerifier.create(client.createAssistantFile(assistantId, fileId1))
                    .assertNext(assistantFile -> {
                        fileAtomicReference1.set(assistantFile);
                        assertNotNull(assistantFile.getCreatedAt());
                        assertEquals(assistantId, assistantFile.getAssistantId());
                        assertEquals("assistant.file", assistantFile.getObject());
                        assertEquals(fileId1, assistantFile.getId());
                    })
                    .verifyComplete();

            StepVerifier.create(client.createAssistantFile(assistantId, fileId2))
                    .assertNext(assistantFile -> {
                        fileAtomicReference2.set(assistantFile);
                        assertNotNull(assistantFile.getCreatedAt());
                        assertEquals(assistantId, assistantFile.getAssistantId());
                        assertEquals("assistant.file", assistantFile.getObject());
                        assertEquals(fileId2, assistantFile.getId());
                    })
                    .verifyComplete();

            StepVerifier.create(client.createAssistantFile(assistantId, fileId3))
                    .assertNext(assistantFile -> {
                        fileAtomicReference3.set(assistantFile);
                        assertNotNull(assistantFile.getCreatedAt());
                        assertEquals(assistantId, assistantFile.getAssistantId());
                        assertEquals("assistant.file", assistantFile.getObject());
                        assertEquals(fileId3, assistantFile.getId());
                    })
                    .verifyComplete();

            StepVerifier.create(client.createAssistantFile(assistantId, fileId4))
                    .assertNext(assistantFile -> {
                        fileAtomicReference4.set(assistantFile);
                        assertNotNull(assistantFile.getCreatedAt());
                        assertEquals(assistantId, assistantFile.getAssistantId());
                        assertEquals("assistant.file", assistantFile.getObject());
                        assertEquals(fileId4, assistantFile.getId());
                    })
                    .verifyComplete();
            AssistantFile assistantFile1 = fileAtomicReference1.get();
            AssistantFile assistantFile2 = fileAtomicReference2.get();
            AssistantFile assistantFile3 = fileAtomicReference3.get();
            AssistantFile assistantFile4 = fileAtomicReference4.get();

            assertEquals(assistantId, assistantFile1.getAssistantId());
            assertEquals(assistantId, assistantFile2.getAssistantId());
            assertEquals(assistantId, assistantFile3.getAssistantId());
            assertEquals(assistantId, assistantFile4.getAssistantId());

            // List only the middle two assistants; sort by name ascending
            StepVerifier.create(client.listAssistantFiles(assistantId, 100,
                            ListSortOrder.ASCENDING, assistantFile1.getId(), assistantFile4.getId()))
                    .assertNext(assistantFiles -> {
                        List<AssistantFile> dataAscending = assistantFiles.getData();
                        assertEquals(2, dataAscending.size());
                        assertEquals(assistantFile2.getId(), dataAscending.get(0).getId());
                        assertEquals(assistantFile3.getId(), dataAscending.get(1).getId());
                    })
                    .verifyComplete();
        });
        deleteFile(client, fileId1);
        deleteFile(client, fileId2);
        deleteFile(client, fileId3);
        deleteFile(client, fileId4);
        deleteAssistant(client, assistantId);
    }
}
