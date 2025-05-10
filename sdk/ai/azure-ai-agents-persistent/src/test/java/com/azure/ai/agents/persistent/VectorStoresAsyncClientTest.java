// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.agents.persistent;

import com.azure.ai.agents.persistent.models.VectorStore;
import com.azure.core.http.HttpClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.azure.ai.agents.persistent.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VectorStoresAsyncClientTest extends ClientTestBase {

    private PersistentAgentsAdministrationClientBuilder clientBuilder;
    private VectorStoresAsyncClient vectorStoresAsyncClient;
    private List<VectorStore> vectorStores;

    private void setup(HttpClient httpClient) {
        clientBuilder = getClientBuilder(httpClient);
        vectorStoresAsyncClient = clientBuilder.buildVectorStoresAsyncClient();
        vectorStores = new ArrayList<>();
    }

    private Mono<VectorStore> createVectorStore(String vectorStoreName) {
        return vectorStoresAsyncClient.createVectorStore(null, vectorStoreName, null, null, null, null)
            .map(vectorStore -> {
                assertNotNull(vectorStore, "Vector store should not be null");
                assertNotNull(vectorStore.getId(), "Vector store ID should not be null");
                assertEquals(vectorStoreName, vectorStore.getName(), "Vector store name should match");
                vectorStores.add(vectorStore);
                return vectorStore;
            });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testCreateVectorStore(HttpClient httpClient) {
        setup(httpClient);
        String vectorStoreName = "test_create_vector_store_async";

        StepVerifier.create(createVectorStore(vectorStoreName)).assertNext(vectorStore -> {
            assertNotNull(vectorStore, "Vector store should not be null");
            assertEquals(vectorStoreName, vectorStore.getName(), "Vector store name should match");
        }).verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testGetVectorStore(HttpClient httpClient) {
        setup(httpClient);
        String vectorStoreName = "test_get_vector_store_async";

        StepVerifier
            .create(createVectorStore(vectorStoreName)
                .flatMap(createdStore -> vectorStoresAsyncClient.getVectorStore(createdStore.getId())))
            .assertNext(retrievedVectorStore -> {
                assertNotNull(retrievedVectorStore, "Retrieved vector store should not be null");
                assertEquals(vectorStoreName, retrievedVectorStore.getName(), "Vector store names should match");
            })
            .verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testModifyVectorStore(HttpClient httpClient) {
        setup(httpClient);
        String vectorStoreName = "test_modify_vector_store_async";
        String updatedName = vectorStoreName + "_updated";
        Map<String, String> metadata = new HashMap<>();
        metadata.put("environment", "test");

        StepVerifier
            .create(createVectorStore(vectorStoreName).flatMap(createdStore -> vectorStoresAsyncClient
                .modifyVectorStore(createdStore.getId(), updatedName, null, metadata)))
            .assertNext(modifiedVectorStore -> {
                assertNotNull(modifiedVectorStore, "Modified vector store should not be null");
                assertEquals(updatedName, modifiedVectorStore.getName(), "Vector store name should be updated");
                assertNotNull(modifiedVectorStore.getMetadata(), "Vector store metadata should not be null");
                assertEquals("test", modifiedVectorStore.getMetadata().get("environment"),
                    "Metadata environment value should match");
            })
            .verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testListVectorStores(HttpClient httpClient) {
        setup(httpClient);
        String vectorStoreName = "test_list_vector_store_async";

        StepVerifier
            .create(createVectorStore(vectorStoreName)
                .then(Mono.defer(() -> vectorStoresAsyncClient.listVectorStores().take(10).collectList())))
            .assertNext(vectorStoreList -> {
                assertNotNull(vectorStoreList, "Vector store list should not be null");
                assertTrue(vectorStoreList.size() > 0, "Vector store list should not be empty");
            })
            .verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testDeleteVectorStore(HttpClient httpClient) {
        setup(httpClient);
        String vectorStoreName = "test_delete_vector_store_async";

        StepVerifier.create(createVectorStore(vectorStoreName).flatMap(createdStore -> {
            return vectorStoresAsyncClient.deleteVectorStore(createdStore.getId()).doOnNext(status -> {
                // Remove from cleanup list since it's already deleted
                vectorStores.remove(createdStore);
            });
        })).assertNext(deletionStatus -> {
            assertNotNull(deletionStatus, "Deletion status should not be null");
            assertTrue(deletionStatus.isDeleted(), "Vector store should be marked as deleted");
        }).verifyComplete();
    }

    @AfterEach
    public void cleanup() {
        for (VectorStore vectorStore : new ArrayList<>(vectorStores)) {
            try {
                vectorStoresAsyncClient.deleteVectorStore(vectorStore.getId()).block();
                vectorStores.remove(vectorStore);
            } catch (Exception e) {
                System.out.println("Failed to clean up vector store: " + vectorStore.getName());
                System.out.println(e.getMessage());
            }
        }
    }
}
