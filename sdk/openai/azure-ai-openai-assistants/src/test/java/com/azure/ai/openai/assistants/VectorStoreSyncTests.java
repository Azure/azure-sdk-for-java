package com.azure.ai.openai.assistants;

import com.azure.ai.openai.assistants.models.PageableList;
import com.azure.ai.openai.assistants.models.VectorStore;
import com.azure.ai.openai.assistants.models.VectorStoreDeletionStatus;
import com.azure.ai.openai.assistants.models.VectorStoreOptions;
import com.azure.core.http.HttpClient;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

import static com.azure.ai.openai.assistants.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VectorStoreSyncTests extends AssistantsClientTestBase {
    private AssistantsClient client;

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void createEmptyVectorStore(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient);
        createVectorStoreRunner((vectorStoreDetails) -> {
            VectorStoreOptions vectorStoreOptions = new VectorStoreOptions();


            // Create Vector Store
            VectorStore vectorStore = client.createVectorStore(vectorStoreOptions);
            assertNotNull(vectorStore);
            assertNotNull(vectorStore.getId());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void createVectorStore(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
         client = getAssistantsClient(httpClient);
         createVectorStoreRunner((vectorStoreDetails) -> {
             // Create Vector Store
             VectorStore vectorStore = client.createVectorStore(vectorStoreDetails);
             assertNotNull(vectorStore);
             assertNotNull(vectorStore.getId());
         });
     }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void listVectorStore(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient);
        listVectorStoreRunner(() -> {
         // List Vector Stores
         PageableList<VectorStore> vectorStores = client.listVectorStores();
         assertNotNull(vectorStores);

         assertFalse(vectorStores.getData().isEmpty());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void getVectorStore(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient);
        getVectorStoreRunner((vectorStoreId) -> {
         // Get Vector Store
         VectorStore vectorStore = client.getVectorStore(vectorStoreId);
         assertNotNull(vectorStore);
         assertEquals(vectorStoreId, vectorStore.getId());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void modifyVectorStore(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient);
        modifyVectorStoreRunner((vectorStoreId, vectorStoreDetails) -> {
         // Modify Vector Store
         VectorStore vectorStore = client.modifyVectorStore(vectorStoreId, vectorStoreDetails);
         assertNotNull(vectorStore);
         assertEquals(vectorStoreId, vectorStore.getId());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void deleteVectorStore(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient);
        deleteVectorStoreRunner((vectorStoreId) -> {
         // Delete Vector Store
         VectorStoreDeletionStatus deletionStatus = client.deleteVectorStore(vectorStoreId);
         assertTrue(deletionStatus.isDeleted());
         assertEquals(deletionStatus.getId(), vectorStoreId);
        });
    }
}
