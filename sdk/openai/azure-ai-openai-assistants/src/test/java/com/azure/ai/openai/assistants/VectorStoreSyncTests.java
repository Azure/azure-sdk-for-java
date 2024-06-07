// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.assistants;

import com.azure.ai.openai.assistants.models.PageableList;
import com.azure.ai.openai.assistants.models.VectorStore;
import com.azure.ai.openai.assistants.models.VectorStoreExpirationPolicy;
import com.azure.ai.openai.assistants.models.VectorStoreFile;
import com.azure.ai.openai.assistants.models.VectorStoreFileBatch;
import com.azure.ai.openai.assistants.models.VectorStoreFileBatchStatus;
import com.azure.ai.openai.assistants.models.VectorStoreFileDeletionStatus;
import com.azure.ai.openai.assistants.models.VectorStoreFileStatus;
import com.azure.ai.openai.assistants.models.VectorStoreOptions;
import com.azure.core.http.HttpClient;
import com.azure.core.util.logging.ClientLogger;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.azure.ai.openai.assistants.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static com.azure.ai.openai.assistants.models.FilePurpose.ASSISTANTS;
import static com.azure.ai.openai.assistants.models.VectorStoreExpirationPolicyAnchor.LAST_ACTIVE_AT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VectorStoreSyncTests extends AssistantsClientTestBase {
    private static final ClientLogger LOGGER = new ClientLogger(VectorStoreSyncTests.class);

    private AssistantsClient client;
    private VectorStore vectorStore;
    private List<String> fileIds = new ArrayList<>();

    protected void beforeTest(HttpClient httpClient) {
        client = getAssistantsClient(httpClient);
        fileIds.add(uploadFile(client, "20210203_alphabet_10K.pdf", ASSISTANTS));
        VectorStoreOptions vectorStoreOptions = new VectorStoreOptions()
                .setName("Financial Statements")
                .setExpiresAfter(new VectorStoreExpirationPolicy(LAST_ACTIVE_AT, 1))
                .setFileIds(fileIds);
        vectorStore = client.createVectorStore(vectorStoreOptions);

        assertNotNull(vectorStore);
        assertNotNull(vectorStore.getId());
    }

    @Override
    protected void afterTest() {
        LOGGER.info("Cleaning up created resources.");
        // clean up the created vector store
        deleteVectorStores(client, vectorStore.getId());
        deleteFiles(client, fileIds.toArray(new String[0]));
        LOGGER.info("Finished cleaning up resources.");
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void updateVectorStoreName(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        beforeTest(httpClient);
        modifyVectorStoreRunner(vectorStoreDetails -> {
            String vectorStoreId = vectorStore.getId();
            // Modify Vector Store
            VectorStore vectorStore = client.modifyVectorStore(vectorStoreId, vectorStoreDetails);
            assertNotNull(vectorStore);
            assertEquals(vectorStoreId, vectorStore.getId());
            assertEquals(vectorStoreDetails.getName(), vectorStore.getName());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void getVectorStore(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        beforeTest(httpClient);
        String vectorStoreId = vectorStore.getId();
        // Get Vector Store
        VectorStore vectorStore = client.getVectorStore(vectorStoreId);
        assertNotNull(vectorStore);
        assertEquals(vectorStoreId, vectorStore.getId());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void listVectorStore(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        beforeTest(httpClient);
        // List Vector Stores
        PageableList<VectorStore> vectorStores = client.listVectorStores();
        assertNotNull(vectorStores);
        assertFalse(vectorStores.getData().isEmpty());
        vectorStores.getData().forEach(vectorStore -> {
            assertNotNull(vectorStore.getId());
            assertNotNull(vectorStore.getCreatedAt());
        });
    }

    // Vector Store with Files
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void createVectorStoreFile(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        beforeTest(httpClient);
        String storeId = vectorStore.getId();
        VectorStoreFile vectorStoreFile = client.createVectorStoreFile(storeId, fileIds.get(0));
        assertNotNull(vectorStoreFile);
        assertNotNull(vectorStoreFile.getId());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void getVectorStoreFile(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        beforeTest(httpClient);
        String storeId = vectorStore.getId();
        String fileId = fileIds.get(0);
        VectorStoreFile vectorStoreFile = client.createVectorStoreFile(storeId, fileId);

        VectorStoreFile vectorStoreFileResponse = client.getVectorStoreFile(storeId, fileId);
        // Get Vector Store File
        while (VectorStoreFileStatus.IN_PROGRESS == vectorStoreFileResponse.getStatus()) {
            vectorStoreFileResponse = client.getVectorStoreFile(storeId, fileId);
        }
        assertNotNull(vectorStoreFileResponse);
        assertEquals(vectorStoreFile.getVectorStoreId(), vectorStoreFileResponse.getVectorStoreId());
        assertEquals(vectorStoreFile.getId(), vectorStoreFileResponse.getId());
        assertEquals(fileId, vectorStoreFileResponse.getId());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void listVectorStoreFiles(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        beforeTest(httpClient);
        String storeId = vectorStore.getId();
        String fileId = fileIds.get(0);
        String fileId2 = uploadFile(client, "20220924_aapl_10k.pdf", ASSISTANTS);
        fileIds.add(fileId2);
        VectorStoreFile vectorStoreFile = client.createVectorStoreFile(storeId, fileId);
        VectorStoreFile vectorStoreFile2 = client.createVectorStoreFile(storeId, fileId2);
        assertEquals(fileId, vectorStoreFile.getId());
        assertEquals(fileId2, vectorStoreFile2.getId());

        // List Vector Store Files
        PageableList<VectorStoreFile> vectorStoreFiles = client.listVectorStoreFiles(storeId);
        assertNotNull(vectorStoreFiles);
        assertFalse(vectorStoreFiles.getData().isEmpty());
        vectorStoreFiles.getData().forEach(storeFile -> {
            assertNotNull(storeFile.getId());
            assertNotNull(storeFile.getCreatedAt());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void deleteVectorStoreFile(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        beforeTest(httpClient);
        String storeId = vectorStore.getId();
        String fileId = fileIds.get(0);

        // Delete Vector Store File
        VectorStoreFileDeletionStatus deletionStatus = client.deleteVectorStoreFile(storeId, fileId);
        assertTrue(deletionStatus.isDeleted());
        assertEquals(fileId, deletionStatus.getId());
    }

    // Vector Store File Batch
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void createVectorStoreFileBatch(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        beforeTest(httpClient);
        String storeId = vectorStore.getId();
        String fileId = fileIds.get(0);
        String fileId2 = uploadFile(client, "20220924_aapl_10k.pdf", ASSISTANTS);
        fileIds.add(fileId2);
        VectorStoreFileBatch vectorStoreFileBatch = client.createVectorStoreFileBatch(storeId, Arrays.asList(fileId, fileId2));
        assertNotNull(vectorStoreFileBatch);
        assertNotNull(vectorStoreFileBatch.getId());
        assertEquals(2, vectorStoreFileBatch.getFileCounts().getTotal());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void getVectorStoreFileBatch(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        beforeTest(httpClient);
        String storeId = vectorStore.getId();
        String fileId = fileIds.get(0);
        String fileId2 = uploadFile(client, "20220924_aapl_10k.pdf", ASSISTANTS);
        fileIds.add(fileId2);
        VectorStoreFileBatch vectorStoreFileBatch = client.createVectorStoreFileBatch(storeId, Arrays.asList(fileId, fileId2));
        String batchId = vectorStoreFileBatch.getId();
        int totalFileCounts = vectorStoreFileBatch.getFileCounts().getTotal();

        // Get Vector Store File
        VectorStoreFileBatch vectorStoreFileBatchResponse = client.getVectorStoreFileBatch(storeId, batchId);
        assertEquals(storeId, vectorStoreFileBatchResponse.getVectorStoreId());
        assertEquals(batchId, vectorStoreFileBatchResponse.getId());
        assertEquals(totalFileCounts, vectorStoreFileBatchResponse.getFileCounts().getTotal());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void listVectorStoreFilesBatch(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        beforeTest(httpClient);
        String storeId = vectorStore.getId();
        String fileId = fileIds.get(0);
        String fileId2 = uploadFile(client, "20220924_aapl_10k.pdf", ASSISTANTS);
        fileIds.add(fileId2);
        VectorStoreFileBatch vectorStoreFileBatch = client.createVectorStoreFileBatch(storeId, Arrays.asList(fileId, fileId2));

        // List Vector Store Files
        PageableList<VectorStoreFile> vectorStoreFiles = client.listVectorStoreFileBatchFiles(storeId, vectorStoreFileBatch.getId());
        assertNotNull(vectorStoreFiles);
        assertFalse(vectorStoreFiles.getData().isEmpty());
        vectorStoreFiles.getData().forEach(vectorStoreFile -> {
            assertNotNull(vectorStoreFile.getId());
            assertNotNull(vectorStoreFile.getCreatedAt());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void cancelVectorStoreFileBatch(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        beforeTest(httpClient);
        String storeId = vectorStore.getId();
        String fileId = fileIds.get(0);
        String fileId2 = uploadFile(client, "20220924_aapl_10k.pdf", ASSISTANTS);
        fileIds.add(fileId2);
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
    }
}
