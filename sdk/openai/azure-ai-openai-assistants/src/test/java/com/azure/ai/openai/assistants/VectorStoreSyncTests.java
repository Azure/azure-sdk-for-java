// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.assistants;

import com.azure.ai.openai.assistants.models.PageableList;
import com.azure.ai.openai.assistants.models.VectorStore;
import com.azure.ai.openai.assistants.models.VectorStoreDeletionStatus;
import com.azure.ai.openai.assistants.models.VectorStoreFile;
import com.azure.ai.openai.assistants.models.VectorStoreFileBatch;
import com.azure.ai.openai.assistants.models.VectorStoreFileBatchStatus;
import com.azure.ai.openai.assistants.models.VectorStoreFileDeletionStatus;
import com.azure.ai.openai.assistants.models.VectorStoreFileStatus;
import com.azure.core.http.HttpClient;
import com.azure.core.util.CoreUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;

import static com.azure.ai.openai.assistants.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static com.azure.ai.openai.assistants.models.FilePurpose.ASSISTANTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VectorStoreSyncTests extends AssistantsClientTestBase {
    private AssistantsClient client;

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void createVectorStore(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient);
        createVectorStoreRunner(vectorStoreDetails -> {
            VectorStore vectorStore = client.createVectorStore(vectorStoreDetails);
            assertNotNull(vectorStore);
            assertNotNull(vectorStore.getId());
            // clean up the created vector store
            deleteVectorStores(client, vectorStore.getId());
        }, client);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void updateVectorStoreName(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient);
        modifyVectorStoreRunner((vectorStoreId, vectorStoreDetails) -> {
            // Modify Vector Store
            VectorStore vectorStore = client.modifyVectorStore(vectorStoreId, vectorStoreDetails);
            assertNotNull(vectorStore);
            assertEquals(vectorStoreId, vectorStore.getId());
            assertEquals(vectorStoreDetails.getName(), vectorStore.getName());

            // clean up the created vector store
            deleteVectorStores(client, vectorStoreId);
        }, client);
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

            // clean up the created vector store
            deleteVectorStores(client, vectorStoreId);
        }, client);
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
        }, client);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void listVectorStore(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient);
        listVectorStoreRunner((store1, store2) -> {
            // List Vector Stores
            PageableList<VectorStore> vectorStores = client.listVectorStores();
            assertNotNull(vectorStores);
            assertFalse(vectorStores.getData().isEmpty());
            vectorStores.getData().forEach(vectorStore -> {
                assertNotNull(vectorStore.getId());
                assertNotNull(vectorStore.getCreatedAt());
            });

            // clean up the created vector stores
            deleteVectorStores(client, store1.getId(), store2.getId());
        }, client);
    }

    // Vector Store with Files
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void createVectorStoreFile(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient);
        createVectorStoreWithFileRunner((storeId, fileId) -> {
            VectorStoreFile vectorStoreFile = client.createVectorStoreFile(storeId, fileId);
            assertNotNull(vectorStoreFile);
            assertNotNull(vectorStoreFile.getId());
            // clean up the created vector store
            deleteVectorStores(client, storeId);
        }, client);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void getVectorStoreFile(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient);
        getVectorStoreFileRunner((vectorStoreFile, fileId) -> {
            String storeId = vectorStoreFile.getVectorStoreId();
            // Get Vector Store File
            while (VectorStoreFileStatus.IN_PROGRESS == vectorStoreFile.getStatus()) {
                vectorStoreFile = client.getVectorStoreFile(storeId, fileId);
            }
            assertNotNull(vectorStoreFile);
            assertEquals(fileId, vectorStoreFile.getId());

            // clean up the created vector store
            deleteVectorStores(client, storeId);
            client.deleteFile(fileId);
        }, client);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void listVectorStoreFiles(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient);
        listVectorStoreFilesRunner((vectorStoreFile1, vectorStoreFile2) -> {
            String storeId = vectorStoreFile1.getVectorStoreId();
            // List Vector Store Files
            PageableList<VectorStoreFile> vectorStoreFiles = client.listVectorStoreFiles(storeId);
            assertNotNull(vectorStoreFiles);
            assertFalse(vectorStoreFiles.getData().isEmpty());
            vectorStoreFiles.getData().forEach(vectorStoreFile -> {
                assertNotNull(vectorStoreFile.getId());
                assertNotNull(vectorStoreFile.getCreatedAt());
            });

            // clean up the created vector stores
            deleteVectorStores(client, storeId);
            client.deleteFile(vectorStoreFile1.getId());
            client.deleteFile(vectorStoreFile2.getId());
        }, client);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void deleteVectorStoreFile(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient);
        deleteVectorStoreFileRunner((vectorStoreFile, fileId) -> {
            String storeId = vectorStoreFile.getVectorStoreId();
            // Delete Vector Store File
            VectorStoreFileDeletionStatus deletionStatus = client.deleteVectorStoreFile(storeId, fileId);
            assertTrue(deletionStatus.isDeleted());
            assertEquals(deletionStatus.getId(), fileId);

            // clean up the created vector store
            deleteVectorStores(client, storeId);
        }, client);
    }

    // Vector Store File Batch
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void createVectorStoreFileBatch(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient);
        createVectorStoreWithFileBatchRunner((storeId, batchFiles) -> {
            VectorStoreFileBatch vectorStoreFileBatch = client.createVectorStoreFileBatch(storeId, batchFiles);
            assertNotNull(vectorStoreFileBatch);
            assertNotNull(vectorStoreFileBatch.getId());
            assertEquals(2, vectorStoreFileBatch.getFileCounts().getTotal());
            // clean up the created vector store
            deleteVectorStores(client, storeId);
        }, client);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void getVectorStoreFileBatch(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient);
        getVectorStoreFileBatchRunner(vectorStoreFileBatch -> {
            String storeId = vectorStoreFileBatch.getVectorStoreId();
            String batchId = vectorStoreFileBatch.getId();
            int totalFileCounts = vectorStoreFileBatch.getFileCounts().getTotal();
            // Get Vector Store File
            VectorStoreFileBatch vectorStoreFileBatchResponse = client.getVectorStoreFileBatch(storeId, vectorStoreFileBatch.getId());
            assertEquals(storeId, vectorStoreFileBatchResponse.getVectorStoreId());
            assertEquals(batchId, vectorStoreFileBatchResponse.getId());
            assertEquals(totalFileCounts, vectorStoreFileBatchResponse.getFileCounts().getTotal());
            // clean up the created vector store
            deleteVectorStores(client, storeId);
        }, client);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void listVectorStoreFilesBatch(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient);
        listVectorStoreFilesBatchFilesRunner((storeId, batchId) -> {
            // List Vector Store Files
            PageableList<VectorStoreFile> vectorStoreFiles = client.listVectorStoreFileBatchFiles(storeId, batchId);
            assertNotNull(vectorStoreFiles);
            assertFalse(vectorStoreFiles.getData().isEmpty());
            vectorStoreFiles.getData().forEach(vectorStoreFile -> {
                assertNotNull(vectorStoreFile.getId());
                assertNotNull(vectorStoreFile.getCreatedAt());
            });

            // clean up the created vector stores
            deleteVectorStores(client, storeId);
            client.deleteFile(vectorStoreFiles.getData().get(0).getId());
            client.deleteFile(vectorStoreFiles.getData().get(1).getId());
        }, client);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void cancelVectorStoreFileBatch(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient);
        cancelVectorStoreFileBatchRunner(vectorStore -> {
            String storeId = vectorStore.getId();
            String fileId = uploadFile(client, "20210203_alphabet_10K.pdf", ASSISTANTS);
            String fileId2 = uploadFile(client, "20220924_aapl_10k.pdf", ASSISTANTS);
            VectorStoreFileBatch vectorStoreFileBatch = client.createVectorStoreFileBatch(storeId, Arrays.asList(fileId, fileId2));
            // Cancel Vector Store File
            VectorStoreFileBatch cancelVectorStoreFileBatch = client.cancelVectorStoreFileBatch(storeId, vectorStoreFileBatch.getId());
            while (VectorStoreFileBatchStatus.IN_PROGRESS == cancelVectorStoreFileBatch.getStatus()) {
                cancelVectorStoreFileBatch = client.getVectorStoreFileBatch(storeId, vectorStoreFileBatch.getId());
            }
            assertNotNull(vectorStoreFileBatch);
            assertNotNull(cancelVectorStoreFileBatch);
            assertEquals(vectorStoreFileBatch.getId(), cancelVectorStoreFileBatch.getId());
            assertEquals(vectorStoreFileBatch.getFileCounts().getTotal(), cancelVectorStoreFileBatch.getFileCounts().getTotal());
            // TODO: investigate why the status is not CANCELLED but FAILED instead
//            assertEquals(VectorStoreFileStatus.CANCELLED, cancelVectorStoreFileBatch.getStatus());
            // clean up the created vector store
            deleteVectorStores(client, storeId);
        }, client);
    }

    private void deleteVectorStores(AssistantsClient client, String... vectorStoreIds) {
        if (!CoreUtils.isNullOrEmpty(vectorStoreIds)) {
            for (String vectorStoreId : vectorStoreIds) {
                client.deleteVectorStore(vectorStoreId);
            }
        }
    }
}
