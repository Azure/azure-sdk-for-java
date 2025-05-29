// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.agents.persistent;

import com.azure.ai.agents.persistent.implementation.models.FileDetails;
import com.azure.ai.agents.persistent.implementation.models.UploadFileRequest;
import com.azure.ai.agents.persistent.models.FileDeletionStatus;
import com.azure.ai.agents.persistent.models.FileInfo;
import com.azure.ai.agents.persistent.models.FilePurpose;
import com.azure.ai.agents.persistent.models.VectorStore;
import com.azure.ai.agents.persistent.models.VectorStoreFile;
import com.azure.ai.agents.persistent.models.VectorStoreFileBatch;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.azure.ai.agents.persistent.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static com.azure.ai.agents.persistent.TestUtils.size;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VectorStoreFileBatchesClientTest extends ClientTestBase {

    private PersistentAgentsAdministrationClientBuilder clientBuilder;
    private VectorStoresClient vectorStoresClient;
    private VectorStoreFileBatchesClient vectorStoreFileBatchesClient;
    private List<VectorStore> vectorStores = new ArrayList<>();
    private FilesClient filesClient;
    private List<FileInfo> uploadedFiles;

    private void setup(HttpClient httpClient) {
        clientBuilder = getClientBuilder(httpClient);
        vectorStoresClient = clientBuilder.buildVectorStoresClient();
        vectorStoreFileBatchesClient = clientBuilder.buildVectorStoreFileBatchesClient();
        filesClient = clientBuilder.buildFilesClient();
        uploadedFiles = new ArrayList<>();
    }

    private FileInfo uploadFile(String fileName) {
        FileDetails fileDetails
            = new FileDetails(BinaryData.fromString("Sample text for testing upload")).setFilename(fileName);
        UploadFileRequest uploadFileRequest = new UploadFileRequest(fileDetails, FilePurpose.AGENTS);
        FileInfo uploadedFile = filesClient.uploadFile(uploadFileRequest);
        assertNotNull(uploadedFile, "Uploaded file should not be null");
        uploadedFiles.add(uploadedFile);
        return uploadedFile;
    }

    // Helper method to create a vector store
    private VectorStore createVectorStore(String name) {
        VectorStore vectorStore = vectorStoresClient.createVectorStore(null, name, null, null, null, null);
        assertNotNull(vectorStore, "Vector store should not be null");
        vectorStores.add(vectorStore);
        return vectorStore;
    }

    private VectorStoreFileBatch createVectorStoreFileBatch(String vectorStoreId, List<String> fileIds) {
        VectorStoreFileBatch fileBatch
            = vectorStoreFileBatchesClient.createVectorStoreFileBatch(vectorStoreId, fileIds, null, null);
        assertNotNull(fileBatch, "Vector store file batch should not be null");
        return fileBatch;
    }

    // Test creation of a vector store file batch
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testCreateVectorStoreFileBatch(HttpClient httpClient) {
        setup(httpClient);

        String vectorStoreName = "test_create_vector_store_file_batch";
        VectorStore vectorStore = createVectorStore(vectorStoreName);

        FileInfo uploadedFile = uploadFile("testCreateVectorStoreFileBatch.txt");
        List<String> fileIds = Arrays.asList(uploadedFile.getId());
        VectorStoreFileBatch createdBatch = createVectorStoreFileBatch(vectorStore.getId(), fileIds);

        assertNotNull(createdBatch.getId(), "Vector store file batch ID should not be null");
    }

    // Test retrieval of a vector store file batch
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testGetVectorStoreFileBatch(HttpClient httpClient) {
        setup(httpClient);
        String vectorStoreName = "test_get_vector_store_file_batch";
        VectorStore vectorStore = createVectorStore(vectorStoreName);

        FileInfo uploadedFile = uploadFile("testGetVectorStoreFileBatch.txt");
        List<String> fileIds = Arrays.asList(uploadedFile.getId());
        VectorStoreFileBatch createdBatch = createVectorStoreFileBatch(vectorStore.getId(), fileIds);

        // Retrieve the file batch by its id.
        VectorStoreFileBatch retrievedBatch
            = vectorStoreFileBatchesClient.getVectorStoreFileBatch(vectorStore.getId(), createdBatch.getId());
        assertNotNull(retrievedBatch, "Retrieved file batch should not be null");
        assertEquals(createdBatch.getId(), retrievedBatch.getId(), "File batch IDs should match");
    }

    @Disabled
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testListVectorStoreFileBatches(HttpClient httpClient) {
        setup(httpClient);
        String vectorStoreName = "test_list_vector_store_file_batches";
        VectorStore vectorStore = createVectorStore(vectorStoreName);

        // Create multiple file batches
        FileInfo uploadedFile = uploadFile("testListVectorStoreFileBatches.txt");
        List<String> fileIds = Arrays.asList(uploadedFile.getId());
        VectorStoreFileBatch createdBatch = createVectorStoreFileBatch(vectorStore.getId(), fileIds);

        // List the file batches for the vector store.
        PagedIterable<VectorStoreFile> vectorStoreFiles
            = vectorStoreFileBatchesClient.listVectorStoreFileBatchFiles(vectorStore.getId(), createdBatch.getId());
        assertNotNull(vectorStoreFiles, "Vector store batch files list response should not be null");

        // At least the two created file batches should be present.
        assertTrue(size(vectorStoreFiles) > 0, "File batch list should not be empty");
    }

    @AfterEach
    public void cleanup() {
        // Clean up all created vector stores.
        for (VectorStore vectorStore : vectorStores) {
            try {
                vectorStoresClient.deleteVectorStore(vectorStore.getId());
            } catch (Exception e) {
                System.out.println("Failed to clean up vector store: " + vectorStore.getName());
                System.out.println(e.getMessage());
            }
        }
        for (FileInfo fileInfo : uploadedFiles) {
            try {
                FileDeletionStatus deletionStatus = filesClient.deleteFile(fileInfo.getId());
            } catch (Exception e) {
                System.out.println("Failed to clean up file: " + fileInfo.getFilename());
                System.out.println(e.getMessage());
            }
        }
    }
}
