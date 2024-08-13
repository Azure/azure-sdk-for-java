// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.assistants;

import com.azure.ai.openai.assistants.models.VectorStore;
import com.azure.ai.openai.assistants.models.VectorStoreAutoChunkingStrategyRequest;
import com.azure.ai.openai.assistants.models.VectorStoreExpirationPolicy;
import com.azure.ai.openai.assistants.models.VectorStoreFile;
import com.azure.ai.openai.assistants.models.VectorStoreFileBatch;
import com.azure.ai.openai.assistants.models.VectorStoreOptions;
import com.azure.ai.openai.assistants.models.VectorStoreStaticChunkingStrategyOptions;
import com.azure.ai.openai.assistants.models.VectorStoreStaticChunkingStrategyRequest;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.util.logging.ClientLogger;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.azure.ai.openai.assistants.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static com.azure.ai.openai.assistants.models.FilePurpose.ASSISTANTS;
import static com.azure.ai.openai.assistants.models.VectorStoreExpirationPolicyAnchor.LAST_ACTIVE_AT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VectorStoreAsyncTests extends VectorStoreTestBase {
    private static final ClientLogger LOGGER = new ClientLogger(VectorStoreSyncTests.class);

    private AssistantsAsyncClient client;
    private VectorStore vectorStore;
    private List<String> fileIds = new ArrayList<>();
    protected void beforeTest(HttpClient httpClient) {
        client = getAssistantsAsyncClient(httpClient);
        addFile(ALPHABET_FINANCIAL_STATEMENT);
        VectorStoreOptions vectorStoreOptions = new VectorStoreOptions()
                .setName("Financial Statements")
                .setExpiresAfter(new VectorStoreExpirationPolicy(LAST_ACTIVE_AT, 1));
        StepVerifier.create(client.createVectorStore(vectorStoreOptions))
                .assertNext(vectorStore -> {
                    this.vectorStore = vectorStore;
                    assertNotNull(vectorStore);
                    assertNotNull(vectorStore.getId());
                })
                .verifyComplete();
    }

    private void addFile(String fileId) {
        fileIds.add(uploadFileAsync(client, fileId, ASSISTANTS));
    }

    @Override
    protected void afterTest() {
        LOGGER.info("Cleaning up created resources.");
        // clean up the created vector store
        deleteVectorStoresAsync(client, vectorStore.getId());
        deleteFilesAsync(client, fileIds.toArray(new String[0]));
        LOGGER.info("Finished cleaning up resources.");
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void updateVectorStoreName(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        beforeTest(httpClient);
        modifyVectorStoreRunner(vectorStoreDetails -> {
            String vectorStoreId = vectorStore.getId();
            // Modify Vector Store
            StepVerifier.create(client.modifyVectorStore(vectorStoreId, vectorStoreDetails))
                    .assertNext(vectorStore -> {
                        assertNotNull(vectorStore);
                        assertEquals(vectorStoreId, vectorStore.getId());
                        assertEquals(vectorStoreDetails.getName(), vectorStore.getName());
                    })
                    .verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void getVectorStore(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        beforeTest(httpClient);
        String vectorStoreId = vectorStore.getId();
        // Get Vector Store
        StepVerifier.create(client.getVectorStore(vectorStoreId))
                .assertNext(vectorStore -> {
                    assertNotNull(vectorStore);
                    assertEquals(vectorStoreId, vectorStore.getId());
                })
                .verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void listVectorStore(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        beforeTest(httpClient);
        // List Vector Stores
        StepVerifier.create(client.listVectorStores())
                .assertNext(vectorStores -> {
                    assertNotNull(vectorStores);
                    assertFalse(vectorStores.getData().isEmpty());
                    vectorStores.getData().forEach(vectorStore -> {
                        assertNotNull(vectorStore.getId());
                        assertNotNull(vectorStore.getCreatedAt());
                    });
                })
                .verifyComplete();
    }

    // Vector Store with Files
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void createVectorStoreFile(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        beforeTest(httpClient);
        StepVerifier.create(client.createVectorStoreFile(vectorStore.getId(), fileIds.get(0)))
                .assertNext(this::assertVectorStoreFile)
                .verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void createVectorStoreFileWithAutoChunkingStrategy(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        beforeTest(httpClient);
        StepVerifier.create(client.createVectorStoreFile(vectorStore.getId(), fileIds.get(0),
                new VectorStoreAutoChunkingStrategyRequest()))
            .assertNext(vectorStoreFile -> {
                assertVectorStoreFile(vectorStoreFile);
                assertStaticChunkingStrategy(vectorStoreFile, 800, 400);
            })
            .verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void createVectorStoreFileWithStaticChunkingStrategy(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        beforeTest(httpClient);
        int maxChunkSizeTokens = 101;
        int chunkOverlapTokens = 50;
        StepVerifier.create(client.createVectorStoreFile(vectorStore.getId(), fileIds.get(0),
                new VectorStoreStaticChunkingStrategyRequest(
                    new VectorStoreStaticChunkingStrategyOptions(maxChunkSizeTokens, chunkOverlapTokens))))
            .assertNext(vectorStoreFile -> {
                assertVectorStoreFile(vectorStoreFile);
                assertStaticChunkingStrategy(vectorStoreFile, maxChunkSizeTokens, chunkOverlapTokens);
            })
            .verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void throwExceptionWhenOverrideExistChunkStrategy(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        beforeTest(httpClient);
        StepVerifier.create(client.createVectorStoreFile(vectorStore.getId(), fileIds.get(0),
                new VectorStoreAutoChunkingStrategyRequest()))
            .assertNext(this::assertVectorStoreFile)
            .verifyComplete();
        StepVerifier.create(client.createVectorStoreFile(vectorStore.getId(), fileIds.get(0),
                new VectorStoreStaticChunkingStrategyRequest(
                    new VectorStoreStaticChunkingStrategyOptions(101, 50))))
            .verifyErrorSatisfies(error -> assertInstanceOf(HttpResponseException.class, error));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void getVectorStoreFile(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        beforeTest(httpClient);
        String storeId = vectorStore.getId();
        String fileId = fileIds.get(0);
        VectorStoreFile vectorStoreFile = client.createVectorStoreFile(storeId, fileId).block();

        // Get Vector Store File
        StepVerifier.create(client.getVectorStoreFile(storeId, fileId))
                    .assertNext(vectorStoreFileResponse -> {
                        assertNotNull(vectorStoreFileResponse);
                        assertEquals(vectorStoreFile.getVectorStoreId(), vectorStoreFileResponse.getVectorStoreId());
                        assertEquals(vectorStoreFile.getId(), vectorStoreFileResponse.getId());
                        assertEquals(fileId, vectorStoreFileResponse.getId());
                    })
                    .verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void listVectorStoreFiles(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        beforeTest(httpClient);
        String storeId = vectorStore.getId();
        String fileId = fileIds.get(0);
        String fileId2 = uploadFileAsync(client, "20220924_aapl_10k.pdf", ASSISTANTS);
        fileIds.add(fileId2);
        VectorStoreFile vectorStoreFile = client.createVectorStoreFile(storeId, fileId).block();
        VectorStoreFile vectorStoreFile2 = client.createVectorStoreFile(storeId, fileId2).block();
        assertEquals(fileId, vectorStoreFile.getId());
        assertEquals(fileId2, vectorStoreFile2.getId());
        // List Vector Store Files
        StepVerifier.create(client.listVectorStoreFiles(storeId))
                .assertNext(vectorStoreFiles -> {
                    assertNotNull(vectorStoreFiles);
                    assertFalse(vectorStoreFiles.getData().isEmpty());
                    vectorStoreFiles.getData().forEach(storeFile -> {
                        assertNotNull(storeFile.getId());
                        assertNotNull(storeFile.getCreatedAt());
                    });
                })
                .verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void deleteVectorStoreFile(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        beforeTest(httpClient);
        String storeId = vectorStore.getId();
        String fileId = fileIds.get(0);
        // Create Vector Store File
        StepVerifier.create(client.createVectorStoreFile(vectorStore.getId(), fileIds.get(0)))
            .assertNext(this::assertVectorStoreFile)
            .verifyComplete();
        // Delete Vector Store File
        StepVerifier.create(client.deleteVectorStoreFile(storeId, fileId))
                .assertNext(deletionStatus -> {
                    assertTrue(deletionStatus.isDeleted());
                    assertEquals(fileId, deletionStatus.getId());
                })
                .verifyComplete();
    }

    // Vector Store File Batch
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void createVectorStoreFileBatch(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        beforeTest(httpClient);
        addFile(APPLE_FINANCIAL_STATEMENT);
        StepVerifier.create(client.createVectorStoreFileBatch(vectorStore.getId(),
                Arrays.asList(fileIds.get(0), fileIds.get(1))))
                .assertNext(vectorStoreFileBatch -> assertVectorStoreFileBatch(vectorStoreFileBatch, 2))
                .verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void createVectorStoreFileBatchWithAutoChunkingStrategy(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        beforeTest(httpClient);
        addFile(APPLE_FINANCIAL_STATEMENT);
        StepVerifier.create(client.createVectorStoreFileBatch(vectorStore.getId(),
                Arrays.asList(fileIds.get(0), fileIds.get(1)),
                new VectorStoreAutoChunkingStrategyRequest()))
            .assertNext(vectorStoreFileBatch -> assertVectorStoreFileBatch(vectorStoreFileBatch, 2))
            .verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void createVectorStoreFileBatchWithStaticChunkingStrategy(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        beforeTest(httpClient);
        addFile(APPLE_FINANCIAL_STATEMENT);
        StepVerifier.create(client.createVectorStoreFileBatch(vectorStore.getId(),
                Arrays.asList(fileIds.get(0), fileIds.get(1)),
                new VectorStoreStaticChunkingStrategyRequest(
                    new VectorStoreStaticChunkingStrategyOptions(101, 50))))
            .assertNext(vectorStoreFileBatch -> assertVectorStoreFileBatch(vectorStoreFileBatch, 2))
            .verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void throwExceptionWhenOverrideExistChunkStrategyInBatch(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        beforeTest(httpClient);
        addFile(APPLE_FINANCIAL_STATEMENT);

        StepVerifier.create(client.createVectorStoreFileBatch(vectorStore.getId(),
                Arrays.asList(fileIds.get(0), fileIds.get(1)),
                new VectorStoreStaticChunkingStrategyRequest(
                    new VectorStoreStaticChunkingStrategyOptions(101, 50))))
            .assertNext(vectorStoreFileBatch -> assertVectorStoreFileBatch(vectorStoreFileBatch, 2))
            .verifyComplete();

        StepVerifier.create(client.createVectorStoreFileBatch(vectorStore.getId(),
                Arrays.asList(fileIds.get(0), fileIds.get(1)),
                new VectorStoreAutoChunkingStrategyRequest()))
            .verifyErrorSatisfies(error -> assertInstanceOf(HttpResponseException.class, error));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void getVectorStoreFileBatch(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        beforeTest(httpClient);
        String storeId = vectorStore.getId();
        String fileId = fileIds.get(0);
        String fileId2 = uploadFileAsync(client, "20220924_aapl_10k.pdf", ASSISTANTS);
        fileIds.add(fileId2);
        VectorStoreFileBatch vectorStoreFileBatch = client.createVectorStoreFileBatch(storeId, Arrays.asList(fileId, fileId2)).block();
        String batchId = vectorStoreFileBatch.getId();
        int totalFileCounts = vectorStoreFileBatch.getFileCounts().getTotal();

        // Get Vector Store File
        StepVerifier.create(client.getVectorStoreFileBatch(storeId, batchId))
                .assertNext(vectorStoreFileBatchResponse -> {
                    assertNotNull(vectorStoreFileBatchResponse);
                    assertEquals(storeId, vectorStoreFileBatchResponse.getVectorStoreId());
                    assertEquals(batchId, vectorStoreFileBatchResponse.getId());
                    assertEquals(totalFileCounts, vectorStoreFileBatchResponse.getFileCounts().getTotal());
                })
                .verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void listVectorStoreFilesBatch(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        beforeTest(httpClient);
        String storeId = vectorStore.getId();
        String fileId = fileIds.get(0);
        String fileId2 = uploadFileAsync(client, "20220924_aapl_10k.pdf", ASSISTANTS);
        fileIds.add(fileId2);
        VectorStoreFileBatch vectorStoreFileBatch = client.createVectorStoreFileBatch(storeId, Arrays.asList(fileId, fileId2)).block();

        // List Vector Store Files
        StepVerifier.create(client.listVectorStoreFileBatchFiles(storeId, vectorStoreFileBatch.getId()))
                .assertNext(vectorStoreFiles -> {
                    assertNotNull(vectorStoreFiles);
                    assertFalse(vectorStoreFiles.getData().isEmpty());
                    vectorStoreFiles.getData().forEach(vectorStoreFile -> {
                        String fid = vectorStoreFile.getId();
                        assertNotNull(fid);
                        assertNotNull(vectorStoreFile.getCreatedAt());
                    });
                })
                .verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void cancelVectorStoreFileBatch(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        beforeTest(httpClient);
        String storeId = vectorStore.getId();
        String fileId = fileIds.get(0);
        String fileId2 = uploadFileAsync(client, "20220924_aapl_10k.pdf", ASSISTANTS);
        fileIds.add(fileId2);
        VectorStoreFileBatch vectorStoreFileBatch = client.createVectorStoreFileBatch(storeId, Arrays.asList(fileId, fileId2)).block();
        // Cancel Vector Store File
        StepVerifier.create(client.cancelVectorStoreFileBatch(storeId, vectorStoreFileBatch.getId()))
                .assertNext(cancelVectorStoreFileBatch -> {
                    assertNotNull(cancelVectorStoreFileBatch);
                    assertEquals(vectorStoreFileBatch.getId(), cancelVectorStoreFileBatch.getId());
                    assertEquals(vectorStoreFileBatch.getFileCounts().getTotal(), cancelVectorStoreFileBatch.getFileCounts().getTotal());
                })
                .verifyComplete();
    }
}
