// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.assistants;

import com.azure.ai.openai.assistants.models.Assistant;
import com.azure.ai.openai.assistants.models.AssistantFile;
import com.azure.ai.openai.assistants.models.AssistantFileDeletionStatus;
import com.azure.ai.openai.assistants.models.ListSortOrder;
import com.azure.ai.openai.assistants.models.OpenAIPageableListOfAssistant;
import com.azure.ai.openai.assistants.models.OpenAIPageableListOfAssistantFile;
import com.azure.ai.openai.assistants.models.UpdateAssistantOptions;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Disabled;
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

public class AzureAssistantsAsyncTest extends AssistantsClientTestBase {
    private AssistantsAsyncClient client;

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void assistantCrud(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getAssistantsAsyncClient(httpClient);
        createAssistantsRunner(assistantCreationOptions -> {
            AtomicReference<Assistant> assistantCreated = new AtomicReference<>();
            // create assistant test
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
    public void assistantCrudWithResponse(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
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
    public void listAssistants(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getAssistantsAsyncClient(httpClient, serviceVersion);
        createAssistantsRunner(assistantCreationOptions -> {
            AtomicReference<Assistant> assistantCreated1 = new AtomicReference<>();
            AtomicReference<Assistant> assistantCreated2 = new AtomicReference<>();
            // create two assistants
            StepVerifier.create(client.createAssistant(assistantCreationOptions.setName("assistant1")))
                    .assertNext(assistant -> {
                        assistantCreated1.set(assistant);
                        assertEquals(assistantCreationOptions.getName(), assistant.getName());
                        assertEquals(assistantCreationOptions.getDescription(), assistant.getDescription());
                        assertEquals(assistantCreationOptions.getInstructions(), assistant.getInstructions());
                    })
                    .verifyComplete();
            Assistant assistant1 = assistantCreated1.get();
            String assistantId1 = assistant1.getId();

            StepVerifier.create(client.createAssistant(assistantCreationOptions.setName("assistant2")))
                    .assertNext(assistant -> {
                        assistantCreated2.set(assistant);
                        assertEquals(assistantCreationOptions.getName(), assistant.getName());
                        assertEquals(assistantCreationOptions.getDescription(), assistant.getDescription());
                        assertEquals(assistantCreationOptions.getInstructions(), assistant.getInstructions());
                    })
                    .verifyComplete();
            Assistant assistant2 = assistantCreated2.get();
            String assistantId2 = assistant2.getId();

            StepVerifier.create(client.listAssistants())
                    .assertNext(assistantsAscending -> {
                        List<Assistant> dataAscending = assistantsAscending.getData();
                        assertTrue(dataAscending.size() >= 2);
                    })
                    .verifyComplete();
            // With response method
            StepVerifier.create(client.listAssistantsWithResponse(new RequestOptions()))
                    .assertNext(response -> {
                        OpenAIPageableListOfAssistant assistantsAscending = assertAndGetValueFromResponse(response,
                                OpenAIPageableListOfAssistant.class, 200);
                        List<Assistant> dataAscending = assistantsAscending.getData();
                        assertTrue(dataAscending.size() >= 2);
                    })
                    .verifyComplete();

            // Deleted created assistants
            StepVerifier.create(client.deleteAssistant(assistantId1))
                    .assertNext(assistantDeletionStatus -> {
                        assertEquals(assistantId1, assistantDeletionStatus.getId());
                        assertTrue(assistantDeletionStatus.isDeleted());
                    })
                    .verifyComplete();
            StepVerifier.create(client.deleteAssistant(assistantId2))
                    .assertNext(assistantDeletionStatus -> {
                        assertEquals(assistantId2, assistantDeletionStatus.getId());
                        assertTrue(assistantDeletionStatus.isDeleted());
                    })
                    .verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void listAssistantsBetweenTwoAssistantId(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getAssistantsAsyncClient(httpClient, serviceVersion);
        createAssistantsRunner(assistantCreationOptions -> {
            // Create assistants
            AtomicReference<Assistant> assistantCreated1 = new AtomicReference<>();
            AtomicReference<Assistant> assistantCreated2 = new AtomicReference<>();
            AtomicReference<Assistant> assistantCreated3 = new AtomicReference<>();
            AtomicReference<Assistant> assistantCreated4 = new AtomicReference<>();
            // create two assistants
            StepVerifier.create(client.createAssistant(assistantCreationOptions.setName("assistant1")))
                    .assertNext(assistant -> {
                        assistantCreated1.set(assistant);
                        assertEquals(assistantCreationOptions.getName(), assistant.getName());
                        assertEquals(assistantCreationOptions.getDescription(), assistant.getDescription());
                        assertEquals(assistantCreationOptions.getInstructions(), assistant.getInstructions());
                    })
                    .verifyComplete();
            Assistant assistant1 = assistantCreated1.get();
            String assistantId1 = assistant1.getId();

            StepVerifier.create(client.createAssistant(assistantCreationOptions.setName("assistant2")))
                    .assertNext(assistant -> {
                        assistantCreated2.set(assistant);
                        assertEquals(assistantCreationOptions.getName(), assistant.getName());
                        assertEquals(assistantCreationOptions.getDescription(), assistant.getDescription());
                        assertEquals(assistantCreationOptions.getInstructions(), assistant.getInstructions());
                    })
                    .verifyComplete();
            Assistant assistant2 = assistantCreated2.get();
            String assistantId2 = assistant2.getId();

            StepVerifier.create(client.createAssistant(assistantCreationOptions.setName("assistant3")))
                    .assertNext(assistant -> {
                        assistantCreated3.set(assistant);
                        assertEquals(assistantCreationOptions.getName(), assistant.getName());
                        assertEquals(assistantCreationOptions.getDescription(), assistant.getDescription());
                        assertEquals(assistantCreationOptions.getInstructions(), assistant.getInstructions());
                    })
                    .verifyComplete();
            Assistant assistant3 = assistantCreated3.get();
            String assistantId3 = assistant3.getId();

            StepVerifier.create(client.createAssistant(assistantCreationOptions.setName("assistant4")))
                    .assertNext(assistant -> {
                        assistantCreated4.set(assistant);
                        assertEquals(assistantCreationOptions.getName(), assistant.getName());
                        assertEquals(assistantCreationOptions.getDescription(), assistant.getDescription());
                        assertEquals(assistantCreationOptions.getInstructions(), assistant.getInstructions());
                    })
                    .verifyComplete();
            Assistant assistant4 = assistantCreated4.get();
            String assistantId4 = assistant4.getId();

            // List only the middle two assistants; sort by name ascending
            StepVerifier.create(client.listAssistants(100, ListSortOrder.ASCENDING, assistant1.getId(),
                            assistant4.getId()))
                    .assertNext(assistantsAscending -> {
                        List<Assistant> dataAscending = assistantsAscending.getData();
                        assertEquals(2, dataAscending.size());
                        assertEquals(assistant2.getId(), dataAscending.get(0).getId());
                        assertEquals(assistant3.getId(), dataAscending.get(1).getId());
                    })
                    .verifyComplete();


            // List only the middle two assistants; sort by name descending
            StepVerifier.create(client.listAssistants(100,
                            ListSortOrder.DESCENDING, assistant4.getId(), assistant1.getId()))
                    .assertNext(assistantsDescending -> {
                        List<Assistant> dataDescending = assistantsDescending.getData();
                        assertEquals(2, dataDescending.size());
                        assertEquals(assistant3.getId(), dataDescending.get(0).getId());
                        assertEquals(assistant2.getId(), dataDescending.get(1).getId());
                    })
                    .verifyComplete();


            // Delete the created assistants
            // Deleted created assistants
            StepVerifier.create(client.deleteAssistant(assistantId1))
                    .assertNext(assistantDeletionStatus -> {
                        assertEquals(assistantId1, assistantDeletionStatus.getId());
                        assertTrue(assistantDeletionStatus.isDeleted());
                    })
                    .verifyComplete();
            StepVerifier.create(client.deleteAssistant(assistantId2))
                    .assertNext(assistantDeletionStatus -> {
                        assertEquals(assistantId2, assistantDeletionStatus.getId());
                        assertTrue(assistantDeletionStatus.isDeleted());
                    })
                    .verifyComplete();
            StepVerifier.create(client.deleteAssistant(assistantId3))
                    .assertNext(assistantDeletionStatus -> {
                        assertEquals(assistantId3, assistantDeletionStatus.getId());
                        assertTrue(assistantDeletionStatus.isDeleted());
                    })
                    .verifyComplete();
            StepVerifier.create(client.deleteAssistant(assistantId4))
                    .assertNext(assistantDeletionStatus -> {
                        assertEquals(assistantId4, assistantDeletionStatus.getId());
                        assertTrue(assistantDeletionStatus.isDeleted());
                    })
                    .verifyComplete();
        });
    }

    @Disabled("Cannot use the hard-coded file id in the test")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void assistantFileCrd(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getAssistantsAsyncClient(httpClient, serviceVersion);
        createAssistantsFileRunner((assistantCreationOptions, fileId) -> {
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

            // Deleted created assistant
            StepVerifier.create(client.deleteAssistant(assistantId))
                    .assertNext(assistantDeletionStatus -> {
                        assertEquals(assistantId, assistantDeletionStatus.getId());
                        assertTrue(assistantDeletionStatus.isDeleted());
                    })
                    .verifyComplete();
        });
    }

    @Disabled("Cannot use the hard-coded file id in the test")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void assistantFileCrdWithResponse(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getAssistantsAsyncClient(httpClient, serviceVersion);
        createAssistantsFileRunner((assistantCreationOptions, fileId) -> {
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

            // Deleted created assistant
            StepVerifier.create(client.deleteAssistant(assistantId))
                    .assertNext(assistantDeletionStatus -> {
                        assertEquals(assistantId, assistantDeletionStatus.getId());
                        assertTrue(assistantDeletionStatus.isDeleted());
                    })
                    .verifyComplete();
        });
    }

    @Disabled("Cannot use the hard-coded file id in the test")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void listAssistantFiles(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getAssistantsAsyncClient(httpClient, serviceVersion);
        createAssistantsFileRunner((assistantCreationOptions, fileId) -> {
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
                        OpenAIPageableListOfAssistantFile assistantFileList = assertAndGetValueFromResponse(response,
                                OpenAIPageableListOfAssistantFile.class, 200);
                        List<AssistantFile> assistantFilesData = assistantFileList.getData();
                        assertEquals(1, assistantFilesData.size());
                        AssistantFile assistantFileOnly = assistantFilesData.get(0);
                        assertEquals(assistantId, assistantFileOnly.getAssistantId());
                        assertEquals("assistant.file", assistantFileOnly.getObject());
                        assertEquals(fileId, assistantFileOnly.getId());
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

    @Disabled("Cannot use the hard-coded file id in the test")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void listAssistantFilesAddSameFile(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getAssistantsAsyncClient(httpClient, serviceVersion);
        createAssistantsFileRunner((assistantCreationOptions, fileId) -> {
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

            // Deleted created assistant
            StepVerifier.create(client.deleteAssistant(assistantId))
                    .assertNext(assistantDeletionStatus -> {
                        assertEquals(assistantId, assistantDeletionStatus.getId());
                        assertTrue(assistantDeletionStatus.isDeleted());
                    })
                    .verifyComplete();
        });
    }

    @Disabled("Cannot use the hard-coded file id in the test")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void listAssistantFilesBetweenTwoAssistantId(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getAssistantsAsyncClient(httpClient, serviceVersion);
        createAssistantsFileRunner((assistantCreationOptions, fileId) -> {
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
            AtomicReference<AssistantFile> fileAtomicReference1 = new AtomicReference<>();
            AtomicReference<AssistantFile> fileAtomicReference2 = new AtomicReference<>();
            AtomicReference<AssistantFile> fileAtomicReference3 = new AtomicReference<>();
            AtomicReference<AssistantFile> fileAtomicReference4 = new AtomicReference<>();

            // Attach a file to the assistant created above and return the assistant file
            StepVerifier.create(client.createAssistantFile(assistantId, fileId))
                    .assertNext(assistantFile -> {
                        fileAtomicReference1.set(assistantFile);
                        assertNotNull(assistantFile.getCreatedAt());
                        assertEquals(assistantId, assistantFile.getAssistantId());
                        assertEquals("assistant.file", assistantFile.getObject());
                        assertEquals(fileId, assistantFile.getId());
                    })
                    .verifyComplete();

            StepVerifier.create(client.createAssistantFile(assistantId, "file-z8QKwkZbGZO3fSGjgvWKfeYj"))
                    .assertNext(assistantFile -> {
                        fileAtomicReference2.set(assistantFile);
                        assertNotNull(assistantFile.getCreatedAt());
                        assertEquals(assistantId, assistantFile.getAssistantId());
                        assertEquals("assistant.file", assistantFile.getObject());
                        assertEquals("file-z8QKwkZbGZO3fSGjgvWKfeYj", assistantFile.getId());
                    })
                    .verifyComplete();

            StepVerifier.create(client.createAssistantFile(assistantId, "file-CzfVqhPj4QUayCScgoJprooC"))
                    .assertNext(assistantFile -> {
                        fileAtomicReference3.set(assistantFile);
                        assertNotNull(assistantFile.getCreatedAt());
                        assertEquals(assistantId, assistantFile.getAssistantId());
                        assertEquals("assistant.file", assistantFile.getObject());
                        assertEquals("file-CzfVqhPj4QUayCScgoJprooC", assistantFile.getId());
                    })
                    .verifyComplete();

            StepVerifier.create(client.createAssistantFile(assistantId, "file-mXHN1is1lJUDcBj36Jms87dq"))
                    .assertNext(assistantFile -> {
                        fileAtomicReference4.set(assistantFile);
                        assertNotNull(assistantFile.getCreatedAt());
                        assertEquals(assistantId, assistantFile.getAssistantId());
                        assertEquals("assistant.file", assistantFile.getObject());
                        assertEquals("file-mXHN1is1lJUDcBj36Jms87dq", assistantFile.getId());
                    })
                    .verifyComplete();
            AssistantFile assistantFile1 = fileAtomicReference1.get();
            AssistantFile assistantFile2 = fileAtomicReference2.get();
            AssistantFile assistantFile3 = fileAtomicReference3.get();
            AssistantFile assistantFile4 = fileAtomicReference4.get();
            assertEquals(assistantId, assistantFile1.getAssistantId());

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

            // Deleted created assistant
            StepVerifier.create(client.deleteAssistant(assistantId))
                    .assertNext(assistantDeletionStatus -> {
                        assertEquals(assistantId, assistantDeletionStatus.getId());
                        assertTrue(assistantDeletionStatus.isDeleted());
                    })
                    .verifyComplete();
        });
    }
}
