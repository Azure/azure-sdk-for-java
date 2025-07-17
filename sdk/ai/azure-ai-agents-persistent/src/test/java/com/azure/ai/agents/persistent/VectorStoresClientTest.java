// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.agents.persistent;

import com.azure.ai.agents.persistent.models.FileDetails;
import com.azure.ai.agents.persistent.models.FileInfo;
import com.azure.ai.agents.persistent.models.FilePurpose;
import com.azure.ai.agents.persistent.models.UploadFileRequest;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.azure.ai.agents.persistent.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static com.azure.ai.agents.persistent.TestUtils.size;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class VectorStoresClientTest extends ClientTestBase {

    private PersistentAgentsClientBuilder clientBuilder;
    private VectorStoresClient vectorStoresClient;
    private List<VectorStore> vectorStores = new ArrayList<>();
    private FilesClient filesClient;
    private List<FileInfo> uploadedFiles = new ArrayList<>();
    private List<VectorStoreFile> vectorStoreFiles = new ArrayList<>();
    private VectorStore vectorStore;

    private void setup(HttpClient httpClient) {
        clientBuilder = getClientBuilder(httpClient);
        PersistentAgentsClient agentsClient = clientBuilder.buildClient();
        vectorStoresClient = agentsClient.getVectorStoresClient();
        filesClient = agentsClient.getFilesClient();
        vectorStore = createVectorStore("vectorStoresClientTest");
    }

    private VectorStore createVectorStore(String vectorStoreName) {
        VectorStore vectorStore = vectorStoresClient.createVectorStore(null, vectorStoreName, null, null, null, null);
        assertNotNull(vectorStore, "Vector store should not be null");
        vectorStores.add(vectorStore);
        return vectorStore;
    }

    private VectorStoreFileBatch createVectorStoreFileBatch(String vectorStoreId, List<String> fileIds) {
        VectorStoreFileBatch fileBatch
            = vectorStoresClient.createVectorStoreFileBatch(vectorStoreId, fileIds, null, null);
        assertNotNull(fileBatch, "Vector store file batch should not be null");
        return fileBatch;
    }

    // Helper method to upload a file using FilesClient.
    private FileInfo uploadFile(String fileName) {
        FileDetails fileDetails
            = new FileDetails(BinaryData.fromString("Sample text for vector store file upload")).setFilename(fileName);
        UploadFileRequest uploadFileRequest = new UploadFileRequest(fileDetails, FilePurpose.AGENTS);
        FileInfo uploadedFile = filesClient.uploadFile(uploadFileRequest);
        assertNotNull(uploadedFile, "Uploaded file should not be null");
        uploadedFiles.add(uploadedFile);
        return uploadedFile;
    }

    private VectorStoreFile createVectorStoreFile(String vectorStoreId, String fileId) {
        VectorStoreFile vectorStoreFile = vectorStoresClient.createVectorStoreFile(vectorStoreId, fileId, null, null);
        assertNotNull(vectorStoreFile, "Vector store file should not be null");

        vectorStoreFiles.add(vectorStoreFile);
        return vectorStoreFile;
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

        try {
            vectorStoresClient.deleteVectorStore(vectorStore.getId());
            assertTrue(true, "Vector store should be marked as deleted");
        } catch (Exception e) {
            fail("Failed to delete vector store: " + e.getMessage());
        }
    }

    // Test uploading a vector store file.
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testCreateVectorStoreFile(HttpClient httpClient) {
        setup(httpClient);

        FileInfo uploadedFile = uploadFile("create_vector_store_file.txt");
        VectorStoreFile vectorStoreFile = createVectorStoreFile(vectorStore.getId(), uploadedFile.getId());

        assertNotNull(vectorStoreFile.getId(), "Vector store file ID should not be null");
    }

    // Test retrieving a vector store file.
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testGetVectorStoreFile(HttpClient httpClient) {
        setup(httpClient);

        FileInfo uploadedFile = uploadFile("get_vector_store_file.txt");
        VectorStoreFile vectorStoreFile = createVectorStoreFile(vectorStore.getId(), uploadedFile.getId());

        VectorStoreFile retrievedFile
            = vectorStoresClient.getVectorStoreFile(vectorStore.getId(), vectorStoreFile.getId());
        assertNotNull(retrievedFile, "Retrieved vector store file should not be null");
        assertEquals(vectorStoreFile.getId(), retrievedFile.getId(), "Vector store file IDs should match");
    }

    // Test listing vector store files.
    @Disabled
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testListVectorStoreFiles(HttpClient httpClient) {
        setup(httpClient);

        FileInfo uploadedFile1 = uploadFile("list_vector_store_file1.txt");
        FileInfo uploadedFile2 = uploadFile("list_vector_store_file2.txt");

        // Upload two files.
        createVectorStoreFile(vectorStore.getId(), uploadedFile1.getId());
        createVectorStoreFile(vectorStore.getId(), uploadedFile2.getId());

        PagedIterable<VectorStoreFile> vectorStoreFiles = vectorStoresClient.listVectorStoreFiles(vectorStore.getId());

        assertNotNull(vectorStoreFiles, "Vector store files list should not be null");
        assertTrue(size(vectorStoreFiles) > 0, "There should be at least one vector store file");
    }

    // Test deleting a vector store file.
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testDeleteVectorStoreFile(HttpClient httpClient) {
        setup(httpClient);

        FileInfo uploadedFile = uploadFile("delete_vector_store_file.txt");
        VectorStoreFile vectorStoreFile = createVectorStoreFile(vectorStore.getId(), uploadedFile.getId());

        try {
            vectorStoresClient.deleteVectorStoreFile(vectorStore.getId(), vectorStoreFile.getId());
            assertTrue(true);
        } catch (Exception e) {
            fail("Failed to delete vector store file: " + e.getMessage());
        }
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
            = vectorStoresClient.getVectorStoreFileBatch(vectorStore.getId(), createdBatch.getId());
        assertNotNull(retrievedBatch, "Retrieved file batch should not be null");
        assertEquals(createdBatch.getId(), retrievedBatch.getId(), "File batch IDs should match");
    }

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
            = vectorStoresClient.listVectorStoreFileBatchFiles(vectorStore.getId(), createdBatch.getId());
        assertNotNull(vectorStoreFiles, "Vector store batch files list response should not be null");

        // At least the two created file batches should be present.
        assertTrue(size(vectorStoreFiles) > 0, "File batch list should not be empty");
    }

    @AfterEach
    public void cleanup() {
        for (VectorStore vectorStore : vectorStores) {
            try {
                vectorStoresClient.deleteVectorStore(vectorStore.getId());
            } catch (Exception e) {
                System.out.println("Failed to clean up vector store: " + vectorStore.getName());
                System.out.println(e.getMessage());
            }
        }
        // Clean up all uploaded files.
        for (FileInfo fileInfo : uploadedFiles) {
            try {
                filesClient.deleteFile(fileInfo.getId());
            } catch (Exception e) {
                System.out.println("Failed to clean up file: " + fileInfo.getFilename());
                System.out.println(e.getMessage());
            }
        }
    }
}
