// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.agents.persistent;

import com.azure.ai.agents.persistent.models.VectorStore;
import com.azure.ai.agents.persistent.models.VectorStoreDeletionStatus;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedIterable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.azure.ai.agents.persistent.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VectorStoresClientTest extends ClientTestBase {

    private PersistentAgentsAdministrationClientBuilder clientBuilder;
    private VectorStoresClient vectorStoresClient;
    private List<VectorStore> vectorStores;

    private void setup(HttpClient httpClient) {
        clientBuilder = getClientBuilder(httpClient);
        vectorStoresClient = clientBuilder.buildVectorStoresClient();
        vectorStores = new ArrayList<>();
    }

    private VectorStore createVectorStore(String vectorStoreName) {
        VectorStore vectorStore = vectorStoresClient.createVectorStore(null, vectorStoreName, null, null, null, null);
        assertNotNull(vectorStore, "Vector store should not be null");
        vectorStores.add(vectorStore);
        return vectorStore;
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testCreateVectorStore(HttpClient httpClient) {
        setup(httpClient);

        String vectorStoreName = "test_create_vector_store";
        VectorStore vectorStore = createVectorStore(vectorStoreName);

        assertNotNull(vectorStore.getId(), "Vector store ID should not be null");
        assertEquals(vectorStoreName, vectorStore.getName(), "Vector store name should match");
        vectorStores.add(vectorStore);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testGetVectorStore(HttpClient httpClient) {
        setup(httpClient);

        String vectorStoreName = "test_get_vector_store";
        VectorStore vectorStore = createVectorStore(vectorStoreName);
        VectorStore retrievedVectorStore = vectorStoresClient.getVectorStore(vectorStore.getId());

        assertNotNull(retrievedVectorStore, "Retrieved vector store should not be null");
        assertEquals(vectorStore.getId(), retrievedVectorStore.getId(), "Vector store IDs should match");
        assertEquals(vectorStore.getName(), retrievedVectorStore.getName(), "Vector store names should match");
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testModifyVectorStore(HttpClient httpClient) {
        setup(httpClient);

        String vectorStoreName = "test_modify_vector_store";
        VectorStore vectorStore = createVectorStore(vectorStoreName);

        // update vector store with a new name and metadata
        String updatedName = vectorStoreName + "_updated";
        Map<String, String> metadata = new HashMap<>();
        metadata.put("environment", "test");

        VectorStore modifiedVectorStore = vectorStoresClient.modifyVectorStore(vectorStore.getId(), updatedName, null, // not modifying expiration policy
            metadata);

        assertNotNull(modifiedVectorStore, "Modified vector store should not be null");
        assertEquals(updatedName, modifiedVectorStore.getName(), "Vector store name should be updated");
        assertNotNull(modifiedVectorStore.getMetadata(), "Vector store metadata should not be null");
        assertEquals("test", modifiedVectorStore.getMetadata().get("environment"),
            "Metadata environment value should match");
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testListVectorStores(HttpClient httpClient) {
        setup(httpClient);

        String vectorStoreName = "test_list_vector_store";
        VectorStore vectorStore = createVectorStore(vectorStoreName);

        // Retrieve the list of vector stores
        PagedIterable<VectorStore> vectorStoreList = vectorStoresClient.listVectorStores();
        assertNotNull(vectorStoreList, "Vector store list should not be null");
        assertTrue(vectorStoreList.stream().count() > 0, "Vector store list should not be empty");
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testDeleteVectorStore(HttpClient httpClient) {
        setup(httpClient);

        String vectorStoreName = "test_delete_vector_store";
        VectorStore vectorStore = createVectorStore(vectorStoreName);

        VectorStoreDeletionStatus deletionStatus = vectorStoresClient.deleteVectorStore(vectorStore.getId());
        assertNotNull(deletionStatus, "Deletion status should not be null");
        assertTrue(deletionStatus.isDeleted(), "Vector store should be marked as deleted");
    }

    @AfterEach
    public void cleanup() {
        for (VectorStore vectorStore : vectorStores) {
            try {
                VectorStoreDeletionStatus deletionStatus = vectorStoresClient.deleteVectorStore(vectorStore.getId());
            } catch (Exception e) {
                System.out.println("Failed to clean up vector store: " + vectorStore.getName());
                System.out.println(e.getMessage());
            }
        }
    }
}
